(() => {
  async function loadMarkup() {
    const response = await fetch(
      `app/connect/connect-device/index.html?v=${Date.now()}`,
      { cache: 'no-store' }
    );

    if (!response.ok) {
      throw new Error('Connect flow could not be loaded.');
    }

    return response.text();
  }

  function attachCloseHandlers(modal) {
    modal.querySelectorAll('[data-device-connect-close]').forEach(button => {
      button.addEventListener('click', () => window.closeModal?.());
    });
  }

  window.showConnectChoice = async function () {
    try {
      const markup = await loadMarkup();
      window.openModal?.(markup);

      const modal = document.getElementById('modal');
      if (!modal) return;

      attachCloseHandlers(modal);

      modal.querySelectorAll('[data-device-target]').forEach(button => {
        button.addEventListener('click', () => {
          window.startNearbyConnection?.(button.dataset.deviceTarget);
        });
      });
    } catch (error) {
      window.toast?.(error?.message || 'Could not open Connect.');
    }
  };
})();
