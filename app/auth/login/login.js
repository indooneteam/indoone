(() => {
  function logoMarkup() {
    return `
      <svg
        class="auth-mark branding-image"
        viewBox="0 0 512 512"
        role="img"
        aria-label="Indoone logo"
        focusable="false"
      >
        <defs>
          <linearGradient id="indooneLoginGradient" x1="0" y1="1" x2="1" y2="0">
            <stop offset="0" stop-color="#32106F"></stop>
            <stop offset="0.5" stop-color="#6526C7"></stop>
            <stop offset="1" stop-color="#B277FF"></stop>
          </linearGradient>
        </defs>
        <path d="M126 154 C176 138 258 145 344 115 C382 102 407 81 421 52 L421 112 C411 142 388 161 353 173 C264 203 190 181 126 206 Z" fill="url(#indooneLoginGradient)"></path>
        <path d="M126 235 C188 211 258 224 350 193 C387 181 408 162 421 137 L421 198 C411 226 388 245 351 257 C263 286 190 260 126 285 Z" fill="url(#indooneLoginGradient)"></path>
        <path d="M126 304 C190 279 258 296 349 265 C386 253 409 235 421 210 L421 270 C411 301 387 323 350 336 C273 364 205 342 166 360 C136 374 126 397 128 419 C130 444 147 457 169 457 C190 457 205 445 205 421 L205 364 C253 378 304 372 351 351 C390 334 413 309 421 281 C418 344 383 389 326 407 C274 423 238 427 208 445 C185 459 154 458 136 440 C114 418 109 387 112 357 C114 336 119 320 126 304 Z" fill="url(#indooneLoginGradient)"></path>
      </svg>
    `;
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
        ${logoMarkup()}
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
      >
        Forgot password?
      </button>

      <button
        type="button"
        class="primary auth-action-button"
        data-auth-action="login-submit"
      >
        Send OTP
      </button>

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
        >
          Verify &amp; Login
        </button>
      </div>

      <button
        type="button"
        class="secondary auth-action-button"
        data-auth-action="signup"
      >
        Create Account
      </button>

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
