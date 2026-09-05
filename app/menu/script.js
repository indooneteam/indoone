(() => {
  const drawer = document.getElementById('drawer');
  const panel = drawer?.querySelector('.drawer-panel');
  const loadedScripts = new Set();
  const loadedStyles = new Set();
  const ASSET_VERSION = '20260905a';

  const featureInitializers = {
    accounts: 'initMenuAccounts',
    favorites: 'initMenuFavorites',
    trash: 'initMenuTrash',
    security: 'initMenuSecurity',
    'terms-of-use': 'initMenuTermsOfUse',
    'privacy-policy': 'initMenuPrivacyPolicy',
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

  function createLegalIcon(type) {
    const svg = document.createElementNS(
      'http://www.w3.org/2000/svg',
      'svg'
    );

    svg.setAttribute('width', '20');
    svg.setAttribute('height', '20');
    svg.setAttribute('viewBox', '0 0 24 24');
    svg.setAttribute('aria-hidden', 'true');
    svg.setAttribute('focusable', 'false');
    svg.setAttribute('fill', 'none');
    svg.setAttribute('stroke', 'currentColor');
    svg.setAttribute('stroke-width', '1.8');
    svg.setAttribute('stroke-linecap', 'round');
    svg.setAttribute('stroke-linejoin', 'round');
    svg.setAttribute('style', 'flex: 0 0 20px;');

    if (type === 'terms') {
      svg.innerHTML = [
        '<path d="M7 3.5h7l3.5 3.5v13.5H7z"></path>',
        '<path d="M14 3.5V7h3.5"></path>',
        '<path d="M9.5 11h5"></path>',
        '<path d="M9.5 14.5h5"></path>',
        '<path d="M9.5 18h3.5"></path>'
      ].join('');
    } else {
      svg.innerHTML = [
        '<path d="M12 3.5 19 6v5.5c0 4.3-2.7 7.7-7 9-4.3-1.3-7-4.7-7-9V6z"></path>',
        '<path d="m9.2 12 1.8 1.8 3.8-3.8"></path>'
      ].join('');
    }

    return svg;
  }

  function createLegalMenuItem(type, label) {
    const item = document.createElement('button');
    item.type = 'button';
    item.className = 'drawer-item';
    item.dataset.action = type === 'terms'
      ? 'terms-of-use'
      : 'privacy-policy';

    item.appendChild(
      createLegalIcon(type)
    );

    const text = document.createElement('span');
    text.textContent = label;
    item.appendChild(text);

    return item;
  }

  function ensureLegalMenuItems() {
    if (!panel) return;

    const securityItem = panel.querySelector(
      '[data-action="security"]'
    );
    const aboutItem = panel.querySelector(
      '[data-action="about"]'
    );

    if (!securityItem || !aboutItem) return;

    if (!panel.querySelector('[data-action="terms-of-use"]')) {
      const termsItem = createLegalMenuItem(
        'terms',
        'Terms of Use'
      );

      securityItem.insertAdjacentElement(
        'afterend',
        termsItem
      );
    }

    if (!panel.querySelector('[data-action="privacy-policy"]')) {
      const privacyItem = createLegalMenuItem(
        'privacy',
        'Privacy Policy'
      );

      const termsItem = panel.querySelector(
        '[data-action="terms-of-use"]'
      );

      termsItem?.insertAdjacentElement(
        'afterend',
        privacyItem
      );
    }
  }

  ensureLegalMenuItems();

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
    link.href = `${base}/style.css?v=${ASSET_VERSION}`;
    document.head.appendChild(link);
    loadedStyles.add(base);
  }

  async function loadScript(base) {
    const src = `${base}/script.js?v=${ASSET_VERSION}`;

    if (loadedScripts.has(src)) return;

    await new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = src;
      script.onload = resolve;
      script.onerror = () => reject(
        new Error('Menu feature script could not be loaded.')
      );
      document.body.appendChild(script);
    });

    loadedScripts.add(src);
  }

  async function loadFeatureMarkup(base) {
    const response = await fetch(
      `${base}/index.html?v=${ASSET_VERSION}`,
      {
        cache: 'no-store'
      }
    );

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
      window.toast?.(
        error?.message || 'Could not open menu item'
      );
    }
  }

  window.toggleMenu = function () {
    if (!drawer) return;

    const open = !drawer.classList.contains('open');
    drawer.classList.toggle('open', open);
    drawer.setAttribute(
      'aria-hidden',
      String(!open)
    );
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
