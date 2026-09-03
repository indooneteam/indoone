(() => {
  let endpoints = new Map();

  const load = async () => {
    const response = await fetch(
      `app/connect/connect-device/index.html?v=20260917a`,
      { cache: 'no-store' }
    );

    if (!response.ok) {
      throw new Error('Connect flow could not be loaded.');
    }

    return response.text();
  };

  const escapeHtml = value =>
    String(value || '').replace(
      /[&<>\"']/g,
      character => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '\"': '&quot;',
        "'": '&#39;'
      }[character])
    );

  const deviceName = () =>
    localStorage.getItem('indoone_connect_device_name') || 'Indoone Device';

  function render() {
    const host = document.getElementById('connectFeatureNearbyList');
    if (!host) {
      return;
    }

    const rows = [...endpoints]
      .map(([id, item]) => `
        <div class="device-nearby-row">
          <div>
            <b>${escapeHtml(item.name)}</b>
            <small>Nearby device</small>
          </div>
          <button
            type="button"
            class="primary"
            data-nearby-endpoint="${escapeHtml(id)}"
          >
            Connect
          </button>
        </div>
      `)
      .join('');

    host.innerHTML =
      rows ||
      '<div class="connect-empty">Searching for nearby Indoone devices…</div>';
  }

  function target(type) {
    const mobile = type === 'phone';

    openModal(`
      <div class="modal-head">
        <div>
          <p class="eyebrow">NEARBY DISCOVERY</p>
          <h2>${mobile ? 'Connect phone' : 'Connect laptop / PC'}</h2>
        </div>
        <button
          type="button"
          class="close-btn"
          data-device-flow-close
          aria-label="Close"
        >
          ×
        </button>
      </div>

      <p class="connect-muted">
        Keep both devices nearby and keep Indoone open.
      </p>

      <div class="device-connect-actions">
        <button type="button" class="primary" data-device-start>
          ${mobile ? 'Find Nearby Phone' : 'Find Nearby Computer'}
        </button>
        <button type="button" class="secondary" data-device-show-qr>
          Show My QR
        </button>
      </div>

      <div id="connectFeatureNearbyList" class="device-nearby-list"></div>

      <p id="connectFeatureNearbyStatus" class="connect-muted">
        Ready to search.
      </p>
    `);

    const modal = document.getElementById('modal');

    modal?.querySelectorAll('[data-device-flow-close]').forEach(button => {
      button.addEventListener('click', () => window.closeModal?.());
    });

    modal?.querySelector('[data-device-start]')?.addEventListener('click', () => {
      const status = modal.querySelector('#connectFeatureNearbyStatus');

      if (window.IndooneNative) {
        window.IndooneNative.requestNearbyPermissions?.();
        window.IndooneNative.startNearbyDiscovery?.();

        if (status) {
          status.textContent = 'Searching for nearby Indoone devices…';
        }
      } else if (status) {
        status.textContent = 'Nearby Connect works in the Android app.';
      }
    });

    modal?.querySelector('[data-device-show-qr]')?.addEventListener(
      'click',
      () => window.showConnectQr?.()
    );

    render();
  }

  window.showConnectChoice = async () => {
    try {
      openModal(await load());

      const modal = document.getElementById('modal');

      modal?.querySelectorAll('[data-device-connect-close]').forEach(button => {
        button.addEventListener('click', () => window.closeModal?.());
      });

      modal?.querySelectorAll('[data-device-target]').forEach(button => {
        button.addEventListener('click', () => {
          target(button.dataset.deviceTarget);
        });
      });
    } catch (error) {
      window.toast?.(error?.message || 'Could not open Connect.');
    }
  };

  window.addEventListener('indoone-nearby', event => {
    const detail = event.detail || {};

    if (detail.type === 'endpointFound') {
      endpoints.set(detail.endpointId || detail.message, {
        name: detail.message || 'Nearby Indoone Device'
      });
      render();
    }

    if (detail.type === 'endpointLost') {
      endpoints.delete(detail.endpointId || detail.message);
      render();
    }

    if (detail.type === 'connectionResult' && detail.message === 'connected') {
      const item = endpoints.get(detail.endpointId);
      window.toast?.(`${item?.name || 'Nearby device'} connected.`);
    }
  });

  document.addEventListener('click', event => {
    const button = event.target.closest('[data-nearby-endpoint]');
    if (!button) {
      return;
    }

    event.preventDefault();

    const endpointId = button.dataset.nearbyEndpoint;

    if (window.IndooneNative?.connectNearbyEndpoint) {
      window.IndooneNative.connectNearbyEndpoint(endpointId, deviceName());
    } else {
      window.toast?.('Nearby Connect works in the Android app.');
    }
  });
})();
