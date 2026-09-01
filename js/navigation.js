window.showTrash = function () {
  openModal(`<h2>Trash</h2><p style="text-align:center;padding:28px 0">No accounts in trash.</p><button class="primary" onclick="closeModal()">Done</button>`);
};

window.showSecurity = function () {
  openModal(`<h2>Security</h2><div class="settings-row"><span>Local storage<small>Encrypted on this device</small></span><b>✓</b></div><div class="settings-row"><span>Network access<small>No server required</small></span><b>✓</b></div><button class="primary" onclick="closeModal()">Done</button>`);
};

window.showAbout = function () {
  openModal(`<h2>About Indoone</h2><p>Indoone Authenticator — a private, offline-first TOTP authenticator demo.</p><button class="primary" onclick="closeModal()">Done</button>`);
};
