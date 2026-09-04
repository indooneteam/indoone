window.showChangeMobile = function () {
  const modal = document.getElementById('modal');
  if (!modal) return;
  fetch('app/settings/profile/mobile/index.html?v=20260904b', { cache: 'no-store' })
    .then(r => r.ok ? r.text() : Promise.reject(new Error('Mobile page could not be loaded.')))
    .then(html => {
      modal.innerHTML = html;
      const auth = window.IndooneFirebase?.auth;
      const db = window.IndooneFirebase?.database;
      const user = auth?.currentUser;
      const normalizeMobile = window.IndooneFirebaseAuthBase?.normalizeMobile || (value => String(value || '').replace(/[^0-9+]/g, '').replace(/^00/, '+'));
      const input = document.getElementById('changeMobileNumber');
      const passwordInput = document.getElementById('changeMobilePassword');
      const sendButton = document.getElementById('saveMobileNumber');
      const verifyButton = document.getElementById('verifyMobileNumber');
      const otpArea = document.getElementById('mobileOtpArea');
      let confirmation = null;
      let recaptcha = null;

      if (!auth || !db || !user) return toast('Please sign in again before changing your mobile number.');
      if (user.phoneNumber) input.value = user.phoneNumber;

      function resetRecaptcha() {
        try { recaptcha?.clear?.(); } catch (_) {}
        recaptcha = null;
        const container = document.getElementById('mobileRecaptcha');
        if (container) container.innerHTML = '';
      }

      function showError(error) {
        const messages = {
          'auth/invalid-phone-number': 'Enter a valid mobile number.',
          'auth/invalid-verification-code': 'Invalid SMS verification code.',
          'auth/code-expired': 'The SMS code expired. Send a new code.',
          'auth/credential-already-in-use': 'That mobile number is already linked to another account.',
          'auth/provider-already-linked': 'That mobile number is already linked to this account.',
          'auth/requires-recent-login': 'Please sign in again, then change your mobile number.',
          'auth/wrong-password': 'Current password is incorrect.',
          'auth/invalid-credential': 'Current password is incorrect.'
        };
        toast(messages[error?.code] || error?.message || 'Could not change mobile number');
      }

      sendButton?.addEventListener('click', async () => {
        const value = normalizeMobile(input?.value || '');
        const password = passwordInput?.value || '';
        if (!/^\+91\d{10}$/.test(value)) return toast('Enter a valid 10-digit Indian mobile number.');
        if (!password) return toast('Enter your current password');
        if (value === normalizeMobile(user.phoneNumber || '')) return toast('Enter a different mobile number');

        sendButton.disabled = true;
        try {
          const existing = await db.ref(`mobileIndex/${encodeURIComponent(value)}`).once('value');
          const identity = existing.val();
          if (identity?.uid && String(identity.uid) !== String(user.uid)) throw new Error('That mobile number is already linked to another account.');
          if (identity?.email && !identity?.uid) throw new Error('That mobile number is already linked to another account.');

          const credential = firebase.auth.EmailAuthProvider.credential(user.email, password);
          await user.reauthenticateWithCredential(credential);

          resetRecaptcha();
          recaptcha = new firebase.auth.RecaptchaVerifier('mobileRecaptcha', {
            size: 'normal',
            callback: () => {},
            'expired-callback': () => toast('reCAPTCHA expired. Please try again.')
          });
          const provider = new firebase.auth.PhoneAuthProvider();
          const verificationId = await provider.verifyPhoneNumber(value, recaptcha);
          confirmation = { verificationId, value };
          otpArea.hidden = false;
          sendButton.hidden = true;
          input.disabled = true;
          passwordInput.disabled = true;
          document.getElementById('changeMobileOtp')?.focus();
          toast('SMS verification code sent.');
        } catch (error) {
          try { await recaptcha?.render?.(); } catch (_) {}
          resetRecaptcha();
          showError(error);
        } finally {
          sendButton.disabled = false;
        }
      });

      verifyButton?.addEventListener('click', async () => {
        const code = String(document.getElementById('changeMobileOtp')?.value || '').replace(/\D/g, '');
        if (!confirmation?.verificationId) return toast('Send the SMS code first.');
        if (!/^\d{6}$/.test(code)) return toast('Enter the 6-digit SMS code.');

        verifyButton.disabled = true;
        try {
          const credential = firebase.auth.PhoneAuthProvider.credential(confirmation.verificationId, code);
          const oldProfile = (await db.ref(`users/${user.uid}/profile`).once('value')).val() || {};
          await user.updatePhoneNumber(credential);
          await user.reload();

          const oldMobile = normalizeMobile(oldProfile.mobile || '');
          const now = Date.now();
          const updates = {
            [`users/${user.uid}/profile/uid`]: user.uid,
            [`users/${user.uid}/profile/email`]: user.email || oldProfile.email || '',
            [`users/${user.uid}/profile/mobile`]: confirmation.value,
            [`users/${user.uid}/profile/updatedAt`]: now,
            [`mobileIndex/${encodeURIComponent(confirmation.value)}`]: {
              uid: user.uid,
              email: user.email || oldProfile.email || '',
              mobile: confirmation.value,
              updatedAt: now
            }
          };
          if (oldMobile && oldMobile !== confirmation.value) updates[`mobileIndex/${encodeURIComponent(oldMobile)}`] = null;
          await db.ref().update(updates);

          toast('Mobile number changed in Firebase.');
          window.showProfile?.();
        } catch (error) {
          showError(error);
        } finally {
          verifyButton.disabled = false;
        }
      });
    })
    .catch(error => toast(error?.message || 'Could not open mobile number'));
};
