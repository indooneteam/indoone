(() => {
  const drawer = document.getElementById('drawer');
  const panel = drawer?.querySelector('.drawer-panel');
  const loadedScripts = new Set();
  const loadedStyles = new Set();
  const ASSET_VERSION = '20260907d';

  const featureInitializers = {
    accounts: 'initMenuAccounts', favorites: 'initMenuFavorites', trash: 'initMenuTrash', security: 'initMenuSecurity',
    'terms-of-use': 'initMenuTermsOfUse', 'privacy-policy': 'initMenuPrivacyPolicy', about: 'initMenuAbout',
    lock: 'initMenuLock', 'danger-zone': 'initMenuDangerZone', logout: 'initMenuLogout'
  };
  const nestedInitializers = {
    'danger-zone/delete-local-data': 'initMenuDeleteLocalData', 'danger-zone/delete-account': 'initMenuDeleteAccount',
    'logout/this-device': 'initMenuLogoutThisDevice', 'logout/all-devices': 'initMenuLogoutAllDevices'
  };
  function showOverlay() { document.getElementById('overlay')?.classList.remove('hidden'); }
  function hideOverlay() { document.getElementById('overlay')?.classList.add('hidden'); }
  function getFeatureBase(path) { return `app/menu/${path}`; }
  async function loadStyle(base) {
    if (loadedStyles.has(base)) return;
    const link = document.createElement('link'); link.rel = 'stylesheet';
    link.href = `${base}/style.css?v=${ASSET_VERSION}`; document.head.appendChild(link); loadedStyles.add(base);
  }
  async function loadScript(base) {
    const src = `${base}/script.js?v=${ASSET_VERSION}`;
    if (loadedScripts.has(src)) return;
    await new Promise((resolve, reject) => {
      const script = document.createElement('script'); script.src = src; script.onload = resolve;
      script.onerror = () => reject(new Error('Menu feature script could not be loaded.'));
      document.body.appendChild(script);
    });
    loadedScripts.add(src);
  }
  async function loadFeatureMarkup(base) {
    const response = await fetch(`${base}/index.html?v=${ASSET_VERSION}`, { cache: 'no-store' });
    if (!response.ok) throw new Error('Menu feature could not be loaded.');
    const modal = document.getElementById('modal');
    if (!modal) throw new Error('Menu modal is unavailable.');
    modal.innerHTML = await response.text();
  }
  async function openPath(path, initializerName) {
    closeDrawer();
    try {
      const base = getFeatureBase(path); await loadFeatureMarkup(base); await loadStyle(base); await loadScript(base);
      const initializer = window[initializerName];
      if (typeof initializer !== 'function') throw new Error('Menu feature initializer is unavailable.');
      const result = await initializer();
      if (result === false) hideOverlay(); else showOverlay();
    } catch (error) { hideOverlay(); window.toast?.(error?.message || 'Could not open menu item'); }
  }
  function createLegalMenuItem(type, label) {
    const item = document.createElement('button'); item.type = 'button'; item.className = 'drawer-item';
    item.dataset.action = type === 'terms' ? 'terms-of-use' : 'privacy-policy'; item.textContent = label; return item;
  }
  function ensureLegalMenuItems() {
    if (!panel) return;
    const securityItem = panel.querySelector('[data-action="security"]'); const aboutItem = panel.querySelector('[data-action="about"]');
    if (!securityItem || !aboutItem) return;
    if (!panel.querySelector('[data-action="terms-of-use"]')) securityItem.insertAdjacentElement('afterend', createLegalMenuItem('terms', 'Terms of Use'));
    if (!panel.querySelector('[data-action="privacy-policy"]')) panel.querySelector('[data-action="terms-of-use"]')?.insertAdjacentElement('afterend', createLegalMenuItem('privacy', 'Privacy Policy'));
  }
  window.toggleMenu = function () {
    if (!drawer) return; ensureLegalMenuItems(); const open = !drawer.classList.contains('open');
    drawer.classList.toggle('open', open); drawer.setAttribute('aria-hidden', String(!open));
  };
  window.closeDrawer = closeDrawer;
  function closeDrawer() { drawer?.classList.remove('open'); drawer?.setAttribute('aria-hidden', 'true'); }
  window.openMenuFeature = feature => { const name = featureInitializers[feature]; if (name) void openPath(feature, name); };
  window.openMenuNested = path => { const name = nestedInitializers[path]; if (name) void openPath(path, name); };
  panel?.addEventListener('click', event => {
    const item = event.target.closest('[data-action]'); if (!item || !panel.contains(item)) return;
    event.preventDefault(); event.stopPropagation(); const action = item.dataset.action;
    if (action === 'accounts') { closeDrawer(); document.getElementById('accountsNav')?.click(); return; }
    window.openMenuFeature(action);
  });
  drawer?.addEventListener('pointerdown', event => {
    if (!drawer.classList.contains('open') || (panel && panel.contains(event.target))) return; closeDrawer();
  });
})();
