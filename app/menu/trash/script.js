window.initMenuTrash = async function () {
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
    const remaining = Math.max(0, Number(purgeAt || 0) - Date.now());
    return Math.max(1, Math.ceil(remaining / day));
  };

  const render = trash => {
    const rows = trash.length
      ? trash.map(item => {
          const id = Number(item.id);
          const left = daysLeft(item.purgeAt);
          return `
            <div class="trash-swipe-wrap" data-trash-id="${id}">
              <div class="trash-swipe-delete">
                <button
                  type="button"
                  class="trash-permanent-btn"
                  data-trash-delete="${id}"
                  aria-label="Delete ${escapeHtml(item.name || 'account')} permanently"
                >
                  Delete
                </button>
              </div>
              <div class="settings-row trash-list-row trash-swipe-card">
                <span>
                  <b>${escapeHtml(item.name || 'Account')}</b>
                  <small>
                    ${escapeHtml(item.email || 'Authenticator account')} ·
                    ${left} day${left === 1 ? '' : 's'} left
                  </small>
                </span>
                <button
                  type="button"
                  class="small-btn"
                  data-trash-restore="${id}"
                >
                  Restore
                </button>
              </div>
            </div>
          `;
        }).join('')
      : `
          <div class="empty-state compact-empty">
            <div class="empty-icon" style="font-size:27px">⌫</div>
            <h3>Trash is empty</h3>
            <p>Deleted accounts stay here for 30 days. Swipe left on an account to permanently delete it sooner.</p>
          </div>
        `;

    modal.innerHTML = `
      <div class="modal-head">
        <div style="display:flex;align-items:center;gap:10px">
          <span class="brand-mark" style="width:36px;height:36px;border-radius:12px;font-size:18px">I</span>
          <div>
            <h2 style="margin:0">Trash</h2>
            <small style="display:block;margin-top:2px;color:#8a8492;font-size:10px;font-weight:600">Deleted accounts</small>
          </div>
        </div>
        <button type="button" class="close-btn" data-close aria-label="Close Trash">×</button>
      </div>
      <div id="trashList" class="drawer-list-modal">${rows}</div>
    `;

    const reset = wrap => wrap?.classList.remove('trash-swipe-open');

    modal.querySelectorAll('[data-trash-restore]').forEach(button => {
      button.addEventListener('click', async event => {
        event.stopPropagation();
        const id = Number(button.dataset.trashRestore);
        if (!id) return;
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
      button.addEventListener('click', async event => {
        event.stopPropagation();
        const id = Number(button.dataset.trashDelete);
        if (!id) return;
        const item = trash.find(candidate => Number(candidate?.id) === id);
        const name = item?.name || 'this account';
        const confirmed = window.confirm(
          `Permanently delete ${name}?\n\nThis removes the account from Trash and all synced account copies. This action cannot be undone.`
        );
        if (!confirmed) return;
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

    let active = null;
    let startX = 0;
    let startY = 0;
    let moved = false;

    modal.querySelectorAll('.trash-swipe-wrap').forEach(wrap => {
      wrap.addEventListener('pointerdown', event => {
        if (event.pointerType === 'mouse' && event.button !== 0) return;
        active = wrap;
        startX = event.clientX;
        startY = event.clientY;
        moved = false;
        wrap.setPointerCapture?.(event.pointerId);
      });

      wrap.addEventListener('pointermove', event => {
        if (active !== wrap) return;
        const dx = event.clientX - startX;
        const dy = event.clientY - startY;
        if (Math.abs(dx) < Math.abs(dy) || Math.abs(dx) < 8) return;
        moved = true;
        wrap.classList.toggle('trash-swipe-open', dx < -48);
      });

      wrap.addEventListener('pointerup', () => {
        if (!active) return;
        active = null;
        if (!moved) return;
        if (!wrap.classList.contains('trash-swipe-open')) reset(wrap);
      });

      wrap.addEventListener('pointercancel', () => {
        active = null;
        reset(wrap);
      });

      wrap.addEventListener('click', event => {
        if (event.target.closest('button')) return;
        modal.querySelectorAll('.trash-swipe-open').forEach(other => {
          if (other !== wrap) reset(other);
        });
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
