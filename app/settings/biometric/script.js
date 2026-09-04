window.IndooneBiometric = (() => {
  const ENABLED_KEY = 'indoone.biometric.enabled.v1';

  function supported() { return typeof window.IndooneNative?.authenticateBiometric === 'function'; }
  function enabled() {
    return localStorage.getItem(ENABLED_KEY) === '1'
      && typeof window.IndooneNative?.hasBiometricSecret === 'function'
      && window.IndooneNative.hasBiometricSecret();
  }
  function setEnabled(value) { value ? localStorage.setItem(ENABLED_KEY, '1') : localStorage.removeItem(ENABLED_KEY); }
  function authenticate(onSuccess, onFallback) {
    if (!supported()) return toast('Biometric unlock is available in the Android app');
    const handler = event => {
      window.removeEventListener('indoone-biometric-result', handler);
      if (event.detail?.success) onSuccess?.(event.detail); else onFallback?.(event.detail?.message || 'Authentication failed');
    };
    window.addEventListener('indoone-biometric-result', handler);
    window.IndooneNative.authenticateBiometric();
  }
  function authenticateForUnlock(onSuccess, onFallback) {
    if (!supported()) return onFallback?.('Biometric unlock is available in the Android app');
    if (!enabled()) return onFallback?.('Biometric unlock is not enabled');
    const handler = event => {
      window.removeEventListener('indoone-biometric-result', handler);
      if (event.detail?.success && event.detail?.pin) onSuccess?.(event.detail.pin); else onFallback?.(event.detail?.message || 'Biometric unlock failed');
    };
    window.addEventListener('indoone-biometric-result', handler);
    window.IndooneNative.authenticateBiometricUnlock();
  }
  function enableForCurrentVault(onSuccess, onFallback) {
    if (!supported()) return onFallback?.('Biometric unlock is available in the Android app');
    if (!window.IndoonePersistence?.isUnlocked?.()) return onFallback?.('Unlock App Lock with PIN first');
    let pin;
    try { pin = IndooneSecureSession.getPin(); } catch (_) { return onFallback?.('Unlock App Lock with PIN first'); }
    const handler = event => {
      window.removeEventListener('indoone-biometric-result', handler);
      if (!event.detail?.success) return onFallback?.(event.detail?.message || 'Biometric authentication cancelled');
      const saved = window.IndooneNative.saveBiometricSecret(pin);
      if (!saved) return onFallback?.('Could not enable biometric unlock');
      setEnabled(true);
      onSuccess?.();
    };
    window.addEventListener('indoone-biometric-result', handler);
    window.IndooneNative.authenticateBiometric();
  }
  function disable() { try { window.IndooneNative?.clearBiometricSecret?.(); } catch (_) {} setEnabled(false); }
  return { supported, enabled, setEnabled, authenticate, authenticateForUnlock, enableForCurrentVault, disable };
})();
