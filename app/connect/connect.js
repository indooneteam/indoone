(() => {
  const get = id => document.getElementById(id);

  // Match the app-wide icon language used by navigation-icons.js:
  // outline icons, 1.8px stroke, round caps/joins, currentColor.
  const svgIcons = {
    pair: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M10.2 13.8 13.8 10.2a3.7 3.7 0 0 1 5.1.1 3.7 3.7 0 0 1 0 5.2l-2.2 2.2a3.7 3.7 0 0 1-5.2 0"/><path d="M13.8 10.2 10.2 13.8a3.7 3.7 0 0 1-5.1-.1 3.7 3.7 0 0 1 0-5.2l2.2-2.2a3.7 3.7 0 0 1 5.2 0"/></svg>',
    connect: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M5 12h14M8 7l-5 5 5 5M16 7l5 5-5 5"/></svg>',
    devices: '<svg viewBox="0 0 24 24" aria-hidden="true"><rect x="4" y="4" width="11" height="16" rx="2"/><rect x="9" y="8" width="11" height="11" rx="2"/><path d="M7 17h2"/></svg>',
    nearby: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 10.5a11 11 0 0 1 16 0M7.2 13.5a6.7 6.7 0 0 1 9.6 0M10.5 16.5a2.2 2.2 0 0 1 3 0"/><circle cx="12" cy="19" r="1.1" fill="currentColor" stroke="none"/></svg>',
    phone: '<svg viewBox="0 0 24 24" aria-hidden="true"><rect x="7" y="3" width="10" height="18" rx="2"/><path d="M10 18h4"/></svg>',
    laptop: '<svg viewBox="0 0 24 24" aria-hidden="true"><rect x="5" y="4" width="14" height="11" rx="1.5"/><path d="M3.5 18h17M9.5 18 10 20h4l.5-2"/></svg>',
    chevron: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="m9 5 7 7-7 7"/></svg>'
  };

  function putIcon(node, name, size = 23) {
    if (!node || !svgIcons[name]) return;
    node.innerHTML = svgIcons[name];
    const svg = node.firstElementChild;
    if (!svg) return;
    svg.style.width = `${size}px`;
    svg.style.height = `${size}px`;
    svg.style.display = 'block';
    svg.setAttribute('fill', 'none');
    svg.setAttribute('stroke', 'currentColor');
    svg.setAttribute('stroke-width', '1.8');
    svg.setAttribute('stroke-linecap', 'round');
    svg.setAttribute('stroke-linejoin', 'round');
  }

  function installConnectIcons() {
    document.querySelectorAll('.connect-action').forEach((button, index) => {
      const icon = button.querySelector('strong');
      if (index === 0) putIcon(icon, 'pair');
      if (index === 1) putIcon(icon, 'connect');
      if (index === 2) putIcon(icon, 'devices');
    });

    putIcon(document.querySelector('.connect-status-icon'), 'nearby', 22);

    document.querySelectorAll('.connect-choice .choice-icon').forEach((icon, index) => {
      putIcon(icon, index === 0 ? 'phone' : 'laptop');
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
    window.closeDrawer?.();
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
