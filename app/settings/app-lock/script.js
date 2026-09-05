window.showAppLock = function (mode = 'unlock') {
  const hasPin = IndoonePersistence.hasAppLock();
  const title =
    mode === 'setup'
      ? 'Create App PIN'
      : (hasPin ? 'Unlock Indoone' : 'Create App PIN');

  openModal(`
    <div class="modal-head">
      <h2>${title}</h2>
      <button class="close-btn" data-close>×</button>
    </div>
    <p>
      ${
        hasPin && mode !== 'setup'
          ? 'Enter your App PIN to unlock Indoone.'
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

          closeModal();
          renderAccounts();

          if (typeof startTOTPRefresh === 'function') {
            startTOTPRefresh();
          }

          toast('App unlocked');
        } else {
          IndooneSecureSession.unlock(value);
          await IndoonePersistence.save([], value);

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

  if (IndooneBiometric.enabled()) {
    showBiometricUnlock();
  } else {
    showAppLock('unlock');
  }
};
