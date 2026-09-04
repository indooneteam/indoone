(() => {
  function mobileField() {
    return `
      <div class="mobile-field">
        <span
          id="signupMobilePrefix"
          class="mobile-prefix"
          hidden
        >+91</span>
        <input
          id="signupMobile"
          name="phone"
          type="tel"
          inputmode="tel"
          autocomplete="tel"
          autocapitalize="none"
          spellcheck="false"
          maxlength="13"
          placeholder="98765 43210"
        />
      </div>
    `;
  }

  function passwordField() {
    return `
      <div class="password-wrap">
        <input
          id="signupPassword"
          name="signupPassword"
          type="password"
          autocomplete="new-password"
          placeholder="Create a strong password"
        />
        <button
          type="button"
          class="password-toggle"
          data-password-toggle="signupPassword"
          aria-label="Show password"
          title="Show password"
        >◉</button>
      </div>
    `;
  }

  function html() {
    return `
      <div class="auth-brand">
        <span class="auth-mark">I</span>
        <div>
          <strong>Indoone</strong>
          <small>Authenticator</small>
        </div>
      </div>

      <div class="auth-copy">
        <p class="eyebrow">GET STARTED</p>
        <h1>Create your account</h1>
        <p>
          Securely create an Indoone account for your authenticator vault.
        </p>
      </div>

      <div class="field">
        <label>EMAIL ID</label>
        <input
          id="signupEmail"
          name="email"
          type="email"
          autocomplete="email"
          autocapitalize="none"
          spellcheck="false"
          placeholder="you@example.com"
        />
      </div>

      <div class="field">
        <label>MOBILE NUMBER</label>
        ${mobileField()}
      </div>

      <div class="field">
        <label>PASSWORD</label>
        ${passwordField()}
      </div>

      <button
        type="button"
        class="primary auth-action-button"
        data-auth-action="signup-submit"
      >
        Send OTP
      </button>

      <div id="signupOtpArea" hidden>
        <p class="auth-otp-note">
          OTP sent to <strong id="signupOtpEmail"></strong>
        </p>

        <div class="field">
          <label>VERIFICATION OTP</label>
          <input
            id="signupOtp"
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
          data-auth-action="signup-verify"
        >
          Verify &amp; Create Account
        </button>
      </div>

      <button
        type="button"
        class="secondary auth-action-button"
        data-auth-action="login"
      >
        Already have an account? Login
      </button>

      <div class="auth-footer">
        Your Indoone account is activated after successful email OTP verification.
      </div>
    `;
  }

  window.IndooneSignupFeature = {
    html,
    show: () => window.IndooneAuthUI?.showSignup?.()
  };
})();
