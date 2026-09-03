window.initMenuSecurity = function () {
  const modal = document.getElementById('modal');

  modal.innerHTML = `
    <div class="modal-head">
      <h2>Security</h2>
      <button class="close-btn" data-close>×</button>
    </div>
    <div class="settings-row">
      <span>
        Cloud account storage
        <small>Authenticator accounts sync to your Indoone account</small>
      </span>
      <b>✓</b>
    </div>
    <div class="settings-row">
      <span>
        Local account storage
        <small>Not used for permanent account data</small>
      </span>
      <b>✓</b>
    </div>
  `;
};
