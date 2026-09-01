window.IndooneAuthUI = (() => {
  function showLogin() {
    openModal(`<div class="modal-head"><h2>Welcome to Indoone</h2><button class="close-btn" data-close>×</button></div><p style="margin:0 0 18px;color:#667085">Sign in to sync and protect your authenticator vault.</p><div class="field"><label>EMAIL OR MOBILE NUMBER</label><input id="authIdentifier" autocomplete="username" placeholder="you@example.com or +91..." /></div><div class="field"><label>PASSWORD</label><input id="authPassword" type="password" autocomplete="current-password" placeholder="Enter your password" /></div><button class="primary" data-auth-login>Login &amp; Verify</button><button class="secondary" data-auth-signup> Create Account</button>`);
  }

  function showSignup() {
    openModal(`<div class="modal-head"><h2>Create Indoone Account</h2><button class="close-btn" data-close>×</button></div><p style="margin:0 0 18px;color:#667085">Create your Indoone account to secure and sync your vault.</p><div class="field"><label>EMAIL ID</label><input id="signupEmail" type="email" autocomplete="email" placeholder="you@example.com" /></div><div class="field"><label>MOBILE NUMBER</label><input id="signupMobile" type="tel" autocomplete="tel" placeholder="+91 98765 43210" /></div><div class="field"><label>PASSWORD</label><input id="signupPassword" type="password" autocomplete="new-password" placeholder="Create a strong password" /></div><button class="primary" data-auth-send-otp>Send OTP</button><div id="signupOtpArea" hidden><div class="field"><label>OTP</label><input id="signupOtp" inputmode="numeric" autocomplete="one-time-code" maxlength="6" placeholder="Enter 6-digit OTP" /></div><button class="primary" data-auth-verify-signup>Verify &amp; Create Account</button></div><button class="secondary" data-auth-login>Already have an account? Login</button>`);
  }

  function loginFromModal() { showLogin(); }
  function signupFromModal() { showSignup(); }

  window.IndooneAuthUI = { showLogin, showSignup, loginFromModal, signupFromModal };
  return window.IndooneAuthUI;
})();