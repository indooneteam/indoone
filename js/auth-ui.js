window.IndooneAuthUI = (() => {
  const AUTH_CLASS = 'indoone-auth-screen';

  function reportError(error) {
    const message = window.IndooneFirebaseAuth && typeof window.IndooneFirebaseAuth.messageFor === 'function'
      ? window.IndooneFirebaseAuth.messageFor(error)
      : (error?.message || 'Authentication failed.');
    if (typeof toast === 'function') toast(message);
    else console.error(message, error);
  }

  function bindCommonActions() {
    const modal = document.getElementById('modal');
    if (!modal) return;
    modal.querySelector('[data-auth-login]')?.addEventListener('click', async () => {
      try {
        if (!window.IndooneFirebaseAuth) throw new Error('Firebase Authentication is not ready.');
        await window.IndooneFirebaseAuth.login();
      } catch (error) { reportError(error); }
    });
    modal.querySelector('[data-auth-signup]')?.addEventListener('click', () => showSignup());
    modal.querySelector('[data-auth-back-login]')?.addEventListener('click', () => showLogin());
    modal.querySelector('[data-auth-send-otp]')?.addEventListener('click', async () => {
      try {
        if (!window.IndooneFirebaseAuth) throw new Error('Firebase Authentication is not ready.');
        await window.IndooneFirebaseAuth.startSignup();
        const button = modal.querySelector('[data-auth-send-otp]');
        if (button) { button.disabled = true; button.textContent = 'OTP step ready'; }
      } catch (error) { reportError(error); }
    });
    modal.querySelector('[data-auth-verify-signup]')?.addEventListener('click', async () => {
      try {
        if (!window.IndooneFirebaseAuth) throw new Error('Firebase Authentication is not ready.');
        await window.IndooneFirebaseAuth.finishSignupAfterOtp();
      } catch (error) { reportError(error); }
    });
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
      <button class="primary" data-auth-login>Login &amp; Verify</button>
      <button class="secondary" data-auth-signup>Create Account</button>
      <div class="auth-footer">Your OTP verification will be handled securely after backend integration.</div>
    `);
  }

  function showSignup() {
    mount(`
      <div class="auth-brand"><span class="auth-mark">I</span><div><strong>Indoone</strong><small>Authenticator</small></div></div>
      <div class="auth-copy"><p class="eyebrow">GET STARTED</p><h1>Create your account</h1><p>Securely create an Indoone account for your authenticator vault.</p></div>
      <div class="field"><label>EMAIL ID</label><input id="signupEmail" type="email" autocomplete="email" placeholder="you@example.com" /></div>
      <div class="field"><label>MOBILE NUMBER</label><input id="signupMobile" type="tel" autocomplete="tel" placeholder="+91 98765 43210" /></div>
      <div class="field"><label>PASSWORD</label><input id="signupPassword" type="password" autocomplete="new-password" placeholder="Create a strong password" /></div>
      <button class="primary" data-auth-send-otp>Send OTP</button>
      <div id="signupOtpArea" hidden><div class="field"><label>OTP</label><input id="signupOtp" inputmode="numeric" autocomplete="one-time-code" maxlength="6" placeholder="Enter 6-digit OTP" /><button type="button" class="secondary" data-auth-back-login>Back to Login</button></div><button class="primary" data-auth-verify-signup>Verify &amp; Create Account</button></div>
      <button class="secondary" data-auth-login>Already have an account? Login</button>
      <div class="auth-footer">OTP and Firebase integration are wired in stages; account creation remains blocked until the real OTP service is connected.</div>
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