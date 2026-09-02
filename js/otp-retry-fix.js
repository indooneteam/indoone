(() => {
  // Preserve the active login OTP challenge after an invalid code.
  // The server remains the source of truth for OTP expiry and attempt limits.
  const authApi = window.IndooneFirebaseAuth;
  const verificationApi = window.IndooneIndoVerification;
  const firebaseApi = window.IndooneFirebase;

  if (!authApi || !verificationApi || !firebaseApi) return;

  authApi.verifyLoginOtp = async function verifyLoginOtpWithoutClearingChallenge() {
    const pending = window.__indooneLoginOtp;
    if (!pending?.challengeId) throw new Error('Please request a new OTP.');

    const otp = String(document.getElementById('loginOtp')?.value || '').replace(/\D/g, '');
    if (!/^\d{6}$/.test(otp)) throw new Error('Enter the 6-digit OTP.');

    // On an invalid OTP, do not sign out or clear the pending challenge.
    // This allows another attempt with the same OTP while it remains valid.
    const result = await verificationApi.verifyLoginOtp({
      email: pending.email,
      challengeId: pending.challengeId,
      otp,
      name: 'Indoone user'
    });

    if (!result?.verified) throw new Error(result?.error || 'OTP verification failed.');

    const user = firebaseApi.auth?.currentUser;
    if (!user) throw new Error('Login session expired. Please login again.');

    const snapshot = await firebaseApi.database.ref(`users/${user.uid}/profile`).once('value');
    await authApi.syncProfile(user, snapshot.val() || { email: pending.email });

    sessionStorage.setItem('indoone_otp_verified_uid', user.uid);
    sessionStorage.setItem('indoone_authenticated_uid', user.uid);
    window.__indooneAuthPending = false;
    window.__indooneLoginOtp = null;

    if (typeof toast === 'function') toast('Login successful.');
    window.IndooneAuthUI?.close?.();
    if (typeof renderAccounts === 'function') renderAccounts();
  };
})();
