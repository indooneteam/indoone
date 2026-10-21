window.IndooneRecoveryPdf = (() => {
  const KEY = 'indoone.recovery.pdf.ready.v1';
  const enc = new TextEncoder();
  const dec = new TextDecoder();

  const b64 = bytes => {
    let s = '';

    new Uint8Array(bytes).forEach(byte => {
      s += String.fromCharCode(byte);
    });

    return btoa(s);
  };

  const unb64 = value =>
    Uint8Array.from(atob(value), char => char.charCodeAt(0));

  async function derive(pin, salt) {
    const material = await crypto.subtle.importKey(
      'raw',
      enc.encode(String(pin)),
      'PBKDF2',
      false,
      ['deriveKey']
    );

    return crypto.subtle.deriveKey(
      {
        name: 'PBKDF2',
        salt,
        iterations: 250000,
        hash: 'SHA-256'
      },
      material,
      {
        name: 'AES-GCM',
        length: 256
      },
      false,
      ['encrypt', 'decrypt']
    );
  }

  async function makePayload(accounts, pin) {
    const salt = crypto.getRandomValues(new Uint8Array(16));
    const iv = crypto.getRandomValues(new Uint8Array(12));
    const key = await derive(pin, salt);
    const clear = enc.encode(
      JSON.stringify({
        v: 1,
        accounts,
        createdAt: new Date().toISOString()
      })
    );
    const cipher = await crypto.subtle.encrypt(
      {
        name: 'AES-GCM',
        iv
      },
      key,
      clear
    );

    return JSON.stringify({
      format: 'INDOONE-RECOVERY-1',
      salt: b64(salt),
      iv: b64(iv),
      data: b64(cipher)
    });
  }

  async function openPayload(payload, pin) {
    if (!payload || payload.format !== 'INDOONE-RECOVERY-1') {
      throw new Error('Invalid recovery file');
    }

    const key = await derive(pin, unb64(payload.salt));
    const clear = await crypto.subtle.decrypt(
      {
        name: 'AES-GCM',
        iv: unb64(payload.iv)
      },
      key,
      unb64(payload.data)
    );
    const result = JSON.parse(dec.decode(clear));

    if (!Array.isArray(result.accounts)) {
      throw new Error('Invalid account data');
    }

    return result.accounts;
  }

  function esc(text) {
    return String(text)
      .replace(/\\/g, '\\\\')
      .replace(/\(/g, '\\(')
      .replace(/\)/g, '\\)');
  }

  function buildPdf(payload) {
    const lines = [
      'INDOONE AUTHENTICATOR',
      'ENCRYPTED RECOVERY FILE',
      'PIN REQUIRED TO RESTORE ACCOUNTS',
      'RECOVERY_PAYLOAD_BEGIN'
    ];

    for (let index = 0; index < payload.length; index += 90) {
      lines.push(payload.slice(index, index + 90));
    }

    lines.push('RECOVERY_PAYLOAD_END');

    let stream = 'BT /F1 9 Tf 40 800 Td 12 TL ';

    lines.forEach((line, index) => {
      stream += `(${esc(line)}) Tj`;

      if (index < lines.length - 1) {
        stream += ' T* ';
      }
    });

    stream += ' ET';

    const objects = [
      '<< /Type /Catalog /Pages 2 0 R >>',
      '<< /Type /Pages /Kids [3 0 R] /Count 1 >>',
      '<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>',
      '<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>',
      `<< /Length ${new TextEncoder().encode(stream).length} >>\nstream\n${stream}\nendstream`
    ];

    let pdf = '%PDF-1.4\n';
    const offsets = [0];

    objects.forEach((object, index) => {
      offsets.push(pdf.length);
      pdf += `${index + 1} 0 obj\n${object}\nendobj\n`;
    });

    const xref = pdf.length;

    pdf += `xref\n0 ${objects.length + 1}\n0000000000 65535 f \n`;

    for (let index = 1; index < offsets.length; index++) {
      pdf += `${String(offsets[index]).padStart(10, '0')} 00000 n \n`;
    }

    pdf += `trailer << /Size ${objects.length + 1} /Root 1 0 R >>\nstartxref\n${xref}\n%%EOF`;

    return new Blob([pdf], {
      type: 'application/pdf'
    });
  }

  function download(blob, filename = 'indoone-recovery.pdf') {
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement('a');

    anchor.href = url;
    anchor.download = filename;
    anchor.click();

    setTimeout(() => URL.revokeObjectURL(url), 1500);
  }

  async function create(accounts, pin) {
    const payload = await makePayload(accounts, pin);

    localStorage.setItem(
      KEY,
      JSON.stringify({
        createdAt: new Date().toISOString()
      })
    );

    download(buildPdf(payload));
    return true;
  }

  function isReady() {
    return Boolean(localStorage.getItem(KEY));
  }

  function clearReady() {
    localStorage.removeItem(KEY);
  }

  function verifyPin(value) {
    return Promise.resolve(
      window.IndooneSecureSession?.verifyPin?.(value) === true
    );
  }

  function extractPayload(text) {
    const match = text.match(
      /RECOVERY_PAYLOAD_BEGIN([\s\S]*?)RECOVERY_PAYLOAD_END/
    );

    if (!match) {
      throw new Error('Recovery payload not found');
    }

    return JSON.parse(match[1].replace(/[\r\n ]/g, ''));
  }

  async function restoreFromFile(file, pin) {
    const text = new TextDecoder().decode(
      await file.arrayBuffer()
    );

    return openPayload(extractPayload(text), pin);
  }

  return {
    create,
    isReady,
    clearReady,
    verifyPin,
    restoreFromFile
  };
})();
