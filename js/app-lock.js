window.showAppLock = function () {
  openModal(`<h2>App Lock</h2><p>Protect Indoone with a PIN before viewing your codes.</p><div class="field"><label>4-DIGIT PIN</label><input type="password" inputmode="numeric" maxlength="4" placeholder="••••"></div><button class="primary" onclick="toast('PIN saved for demo');closeModal()">Save PIN</button>`);
};
