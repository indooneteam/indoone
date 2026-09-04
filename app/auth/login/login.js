(() => {
  function mobileField() {
    return `
      <div class="mobile-field">
        <span id="loginMobilePrefix" class="mobile-prefix" hidden>+91</span>
        <input
          id="authIdentifier"
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
          id="authPassword"
          name="authPassword"
          type="password"
          autocomplete="current-password"
          placeholder="Enter your password"
        />
        <button
          type="button"
          class="password-toggle"
          data-password-toggle="authPassword"
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
        <p class="eyebrow">SECURE &amp; PRIVATE</p>
        <h1>Welcome back</h1>
        <p>Sign in to protect and sync your authenticator vault.</p>
      </div>

      <div class="field">
        <label>EMAIL OR MOBILE NUMBER</label>
        ${mobileField()}
      </div>

      <div class="field">
        <label>PASSWORD</label>
        ${passwordField()}
      </div>

      <button
        type="button"
        class="link-button auth-action-button"
        data-auth-action="forgot-password"
      >Forgot password?</button>

      <button
        type="button"
        class="primary auth-action-button"
        data-auth-action="login-submit"
      >Send OTP</button>

      <div id="loginOtpArea" class="auth-otp-area" hidden>
        <p class="auth-otp-note">
          OTP sent to <strong id="loginOtpEmail"></strong>
        </p>

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
        >Verify &amp; Login</button>
      </div>

      <button
        type="button"
        class="secondary auth-action-button"
        data-auth-action="signup"
      >Create Account</button>

      <div class="auth-footer">
        Protect your Indoone account with password and email OTP verification.
      </div>
    `;
  }

  window.IndooneLoginFeature = {
    html,
    show: () => window.IndooneAuthUI?.showLogin?.()
  };
})();
