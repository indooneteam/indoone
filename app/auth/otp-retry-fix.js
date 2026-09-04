(() => {
  // Preserve the active login OTP challenge after an invalid code.
  // The server remains the source of truth for OTP expiry and attempt limits.
  const authApi = window.IndooneFirebaseAuth;
  const verificationApi = window.IndooneIndoVerification;
  const firebaseApi = window.IndooneFirebase;

  if (!authApi || !verificationApi || !firebaseApi) return;

  function updateMobilePrefix(input) {
    if (!input) return;

    const value = String(input.value || '');
    const isEmailLike = /[A-Za-z@]/.test(value);
    const prefixId =
      input.id === 'authIdentifier'
        ? 'loginMobilePrefix'
        : 'signupMobilePrefix';
    const prefix = document.getElementById(prefixId);
    const hasMobileInput = !isEmailLike && /\d/.test(value);

    if (prefix) {
      prefix.hidden = !hasMobileInput;
    }

    input.classList.toggle('has-mobile-prefix', hasMobileInput);
  }

  function installMobileInputStabilityFix() {
    if (window.__indooneMobileInputStabilityFixInstalled) return;

    window.__indooneMobileInputStabilityFixInstalled = true;

    // Presentation-only listener. Never rewrite the value, selection, focus,
    // type, or inputmode, and never cancel propagation from the input element.
    document.addEventListener('input', event => {
      const input = event.target;

      if (!(input instanceof HTMLInputElement)) return;
      if (input.id !== 'authIdentifier' && input.id !== 'signupMobile') {
        return;
      }

      updateMobilePrefix(input);
    });

    document.addEventListener('paste', event => {
      const input = event.target;

      if (!(input instanceof HTMLInputElement)) return;
      if (input.id !== 'authIdentifier' && input.id !== 'signupMobile') {
        return;
      }

      window.setTimeout(() => {
        updateMobilePrefix(input);
      }, 0);
    });
  }

  authApi.verifyLoginOtp = async function verifyLoginOtpWithoutClearingChallenge() {
    const pending = window.__indooneLoginOtp;

    if (!pending?.challengeId) {
      throw new Error('Please request a new OTP.');
    }

    const otp = String(
      document.getElementById('loginOtp')?.value || ''
    ).replace(/\D/g, '');

    if (!/^\d{6}$/.test(otp)) {
      throw new Error('Enter the 6-digit OTP.');
    }

    const result = await verificationApi.verifyLoginOtp({
      email: pending.email,
      challengeId: pending.challengeId,
      otp,
      name: 'Indoone user'
    });

    if (!result?.verified) {
      throw new Error(result?.error || 'OTP verification failed.');
    }

    const user = firebaseApi.auth?.currentUser;

    if (!user) {
      throw new Error('Login session expired. Please login again.');
    }

    const snapshot = await firebaseApi.database
      .ref(`users/${user.uid}/profile`)
      .once('value');

    await authApi.syncProfile(
      user,
      snapshot.val() || { email: pending.email }
    );

    localStorage.setItem('indoone_otp_verified_uid', user.uid);
    localStorage.setItem('indoone_authenticated_uid', user.uid);
    sessionStorage.setItem('indoone_otp_verified_uid', user.uid);
    sessionStorage.setItem('indoone_authenticated_uid', user.uid);
    window.__indooneAuthPending = false;
    window.__indooneLoginOtp = null;

    if (typeof toast === 'function') {
      toast('Login successful.');
    }

    window.IndooneAuthUI?.close?.();

    if (typeof renderAccounts === 'function') {
      renderAccounts();
    }
  };

  installMobileInputStabilityFix();
})();
