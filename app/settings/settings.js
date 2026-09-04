(() => {
  const SETTINGS = [
    { id: 'profile', title: 'Profile', subtitle: 'Email & mobile number', action: () => window.showProfile?.() },
    { id: 'app-lock', title: 'App Lock', subtitle: 'PIN', action: () => window.showAppLock?.() },
    { id: 'biometric', title: 'Biometric Unlock', subtitle: 'Fingerprint / device credential', toggle: true },
    { id: 'auto-lock', title: 'Auto-Lock', subtitle: () => {
      const value = window.IndooneAutoLock?.minutes?.() ?? 0;
      return value ? `After ${value} minute${value === 1 ? '' : 's'}` : 'Never';
    }, action: () => window.showAutoLock?.() },
    { id: 'about', title: 'About Indoone', subtitle: 'Version 1.8', action: () => window.showAboutSettings?.() }
  ];

  function rowMarkup(item, biometricOn) {
    if (item.toggle) {
      return `<div class="settings-row"><span>${item.title}<small>${item.subtitle}</small></span><button class="toggle ${biometricOn ? 'on' : ''}" id="biometricToggle" aria-label="Toggle biometric unlock"></button></div>`;
    }
    const subtitle = typeof item.subtitle === 'function' ? item.subtitle() : item.subtitle;
    return `<button type="button" class="settings-row settings-row-button" data-settings-action="${item.id}"><span>${item.title}<small>${subtitle}</small></span><span class="settings-arrow">›</span></button>`;
  }

  window.showSettings = function ({ remember = true } = {}) {
    if (remember) window.IndoonePageState?.set('settings');
    const biometricOn = window.IndooneBiometric?.enabled?.() || false;
    openModal(`<div class="modal-head"><h2>Settings</h2><button class="close-btn" data-close>×</button></div><div class="settings-section-label">Account</div>${rowMarkup(SETTINGS[0], biometricOn)}<div class="settings-section-label">Security</div>${rowMarkup(SETTINGS[1], biometricOn)}${rowMarkup(SETTINGS[2], biometricOn)}${rowMarkup(SETTINGS[3], biometricOn)}<div class="settings-section-label">App</div>${rowMarkup(SETTINGS[4], biometricOn)}<button class="primary" id="settingsDone">Done</button>`);

    document.querySelector('[data-settings-action="profile"]')?.addEventListener('click', () => window.showProfile?.());
    document.querySelector('[data-settings-action="app-lock"]')?.addEventListener('click', () => window.showAppLock?.());
    document.querySelector('[data-settings-action="auto-lock"]')?.addEventListener('click', () => window.showAutoLock?.());
    document.querySelector('[data-settings-action="about"]')?.addEventListener('click', () => window.showAboutSettings?.());
    document.getElementById('biometricToggle')?.addEventListener('click', event => toggleBiometricSetting(event, event.currentTarget));
    document.getElementById('settingsDone')?.addEventListener('click', () => closeModal());
  };

  window.toggleBiometricSetting = function (event, button) {
    event?.stopPropagation?.();
    const enabled = button.classList.contains('on');
    if (enabled) {
      IndooneBiometric.disable();
      button.classList.remove('on');
      toast('Biometric unlock disabled');
      return;
    }
    if (!window.IndooneBiometric?.supported?.()) {
      toast('Biometric unlock is available in the Android app');
      return;
    }
    IndooneBiometric.enableForCurrentVault(
      () => { button.classList.add('on'); toast('Biometric unlock enabled'); },
      message => toast(message || 'Biometric authentication cancelled')
    );
  };

  window.showAboutSettings = function () {
    openModal(`<div class="modal-head"><h2>About Indoone</h2><button class="close-btn" data-close>×</button></div><div class="about-mark">I</div><h3 class="about-title">Indoone Authenticator</h3><p class="about-copy">Private authenticator with an encrypted local vault, cloud sync and secure device pairing.</p><div class="about-meta"><span>Version</span><b>1.8</b></div><div class="about-meta"><span>Storage</span><b>Encrypted vault</b></div><button class="primary" data-close>Done</button>`);
  };
})();
