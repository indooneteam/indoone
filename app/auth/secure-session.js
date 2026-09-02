window.IndooneSecureSession = (() => {
  let pin = null;
  let locked = true;
  return {
    unlock(value) { if (!/^\d{4,12}$/.test(String(value))) throw new Error('Invalid PIN'); pin = String(value); locked = false; return true; },
    lock() { pin = null; locked = true; },
    isLocked() { return locked; },
    getPin() { if (locked || !pin) throw new Error('Vault is locked'); return pin; }
  };
})();
