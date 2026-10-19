(() => {
  // The web counterpart of android/settings/profile/ProfileScreen.kt - the
// ACCOUNT screen, not the drawer's profile editor, which is menu/profile.
  //
  // This screen is a menu feature, opened by menu/script.js the same way Danger
  // Zone and Log out are: the loader fetches the fragment into #modal, calls
  // this initializer, then shows the page. It used to be its own little modal -
  // it fetched the fragment itself and unhid #overlay, which put a grey dim
  // across the app and shrank the screen to a card. That is the bug that showed
  // when Profile was tapped. It does not touch #overlay any more.
  //
  // The rows inside are wired here, and the × carries data-close, which
  // shared/events.js handles globally.
  function currentUser() {
    return window.IndooneFirebase?.auth?.currentUser || null;
  }

  async function renderProfile() {
    const user = currentUser();
    const email = document.getElementById('profileEmail');
    const phone = document.getElementById('profilePhone');

    if (email) {
      email.textContent = user?.email || 'Email not available';
    }

    if (phone) {
      phone.textContent = 'Mobile number not set';
    }

    if (!user || !phone) {
      return;
    }

    try {
      const db = window.IndooneFirebase?.database;

      if (!db) {
        return;
      }

      const snapshot = await db
        .ref(`users/${user.uid}/profile/mobile`)
        .once('value');

      const mobile =
        window.IndooneFirebaseAuthBase?.normalizeMobile?.(
          snapshot.val() || ''
        ) || String(snapshot.val() || '');

      phone.textContent =
        mobile || 'Mobile number not set';
    } catch (_) {
      phone.textContent =
        'Mobile number not available';
    }
  }

  window.initMenuAccount = function () {
    const page = document.getElementById('modal');

    if (!page) {
      return false;
    }

    void renderProfile();

    page
      .querySelectorAll('[data-profile-action]')
      .forEach(button => {
        const action = button.dataset.profileAction;

        if (action === 'mobile') {
          button.addEventListener(
            'click',
            () => window.openMenuFeature?.('change-mobile-number')
          );
        }

        if (action === 'email') {
          button.addEventListener(
            'click',
            () => window.openMenuFeature?.('change-email')
          );
        }
      });

    return true;
  };
})();
