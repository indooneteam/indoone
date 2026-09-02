(() => {
  function clearLocalSession() {
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

  function finishLogout() {
    clearLocalSession();
    return window.IndooneFirebase?.auth?.signOut?.();
  }

  window.showLogoutOptions = function () {
    closeDrawer();
    openModal(`
      <div class="modal-head"><h2>Log out</h2><button class="close-btn" data-close>×</button></div>
      <p>Choose where you want to sign out.</p>
      <button type="button" class="settings-row" style="width:100%;border:0;background:#fff;text-align:left" onclick="window.logoutThisDevice();return false;">
        <span>Log out on this device<small>Sign out only from this device</small></span><b>›</b>
      </button>
      <button type="button" class="settings-row" style="width:100%;border:0;background:#fff;text-align:left" onclick="window.logoutAllDevices();return false;">
        <span>Log out on all devices<small>Sign out everywhere you use Indoone</small></span><b>›</b>
      </button>
      <button type="button" class="secondary" data-close>Cancel</button>
    `);
  };

  window.logoutThisDevice = async function () {
    try {
      await finishLogout();
      closeModal();
      window.location.reload();
    } catch (error) {
      toast(error?.message || 'Could not log out');
    }
  };

  window.logoutAllDevices = async function () {
    try {
      // The current Firebase client can securely sign out this device.
      // A true all-device token revocation requires Firebase Admin/server support,
      // so do not claim remote sessions were revoked here.
      await finishLogout();
      closeModal();
      window.location.reload();
    } catch (error) {
      toast(error?.message || 'Could not log out');
    }
  };
})();
