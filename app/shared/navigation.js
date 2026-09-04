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

// Replace drawer Unicode glyphs with one consistent SVG icon family.
const DRAWER_ICONS = {
  accounts: '<rect x="4" y="4" width="6" height="6" rx="1.5"></rect><rect x="14" y="4" width="6" height="6" rx="1.5"></rect><rect x="4" y="14" width="6" height="6" rx="1.5"></rect><rect x="14" y="14" width="6" height="6" rx="1.5"></rect>',
  favorites: '<path d="m12 4 2.5 5.1 5.6.8-4 4 1 5.6-5.1-2.7-5.1 2.7 1-5.6-4-4 5.6-.8L12 4Z"></path>',
  trash: '<path d="M5 7h14M9 7V5h6v2M8 10v7M12 10v7M16 10v7M7 7l1 13h8l1-13"></path>',
  security: '<path d="M12 3.5 19 6v5.2c0 4.4-2.8 7.5-7 9.3-4.2-1.8-7-4.9-7-9.3V6l7-2.5Z"></path><path d="m9 12 2 2 4-4"></path>',
  about: '<circle cx="12" cy="12" r="8"></circle><path d="M12 10.5v5M12 7.5h.01"></path>',
  lock: '<rect x="5" y="10" width="14" height="10" rx="2"></rect><path d="M8 10V7a4 4 0 0 1 8 0v3"></path>',
  'danger-zone': '<path d="m12 4 9 16H3L12 4Z"></path><path d="M12 9v5M12 17h.01"></path>',
  logout: '<path d="M10 5H6a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h4M14 8l4 4-4 4M18 12H9"></path>'
};

function applyDrawerIcons() {
  document.querySelectorAll('.drawer-item[data-action]').forEach(item => {
    if (item.querySelector('.drawer-icon')) return;
    const action = item.dataset.action;
    const paths = DRAWER_ICONS[action];
    if (!paths) return;

    const icon = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    icon.setAttribute('class', 'drawer-icon');
    icon.setAttribute('viewBox', '0 0 24 24');
    icon.setAttribute('aria-hidden', 'true');
    icon.setAttribute('focusable', 'false');
    icon.setAttribute('width', '22');
    icon.setAttribute('height', '22');
    icon.setAttribute('fill', 'none');
    icon.setAttribute('stroke', 'currentColor');
    icon.setAttribute('stroke-width', '1.8');
    icon.setAttribute('stroke-linecap', 'round');
    icon.setAttribute('stroke-linejoin', 'round');
    icon.style.cssText = 'width:22px;height:22px;display:block;flex:none;';
    icon.innerHTML = paths;
    item.insertBefore(icon, item.firstChild);

    const firstText = Array.from(item.childNodes).find(node => node.nodeType === Node.TEXT_NODE && node.textContent.trim());
    if (firstText) firstText.remove();
  });
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => {
    applyTopbarBrandSpacing();
    applyDrawerIcons();
  }, { once: true });
} else {
  applyTopbarBrandSpacing();
  applyDrawerIcons();
}
