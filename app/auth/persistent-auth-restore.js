(() => {
  const VERIFIED_KEY = 'indoone_otp_verified_uid';
  let attached = false;
  let loading = false;
  let verificationWatch = null;

  const wait = ms => new Promise(resolve => setTimeout(resolve, ms));

  async function restoreAccounts(user) {
    if (!user || loading) return false;
    const verifiedUid = localStorage.getItem(VERIFIED_KEY);
    if (!verifiedUid || verifiedUid !== user.uid) return false;

    loading = true;
    try {
      window.IndooneAuthUI?.close?.();
      document.body.classList.remove('auth-open', 'auth-pending');

      const loader = window.loadFirebaseAccounts || window.IndooneCloudAccounts?.load;

      if (typeof loader === 'function') {
        let accounts = [];

        for (let attempt = 0; attempt < 4; attempt += 1) {
          try {
            accounts = await loader();

            if (Array.isArray(accounts) && accounts.length > 0) {
              break;
            }
          } catch (error) {
            console.warn(
              `Indoone Firebase account restore attempt ${attempt + 1} failed:`,
              error
            );
          }

          if (attempt < 3) {
            await wait(250 * (attempt + 1));
          }
        }

        if (typeof renderAccounts === 'function') {
          renderAccounts();
        }

        if (typeof refreshAccountCodes === 'function') {
          await refreshAccountCodes();
        }
      } else if (typeof renderAccounts === 'function') {
        renderAccounts();
      }

      if (
        typeof startDemoTimers === 'function' &&
        !IndoonePersistence.hasVault()
      ) {
        startDemoTimers();
      }

      return true;
    } catch (error) {
      console.warn('Persistent Firebase account restore failed:', error);
      return false;
    } finally {
      loading = false;
    }
  }

  function stopVerificationWatch() {
    if (verificationWatch) {
      clearInterval(verificationWatch);
      verificationWatch = null;
    }
  }

  function watchForOtpVerification(user) {
    stopVerificationWatch();

    if (!user) {
      return;
    }

    const startedAt = Date.now();
    verificationWatch = setInterval(async () => {
      if (Date.now() - startedAt > 10000) {
        stopVerificationWatch();
        return;
      }

      const verifiedUid = localStorage.getItem(VERIFIED_KEY);

      if (!verifiedUid || verifiedUid !== user.uid || loading) {
        return;
      }

      stopVerificationWatch();
      await restoreAccounts(user);
    }, 100);
  }

  function handleAuthState(user) {
    if (!user) {
      stopVerificationWatch();
      return;
    }

    restoreAccounts(user);
    watchForOtpVerification(user);
  }

  function attach() {
    const firebase = window.IndooneFirebase;

    if (!firebase?.auth?.onAuthStateChanged || attached) {
      return false;
    }

    attached = true;
    firebase.auth.onAuthStateChanged(handleAuthState);

    if (firebase.auth.currentUser) {
      handleAuthState(firebase.auth.currentUser);
    }

    return true;
  }

  if (!attach()) {
    const timer = setInterval(() => {
      if (attach()) {
        clearInterval(timer);
      }
    }, 100);

    setTimeout(() => clearInterval(timer), 10000);
  }
})();
