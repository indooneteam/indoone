window.IndooneCloudAccounts = (() => {
  const accountsPath = () => {
    const user = window.IndooneFirebase?.auth?.currentUser;
    if (!user) throw new Error('Please login first.');
    return window.IndooneFirebase.database.ref(`users/${user.uid}/accounts`);
  };

  const trashPath = () => {
    const user = window.IndooneFirebase?.auth?.currentUser;
    if (!user) throw new Error('Please login first.');
    return window.IndooneFirebase.database.ref(`users/${user.uid}/trash`);
  };

  const userPath = () => {
    const user = window.IndooneFirebase?.auth?.currentUser;
    if (!user) throw new Error('Please login first.');
    return window.IndooneFirebase.database.ref(`users/${user.uid}`);
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

  const createTrashItem = account => {
    const cleaned = cleanAccount(account);
    const deletedAt = Date.now();
    return {
      ...cleaned,
      deletedAt,
      purgeAt: deletedAt + (30 * 24 * 60 * 60 * 1000)
    };
  };

  function clearLegacyLocalVault() {
    localStorage.removeItem('indoone.vault.blob.v1');
    localStorage.removeItem('indoone.vault.meta.v1');
  }

  async function purgeExpiredTrash() {
    const snapshot = await trashPath().once('value');
    const value = snapshot.val() || {};
    const now = Date.now();
    const removals = [];

    Object.entries(value).forEach(([id, item]) => {
      if (!item) return;
      const purgeAt = Number(item.purgeAt || 0);
      if (purgeAt && purgeAt <= now) {
        removals.push(trashPath().child(id).remove());
      }
    });

    await Promise.all(removals);
    return removals.length;
  }

  let purgeTimer = null;
  function startTrashPurgeTimer() {
    if (purgeTimer) return;
    purgeTimer = setInterval(() => {
      purgeExpiredTrash().catch(() => {});
    }, 60 * 60 * 1000);
    purgeExpiredTrash().catch(() => {});
  }

  async function load() {
    clearLegacyLocalVault();
    await purgeExpiredTrash();
    startTrashPurgeTimer();

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

  async function moveToTrash(account) {
    const item = createTrashItem(account);
    await userPath().update({
      [`trash/${item.id}`]: item,
      [`accounts/${item.id}`]: null
    });
    return item;
  }

  async function listTrash() {
    await purgeExpiredTrash();
    const snapshot = await trashPath().once('value');
    const value = snapshot.val() || {};
    return Object.values(value)
      .filter(Boolean)
      .sort((a, b) => Number(b.deletedAt || 0) - Number(a.deletedAt || 0));
  }

  async function restoreFromTrash(id) {
    const key = String(Number(id));
    const ref = trashPath().child(key);
    const snapshot = await ref.once('value');
    const item = snapshot.val();
    if (!item) throw new Error('Trash item not found.');

    if (Number(item.purgeAt || 0) <= Date.now()) {
      await ref.remove();
      throw new Error('This account has expired from Trash.');
    }

    const account = cleanAccount(item);
    await userPath().update({
      [`accounts/${key}`]: account,
      [`trash/${key}`]: null
    });
    return account;
  }

  async function remove(id) {
    const account = (window.indooneState?.accounts || []).find(a => Number(a.id) === Number(id));
    if (!account) throw new Error('Account not found.');
    return moveToTrash(account);
  }

  return {
    load,
    save,
    saveAll,
    remove,
    moveToTrash,
    listTrash,
    restoreFromTrash,
    purgeExpiredTrash,
    clearLegacyLocalVault
  };
})();

IndooneCloudAccounts.clearLegacyLocalVault();
