(() => {
  window.showAccessToMyDevice = async () => {
    const response = await fetch(
      'app/connect/devices/access-to-my-device/index.html?v=20260903a',
      { cache: 'no-store' }
    );

    if (!response.ok) {
      throw new Error('Access to my device could not be loaded.');
    }

    window.openModal?.(await response.text());
  };
})();
