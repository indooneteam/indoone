window.initMenuTrashV2 = async function () {
  const modal = document.getElementById('modal');
  const cloud = window.IndooneCloudAccounts;

  const escapeHtml = value => String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\"/g, '&quot;')
    .replace(/'/g, '&#039;');

  const daysLeft = purgeAt => {
    const day = 24 * 60 * 60 * 1000;
    return Math.max(1, Math.ceil(Math.max(0, Number(purgeAt || 0) - Date.now()) / day));
  };

  const render = trash => {
    const rows = trash.length
      ? trash.map(item => {
          const id = Number(item.id);
          const left = daysLeft(item.purgeAt);
          return `
            <article class="trash-row">
              <div class="trash-row-main">
                <strong>${escapeHtml(item.name || 'Account')}</strong>
                <small>${escapeHtml(item.email || 'Authenticator account')}</small>
                <small class="trash-expiry">Auto-delete in ${left} day${left === 1 ? '' : 's'}</small>
              </div>
              <div class="trash-row-actions">
                <button type="button" class="small-btn" data-trash-restore="${id}">Restore</button>
                <button type="button" class="trash-delete-btn" data-trash-delete="${id}">Delete permanently</button>
              </div>
            </article>
          `;
        }).join('')
      : '<div class="empty-state compact-empty"><h3>Trash is empty</h3><p>Deleted accounts can be restored for 30 days, or permanently deleted before they expire.</p></div>';

    modal.innerHTML = `
      <div class="modal-head">
        <div>
          <h2>Trash</h2>
          <small class="trash-header-note">30-day recovery</small>
        </div>
        <button type="button" class="close-btn" data-close aria-label="Close Trash">×</button>
      </div>
      <p class="trash-policy-note">Restore returns the account with its saved settings. “Delete permanently” removes it from Trash and synced account storage and cannot be undone.</p>
      <div id="trashList">${rows}</div>
    `;

    modal.querySelectorAll('[data-trash-restore]').forEach(button => {
      button.addEventListener('click', async () => {
        const id = Number(button.dataset.trashRestore);
        if (!id || !cloud?.restoreFromTrash) return;
        button.disabled = true;
        try {
          await cloud.restoreFromTrash(id);
          await cloud.load();
          window.renderAccounts?.();
          await refresh();
          toast('Account restored successfully');
        } catch (error) {
          button.disabled = false;
          toast(error?.message || 'Could not restore account');
        }
      });
    });

    modal.querySelectorAll('[data-trash-delete]').forEach(button => {
      button.addEventListener('click', async () => {
        const id = Number(button.dataset.trashDelete);
        if (!id || !cloud?.permanentlyDeleteFromTrash) return;
        const item = trash.find(candidate => Number(candidate?.id) === id);
        const name = item?.name || 'this account';
        if (!window.confirm(`Permanently delete ${name}?\n\nThis action cannot be undone.`)) return;
        button.disabled = true;
        try {
          await cloud.permanentlyDeleteFromTrash(id);
          await cloud.load();
          window.renderAccounts?.();
          await refresh();
          toast('Account permanently deleted');
        } catch (error) {
          button.disabled = false;
          toast(error?.message || 'Could not permanently delete account');
        }
      });
    });
  };

  async function refresh() {
    const trash = await cloud.listTrash();
    render(trash);
  }

  try {
    if (!modal || !cloud?.listTrash || !cloud?.restoreFromTrash || !cloud?.permanentlyDeleteFromTrash) {
      throw new Error('Trash storage is unavailable.');
    }
    await refresh();
  } catch (error) {
    toast(error?.message || 'Could not load Trash');
  }
};
