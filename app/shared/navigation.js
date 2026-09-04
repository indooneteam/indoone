window.showFavorites = async function () {
  closeModal?.();
  try {
    if (window.IndooneCloudAccounts?.load) await window.IndooneCloudAccounts.load();
    const favorites = (window.indooneState?.accounts || []).filter(a => a.favorite);
    const rows = favorites.length
      ? favorites.map(a => `<button type="button" class="settings-row" style="width:100%;text-align:left;border:0;background:#fff;" onclick="closeModal();openAccount(${Number(a.id)})"><span>${a.name}<small>${a.email || 'Authenticator account'}</small></span><b>${a.code || '------'}</b></button>`).join('')
      : `<p style="text-align:center;padding:28px 0">No favorite accounts yet.</p>`;
    openModal(`<div class="modal-head"><h2>Favorites</h2><button class="close-btn" data-close>×</button></div><div id="favoritesList">${rows}</div><button class="primary" data-close>Done</button>`);
  } catch (error) {
    toast(error?.message || 'Could not load favorites');
  }
};

window.showTrash = function () {
  openModal(`<div class="modal-head"><h2>Trash</h2><button class="close-btn" data-close>×</button></div><p style="text-align:center;padding:28px 0">Deleted accounts are permanently removed from Firebase. Nothing is kept in Trash.</p><button class="primary" data-close>Done</button>`);
};

window.showSecurity = function () {
  openModal(`<h2>Security</h2><div class="settings-row"><span>Firebase account storage<small>Authenticator accounts synced to your user account</small></span><b>✓</b></div><div class="settings-row"><span>Local account storage<small>Not used for permanent account data</small></span><b>✓</b></div><button class="primary" data-close>Done</button>`);
};

window.showAbout = function () {
  openModal(`<h2>About Indoone</h2><p>Indoone Authenticator — a login-based cloud-synced TOTP authenticator.</p><button class="primary" data-close>Done</button>`);
};

// Keep Lock App packed with the menu items instead of pinning it to the bottom.
document.querySelector('.lock-item')?.style.setProperty('margin-top', '0');

// Keep a clear, consistent space between the Indoone brand and the menu button.
function applyTopbarBrandSpacing() {
  const topbarLeft = document.querySelector('.topbar-left');
  if (topbarLeft) topbarLeft.style.gap = '14px';
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', applyTopbarBrandSpacing, { once: true });
} else {
  applyTopbarBrandSpacing();
}
