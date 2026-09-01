window.IndooneTotpController = (() => {
  let timerId = null;

  function remaining(period=30) {
    const now = Math.floor(Date.now() / 1000);
    return period - (now % period);
  }

  async function refreshAccount(account) {
    if (!account?.secret) return;
    try {
      const period = Number(account.period || 30);
      const counter = Math.floor(Date.now() / 1000 / period);
      const code = await TOTP.generate(account.secret, counter, Number(account.digits || 6), account.algorithm || 'SHA1');
      account.code = code.replace(/(\d{3})(\d{3})/, '$1 $2');
      account.seconds = remaining(period);
    } catch (_) {
      account.code = '------';
      account.seconds = remaining(Number(account.period || 30));
    }
  }

  async function refreshAll() {
    await Promise.all(indooneState.accounts.map(refreshAccount));
    renderAccounts();
    persist();
  }

  function persist() {
    if (window.IndooneStorage) {
      IndooneStorage.save(indooneState.accounts);
    }
  }

  function start() {
    refreshAll();
    clearInterval(timerId);
    timerId = setInterval(refreshAll, 1000);
  }

  return { refreshAccount, refreshAll, start, persist };
})();
