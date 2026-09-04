(() => {
  // Compatibility facade for older callers.
  // The canonical auth implementation lives in auth-page.js.
  // Keep this file so existing imports/callers continue to work without
  // installing a second click bridge or a second authentication UI.

  function delegate(method, ...args) {
    const ui = window.IndooneAuthUI;

    if (!ui || ui[method] === delegate[method]) return false;

    return ui[method](...args);
  }

  function showLogin(...args) {
    return delegate('showLogin', ...args);
  }

  function showSignup(...args) {
    return delegate('showSignup', ...args);
  }

  function showForgotPassword(...args) {
    return delegate('showForgotPassword', ...args);
  }

  function close(...args) {
    return delegate('close', ...args);
  }

  showLogin.showLogin = showLogin;
  showSignup.showSignup = showSignup;
  showForgotPassword.showForgotPassword = showForgotPassword;
  close.close = close;

  // Do not replace the canonical UI when auth-page.js has already loaded.
  if (!window.IndooneAuthUI) {
    window.IndooneAuthUI = {
      showLogin,
      showSignup,
      showForgotPassword,
      close
    };
  }
})();
