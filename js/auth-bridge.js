(() => {
  document.addEventListener('click', event => {
    const signup = event.target.closest('[data-auth-signup]');
    if (signup && window.IndooneAuthUI?.showSignup) {
      event.preventDefault();
      event.stopPropagation();
      window.IndooneAuthUI.showSignup();
      return;
    }

    const back = event.target.closest('[data-auth-back-login]');
    if (back && window.IndooneAuthUI?.showLogin) {
      event.preventDefault();
      event.stopPropagation();
      window.IndooneAuthUI.showLogin();
    }
  }, true);
})();