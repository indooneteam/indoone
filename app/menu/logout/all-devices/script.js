window.initMenuLogoutAllDevices = function () {
  const modal = document.getElementById('modal');

  modal.innerHTML = `
    <div class="modal-head">
      <h2>Log out on all devices</h2>
      <button class="close-btn" data-close>×</button>
    </div>
    <p>
      This signs you out on this device. Remote session revocation requires
      server-side Firebase Admin support.
    </p>
    <button type="button" class="primary" id="confirmAllDevicesLogout">
      Log out on all devices
    </button>
    <button type="button" class="secondary" data-close>
      Cancel
    </button>
  `;

  const clearLocalSession = () => {
    try {
      Object.keys(localStorage).forEach(key => {
        if (key.startsWith('indoone')) {
          localStorage.removeItem(key);
        }
      });
    } catch (_) {}

    try {
      Object.keys(sessionStorage).forEach(key => {
        if (key.startsWith('indoone')) {
          sessionStorage.removeItem(key);
        }
      });
    } catch (_) {}

    try {
      window.IndoonePersistence?.lock?.();
    } catch (_) {}

    try {
      window.IndoonePersistence?.clear?.();
    } catch (_) {}

    try {
      window.IndooneBiometric?.disable?.();
    } catch (_) {}
  };

  document.getElementById('confirmAllDevicesLogout')?.addEventListener('click', async () => {
    try {
      clearLocalSession();
      await window.IndooneFirebase?.auth?.signOut?.();
      closeModal();
      window.location.reload();
    } catch (error) {
      toast(error?.message || 'Could not log out');
    }
  });
};
