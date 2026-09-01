window.IndooneStorage = (() => {
  const KEY = 'indoone.authenticator.accounts.v1';

  function load() {
    try {
      const raw = localStorage.getItem(KEY);
      return raw ? JSON.parse(raw) : null;
    } catch (_) {
      return null;
    }
  }

  function save(accounts) {
    localStorage.setItem(KEY, JSON.stringify(accounts));
  }

  function initialize() {
    const saved = load();
    if (Array.isArray(saved)) {
      window.indooneState.accounts = saved;
      return true;
    }
    save(window.indooneState.accounts);
    return false;
  }

  return { load, save, initialize };
})();

IndooneStorage.initialize();
