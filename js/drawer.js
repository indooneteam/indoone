const drawerEl = document.getElementById('drawer');
const drawerPanel = drawerEl?.querySelector('.drawer-panel');

window.toggleMenu = function () {
  const open = drawerEl.classList.toggle('open');
  drawerEl.setAttribute('aria-hidden', String(!open));
};

window.closeDrawer = function () {
  drawerEl?.classList.remove('open');
  drawerEl?.setAttribute('aria-hidden', 'true');
};

function escapeHtml(value) {
  return String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

function daysLeft(purgeAt) {
  const ms = Math.max(0, Number(purgeAt || 0) - Date.now());
  return Math.max(1, Math.ceil(ms / (24 * 60 * 60 * 1000)));
}

function clearIndooneLocalData() {
  try {
    Object.keys(localStorage).forEach(key => {
      if (key.startsWith('indoone')) localStorage.removeItem(key);
    });
  } catch (_) {}

  try {
    Object.keys(sessionStorage).forEach(key => {
      if (key.startsWith('indoone')) sessionStorage.removeItem(key);
    });
  } catch (_) {}

  try { window.IndoonePersistence?.lock?.(); } catch (_) {}
  try { window.IndoonePersistence?.clear?.(); } catch (_) {}
  try { window.IndooneBiometric?.disable?.(); } catch (_) {}
}

function currentFirebaseUser() {
  return window.IndooneFirebase?.auth?.currentUser || null;
}

window.showFavorites = async function () {
  closeDrawer();
  try {
    const cloud = window.IndooneCloudAccounts;
    if (!cloud?.load) throw new Error('Account storage is unavailable.');
    await cloud.load();
    const favorites = (window.indooneState?.accounts || []).filter(account => account.favorite);

    const rows = favorites.length
      ? favorites.map(account => `
          <button type="button" class="settings-row favorite-list-row" data-favorite-account="${Number(account.id)}">
            <span>
              <b>${escapeHtml(account.name)}</b>
              <small>${escapeHtml(account.email || 'Authenticator account')}</small>
            </span>
            <b class="favorite-code">${escapeHtml(account.code || '------')}</b>
          </button>`).join('')
      : '<div class="empty-state compact-empty"><div class="empty-icon">☆</div><h3>No favorite accounts</h3><p>Star an account to see it here.</p></div>';

    openModal(`
      <div class="modal-head">
        <h2>Favorites</h2>
        <button type="button" class="close-btn" aria-label="Close Favorites" onclick="closeModal();return false;">×</button>
      </div>
      <div id="favoritesList" class="drawer-list-modal">${rows}</div>
    `);
  } catch (error) {
    toast(error?.message || 'Could not load favorites');
  }
};

window.showTrash = async function () {
  closeDrawer();
  try {
    const cloud = window.IndooneCloudAccounts;
    if (!cloud?.listTrash) throw new Error('Trash storage is unavailable.');
    const trash = await cloud.listTrash();

    const rows = trash.length
      ? trash.map(item => {
          const left = daysLeft(item.purgeAt);
          return `
            <div class="settings-row trash-list-row">
              <span>
                <b>${escapeHtml(item.name)}</b>
                <small>${escapeHtml(item.email || 'Authenticator account')} · ${left} day${left === 1 ? '' : 's'} left</small>
              </span>
              <button type="button" class="small-btn" data-trash-restore="${Number(item.id)}">Restore</button>
            </div>`;
        }).join('')
      : '<div class="empty-state compact-empty"><div class="empty-icon" style="font-size:27px">⌫</div><h3>Trash is empty</h3><p>Deleted accounts stay here for 30 days. After that, they are permanently removed.</p></div>';

    openModal(`
      <div class="modal-head">
        <div style="display:flex;align-items:center;gap:10px">
          <span class="brand-mark" style="width:36px;height:36px;border-radius:12px;font-size:18px">I</span>
          <div>
            <h2 style="margin:0">Trash</h2>
            <small style="display:block;margin-top:2px;color:#8a8492;font-size:10px;font-weight:600">Deleted accounts</small>
          </div>
        </div>
        <button type="button" class="close-btn" aria-label="Close Trash" onclick="closeModal();return false;">×</button>
      </div>
      <div id="trashList" class="drawer-list-modal">${rows}</div>
    `);
  } catch (error) {
    toast(error?.message || 'Could not load Trash');
  }
};

window.showSecurity = function () {
  closeDrawer();
  openModal(`
    <div class="modal-head"><h2>Security</h2><button class="close-btn" data-close>×</button></div>
    <div class="settings-row"><span>Cloud account storage<small>Authenticator accounts sync to your Indoone account</small></span><b>✓</b></div>
    <div class="settings-row"><span>Local account storage<small>Not used for permanent account data</small></span><b>✓</b></div>
  `);
};

window.showAbout = function () {
  closeDrawer();
  openModal(`
    <div class="modal-head"><h2>About Indoone</h2><button class="close-btn" data-close>×</button></div>
    <div class="token-icon">I</div>
    <p style="text-align:center"><b>Indoone Authenticator</b><br>Login-based cloud-synced TOTP authenticator.</p>
  `);
};

window.showDangerZone = function () {
  closeDrawer();
  openModal(`
    <div class="modal-head"><h2>Danger Zone</h2><button class="close-btn" data-close>×</button></div>
    <p>These actions can permanently remove Indoone data. Continue only when you are sure.</p>
    <button type="button" class="settings-row danger" style="width:100%;border:0;background:#fff;text-align:left" data-danger-delete-local onclick="confirmDeleteLocalData();return false;">
      <span>Delete local data<small>Remove data stored on this device</small></span><b>›</b>
    </button>
    <button type="button" class="settings-row danger" style="width:100%;border:0;background:#fff;text-align:left" data-danger-delete-account onclick="confirmDeleteIndooneAccount();return false;">
      <span>Delete Indoone account<small>Permanently delete your Indoone account and cloud data</small></span><b>›</b>
    </button>
  `);
};

window.confirmDeleteLocalData = function () {
  openModal(`
    <div class="modal-head"><h2>Delete local data?</h2><button class="close-btn" data-close>×</button></div>
    <p>This removes Indoone data stored on this device, including the encrypted vault and local sign-in markers. Your Indoone account and cloud data will not be deleted.</p>
    <button type="button" class="primary danger" id="confirmLocalDelete" onclick="executeDeleteLocalData();return false;">Delete local data</button>
    <button type="button" class="secondary" data-close>Cancel</button>
  `);
};

window.executeDeleteLocalData = async function () {
  try {
    clearIndooneLocalData();
    await window.IndooneFirebase?.auth?.signOut?.();
    closeModal();
    window.location.reload();
  } catch (error) {
    toast(error?.message || 'Could not delete local data');
  }
};

window.confirmDeleteIndooneAccount = function () {
  const user = currentFirebaseUser();
  if (!user) return toast('Please login first.');

  openModal(`
    <div class="modal-head"><h2>Delete Indoone account?</h2><button class="close-btn" data-close>×</button></div>
    <p>This permanently deletes your Indoone cloud data and Firebase account. This action cannot be undone.</p>
    <div class="field"><label>ACCOUNT PASSWORD</label><input id="deleteAccountPassword" type="password" autocomplete="current-password" placeholder="Enter your password"></div>
    <div class="field"><label>TYPE DELETE TO CONFIRM</label><input id="deleteAccountConfirm" type="text" autocomplete="off" placeholder="DELETE"></div>
    <button type="button" class="primary danger" id="confirmAccountDelete" onclick="executeDeleteIndooneAccount();return false;">Delete Indoone account</button>
    <button type="button" class="secondary" data-close>Cancel</button>
  `);
};

window.executeDeleteIndooneAccount = async function () {
  const password = String(document.getElementById('deleteAccountPassword')?.value || '');
  const confirmation = String(document.getElementById('deleteAccountConfirm')?.value || '').trim();
  if (!password) return toast('Enter your account password');
  if (confirmation !== 'DELETE') return toast('Type DELETE to confirm');

  const firebase = window.IndooneFirebase;
  const db = firebase?.database;
  const auth = firebase?.auth;
  const liveUser = auth?.currentUser;
  if (!db || !auth || !liveUser) return toast('Login session expired. Please login again.');
  if (!liveUser.email) return toast('This account cannot be re-authenticated here.');

  const button = document.getElementById('confirmAccountDelete');
  if (button) button.disabled = true;

  try {
    const credential = firebase.auth.EmailAuthProvider.credential(liveUser.email, password);
    await liveUser.reauthenticateWithCredential(credential);

    const profileSnapshot = await db.ref(`users/${liveUser.uid}/profile`).once('value');
    const profile = profileSnapshot.val() || {};
    const mobile = String(profile.mobile || '').trim();

    const updates = {
      [`users/${liveUser.uid}`]: null
    };
    if (mobile) updates[`mobileIndex/${encodeURIComponent(mobile)}`] = null;
    await db.ref().update(updates);

    await liveUser.delete();
    clearIndooneLocalData();
    closeModal();
    window.location.reload();
  } catch (error) {
    if (button) button.disabled = false;
    const code = error?.code || '';
    if (code === 'auth/wrong-password' || code === 'auth/invalid-credential') {
      toast('Incorrect account password');
    } else if (code === 'auth/requires-recent-login') {
      toast('Please login again, then retry account deletion');
    } else if (code === 'PERMISSION_DENIED') {
      toast('Could not remove cloud data. Check your connection and try again.');
    } else {
      toast(error?.message || 'Could not delete Indoone account');
    }
  }
};

drawerPanel?.addEventListener('click', event => {
  const item = event.target.closest('[data-action]');
  if (!item || !drawerPanel.contains(item)) return;
  event.preventDefault();
  event.stopPropagation();

  const action = item.dataset.action;
  if (action === 'accounts') {
    closeDrawer();
    document.getElementById('accountsNav')?.click();
  } else if (action === 'favorites') {
    void showFavorites();
  } else if (action === 'trash') {
    void showTrash();
  } else if (action === 'security') {
    showSecurity();
  } else if (action === 'about') {
    showAbout();
  } else if (action === 'danger-zone') {
    showDangerZone();
  } else if (action === 'logout') {
    closeDrawer();
    if (typeof window.showLogoutOptions === 'function') window.showLogoutOptions();
    else toast('Logout options are unavailable.');
  } else if (action === 'lock') {
    closeDrawer();
    lockIndoone();
  }
});

drawerEl?.addEventListener('pointerdown', event => {
  if (drawerEl.classList.contains('open') && drawerPanel && !drawerPanel.contains(event.target)) {
    closeDrawer();
  }
});

document.addEventListener('click', async event => {
  const favoriteRow = event.target.closest('[data-favorite-account]');
  if (favoriteRow) {
    const id = Number(favoriteRow.dataset.favoriteAccount);
    if (!id) return;
    closeModal();
    openAccount(id);
    return;
  }

  const restoreButton = event.target.closest('[data-trash-restore]');
  if (!restoreButton) return;
  const id = Number(restoreButton.dataset.trashRestore);
  if (!id) return;

  restoreButton.disabled = true;
  try {
    const restored = await window.IndooneCloudAccounts.restoreFromTrash(id);
    if (!window.indooneState.accounts.some(account => Number(account.id) === Number(restored.id))) {
      window.indooneState.accounts.push(restored);
    }
    renderAccounts();
    closeModal();
    toast('Account restored');
  } catch (error) {
    restoreButton.disabled = false;
    toast(error?.message || 'Could not restore account');
  }
});
