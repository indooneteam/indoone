(() => {
  const SCRIPT_URL = 'app/connect/scanner/scanner/script.js?v=20260903a';
  let loading = null;

  window.showConnectScanner = async () => {
    if (window.IndooneNestedScanner?.show) {
      return window.IndooneNestedScanner.show();
    }

    if (!loading) {
      loading = new Promise((resolve, reject) => {
        const script = document.createElement('script');
        script.src = SCRIPT_URL;
        script.onload = resolve;
        script.onerror = () => reject(new Error('Scanner feature could not be loaded.'));
        document.head.appendChild(script);
      });
    }

    await loading;
    return window.IndooneNestedScanner?.show?.();
  };
})();
