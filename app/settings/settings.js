(() => {
  const SETTINGS = [
    { id: 'app-lock', title: 'App Lock', subtitle: 'PIN' },
    { id: 'biometric', title: 'Biometric Unlock', subtitle: 'Fingerprint / device credential', toggle: true },
    { id: 'auto-lock', title: 'Auto-Lock', subtitle: () => { const value = window.IndooneAutoLock?.minutes?.() ?? 0; return value ? `After ${value} minute${value === 1 ? '' : 's'}` : 'Never'; } },
    { id: 'about', title: 'About Indoone', subtitle: 'Version 1.8' }
  ];

  function rowMarkup(item, biometricOn) {
    if (item.toggle) {
      return `<div class="settings-row"><span>${item.title}<small>${item.subtitle}</small></span><button class="toggle ${biometricOn ? 'on' : ''}" id="biometricToggle" aria-label="Toggle biometric unlock"></button></div>`;
    }
    const subtitle = typeof item.subtitle === 'function' ? item.subtitle() : item.subtitle;
    return `<button type="button" class="settings-row settings-row-button" data-settings-action="${item.id}"><span>${item.title}<small>${subtitle}</small></span><span class="settings-arrow">›</span></button>`;
  }

  window.showSettings = function () {
    const biometricOn = window.IndooneBiometric?.enabled?.() || false;
    openModal(`<div class="modal-head"><h2>Settings</h2><button class="close-btn" data-close>×</button></div><div class="settings-section-label">Security</div>${rowMarkup(SETTINGS[0], biometricOn)}${rowMarkup(SETTINGS[1], biometricOn)}${rowMarkup(SETTINGS[2], biometricOn)}<div class="settings-section-label">App</div>${rowMarkup(SETTINGS[3], biometricOn)}<button class="primary" id="settingsDone">Done</button>`);

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
})();
