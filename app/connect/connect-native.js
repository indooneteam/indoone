(() => {
  const native = window.IndooneNative;
  const DEVICE_KEY = 'indoone_connect_devices_v1';
  const state = {
    mode: null,
    targetType: null,
    endpoints: new Map(),
    pendingMode: null,
    activeEndpointId: ''
  };

  if (!native) return;

  function escapeHtml(value) {
    return String(value || '').replace(/[&<>'\"]/g, char => ({
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      "'": '&#39;',
      '\"': '&quot;'
    }[char]));
  }

  function deviceName() {
    const saved = localStorage.getItem('indoone_connect_device_name');
    if (saved) return saved;

    const uid = window.IndooneFirebase?.auth?.currentUser?.uid || '';
    const suffix = uid
      ? uid.slice(-4).toUpperCase()
      : Math.random().toString(36).slice(2, 6).toUpperCase();
    const mobile = /Mobi|Android/i.test(navigator.userAgent);
    const name = `Indoone ${mobile ? 'Phone' : 'Computer'} ${suffix}`;

    localStorage.setItem('indoone_connect_device_name', name);
    return name;
  }

  function loadDevices() {
    try {
      const value = JSON.parse(localStorage.getItem(DEVICE_KEY) || '[]');
      return Array.isArray(value) ? value : [];
    } catch (_) {
      return [];
    }
  }

  function saveDevices(devices) {
    localStorage.setItem(DEVICE_KEY, JSON.stringify(devices));
  }

  function notify(message) {
    window.toast?.(message);
  }

  function openStatus(title, body) {
    if (typeof window.openModal !== 'function') return;

    window.openModal(`
      <div class="modal-head">
        <h2>${escapeHtml(title)}</h2>
        <button type="button" class="close-btn" data-close>×</button>
      </div>
      ${body}
    `);
  }

  function rememberConnected(name, endpointId) {
    const devices = loadDevices();
    let device = devices.find(item =>
      item.endpointId === endpointId || item.name === name
    );

    if (!device) {
      device = {
        id: `nearby-${Date.now()}`,
        name,
        endpointId,
        type: /computer|laptop|pc/i.test(name) ? 'computer' : 'phone',
        connected: true,
        trusted: false,
        permissions: {
          photos: false,
          videos: false,
          documents: false,
          downloads: false,
          upload: false,
          delete: false,
          alwaysAllow: false
        },
        createdAt: Date.now()
      };
      devices.push(device);
    } else {
      device.name = name;
      device.endpointId = endpointId;
      device.connected = true;
      device.updatedAt = Date.now();
    }

    saveDevices(devices);
  }

  function updateDeviceConnection(endpointId, connected) {
    const devices = loadDevices();
    let changed = false;

    const next = devices.map(device => {
      if (device.endpointId !== endpointId) return device;
      changed = true;
      return {
        ...device,
        connected,
        updatedAt: Date.now()
      };
    });

    if (changed) saveDevices(next);
  }

  function startTransport() {
    if (!state.pendingMode) return;

    const mode = state.pendingMode;
    state.pendingMode = null;
    state.mode = mode;

    try {
      if (mode === 'advertise') {
        native.startNearbyAdvertising(deviceName());
      } else {
        native.startNearbyDiscovery();
      }
    } catch (_) {
      notify('Nearby connection is unavailable on this device.');
      return;
    }

    const status = document.getElementById('nearbyStatus');
    if (status) {
      status.textContent = mode === 'advertise'
        ? 'Your device is discoverable nearby.'
        : 'Searching for nearby Indoone devices…';
    }
  }

  function beginNearby(mode, targetType) {
    state.endpoints.clear();
    state.pendingMode = mode;
    state.targetType = targetType;

    const isAdvertise = mode === 'advertise';
    openStatus(
      isAdvertise ? 'Waiting for connection' : 'Nearby devices',
      isAdvertise
        ? `
          <div class="device-summary">
            <div class="device-icon large">${targetType === 'computer' ? '💻' : '📱'}</div>
            <div>
              <b>${escapeHtml(deviceName())}</b>
              <small>Waiting for a nearby connection</small>
            </div>
          </div>
          <div class="connect-empty">
            Keep this device open. Another Indoone device can discover it and request a connection.
          </div>
          <div id="nearbyStatus" class="connect-muted">Requesting Nearby permissions…</div>
          <button type="button" class="secondary" data-nearby-stop>Stop</button>
        `
        : `
          <div id="nearbyDiscoveryList">
            <div class="connect-empty">Scanning for nearby Indoone devices…</div>
          </div>
          <div id="nearbyStatus" class="connect-muted">Requesting Nearby permissions…</div>
          <button type="button" class="secondary" data-nearby-stop>Stop</button>
        `
    );

    native.requestNearbyPermissions?.();
  }

  window.startNearbyConnection = function (targetType = 'phone') {
    const label = targetType === 'computer' ? 'Laptop / PC' : 'Phone';
    const icon = targetType === 'computer' ? '💻' : '📱';

    openStatus(
      `Connect ${label}`,
      `
        <div class="device-summary">
          <div class="device-icon large">${icon}</div>
          <div>
            <b>Nearby connection</b>
            <small>Connection and data access are separate.</small>
          </div>
        </div>
        <div class="connect-empty">
          Keep both devices nearby. One device searches while the other is discoverable.
        </div>
        <button type="button" class="primary" data-nearby-mode="discover">Find nearby ${label}</button>
        <button type="button" class="secondary" data-nearby-mode="advertise">Make this device discoverable</button>
        <button type="button" class="secondary" data-close>Cancel</button>
      `
    );
  };

  document.addEventListener('click', event => {
    const modeButton = event.target.closest('[data-nearby-mode]');
    if (modeButton) {
      event.preventDefault();
      event.stopPropagation();
      const mode = modeButton.dataset.nearbyMode === 'advertise'
        ? 'advertise'
        : 'discover';
      beginNearby(mode, state.targetType || 'phone');
      return;
    }

    const endpointButton = event.target.closest('[data-nearby-endpoint]');
    if (endpointButton) {
      event.preventDefault();
      event.stopPropagation();

      const endpointId = endpointButton.dataset.nearbyEndpoint;
      const item = state.endpoints.get(endpointId);
      if (!item) return;

      state.activeEndpointId = endpointId;
      native.connectNearbyEndpoint(endpointId, deviceName());
      notify(`Connection request sent to ${item.name}.`);
      return;
    }

    const acceptButton = event.target.closest('[data-nearby-accept]');
    if (acceptButton) {
      event.preventDefault();
      native.acceptNearbyConnection?.(acceptButton.dataset.nearbyAccept);
      return;
    }

    const rejectButton = event.target.closest('[data-nearby-reject]');
    if (rejectButton) {
      event.preventDefault();
      native.rejectNearbyConnection?.(rejectButton.dataset.nearbyReject);
      window.closeModal?.();
      return;
    }

    const stopButton = event.target.closest('[data-nearby-stop]');
    if (stopButton) {
      event.preventDefault();
      native.stopNearby?.();
      window.closeModal?.();
    }
  }, true);

  window.stopNearby = function () {
    native.stopNearby?.();
    state.mode = null;
    state.pendingMode = null;
    state.endpoints.clear();
  };

  window.addEventListener('indoone-nearby', event => {
    const detail = event.detail || {};

    if (detail.type === 'permissions') {
      const status = document.getElementById('nearbyStatus');
      const granted = detail.message === 'granted';

      if (status) {
        status.textContent = granted
          ? 'Nearby permissions granted.'
          : 'Nearby permissions denied.';
      }

      if (granted) {
        startTransport();
      } else {
        state.pendingMode = null;
        notify('Nearby permissions are required.');
      }
      return;
    }

    if (detail.type === 'endpointFound') {
      const endpointId = detail.endpointId;
      if (!endpointId) return;

      state.endpoints.set(endpointId, {
        endpointId,
        name: detail.message || 'Nearby Indoone Device'
      });

      const list = document.getElementById('nearbyDiscoveryList');
      if (!list) return;

      list.innerHTML = [...state.endpoints.values()].map(item => `
        <button type="button" class="device-card connected" data-nearby-endpoint="${escapeHtml(item.endpointId)}">
          <div class="device-icon">${state.targetType === 'computer' ? '💻' : '📱'}</div>
          <div>
            <b>${escapeHtml(item.name)}</b>
            <small>Nearby • Ready to connect</small>
          </div>
          <span class="device-dot"></span>
        </button>
      `).join('');
      return;
    }

    if (detail.type === 'endpointLost') {
      state.endpoints.delete(detail.endpointId);
      const list = document.getElementById('nearbyDiscoveryList');

      if (list) {
        list.innerHTML = state.endpoints.size
          ? [...state.endpoints.values()].map(item => `
              <button type="button" class="device-card connected" data-nearby-endpoint="${escapeHtml(item.endpointId)}">
                <div class="device-icon">📱</div>
                <div>
                  <b>${escapeHtml(item.name)}</b>
                  <small>Nearby • Ready to connect</small>
                </div>
                <span class="device-dot"></span>
              </button>
            `).join('')
          : '<div class="connect-empty">No nearby Indoone devices found.</div>';
      }
      return;
    }

    if (detail.type === 'connectionInitiated' && detail.incoming) {
      openStatus(
        'Connection request',
        `
          <div class="device-summary">
            <div class="device-icon large">📱</div>
            <div>
              <b>${escapeHtml(detail.message || 'Nearby device')}</b>
              <small>Wants to connect to this device</small>
            </div>
          </div>
          <div class="connect-empty">
            <b>Verification code: ${escapeHtml(detail.authenticationDigits || '----')}</b><br>
            Confirm that the code matches on both devices before accepting.
          </div>
          <button type="button" class="primary" data-nearby-accept="${escapeHtml(detail.endpointId)}">Accept connection</button>
          <button type="button" class="secondary" data-nearby-reject="${escapeHtml(detail.endpointId)}">Reject</button>
        `
      );
      return;
    }

    if (detail.type === 'connectionInitiated' && !detail.incoming) {
      notify(`Waiting for ${detail.message || 'the device'} to approve the connection.`);
      return;
    }

    if (detail.type === 'connectionResult') {
      if (detail.message === 'connected') {
        const item = state.endpoints.get(detail.endpointId);
        const name = item?.name || 'Nearby device';

        rememberConnected(name, detail.endpointId);
        state.activeEndpointId = detail.endpointId;
        notify(`Connected to ${name}.`);

        openStatus(
          'Connected',
          `
            <div class="device-summary">
              <div class="device-icon large">📱</div>
              <div>
                <b>${escapeHtml(name)}</b>
                <small>Connected • Nearby</small>
              </div>
            </div>
            <div class="connect-empty">
              Connection is ready. Data access remains separate and requires explicit permission.
            </div>
            <button type="button" class="primary" data-close>Done</button>
          `
        );
      } else {
        notify('Nearby connection was rejected or could not be completed.');
      }
      return;
    }

    if (detail.type === 'disconnected') {
      updateDeviceConnection(detail.endpointId, false);
      state.activeEndpointId = '';
      notify('Nearby device disconnected.');
      return;
    }

    if (detail.type === 'error') {
      notify(detail.message || 'Nearby connection error.');
    }
  });
})();
