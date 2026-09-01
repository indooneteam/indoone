window.IndooneAuthUI = (() => {
  const AUTH_CLASS = 'indoone-auth-screen';

  function reportError(error) {
    const message = window.IndooneFirebaseAuth && typeof window.IndooneFirebaseAuth.errorMessage === 'function'
      ? window.IndooneFirebaseAuth.errorMessage(error)
      : (error?.message || 'Authentication failed.');
    if (typeof toast === 'function') toast(message);
    else console.error(message, error);
  }

  function setBusy(button, busyText, idleText) {
    if (!button) return () => {};
    const previous = button.textContent;
    button.disabled = true;
    button.setAttribute('aria-busy', 'true');
    button.textContent = busyText;
    return () => {
      button.disabled = false;
      button.removeAttribute('aria-busy');
      button.textContent = idleText || previous;
    };
  }

  function bindCommonActions() {
    const modal = document.getElementById('modal');
    if (!modal) return;

    modal.querySelectorAll('[data-auth-login]').forEach(button => button.addEventListener('click', async (event) => {
      event.preventDefault();
      event.stopPropagation();
      const done = setBusy(event.currentTarget, 'Checking…', 'Continue & Send OTP');
      try { await window.IndooneFirebaseAuth.login(); }
      catch (error) { reportError(error); }
      finally { done(); }
    }));

    modal.querySelectorAll('[data-auth-verify-login]').forEach(button => button.addEventListener('click', async (event) => {
      event.preventDefault();
      event.stopPropagation();
      const done = setBusy(event.currentTarget, 'Verifying…', 'Verify & Login');
      try { await window.IndooneFirebaseAuth.verifyLoginOtp(); }
      catch (error) { reportError(error); }
      finally { done(); }
    }));

    modal.querySelectorAll('[data-auth-resend-login]').forEach(button => button.addEventListener('click', async (event) => {
      event.preventDefault();
      event.stopPropagation();
      const done = setBusy(event.currentTarget, 'Sending…', 'Resend OTP');
      try { await window.IndooneFirebaseAuth.resendLoginOtp(); }
      catch (error) { reportError(error); }
      finally { done(); }
    }));

    modal.querySelectorAll('[data-auth-signup]').forEach(button => button.addEventListener('click', (event) => {
      event.preventDefault();
      event.stopPropagation();
      showSignup();
    }));

    modal.querySelectorAll('[data-auth-back-login]').forEach(button => button.addEventListener('click', (event) => {
      event.preventDefault();
      event.stopPropagation();
      showLogin();
    }));

    modal.querySelectorAll('[data-auth-send-otp]').forEach(button => button.addEventListener('click', async (event) => {
      event.preventDefault();
      event.stopPropagation();
      const done = setBusy(event.currentTarget, 'Sending OTP…', 'Send OTP');
      try {
        await window.IndooneFirebaseAuth.startSignup();
        event.currentTarget.disabled = false;
        event.currentTarget.removeAttribute('aria-busy');
        event.currentTarget.textContent = 'OTP sent';
      } catch (error) {
        done();
        reportError(error);
      }
    }));

    modal.querySelectorAll('[data-auth-verify-signup]').forEach(button => button.addEventListener('click', async (event) => {
      event.preventDefault();
      event.stopPropagation();
      const done = setBusy(event.currentTarget, 'Verifying…', 'Verify & Create Account');
      try { await window.IndooneFirebaseAuth.finishSignupAfterOtp(); }
      catch (error) { done(); reportError(error); }
    }));
  }

  function mount(content) {
    const overlay = document.getElementById('overlay');
    const modal = document.getElementById('modal');
    if (!overlay || !modal) return;
    overlay.classList.remove('hidden');
    overlay.classList.add(AUTH_CLASS);
    modal.innerHTML = `<div class="auth-page">${content}</div>`;
    document.body.classList.add('auth-open');
    bindCommonActions();
  }

  function showLogin() {
    mount(`
      <div class="auth-brand"><span class="auth-mark">I</span><div><strong>Indoone</strong><small>Authenticator</small></div></div>
      <div class="auth-copy"><p class="eyebrow">SECURE &amp; PRIVATE</p><h1>Welcome back</h1><p>Sign in to protect and sync your authenticator vault.</p></div>
      <div class="field"><label>EMAIL OR MOBILE NUMBER</label><input id="authIdentifier" autocomplete="username" placeholder="you@example.com or +91..." /></div>
      <div class="field"><label>PASSWORD</label><input id="authPassword" type="password" autocomplete="current-password" placeholder="Enter your password" /></div>
      <button type="button" class="primary" data-auth-login>Continue &amp; Send OTP</button>
      <div id="loginOtpArea" class="auth-otp-area" hidden><p class="auth-otp-note">OTP sent to <strong id="loginOtpEmail"></strong></p><div class="field"><label>VERIFICATION OTP</label><input id="loginOtp" inputmode="numeric" autocomplete="one-time-code" maxlength="6" placeholder="Enter 6-digit OTP" /></div><button type="button" class="primary" data-auth-verify-login>Verify &amp; Login</button><button type="button" class="secondary" data-auth-resend-login>Resend OTP</button></div>
      <button type="button" class="secondary" data-auth-signup>Create Account</button>
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
      <button type="button" class="primary" data-auth-send-otp>Send OTP</button>
      <div id="signupOtpArea" hidden><p class="auth-otp-note">OTP sent to <strong id="signupOtpEmail"></strong></p><div class="field"><label>VERIFICATION OTP</label><input id="signupOtp" inputmode="numeric" autocomplete="one-time-code" maxlength="6" placeholder="Enter 6-digit OTP" /><button type="button" class="secondary" data-auth-back-login>Back to Login</button></div><button type="button" class="primary" data-auth-verify-signup>Verify &amp; Create Account</button></div>
      <button type="button" class="secondary" data-auth-back-login>Already have an account? Login</button>
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
