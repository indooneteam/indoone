(() => {
  const VERIFIED_KEY = 'indoone_otp_verified_uid';
  const AUTH_KEY = 'indoone_authenticated_uid';

  const copyVerifiedSessionToLocal = () => {
    const verifiedUid = sessionStorage.getItem(VERIFIED_KEY);
    const authUid = sessionStorage.getItem(AUTH_KEY);
    if (verifiedUid) localStorage.setItem(VERIFIED_KEY, verifiedUid);
    if (authUid) localStorage.setItem(AUTH_KEY, authUid);
  };

  const restorePersistentSession = () => {
    const verifiedUid = localStorage.getItem(VERIFIED_KEY);
    const authUid = localStorage.getItem(AUTH_KEY);
    if (verifiedUid) sessionStorage.setItem(VERIFIED_KEY, verifiedUid);
    if (authUid) sessionStorage.setItem(AUTH_KEY, authUid);
  };

  restorePersistentSession();

  const auth = window.IndooneFirebaseAuth;
  if (!auth || auth.__persistentSessionWrapped) return;

  const wrap = name => {
    if (typeof auth[name] !== 'function') return;
    const original = auth[name];
    auth[name] = async (...args) => {
      const result = await original(...args);
      copyVerifiedSessionToLocal();
      return result;
    };
  };

  wrap('verifyLoginOtp');
  wrap('finishSignupAfterOtp');
  auth.__persistentSessionWrapped = true;
})();
