(() => {
  const $ = id => document.getElementById(id);
  const overlay = $('overlay');
  let authResolved = false;
  let authGateRunning = false;
  let authStateResolved = false;

  function markAuthReady() {
    if (authResolved) return;
    authResolved = true;
    document.body.classList.add('auth-ready');
    document.body.classList.remove('auth-boot');
  }

  function renderLoginNow() {
    try {
      if (window.IndooneAuthUI?.showLogin) {
        window.IndooneAuthUI.showLogin();
        markAuthReady();
        return true;
      }
    } catch (error) {
      console.error('Indoone login UI error:', error);
    }
    return false;
  }

  function bindClick(element, handler) {
    if (!element || element.dataset.indooneBound === 'true') return;
    element.dataset.indooneBound = 'true';
    element.addEventListener('click', handler);
  }

  function showAccounts({ remember = true } = {}) {
    if (remember) window.IndoonePageState?.set('home');
    closeDrawer();
    closeModal();
    window.showConnectHome?.({ remember: false });
    $('content')?.removeAttribute('hidden');
    $('connectContent')?.setAttribute('hidden', '');
    $('addBtn')?.removeAttribute('hidden');
    $('searchWrap')?.removeAttribute('hidden');
    $('accountsNav')?.classList.add('active');
    $('lobbyNav')?.classList.remove('active');
    $('connectNav')?.classList.remove('active');
    $('settingsNav')?.classList.remove('active');
    window.IndooneHome?.restoreHome?.();
  }

  function showSettingsPanel({ remember = true } = {}) {
    if (remember) window.IndoonePageState?.set('settings');
    closeDrawer();
    $('content')?.removeAttribute('hidden');
    $('connectContent')?.setAttribute('hidden', '');
    $('addBtn')?.setAttribute('hidden', '');
    $('searchWrap')?.setAttribute('hidden', '');
    $('settingsNav')?.classList.add('active');
    $('accountsNav')?.classList.remove('active');
    $('lobbyNav')?.classList.remove('active');
    $('connectNav')?.classList.remove('active');
    window.showSettings?.({ remember: false });
  }

  async function restoreSavedPage() {
    const hash = window.location.hash || '';

    // URL routes are authoritative for nested Add Account pages. Restore them
    // through the same Home/Add Account router so refresh never falls back to
    // the Accounts page first and later races to correct itself.
    if (hash === '#home/add-account' || hash.startsWith('#home/add-account/')) {
      try {
        await window.IndooneHome?.showAddAccount?.({ push: false });
        return;
      } catch (error) {
        console.warn('Add Account route restore failed:', error);
      }
    }

    const page = window.IndoonePageState?.get?.() || 'home';

    switch (page) {
      case 'settings':
        showSettingsPanel({ remember: false });
        break;
      case 'profile':
        showSettingsPanel({ remember: false });
        window.showProfile?.({ remember: false });
        break;
      case 'mobile':
        showSettingsPanel({ remember: false });
        window.showProfile?.({ remember: false });
        window.showChangeMobile?.({ remember: false });
        break;
      case 'email':
        showSettingsPanel({ remember: false });
        window.showProfile?.({ remember: false });
        window.showChangeEmail?.({ remember: false });
        break;
      case 'connect':
        window.showConnect?.({ remember: false });
        break;
      case 'connect-choice':
        window.showConnectChoice?.({ remember: false });
        break;
      case 'pair':
        window.showConnect?.({ remember: false });
        await window.showConnectPair?.({ remember: false });
        break;
      case 'home':
      default:
        showAccounts({ remember: false });
        break;
    }
  }

  if (!localStorage.getItem('indoone_otp_verified_uid')) renderLoginNow();

  bindClick($('menuBtn'), () => window.toggleMenu?.());
  bindClick($('addBtn'), () => window.showAdd?.());
  bindClick($('emptyAddBtn'), () => window.showAdd?.());
  bindClick($('searchBtn'), () => window.IndooneHomeSearch?.show?.());
  bindClick($('clearSearch'), () => window.IndooneHomeSearch?.clear?.());
  bindClick($('brandBtn'), showAccounts);
  bindClick($('accountsNav'), showAccounts);
  bindClick($('settingsNav'), showSettingsPanel);
  bindClick($('lobbyNav'), () => window.showLobby?.() || toast('Lobby is planned next'));
  bindClick($('connectNav'), () => window.showConnect?.());
  bindClick($('sortBtn'), () => window.handleSortAccounts?.());

  $('drawerBackdrop')?.addEventListener('click', () => window.closeDrawer?.());

  $('drawer')?.addEventListener('click', event => {
    const item = event.target.closest('[data-action]');
    if (!item) return;
    const action = item.dataset.action;
    if (action === 'accounts') return showAccounts();
    if (action === 'favorites') return window.showFavorites?.();
    if (action === 'trash') return window.showTrash?.();
    if (action === 'security') return window.showSecurity?.();
    if (action === 'about') return window.showAbout?.();
    if (action === 'lock') return window.lockIndoone?.();
    if (action === 'danger-zone') return window.showDangerZone?.();
    if (action === 'logout') return window.showLogout?.();
  });

  document.addEventListener('click', event => {
    const card = event.target.closest('.account');
    if (card && !event.target.closest('[data-fav]')) window.openAccount?.(card.dataset.id);
    const fav = event.target.closest('[data-fav]');
    if (fav) {
      event.stopPropagation();
      window.toggleFavorite?.(fav.dataset.fav);
    }
  });

  document.addEventListener('keydown', event => {
    const card = event.target.closest('.account');
    if (card && (event.key === 'Enter' || event.key === ' ')) {
      event.preventDefault();
      window.openAccount?.(card.dataset.id);
    }
  });

  overlay?.addEventListener('click', event => {
    if (event.target === overlay || event.target.closest('[data-close]')) return window.closeModal?.();
    if (event.target.closest('[data-camera]')) return window.IndooneQrScanner?.start?.();
    if (event.target.closest('[data-save-account]')) return window.saveAccount?.();
    if (event.target.closest('[data-copy]')) return window.copyCurrentCode?.();
    if (event.target.closest('[data-delete]')) return window.deleteCurrent?.();
    if (event.target.closest('[data-edit]')) return window.editCurrent?.();
    if (event.target.closest('[data-create-recovery]')) return window.createBackup?.();
    if (event.target.closest('[data-restore-recovery]')) return window.restoreRecoveryPdf?.();
    if (event.target.closest('[data-delete-recovery]')) return window.deleteRecoveryPdf?.();
    if (event.target.closest('[data-pin]')) return window.showAppLock?.('setup');
    if (event.target.closest('[data-backup]')) return window.showBackup?.();
    if (event.target.closest('[data-about]')) return window.showAbout?.();
    if (event.target.closest('[data-security]')) return window.showSecurity?.();
    if (event.target.closest('[data-sort-modal]')) {
      window.handleSortAccounts?.();
      return window.closeModal?.();
    }
    if (event.target.closest('[data-stop-scan]')) {
      window.IndooneQrScanner?.stop?.();
      return window.closeModal?.();
    }
  });

  async function loadFirebaseAccounts() {
    if (!window.IndooneCloudAccounts?.load) throw new Error('Firebase account storage is unavailable.');
    return window.IndooneCloudAccounts.load();
  }
  window.loadFirebaseAccounts = loadFirebaseAccounts;

  async function showAuthOrHome(user) {
    if (authGateRunning) return;
    authGateRunning = true;
    try {
      if (window.__indooneAuthPending) return;
      const persistedUid = localStorage.getItem('indoone_otp_verified_uid');
      if (!user) {
        if (persistedUid && !authStateResolved) return;
        localStorage.removeItem('indoone_otp_verified_uid');
        localStorage.removeItem('indoone_authenticated_uid');
        sessionStorage.removeItem('indoone_otp_verified_uid');
        sessionStorage.removeItem('indoone_authenticated_uid');
        renderLoginNow();
        return;
      }
      authStateResolved = true;
      if (!persistedUid || persistedUid !== user.uid) {
        localStorage.removeItem('indoone_otp_verified_uid');
        localStorage.removeItem('indoone_authenticated_uid');
        sessionStorage.removeItem('indoone_otp_verified_uid');
        sessionStorage.removeItem('indoone_authenticated_uid');
        await window.IndooneFirebase?.auth?.signOut?.().catch(() => {});
        renderLoginNow();
        return;
      }
      sessionStorage.setItem('indoone_otp_verified_uid', persistedUid);
      sessionStorage.setItem('indoone_authenticated_uid', persistedUid);
      await loadFirebaseAccounts();
      await restoreSavedPage();
      if (typeof startDemoTimers === 'function') startDemoTimers();
    } catch (error) {
      console.error('Indoone auth gate error:', error);
      renderLoginNow();
    } finally {
      authGateRunning = false;
      markAuthReady();
    }
  }

  window.addEventListener('load', async () => {
    const auth = window.IndooneFirebase?.auth;
    window.__indooneLoginOtp = null;
    window.__indooneSignupDraft = null;
    window.__indooneAuthPending = false;
    if (!auth) return renderLoginNow();
    try { await (window.IndooneFirebase?.persistenceReady || Promise.resolve()); }
    catch (error) { console.warn('Indoone Firebase persistence initialization failed:', error); }
    if (typeof auth.onAuthStateChanged === 'function') {
      auth.onAuthStateChanged(showAuthOrHome);
      setTimeout(() => {
        if (!authStateResolved && !auth.currentUser && !localStorage.getItem('indoone_otp_verified_uid')) renderLoginNow();
      }, 5000);
    } else renderLoginNow();
  });

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => window.IndooneHome?.init?.(), { once: true });
  } else {
    window.IndooneHome?.init?.();
  }
})();
