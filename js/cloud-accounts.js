window.IndooneCloudAccounts = (() => {
  const currentUser = () => {
    const user = window.IndooneFirebase?.auth?.currentUser;
    if (!user) throw new Error('Please login first.');
    return user;
  };

  const accountsPath = () => window.IndooneFirebase.database.ref(`users/${currentUser().uid}/accounts`);
  const trashPath = () => window.IndooneFirebase.database.ref(`users/${currentUser().uid}/trash`);
  const userPath = () => window.IndooneFirebase.database.ref(`users/${currentUser().uid}`);
  const usersByEmailPath = () => {
    const email = String(currentUser().email || '').trim().toLowerCase();
    return window.IndooneFirebase.database.ref('users').orderByChild('profile/email').equalTo(email);
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

  let purgeTimer = null;
  async function purgeExpiredTrash() {
    const snapshot = await trashPath().once('value');
    const value = snapshot.val() || {};
    const now = Date.now();
    const removals = [];
    Object.entries(value).forEach(([id, item]) => {
      if (!item) return;
      const purgeAt = Number(item.purgeAt || 0);
      if (purgeAt && purgeAt <= now) removals.push(trashPath().child(id).remove());
    });
    await Promise.all(removals);
    return removals.length;
  }

  function startTrashPurgeTimer() {
    if (purgeTimer) return;
    purgeTimer = setInterval(() => purgeExpiredTrash().catch(() => {}), 60 * 60 * 1000);
    purgeExpiredTrash().catch(() => {});
  }

  async function load() {
    clearLegacyLocalVault();
    startTrashPurgeTimer();

    const ownSnapshot = await accountsPath().once('value');
    const ownValue = ownSnapshot.val() || {};
    const byId = new Map();

    // Normal case: load the currently signed-in Firebase UID.
    Object.values(ownValue).forEach(item => {
      if (item) byId.set(Number(item.id), cleanAccount(item));
    });

    // Recovery/compatibility case: the same email may have legacy profile
    // records under another UID. Firebase rules allow this exact email query.
    try {
      const usersSnapshot = await usersByEmailPath().once('value');
      const users = usersSnapshot.val() || {};
      Object.values(users).forEach(userNode => {
        Object.values(userNode?.accounts || {}).forEach(item => {
          if (!item) return;
          const account = cleanAccount(item);
          const current = byId.get(account.id);
          if (!current || account.updatedAt >= current.updatedAt) byId.set(account.id, account);
        });
      });
    } catch (error) {
      console.warn('Indoone legacy account lookup skipped:', error);
    }

    const accounts = [...byId.values()].sort((a, b) => b.id - a.id);
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
