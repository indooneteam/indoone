(() => {
  const VERIFIED_KEY = 'indoone_otp_verified_uid';

  function restoreOverlay(user) {
    if (!user) return;
    const verifiedUid = localStorage.getItem(VERIFIED_KEY);
    if (verifiedUid && verifiedUid === user.uid) {
      try {
        window.IndooneAuthUI?.close?.();
        document.body.classList.remove('auth-open', 'auth-pending');
        if (typeof renderAccounts === 'function') renderAccounts();
        if (typeof startDemoTimers === 'function' && !IndoonePersistence.hasVault()) startDemoTimers();
      } catch (error) {
        console.warn('Persistent auth restore UI update failed:', error);
      }
    }
  }

  function attach() {
    const firebase = window.IndooneFirebase;
    if (!firebase?.auth?.onAuthStateChanged) return false;
    firebase.auth.onAuthStateChanged(restoreOverlay);
    return true;
  }

  if (!attach()) {
    const timer = setInterval(() => {
      if (attach()) clearInterval(timer);
    }, 100);
    setTimeout(() => clearInterval(timer), 10000);
  }
})();
