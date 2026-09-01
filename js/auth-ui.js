window.IndooneAuthUI = (() => {
  const AUTH_CLASS = 'indoone-auth-screen';

  function status(text, isError = false) {
    const node = document.getElementById('authStatus');
    if (!node) return;
    node.textContent = text || '';
    node.dataset.error = isError ? 'true' : 'false';
    node.hidden = !text;
  }

  function reportError(error) {
    const message = window.IndooneFirebaseAuth && typeof window.IndooneFirebaseAuth.errorMessage === 'function'
      ? window.IndooneFirebaseAuth.errorMessage(error)
      : (error?.message || 'Authentication failed.');
    status(message, true);
    if (typeof toast === 'function') toast(message);
    else console.error(message, error);
  }

  function setBusy(button, busyText) {
    const idleText = button.dataset.idleText || button.textContent;
    button.dataset.idleText = idleText;
    button.disabled = true;
    button.setAttribute('aria-busy', 'true');
    button.textContent = busyText;
    return () => {
      button.disabled = false;
      button.removeAttribute('aria-busy');
      button.textContent = button.dataset.idleText || idleText;
    };
  }

  function bindButtons() {
    const modal = document.getElementById('modal');
    if (!modal) return;

    modal.querySelectorAll('[data-auth-action]').forEach(button => {
      button.onclick = async (event) => {
        event.preventDefault();
        event.stopPropagation();
        const action = button.dataset.authAction;

        if (action === 'signup') {
          showSignup();
          return;
        }
        if (action === 'login') {
          showLogin();
          return;
        }

        status('', false);
        try {
          if (action === 'login-submit') {
            const done = setBusy(button, 'Checking…');
            try {
              await window.IndooneFirebaseAuth.login();
            } finally {
              done();
            }
            return;
          }

          if (action === 'login-verify') {
            const done = setBusy(button, 'Verifying…');
            try {
              await window.IndooneFirebaseAuth.verifyLoginOtp();
            } finally {
              done();
            }
            return;
          }

          if (action === 'login-resend') {
            const done = setBusy(button, 'Sending…');
            try {
              await window.IndooneFirebaseAuth.resendLoginOtp();
            } finally {
              done();
            }
            return;
          }

          if (action === 'signup-submit') {
            const done = setBusy(button, 'Sending OTP…');
            status('Sending OTP…');
            try {
              await window.IndooneFirebaseAuth.startSignup();
              button.textContent = 'OTP sent';
              button.disabled = false;
              button.removeAttribute('aria-busy');
              button.dataset.idleText = 'OTP sent';
              status('OTP sent. Enter the code from your email.');
            } catch (error) {
              done();
              throw error;
            }
            return;
          }

          if (action === 'signup-verify') {
            const done = setBusy(button, 'Verifying…');
            try {
              await window.IndooneFirebaseAuth.finishSignupAfterOtp();
            } finally {
              done();
            }
          }
        } catch (error) {
          reportError(error);
        }
      };
    });
  }

  function mount(content) {
    const overlay = document.getElementById('overlay');
    const modal = document.getElementById('modal');
    if (!overlay || !modal) return;
    overlay.classList.remove('hidden');
    overlay.classList.add(AUTH_CLASS);
    modal.innerHTML = `<div class="auth-page">${content}<div id="authStatus" class="auth-status" hidden aria-live="polite"></div></div>`;
    document.body.classList.add('auth-open');
    bindButtons();
  }

  function showLogin() {
    mount(`
      <div class="auth-brand"><span class="auth-mark">I</span><div><strong>Indoone</strong><small>Authenticator</small></div></div>
      <div class="auth-copy"><p class="eyebrow">SECURE &amp; PRIVATE</p><h1>Welcome back</h1><p>Sign in to protect and sync your authenticator vault.</p></div>
      <div class="field"><label>EMAIL OR MOBILE NUMBER</label><input id="authIdentifier" autocomplete="username" placeholder="you@example.com or +91..." /></div>
      <div class="field"><label>PASSWORD</label><input id="authPassword" type="password" autocomplete="current-password" placeholder="Enter your password" /></div>
      <button type="button" class="primary" data-auth-action="login-submit">Continue &amp; Send OTP</button>
      <div id="loginOtpArea" class="auth-otp-area" hidden><p class="auth-otp-note">OTP sent to <strong id="loginOtpEmail"></strong></p><div class="field"><label>VERIFICATION OTP</label><input id="loginOtp" inputmode="numeric" autocomplete="one-time-code" maxlength="6" placeholder="Enter 6-digit OTP" /></div><button type="button" class="primary" data-auth-action="login-verify">Verify &amp; Login</button><button type="button" class="secondary" data-auth-action="login-resend">Resend OTP</button></div>
      <button type="button" class="secondary" data-auth-action="signup">Create Account</button>
      <div class="auth-footer">Password is checked by Firebase first; login is completed only after OTP verification.</div>
    `);
  }

  function showSignup() {
    mount(`
      <div class="auth-brand"><span class="auth-mark">I</span><div><strong>Indoone</strong><small>Authenticator</small></div></div>
      <div class="auth-copy"><p class="eyebrow">GET STARTED</p><h1>Create your account</h1><p>Securely create an Indoone account for your authenticator vault.</p></div>
      <div class="field"><label>EMAIL ID</label><input id="signupEmail" type="email" autocomplete="email" placeholder="you@example.com" /></div>
      <div class="field"><label>MOBILE NUMBER</label><input id="signupMobile" type="tel" autocomplete="tel" placeholder="+91 98765 43210" /></div>
      <div class="field"><label>PASSWORD</label><input id="signupPassword" type="password" autocomplete="new-password" placeholder="Create a strong password" /></div>
      <button type="button" class="primary" data-auth-action="signup-submit">Send OTP</button>
      <div id="signupOtpArea" hidden><p class="auth-otp-note">OTP sent to <strong id="signupOtpEmail"></strong></p><div class="field"><label>VERIFICATION OTP</label><input id="signupOtp" inputmode="numeric" autocomplete="one-time-code" maxlength="6" placeholder="Enter 6-digit OTP" /></div><button type="button" class="primary" data-auth-action="signup-verify">Verify &amp; Create Account</button></div>
      <button type="button" class="secondary" data-auth-action="login">Already have an account? Login</button>
      <div class="auth-footer">The Firebase account is created only after the real IndoVerification OTP is successfully verified.</div>
    `);
  }

  function close() {
    const overlay = document.getElementById('overlay');
    const modal = document.getElementById('modal');
    if (!overlay) return;
    overlay.classList.remove(AUTH_CLASS);
    overlay.classList.add('hidden');
    document.body.classList.remove('auth-open');
    if (modal) modal.innerHTML = '';
  }

  window.IndooneAuthUI = { showLogin, showSignup, close };
  return window.IndooneAuthUI;
})();
