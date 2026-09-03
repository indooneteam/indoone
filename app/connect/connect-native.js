(() => {
  const native = window.IndooneNative;
  const DEVICE_KEY = 'indoone_connect_devices_v1';
  const PENDING_DEVICE_KEY = 'indoone_connect_pending_device_v1';
  const state = {
    mode: null,
    targetType: null,
    endpoints: new Map(),
    pendingMode: null,
    pendingPairing: null,
    activeEndpointId: '',
    endpointDirections: new Map()
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
    window.dispatchEvent(new CustomEvent('indoone-devices-changed'));
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

  function readPendingDevice() {
    try {
      const value = JSON.parse(sessionStorage.getItem(PENDING_DEVICE_KEY) || '');
      return value && typeof value === 'object' ? value : null;
    } catch (_) {
      return null;
    }
  }

  function rememberConnected(name, endpointId, direction = 'access-other-device') {
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
        direction,
        color: direction === 'access-to-my-device' ? 'red' : 'green',
        permissions: {
          photos: false,
          videos: false,
          documents: false,
          files: false,
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
      device.direction = direction;
      device.color = direction === 'access-to-my-device' ? 'red' : 'green';
      device.updatedAt = Date.now();
    }

    saveDevices(devices);
    return device;
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
        : state.pendingPairing
          ? 'Looking for the scanned device nearby…'
          : 'Searching for nearby Indoone devices…';
    }
  }

  function beginNearby(mode, targetType) {
    state.endpoints.clear();
    state.pendingPairing = null;
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

  window.IndooneConnectNative = window.IndooneConnectNative || {};
  window.IndooneConnectNative.pairWithCode = function (code) {
    const pending = readPendingDevice() || {};
    const pendingCode = String(code || '').replace(/\D/g, '');

    if (!/^\d{5}$/.test(pendingCode)) {
      notify('Invalid pairing code.');
      return false;
    }

    state.endpoints.clear();
    state.pendingPairing = {
      code: pendingCode,
      name: String(pending.name || ''),
      type: pending.type === 'computer' ? 'computer' : 'phone'
    };
    state.targetType = state.pendingPairing.type;
    state.pendingMode = 'discover';

    try {
      native.requestNearbyPermissions?.();
    } catch (_) {
      notify('Nearby permissions are required to connect this device.');
      return false;
    }

    return true;
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
      state.endpointDirections.set(endpointId, 'access-other-device');
      native.connectNearbyEndpoint(endpointId, deviceName());
      notify(`Connection request sent to ${item.name}.`);
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
    state.pendingPairing = null;
    state.endpoints.clear();
    state.endpointDirections.clear();
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
        state.pendingPairing = null;
        notify('Nearby permissions are required.');
      }
      return;
    }

    if (detail.type === 'endpointFound') {
      const endpointId = detail.endpointId;
      if (!endpointId) return;

      const rawName = detail.message || 'Nearby Indoone Device';
      const codeMatch = rawName.match(/\[(\d{5})\]\s*$/);
      const discoveredCode = codeMatch?.[1] || '';
      const displayName = rawName.replace(/\s*\[\d{5}\]\s*$/, '').trim() || rawName;
      const item = {
        endpointId,
        name: displayName,
        rawName,
        pairingCode: discoveredCode
      };
      state.endpoints.set(endpointId, item);

      if (
        state.pendingPairing &&
        state.pendingPairing.code &&
        discoveredCode === state.pendingPairing.code
      ) {
        state.activeEndpointId = endpointId;
        state.endpointDirections.set(endpointId, 'access-other-device');
        native.connectNearbyEndpoint(endpointId, deviceName());
        state.pendingPairing = null;
        notify(`Found ${displayName}. Connecting nearby…`);
      }

      const list = document.getElementById('nearbyDiscoveryList');
      if (!list) return;

      list.innerHTML = [...state.endpoints.values()].map(found => `
        <div class="device-card connected">
          <div class="device-icon">${state.targetType === 'computer' ? '💻' : '📱'}</div>
          <div>
            <b>${escapeHtml(found.name)}</b>
            <small>Nearby • ${
              state.pendingPairing
                ? (found.pairingCode === state.pendingPairing.code ? 'Matched scanned device' : 'Waiting for match')
                : 'Ready to connect'
            }</small>
          </div>
          <span class="device-dot"></span>
        </div>
      `).join('');
      return;
    }

    if (detail.type === 'endpointLost') {
      state.endpoints.delete(detail.endpointId);
      const list = document.getElementById('nearbyDiscoveryList');

      if (list) {
        list.innerHTML = state.endpoints.size
          ? [...state.endpoints.values()].map(item => `
              <div class="device-card connected">
                <div class="device-icon">📱</div>
                <div>
                  <b>${escapeHtml(item.name)}</b>
                  <small>Nearby • Ready to connect</small>
                </div>
                <span class="device-dot"></span>
              </div>
            `).join('')
          : '<div class="connect-empty">No nearby Indoone devices found.</div>';
      }
      return;
    }

    if (detail.type === 'connectionInitiated') {
      if (detail.incoming) {
        state.endpointDirections.set(detail.endpointId, 'access-to-my-device');
        native.acceptNearbyConnection?.(detail.endpointId);
        notify(`Connected request from ${detail.message || 'nearby device'} accepted.`);
      } else {
        state.endpointDirections.set(detail.endpointId, 'access-other-device');
        notify(`Connecting to ${detail.message || 'the device'}…`);
      }
      return;
    }

    if (detail.type === 'connectionResult') {
      if (detail.message === 'connected') {
        const item = state.endpoints.get(detail.endpointId);
        const name = item?.name || 'Nearby device';
        const direction = state.endpointDirections.get(detail.endpointId) || 'access-other-device';

        rememberConnected(name, detail.endpointId, direction);
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
              <b>${direction === 'access-to-my-device' ? 'This device will receive data access permission next.' : 'Your data access permission will be selected next.'}</b>
            </div>
            <button type="button" class="primary" data-close>Done</button>
          `
        );
      } else {
        notify('Nearby connection could not be completed.');
      }
      return;
    }

    if (detail.type === 'disconnected') {
      updateDeviceConnection(detail.endpointId, false);
      state.activeEndpointId = '';
      state.endpointDirections.delete(detail.endpointId);
      notify('Nearby device disconnected.');
      return;
    }

    if (detail.type === 'error') {
      notify(detail.message || 'Nearby connection error.');
    }
  });
})();
