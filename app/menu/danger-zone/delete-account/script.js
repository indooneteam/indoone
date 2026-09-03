window.initMenuDeleteAccount = function () {
  const user = window.IndooneFirebase?.auth?.currentUser;
  if (!user) return toast('Please login first.');
  document.getElementById('modal').innerHTML = `<div class="modal-head"><h2>Delete Indoone account?</h2><button class="close-btn" data-close>×</button></div><p>This permanently deletes your Indoone cloud data and Firebase account. This action cannot be undone.</p><div class="field"><label>ACCOUNT PASSWORD</label><input id="deleteAccountPassword" type="password" autocomplete="current-password" placeholder="Enter your password"></div><div class="field"><label>TYPE DELETE TO CONFIRM</label><input id="deleteAccountConfirm" type="text" autocomplete="off" placeholder="DELETE" autocapitalize="characters" spellcheck="false"></div><div id="accountDeleteStatus" class="auth-status" hidden></div><button type="button" class="primary danger" id="confirmAccountDelete">Delete Indoone account</button><button type="button" class="secondary" data-close>Cancel</button>`;
  const status = message => { const el = document.getElementById('accountDeleteStatus'); if (el) { el.hidden = false; el.removeAttribute('data-error'); el.textContent = message; } };
  const errorStatus = message => { const el = document.getElementById('accountDeleteStatus'); if (el) { el.hidden = false; el.setAttribute('data-error', 'true'); el.textContent = message; } };
  document.getElementById('confirmAccountDelete')?.addEventListener('click', async () => {
    const auth = window.IndooneFirebase?.auth;
    const db = window.IndooneFirebase?.database;
    const liveUser = auth?.currentUser;
    const passwordInput = document.getElementById('deleteAccountPassword');
    const confirmInput = document.getElementById('deleteAccountConfirm');
    const password = String(passwordInput?.value || '');
    const confirmation = String(confirmInput?.value || '').trim().toUpperCase();
    if (!password) { passwordInput?.focus(); return toast('Enter your account password'); }
    if (confirmation !== 'DELETE') { confirmInput?.focus(); return toast('Type DELETE to confirm'); }
    if (!auth || !db || !liveUser) return toast('Login session expired. Please login again.');
    if (!liveUser.email) return toast('This account cannot be re-authenticated here.');
    const button = document.getElementById('confirmAccountDelete');
    if (button) button.disabled = true;
    status('Verifying your account…');
    try {
      const provider = window.firebase?.auth?.EmailAuthProvider;
      const credential = provider?.credential?.(liveUser.email, password);
      if (!credential) throw new Error('Firebase email authentication is unavailable.');
      await liveUser.reauthenticateWithCredential(credential);
      await liveUser.reload();
      const userAfterReload = auth.currentUser;
      if (!userAfterReload || userAfterReload.uid !== liveUser.uid) throw new Error('Login session expired. Please login again.');
      status('Removing your Indoone cloud data…');
      const profileSnapshot = await db.ref(`users/${userAfterReload.uid}/profile`).once('value');
      const profile = profileSnapshot.val() || {};
      const email = String(userAfterReload.email || profile.email || '').trim().toLowerCase();
      const mobile = String(profile.mobile || '').trim();
      const matchesSnapshot = await db.ref('users').orderByChild('profile/email').equalTo(email).once('value');
      const matches = matchesSnapshot.val() || {};
      const updates = {};
      const mobiles = new Set(mobile ? [mobile] : []);
      Object.entries(matches).forEach(([uid, item]) => {
        if (!item?.profile?.email || String(item.profile.email).trim().toLowerCase() !== email) return;
        updates[`users/${uid}`] = null;
        const matchedMobile = String(item.profile.mobile || '').trim();
        if (matchedMobile) mobiles.add(matchedMobile);
      });
      updates[`users/${userAfterReload.uid}`] = null;
      mobiles.forEach(value => { updates[`mobileIndex/${encodeURIComponent(value)}`] = null; });
      await db.ref().update(updates);
      status('Deleting your Firebase account…');
      await userAfterReload.delete();
      try { Object.keys(localStorage).forEach(key => { if (key.startsWith('indoone')) localStorage.removeItem(key); }); } catch (_) {}
      try { Object.keys(sessionStorage).forEach(key => { if (key.startsWith('indoone')) sessionStorage.removeItem(key); }); } catch (_) {}
      try { window.IndoonePersistence?.lock?.(); } catch (_) {}
      try { window.IndoonePersistence?.clear?.(); } catch (_) {}
      try { window.IndooneBiometric?.disable?.(); } catch (_) {}
      closeModal();
      window.location.reload();
    } catch (error) {
      if (button) button.disabled = false;
      const code = String(error?.code || '');
      if (code === 'auth/wrong-password' || code === 'auth/invalid-credential') { errorStatus('Incorrect account password.'); toast('Incorrect account password'); }
      else if (code === 'auth/requires-recent-login') { errorStatus('A fresh login is required. Please login again and retry.'); toast('Please login again, then retry account deletion'); }
      else if (code === 'PERMISSION_DENIED' || /permission_denied/i.test(String(error?.message || ''))) { errorStatus('Firebase denied deletion of your cloud data. Check Firebase database rules.'); toast('Firebase denied cloud-data deletion'); }
      else if (code === 'auth/network-request-failed') { errorStatus('Network error. Check your connection and retry.'); toast('Network error. Please retry'); }
      else { errorStatus(error?.message || 'Account deletion failed.'); toast(error?.message || 'Could not delete Indoone account'); }
    }
  });
};