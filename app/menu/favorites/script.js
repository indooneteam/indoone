window.initMenuFavorites = async function () {
  const modal = document.getElementById('modal');

  const escapeHtml = value => String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');

  if (!modal) {
    return;
  }

  try {
    const cloud = window.IndooneCloudAccounts;

    if (!cloud?.load) {
      throw new Error('Account storage is unavailable.');
    }

    await cloud.load();

    const favorites = (window.indooneState?.accounts || [])
      .filter(account => account.favorite);

    const rows = favorites.length
      ? favorites.map(account => `
          <button
            type="button"
            class="favorite-account-row"
            data-favorite-account="${Number(account.id)}"
          >
            <span class="favorite-account-main">
              <span class="favorite-account-name">${escapeHtml(account.name)}</span>
              <span class="favorite-account-email">${escapeHtml(account.email || 'Authenticator account')}</span>
            </span>
            <span class="favorite-account-code">${escapeHtml(account.code || '------')}</span>
          </button>
        `).join('')
      : `
          <div class="favorite-empty">
            <div class="empty-icon">☆</div>
            <h3>No favorite accounts</h3>
            <p>Star an account to see it here.</p>
          </div>
        `;

    modal.innerHTML = `
      <section class="menu-feature-shell favorites-feature" data-menu-feature data-menu-section="favorites">
        <div class="modal-head">
          <div>
            <p class="eyebrow">SAVED ACCOUNTS</p>
            <h2>Favorites</h2>
          </div>
          <button
            type="button"
            class="close-btn"
            data-close
            aria-label="Close Favorites"
          >
            ×
          </button>
        </div>
        <div id="favoritesList" class="favorite-account-list">
          ${rows}
        </div>
      </section>
    `;

    modal.querySelectorAll('[data-favorite-account]').forEach(row => {
      row.addEventListener('click', () => {
        const id = Number(row.dataset.favoriteAccount);

        closeModal();

        if (id && typeof openAccount === 'function') {
          openAccount(id);
        }
      });
    });
  } catch (error) {
    toast(error?.message || 'Could not load favorites');
  }
};
