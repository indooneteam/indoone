(() => {
  const FIRST_ACCOUNT_PROMPT_KEY = 'indoone.app.lock.prompt.dismissed.v1';
  const SESSION_PIN_KEY = 'indoone.app.lock.session.pin.v1';
  const SESSION_UNLOCK_AT_KEY = 'indoone.app.lock.session.unlocked-at.v1';
  const SESSION_GRACE_MS = 5 * 60 * 1000;
  let startupUnlockShown = false;
  let firstAccountPromptShown = false;
  let sessionLockTimer = null;

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

    sessionLockTimer = setTimeout(
      expireSessionLock,
      remaining
    );
  }

  function expireSessionLock() {
    clearSession();
    IndoonePersistence.lock();
    startupUnlockShown = false;
    firstAccountPromptShown = false;
    document.body.classList.add('app-lock-active');

    if (currentUser() && IndoonePersistence.hasAppLock()) {
      if (IndooneBiometric.enabled()) {
        showBiometricUnlock();
      } else {
        showAppLock('unlock');
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

      if (typeof renderAccounts === 'function') {
        renderAccounts();
      }

      if (typeof startTOTPRefresh === 'function') {
        startTOTPRefresh();
      }

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
        showAppLock('setup');
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
      showBiometricUnlock();
    } else {
      showAppLock('unlock');
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

  window.showChangeAppPin = function () {
    openModal(`
      <div class="modal-head">
        <h2>Change your PIN</h2>
        <button class="close-btn" data-close aria-label="Close">×</button>
      </div>
      <p>
        Enter your current App PIN, then choose a new 4–12 digit PIN.
      </p>

      <div class="field">
        <label>Current PIN</label>
        <input
          id="currentVaultPin"
          type="password"
          inputmode="numeric"
          maxlength="12"
          autocomplete="off"
          placeholder="Current PIN"
        >
      </div>

      <div class="field">
        <label>New PIN</label>
        <input
          id="newVaultPin"
          type="password"
          inputmode="numeric"
          maxlength="12"
          autocomplete="off"
          placeholder="4–12 digits"
        >
      </div>

      <div class="field">
        <label>Confirm new PIN</label>
        <input
          id="confirmVaultPin"
          type="password"
          inputmode="numeric"
          maxlength="12"
          autocomplete="off"
          placeholder="Re-enter new PIN"
        >
      </div>

      <button class="primary" id="changeVaultPinAction">
        Change PIN
      </button>
    `);

    document
      .getElementById('changeVaultPinAction')
      ?.addEventListener('click', async () => {
        const currentPin =
          document.getElementById('currentVaultPin')?.value || '';
        const newPin =
          document.getElementById('newVaultPin')?.value || '';
        const confirmPin =
          document.getElementById('confirmVaultPin')?.value || '';

        if (!/^\d{4,12}$/.test(currentPin)) {
          return toast('Current PIN must be 4–12 digits');
        }

        if (!/^\d{4,12}$/.test(newPin)) {
          return toast('New PIN must be 4–12 digits');
        }

        if (newPin !== confirmPin) {
          return toast('New PINs do not match');
        }

        if (newPin === currentPin) {
          return toast('New PIN must be different from the current PIN');
        }

        try {
          const verified = await IndoonePersistence.unlock(currentPin);

          if (!verified) {
            return toast('Incorrect current PIN');
          }

          await IndoonePersistence.save([], newPin);
          setSession(newPin);
          closeModal();
          toast('App PIN changed');
        } catch (error) {
          toast(error?.message || 'App PIN change failed');
        }
      });
  };

  window.showAppLock = function (mode = 'unlock') {
    const hasPin = IndoonePersistence.hasAppLock();
    const title =
      mode === 'setup'
        ? 'Create App PIN'
        : (hasPin ? 'Unlock Indoone' : 'Create App PIN');

    openModal(`
      <div class="modal-head">
        <h2>${title}</h2>
        <button class="close-btn" data-close ${
          hasPin && mode !== 'setup'
            ? 'aria-label="Unlock required"'
            : 'aria-label="Close"'
        }>×</button>
      </div>
      <p>
        ${
          hasPin && mode !== 'setup'
            ? 'Enter your App PIN to unlock Indoone. App access is locked until the correct PIN is entered.'
            : 'Your App PIN controls access to Indoone when the app is locked.'
        }
      </p>
      <div class="field">
        <label>PIN</label>
        <input
          id="vaultPin"
          type="password"
          inputmode="numeric"
          maxlength="12"
          autocomplete="off"
          placeholder="4–12 digits"
        >
      </div>
      <button class="primary" id="vaultPinAction">
        ${
          hasPin && mode !== 'setup'
            ? 'Unlock App'
            : 'Create App PIN'
        }
      </button>
      ${
        hasPin && mode !== 'setup'
          ? '<button class="secondary" id="changeAppPinAction">Change your PIN</button>'
          : ''
      }
    `);

    document
      .getElementById('changeAppPinAction')
      ?.addEventListener('click', () => {
        window.showChangeAppPin();
      });

    document
      .getElementById('vaultPinAction')
      ?.addEventListener('click', async () => {
        const value =
          document.getElementById('vaultPin')?.value || '';

        if (!/^\d{4,12}$/.test(value)) {
          return toast('PIN must be 4–12 digits');
        }

        try {
          if (hasPin && mode !== 'setup') {
            const ok = await IndoonePersistence.unlock(value);

            if (!ok) {
              return toast('Incorrect PIN');
            }

            setSession(value);
            startupUnlockShown = true;
            unmaskVisibleAccountCodes();
            document.body.classList.remove('app-lock-active');
            closeModal();
            renderAccounts();

            if (typeof startTOTPRefresh === 'function') {
              startTOTPRefresh();
            }

            toast('App unlocked');
          } else {
            IndooneSecureSession.unlock(value);
            await IndoonePersistence.save([], value);
            setSession(value);
            dismissFirstAccountPrompt();

            firstAccountPromptShown = false;
            unmaskVisibleAccountCodes();
            document.body.classList.remove('app-lock-active');
            closeModal();
            toast('App PIN created');
          }
        } catch (error) {
          toast(error?.message || 'App PIN operation failed');
        }
      });
  };

  window.showBiometricUnlock = function () {
    openModal(`
      <div class="modal-head">
        <h2>Unlock Indoone</h2>
      </div>
      <div class="token-icon">●</div>
      <p style="text-align:center">
        Use your fingerprint or device biometric to unlock Indoone.
      </p>
      <button class="primary" id="biometricUnlockAction">
        Use Fingerprint
      </button>
      <button class="secondary" id="pinFallbackAction">
        Use App PIN
      </button>
    `);

    const biometricButton = document.getElementById(
      'biometricUnlockAction'
    );
    const pinFallbackButton = document.getElementById(
      'pinFallbackAction'
    );

    biometricButton?.addEventListener('click', () => {
      biometricButton.disabled = true;

      IndooneBiometric.authenticateForUnlock(
        async pin => {
          try {
            const ok = await IndoonePersistence.unlock(pin);

            if (!ok) {
              throw new Error('Biometric credential is invalid');
            }

            setSession(pin);
            startupUnlockShown = true;
            unmaskVisibleAccountCodes();
            document.body.classList.remove('app-lock-active');
            closeModal();
            renderAccounts();

            if (typeof startTOTPRefresh === 'function') {
              startTOTPRefresh();
            }

            toast('App unlocked with fingerprint');
          } catch (error) {
            biometricButton.disabled = false;
            toast(
              error?.message ||
                'Biometric unlock failed'
            );
          }
        },
        message => {
          biometricButton.disabled = false;
          toast(message);
        }
      );
    });

    pinFallbackButton?.addEventListener(
      'click',
      () => showAppLock('unlock')
    );

    setTimeout(
      () => biometricButton?.click(),
      120
    );
  };

  window.lockIndoone = function () {
    clearSession();
    IndoonePersistence.lock();
    startupUnlockShown = true;
    firstAccountPromptShown = false;
    document.body.classList.add('app-lock-active');
    if (IndooneBiometric.enabled()) {
      showBiometricUnlock();
    } else {
      showAppLock('unlock');
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
