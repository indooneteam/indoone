window.showChangeMobile = function ({ remember = true } = {}) {
  const modal = document.getElementById('modal');

  if (!modal) return;

  if (remember) {
    window.IndoonePageState?.set('mobile');
  }

  fetch(
    'app/settings/profile/mobile/index.html?v=20260904c',
    { cache: 'no-store' }
  )
    .then(response =>
      response.ok
        ? response.text()
        : Promise.reject(
            new Error('Mobile page could not be loaded.')
          )
    )
    .then(html => {
      modal.innerHTML = html;

      const auth = window.IndooneFirebase?.auth;
      const db = window.IndooneFirebase?.database;
      const user = auth?.currentUser;

      const normalizeMobile =
        window.IndooneFirebaseAuthBase?.normalizeMobile ||
        (value => {
          const raw = String(value || '').trim();
          const digits = raw.replace(/\D/g, '');

          if (/^91\d{10}$/.test(digits)) {
            return `+${digits}`;
          }

          if (/^\d{10}$/.test(digits)) {
            return `+91${digits}`;
          }

          return raw
            .replace(/[^0-9+]/g, '')
            .replace(/^00/, '+');
        });

      const input = document.getElementById(
        'changeMobileNumber'
      );
      const saveButton = document.getElementById(
        'saveMobileNumber'
      );

      if (!auth || !db || !user) {
        return toast(
          'Please sign in again before changing your mobile number.'
        );
      }

      async function loadCurrentMobile() {
        const snapshot = await db
          .ref(`users/${user.uid}/profile/mobile`)
          .once('value');

        return normalizeMobile(snapshot.val() || '');
      }

      if (input) {
        loadCurrentMobile()
          .then(mobile => {
            if (mobile) {
              input.value = mobile;
            }
          })
          .catch(() => {});
      }

      saveButton?.addEventListener('click', async () => {
        const value = normalizeMobile(input?.value || '');

        if (!/^\+91\d{10}$/.test(value)) {
          return toast(
            'Enter a valid 10-digit Indian mobile number.'
          );
        }

        saveButton.disabled = true;

        try {
          const profileSnapshot = await db
            .ref(`users/${user.uid}/profile`)
            .once('value');
          const profile = profileSnapshot.val() || {};
          const oldMobile = normalizeMobile(profile.mobile || '');

          if (value === oldMobile) {
            return toast('Enter a different mobile number.');
          }

          const candidates = Array.from(
            new Set(
              [
                value,
                value.replace(/\D/g, ''),
                value.replace(/^\+/, ''),
                value.replace(/^\+91/, '')
              ].filter(Boolean)
            )
          );

          for (const candidate of candidates) {
            const existingSnapshot = await db
              .ref(
                `mobileIndex/${encodeURIComponent(candidate)}`
              )
              .once('value');
            const existing = existingSnapshot.val();

            if (
              existing?.uid &&
              String(existing.uid) !== String(user.uid)
            ) {
              throw new Error(
                'That mobile number is already linked to another account.'
              );
            }

            if (existing?.email && !existing?.uid) {
              throw new Error(
                'That mobile number is already linked to another account.'
              );
            }
          }

          const now = Date.now();
          const updates = {
            [`users/${user.uid}/profile/mobile`]: value,
            [`users/${user.uid}/profile/uid`]: user.uid,
            [`users/${user.uid}/profile/email`]:
              user.email || profile.email || '',
            [`users/${user.uid}/profile/updatedAt`]: now,
            [`mobileIndex/${encodeURIComponent(value)}`]: {
              uid: user.uid,
              email: user.email || profile.email || '',
              updatedAt: now
            }
          };

          if (oldMobile && oldMobile !== value) {
            updates[
              `mobileIndex/${encodeURIComponent(oldMobile)}`
            ] = null;
          }

          await db.ref().update(updates);
          toast('Mobile number updated.');
          window.showProfile?.({ remember: false });
        } catch (error) {
          toast(
            error?.message ||
              'Could not change mobile number.'
          );
        } finally {
          saveButton.disabled = false;
        }
      });
    })
    .catch(error =>
      toast(
        error?.message ||
          'Could not open mobile number'
      )
    );
};
