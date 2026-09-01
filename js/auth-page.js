(() => {
  const AUTH_CLASS = 'indoone-auth-screen';
  let busy = false;
  const $ = id => document.getElementById(id);

  function setStatus(message = '', error = false) {
    const node = $('authStatus');
    if (!node) return;
    node.textContent = message;
    node.hidden = !message;
    node.dataset.error = error ? 'true' : 'false';
  }

  function showShell(html) {
    const overlay = $('overlay');
    const modal = $('modal');
    if (!overlay || !modal) return false;
    overlay.classList.remove('hidden');
    overlay.classList.add(AUTH_CLASS);
    document.body.classList.add('auth-open', 'auth-pending');
    modal.innerHTML = `<div class="auth-page">${html}<div id="authStatus" class="auth-status" hidden aria-live="polite"></div></div>`;
    bindButtons(modal);
    return true;
  }

  function showLogin() {
    showShell(`<div class="auth-brand"><span class="auth-mark">I</span><div><strong>Indoone</strong><small>Authenticator</small></div></div><div class="auth-copy"><p class="eyebrow">SECURE &amp; PRIVATE</p><h1>Welcome back</h1><p>Sign in to protect and sync your authenticator vault.</p></div><div class="field"><label>EMAIL OR MOBILE NUMBER</label><input id="authIdentifier" autocomplete="username" placeholder="you@example.com or +91..." /></div><div class="field"><label>PASSWORD</label><input id="authPassword" type="password" autocomplete="current-password" placeholder="Enter your password" /></div><button type="button" class="primary auth-action-button" data-auth-action="login-submit">Continue &amp; Send OTP</button><div id="loginOtpArea" class="auth-otp-area" hidden><p class="auth-otp-note">OTP sent to <strong id="loginOtpEmail"></strong></p><div class="field"><label>VERIFICATION OTP</label><input id="loginOtp" inputmode="numeric" autocomplete="one-time-code" maxlength="6" placeholder="Enter 6-digit OTP" /></div><button type="button" class="primary auth-action-button" data-auth-action="login-verify">Verify &amp; Login</button><button type="button" class="secondary auth-action-button" data-auth-action="login-resend">Resend OTP</button></div><button type="button" class="secondary auth-action-button" data-auth-action="signup">Create Account</button><div class="auth-footer">Password is checked by Firebase first; login is completed only after OTP verification.</div>`);
  }

  function showSignup() {
    showShell(`<div class="auth-brand"><span class="auth-mark">I</span><div><strong>Indoone</strong><small>Authenticator</small></div></div><div class="auth-copy"><p class="eyebrow">GET STARTED</p><h1>Create your account</h1><p>Securely create an Indoone account for your authenticator vault.</p></div><div class="field"><label>EMAIL ID</label><input id="signupEmail" type="email" autocomplete="email" placeholder="you@example.com" /></div><div class="field"><label>MOBILE NUMBER</label><input id="signupMobile" type="tel" autocomplete="tel" placeholder="+91 98765 43210" /></div><div class="field"><label>PASSWORD</label><input id="signupPassword" type="password" autocomplete="new-password" placeholder="Create a strong password" /></div><button type="button" class="primary auth-action-button" data-auth-action="signup-submit">Send OTP</button><div id="signupOtpArea" hidden><p class="auth-otp-note">OTP sent to <strong id="signupOtpEmail"></strong></p><div class="field"><label>VERIFICATION OTP</label><input id="signupOtp" inputmode="numeric" autocomplete="one-time-code" maxlength="6" placeholder="Enter 6-digit OTP" /></div><button type="button" class="primary auth-action-button" data-auth-action="signup-verify">Verify &amp; Create Account</button></div><button type="button" class="secondary auth-action-button" data-auth-action="login">Already have an account? Login</button><div class="auth-footer">The Firebase account is created only after the real IndoVerification OTP is successfully verified.</div>`);
  }

  async function run(button) {
    if (!button || busy || button.disabled) return;
    const action = button.dataset.authAction;
    if (action === 'signup') return showSignup();
    if (action === 'login') return showLogin();
    const auth = window.IndooneFirebaseAuth;
    if (!auth) return setStatus('Authentication service is still loading. Please try again.', true);

    busy = true;
    const previous = button.textContent;
    button.disabled = true;
    button.setAttribute('aria-busy', 'true');
    setStatus('', false);

    try {
      if (action === 'login-submit') {
        button.textContent = 'Checking…';
        await auth.login();
      } else if (action === 'login-verify') {
        button.textContent = 'Verifying…';
        await auth.verifyLoginOtp();
      } else if (action === 'login-resend') {
        button.textContent = 'Sending…';
        await auth.resendLoginOtp();
      } else if (action === 'signup-submit') {
        button.textContent = 'Sending OTP…';
        await auth.startSignup();
        button.textContent = 'OTP sent';
        setStatus('OTP sent. Enter the code from your email.');
        button.disabled = false;
        button.removeAttribute('aria-busy');
        busy = false;
        return;
      } else if (action === 'signup-verify') {
        button.textContent = 'Verifying…';
        await auth.finishSignupAfterOtp();
      }
    } catch (error) {
      const message = typeof auth.errorMessage === 'function' ? auth.errorMessage(error) : (error?.message || 'Authentication failed.');
      setStatus(message, true);
      console.error('Indoone auth action failed:', error);
    } finally {
      if (button.isConnected) {
        if (button.textContent !== 'OTP sent') button.textContent = previous;
        button.disabled = false;
        button.removeAttribute('aria-busy');
      }
      busy = false;
    }
  }

  function bindButtons(root) {
    root.querySelectorAll('button[data-auth-action]').forEach(button => {
      if (button.dataset.authBound === 'true') return;
      button.dataset.authBound = 'true';
      button.addEventListener('click', event => {
        event.preventDefault();
        event.stopPropagation();
        void run(button);
      });
    });
  }

  function close() {
    $('overlay')?.classList.remove(AUTH_CLASS);
    $('overlay')?.classList.add('hidden');
    document.body.classList.remove('auth-open', 'auth-pending');
    if ($('modal')) $('modal').innerHTML = '';
  }

  window.IndooneAuthUI = { showLogin, showSignup, close };

  function init() {
    showLogin();
  }

  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init, { once: true });
  else init();
})();
