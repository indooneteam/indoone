(() => {
  const PENDING_KEY = 'indoone_connect_permission_draft_v1';
  const drafts = new Map();
  const connectionMeta = new Map();

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
    } catch (_) {
      return null;
    }
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

  function permissionPageAlreadyOpen() {
    const modal = document.getElementById('modal');
    return Boolean(modal?.querySelector('[data-live-permission-save], [data-bilateral-permission-save]'));
  }

  function openPermissionPage(endpointId, deviceName, direction) {
    if (permissionPageAlreadyOpen()) return;

    const existingDraft = loadDraft(endpointId) || {
      photos: false,
      videos: false,
      documents: false,
      files: false
    };

    const title = direction === 'incoming'
      ? 'Allow access to your data'
      : 'Choose data you will share';
    const description = direction === 'incoming'
      ? `${deviceName} is connected to this device. Choose exactly what it may access.`
      : `Choose exactly what ${deviceName} may access from this device.`;

    const markup = `
      <section class="connect-feature connect-permission-feature" data-bilateral-permission="${escapeHtml(endpointId)}">
        <div class="modal-head">
          <div>
            <p class="eyebrow">DEVICE ACCESS</p>
            <h2>${escapeHtml(title)}</h2>
          </div>
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
        <p class="connect-muted" data-bilateral-permission-status>Nearby connection is established. Choose and save what this device may access.</p>
        <button type="button" class="primary" data-bilateral-permission-save>Allow selected</button>
        <button type="button" class="secondary" data-bilateral-permission-deny>Deny all</button>
      </section>
    `;

    window.openModal?.(markup);
    const modal = document.getElementById('modal');
    if (!modal) return;

    const save = allowed => {
      const draft = {
        photos: allowed.includes('photos'),
        videos: allowed.includes('videos'),
        documents: allowed.includes('documents'),
        files: allowed.includes('files'),
        savedAt: Date.now()
      };
      saveDraft(endpointId, draft);

      const message = JSON.stringify({
        type: 'indoone-permissions',
        permissions: {
          photos: draft.photos,
          videos: draft.videos,
          documents: draft.documents,
          files: draft.files
        }
      });

      try { window.IndooneNative?.sendNearbyText?.(endpointId, message); } catch (_) {}
      window.dispatchEvent(new CustomEvent('indoone-permissions-saved', {
        detail: { endpointId, permissions: draft }
      }));
      window.toast?.(allowed.length ? 'Permissions saved.' : 'All permissions denied.');
      window.closeModal?.();
    };

    modal.querySelector('[data-bilateral-permission-save]')?.addEventListener('click', () => {
      const allowed = [...modal.querySelectorAll('[data-bilateral-permission]:checked')]
        .map(input => input.dataset.bilateralPermission)
        .filter(key => ['photos', 'videos', 'documents', 'files'].includes(key));
      save(allowed);
    });
    modal.querySelector('[data-bilateral-permission-deny]')?.addEventListener('click', () => save([]));
    modal.querySelector('[data-bilateral-permission-close]')?.addEventListener('click', () => window.closeModal?.());
  }

  window.addEventListener('indoone-nearby', event => {
    const detail = event.detail || {};
    const endpointId = String(detail.endpointId || '').trim();
    if (!endpointId) return;

    if (detail.type === 'connectionInitiated') {
      connectionMeta.set(endpointId, {
        name: String(detail.message || 'Nearby device'),
        direction: detail.incoming ? 'incoming' : 'outgoing'
      });
      return;
    }

    if (detail.type === 'connectionResult' && detail.message === 'connected') {
      const meta = connectionMeta.get(endpointId) || { name: 'Nearby device', direction: 'outgoing' };
      window.setTimeout(() => openPermissionPage(endpointId, meta.name, meta.direction), 0);

      const draft = loadDraft(endpointId);
      if (draft) {
        const message = JSON.stringify({
          type: 'indoone-permissions',
          permissions: {
            photos: Boolean(draft.photos),
            videos: Boolean(draft.videos),
            documents: Boolean(draft.documents),
            files: Boolean(draft.files)
          }
        });
        try { window.IndooneNative?.sendNearbyText?.(endpointId, message); } catch (_) {}
      }
      return;
    }

    if (detail.type === 'disconnected') {
      clearDraft(endpointId);
      connectionMeta.delete(endpointId);
    }
  });
})();
