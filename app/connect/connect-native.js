(() => {
  const native = window.IndooneNative;
  const DEVICE_KEY = 'indoone_connect_devices_v1';
  const PERMISSION_KEY = 'indoone_connect_pending_permissions_v1';

  const state = {
    mode: null,
    targetType: null,
    endpoints: new Map(),
    pendingMode: null,
    pendingPairing: null,
    activeEndpointId: '',
    endpointDirections: new Map(),
    incomingEndpoints: new Map(),
    connectedEndpoints: new Set()
  };

  if (!native) return;

  const escapeHtml = value =>
    String(value || '').replace(
      /[&<>'\"]/g,
      char => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        "'": '&#39;',
        '\"': '&quot;'
      }[char])
    );

  const deviceName = () => {
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
  };

  const loadDevices = () => {
    try {
      const value = JSON.parse(
        localStorage.getItem(DEVICE_KEY) || '[]'
      );
      return Array.isArray(value) ? value : [];
    } catch (_) {
      return [];
    }
  };

  const saveDevices = devices => {
    localStorage.setItem(
      DEVICE_KEY,
      JSON.stringify(devices)
    );
    window.dispatchEvent(
      new CustomEvent('indoone-devices-changed')
    );
  };

  const notify = message => window.toast?.(message);

  function openStatus(title, body) {
    if (typeof window.openModal !== 'function') return;

    window.openModal(
      `<div class="modal-head">
        <h2>
          ${escapeHtml(title)}
        </h2>
        <button
          type="button"
          class="close-btn"
          data-close
        >
          ×
        </button>
      </div>${body}`
    );
  }

  function rememberConnected(
    name,
    endpointId,
    direction = 'access-other-device'
  ) {
    const devices = loadDevices();
    let device = devices.find(
      item => item.endpointId === endpointId || item.name === name
    );

    if (!device) {
      device = {
        id: `nearby-${Date.now()}`,
        name,
        endpointId,
        type: /computer|laptop|pc/i.test(name)
          ? 'computer'
          : 'phone',
        connected: true,
        trusted: false,
        direction,
        color: direction === 'access-to-my-device'
          ? 'red'
          : 'green',
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
        remotePermissions: {
          photos: false,
          videos: false,
          documents: false,
          files: false
        },
        createdAt: Date.now()
      };

      devices.push(device);
    } else {
      device.name = name;
      device.endpointId = endpointId;
      device.connected = true;
      device.direction = direction;
      device.color = direction === 'access-to-my-device'
        ? 'red'
        : 'green';
      device.updatedAt = Date.now();
    }

    saveDevices(devices);
    return device;
  }

  function updateDeviceConnection(endpointId, connected) {
    const devices = loadDevices();
    const next = devices.map(device =>
      device.endpointId === endpointId
        ? {
            ...device,
            connected,
            updatedAt: Date.now()
          }
        : device
    );

    saveDevices(next);
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

    const advertiseBody = `
      <div class="device-summary">
        <div class="device-icon large">
          ${targetType === 'computer' ? '💻' : '📱'}
        </div>
        <div>
          <b>
            ${escapeHtml(deviceName())}
          </b>
          <small>
            Waiting for a nearby connection
          </small>
        </div>
      </div>
      <div class="connect-empty">
        Keep this device open. Another Indoone device can connect to it.
      </div>
      <div
        id="nearbyStatus"
        class="connect-muted"
      >
        Requesting Nearby permissions…
      </div>
      <button
        type="button"
        class="secondary"
        data-nearby-stop
      >
        Stop
      </button>
    `;

    const discoverBody = `
      <div id="nearbyDiscoveryList">
        <div class="connect-empty">
          Scanning for nearby Indoone devices…
        </div>
      </div>
      <div
        id="nearbyStatus"
        class="connect-muted"
      >
        Requesting Nearby permissions…
      </div>
      <button
        type="button"
        class="secondary"
        data-nearby-stop
      >
        Stop
      </button>
    `;

    openStatus(
      isAdvertise ? 'Waiting for connection' : 'Nearby devices',
      isAdvertise ? advertiseBody : discoverBody
    );

    native.requestNearbyPermissions?.();
  }

  window.startNearbyConnection = function (targetType = 'phone') {
    const label = targetType === 'computer'
      ? 'Laptop / PC'
      : 'Phone';
    const icon = targetType === 'computer'
      ? '💻'
      : '📱';

    openStatus(
      `Connect ${label}`,
      `
        <div class="device-summary">
          <div class="device-icon large">
            ${icon}
          </div>
          <div>
            <b>
              Nearby connection
            </b>
            <small>
              Connection and data access are separate.
            </small>
          </div>
        </div>
        <div class="connect-empty">
          Keep both devices nearby. One device searches while the other is discoverable.
        </div>
        <button
          type="button"
          class="primary"
          data-nearby-mode="discover"
        >
          Find nearby ${label}
        </button>
        <button
          type="button"
          class="secondary"
          data-nearby-mode="advertise"
        >
          Make this device discoverable
        </button>
        <button
          type="button"
          class="secondary"
          data-close
        >
          Cancel
        </button>
      `
    );
  };

  window.IndooneConnectNative =
    window.IndooneConnectNative || {};

  window.IndooneConnectNative.pairWithCode = function (code) {
    const pending = (() => {
      try {
        return JSON.parse(
          sessionStorage.getItem(
            'indoone_connect_pending_device_v1'
          ) || ''
        ) || {};
      } catch (_) {
        return {};
      }
    })();

    const pendingCode = String(code || '').replace(/\D/g, '');

    if (!/^\d{5}$/.test(pendingCode)) {
      notify('Invalid pairing code.');
      return false;
    }

    state.endpoints.clear();
    state.pendingPairing = {
      code: pendingCode,
      name: String(pending.name || ''),
      type: pending.type === 'computer'
        ? 'computer'
        : 'phone'
    };
    state.targetType = state.pendingPairing.type;
    state.pendingMode = 'discover';

    try {
      native.requestNearbyPermissions?.();
    } catch (_) {
      notify(
        'Nearby permissions are required to connect this device.'
      );
      return false;
    }

    return true;
  };

  document.addEventListener(
    'click',
    event => {
      const modeButton = event.target.closest(
        '[data-nearby-mode]'
      );

      if (modeButton) {
        event.preventDefault();
        event.stopPropagation();

        beginNearby(
          modeButton.dataset.nearbyMode === 'advertise'
            ? 'advertise'
            : 'discover',
          state.targetType || 'phone'
        );
        return;
      }

      const endpointButton = event.target.closest(
        '[data-nearby-endpoint]'
      );

      if (endpointButton) {
        event.preventDefault();
        event.stopPropagation();

        const endpointId =
          endpointButton.dataset.nearbyEndpoint;
        const item = state.endpoints.get(endpointId);
        if (!item) return;

        state.activeEndpointId = endpointId;
        state.endpointDirections.set(
          endpointId,
          'access-other-device'
        );
        native.connectNearbyEndpoint(
          endpointId,
          deviceName()
        );
        notify(
          `Connection request sent to ${item.name}.`
        );
      }

      const stopButton = event.target.closest(
        '[data-nearby-stop]'
      );

      if (stopButton) {
        event.preventDefault();
        native.stopNearby?.();
        window.closeModal?.();
      }
    },
    true
  );

  window.stopNearby = function () {
    native.stopNearby?.();
    state.mode = null;
    state.pendingMode = null;
    state.pendingPairing = null;
    state.endpoints.clear();
    state.endpointDirections.clear();
    state.incomingEndpoints.clear();
    state.connectedEndpoints.clear();
  };

  window.addEventListener('indoone-nearby', event => {
    const detail = event.detail || {};

    if (detail.type === 'permissions') {
      if (detail.message === 'granted') {
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

      const rawName =
        detail.message || 'Nearby Indoone Device';
      const codeMatch = rawName.match(
        /\[(\d{5})\]\s*$/
      );
      const discoveredCode = codeMatch?.[1] || '';
      const displayName = rawName
        .replace(/\s*\[\d{5}\]\s*$/, '')
        .trim() || rawName;

      state.endpoints.set(
        endpointId,
        {
          endpointId,
          name: displayName,
          rawName,
          pairingCode: discoveredCode
        }
      );

      if (
        state.pendingPairing?.code &&
        discoveredCode === state.pendingPairing.code
      ) {
        state.activeEndpointId = endpointId;
        state.endpointDirections.set(
          endpointId,
          'access-other-device'
        );
        native.connectNearbyEndpoint(
          endpointId,
          deviceName()
        );
        state.pendingPairing = null;
        notify(
          `Found ${displayName}. Connecting nearby…`
        );
      }
      return;
    }

    if (detail.type === 'endpointLost') {
      state.endpoints.delete(detail.endpointId);
      return;
    }

    if (detail.type === 'connectionInitiated') {
      const endpointId = detail.endpointId;
      if (!endpointId) return;

      if (detail.incoming) {
        state.endpointDirections.set(
          endpointId,
          'access-to-my-device'
        );
        state.incomingEndpoints.set(
          endpointId,
          detail.message || 'Nearby device'
        );
      } else {
        state.endpointDirections.set(
          endpointId,
          'access-other-device'
        );
      }

      // Nearby requires both sides to accept before
      // onConnectionResult reports success.
      // Accept on both incoming and outgoing sides;
      // the permission screen is still opened only after
      // the real connection succeeds.
      native.acceptNearbyConnection?.(endpointId);

      if (!detail.incoming) {
        notify(
          `Connecting to ${detail.message || 'the device'}…`
        );
      }
      return;
    }

    if (detail.type === 'connectionResult') {
      if (detail.message !== 'connected') {
        notify('Nearby connection could not be completed.');
        return;
      }

      const endpointId = detail.endpointId;
      if (
        !endpointId ||
        state.connectedEndpoints.has(endpointId)
      ) {
        return;
      }

      state.connectedEndpoints.add(endpointId);

      const incomingName =
        state.incomingEndpoints.get(endpointId);
      const item = state.endpoints.get(endpointId);
      const name =
        item?.name || incomingName || 'Nearby device';
      const direction =
        state.endpointDirections.get(endpointId) ||
        'access-other-device';

      rememberConnected(
        name,
        endpointId,
        direction
      );
      state.activeEndpointId = endpointId;
      window.closeModal?.();
      notify(`Connected to ${name}.`);

      // Permission UI opens from the bilateral
      // connection-result listener.
      return;
    }

    if (detail.type === 'payload') {
      try {
        const message = JSON.parse(
          detail.message || '{}'
        );
        if (message.type !== 'indoone-permissions') {
          return;
        }

        const devices = loadDevices();
        const device = devices.find(
          item => item.endpointId === detail.endpointId
        );
        if (!device) return;

        device.remotePermissions = {
          photos: Boolean(message.permissions?.photos),
          videos: Boolean(message.permissions?.videos),
          documents: Boolean(message.permissions?.documents),
          files: Boolean(message.permissions?.files)
        };
        device.updatedAt = Date.now();
        saveDevices(devices);
        notify(
          'The other device updated its data permissions.'
        );
      } catch (_) {}
      return;
    }

    if (detail.type === 'disconnected') {
      updateDeviceConnection(
        detail.endpointId,
        false
      );
      state.activeEndpointId = '';
      state.endpointDirections.delete(
        detail.endpointId
      );
      state.incomingEndpoints.delete(
        detail.endpointId
      );
      state.connectedEndpoints.delete(
        detail.endpointId
      );
      notify('Nearby device disconnected.');
      return;
    }

    if (detail.type === 'error') {
      notify(
        detail.message || 'Nearby connection error.'
      );
    }
  });
})();
