window.IndooneVault = (() => {
  const DB_KEY = 'indoone.vault.v1';
  const META_KEY = 'indoone.vault.meta.v1';

  const enc = new TextEncoder();
  const dec = new TextDecoder();

  function bytesToB64(bytes) {
    let s = '';
    for (const b of bytes) s += String.fromCharCode(b);
    return btoa(s);
  }

  function b64ToBytes(value) {
    const s = atob(value);
    const out = new Uint8Array(s.length);
    for (let i = 0; i < s.length; i++) out[i] = s.charCodeAt(i);
    return out;
  }

  async function deriveKey(pin, salt) {
    const material = await crypto.subtle.importKey('raw', enc.encode(pin), 'PBKDF2', false, ['deriveKey']);
    return crypto.subtle.deriveKey(
      { name: 'PBKDF2', salt, iterations: 250000, hash: 'SHA-256' },
      material,
      { name: 'AES-GCM', length: 256 },
      false,
      ['encrypt', 'decrypt']
    );
  }

  async function encryptPayload(payload, pin) {
    const salt = crypto.getRandomValues(new Uint8Array(16));
    const iv = crypto.getRandomValues(new Uint8Array(12));
    const key = await deriveKey(pin, salt);
    const ciphertext = new Uint8Array(await crypto.subtle.encrypt({ name: 'AES-GCM', iv }, key, enc.encode(JSON.stringify(payload))));
    return JSON.stringify({ v: 1, salt: bytesToB64(salt), iv: bytesToB64(iv), data: bytesToB64(ciphertext) });
  }

  async function decryptPayload(blob, pin) {
    const parsed = JSON.parse(blob);
    const key = await deriveKey(pin, b64ToBytes(parsed.salt));
    const plaintext = await crypto.subtle.decrypt({ name: 'AES-GCM', iv: b64ToBytes(parsed.iv) }, key, b64ToBytes(parsed.data));
    return JSON.parse(dec.decode(plaintext));
  }

  function getBlob() { return localStorage.getItem(DB_KEY); }
  function hasVault() { return !!getBlob(); }
  function clearVault() { localStorage.removeItem(DB_KEY); localStorage.removeItem(META_KEY); }

  async function save(accounts, pin) {
    if (!pin || String(pin).length < 4) throw new Error('PIN must be at least 4 digits');
    localStorage.setItem(DB_KEY, await encryptPayload({ accounts, updatedAt: new Date().toISOString() }, String(pin)));
    localStorage.setItem(META_KEY, JSON.stringify({ v: 1, created: localStorage.getItem(META_KEY) ? JSON.parse(localStorage.getItem(META_KEY)).created : new Date().toISOString() }));
  }

  async function load(pin) {
    const blob = getBlob();
    if (!blob) return null;
    return decryptPayload(blob, String(pin));
  }

  return { save, load, hasVault, clearVault };
})();
