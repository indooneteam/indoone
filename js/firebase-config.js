(() => {
  // The app must never be visually blank while JavaScript/Firebase starts.
  // Build a real login screen before any optional startup dependency runs.
  function bootstrapLoginScreen() {
    const overlay = document.getElementById('overlay');
    const modal = document.getElementById('modal');
    if (!overlay || !modal) return;

    overlay.classList.remove('hidden');
    overlay.classList.add('indoone-auth-screen');
    document.body.classList.add('auth-open');
    modal.innerHTML = `
      <div class="auth-page">
        <div class="auth-brand"><span class="auth-mark">I</span><div><strong>Indoone</strong><small>Authenticator</small></div></div>
        <div class="auth-copy"><p class="eyebrow">SECURE &amp; PRIVATE</p><h1>Welcome back</h1><p>Sign in to protect and sync your authenticator vault.</p></div>
        <div class="field"><label>EMAIL OR MOBILE NUMBER</label><input id="authIdentifier" autocomplete="username" placeholder="you@example.com or +91..." /></div>
        <div class="field"><label>PASSWORD</label><input id="authPassword" type="password" autocomplete="current-password" placeholder="Enter your password" /></div>
        <button type="button" class="primary" data-auth-login>Continue &amp; Send OTP</button>
        <div id="loginOtpArea" class="auth-otp-area" hidden><p class="auth-otp-note">OTP sent to <strong id="loginOtpEmail"></strong></p><div class="field"><label>VERIFICATION OTP</label><input id="loginOtp" inputmode="numeric" autocomplete="one-time-code" maxlength="6" placeholder="Enter 6-digit OTP" /></div><button type="button" class="primary" data-auth-verify-login>Verify &amp; Login</button><button type="button" class="secondary" data-auth-resend-login>Resend OTP</button></div>
        <button type="button" class="secondary" data-auth-signup>Create Account</button>
        <div class="auth-footer">Password is checked by Firebase first; login is completed only after OTP verification.</div>
      </div>`;
  }

  // This file is loaded before auth-ui.js, so the first paint is guaranteed.
  try { bootstrapLoginScreen(); } catch (error) { console.error('Indoone login bootstrap failed:', error); }

  const firebaseConfig = {
    apiKey: "AIzaSyCwp4d_vMD44UBK38WtVq7vF8CHT3QzA8c",
    authDomain: "indoone.firebaseapp.com",
    databaseURL: "https://indoone-default-rtdb.asia-southeast1.firebasedatabase.app/",
    projectId: "indoone",
    storageBucket: "indoone.firebasestorage.app",
    messagingSenderId: "715011875230",
    appId: "1:715011875230:web:4de5306bb94a0388e0b68f",
    measurementId: "G-RE8HGJ64GR"
  };

  if (!window.firebase) {
    console.error('Indoone Firebase SDK is not loaded.');
    return;
  }

  const app = firebase.apps.length ? firebase.app() : firebase.initializeApp(firebaseConfig);
  const auth = firebase.auth();
  const database = firebase.database ? firebase.database() : null;

  window.IndooneFirebase = { app, auth, database, config: firebaseConfig };
})();