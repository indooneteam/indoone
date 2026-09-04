(() => {
  const PENDING_KEY = 'indoone_connect_permission_draft_v1';
  const DEVICE_KEY = 'indoone_connect_devices_v1';
  const drafts = new Map();
  const connectionMeta = new Map();
  const openEndpoints = new Set();

  const escapeHtml = value => String(value || '').replace(/[&<>\"']/g, char => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '\"': '&quot;',
    "'": '&#39;'
  }[char]));

  function loadDraft(endpointId) {
    if (drafts.has(endpointId)) return drafts.get(endpointId);
    try {
      const all = JSON.parse(sessionStorage.getItem(PENDING_KEY) || '{}');
      const value = all?.[endpointId];
      if (value) drafts.set(endpointId, value);
      return value || null;
    } catch (_) { return null; }
  }

  function saveDraft(endpointId, draft) {
    drafts.set(endpointId, draft);
    try {
      const all = JSON.parse(sessionStorage.getItem(PENDING_KEY) || '{}');
      all[endpointId] = draft;
      sessionStorage.setItem(PENDING_KEY, JSON.stringify(all));
    } catch (_) {}
  }

  function clearDraft(endpointId) {
    drafts.delete(endpointId);
    try {
      const all = JSON.parse(sessionStorage.getItem(PENDING_KEY) || '{}');
      delete all[endpointId];
      sessionStorage.setItem(PENDING_KEY, JSON.stringify(all));
    } catch (_) {}
  }

  function updateLocalPermissions(endpointId, permissions) {
    try {
      const devices = JSON.parse(localStorage.getItem(DEVICE_KEY) || '[]');
      if (!Array.isArray(devices)) return;
      const device = devices.find(item => item.endpointId === endpointId);
      if (!device) return;
      device.permissions = {
        ...(device.permissions || {}),
        photos: Boolean(permissions.photos),
        videos: Boolean(permissions.videos),
        documents: Boolean(permissions.documents),
        files: Boolean(permissions.files)
      };
      device.updatedAt = Date.now();
      localStorage.setItem(DEVICE_KEY, JSON.stringify(devices));
      window.dispatchEvent(new CustomEvent('indoone-devices-changed'));
    } catch (_) {}
  }

  function sendPermissions(endpointId, permissions) {
    const message = JSON.stringify({
      type: 'indoone-permissions',
      permissions: {
        photos: Boolean(permissions.photos),
        videos: Boolean(permissions.videos),
        documents: Boolean(permissions.documents),
        files: Boolean(permissions.files)
      }
    });
    try { window.IndooneNative?.sendNearbyText?.(endpointId, message); } catch (_) {}
  }

  function openPermissionPage(endpointId, deviceName, direction) {
    endpointId = String(endpointId || '').trim();
    if (!endpointId || openEndpoints.has(endpointId)) return;
    const modal = document.getElementById('modal');
    if (modal?.querySelector('[data-live-permission-save], [data-bilateral-permission-save]')) return;

    openEndpoints.add(endpointId);
    const existingDraft = loadDraft(endpointId) || { photos: false, videos: false, documents: false, files: false };
    const title = direction === 'incoming' ? 'Allow access to your data' : 'Choose data you will share';
    const description = direction === 'incoming'
      ? `${deviceName} is connecting to this device. Choose exactly what it may access.`
      : `Choose exactly what ${deviceName} may access from this device.`;

    window.openModal?.(`
      <section class="connect-feature connect-permission-feature" data-bilateral-permission="${escapeHtml(endpointId)}">
        <div class="modal-head">
          <div><p class="eyebrow">DEVICE ACCESS</p><h2>${escapeHtml(title)}</h2></div>
          <button type="button" class="close-btn" data-bilateral-permission-close aria-label="Close permissions">×</button>
        </div>
        <p class="connect-muted">${escapeHtml(description)}</p>
        <div class="permission-list">
          ${[
            ['photos', 'Photos'],
            ['videos', 'Videos'],
            ['documents', 'PDF / Documents'],
            ['files', 'Files']
          ].map(([key, label]) => `
            <label class="permission-item">
              <input type="checkbox" data-bilateral-permission="${key}" ${existingDraft[key] ? 'checked' : ''} />
              <span><b>${label}</b><small>Allow access to approved ${label.toLowerCase()}.</small></span>
            </label>
          `).join('')}
        </div>
        <p class="connect-muted" data-bilateral-permission-status>Choose what this device may access, then save.</p>
        <button type="button" class="primary" data-bilateral-permission-save>Allow selected</button>
        <button type="button" class="secondary" data-bilateral-permission-deny>Deny all</button>
      </section>
    `);

    const currentModal = document.getElementById('modal');
    if (!currentModal) { openEndpoints.delete(endpointId); return; }

    const save = allowed => {
      const draft = {
        photos: allowed.includes('photos'),
        videos: allowed.includes('videos'),
        documents: allowed.includes('documents'),
        files: allowed.includes('files'),
        savedAt: Date.now()
      };
      saveDraft(endpointId, draft);
      updateLocalPermissions(endpointId, draft);
      sendPermissions(endpointId, draft);
      window.dispatchEvent(new CustomEvent('indoone-permissions-saved', { detail: { endpointId, permissions: draft } }));
      window.toast?.(allowed.length ? 'Permissions saved.' : 'All permissions denied.');
      window.closeModal?.();
      openEndpoints.delete(endpointId);
    };

    currentModal.querySelector('[data-bilateral-permission-save]')?.addEventListener('click', () => {
      const allowed = [...currentModal.querySelectorAll('[data-bilateral-permission]:checked')]
        .map(input => input.dataset.bilateralPermission)
        .filter(key => ['photos', 'videos', 'documents', 'files'].includes(key));
      save(allowed);
    });
    currentModal.querySelector('[data-bilateral-permission-deny]')?.addEventListener('click', () => save([]));
    currentModal.querySelector('[data-bilateral-permission-close]')?.addEventListener('click', () => {
      openEndpoints.delete(endpointId);
      clearDraft(endpointId);
      window.closeModal?.();
    });
  }

  window.addEventListener('indoone-nearby', event => {
    const detail = event.detail || {};
    const endpointId = String(detail.endpointId || '').trim();
    if (!endpointId) return;

    if (detail.type === 'connectionInitiated') {
      const meta = {
        name: String(detail.message || 'Nearby device'),
        direction: detail.incoming ? 'incoming' : 'outgoing'
      };
      connectionMeta.set(endpointId, meta);
      window.setTimeout(() => openPermissionPage(endpointId, meta.name, meta.direction), 0);
      return;
    }

    if (detail.type === 'connectionResult' && detail.message === 'connected') {
      const meta = connectionMeta.get(endpointId) || { name: 'Nearby device', direction: 'outgoing' };
      window.setTimeout(() => openPermissionPage(endpointId, meta.name, meta.direction), 0);
      const draft = loadDraft(endpointId);
      if (draft) sendPermissions(endpointId, draft);
      return;
    }

    if (detail.type === 'disconnected') {
      clearDraft(endpointId);
      openEndpoints.delete(endpointId);
      connectionMeta.delete(endpointId);
    }
  });
})();
