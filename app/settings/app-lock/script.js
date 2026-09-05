(() => {
  const FIRST_ACCOUNT_PROMPT_KEY = 'indoone.app.lock.prompt.dismissed.v1';
  let startupUnlockShown = false;
  let firstAccountPromptShown = false;

  function currentUser() {
    return window.IndooneFirebase?.auth?.currentUser || null;
  }

  function accountCount() {
    return Array.isArray(window.indooneState?.accounts)
      ? window.indooneState.accounts.length
      : 0;
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

  function showStartupUnlock() {
    if (
      startupUnlockShown ||
      !currentUser() ||
      !IndoonePersistence.hasAppLock()
    ) {
      return false;
    }

    startupUnlockShown = true;
    firstAccountPromptShown = false;
    document.body.classList.add('app-lock-active');
    showAppLock('unlock');
    return true;
  }

  function monitorAppLockState() {
    let attempts = 0;
    const timer = setInterval(() => {
      attempts += 1;

      if (!currentUser()) {
        if (attempts >= 200) clearInterval(timer);
        return;
      }

      if (IndoonePersistence.hasAppLock()) {
        showStartupUnlock();
      } else {
        showFirstAccountPrompt();
      }

      if (startupUnlockShown || firstAccountPromptShown || attempts >= 200) {
        clearInterval(timer);
      }
    }, 100);
  }

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
    `);

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

            startupUnlockShown = true;
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
            dismissFirstAccountPrompt();

            firstAccountPromptShown = false;
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
    IndoonePersistence.lock();
    startupUnlockShown = true;
    firstAccountPromptShown = false;
    document.body.classList.add('app-lock-active');
    showAppLock('unlock');
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
