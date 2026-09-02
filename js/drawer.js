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
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

window.showFavorites = async function () {
  closeDrawer();
  try {
    const cloud = window.IndooneCloudAccounts;
    if (!cloud?.load) throw new Error('Firebase account storage is unavailable.');

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

window.showTrash = function () {
  closeDrawer();
  openModal(`
    <div class="modal-head">
      <h2>Trash</h2>
      <button type="button" class="close-btn" aria-label="Close Trash" onclick="closeModal();return false;">×</button>
    </div>
    <div class="empty-state compact-empty">
      <div class="empty-icon">♙</div>
      <h3>No accounts in trash</h3>
      <p>Deleted accounts are permanently removed from Firebase.</p>
    </div>
  `);
};

window.showSecurity = function () {
  closeDrawer();
  openModal(`
    <div class="modal-head"><h2>Security</h2><button class="close-btn" data-close>×</button></div>
    <div class="settings-row"><span>Firebase account storage<small>Authenticator accounts sync to your user account</small></span><b>✓</b></div>
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
    showTrash();
  } else if (action === 'security') {
    showSecurity();
  } else if (action === 'backup') {
    closeDrawer();
  } else if (action === 'settings') {
    closeDrawer();
    showSettings();
  } else if (action === 'about') {
    showAbout();
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

document.addEventListener('click', event => {
  const row = event.target.closest('[data-favorite-account]');
  if (!row) return;
  const id = Number(row.dataset.favoriteAccount);
  if (!id) return;
  closeModal();
  openAccount(id);
});
