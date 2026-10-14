// The login screen, built as the app builds it in LoginScreen.kt.
//
// Three steps, and only one is on screen at a time:
//   identifier  email or mobile, then Continue with OTP / Continue with Password
//   otp         the code, then Verify & Login / Resend OTP
//   password    the password, then Complete Login / Back
(() => {
  function logoMarkup() {
    return '<span class="auth-mark" aria-label="Indoone logo"></span>';
  }

  function mobileField() {
    return `
      <div class="mobile-field">
        <span
          id="loginMobilePrefix"
          class="mobile-prefix"
          hidden
        >+91</span>
        <input
          id="loginIdentifier"
          name="login-identifier"
          type="text"
          inputmode="text"
          autocomplete="username"
          autocapitalize="none"
          spellcheck="false"
          maxlength="320"
          placeholder="you@example.com or 98765 43210"
        />
      </div>
    `;
  }

  function passwordField() {
    return `
      <div class="password-wrap">
        <input
          id="loginPassword"
          name="loginPassword"
          type="password"
          autocomplete="current-password"
          placeholder="Enter your password"
        />
        <button
          type="button"
          class="password-toggle"
          data-password-toggle="loginPassword"
          aria-label="Show password"
          title="Show password"
        >◉</button>
      </div>
    `;
  }

  function html() {
    return `
      <div class="auth-brand">
        ${logoMarkup()}
        <div>
          <strong>Indoone</strong>
          <small>Authenticator</small>
        </div>
      </div>

      <div class="auth-step" data-login-step="identifier">
        <div class="auth-copy">
          <p class="eyebrow">SECURE &amp; PRIVATE</p>
          <h1>Welcome to your Indoone account</h1>
          <p>Continue with your Indoone account</p>
        </div>

        <div class="field">
          <label>EMAIL OR MOBILE NUMBER</label>
          ${mobileField()}
        </div>

        <button
          type="button"
          class="primary auth-action-button"
          data-auth-action="login-continue-otp"
        >
          Continue with OTP
        </button>

        <button
          type="button"
          class="secondary auth-action-button"
          data-auth-action="login-continue-password"
        >
          Continue with Password
        </button>

        <button
          type="button"
          class="secondary auth-action-button"
          data-auth-action="signup"
        >
          Create Account
        </button>

        <button
          type="button"
          class="link-button auth-action-button"
          data-auth-action="forgot-password"
        >
          Forgot password?
        </button>
      </div>

      <div class="auth-step" data-login-step="otp" hidden>
        <div class="auth-copy">
          <p class="eyebrow">VERIFICATION</p>
          <h1>Enter OTP</h1>
          <p>
            We have sent a 6-digit OTP to<br />
            <strong id="loginOtpEmail"></strong>
          </p>
        </div>

        <div class="field">
          <label>VERIFICATION OTP</label>
          <input
            id="loginOtp"
            name="one-time-code"
            inputmode="numeric"
            autocomplete="one-time-code"
            maxlength="6"
            placeholder="Enter 6-digit OTP"
          />
        </div>

        <button
          type="button"
          class="primary auth-action-button"
          data-auth-action="login-verify"
        >
          Verify &amp; Login
        </button>

        <button
          type="button"
          class="secondary auth-action-button"
          data-auth-action="login-resend"
        >
          Resend OTP
        </button>

        <button
          type="button"
          class="link-button auth-action-button"
          data-auth-action="login-back"
        >
          ← Back
        </button>
      </div>

      <div class="auth-step" data-login-step="password" hidden>
        <div class="auth-copy">
          <p class="eyebrow">PASSWORD</p>
          <h1>Enter Password</h1>
          <p>Continue with your Indoone account</p>
        </div>

        <div class="field">
          <label>EMAIL OR MOBILE NUMBER</label>
          <input
            id="loginPasswordIdentifier"
            name="login-password-identifier"
            type="text"
            inputmode="text"
            autocomplete="username"
            autocapitalize="none"
            spellcheck="false"
            maxlength="320"
            placeholder="you@example.com or 98765 43210"
          />
        </div>

        <div class="field">
          <label>PASSWORD</label>
          ${passwordField()}
        </div>

        <button
          type="button"
          class="primary auth-action-button"
          data-auth-action="login-complete"
        >
          Complete Login
        </button>

        <button
          type="button"
          class="link-button auth-action-button"
          data-auth-action="login-back"
        >
          ← Back
        </button>
      </div>

      <div class="auth-footer">
        Protect your Indoone account with password and email OTP verification.
      </div>
    `;
  }

  // Only one step is ever visible. The identifier carries across so the
  // password step starts from what was already typed.
  function showStep(step, root = document) {
    const identifier = root.querySelector('#loginIdentifier');
    const passwordIdentifier = root.querySelector('#loginPasswordIdentifier');

    if (step === 'password' && identifier && passwordIdentifier) {
      passwordIdentifier.value = identifier.value;
    }

    root.querySelectorAll('[data-login-step]').forEach(panel => {
      panel.hidden = panel.dataset.loginStep !== step;
    });

    const focus = {
      identifier: '#loginIdentifier',
      otp: '#loginOtp',
      password: '#loginPassword'
    }[step];

    root.querySelector(focus)?.focus();
  }

  function otpEmail(email, root = document) {
    const node = root.querySelector('#loginOtpEmail');

    if (node) {
      node.textContent = email || '';
    }
  }

  window.IndooneLoginFeature = {
    html,
    showStep,
    otpEmail,
    show: () => window.IndooneAuthUI?.showLogin?.()
  };
})();
