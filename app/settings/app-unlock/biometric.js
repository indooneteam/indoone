(() => {
  function showBiometricUnlock() {
    const bridge = window.IndooneAppLockBridge;

    bridge.openScreen(`
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

            bridge.setSession(pin);
            bridge.setStartupUnlockShown(true);
            bridge.unmask();
            bridge.closeScreen();
            bridge.renderAccounts();
            bridge.startTOTPRefresh();

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
      () => window.showAppLock?.('unlock')
    );

    setTimeout(
      () => biometricButton?.click(),
      120
    );
  }

  window.IndooneAppLockBiometric = {
    showBiometricUnlock
  };
})();
