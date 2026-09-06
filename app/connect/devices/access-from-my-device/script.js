(() => {
  window.showAccessFromMyDevice = async () => {
    const response = await fetch(
      'app/connect/devices/access-from-my-device/index.html?v=20260903a',
      { cache: 'no-store' }
    );

    if (!response.ok) {
      throw new Error('Access from my device could not be loaded.');
    }

    window.openModal?.(await response.text());
  };
})();
