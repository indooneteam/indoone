window.IndooneCloudAccounts = (() => {
  const accountsPath = () => {
    const user = window.IndooneFirebase?.auth?.currentUser;
    if (!user) throw new Error('Please login first.');
    return window.IndooneFirebase.database.ref(`users/${user.uid}/accounts`);
  };

  const cleanAccount = account => ({
    id: Number(account.id),
    name: String(account.name || ''),
    email: String(account.email || ''),
    secret: String(account.secret || ''),
    digits: Number(account.digits || 6),
    period: Number(account.period || 30),
    algorithm: String(account.algorithm || 'SHA1'),
    favorite: !!account.favorite,
    icon: String(account.icon || String(account.name || '?').charAt(0).toUpperCase()),
    cls: String(account.cls || 'google'),
    updatedAt: Number(account.updatedAt || Date.now())
  });

  function clearLegacyLocalVault() {
    localStorage.removeItem('indoone.vault.blob.v1');
    localStorage.removeItem('indoone.vault.meta.v1');
  }

  async function load() {
    clearLegacyLocalVault();
    const snapshot = await accountsPath().once('value');
    const value = snapshot.val() || {};
    const accounts = Object.values(value).filter(Boolean).map(cleanAccount);
    accounts.sort((a, b) => b.id - a.id);
    window.indooneState.accounts = accounts;
    window.indooneState.trash = [];
    if (typeof renderAccounts === 'function') renderAccounts();
    if (typeof refreshAccountCodes === 'function') await refreshAccountCodes();
    return accounts;
  }

  async function save(account) {
    const cleaned = cleanAccount(account);
    await accountsPath().child(String(cleaned.id)).set(cleaned);
    return cleaned;
  }

  async function saveAll(accounts) {
    const payload = {};
    for (const account of accounts) {
      const cleaned = cleanAccount(account);
      payload[String(cleaned.id)] = cleaned;
    }
    await accountsPath().set(payload);
  }

  async function remove(id) {
    await accountsPath().child(String(Number(id))).remove();
  }

  return { load, save, saveAll, remove, clearLegacyLocalVault };
})();

IndooneCloudAccounts.clearLegacyLocalVault();
