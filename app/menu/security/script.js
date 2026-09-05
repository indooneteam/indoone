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
        When creating an account, the user provides their email or mobile number,
        password, and OTP. The OTP is securely verified through the
        IndoVerification system. After successful verification, the user’s account
        is created securely in Firebase.

        During login, the user enters their registered email or mobile number,
        password, and OTP to authenticate their existing account.

        The app features auto-lock, app lock, and biometric authentication. Once
        enabled, these security features help protect the app. Additionally, the
        Accounts page prevents screenshots to help protect account information.
      </span>
    </div>
  `;
};
