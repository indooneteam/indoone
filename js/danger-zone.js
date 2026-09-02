(() => {
  function firebaseState() {
    const firebase = window.IndooneFirebase;
    return {
      firebase,
      auth: firebase?.auth || null,
      db: firebase?.database || null
    };
  }

  function currentUser() {
    return firebaseState().auth?.currentUser || null;
  }

  function clearLocal() {
    try {
      Object.keys(localStorage).forEach(key => {
        if (key.startsWith('indoone')) localStorage.removeItem(key);
      });
    } catch (_) {}
    try {
      Object.keys(sessionStorage).forEach(key => {
        if (key.startsWith('indoone')) sessionStorage.removeItem(key);
      });
    } catch (_) {}
    try { window.IndoonePersistence?.lock?.(); } catch (_) {}
    try { window.IndoonePersistence?.clear?.(); } catch (_) {}
    try { window.IndooneBiometric?.disable?.(); } catch (_) {}
  }

  function setStatus(message, error = false) {
    const status = document.getElementById('accountDeleteStatus');
    if (!status) return;
    status.hidden = false;
    if (error) status.setAttribute('data-error', 'true');
    else status.removeAttribute('data-error');
    status.textContent = message;
  }

  window.showDangerZone = function () {
    closeDrawer();
    openModal(`
      <div class="modal-head"><h2>Danger Zone</h2><button class="close-btn" data-close>×</button></div>
      <p>These actions can permanently remove Indoone data. Continue only when you are sure.</p>
      <button type="button" class="settings-row danger" style="width:100%;border:0;background:#fff;text-align:left" onclick="confirmDeleteLocalData();return false;">
        <span>Delete local data<small>Remove data stored on this device</small></span><b>›</b>
      </button>
      <button type="button" class="settings-row danger" style="width:100%;border:0;background:#fff;text-align:left" onclick="confirmDeleteIndooneAccount();return false;">
        <span>Delete Indoone account<small>Permanently delete your Indoone account and cloud data</small></span><b>›</b>
      </button>
    `);
  };

  window.confirmDeleteLocalData = function () {
    openModal(`
      <div class="modal-head"><h2>Delete local data?</h2><button class="close-btn" data-close>×</button></div>
      <p>This removes Indoone data stored on this device, including the encrypted vault and local sign-in markers. Your Indoone account and cloud data will not be deleted.</p>
      <button type="button" class="primary danger" onclick="executeDeleteLocalData();return false;">Delete local data</button>
      <button type="button" class="secondary" data-close>Cancel</button>
    `);
  };

  window.executeDeleteLocalData = async function () {
    try {
      clearLocal();
      const auth = firebaseState().auth;
      if (auth) await auth.signOut();
      closeModal();
      window.location.reload();
    } catch (error) {
      toast(error?.message || 'Could not delete local data');
    }
  };

  window.confirmDeleteIndooneAccount = function () {
    const user = currentUser();
    if (!user) return toast('Please login first.');

    openModal(`
      <div class="modal-head"><h2>Delete Indoone account?</h2><button class="close-btn" data-close>×</button></div>
      <p>This permanently deletes your Indoone cloud data and Firebase account. This action cannot be undone.</p>
      <div class="field"><label>ACCOUNT PASSWORD</label><input id="deleteAccountPassword" type="password" autocomplete="current-password" placeholder="Enter your password"></div>
      <div class="field"><label>TYPE DELETE TO CONFIRM</label><input id="deleteAccountConfirm" type="text" autocomplete="off" placeholder="DELETE"></div>
      <div id="accountDeleteStatus" class="auth-status" hidden></div>
      <button type="button" class="primary danger" id="confirmAccountDelete" onclick="executeDeleteIndooneAccount();return false;">Delete Indoone account</button>
      <button type="button" class="secondary" data-close>Cancel</button>
    `);
  };

  window.executeDeleteIndooneAccount = async function () {
    const { firebase, auth, db } = firebaseState();
    const user = auth?.currentUser || null;
    const password = String(document.getElementById('deleteAccountPassword')?.value || '');
    const confirmation = String(document.getElementById('deleteAccountConfirm')?.value || '').trim();
    const button = document.getElementById('confirmAccountDelete');

    if (!password) return toast('Enter your account password');
    if (confirmation !== 'DELETE') return toast('Type DELETE to confirm');
    if (!firebase || !auth || !db || !user) return toast('Login session expired. Please login again.');
    if (!user.email) return toast('This account cannot be re-authenticated here.');

    if (button) button.disabled = true;
    setStatus('Verifying your account…');

    try {
      const provider = firebase.auth?.EmailAuthProvider;
      if (!provider?.credential) throw new Error('Firebase email authentication is unavailable.');

      const credential = provider.credential(user.email, password);
      await user.reauthenticateWithCredential(credential);
      await user.reload();

      const liveUser = auth.currentUser;
      if (!liveUser || liveUser.uid !== user.uid) throw new Error('Login session expired. Please login again.');

      setStatus('Deleting your Indoone cloud data…');

      const profileSnapshot = await db.ref(`users/${liveUser.uid}/profile`).once('value');
      const profile = profileSnapshot.val() || {};
      const mobile = String(profile.mobile || '').trim();

      const updates = {};
      updates[`users/${liveUser.uid}`] = null;
      if (mobile) updates[`mobileIndex/${encodeURIComponent(mobile)}`] = null;
      await db.ref().update(updates);

      setStatus('Deleting your Firebase account…');
      await liveUser.delete();

      clearLocal();
      closeModal();
      window.location.reload();
    } catch (error) {
      if (button) button.disabled = false;
      const code = String(error?.code || '');
      if (code === 'auth/wrong-password' || code === 'auth/invalid-credential') {
        setStatus('Incorrect account password.', true);
        toast('Incorrect account password');
      } else if (code === 'auth/requires-recent-login') {
        setStatus('A fresh login is required. Please login again and retry.', true);
        toast('Please login again, then retry account deletion');
      } else if (code === 'PERMISSION_DENIED' || /permission_denied/i.test(String(error?.message || ''))) {
        setStatus('Firebase denied deletion of your cloud data. Check Firebase database rules.', true);
        toast('Firebase denied cloud-data deletion');
      } else if (code === 'auth/network-request-failed') {
        setStatus('Network error. Check your connection and retry.', true);
        toast('Network error. Please retry');
      } else {
        setStatus(error?.message || 'Account deletion failed.', true);
        toast(error?.message || 'Could not delete Indoone account');
      }
    }
  };
})();
