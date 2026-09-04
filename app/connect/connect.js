(() => {
  const get = id => document.getElementById(id);

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
  }

  function showConnectHome({ remember = true } = {}) {
    if (remember) window.IndoonePageState?.set('connect');
    setConnectPage('home');
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
  };

  window.showConnectChoice = function ({ remember = true } = {}) {
    if (remember) window.IndoonePageState?.set('connect');
    window.closeModal?.();
    showConnectContent();
    setConnectPage('choice');
    setActive('connect');
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
})();
