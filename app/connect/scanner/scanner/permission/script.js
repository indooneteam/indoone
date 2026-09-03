(() => {
  const MARKUP_URL = 'app/connect/scanner/scanner/permission/index.html?v=20260903a';
  const PERMISSION_KEY = 'indoone_connect_pending_permissions_v1';

  async function show() {
    try {
      const response = await fetch(MARKUP_URL, { cache: 'no-store' });
      if (!response.ok) throw new Error('Permission page could not be loaded.');

      window.openModal?.(await response.text());

      const modal = document.getElementById('modal');
      if (!modal) return;

      modal.querySelector('[data-permission-close]')?.addEventListener('click', () => {
        window.closeModal?.();
      });

      const save = allowed => {
        const permissions = {
          photos: false,
          videos: false,
          documents: false,
          files: false
        };

        allowed.forEach(name => {
          permissions[name] = true;
        });

        localStorage.setItem(PERMISSION_KEY, JSON.stringify({
          permissions,
          savedAt: Date.now()
        }));

        window.dispatchEvent(new CustomEvent('indoone-permissions-saved', {
          detail: { permissions }
        }));

        const status = modal.querySelector('#scannerPermissionStatus');
        if (status) {
          status.textContent = allowed.length
            ? 'Selected access has been saved for this device.'
            : 'All file access is denied.';
        }

        window.toast?.(allowed.length ? 'Permissions saved.' : 'All permissions denied.');

        if (allowed.length) {
          window.closeModal?.();
        }
      };

      modal.querySelector('[data-permission-save]')?.addEventListener('click', event => {
        event.preventDefault();
        const allowed = [...modal.querySelectorAll('[data-permission]:checked')]
          .map(input => input.dataset.permission)
          .filter(Boolean);
        save(allowed);
      });

      modal.querySelector('[data-permission-deny]')?.addEventListener('click', event => {
        event.preventDefault();
        modal.querySelectorAll('[data-permission]').forEach(input => {
          input.checked = false;
        });
        save([]);
      });
    } catch (error) {
      window.toast?.(error?.message || 'Could not open permissions.');
    }
  }

  window.IndooneScannerPermission = { show };
})();
