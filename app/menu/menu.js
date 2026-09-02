window.toggleMenu = function () {
  openModal(`<h2>Indoone</h2><div class="settings-row"><span>Accounts<small>Your authenticator accounts</small></span><b>›</b></div><div class="settings-row"><span>Favorites<small>Quick access accounts</small></span><b>›</b></div><div class="settings-row"><span>Trash<small>Recently removed accounts</small></span><b>›</b></div><div class="settings-row"><span>Security<small>App protection</small></span><b>›</b></div><button class="primary" onclick="closeModal()">Close</button>`);
};
