(() => {
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

    const bridge = window.IndooneAppLockBridge;

    function render() {
      const actionDisabled = buffer.length < minLength;

      bridge.openScreen(`
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
        ?.addEventListener('click', () => window.showBiometricUnlock?.());

      document
        .getElementById('appLockCancel')
        ?.addEventListener('click', bridge.closeScreen);
    }

    render();
  }

  function showChangeAppPin() {
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
            bridge.setSession(newPin);
            bridge.closeScreen();
            toast('App PIN changed');
            return null;
          } catch (error) {
            return error?.message || 'App PIN change failed';
          }
        }
      });
    };

    renderStep();
  }

  function showDisableAppLock() {
    showCustomPinPad({
      title: 'Disable App Lock',
      description: 'Enter your current App PIN to disable App Lock.',
      actionLabel: 'Disable App Lock',
      allowClose: true,
      onSubmit: async value => {
        const verified = await IndoonePersistence.unlock(value);

        if (!verified) {
          return 'Incorrect current PIN';
        }

        try {
          IndoonePersistence.clear();
          bridge.clearSession();
          bridge.setStartupUnlockShown(false);
          bridge.setFirstAccountPromptShown(false);
          bridge.unmask();
          bridge.closeScreen();
          toast('App Lock disabled');
          return null;
        } catch (error) {
          return error?.message || 'Unable to disable App Lock';
        }
      }
    });
  }

  function showAppLockSettings() {
    const hasPin = IndoonePersistence.hasAppLock();

    bridge.openScreen(`
      <div class="app-lock-settings-modal">
        <div class="modal-head">
          <h2>App Lock</h2>
          <button class="close-btn" id="appLockSettingsClose" aria-label="Close">×</button>
        </div>
        <p>${hasPin
          ? 'Manage your Indoone App PIN.'
          : 'Protect Indoone with a 4–12 digit App PIN.'}</p>
        <div class="app-lock-settings-actions">
          ${hasPin
            ? `
              <button class="primary" type="button" id="appLockChangeAction">Change App PIN</button>
              <button class="secondary" type="button" id="appLockDisableAction">Disable App Lock</button>
            `
            : '<button class="primary" type="button" id="appLockSetAction">Set App Lock</button>'}
        </div>
      </div>
    `);

    document
      .getElementById('appLockSettingsClose')
      ?.addEventListener('click', bridge.closeScreen);

    document
      .getElementById('appLockSetAction')
      ?.addEventListener('click', () => showAppLock('setup'));

    document
      .getElementById('appLockChangeAction')
      ?.addEventListener('click', showChangeAppPin);

    document
      .getElementById('appLockDisableAction')
      ?.addEventListener('click', showDisableAppLock);
  }

  function showAppLock(mode = 'unlock') {
    const bridge = window.IndooneAppLockBridge;
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

            bridge.setSession(value);
            bridge.setStartupUnlockShown(true);
            bridge.unmask();
            bridge.closeScreen();
            bridge.renderAccounts();
            bridge.startTOTPRefresh();
            toast('App unlocked');
            return null;
          }

          IndooneSecureSession.unlock(value);
          await IndoonePersistence.save([], value);
          bridge.setSession(value);
          bridge.dismissFirstAccountPrompt();
          bridge.setFirstAccountPromptShown(false);
          bridge.unmask();
          bridge.closeScreen();
          toast('App PIN created');
          return null;
        } catch (error) {
          return error?.message || 'App PIN operation failed';
        }
      }
    });
  }

  window.IndooneAppLockPin = {
    showAppLock,
    showChangeAppPin,
    showDisableAppLock,
    showAppLockSettings
  };
})();
