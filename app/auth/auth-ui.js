window.IndooneAuthUI = (() => {
  const AUTH_CLASS = 'indoone-auth-screen';
  let actionBusy = false;

  function status(text, isError = false) {
    const node = document.getElementById('authStatus');

    if (!node) return;

    node.textContent = text || '';
    node.dataset.error = isError ? 'true' : 'false';
    node.hidden = !text;
  }

  function reportError(error) {
    const message =
      window.IndooneFirebaseAuth &&
      typeof window.IndooneFirebaseAuth.errorMessage === 'function'
        ? window.IndooneFirebaseAuth.errorMessage(error)
        : (error?.message || 'Authentication failed.');

    status(message, true);

    if (typeof toast === 'function') {
      toast(message);
    } else {
      console.error(message, error);
    }
  }

  function setBusy(button, text) {
    const previous = button.textContent;

    button.disabled = true;
    button.setAttribute('aria-busy', 'true');
    button.textContent = text;

    return () => {
      button.disabled = false;
      button.removeAttribute('aria-busy');
      button.textContent = previous;
    };
  }

  async function runAction(button) {
    if (!button || button.disabled || actionBusy) return;

    const action = button.dataset.authAction;

    if (!action) return;

    if (action === 'signup') {
      showSignup();
      return;
    }

    if (action === 'login') {
      showLogin();
      return;
    }

    actionBusy = true;
    status('', false);

    let done = () => {};

    try {
      if (action === 'login-submit') {
        done = setBusy(button, 'Checking…');
        await window.IndooneFirebaseAuth.login();
      } else if (action === 'login-verify') {
        done = setBusy(button, 'Verifying…');
        await window.IndooneFirebaseAuth.verifyLoginOtp();
      } else if (action === 'login-resend') {
        done = setBusy(button, 'Sending…');
        await window.IndooneFirebaseAuth.resendLoginOtp();
      } else if (action === 'signup-submit') {
        done = setBusy(button, 'Sending OTP…');
        status('Sending OTP…');
        await window.IndooneFirebaseAuth.startSignup();
        button.disabled = false;
        button.removeAttribute('aria-busy');
        button.textContent = 'OTP sent';
        status('OTP sent. Enter the code from your email.');
        done = () => {};
      } else if (action === 'signup-verify') {
        done = setBusy(button, 'Verifying…');
        await window.IndooneFirebaseAuth.finishSignupAfterOtp();
      }
    } catch (error) {
      reportError(error);
    } finally {
      done();
      actionBusy = false;
    }
  }

  function bindActionBridge() {
    if (window.__indooneAuthActionBridgeInstalled) return;

    const handler = event => {
      const element = event.target;

      if (!(element instanceof Element)) return;

      const button = element.closest(
        '#modal button[data-auth-action]'
      );

      if (!button) return;

      event.preventDefault();
      event.stopImmediatePropagation();
      void runAction(button);
    };

    document.addEventListener('click', handler, true);
    window.__indooneAuthActionBridgeInstalled = true;
  }

  function mount(content) {
    const overlay = document.getElementById('overlay');
    const modal = document.getElementById('modal');

    if (!overlay || !modal) return;

    bindActionBridge();
    overlay.classList.remove('hidden');
    overlay.classList.add(AUTH_CLASS);
    modal.innerHTML = `
      <div class="auth-page">
        ${content}
        <div
          id="authStatus"
          class="auth-status"
          hidden
          aria-live="polite"
        ></div>
      </div>
    `;
    document.body.classList.add('auth-open');
  }

  function showLogin() {
    mount(`
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
        <p>
          Sign in to protect and sync your authenticator vault.
        </p>
      </div>

      <div class="field">
        <label>EMAIL OR MOBILE NUMBER</label>
        <input
          id="authIdentifier"
          autocomplete="username"
          placeholder="you@example.com or +91..."
        />
      </div>

      <div class="field">
        <label>PASSWORD</label>
        <input
          id="authPassword"
          type="password"
          autocomplete="current-password"
          placeholder="Enter your password"
        />
      </div>

      <button
        type="button"
        class="primary auth-action-button"
        data-auth-action="login-submit"
      >
        Continue &amp; Send OTP
      </button>

      <div id="loginOtpArea" class="auth-otp-area" hidden>
        <p class="auth-otp-note">
          OTP sent to <strong id="loginOtpEmail"></strong>
        </p>

        <div class="field">
          <label>VERIFICATION OTP</label>
          <input
            id="loginOtp"
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

      <button
        type="button"
        class="secondary auth-action-button"
        data-auth-action="signup"
      >
        Create Account
      </button>

      <div class="auth-footer">
        Password is checked by Firebase first; login is completed only after OTP verification.
      </div>
    `);
  }

  function showSignup() {
    mount(`
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
          type="email"
          autocomplete="email"
          placeholder="you@example.com"
        />
      </div>

      <div class="field">
        <label>MOBILE NUMBER</label>
        <input
          id="signupMobile"
          type="tel"
          autocomplete="tel"
          placeholder="+91 98765 43210"
        />
      </div>

      <div class="field">
        <label>PASSWORD</label>
        <input
          id="signupPassword"
          type="password"
          autocomplete="new-password"
          placeholder="Create a strong password"
        />
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
        The Firebase account is created only after the real IndoVerification OTP is successfully verified.
      </div>
    `);
  }

  function close() {
    const overlay = document.getElementById('overlay');
    const modal = document.getElementById('modal');

    if (!overlay) return;

    overlay.classList.remove(AUTH_CLASS);
    overlay.classList.add('hidden');
    document.body.classList.remove('auth-open');

    if (modal) {
      modal.innerHTML = '';
    }
  }

  bindActionBridge();
  window.IndooneAuthUI = {
    showLogin,
    showSignup,
    close
  };

  return window.IndooneAuthUI;
})();
