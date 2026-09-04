(() => {
  const get = id => document.getElementById(id);
  const PERMISSION_FLOW_URL = 'app/connect/permissions/permission-connection-flow.js?v=20260920a';
  let permissionFlowLoading = null;

  function loadPermissionFlow() {
    if (window.IndoonePermissionConnectionFlowLoaded) return Promise.resolve();
    if (permissionFlowLoading) return permissionFlowLoading;
    permissionFlowLoading = new Promise(resolve => {
      const script = document.createElement('script');
      script.src = PERMISSION_FLOW_URL;
      script.async = true;
      script.onload = () => {
        window.IndoonePermissionConnectionFlowLoaded = true;
        resolve();
      };
      script.onerror = () => resolve();
      document.head.appendChild(script);
    });
    return permissionFlowLoading;
  }

  void loadPermissionFlow();

  function setActive(tab) {
    get('accountsNav')?.classList.toggle('active', tab === 'accounts');
    get('lobbyNav')?.classList.toggle('active', tab === 'lobby');
    get('connectNav')?.classList.toggle('active', tab === 'connect');
    get('settingsNav')?.classList.toggle('active', tab === 'settings');
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

  function showConnectHome() {
    get('connectChoicePage')?.setAttribute('hidden', '');
    get('connectHomePage')?.removeAttribute('hidden');
  }

  window.showConnect = function () {
    window.closeModal?.();
    window.closeDrawer?.();
    showConnectContent();
    showConnectHome();
    setActive('connect');
  };

  window.showConnectHome = function () {
    window.closeModal?.();
    showConnectHome();
  };

  document.addEventListener('click', event => {
    const action = event.target.closest('[data-connect-action]');
    if (!action) return;

    const name = action.dataset.connectAction;

    if (name === 'qr') {
      event.preventDefault();
      window.showConnectQr?.();
      return;
    }

    if (name === 'scanner') {
      event.preventDefault();
      window.showConnectScanner?.();
      return;
    }

    if (name === 'connect') {
      event.preventDefault();
      window.showConnectChoice?.();
      return;
    }

    if (name === 'devices') {
      event.preventDefault();
      window.showConnectDevices?.();
      return;
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
