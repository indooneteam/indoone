window.IndooneRecoveryPdf = (() => {
  const KEY = 'indoone.recovery.pdf.ready.v1';
  const enc = new TextEncoder();
  const dec = new TextDecoder();

  const b64 = bytes => {
    let s = '';
    new Uint8Array(bytes).forEach(b => s += String.fromCharCode(b));
    return btoa(s);
  };
  const unb64 = s => Uint8Array.from(atob(s), c => c.charCodeAt(0));

  async function derive(pin, salt) {
    const material = await crypto.subtle.importKey('raw', enc.encode(pin), 'PBKDF2', false, ['deriveKey']);
    return crypto.subtle.deriveKey({name:'PBKDF2',salt,iterations:250000,hash:'SHA-256'}, material, {name:'AES-GCM',length:256}, false, ['encrypt','decrypt']);
  }

  async function makePayload(accounts, pin) {
    const salt = crypto.getRandomValues(new Uint8Array(16));
    const iv = crypto.getRandomValues(new Uint8Array(12));
    const key = await derive(pin, salt);
    const plaintext = enc.encode(JSON.stringify({v:1,accounts,createdAt:new Date().toISOString()}));
    const cipher = await crypto.subtle.encrypt({name:'AES-GCM',iv}, key, plaintext);
    return JSON.stringify({format:'INDOONE-RECOVERY-1',salt:b64(salt),iv:b64(iv),data:b64(cipher)});
  }

  async function openPayload(payload, pin) {
    if (!payload || payload.format !== 'INDOONE-RECOVERY-1') throw new Error('Invalid recovery file');
    const key = await derive(pin, unb64(payload.salt));
    const clear = await crypto.subtle.decrypt({name:'AES-GCM',iv:unb64(payload.iv)}, key, unb64(payload.data));
    const result = JSON.parse(dec.decode(clear));
    if (!Array.isArray(result.accounts)) throw new Error('Invalid account data');
    return result.accounts;
  }

  function esc(text) {
    return String(text).replace(/\\/g,'\\\\').replace(/\\(/g,'\\(').replace(/\\)/g,'\\)');
  }

  function buildPdf(payloadText) {
    const lines = ['INDOONE AUTHENTICATOR','ENCRYPTED RECOVERY FILE','Keep this PDF private. A PIN is required to restore accounts.','RECOVERY_PAYLOAD_BEGIN'];
    for (let i=0;i<payloadText.length;i+=88) lines.push(payloadText.slice(i,i+88));
    lines.push('RECOVERY_PAYLOAD_END');
    let content = 'BT /F1 10 Tf 45 760 Td 14 TL ';
    lines.forEach((line,i)=>{content += `(${esc(line)}) Tj`; if(i<lines.length-1) content += ' T* ';});
    content += ' ET';
    const objects = [];
    objects.push('<< /Type /Catalog /Pages 2 0 R >>');
    objects.push('<< /Type /Pages /Kids [3 0 R] /Count 1 >>');
    objects.push('<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>');
    objects.push('<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>');
    objects.push(`<< /Length ${content.length} >>\\nstream\\n${content}\\nendstream`);
    let pdf = '%PDF-1.4\\n';
    const offsets = [0];
    objects.forEach((obj,i)=>{offsets.push(pdf.length); pdf += `${i+1} 0 obj\\n${obj}\\nendobj\\n`;});
    const xref = pdf.length;
    pdf += `xref\\n0 ${objects.length+1}\\n0000000000 65535 f \\n`;
    for(let i=1;i<offsets.length;i++) pdf += `${String(offsets[i]).padStart(10,'0')} 00000 n \\n`;
    pdf += `trailer << /Size ${objects.length+1} /Root 1 0 R >>\\nstartxref\\n${xref}\\n%%EOF`;
    return new Blob([pdf],{type:'application/pdf'});
  }

  function download(blob, filename='indoone-recovery.pdf') {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a'); a.href=url; a.download=filename; a.click();
    setTimeout(()=>URL.revokeObjectURL(url),1500);
  }

  async function create(accounts, pin) {
    const payload = await makePayload(accounts,pin);
    localStorage.setItem(KEY, JSON.stringify({createdAt:new Date().toISOString()}));
    download(buildPdf(payload));
    return true;
  }

  function isReady(){ return Boolean(localStorage.getItem(KEY)); }

  function extractPayload(text) {
    const match = text.match(/RECOVERY_PAYLOAD_BEGIN([\\s\\S]*?)RECOVERY_PAYLOAD_END/);
    if (!match) throw new Error('Recovery payload not found');
    return JSON.parse(match[1].replace(/[^A-Za-z0-9+/=]/g,''));
  }

  async function restoreFromFile(file, pin) {
    const raw = await file.arrayBuffer();
    const text = new TextDecoder().decode(raw);
    const payload = extractPayload(text);
    return openPayload(payload,pin);
  }

  async function verifyPin(pin) {
    const result = await IndooneVault.load(pin);
    return Boolean(result);
  }

  return {create,isReady,restoreFromFile,verifyPin};
})();
