// The sign-up screen, built from SignUpScreen.kt.
(() => {
  const TITLE_LINES = [
    { text: 'Get started with', purple: true },
    { text: 'Creating your account', purple: false }
  ];
  const DESCRIPTION =
    'Securely create an Indoone account for your authenticator vault.';
  const DESCRIPTION_MS = 2000;

  function logoMarkup() {
    return '<span class="auth-mark" aria-label="Indoone logo"></span>';
  }

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
        ${logoMarkup()}
        <div>
          <strong>Indoone</strong>
          <small>Authenticator</small>
        </div>
      </div>

      <div class="auth-step" data-signup-step="details">
        <div class="auth-title-box">
          <h1
            class="auth-heading"
            id="signupTitle"
          ></h1>
        </div>

        <p
          class="auth-note"
          id="signupDescription"
        ></p>

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
          Continue with OTP
        </button>

        <button
          type="button"
          class="primary auth-action-button"
          data-auth-action="signup-password"
        >
          Continue with Password
        </button>
      </div>

      <div class="auth-step" data-signup-step="otp" hidden>
        <p class="auth-note">
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

        <button
          type="button"
          class="primary auth-action-button"
          data-auth-action="signup-resend"
        >
          Resend OTP
        </button>
      </div>

      <button
        type="button"
        class="secondary auth-green auth-action-button"
        data-auth-action="login"
      >
        Already have an account? Login
      </button>

      <p class="auth-footer">
        Continue with OTP verifies your email. Continue with Password creates
        the account directly using your password.
      </p>
    `;
  }

  // The app alternates the title every two seconds, and types the description
  // out over the same two seconds.
  let titleTimer = null;
  let typeTimer = null;
  let titleIndex = 0;

  function paintTitle(root) {
    const node = root.querySelector('#signupTitle');

    if (!node) return;

    const line = TITLE_LINES[titleIndex % TITLE_LINES.length];
    node.textContent = line.text;
    node.classList.toggle('is-purple', line.purple);
  }

  function startTitle(root) {
    stopTitle();
    titleIndex = 0;
    paintTitle(root);
    titleTimer = setInterval(() => {
      titleIndex += 1;
      paintTitle(root);
    }, 2000);
  }

  function stopTitle() {
    if (titleTimer) {
      clearInterval(titleTimer);
      titleTimer = null;
    }
  }

  function startTyping(root) {
    stopTyping();

    const node = root.querySelector('#signupDescription');

    if (!node) return;

    let shown = 0;
    node.textContent = '';
    const step = Math.max(DESCRIPTION_MS / DESCRIPTION.length, 16);

    typeTimer = setInterval(() => {
      shown += 1;
      node.textContent = DESCRIPTION.slice(0, shown);

      if (shown >= DESCRIPTION.length) {
        stopTyping();
      }
    }, step);
  }

  function stopTyping() {
    if (typeTimer) {
      clearInterval(typeTimer);
      typeTimer = null;
    }
  }

  function showStep(step, root = document) {
    root.querySelectorAll('[data-signup-step]').forEach(panel => {
      panel.hidden = panel.dataset.signupStep !== step;
    });

    const focus = {
      details: '#signupEmail',
      otp: '#signupOtp'
    }[step];

    root.querySelector(focus)?.focus();

    if (step === 'details') {
      startTitle(root);
      startTyping(root);
    } else {
      stopTitle();
      stopTyping();
    }
  }

  function otpEmail(email, root = document) {
    const node = root.querySelector('#signupOtpEmail');

    if (node) {
      node.textContent = email || '';
    }
  }

  function values(root = document) {
    return {
      email: root.querySelector('#signupEmail')?.value || '',
      mobile: root.querySelector('#signupMobile')?.value || '',
      password: root.querySelector('#signupPassword')?.value || ''
    };
  }

  window.IndooneSignupFeature = {
    html,
    showStep,
    otpEmail,
    values,
    show: () => window.IndooneAuthUI?.showSignup?.()
  };
})();
