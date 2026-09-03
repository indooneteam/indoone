(() => {
  const AUTH_CLASS = 'indoone-auth-screen';
  let busy = false;
  const $ = id => document.getElementById(id);

  function mobileField() {
    return `<div class="mobile-field"><span id="loginMobilePrefix" class="mobile-prefix" hidden>+91</span><input id="authIdentifier" name="login-identifier" type="text" inputmode="text" autocomplete="username" autocapitalize="none" spellcheck="false" maxlength="320" placeholder="you@example.com or 98765 43210" /></div>`;
  }

  function passwordField() {
    return `<div class="password-wrap"><input id="authPassword" name="authPassword" type="password" autocomplete="current-password" placeholder="Enter your password" /><button type="button" class="password-toggle" data-password-toggle="authPassword" aria-label="Show password" title="Show password">◉</button></div>`;
  }

  function html() {
    return `<div class="auth-brand"><span class="auth-mark">I</span><div><strong>Indoone</strong><small>Authenticator</small></div></div><div class="auth-copy"><p class="eyebrow">SECURE &amp; PRIVATE</p><h1>Welcome back</h1><p>Sign in to protect and sync your authenticator vault.</p></div><div class="field"><label>EMAIL OR MOBILE NUMBER</label>${mobileField()}</div><div class="field"><label>PASSWORD</label>${passwordField()}</div><button type="button" class="primary auth-action-button" data-login-action="submit">Send OTP</button><div id="loginOtpArea" hidden><p class="auth-otp-note">OTP sent to <strong id="loginOtpEmail"></strong></p><div class="field"><label>VERIFICATION OTP</label><input id="loginOtp" name="one-time-code" inputmode="numeric" autocomplete="one-time-code" maxlength="6" placeholder="Enter 6-digit OTP" /></div><button type="button" class="primary auth-action-button" data-login-action="verify">Verify &amp; Login</button><button type="button" class="secondary auth-action-button" data-login-action="resend">Resend OTP</button></div><button type="button" class="secondary auth-action-button" data-login-action="signup">Create Account</button><div class="auth-footer">Protect your Indoone account with password and email OTP verification.</div>`;
  }

  function showShell(markup) {
    const overlay = $('overlay');
    const modal = $('modal');
    if (!overlay || !modal) return;
    overlay.classList.remove('hidden');
    overlay.classList.add(AUTH_CLASS);
    document.body.classList.add('auth-open','auth-pending');
    modal.innerHTML = `<div class="auth-page auth-login-page">${markup}<div id="authStatus" class="auth-status" hidden aria-live="polite"></div></div>`;
    bindFields(modal);
    bindActions(modal);
  }

  function bindFields(root) {
    const input = root.querySelector('#authIdentifier');
    const prefix = root.querySelector('#loginMobilePrefix');
    const normalize = () => {
      if (!input) return;
      const raw = String(input.value || '');
      if (/[A-Za-z@]/.test(raw)) {
        prefix?.setAttribute('hidden','');
        input.classList.remove('has-mobile-prefix');
        return;
      }
      const digits = raw.replace(/\D/g,'');
      const value = /^91\d{10}$/.test(digits) ? digits.slice(2) : digits.slice(0,10);
      input.value = value;
      prefix?.toggleAttribute('hidden', value.length === 0);
      input.classList.toggle('has-mobile-prefix', value.length > 0);
    };
    ['input','change','blur','focus'].forEach(type => input?.addEventListener(type,normalize));
    input?.addEventListener('paste',() => setTimeout(normalize,0));
    [50,250,700].forEach(delay => setTimeout(normalize,delay));
    root.querySelector('[data-password-toggle]')?.addEventListener('click',event => {
      event.preventDefault();
      const button = event.currentTarget;
      const password = $('authPassword');
      if (!password) return;
      const show = password.type === 'password';
      password.type = show ? 'text' : 'password';
      button.textContent = show ? '◌' : '◉';
      button.setAttribute('aria-label',show ? 'Hide password' : 'Show password');
      password.focus();
    });
  }

  function setStatus(message = '', error = false) {
    const node = $('authStatus');
    if (!node) return;
    node.textContent = message;
    node.hidden = !message;
    node.dataset.error = error ? 'true' : 'false';
  }

  async function run(button, action) {
    if (busy) return;
    if (action === 'signup') return window.IndooneAuthUI?.showSignup?.();
    const auth = window.IndooneFirebaseAuth;
    if (!auth) return setStatus('Authentication service is still loading. Please try again.',true);
    busy = true;
    const old = button.textContent;
    button.disabled = true;
    button.setAttribute('aria-busy','true');
    try {
      if (action === 'submit') {
        button.textContent = 'Checking…';
        await auth.login();
        $('loginOtpArea')?.removeAttribute('hidden');
        const email = window.__indooneLoginOtp?.email || '';
        const label = $('loginOtpEmail');
        if (label) label.textContent = email;
        $('loginOtp')?.focus();
        setStatus('OTP sent. Enter the code from your email.');
      } else if (action === 'verify') {
        button.textContent = 'Verifying…';
        await auth.verifyLoginOtp();
      } else if (action === 'resend') {
        button.textContent = 'Sending…';
        await auth.resendLoginOtp();
        $('loginOtp')?.focus();
        setStatus('New OTP sent. Check your email.');
      }
    } catch (error) {
      const message = typeof auth.errorMessage === 'function' ? auth.errorMessage(error) : (error?.message || 'Authentication failed.');
      setStatus(message,true);
      console.error('Indoone login failed:',error);
    } finally {
      if (button.isConnected) {
        button.disabled = false;
        button.removeAttribute('aria-busy');
        button.textContent = action === 'resend' ? 'Resend OTP' : old;
      }
      busy = false;
    }
  }

  function bindActions(root) {
    root.querySelectorAll('[data-login-action]').forEach(button => {
      button.addEventListener('click',event => {
        event.preventDefault();
        event.stopPropagation();
        void run(button,button.dataset.loginAction);
      });
    });
  }

  window.IndooneLoginFeature = { html, show: () => showShell(html()) };

  document.addEventListener('DOMContentLoaded',() => {
    setTimeout(() => {
      if (!localStorage.getItem('indoone_otp_verified_uid')) showShell(html());
    },0);
  },{ once: true });
})();
