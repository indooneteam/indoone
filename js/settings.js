window.showSettings = function () {
  const biometricOn = window.IndooneBiometric?.enabled?.() || false;
  openModal(`<h2>Settings</h2><div class="settings-row" onclick="showAppLock()"><span>App Lock<small>PIN</small></span><button class="icon-btn" type="button">›</button></div><div class="settings-row"><span>Biometric Unlock<small>Fingerprint / device credential</small></span><button class="toggle ${biometricOn ? 'on' : ''}" id="biometricToggle" onclick="toggleBiometricSetting(event, this)"></button></div><div class="settings-row"><span>Hide Codes<small>Protect codes on screen</small></span><button class="icon-btn" onclick="toggleSetting(this)">○</button></div><div class="settings-row"><span>Encrypted Backup<small>Export accounts securely</small></span><button class="icon-btn" onclick="showBackup()">›</button></div><button class="primary" onclick="showHome()">Done</button>`);
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
    () => {
      button.classList.add('on');
      toast('Biometric unlock enabled');
    },
    message => toast(message || 'Biometric authentication cancelled')
  );
};

window.toggleSetting = function (button) {
  button.classList.toggle('on');
  toast(button.classList.contains('on') ? 'Setting enabled' : 'Setting disabled');
};
