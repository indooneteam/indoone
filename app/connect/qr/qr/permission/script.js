(() => {
  const KEY = 'indoone_connect_qr_permissions_v1';
  const MARKUP_URL = 'app/connect/qr/qr/permission/index.html?v=20260903a';

  async function show() {
    const response = await fetch(MARKUP_URL, { cache: 'no-store' });
    if (!response.ok) throw new Error('Permission page could not be loaded.');

    window.openModal?.(await response.text());
    const modal = document.getElementById('modal');
    if (!modal) return;

    const save = values => {
      const permissions = {
        photos: values.includes('photos'),
        videos: values.includes('videos'),
        documents: values.includes('documents'),
        files: values.includes('files')
      };
      localStorage.setItem(KEY, JSON.stringify(permissions));
      const status = modal.querySelector('#qrPermissionStatus');
      if (status) status.textContent = values.length ? 'Selected access has been saved.' : 'All file access is denied.';
      window.toast?.(values.length ? 'Permissions saved.' : 'All permissions denied.');
    };

    modal.querySelector('[data-permission-close]')?.addEventListener('click', () => window.closeModal?.());
    modal.querySelector('[data-permission-save]')?.addEventListener('click', () => {
      save([...modal.querySelectorAll('[data-permission]:checked')].map(input => input.dataset.permission));
    });
    modal.querySelector('[data-permission-deny]')?.addEventListener('click', () => {
      modal.querySelectorAll('[data-permission]').forEach(input => { input.checked = false; });
      save([]);
    });
  }

  window.IndooneQrPermission = { show };
})();
