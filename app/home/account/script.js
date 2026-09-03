(() => {
  const legacyOpenAccount = window.openAccount;

  function open(id) {
    if (typeof legacyOpenAccount === 'function') {
      return legacyOpenAccount(id);
    }
  }

  window.IndooneHomeAccount = {
    open
  };
})();
