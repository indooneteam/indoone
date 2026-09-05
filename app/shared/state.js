window.indooneState = {
  accounts: [],
  search: '',
  newestFirst: true,
  trash: []
};

window.handleSortAccounts = function () {
  indooneState.newestFirst = !indooneState.newestFirst;
  renderAccounts();
};

window.refreshAccountCodes = async function () {
  const now = Math.floor(Date.now() / 1000);

  for (const a of indooneState.accounts) {
    if (!a.secret) continue;

    a.period = Number(a.period || 30);
    a.digits = Number(a.digits || 6);
    a.algorithm = a.algorithm || 'SHA1';
    a.seconds = a.period - (now % a.period);

    try {
      a.code = await TOTP.generate(
        a.secret,
        Math.floor(now / a.period),
        a.digits,
        a.algorithm
      );
    } catch (_) {
      a.code = '------';
    }
  }

  renderAccounts();
};

window.renderAccounts = function () {
  const {
    accounts,
    search,
    newestFirst
  } = indooneState;

  // Search matches only the account/service name, not the email address.
  let list = accounts.filter(a =>
    String(a.name || '')
      .toLowerCase()
      .includes(search)
  );

  list = [...list].sort(
    newestFirst
      ? (a, b) => b.id - a.id
      : (a, b) => a.name.localeCompare(b.name)
  );

  const listEl = document.getElementById('accountList');

  listEl.innerHTML = list.map(a => `
    <article
      class="account"
      data-id="${a.id}"
      tabindex="0"
    >
      <div class="service-icon ${a.cls}">${a.icon}</div>
      <div class="account-info">
        <b>${a.name}</b>
        <span>${a.email}</span>
        <strong>${a.code || '------'}</strong>
      </div>
      <button
        class="fav ${a.favorite ? 'active' : ''}"
        data-fav="${a.id}"
        aria-label="Favorite"
      >
        ${a.favorite ? '★' : '☆'}
      </button>
      <div class="timer">
        ${a.seconds ?? 30}
      </div>
    </article>
  `).join('');

  document.getElementById('emptyState').hidden = list.length > 0;
  document.getElementById('drawerCount').textContent = accounts.length;
};

window.startDemoTimers = function () {
  setInterval(refreshAccountCodes, 1000);
  refreshAccountCodes();
};
