(() => {
  const drawer = document.getElementById('drawer');
  const panel = drawer?.querySelector('.drawer-panel');
  const loadedScripts = new Set();
  const loadedStyles = new Set();

  const featureInitializers = {
    accounts: 'initMenuAccounts',
    favorites: 'initMenuFavorites',
    trash: 'initMenuTrash',
    security: 'initMenuSecurity',
    about: 'initMenuAbout',
    lock: 'initMenuLock',
    'danger-zone': 'initMenuDangerZone',
    logout: 'initMenuLogout'
  };

  const nestedInitializers = {
    'danger-zone/delete-local-data': 'initMenuDeleteLocalData',
    'danger-zone/delete-account': 'initMenuDeleteAccount',
    'logout/this-device': 'initMenuLogoutThisDevice',
    'logout/all-devices': 'initMenuLogoutAllDevices'
  };

  function showOverlay() {
    document.getElementById('overlay')?.classList.remove('hidden');
  }

  function hideOverlay() {
    document.getElementById('overlay')?.classList.add('hidden');
  }

  function getFeatureBase(path) {
    return `app/menu/${path}`;
  }

  async function loadStyle(base) {
    if (loadedStyles.has(base)) return;

    const link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = `${base}/style.css?v=20260903d`;
    document.head.appendChild(link);
    loadedStyles.add(base);
  }

  async function loadScript(base) {
    const src = `${base}/script.js?v=20260903d`;

    if (loadedScripts.has(src)) return;

    await new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = src;
      script.onload = resolve;
      script.onerror = () => reject(new Error('Menu feature script could not be loaded.'));
      document.body.appendChild(script);
    });

    loadedScripts.add(src);
  }

  async function loadFeatureMarkup(base) {
    const response = await fetch(`${base}/index.html?v=20260903d`, {
      cache: 'no-store'
    });

    if (!response.ok) {
      throw new Error('Menu feature could not be loaded.');
    }

    const modal = document.getElementById('modal');
    if (!modal) {
      throw new Error('Menu modal is unavailable.');
    }

    modal.innerHTML = await response.text();
  }

  async function openPath(path, initializerName) {
    closeDrawer();

    try {
      const base = getFeatureBase(path);

      await loadFeatureMarkup(base);
      await loadStyle(base);
      await loadScript(base);

      const initializer = window[initializerName];
      if (typeof initializer !== 'function') {
        throw new Error('Menu feature initializer is unavailable.');
      }

      const result = await initializer();

      if (result === false) {
        hideOverlay();
      } else {
        showOverlay();
      }
    } catch (error) {
      hideOverlay();
      window.toast?.(error?.message || 'Could not open menu item');
    }
  }

  window.toggleMenu = function () {
    if (!drawer) return;

    const open = !drawer.classList.contains('open');
    drawer.classList.toggle('open', open);
    drawer.setAttribute('aria-hidden', String(!open));
  };

  window.closeDrawer = function () {
    drawer?.classList.remove('open');
    drawer?.setAttribute('aria-hidden', 'true');
  };

  window.openMenuFeature = function (feature) {
    const initializerName = featureInitializers[feature];
    if (!initializerName) return;

    void openPath(feature, initializerName);
  };

  window.openMenuNested = function (path) {
    const initializerName = nestedInitializers[path];
    if (!initializerName) return;

    void openPath(path, initializerName);
  };

  panel?.addEventListener('click', event => {
    const item = event.target.closest('[data-action]');
    if (!item || !panel.contains(item)) return;

    event.preventDefault();
    event.stopPropagation();

    const action = item.dataset.action;

    if (action === 'accounts') {
      closeDrawer();
      document.getElementById('accountsNav')?.click();
      return;
    }

    window.openMenuFeature(action);
  });

  drawer?.addEventListener('pointerdown', event => {
    if (!drawer.classList.contains('open')) return;
    if (panel && panel.contains(event.target)) return;

    closeDrawer();
  });
})();
