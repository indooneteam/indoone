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

  function ensureAppLockStyles() {
    if (document.getElementById('indoone-app-lock-runtime-styles')) return;

    const link = document.createElement('link');
    link.id = 'indoone-app-lock-runtime-styles';
    link.rel = 'stylesheet';
    link.href = 'app/settings/app-lock/style.css?v=20260907b';
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

  function pinDots(buffer, maxLength = 12) {
    return Array.from({ length: maxLength }, (_, index) =>
      `<span class="app-lock-dot${index < buffer.length ? ' filled' : ''}"></span>`
    ).join('');
  }

  function showCustomPinPad({
    title,
    description,
    actionLabel,
    maxLength = 12,
    minLength = 4,
    allowClose = false,
    showBiometricSwitch = false,
    onSubmit
  }) {
    let buffer = '';
    let errorMessage = '';

    function render() {
      const actionDisabled = buffer.length < minLength;

      openAppLockScreen(`
        <div class="app-lock-screen">
          <div class="app-lock-top">
            <div class="app-lock-brand" aria-hidden="true">
              <span class="app-lock-brand-mark">I</span>
              <span class="app-lock-brand-name">Indoone</span>
            </div>

            <h2 class="app-lock-title">${title}</h2>
            <p class="app-lock-description">${description}</p>

            <div
              class="app-lock-dots"
              aria-label="PIN length ${buffer.length}"
            >${pinDots(buffer, maxLength)}</div>
            <div class="app-lock-error" aria-live="polite">${errorMessage}</div>

            <button
              type="button"
              class="app-lock-action"
              id="appLockPadAction"
              ${actionDisabled ? 'disabled' : ''}
            >
              ${actionLabel}
            </button>
          </div>

          <div>
            <div class="app-lock-keypad" aria-label="PIN keypad">
              <button type="button" class="app-lock-key" data-app-lock-digit="1">1</button>
              <button type="button" class="app-lock-key" data-app-lock-digit="2">2</button>
              <button type="button" class="app-lock-key" data-app-lock-digit="3">3</button>
              <button type="button" class="app-lock-key" data-app-lock-digit="4">4</button>
              <button type="button" class="app-lock-key" data-app-lock-digit="5">5</button>
              <button type="button" class="app-lock-key" data-app-lock-digit="6">6</button>
              <button type="button" class="app-lock-key" data-app-lock-digit="7">7</button>
              <button type="button" class="app-lock-key" data-app-lock-digit="8">8</button>
              <button type="button" class="app-lock-key" data-app-lock-digit="9">9</button>
              <button type="button" class="app-lock-key secondary-key" data-app-lock-clear>Clear</button>
              <button type="button" class="app-lock-key" data-app-lock-digit="0">0</button>
              <button type="button" class="app-lock-key secondary-key" data-app-lock-back>⌫</button>
            </div>

            <div class="app-lock-footer">
              ${showBiometricSwitch
                ? '<button type="button" class="app-lock-cancel" id="appLockBiometricSwitch">Use Biometric</button>'
                : allowClose
                  ? '<button type="button" class="app-lock-cancel" id="appLockCancel">Cancel</button>'
                  : 'PIN is entered using the secure on-screen keypad'}
            </div>
          </div>
        </div>
      `);

      document.querySelectorAll('[data-app-lock-digit]').forEach(button => {
        button.addEventListener('click', () => {
          if (buffer.length >= maxLength) return;
          buffer += button.getAttribute('data-app-lock-digit');
          errorMessage = '';
          render();
        });
      });

      document.querySelectorAll('[data-app-lock-back]').forEach(button => {
        button.addEventListener('click', () => {
          buffer = buffer.slice(0, -1);
          errorMessage = '';
          render();
        });
      });

      document.querySelectorAll('[data-app-lock-clear]').forEach(button => {
        button.addEventListener('click', () => {
          buffer = '';
          errorMessage = '';
          render();
        });
      });

      document
        .getElementById('appLockPadAction')
        ?.addEventListener('click', async () => {
          if (buffer.length < minLength) return;

          const value = buffer;
          const result = await onSubmit(value);

          if (typeof result === 'string' && result) {
            buffer = '';
            errorMessage = result;
            render();
          }
        });

      document
        .getElementById('appLockBiometricSwitch')
        ?.addEventListener('click', () => showBiometricUnlock());

      document
        .getElementById('appLockCancel')
        ?.addEventListener('click', closeAppLockScreen);
    }

    render();
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
    maskVisibleAccountCodes();

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
    let step = 'current';
    let newPin = '';

    const renderStep = () => {
      const meta = {
        current: {
          title: 'Verify current PIN',
          description: 'Enter your current App PIN to continue.',
          action: 'Continue'
        },
        new: {
          title: 'Create new PIN',
          description: 'Choose a new 4–12 digit App PIN.',
          action: 'Continue'
        },
        confirm: {
          title: 'Confirm new PIN',
          description: 'Enter the new PIN again to confirm it.',
          action: 'Change PIN'
        }
      }[step];

      showCustomPinPad({
        title: meta.title,
        description: meta.description,
        actionLabel: meta.action,
        allowClose: true,
        onSubmit: async value => {
          if (step === 'current') {
            const verified = await IndoonePersistence.unlock(value);
            if (!verified) return 'Incorrect current PIN';
            step = 'new';
            renderStep();
            return null;
          }

          if (step === 'new') {
            newPin = value;
            step = 'confirm';
            renderStep();
            return null;
          }

          if (value !== newPin) {
            return 'New PINs do not match';
          }

          if (value.length < 4 || value.length > 12) {
            return 'PIN must be 4–12 digits';
          }

          try {
            await IndoonePersistence.save([], newPin);
            setSession(newPin);
            closeAppLockScreen();
            toast('App PIN changed');
            return null;
          } catch (error) {
            return error?.message || 'App PIN change failed';
          }
        }
      });
    };

    renderStep();
  };

  window.showAppLock = function (mode = 'unlock') {
    const hasPin = IndoonePersistence.hasAppLock();
    const isUnlock = hasPin && mode !== 'setup';

    showCustomPinPad({
      title: isUnlock ? 'Unlock Indoone' : 'Create App PIN',
      description: isUnlock
        ? 'Enter your App PIN. Indoone stays locked until the correct PIN is entered.'
        : 'Create a 4–12 digit PIN to protect Indoone.',
      actionLabel: isUnlock ? 'Unlock App' : 'Create App PIN',
      allowClose: !isUnlock,
      showBiometricSwitch: isUnlock && IndooneBiometric.enabled(),
      onSubmit: async value => {
        try {
          if (isUnlock) {
            const ok = await IndoonePersistence.unlock(value);

            if (!ok) {
              return 'Incorrect PIN';
            }

            setSession(value);
            startupUnlockShown = true;
            unmaskVisibleAccountCodes();
            closeAppLockScreen();
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
            closeAppLockScreen();
            toast('App PIN created');
          }

          return null;
        } catch (error) {
          return error?.message || 'App PIN operation failed';
        }
      }
    });
  };

  window.showBiometricUnlock = function () {
    openAppLockScreen(`
      <div class="app-lock-screen biometric-unlock-screen">
        <div class="app-lock-top">
          <div class="app-lock-brand" aria-hidden="true">
            <span class="app-lock-brand-mark">I</span>
            <span class="app-lock-brand-name">Indoone</span>
          </div>

          <h2 class="app-lock-title">Unlock Indoone</h2>
          <div class="token-icon">●</div>
          <p class="app-lock-description" style="text-align:center">
            Use your fingerprint or device biometric to unlock Indoone.
          </p>

          <button class="primary" id="biometricUnlockAction">
            Use Fingerprint
          </button>
          <button class="secondary" id="pinFallbackAction">
            Use App PIN
          </button>
        </div>
      </div>
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
            closeAppLockScreen();
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
    maskVisibleAccountCodes();

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