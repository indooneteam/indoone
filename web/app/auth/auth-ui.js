(() => {
  // Compatibility facade for older callers.
  // auth-page.js is the canonical implementation. This file deliberately
  // keeps the legacy entry point without installing a second auth UI or
  // global click handler.

  function delegate(method, args) {
    const ui = window.__indooneCanonicalAuthUI;

    if (!ui || typeof ui[method] !== 'function') return false;

    return ui[method](...args);
  }

  const compatibilityUI = {
    showLogin: (...args) => delegate('showLogin', args),
    showSignup: (...args) => delegate('showSignup', args),
    showForgotPassword: (...args) => delegate('showForgotPassword', args),
    close: (...args) => delegate('close', args)
  };

  // Never overwrite the canonical UI if auth-page.js has already loaded.
  if (!window.__indooneCanonicalAuthUI) {
    window.IndooneAuthUI = compatibilityUI;
  }
})();
