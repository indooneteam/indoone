// The login screen, built from LoginScreen.kt.
//
// Three steps, one on screen at a time, with the app's own texts and motion.
(() => {
  const WELCOME_LINES = [
    { text: 'Welcome to your Indoone account', gradient: false },
    { text: 'Continue with your Indoone account', gradient: true }
  ];

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
        <h1
          class="auth-welcome"
          id="loginWelcome"
        ></h1>

        <div class="field">
          <label>EMAIL OR MOBILE NUMBER</label>
          ${mobileField()}
        </div>

        <div class="auth-button-row">
          <button
            type="button"
            class="primary auth-action-button"
            data-auth-action="login-continue-otp"
          >
            Continue with OTP
          </button>

          <button
            type="button"
            class="primary auth-action-button"
            data-auth-action="login-continue-password"
          >
            Continue with Password
          </button>
        </div>

        <button
          type="button"
          class="secondary auth-green auth-action-button"
          data-auth-action="signup"
        >
          Create Account
        </button>
      </div>

      <div class="auth-step" data-login-step="otp" hidden>
        <button
          type="button"
          class="auth-back auth-action-button"
          data-auth-action="login-back"
        >
          ← Back
        </button>

        <h1 class="auth-heading">Enter OTP</h1>

        <p class="auth-note">
          We have sent a 6-digit OTP to<br />
          <strong id="loginOtpEmail"></strong>
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

        <button
          type="button"
          class="secondary auth-action-button"
          data-auth-action="login-resend"
        >
          Resend OTP
        </button>
      </div>

      <div class="auth-step" data-login-step="password" hidden>
        <button
          type="button"
          class="auth-back auth-action-button"
          data-auth-action="login-back"
        >
          ← Back
        </button>

        <h1 class="auth-heading">Enter Password</h1>

        <p class="auth-note auth-note-gradient">
          Continue with your Indoone account
        </p>

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
      </div>

      <div
        class="auth-dots"
        id="loginDots"
        aria-hidden="true"
      >
        <span data-dot="identifier"></span>
        <span data-dot="otp"></span>
        <span data-dot="password"></span>
      </div>
    `;
  }

  // The app swaps the welcome line every two seconds: the outgoing line fades
  // out while sliding left, the incoming one fades in from the right.
  let welcomeTimer = null;
  let welcomeIndex = 0;

  function paintWelcome(root) {
    const node = root.querySelector('#loginWelcome');

    if (!node) return;

    const line = WELCOME_LINES[welcomeIndex % WELCOME_LINES.length];
    node.textContent = line.text;
    node.classList.toggle('is-gradient', line.gradient);
    node.classList.remove('is-entering');
    void node.offsetWidth;
    node.classList.add('is-entering');
  }

  function startWelcome(root) {
    stopWelcome();
    welcomeIndex = 0;
    paintWelcome(root);
    welcomeTimer = setInterval(() => {
      welcomeIndex += 1;
      paintWelcome(root);
    }, 2000);
  }

  function stopWelcome() {
    if (welcomeTimer) {
      clearInterval(welcomeTimer);
      welcomeTimer = null;
    }
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

    root.querySelectorAll('#loginDots [data-dot]').forEach(dot => {
      dot.classList.toggle('on', dot.dataset.dot === step);
    });

    const focus = {
      identifier: '#loginIdentifier',
      otp: '#loginOtp',
      password: '#loginPassword'
    }[step];

    root.querySelector(focus)?.focus();

    if (step === 'identifier') {
      startWelcome(root);
    } else {
      stopWelcome();
    }
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
    stopWelcome,
    show: () => window.IndooneAuthUI?.showLogin?.()
  };
})();
