(() => {
  const get = id => document.getElementById(id);

  const svgIcons = {
    pair: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M10.5 13.5 13.5 10.5a3.5 3.5 0 0 1 5 5l-2 2a3.5 3.5 0 0 1-5 0" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"/><path d="m13.5 10.5-3 3a3.5 3.5 0 0 1-5-5l2-2a3.5 3.5 0 0 1 5 0" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"/></svg>',
    connect: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M3 12h18" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round"/><path d="m8 7-5 5 5 5" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"/><path d="m16 7 5 5-5 5" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"/></svg>',
    devices: '<svg viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="5" width="12" height="15" rx="2.2" fill="none" stroke="currentColor" stroke-width="1.8"/><rect x="8" y="9" width="13" height="10" rx="1.8" fill="none" stroke="currentColor" stroke-width="1.8"/><path d="M7 17h4" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>',
    nearby: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 10.5a11.2 11.2 0 0 1 16 0M7.2 13.5a6.8 6.8 0 0 1 9.6 0M10.3 16.5a2.4 2.4 0 0 1 3.4 0" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><circle cx="12" cy="19" r="1.25" fill="currentColor"/></svg>',
    phone: '<svg viewBox="0 0 24 24" aria-hidden="true"><rect x="6.5" y="2.8" width="11" height="18.4" rx="2.2" fill="none" stroke="currentColor" stroke-width="1.8"/><path d="M10 18.2h4" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>',
    laptop: '<svg viewBox="0 0 24 24" aria-hidden="true"><rect x="5" y="4" width="14" height="11" rx="1.7" fill="none" stroke="currentColor" stroke-width="1.8"/><path d="M3.5 18h17" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/><path d="M9.5 18 10 20h4l.5-2" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/></svg>',
    chevron: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="m9 5 7 7-7 7" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"/></svg>'
  };

  function putIcon(node, name, size = 22) {
    if (!node || !svgIcons[name]) return;
    node.innerHTML = svgIcons[name];
    const svg = node.firstElementChild;
    if (!svg) return;
    svg.style.width = `${size}px`;
    svg.style.height = `${size}px`;
    svg.style.display = 'block';
  }

  function installConnectIcons() {
    document.querySelectorAll('.connect-action').forEach((button, index) => {
      const icon = button.querySelector('strong');
      if (index === 0) putIcon(icon, 'pair', 22);
      if (index === 1) putIcon(icon, 'connect', 22);
      if (index === 2) putIcon(icon, 'devices', 22);
    });

    putIcon(document.querySelector('.connect-status-icon'), 'nearby', 21);

    document.querySelectorAll('.connect-choice .choice-icon').forEach((icon, index) => {
      putIcon(icon, index === 0 ? 'phone' : 'laptop', 23);
    });

    document.querySelectorAll('.connect-choice .arrow').forEach(arrow => putIcon(arrow, 'chevron', 18));
  }

  function setActive(tab) {
    get('accountsNav')?.classList.toggle('active', tab === 'accounts');
    get('lobbyNav')?.classList.toggle('active', tab === 'lobby');
    get('connectNav')?.classList.toggle('active', tab === 'connect');
    get('settingsNav')?.classList.toggle('active', tab === 'settings');
  }

  function setConnectPage(page) {
    get('connectHomePage')?.toggleAttribute('hidden', page !== 'home');
    get('connectChoicePage')?.toggleAttribute('hidden', page !== 'choice');
    get('connectContent')?.querySelectorAll('.connect-pair-page').forEach(node => node.remove());
  }

  function showAccounts() {
    get('connectContent')?.setAttribute('hidden', '');
    get('content')?.removeAttribute('hidden');
    get('addBtn')?.removeAttribute('hidden');
    get('searchWrap')?.removeAttribute('hidden');
    setActive('accounts');
  }

  function showConnectContent() {
    get('content')?.setAttribute('hidden', '');
    get('addBtn')?.setAttribute('hidden', '');
    get('searchWrap')?.setAttribute('hidden', '');
    get('connectContent')?.removeAttribute('hidden');
    installConnectIcons();
  }

  function showConnectHome({ remember = true } = {}) {
    if (remember) window.IndoonePageState?.set('connect');
    setConnectPage('home');
    installConnectIcons();
  }

  window.showConnect = function ({ remember = true } = {}) {
    if (remember) window.IndoonePageState?.set('connect');
    window.closeModal?.();
    window.closeDrawer?.();
    showConnectContent();
    showConnectHome({ remember: false });
    setActive('connect');
  };

  window.showConnectHome = function ({ remember = true } = {}) {
    if (remember) window.IndoonePageState?.set('connect');
    window.closeModal?.();
    showConnectContent();
    showConnectHome({ remember: false });
    setActive('connect');
  };

  window.showConnectChoice = function ({ remember = true } = {}) {
    if (remember) window.IndoonePageState?.set('connect-choice');
    window.closeModal?.();
    showConnectContent();
    setConnectPage('choice');
    setActive('connect');
    installConnectIcons();
  };

  document.addEventListener('click', event => {
    const action = event.target.closest('[data-connect-action]');
    if (!action) return;

    const name = action.dataset.connectAction;

    if (name === 'pair') {
      event.preventDefault();
      return window.showConnectPair?.();
    }

    if (name === 'qr') {
      event.preventDefault();
      return window.showConnectQr?.();
    }

    if (name === 'scanner') {
      event.preventDefault();
      return window.showConnectScanner?.();
    }

    if (name === 'connect') {
      event.preventDefault();
      return window.showConnectChoice?.();
    }

    if (name === 'devices') {
      event.preventDefault();
      return window.showConnectDevices?.();
    }

    if (name === 'home') {
      event.preventDefault();
      window.showConnectHome?.();
    }
  });

  get('connectNav')?.addEventListener('click', event => {
    event.preventDefault();
    window.showConnect();
  });

  get('accountsNav')?.addEventListener('click', event => {
    event.preventDefault();
    window.stopNearby?.();
    showAccounts();
  });

  installConnectIcons();
})();
