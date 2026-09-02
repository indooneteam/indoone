window.IndooneCloudAccounts = (() => {
  const currentUser = () => {
    const user = window.IndooneFirebase?.auth?.currentUser;
    if (!user) throw new Error('Please login first.');
    return user;
  };

  const accountsPath = () => window.IndooneFirebase.database.ref(`users/${currentUser().uid}/accounts`);
  const trashPath = () => window.IndooneFirebase.database.ref(`users/${currentUser().uid}/trash`);
  const userPath = () => window.IndooneFirebase.database.ref(`users/${currentUser().uid}`);

  // Canonical account storage is keyed by the signed-in Firebase email so the
  // same account set is available after email/mobile login on another device.
  const identityEmail = () => String(currentUser().email || '').trim().toLowerCase();
  const identityKey = () => encodeURIComponent(identityEmail());
  const sharedAccountsPath = () => window.IndooneFirebase.database.ref(`accountVault/${identityKey()}/accounts`);
  const sharedTrashPath = () => window.IndooneFirebase.database.ref(`accountVault/${identityKey()}/trash`);

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
    const snapshots = await Promise.all([trashPath().once('value'), sharedTrashPath().once('value')]);
    const now = Date.now();
    const removals = [];
    for (const snapshot of snapshots) {
      const value = snapshot.val() || {};
      Object.entries(value).forEach(([id, item]) => {
        if (!item) return;
        const purgeAt = Number(item.purgeAt || 0);
        if (purgeAt && purgeAt <= now) {
          const ref = snapshot.ref.parent.key === 'accountVault' ? sharedTrashPath().child(id) : trashPath().child(id);
          removals.push(ref.remove());
        }
      });
    }
    await Promise.all(removals);
    return removals.length;
  }

  let purgeTimer = null;
  function startTrashPurgeTimer() {
    if (purgeTimer) return;
    purgeTimer = setInterval(() => purgeExpiredTrash().catch(() => {}), 60 * 60 * 1000);
    purgeExpiredTrash().catch(() => {});
  }

  async function load() {
    clearLegacyLocalVault();
    startTrashPurgeTimer();
    await purgeExpiredTrash();

    const [uidSnapshot, sharedSnapshot] = await Promise.all([
      accountsPath().once('value'),
      sharedAccountsPath().once('value')
    ]);

    const byId = new Map();
    for (const item of Object.values(uidSnapshot.val() || {})) {
      if (item) byId.set(Number(item.id), cleanAccount(item));
    }
    for (const item of Object.values(sharedSnapshot.val() || {})) {
      if (!item) continue;
      const cleaned = cleanAccount(item);
      const current = byId.get(cleaned.id);
      if (!current || cleaned.updatedAt >= current.updatedAt) byId.set(cleaned.id, cleaned);
    }

    const accounts = [...byId.values()].sort((a, b) => b.id - a.id);

    // Heal the old per-UID store into the canonical email store.
    const payload = {};
    accounts.forEach(account => { payload[String(account.id)] = account; });
    await sharedAccountsPath().set(payload);

    window.indooneState.accounts = accounts;
    window.indooneState.trash = [];
    if (typeof renderAccounts === 'function') renderAccounts();
    if (typeof refreshAccountCodes === 'function') await refreshAccountCodes();
    return accounts;
  }

  async function save(account) {
    const cleaned = cleanAccount(account);
    await Promise.all([
      accountsPath().child(String(cleaned.id)).set(cleaned),
      sharedAccountsPath().child(String(cleaned.id)).set(cleaned)
    ]);
    return cleaned;
  }

  async function saveAll(accounts) {
    const payload = {};
    for (const account of accounts) {
      const cleaned = cleanAccount(account);
      payload[String(cleaned.id)] = cleaned;
    }
    await Promise.all([accountsPath().set(payload), sharedAccountsPath().set(payload)]);
  }

  async function moveToTrash(account) {
    const item = createTrashItem(account);
    await Promise.all([
      userPath().update({ [`trash/${item.id}`]: item, [`accounts/${item.id}`]: null }),
      window.IndooneFirebase.database.ref(`accountVault/${identityKey()}`).update({
        [`trash/${item.id}`]: item,
        [`accounts/${item.id}`]: null
      })
    ]);
    return item;
  }

  async function listTrash() {
    await purgeExpiredTrash();
    const [uidSnapshot, sharedSnapshot] = await Promise.all([trashPath().once('value'), sharedTrashPath().once('value')]);
    const byId = new Map();
    for (const item of Object.values(uidSnapshot.val() || {})) if (item) byId.set(Number(item.id), item);
    for (const item of Object.values(sharedSnapshot.val() || {})) if (item) byId.set(Number(item.id), item);
    return [...byId.values()].sort((a, b) => Number(b.deletedAt || 0) - Number(a.deletedAt || 0));
  }

  async function restoreFromTrash(id) {
    const key = String(Number(id));
    const sources = [trashPath().child(key), sharedTrashPath().child(key)];
    let item = null;
    let source = null;
    for (const ref of sources) {
      const snapshot = await ref.once('value');
      if (snapshot.exists()) { item = snapshot.val(); source = ref; break; }
    }
    if (!item) throw new Error('Trash item not found.');
    if (Number(item.purgeAt || 0) <= Date.now()) {
      await Promise.all(sources.map(ref => ref.remove()));
      throw new Error('This account has expired from Trash.');
    }
    const account = cleanAccount(item);
    await Promise.all([
      accountsPath().child(key).set(account),
      sharedAccountsPath().child(key).set(account),
      source.remove(),
      trashPath().child(key).remove(),
      sharedTrashPath().child(key).remove()
    ]);
    return account;
  }

  async function remove(id) {
    const account = (window.indooneState?.accounts || []).find(a => Number(a.id) === Number(id));
    if (!account) throw new Error('Account not found.');
    return moveToTrash(account);
  }

  return { load, save, saveAll, remove, moveToTrash, listTrash, restoreFromTrash, purgeExpiredTrash, clearLegacyLocalVault };
})();

IndooneCloudAccounts.clearLegacyLocalVault();
