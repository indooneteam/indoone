(() => {
  const $ = id => document.getElementById(id);
  let busy = false;

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
          data-signup-password-toggle
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
        data-signup-action="submit"
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
          data-signup-action="verify"
        >
          Verify &amp; Create Account
        </button>
      </div>

      <button
        type="button"
        class="secondary auth-action-button"
        data-signup-action="login"
      >
        Already have an account? Login
      </button>

      <div class="auth-footer">
        Your Indoone account is activated after successful email OTP verification.
      </div>
    `;
  }

  function showShell(markup) {
    const overlay = $('overlay');
    const modal = $('modal');
    if (!overlay || !modal) return false;

    overlay.classList.remove('hidden');
    overlay.classList.add('indoone-auth-screen');
    document.body.classList.add('auth-open', 'auth-pending');

    modal.innerHTML = `
      <div class="auth-page auth-signup-page">
        ${markup}
        <div
          id="authStatus"
          class="auth-status"
          hidden
          aria-live="polite"
        ></div>
      </div>
    `;

    bind(modal);
    return true;
  }

  function status(message = '', error = false) {
    const node = $('authStatus');
    if (!node) return;

    node.textContent = message;
    node.hidden = !message;
    node.dataset.error = error ? 'true' : 'false';
  }

  function bindPassword() {
    const button = document.querySelector('[data-signup-password-toggle]');
    if (!button || button.dataset.bound === 'true') return;

    button.dataset.bound = 'true';

    button.addEventListener('click', event => {
      event.preventDefault();

      const input = $('signupPassword');
      if (!input) return;

      const showing = input.type === 'text';
      input.type = showing ? 'password' : 'text';
      button.textContent = showing ? '◉' : '◌';
      button.setAttribute(
        'aria-label',
        showing ? 'Show password' : 'Hide password'
      );
      input.focus();
    });
  }

  function bind(root) {
    bindPassword();

    root
      .querySelectorAll('button[data-signup-action]')
      .forEach(button => {
        button.addEventListener('click', event => {
          event.preventDefault();
          event.stopPropagation();
          void run(button);
        });
      });
  }

  async function run(button) {
    if (!button || busy || button.disabled) return;

    const action = button.dataset.signupAction;

    if (action === 'login') {
      return window.IndooneAuthUI?.showLogin?.();
    }

    const auth = window.IndooneFirebaseAuth;
    if (!auth) {
      return status(
        'Authentication service is still loading. Please try again.',
        true
      );
    }

    busy = true;
    const previous = button.textContent;

    button.disabled = true;
    button.setAttribute('aria-busy', 'true');
    status('');

    try {
      if (action === 'submit') {
        button.textContent = 'Sending OTP…';
        await auth.startSignup();
        button.textContent = 'Resend OTP';
        status('OTP sent. Enter the code from your email.');
      } else if (action === 'verify') {
        button.textContent = 'Verifying…';
        await auth.finishSignupAfterOtp();
      }
    } catch (error) {
      const message =
        typeof auth.errorMessage === 'function'
          ? auth.errorMessage(error)
          : error?.message || 'Authentication failed.';

      status(message, true);
      console.error('Indoone signup action failed:', error);
    } finally {
      if (button.isConnected) {
        if (
          button.dataset.signupAction === 'submit' &&
          button.textContent === 'Resend OTP'
        ) {
          button.disabled = false;
        }

        if (button.textContent !== 'Resend OTP') {
          button.textContent = previous;
        }

        button.removeAttribute('aria-busy');
      }

      busy = false;
    }
  }

  window.IndooneSignupFeature = {
    html,
    show: () => showShell(html())
  };
})();
