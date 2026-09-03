window.initMenuDeleteLocalData = function () {
  const modal = document.getElementById('modal');

  modal.innerHTML = `
    <div class="modal-head">
      <h2>Delete local data?</h2>
      <button class="close-btn" data-close>×</button>
    </div>
    <p>
      This removes Indoone data stored on this device, including the encrypted
      vault and local sign-in markers. Your Indoone account and cloud data will
      not be deleted.
    </p>
    <button type="button" class="primary danger" id="confirmLocalDelete">
      Delete local data
    </button>
    <button type="button" class="secondary" data-close>
      Cancel
    </button>
  `;

  const clearLocal = () => {
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

  document.getElementById('confirmLocalDelete')?.addEventListener('click', async () => {
    try {
      clearLocal();
      await window.IndooneFirebase?.auth?.signOut?.();
      closeModal();
      window.location.reload();
    } catch (error) {
      toast(error?.message || 'Could not delete local data');
    }
  });
};
