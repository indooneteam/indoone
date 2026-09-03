window.initMenuLogoutThisDevice = function () {
  const modal = document.getElementById('modal');
  modal.innerHTML = `<div class="modal-head"><h2>Log out on this device</h2><button class="close-btn" data-close>×</button></div><p>This signs you out only from this device.</p><button type="button" class="primary" id="confirmThisDeviceLogout">Log out on this device</button><button type="button" class="secondary" data-close>Cancel</button>`;
  document.getElementById('confirmThisDeviceLogout')?.addEventListener('click', async () => {
    try {
      const clearLocal = () => {
        try { Object.keys(localStorage).forEach(key => { if (key.startsWith('indoone')) localStorage.removeItem(key); }); } catch (_) {}
        try { Object.keys(sessionStorage).forEach(key => { if (key.startsWith('indoone')) sessionStorage.removeItem(key); }); } catch (_) {}
        try { window.IndoonePersistence?.lock?.(); } catch (_) {}
        try { window.IndoonePersistence?.clear?.(); } catch (_) {}
        try { window.IndooneBiometric?.disable?.(); } catch (_) {}
      };
      clearLocal();
      await window.IndooneFirebase?.auth?.signOut?.();
      closeModal();
      window.location.reload();
    } catch (error) {
      toast(error?.message || 'Could not log out');
    }
  });
};