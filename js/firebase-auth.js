(() => {
  const getFirebase = () => window.IndooneFirebase;
  const cleanEmail = value => String(value || '').trim().toLowerCase();
  const normalizeMobile = value => String(value || '').replace(/[^0-9+]/g, '').replace(/^00/, '+');

  function errorMessage(error) {
    const map = {
      'auth/invalid-email': 'Enter a valid email address.',
      'auth/user-not-found': 'No Indoone account was found.',
      'auth/wrong-password': 'Incorrect password.',
      'auth/invalid-credential': 'Email or password is incorrect.',
      'auth/too-many-requests': 'Too many attempts. Please try again later.',
      'auth/network-request-failed': 'Network error. Check your connection and try again.',
      'auth/email-already-in-use': 'An account already exists with this email.',
      'auth/weak-password': 'Password should be at least 6 characters.'
    };
    return map[error?.code] || error?.message || 'Authentication failed.';
  }

  function toastSafe(text) {
    if (typeof toast === 'function') toast(text); else console.warn(text);
  }

  function db() {
    const fb = getFirebase();
    if (!fb?.database) throw new Error('Firebase Realtime Database is not initialized.');
    return fb.database;
  }

  async function emailForMobile(mobile) {
    const snapshot = await db().ref(`mobileIndex/${encodeURIComponent(mobile)}`).once('value');
    const value = snapshot.val();
    if (!value?.email) throw new Error('No Indoone account is linked to this mobile number.');
    return value.email;
  }

  async function syncProfile(user, profile = {}) {
    if (!user) return;
    const now = Date.now();
    const payload = {
      uid: user.uid,
      email: user.email || profile.email || '',
      mobile: profile.mobile || '',
      updatedAt: now
    };
    await db().ref(`users/${user.uid}/profile`).update(payload);
    if (payload.mobile) {
      await db().ref(`mobileIndex/${encodeURIComponent(normalizeMobile(payload.mobile))}`).set({
        uid: user.uid,
        email: payload.email,
        updatedAt: now
      });
    }
  }

  async function login() {
    const fb = getFirebase();
    if (!fb?.auth) throw new Error('Firebase Authentication is not initialized.');
    const identifier = String(document.getElementById('authIdentifier')?.value || '').trim();
    const password = String(document.getElementById('authPassword')?.value || '');
    if (!identifier || !password) throw new Error('Enter your email/mobile number and password.');

    const email = identifier.includes('@') ? cleanEmail(identifier) : await emailForMobile(normalizeMobile(identifier));
    const credential = await fb.auth.signInWithEmailAndPassword(email, password);
    const snapshot = await db().ref(`users/${credential.user.uid}/profile`).once('value');
    await syncProfile(credential.user, snapshot.val() || { email: credential.user.email || email });
    sessionStorage.setItem('indoone_authenticated_uid', credential.user.uid);
    toastSafe('Firebase login successful.');
    window.IndooneAuthUI?.close?.();
    if (typeof renderAccounts === 'function') renderAccounts();
  }

  async function startSignup() {
    const email = cleanEmail(document.getElementById('signupEmail')?.value);
    const mobile = normalizeMobile(document.getElementById('signupMobile')?.value);
    const password = String(document.getElementById('signupPassword')?.value || '');
    if (!email || !email.includes('@')) throw new Error('Enter a valid email address.');
    if (!/^\+[1-9]\d{7,14}$/.test(mobile)) throw new Error('Enter a valid international mobile number.');
    if (password.length < 6) throw new Error('Password should be at least 6 characters.');

    // Do not persist passwords in browser storage. The password is submitted only
    // when the verified signup flow is completed.
    window.__indooneSignupDraft = { email, mobile, password, createdAt: Date.now() };
    sessionStorage.setItem('indoone_signup_identity', JSON.stringify({ email, mobile, createdAt: Date.now() }));
    const area = document.getElementById('signupOtpArea');
    if (area) area.hidden = false;
    toastSafe('Details saved. Verify the OTP before creating the Firebase account.');
  }

  async function finishSignupAfterOtp() {
    const draft = window.__indooneSignupDraft;
    if (!draft) throw new Error('Signup session expired. Enter your details again.');
    const otp = String(document.getElementById('signupOtp')?.value || '').replace(/\D/g, '');
    if (!/^\d{6}$/.test(otp)) throw new Error('Enter the 6-digit OTP.');
    throw new Error('IndoVerification OTP is not connected yet. Firebase account creation remains blocked until OTP verification is connected.');
  }

  function listenForAuthState() {
    const fb = getFirebase();
    if (!fb?.auth) return;
    fb.auth.onAuthStateChanged(async user => {
      if (!user) return;
      sessionStorage.setItem('indoone_authenticated_uid', user.uid);
      try {
        const snapshot = await db().ref(`users/${user.uid}/profile`).once('value');
        if (snapshot.exists()) return;
      } catch (error) {
        console.warn('Firebase profile sync skipped:', error);
      }
    });
  }

  window.IndooneFirebaseAuth = { login, startSignup, finishSignupAfterOtp, syncProfile, listenForAuthState, errorMessage };
  window.IndooneFirebaseAuth.listenForAuthState();
})();