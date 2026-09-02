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
    bindMobileDefaults(modal);
    bindPasswordToggles(modal);
    return true;
  }

  function mobileField(id, prefixId, placeholder = '98765 43210') {
    return `<div class="mobile-field"><span id="${prefixId}" class="mobile-prefix" hidden>+91</span><input id="${id}" type="tel" inputmode="numeric" autocomplete="tel" maxlength="15" placeholder="${placeholder}" /></div>`;
  }

  function syncMobilePrefix(input, prefix, rawValue = input?.value || '') {
    if (!input) return;
    const raw = String(rawValue || '');
    const isEmail = /[A-Za-z@]/.test(raw);
    const digits = raw.replace(/\D/g, '');
    const normalizedDigits = /^91\d{10}$/.test(digits) ? digits.slice(2) : digits.slice(0, 10);
    const showPrefix = !isEmail && normalizedDigits.length > 0;
    if (prefix) prefix.hidden = !showPrefix;
    input.classList.toggle('has-mobile-prefix', showPrefix);
    if (!isEmail) input.value = normalizedDigits;
  }

  function bindMobileInput(input, prefix) {
    if (!input || input.dataset.indiaMobileBound === 'true') return;
    input.dataset.indiaMobileBound = 'true';
    syncMobilePrefix(input, prefix);

    const normalize = () => {
      const raw = input.value || '';
      const isEmail = /[A-Za-z@]/.test(raw);
      if (isEmail) {
        if (prefix) prefix.hidden = true;
        input.classList.remove('has-mobile-prefix');
        return;
      }

      const digits = raw.replace(/\D/g, '');
      const normalized = /^91\d{10}$/.test(digits) ? digits.slice(2) : digits.slice(0, 10);
      input.value = normalized;
      syncMobilePrefix(input, prefix, normalized);
    };

    input.addEventListener('input', normalize);
    input.addEventListener('change', normalize);
    input.addEventListener('blur', normalize);
    input.addEventListener('paste', () => setTimeout(normalize, 0));
  }

  function bindMobileDefaults(root) {
    const loginInput = root.querySelector('#authIdentifier');
    const loginPrefix = root.querySelector('#loginMobilePrefix');
    bindMobileInput(loginInput, loginPrefix);

    const signupInput = root.querySelector('#signupMobile');
    const signupPrefix = root.querySelector('#signupMobilePrefix');
    bindMobileInput(signupInput, signupPrefix);
  }

  function bindPasswordToggles(root) {
    root.querySelectorAll('button[data-password-toggle]').forEach(button => {
      if (button.dataset.passwordToggleBound === 'true') return;
      button.dataset.passwordToggleBound = 'true';
      button.addEventListener('click', event => {
        event.preventDefault();
        event.stopPropagation();
        const inputId = button.dataset.passwordToggle;
        const input = inputId ? $(inputId) : null;
        if (!input) return;
        const showing = input.type === 'text';
        input.type = showing ? 'password' : 'text';
        button.textContent = showing ? '◉' : '◌';
        button.setAttribute('aria-label', showing ? 'Show password' : 'Hide password');
        button.setAttribute('title', showing ? 'Show password' : 'Hide password');
        input.focus();
      });
    });
  }

  function passwordField(id, autocomplete, placeholder) {
    return `<div class="password-wrap"><input id="${id}" type="password" autocomplete="${autocomplete}" placeholder="${placeholder}" /><button type="button" class="password-toggle" data-password-toggle="${id}" aria-label="Show password" title="Show password">◉</button></div>`;
  }

  function showLogin() {
    showShell(`<div class="auth-brand"><span class="auth-mark">I</span><div><strong>Indoone</strong><small>Authenticator</small></div></div><div class="auth-copy"><p class="eyebrow">SECURE &amp; PRIVATE</p><h1>Welcome back</h1><p>Sign in to protect and sync your authenticator vault.</p></div><div class="field"><label>EMAIL OR MOBILE NUMBER</label>${mobileField('authIdentifier', 'loginMobilePrefix', 'you@example.com or 98765 43210')}</div><div class="field"><label>PASSWORD</label>${passwordField('authPassword', 'current-password', 'Enter your password')}</div><button type="button" class="primary auth-action-button" data-auth-action="login-submit">Send OTP</button><div id="loginOtpArea" class="auth-otp-area" hidden><p class="auth-otp-note">OTP sent to <strong id="loginOtpEmail"></strong></p><div class="field"><label>VERIFICATION OTP</label><input id="loginOtp" inputmode="numeric" autocomplete="one-time-code" maxlength="6" placeholder="Enter 6-digit OTP" /></div><button type="button" class="primary auth-action-button" data-auth-action="login-verify">Verify &amp; Login</button></div><button type="button" class="secondary auth-action-button" data-auth-action="signup">Create Account</button><div class="auth-footer">Protect your Indoone account with password and email OTP verification.</div>`);
  }

  function showSignup() {
    showShell(`<div class="auth-brand"><span class="auth-mark">I</span><div><strong>Indoone</strong><small>Authenticator</small></div></div><div class="auth-copy"><p class="eyebrow">GET STARTED</p><h1>Create your account</h1><p>Securely create an Indoone account for your authenticator vault.</p></div><div class="field"><label>EMAIL ID</label><input id="signupEmail" type="email" autocomplete="email" placeholder="you@example.com" /></div><div class="field"><label>MOBILE NUMBER</label>${mobileField('signupMobile', 'signupMobilePrefix')}</div><div class="field"><label>PASSWORD</label>${passwordField('signupPassword', 'new-password', 'Create a strong password')}</div><button type="button" class="primary auth-action-button" data-auth-action="signup-submit">Send OTP</button><div id="signupOtpArea" hidden><p class="auth-otp-note">OTP sent to <strong id="signupOtpEmail"></strong></p><div class="field"><label>VERIFICATION OTP</label><input id="signupOtp" inputmode="numeric" autocomplete="one-time-code" maxlength="6" placeholder="Enter 6-digit OTP" /></div><button type="button" class="primary auth-action-button" data-auth-action="signup-verify">Verify &amp; Create Account</button></div><button type="button" class="secondary auth-action-button" data-auth-action="login">Already have an account? Login</button><div class="auth-footer">Your Indoone account is activated after successful email OTP verification.</div>`);
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
        button.dataset.authAction = 'login-resend';
        button.textContent = 'Resend OTP';
        setStatus('OTP sent. Enter the code from your email.');
      } else if (action === 'login-verify') {
        button.textContent = 'Verifying…';
        await auth.verifyLoginOtp();
      } else if (action === 'login-resend') {
        button.textContent = 'Sending…';
        await auth.resendLoginOtp();
        button.textContent = 'Resend OTP';
        setStatus('New OTP sent. Check your email.');
      } else if (action === 'signup-submit') {
        button.textContent = 'Sending OTP…';
        await auth.startSignup();
        button.textContent = 'Resend OTP';
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
        if (button.dataset.authAction === 'login-resend') button.textContent = 'Resend OTP';
        else if (button.textContent !== 'Resend OTP') button.textContent = previous;
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
