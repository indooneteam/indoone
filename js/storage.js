window.IndooneStorage = (() => {
  const LEGACY_KEY = 'indoone.authenticator.accounts.v1';
  let unlocked = false;

  function loadLegacy() {
    try {
      const raw = localStorage.getItem(LEGACY_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch (_) {
      return null;
    }
  }

  async function unlock(pin) {
    const accounts = await IndooneEncryptedVault.decrypt(pin);
    if (!accounts) throw new Error('Vault not found');
    window.indooneState.accounts = accounts;
    unlocked = true;
    return accounts;
  }

  async function save(accounts, pin) {
    if (pin) {
      await IndooneEncryptedVault.encrypt(accounts, pin);
      unlocked = true;
    }
  }

  function isUnlocked() { return unlocked; }
  function lock() { unlocked = false; }
  function hasVault() { return IndooneEncryptedVault.exists(); }

  async function migrateLegacy(pin) {
    const legacy = loadLegacy();
    if (!Array.isArray(legacy) || !legacy.length) return false;
    await IndooneEncryptedVault.encrypt(legacy, pin);
    localStorage.removeItem(LEGACY_KEY);
    window.indooneState.accounts = legacy;
    unlocked = true;
    return true;
  }

  return {unlock, save, isUnlocked, lock, hasVault, migrateLegacy};
})();
