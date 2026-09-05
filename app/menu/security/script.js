window.initMenuSecurity = function () {
  const modal = document.getElementById('modal');

  modal.innerHTML = `
    <div class="modal-head">
      <h2>
        Security
      </h2>
      <button
        class="close-btn"
        data-close
        aria-label="Close Security"
      >
        ×
      </button>
    </div>
    <div class="settings-row">
      <span>
        When creating an account, the user provides email or mobile, password, and OTP. OTP is securely verified via IndoVerification system. Upon verification, the account is created securely in Firebase. During login, email or mobile, password, and OTP are entered again. The app features auto-lock, app lock, and biometric. Once enabled, the app is secure. Additionally, the accounts page prevents screenshots. This ensures complete security.
      </span>
    </div>
  `;
};
