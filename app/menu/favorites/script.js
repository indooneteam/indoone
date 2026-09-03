window.initMenuFavorites = async function () {
  function escapeHtml(value) {
    return String(value ?? '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/\"/g, '&quot;').replace(/'/g, '&#039;');
  }
  const modal = document.getElementById('modal');
  try {
    const cloud = window.IndooneCloudAccounts;
    if (!cloud?.load) throw new Error('Account storage is unavailable.');
    await cloud.load();
    const favorites = (window.indooneState?.accounts || []).filter(account => account.favorite);
    const rows = favorites.length ? favorites.map(account => `
      <button type="button" class="settings-row favorite-list-row" data-favorite-account="${Number(account.id)}">
        <span><b>${escapeHtml(account.name)}</b><small>${escapeHtml(account.email || 'Authenticator account')}</small></span>
        <b class="favorite-code">${escapeHtml(account.code || '------')}</b>
      </button>`).join('') : '<div class="empty-state compact-empty"><div class="empty-icon">☆</div><h3>No favorite accounts</h3><p>Star an account to see it here.</p></div>';
    modal.innerHTML = `<div class="modal-head"><h2>Favorites</h2><button type="button" class="close-btn" data-close aria-label="Close Favorites">×</button></div><div id="favoritesList" class="drawer-list-modal">${rows}</div>`;
    modal.querySelectorAll('[data-favorite-account]').forEach(row => row.addEventListener('click', () => {
      const id = Number(row.dataset.favoriteAccount);
      closeModal();
      if (id) openAccount(id);
    }));
  } catch (error) {
    toast(error?.message || 'Could not load favorites');
  }
};