window.showChangeEmail = function ({ remember = true } = {}) {
  const modal = document.getElementById('modal');

  if (!modal) return;

  if (remember) {
    window.IndoonePageState?.set('email');
  }

  fetch(
    'app/settings/profile/email/index.html?v=20260904b',
    { cache: 'no-store' }
  )
    .then(r =>
      r.ok
        ? r.text()
        : Promise.reject(
            new Error('Email page could not be loaded.')
          )
    )
    .then(html => {
      modal.innerHTML = html;

      const auth = window.IndooneFirebase?.auth;
      const db = window.IndooneFirebase?.database;
      const user = auth?.currentUser;
      const input = document.getElementById('changeEmail');
      const passwordInput = document.getElementById(
        'changeEmailPassword'
      );
      const saveButton = document.getElementById('saveEmail');

      if (!auth || !db || !user) {
        return toast(
          'Please sign in again before changing your email.'
        );
      }

      if (input && user.email) {
        input.value = user.email;
      }

      saveButton?.addEventListener('click', async () => {
        const value =
          input?.value.trim().toLowerCase() || '';
        const password = passwordInput?.value || '';

        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
          return toast('Enter a valid email address');
        }

        if (!password) {
          return toast('Enter your current password');
        }

        if (
          value ===
          String(user.email || '').trim().toLowerCase()
        ) {
          return toast('Enter a different email address');
        }

        saveButton.disabled = true;

        try {
          const methods =
            await auth.fetchSignInMethodsForEmail(value).catch(
              error => {
                if (error?.code === 'auth/invalid-email') {
                  throw error;
                }

                return [];
              }
            );

          if (Array.isArray(methods) && methods.length) {
            throw new Error(
              'An account already exists with this email.'
            );
          }

          const credential =
            firebase.auth.EmailAuthProvider.credential(
              user.email,
              password
            );

          await user.reauthenticateWithCredential(credential);
          await user.updateEmail(value);

          const snapshot = await db
            .ref(`users/${user.uid}/profile`)
            .once('value');
          const profile = snapshot.val() || {};

          const updates = {
            [`users/${user.uid}/profile/email`]: value,
            [`users/${user.uid}/profile/uid`]: user.uid,
            [`users/${user.uid}/profile/updatedAt`]: Date.now()
          };

          if (profile.mobile) {
            const mobile =
              window.IndooneFirebaseAuthBase?.normalizeMobile?.(
                profile.mobile
              ) || profile.mobile;

            updates[
              `users/${user.uid}/profile/mobile`
            ] = mobile;

            updates[
              `mobileIndex/${encodeURIComponent(mobile)}`
            ] = {
              uid: user.uid,
              email: value,
              updatedAt: Date.now()
            };
          }

          await db.ref().update(updates);

          await user
            .sendEmailVerification()
            .catch(error =>
              console.warn(
                'Email verification send failed:',
                error
              )
            );

          input.value = value;
          toast(
            'Email changed in Firebase. A verification email was sent.'
          );
        } catch (error) {
          const messages = {
            'auth/wrong-password':
              'Current password is incorrect.',
            'auth/invalid-credential':
              'Current password is incorrect.',
            'auth/requires-recent-login':
              'Please sign in again, then change your email.',
            'auth/email-already-in-use':
              'An account already exists with this email.',
            'auth/invalid-email':
              'Enter a valid email address.'
          };

          toast(
            messages[error?.code] ||
              error?.message ||
              'Could not change email'
          );
        } finally {
          saveButton.disabled = false;
        }
      });
    })
    .catch(error =>
      toast(
        error?.message ||
          'Could not open email settings'
      )
    );
};
