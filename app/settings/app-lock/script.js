(() => {
  const FIRST_ACCOUNT_PROMPT_KEY = 'indoone.app.lock.prompt.dismissed.v1';
  const SESSION_PIN_KEY = 'indoone.app.lock.session.pin.v1';
  const SESSION_UNLOCK_AT_KEY = 'indoone.app.lock.session.unlocked-at.v1';
  const SESSION_GRACE_MS = 5 * 60 * 1000;
  const MODULE_BASE = 'app/settings/app-unlock/';

  let startupUnlockShown = false;
  let firstAccountPromptShown = false;
  let sessionLockTimer = null;
  const modulePromises = new Map();

  function currentUser() {
    return window.IndooneFirebase?.auth?.currentUser || null;
  }

  function accountCount() {
    return Array.isArray(window.indooneState?.accounts)
      ? window.indooneState.accounts.length
      : 0;
  }

  function getSessionPin() {
    try {
      return sessionStorage.getItem(SESSION_PIN_KEY) || '';
    } catch (_) {
      return '';
    }
  }

  function getSessionUnlockAt() {
    try {
      return Number(sessionStorage.getItem(SESSION_UNLOCK_AT_KEY) || 0);
    } catch (_) {
      return 0;
    }
  }

  function setSession(pin) {
    try {
      sessionStorage.setItem(SESSION_PIN_KEY, String(pin));
      sessionStorage.setItem(SESSION_UNLOCK_AT_KEY, String(Date.now()));
    } catch (_) {}

    scheduleSessionLock();
  }

  function clearSession() {
    try {
      sessionStorage.removeItem(SESSION_PIN_KEY);
      sessionStorage.removeItem(SESSION_UNLOCK_AT_KEY);
    } catch (_) {}

    if (sessionLockTimer) {
      clearTimeout(sessionLockTimer);
      sessionLockTimer = null;
    }
  }

  function maskVisibleAccountCodes() {
    document.querySelectorAll('.account-info strong').forEach(node => {
      node.dataset.indooneLockHidden = 'true';
      node.style.visibility = 'hidden';
    });
  }

  function unmaskVisibleAccountCodes() {
    document.querySelectorAll('.account-info strong').forEach(node => {
      if (node.dataset.indooneLockHidden === 'true') {
        node.style.removeProperty('visibility');
        delete node.dataset.indooneLockHidden;
      }
    });
  }

  function ensureAppLockStyles() {
    if (document.getElementById('indoone-app-lock-runtime-styles')) return;

    const link = document.createElement('link');
    link.id = 'indoone-app-lock-runtime-styles';
    link.rel = 'stylesheet';
    link.href = 'app/settings/app-lock/style.css?v=20260907c';
    document.head.appendChild(link);
  }

  function clearAppLockOverlayClasses() {
    document.getElementById('overlay')?.classList.remove(
      'indoone-app-lock-overlay'
    );
    document.getElementById('modal')?.classList.remove(
      'indoone-app-lock-modal'
    );
  }

  function openAppLockScreen(html) {
    ensureAppLockStyles();
    openModal(html);
    document.getElementById('overlay')?.classList.add(
      'indoone-app-lock-overlay'
    );
    document.getElementById('modal')?.classList.add(
      'indoone-app-lock-modal'
    );
    document.body.classList.add('app-lock-active');
  }

  function closeAppLockScreen() {
    clearAppLockOverlayClasses();
    document.body.classList.remove('app-lock-active');
    closeModal();
  }

  function loadModule(name) {
    if (modulePromises.has(name)) {
      return modulePromises.get(name);
    }

    const promise = new Promise((resolve, reject) => {
      const existing = document.querySelector(
        `script[data-indoone-app-lock-module="${name}"]`
      );

      if (existing) {
        if (existing.dataset.loaded === 'true') {
          resolve();
        } else {
          existing.addEventListener('load', resolve, { once: true });
          existing.addEventListener('error', reject, { once: true });
        }
        return;
      }

      const script = document.createElement('script');
      script.src = `${MODULE_BASE}${name}.js?v=20260907c`;
      script.async = true;
      script.dataset.indooneAppLockModule = name;
      script.addEventListener(
        'load',
        () => {
          script.dataset.loaded = 'true';
          resolve();
        },
        { once: true }
      );
      script.addEventListener('error', reject, { once: true });
      document.head.appendChild(script);
    });

    modulePromises.set(name, promise);
    return promise;
  }

  const bridge = {
    openScreen: openAppLockScreen,
    closeScreen: closeAppLockScreen,
    setSession,
    clearSession,
    lock: () => IndoonePersistence.lock(),
    mask: maskVisibleAccountCodes,
    unmask: unmaskVisibleAccountCodes,
    renderAccounts: () => {
      if (typeof renderAccounts === 'function') renderAccounts();
    },
    startTOTPRefresh: () => {
      if (typeof startTOTPRefresh === 'function') startTOTPRefresh();
    },
    dismissFirstAccountPrompt: () => dismissFirstAccountPrompt(),
    setFirstAccountPromptShown: value => {
      firstAccountPromptShown = Boolean(value);
    },
    setStartupUnlockShown: value => {
      startupUnlockShown = Boolean(value);
    }
  };

  window.IndooneAppLockBridge = bridge;

  async function ensurePinModule() {
    await loadModule('pin');
    return window.IndooneAppLockPin;
  }

  async function ensureBiometricModule() {
    await loadModule('biometric');
    return window.IndooneAppLockBiometric;
  }

  window.showAppLock = async function (mode) {
    try {
      const module = await ensurePinModule();

      if (typeof mode === 'undefined') {
        module?.showAppLockSettings?.();
        return;
      }

      module?.showAppLock?.(mode);
    } catch (error) {
      toast(error?.message || 'App PIN screen failed to load');
    }
  };

  window.showChangeAppPin = async function () {
    try {
      const module = await ensurePinModule();
      module?.showChangeAppPin?.();
    } catch (error) {
      toast(error?.message || 'Change PIN screen failed to load');
    }
  };

  window.showBiometricUnlock = async function () {
    try {
      const module = await ensureBiometricModule();
      module?.showBiometricUnlock?.();
    } catch (error) {
      toast(error?.message || 'Biometric unlock screen failed to load');
    }
  };

  function scheduleSessionLock() {
    if (sessionLockTimer) {
      clearTimeout(sessionLockTimer);
      sessionLockTimer = null;
    }

    const unlockedAt = getSessionUnlockAt();
    const remaining = SESSION_GRACE_MS - (Date.now() - unlockedAt);

    if (remaining <= 0) {
      expireSessionLock();
      return;
    }

    sessionLockTimer = setTimeout(expireSessionLock, remaining);
  }

  function expireSessionLock() {
    clearSession();
    IndoonePersistence.lock();
    startupUnlockShown = false;
    firstAccountPromptShown = false;
    document.body.classList.add('app-lock-active');
    maskVisibleAccountCodes();

    if (currentUser() && IndoonePersistence.hasAppLock()) {
      if (IndooneBiometric.enabled()) {
        window.showBiometricUnlock();
      } else {
        window.showAppLock('unlock');
      }
    }
  }

  async function restoreSessionUnlock() {
    if (
      startupUnlockShown ||
      !currentUser() ||
      !IndoonePersistence.hasAppLock()
    ) {
      return false;
    }

    const unlockedAt = getSessionUnlockAt();
    const sessionPin = getSessionPin();

    if (
      !/^\d{4,12}$/.test(sessionPin) ||
      !unlockedAt ||
      (Date.now() - unlockedAt) >= SESSION_GRACE_MS
    ) {
      clearSession();
      return false;
    }

    try {
      const restored = await IndoonePersistence.unlock(sessionPin);

      if (!restored) {
        clearSession();
        return false;
      }

      startupUnlockShown = true;
      firstAccountPromptShown = false;
      document.body.classList.remove('app-lock-active');
      scheduleSessionLock();
      unmaskVisibleAccountCodes();

      if (typeof renderAccounts === 'function') renderAccounts();
      if (typeof startTOTPRefresh === 'function') startTOTPRefresh();

      return true;
    } catch (_) {
      clearSession();
      return false;
    }
  }

  function promptDismissedForCurrentUser() {
    const user = currentUser();
    if (!user?.uid) return false;

    try {
      return localStorage.getItem(
        `${FIRST_ACCOUNT_PROMPT_KEY}:${user.uid}`
      ) === 'true';
    } catch (_) {
      return false;
    }
  }

  function dismissFirstAccountPrompt() {
    const user = currentUser();
    if (!user?.uid) return;

    try {
      localStorage.setItem(
        `${FIRST_ACCOUNT_PROMPT_KEY}:${user.uid}`,
        'true'
      );
    } catch (_) {}
  }

  function showFirstAccountPrompt() {
    if (
      firstAccountPromptShown ||
      startupUnlockShown ||
      !currentUser() ||
      accountCount() < 1 ||
      IndoonePersistence.hasAppLock() ||
      promptDismissedForCurrentUser()
    ) {
      return false;
    }

    firstAccountPromptShown = true;

    openModal(`
      <div class="modal-head">
        <h2>Enable App Lock?</h2>
        <button class="close-btn" id="appLockPromptClose" aria-label="Not now">×</button>
      </div>
      <p>
        You have added your first authenticator account. You can protect
        Indoone with an App PIN. This is optional.
      </p>
      <button class="primary" id="appLockEnableAction">
        Enable App Lock
      </button>
      <button class="secondary" id="appLockLaterAction">
        Not now
      </button>
    `);

    const dismiss = () => {
      dismissFirstAccountPrompt();
      firstAccountPromptShown = false;
      closeModal();
    };

    document
      .getElementById('appLockEnableAction')
      ?.addEventListener('click', () => {
        firstAccountPromptShown = false;
        window.showAppLock('setup');
      });

    document
      .getElementById('appLockLaterAction')
      ?.addEventListener('click', dismiss);

    document
      .getElementById('appLockPromptClose')
      ?.addEventListener('click', dismiss);

    return true;
  }

  async function showStartupUnlock() {
    if (
      startupUnlockShown ||
      !currentUser() ||
      !IndoonePersistence.hasAppLock()
    ) {
      return false;
    }

    if (await restoreSessionUnlock()) {
      return true;
    }

    startupUnlockShown = true;
    firstAccountPromptShown = false;
    maskVisibleAccountCodes();
    document.body.classList.add('app-lock-active');

    if (IndooneBiometric.enabled()) {
      window.showBiometricUnlock();
    } else {
      window.showAppLock('unlock');
    }

    return true;
  }

  function monitorAppLockState() {
    let attempts = 0;
    const timer = setInterval(async () => {
      attempts += 1;

      if (!currentUser()) {
        if (attempts >= 200) clearInterval(timer);
        return;
      }

      if (IndoonePersistence.hasAppLock()) {
        await showStartupUnlock();
      } else {
        showFirstAccountPrompt();
      }

      if (startupUnlockShown || firstAccountPromptShown || attempts >= 200) {
        clearInterval(timer);
      }
    }, 100);
  }

  window.lockIndoone = function () {
    clearSession();
    IndoonePersistence.lock();
    startupUnlockShown = true;
    firstAccountPromptShown = false;
    document.body.classList.add('app-lock-active');
    maskVisibleAccountCodes();

    if (IndooneBiometric.enabled()) {
      window.showBiometricUnlock();
    } else {
      window.showAppLock('unlock');
    }
  };

  if (document.readyState === 'loading') {
    document.addEventListener(
      'DOMContentLoaded',
      monitorAppLockState,
      { once: true }
    );
  } else {
    monitorAppLockState();
  }
})();
