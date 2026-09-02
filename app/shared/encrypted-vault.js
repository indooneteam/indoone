window.IndooneEncryptedVault = (() => {
  const KEY = 'indoone.encrypted.vault.v1';
  const SALT_KEY = 'indoone.encrypted.vault.salt.v1';
  const ITERATIONS = 250000;

  const enc = new TextEncoder();
  const dec = new TextDecoder();

  function bytesToB64(bytes) {
    let s = '';
    bytes.forEach(b => s += String.fromCharCode(b));
    return btoa(s);
  }

  function b64ToBytes(value) {
    const s = atob(value);
    const out = new Uint8Array(s.length);
    for (let i = 0; i < s.length; i++) out[i] = s.charCodeAt(i);
    return out;
  }

  async function deriveKey(pin, salt) {
    const material = await crypto.subtle.importKey('raw', enc.encode(String(pin)), 'PBKDF2', false, ['deriveKey']);
    return crypto.subtle.deriveKey(
      {name:'PBKDF2', salt, iterations:ITERATIONS, hash:'SHA-256'},
      material,
      {name:'AES-GCM', length:256},
      false,
      ['encrypt','decrypt']
    );
  }

  async function encrypt(accounts, pin) {
    if (!pin || String(pin).length < 4) throw new Error('PIN must be at least 4 digits');
    let salt = localStorage.getItem(SALT_KEY);
    if (!salt) {
      const saltBytes = crypto.getRandomValues(new Uint8Array(16));
      salt = bytesToB64(saltBytes);
      localStorage.setItem(SALT_KEY, salt);
    }
    const iv = crypto.getRandomValues(new Uint8Array(12));
    const key = await deriveKey(pin, b64ToBytes(salt));
    const data = enc.encode(JSON.stringify(accounts));
    const ciphertext = await crypto.subtle.encrypt({name:'AES-GCM', iv}, key, data);
    const payload = JSON.stringify({v:1,salt,iv:bytesToB64(iv),data:bytesToB64(new Uint8Array(ciphertext))});
    localStorage.setItem(KEY, payload);
  }

  async function decrypt(pin) {
    const raw = localStorage.getItem(KEY);
    if (!raw) return null;
    const payload = JSON.parse(raw);
    const key = await deriveKey(pin, b64ToBytes(payload.salt));
    const clear = await crypto.subtle.decrypt({name:'AES-GCM', iv:b64ToBytes(payload.iv)}, key, b64ToBytes(payload.data));
    const accounts = JSON.parse(dec.decode(clear));
    if (!Array.isArray(accounts)) throw new Error('Invalid vault');
    return accounts;
  }

  function exists() { return Boolean(localStorage.getItem(KEY)); }
  function clear() { localStorage.removeItem(KEY); localStorage.removeItem(SALT_KEY); }
  return {encrypt, decrypt, exists, clear};
})();
