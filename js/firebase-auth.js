(() => {
  const getFirebase = () => window.IndooneFirebase;

  function cleanEmail(value) {
    return String(value || '').trim().toLowerCase();
  }

  function normalizeMobile(value) {
    return String(value || '').replace(/[^0-9+]/g, '').replace(/^00/, '+');
  }

  function messageFor(error) {
    const code = error && error.code ? error.code : '';
    const map = {
      'auth/invalid-email': 'Enter a valid email address.',
      'auth/user-not-found': 'No Indoone account was found.',
      'auth/wrong-password': 'Incorrect password.',
      'auth/invalid-credential': 'Email or password is incorrect.',
      'auth/too-many-requests': 'Too many attempts. Please try again later.',
      'auth/email-already-in-use': 'An account already exists with this email.',
      'auth/weak-password': 'Password should be at least 6 characters.',
      'auth/network-request-failed': 'Network error. Check your connection and try again.'
    };
    return map[code] || (error && error.message) || 'Authentication failed.';
  }

  function toastSafe(text) {
    if (typeof toast === 'function') toast(text);
    else console.warn(text);
  }

  async function findEmailByMobile(mobile) {
    const fb = getFirebase();
    if (!fb || !fb.db) throw new Error('Firebase database is not available.');
    const snapshot = await fb.db.ref('mobileIndex').child(encodeURIComponent(mobile)).once('value');
    const value = snapshot.val();
    if (!value || !value.email) throw new Error('No Indoone account is linked to this mobile number.');
    return value.email;
  }

  async function syncProfile(user, profile) {
    const fb = getFirebase();
    if (!fb || !fb.db || !user) return;
    const now = Date.now();
    const payload = {
      uid: user.uid,
      email: user.email || profile.email || '',
      mobile: profile.mobile || '',
      updatedAt: now
    };
    await fb.db.ref(`users/${user.uid}/profile`).update(payload);
    if (profile.mobile) {
      await fb.db.ref(`mobileIndex/${encodeURIComponent(normalizeMobile(profile.mobile))}`).set({
        uid: user.uid,
        email: payload.email,
        updatedAt: now
      });
    }
  }

  async function login() {
    const fb = getFirebase();
    if (!fb || !fb.auth) throw new Error('Firebase Authentication is not initialized.');
    const identifier = String(document.getElementById('authIdentifier')?.value || '').trim();
    const password = String(document.getElementById('authPassword')?.value || '');
    if (!identifier || !password) throw new Error('Enter your email/mobile number and password.');

    const email = identifier.includes('@') ? cleanEmail(identifier) : await findEmailByMobile(normalizeMobile(identifier));
    const credential = await fb.auth.signInWithEmailAndPassword(email, password);
    const snapshot = await fb.db.ref(`users/${credential.user.uid}/profile`).once('value');
    const profile = snapshot.val() || { email: credential.user.email || email };
    await syncProfile(credential.user, profile);
    toastSafe('Firebase login successful.');
    if (window.IndooneAuthUI && typeof window.IndooneAuthUI.close === 'function') window.IndooneAuthUI.close();
    if (typeof renderAccounts === 'function') renderAccounts();
  }

  async function startSignup() {
    const email = cleanEmail(document.getElementById('signupEmail')?.value);
    const mobile = normalizeMobile(document.getElementById('signupMobile')?.value);
    const password = String(document.getElementById('signupPassword')?.value || '');
    if (!email || !email.includes('@')) throw new Error('Enter a valid email address.');
    if (!/^\+[1-9]\d{7,14}$/.test(mobile)) throw new Error('Enter a valid international mobile number.');
    if (password.length < 6) throw new Error('Password should be at least 6 characters.');

    sessionStorage.setItem('indoone_signup_draft', JSON.stringify({ email, mobile, password, createdAt: Date.now() }));
    const area = document.getElementById('signupOtpArea');
    if (area) area.hidden = false;
    toastSafe('Signup details saved. OTP verification is required before account creation.');
  }

  async function finishSignupAfterOtp() {
    const raw = sessionStorage.getItem('indoone_signup_draft');
    if (!raw) throw new Error('Signup session expired. Enter your details again.');
    const draft = JSON.parse(raw);
    const enteredOtp = String(document.getElementById('signupOtp')?.value || '').replace(/\D/g, '');
    if (!/^\d{6}$/.test(enteredOtp)) throw new Error('Enter the 6-digit OTP.');
    throw new Error('IndoVerification OTP is not connected yet. Account creation is intentionally blocked until OTP verification is wired.');
  }

  window.IndooneFirebaseAuth = {
    login,
    startSignup,
    finishSignupAfterOtp,
    syncProfile
  };
})();