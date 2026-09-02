(() => {
  const getFirebase = () => window.IndooneFirebase;
  const getVerification = () => window.IndooneIndoVerification;
  const cleanEmail = value => String(value || '').trim().toLowerCase();
  const normalizeMobile = value => {
    const raw = String(value || '').trim();
    const digits = raw.replace(/\D/g, '');
    if (/^91\d{10}$/.test(digits)) return `+${digits}`;
    if (/^\d{10}$/.test(digits)) return `+91${digits}`;
    return raw.replace(/[^0-9+]/g, '').replace(/^00/, '+');
  };
  const setAuthSession = uid => {
    localStorage.setItem('indoone_otp_verified_uid', uid);
    localStorage.setItem('indoone_authenticated_uid', uid);
    sessionStorage.setItem('indoone_otp_verified_uid', uid);
    sessionStorage.setItem('indoone_authenticated_uid', uid);
  };
  const clearAuthSession = () => {
    localStorage.removeItem('indoone_otp_verified_uid');
    localStorage.removeItem('indoone_authenticated_uid');
    sessionStorage.removeItem('indoone_otp_verified_uid');
    sessionStorage.removeItem('indoone_authenticated_uid');
  };

  function errorMessage(error) {
    const map = {
      'auth/invalid-email': 'Enter a valid email address.',
      'auth/user-not-found': 'No Indoone account was found.',
      'auth/wrong-password': 'Email or password is incorrect.',
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

  function auth() {
    const fb = getFirebase();
    if (!fb?.auth) throw new Error('Firebase Authentication is not initialized.');
    return fb.auth;
  }

  function verification() {
    const api = getVerification();
    if (!api) throw new Error('IndoVerification service is not loaded.');
    return api;
  }

  async function mobileIdentity(mobile) {
    const normalizedMobile = normalizeMobile(mobile);
    const snapshot = await db().ref(`mobileIndex/${encodeURIComponent(normalizedMobile)}`).once('value');
    const value = snapshot.val();
    if (!value?.email || !value?.uid) {
      throw new Error('No Indoone account is linked to this mobile number.');
    }
    return {
      mobile: normalizedMobile,
      email: cleanEmail(value.email),
      uid: String(value.uid)
    };
  }

  async function emailForMobile(mobile) {
    return (await mobileIdentity(mobile)).email;
  }

  async function signupIdentityExists(email, mobile) {
    const normalizedEmail = cleanEmail(email);
    const normalizedMobile = normalizeMobile(mobile);

    try {
      const methods = await auth().fetchSignInMethodsForEmail(normalizedEmail);
      if (Array.isArray(methods) && methods.length > 0) {
        throw new Error('An account already exists with this email.');
      }
    } catch (error) {
      if (error?.message === 'An account already exists with this email.') throw error;
      const code = String(error?.code || '');
      if (code === 'auth/email-already-in-use') throw new Error('An account already exists with this email.');
    }

    const mobileSnapshot = await db().ref(`mobileIndex/${encodeURIComponent(normalizedMobile)}`).once('value');
    const mobileValue = mobileSnapshot.val();
    if (mobileValue?.uid || mobileValue?.email) {
      throw new Error('An account already exists with this mobile number.');
    }
  }

  async function syncProfile(user, profile = {}) {
    if (!user) return;
    const now = Date.now();
    const payload = {
      uid: user.uid,
      email: user.email || profile.email || '',
      mobile: profile.mobile ? normalizeMobile(profile.mobile) : '',
      updatedAt: now
    };
    await db().ref(`users/${user.uid}/profile`).update(payload);
    if (payload.mobile) {
      await db().ref(`mobileIndex/${encodeURIComponent(payload.mobile)}`).set({
        uid: user.uid,
        email: payload.email,
        updatedAt: now
      });
    }
  }

  async function login() {
    const identifier = String(document.getElementById('authIdentifier')?.value || '').trim();
    const password = String(document.getElementById('authPassword')?.value || '');
    if (!identifier || !password) throw new Error('Enter your email/mobile number and password.');

    const isEmail = identifier.includes('@');
    let email = '';
    let mobileLookup = null;

    if (isEmail) {
      email = cleanEmail(identifier);
    } else {
      mobileLookup = await mobileIdentity(identifier);
      email = mobileLookup.email;
    }

    clearAuthSession();
    window.__indooneAuthPending = true;

    try {
      await auth().signInWithEmailAndPassword(email, password);
      const user = auth().currentUser;
      if (!user) throw new Error('Login session expired. Please login again.');

      if (mobileLookup && String(user.uid) !== mobileLookup.uid) {
        await auth().signOut().catch(() => {});
        throw new Error('This mobile number is not linked to that Indoone account.');
      }

      const profileSnapshot = await db().ref(`users/${user.uid}/profile`).once('value');
      const profile = profileSnapshot.val() || {};

      if (mobileLookup) {
        const savedMobile = normalizeMobile(profile.mobile || '');
        if (savedMobile !== mobileLookup.mobile) {
          await auth().signOut().catch(() => {});
          throw new Error('This mobile number is not linked to that Indoone account.');
        }
      } else if (cleanEmail(profile.email || '') && cleanEmail(profile.email) !== email) {
        await auth().signOut().catch(() => {});
        throw new Error('The Firebase account profile does not match this email address.');
      }

      const result = await verification().requestLoginOtp(email, 'Indoone user');
      if (!result?.challengeId) throw new Error('OTP service did not return a challenge ID.');

      window.__indooneLoginOtp = { email, challengeId: result.challengeId, requestedAt: Date.now() };
      const area = document.getElementById('loginOtpArea');
      if (area) area.hidden = false;
      const label = document.getElementById('loginOtpEmail');
      if (label) label.textContent = email;
      const input = document.getElementById('loginOtp');
      if (input) { input.value = ''; input.focus(); }
      toastSafe('OTP sent. Check your email to finish login.');
    } catch (error) {
      await auth().signOut().catch(() => {});
      window.__indooneAuthPending = false;
      window.__indooneLoginOtp = null;
      clearAuthSession();
      throw error;
    }
  }

  async function verifyLoginOtp() {
    const pending = window.__indooneLoginOtp;
    if (!pending?.challengeId) throw new Error('Please request a new OTP.');
    const otp = String(document.getElementById('loginOtp')?.value || '').replace(/\D/g, '');
    if (!/^\d{6}$/.test(otp)) throw new Error('Enter the 6-digit OTP.');

    try {
      const result = await verification().verifyLoginOtp({ email: pending.email, challengeId: pending.challengeId, otp, name: 'Indoone user' });
      if (!result?.verified) throw new Error(result?.error || 'OTP verification failed.');

      const user = auth().currentUser;
      if (!user) throw new Error('Login session expired. Please login again.');
      const snapshot = await db().ref(`users/${user.uid}/profile`).once('value');
      await syncProfile(user, snapshot.val() || { email: pending.email });
      setAuthSession(user.uid);
      window.__indooneAuthPending = false;
      window.__indooneLoginOtp = null;
      toastSafe('Login successful.');
      window.IndooneAuthUI?.close?.();
      if (typeof window.loadFirebaseAccounts === 'function') await window.loadFirebaseAccounts();
      if (typeof renderAccounts === 'function') renderAccounts();
    } catch (error) {
      await auth().signOut().catch(() => {});
      window.__indooneAuthPending = false;
      window.__indooneLoginOtp = null;
      clearAuthSession();
      throw error;
    }
  }

  async function resendLoginOtp() {
    const pending = window.__indooneLoginOtp;
    if (!pending?.email) throw new Error('Login session expired. Please login again.');
    const result = await verification().resendOtp(pending.email, 'login');
    if (!result?.challengeId) throw new Error('OTP service did not return a new challenge ID.');
    pending.challengeId = result.challengeId;
    pending.requestedAt = Date.now();
    const input = document.getElementById('loginOtp');
    if (input) { input.value = ''; input.focus(); }
    toastSafe('New OTP sent to your email.');
  }

  async function startSignup() {
    const email = cleanEmail(document.getElementById('signupEmail')?.value);
    const mobile = normalizeMobile(document.getElementById('signupMobile')?.value);
    const password = String(document.getElementById('signupPassword')?.value || '');
    if (!email || !email.includes('@')) throw new Error('Enter a valid email address.');
    if (!/^\+91\d{10}$/.test(mobile)) throw new Error('Enter a valid 10-digit Indian mobile number.');
    if (password.length < 6) throw new Error('Password should be at least 6 characters.');

    await signupIdentityExists(email, mobile);

    const result = await verification().requestSignupOtp(email, 'Indoone user');
    if (!result?.challengeId) throw new Error('OTP service did not return a challenge ID.');
    window.__indooneSignupDraft = { email, mobile, password, challengeId: result.challengeId, createdAt: Date.now() };
    const area = document.getElementById('signupOtpArea');
    if (area) area.hidden = false;
    const label = document.getElementById('signupOtpEmail');
    if (label) label.textContent = email;
    const input = document.getElementById('signupOtp');
    if (input) { input.value = ''; input.focus(); }
    toastSafe('OTP sent. Check your email to finish account creation.');
  }

  async function finishSignupAfterOtp() {
    const draft = window.__indooneSignupDraft;
    if (!draft?.challengeId) throw new Error('Signup session expired. Enter your details again.');
    const otp = String(document.getElementById('signupOtp')?.value || '').replace(/\D/g, '');
    if (!/^\d{6}$/.test(otp)) throw new Error('Enter the 6-digit OTP.');

    const result = await verification().verifySignupOtp({ email: draft.email, challengeId: draft.challengeId, otp, name: 'Indoone user' });
    if (!result?.verified) throw new Error(result?.error || 'OTP verification failed.');

    window.__indooneAuthPending = true;
    try {
      await signupIdentityExists(draft.email, draft.mobile);

      const credential = await auth().createUserWithEmailAndPassword(draft.email, draft.password);
      await syncProfile(credential.user, { email: draft.email, mobile: draft.mobile });

      let welcomeSent = true;
      try {
        await verification().sendSignupWelcome({
          email: draft.email,
          name: 'Indoone user',
          welcomeToken: result.welcomeToken
        });
      } catch (error) {
        welcomeSent = false;
        console.warn('Indoone welcome email failed:', error);
      }

      setAuthSession(credential.user.uid);
      window.__indooneSignupDraft = null;
      sessionStorage.removeItem('indoone_signup_identity');
      toastSafe(welcomeSent ? 'Account created successfully.' : 'Account created. Welcome email could not be sent.');
      window.__indooneAuthPending = false;
      window.IndooneAuthUI?.close?.();
      if (typeof window.loadFirebaseAccounts === 'function') await window.loadFirebaseAccounts();
      if (typeof renderAccounts === 'function') renderAccounts();
    } catch (error) {
      await auth().signOut().catch(() => {});
      window.__indooneAuthPending = false;
      window.__indooneSignupDraft = null;
      clearAuthSession();
      throw error;
    }
  }

  window.IndooneFirebaseAuth = {
    login,
    verifyLoginOtp,
    resendLoginOtp,
    startSignup,
    finishSignupAfterOtp,
    syncProfile,
    errorMessage
  };
})();
