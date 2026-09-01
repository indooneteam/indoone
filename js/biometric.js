window.IndooneBiometric = (() => {
  function supported() {
    return typeof window.IndooneNative?.authenticateBiometric === 'function';
  }

  function authenticate(onSuccess, onFallback) {
    if (!supported()) {
      toast('Biometric unlock is available in the Android app');
      return;
    }

    const handler = event => {
      window.removeEventListener('indoone-biometric-result', handler);
      if (event.detail?.success) {
        onSuccess?.();
      } else {
        onFallback?.(event.detail?.message || 'Authentication failed');
      }
    };

    window.addEventListener('indoone-biometric-result', handler);
    window.IndooneNative.authenticateBiometric();
  }

  return { supported, authenticate };
})();
