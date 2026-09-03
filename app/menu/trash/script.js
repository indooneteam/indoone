window.initMenuTrash = async function () {
  const modal = document.getElementById('modal');

  const escapeHtml = value => String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\"/g, '&quot;')
    .replace(/'/g, '&#039;');

  const daysLeft = purgeAt => {
    const milliseconds = Math.max(0, Number(purgeAt || 0) - Date.now());
    const day = 24 * 60 * 60 * 1000;

    return Math.max(1, Math.ceil(milliseconds / day));
  };

  try {
    const cloud = window.IndooneCloudAccounts;

    if (!cloud?.listTrash) {
      throw new Error('Trash storage is unavailable.');
    }

    const trash = await cloud.listTrash();

    const rows = trash.length
      ? trash.map(item => {
          const left = daysLeft(item.purgeAt);

          return `
            <div class="settings-row trash-list-row">
              <span>
                <b>${escapeHtml(item.name)}</b>
                <small>
                  ${escapeHtml(item.email || 'Authenticator account')} ·
                  ${left} day${left === 1 ? '' : 's'} left
                </small>
              </span>
              <button
                type="button"
                class="small-btn"
                data-trash-restore="${Number(item.id)}"
              >
                Restore
              </button>
            </div>
          `;
        }).join('')
      : `
          <div class="empty-state compact-empty">
            <div class="empty-icon" style="font-size:27px">⌫</div>
            <h3>Trash is empty</h3>
            <p>
              Deleted accounts stay here for 30 days. After that, they are
              permanently removed.
            </p>
          </div>
        `;

    modal.innerHTML = `
      <div class="modal-head">
        <div style="display:flex;align-items:center;gap:10px">
          <span
            class="brand-mark"
            style="width:36px;height:36px;border-radius:12px;font-size:18px"
          >I</span>
          <div>
            <h2 style="margin:0">Trash</h2>
            <small
              style="display:block;margin-top:2px;color:#8a8492;font-size:10px;font-weight:600"
            >
              Deleted accounts
            </small>
          </div>
        </div>
        <button type="button" class="close-btn" data-close aria-label="Close Trash">×</button>
      </div>
      <div id="trashList" class="drawer-list-modal">
        ${rows}
      </div>
    `;

    modal.querySelectorAll('[data-trash-restore]').forEach(button => {
      button.addEventListener('click', async () => {
        const id = Number(button.dataset.trashRestore);

        if (!id) return;

        button.disabled = true;

        try {
          const restored = await window.IndooneCloudAccounts.restoreFromTrash(id);
          const accounts = window.indooneState?.accounts || [];
          const exists = accounts.some(account => Number(account.id) === Number(restored.id));

          if (!exists) {
            accounts.push(restored);
          }

          window.renderAccounts?.();
          closeModal();
          toast('Account restored');
        } catch (error) {
          button.disabled = false;
          toast(error?.message || 'Could not restore account');
        }
      });
    });
  } catch (error) {
    toast(error?.message || 'Could not load Trash');
  }
};
