(() => {
  const SCRIPT_URL = 'app/connect/qr/qr/script.js?v=20260903a';
  let loading = null;

  window.showConnectQr = async () => {
    if (window.IndooneNestedQr?.show) {
      return window.IndooneNestedQr.show();
    }

    if (!loading) {
      loading = new Promise((resolve, reject) => {
        const script = document.createElement('script');
        script.src = SCRIPT_URL;
        script.onload = resolve;
        script.onerror = () => reject(new Error('QR feature could not be loaded.'));
        document.head.appendChild(script);
      });
    }

    await loading;
    return window.IndooneNestedQr?.show?.();
  };
})();
