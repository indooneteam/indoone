(() => {
  const drawerEl = document.getElementById('drawer');
  const drawerPanel = drawerEl?.querySelector('.drawer-panel');
  const loaded = new Set();
  const cssLoaded = new Set();

  window.toggleMenu = function () {
    if (!drawerEl) return;
    const open = drawerEl.classList.toggle('open');
    drawerEl.setAttribute('aria-hidden', String(!open));
  };

  window.closeDrawer = function () {
    drawerEl?.classList.remove('open');
    drawerEl?.setAttribute('aria-hidden', 'true');
  };

  async function openPath(path, initName) {
    const modal = document.getElementById('modal');
    if (!modal) return;
    try {
      const base = `app/menu/${path}`;
      const response = await fetch(`${base}/index.html?v=20260903c`, { cache: 'no-store' });
      if (!response.ok) throw new Error('Menu feature could not be loaded.');
      modal.innerHTML = await response.text();
      if (!cssLoaded.has(base)) {
        const link = document.createElement('link');
        link.rel = 'stylesheet';
        link.href = `${base}/style.css?v=20260903c`;
        document.head.appendChild(link);
        cssLoaded.add(base);
      }
      const src = `${base}/script.js?v=20260903c`;
      if (!loaded.has(src)) {
        await new Promise((resolve, reject) => {
          const script = document.createElement('script');
          script.src = src;
          script.onload = resolve;
          script.onerror = () => reject(new Error('Menu feature script could not be loaded.'));
          document.body.appendChild(script);
        });
        loaded.add(src);
      }
      const keepOverlayOpen = window[initName]?.();
      const overlay = document.getElementById('overlay');
      if (keepOverlayOpen === false) overlay?.classList.add('hidden');
      else overlay?.classList.remove('hidden');
    } catch (error) {
      toast(error?.message || 'Could not open menu item');
    }
  }

  window.openMenuFeature = function (feature) {
    const names = {
      favorites: 'initMenuFavorites',
      trash: 'initMenuTrash',
      security: 'initMenuSecurity',
      about: 'initMenuAbout',
      lock: 'initMenuLock',
      'danger-zone': 'initMenuDangerZone',
      logout: 'initMenuLogout'
    };
    const initName = names[feature];
    if (!initName) return;
    closeDrawer();
    void openPath(feature, initName);
  };

  window.openMenuNested = function (path) {
    const last = path.split('/').pop();
    const map = {
      'delete-local-data': 'initMenuDeleteLocalData',
      'delete-account': 'initMenuDeleteAccount',
      'this-device': 'initMenuLogoutThisDevice',
      'all-devices': 'initMenuLogoutAllDevices'
    };
    const initName = map[last];
    if (!initName) return;
    void openPath(path, initName);
  };

  drawerPanel?.addEventListener('click', event => {
    const item = event.target.closest('[data-action]');
    if (!item) return;
    event.preventDefault();
    event.stopPropagation();
    const action = item.dataset.action;
    if (action === 'accounts') {
      closeDrawer();
      document.getElementById('accountsNav')?.click();
      return;
    }
    openMenuFeature(action);
  });

  drawerEl?.addEventListener('pointerdown', event => {
    if (drawerEl.classList.contains('open') && drawerPanel && !drawerPanel.contains(event.target)) closeDrawer();
  });
})();