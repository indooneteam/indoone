window.showSettings = function () {
  openModal(`<h2>Settings</h2><div class="settings-row"><span>App Lock<small>PIN</small></span><button class="icon-btn" onclick="showAppLock()">›</button></div><div class="settings-row"><span>Biometric Unlock<small>Fingerprint</small></span><button class="toggle" onclick="toggleSetting(this)"></button></div><div class="settings-row"><span>Hide Codes<small>Protect codes on screen</small></span><button class="icon-btn" onclick="toggleSetting(this)">○</button></div><div class="settings-row"><span>Encrypted Backup<small>Export accounts securely</small></span><button class="icon-btn" onclick="showBackup()">›</button></div><button class="primary" onclick="showHome()">Done</button>`);
};

window.toggleSetting = function (button) {
  button.classList.toggle('enabled');
  toast(button.classList.contains('enabled') ? 'Setting enabled' : 'Setting disabled');
};
