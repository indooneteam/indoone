window.IndoonePersistence = (() => {
  const KEY = 'indoone.vault.blob.v1';
  let unlocked = false;

  async function save(accounts, pin) {
    await IndooneVault.save(accounts, pin);
    unlocked = true;
  }

  async function unlock(pin) {
    const payload = await IndooneVault.load(pin);
    if (!payload) return false;
    IndooneSecureSession.unlock(pin);
    unlocked = true;
    window.indooneState.accounts = payload.accounts || [];
    return true;
  }

  function isUnlocked() { return unlocked && !IndooneSecureSession.isLocked(); }

  function lock() {
    unlocked = false;
    IndooneSecureSession.lock();
    window.indooneState.accounts = [];
    if (typeof renderAccounts === 'function') renderAccounts();
  }

  async function persistCurrent() {
    if (!isUnlocked()) throw new Error('Vault is locked');
    await save(window.indooneState.accounts, IndooneSecureSession.getPin());
  }

  return { save, unlock, lock, isUnlocked, persistCurrent, hasVault: () => IndooneVault.hasVault(), clear: () => IndooneVault.clearVault(), storageKey: KEY };
})();
