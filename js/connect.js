(() => {
  const DEVICE_KEY = 'indoone_connect_devices_v1';
  const $ = id => document.getElementById(id);

  const defaultPermissions = () => ({
    alwaysAllow: false,
    photos: true,
    videos: true,
    documents: false,
    downloads: true,
    upload: false,
    delete: false,
  });

  const nearby = {
    endpoints: new Map(),
    advertising: false,
    discovering: false,
    connectedEndpoint: null,
  };

  function nativeAvailable() {
    return !!window.IndooneNative;
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

  function getDevice(name) {
    return loadDevices().find(device => device.name === name) || null;
  }

  function upsertNearbyDevice(name, endpointId, type = 'phone', connected = false) {
    const cleanName = name || 'Nearby Indoone Device';
    const devices = loadDevices();
    let device = devices.find(item => item.endpointId === endpointId || item.name === cleanName);
    if (!device) {
      device = {
        id: `${type}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
        endpointId,
        name: cleanName,
        type,
        connected,
        trusted: false,
        createdAt: Date.now(),
        permissions: defaultPermissions(),
      };
      devices.push(device);
    } else {
      device.endpointId = endpointId || device.endpointId;
      device.name = cleanName;
      device.type = type;
      device.connected = connected;
      device.updatedAt = Date.now();
    }
    saveDevices(devices);
    return device;
  }

  function markEndpoint(endpointId, connected) {
    const devices = loadDevices();
    let changed = false;
    devices.forEach(device => {
      if (device.endpointId === endpointId) {
        device.connected = connected;
        device.updatedAt = Date.now();
        changed = true;
      }
    });
    if (changed) saveDevices(devices);
  }

  function escapeHtml(value) {
    return String(value || '').replace(/[&<>'\"]/g, char => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', "'":'&#39;', '\"':'&quot;' }[char]));
  }

  function setActive(tab) {
    $('accountsNav')?.classList.toggle('active', tab === 'accounts');
    $('lobbyNav')?.classList.toggle('active', tab === 'lobby');
    $('connectNav')?.classList.toggle('active', tab === 'connect');
    $('settingsNav')?.classList.toggle('active', tab === 'settings');
  }

  function showMain() {
    $('connectContent')?.setAttribute('hidden', '');
    $('content')?.removeAttribute('hidden');
    $('addBtn')?.removeAttribute('hidden');
    $('searchWrap')?.removeAttribute('hidden');
  }

  function showConnectContent() {
    $('content')?.setAttribute('hidden', '');
    $('addBtn')?.setAttribute('hidden', '');
    $('searchWrap')?.setAttribute('hidden', '');
    $('connectContent')?.removeAttribute('hidden');
  }

  window.showConnect = function () {
    closeModal();
    closeDrawer();
    showConnectContent();
    setActive('connect');
  };

  function deviceType() {
    return /Mobi|Android/i.test(navigator.userAgent) ? 'phone' : 'computer';
  }

  function localDeviceName() {
    const currentUser = window.IndooneFirebase?.auth?.currentUser;
    const saved = localStorage.getItem('indoone_connect_device_name');
    if (saved) return saved;
    const suffix = currentUser?.uid ? currentUser.uid.slice(-4).toUpperCase() : Math.random().toString(36).slice(2, 6).toUpperCase();
    const name = deviceType() === 'phone' ? `Indoone Phone ${suffix}` : `Indoone Computer ${suffix}`;
    localStorage.setItem('indoone_connect_device_name', name);
    return name;
  }

  function pairingCode() {
    const base = localDeviceName().replace(/[^A-Z0-9]/gi, '').toUpperCase().slice(-8);
    const seed = `${base}-${location.hostname.replace(/[^A-Z0-9]/gi, '').slice(-4).toUpperCase() || 'LOCAL'}`;
    return seed.slice(0, 16);
  }

  function makePairingVisual(code) {
    const bits = Array.from(code).reduce((sum, char, index) => sum + char.charCodeAt(0) * (index + 3), 0);
    const rows = [];
    for (let y = 0; y < 21; y += 1) {
      let row = '';
      for (let x = 0; x < 21; x += 1) {
        const finder = (x < 7 && y < 7) || (x >= 14 && y < 7) || (x < 7 && y >= 14);
        const noise = ((bits + x * 17 + y * 31 + x * y * 7) % 11) < 5;
        row += finder ? (((x % 6 === 0 || y % 6 === 0) || (x > 1 && x < 5 && y > 1 && y < 5)) ? '1' : '0') : (noise ? '1' : '0');
      }
      rows.push(row);
    }
    return `<div class="connect-qr-shell"><div class="connect-qr" aria-label="Indoone pairing code visual">${rows.map(row => [...row].map(cell => `<i class="${cell === '1' ? 'on' : ''}"></i>`).join('')).join('')}<span class="connect-qr-brand" aria-hidden="true">I</span></div></div>`;
  }

  window.showConnectQr = function () {
    const name = localDeviceName();
    const code = pairingCode();
    openModal(`<div class="modal-head"><h2>My QR Code</h2><button type="button" class="close-btn" data-close onclick="event.preventDefault(); closeModal();">×</button></div>
      <div class="connect-qr-wrap">${makePairingVisual(code)}</div>
      <div class="connect-identity"><div class="connect-avatar">I</div><div><b>${escapeHtml(name)}</b><small>Pairing code: ${escapeHtml(code)}</small></div></div>
      <p class="connect-muted">QR pairing UI is ready. Nearby discovery is active in the Android app.</p>
      <button type="button" class="primary" data-close onclick="event.preventDefault(); closeModal();">Done</button>`);
  };

  window.showConnectScanner = function () {
    openModal(`<div class="modal-head"><h2>Scan to Connect</h2><button type="button" class="close-btn" data-close>×</button></div>
      <div class="connect-scanner"><div class="connect-scan-corners"></div><div class="connect-scan-icon">⌁</div></div>
      <p class="connect-muted center">QR scanner will be connected to the native pairing flow next. Nearby discovery can already be tested now.</p>
      <button class="secondary" data-connect-gallery>Choose QR image</button>
      <button class="primary" data-close>Cancel</button>`);
  };

  function showNearbyStatus(title, detail, actionHtml = '') {
    openModal(`<div class="modal-head"><h2>${escapeHtml(title)}</h2><button class="close-btn" data-close>×</button></div>
      <div class="device-summary"><div class="device-icon large">📡</div><div><b>${escapeHtml(detail)}</b><small>Nearby Connect transport</small></div></div>
      <div id="nearbyEndpointList" class="nearby-endpoint-list"></div>
      <p id="nearbyStatusText" class="connect-muted">Starting nearby…</p>
      ${actionHtml}
      <button class="secondary" data-nearby-stop>Stop Nearby</button>`);
    renderNearbyEndpoints();
  }

  function renderNearbyEndpoints() {
    const host = $('nearbyEndpointList');
    if (!host) return;
    const entries = [...nearby.endpoints.entries()];
    if (!entries.length) {
      host.innerHTML = '<div class="connect-empty">No nearby Indoone devices found yet.<br>Keep the other phone close and make sure it is advertising.</div>';
      return;
    }
    host.innerHTML = entries.map(([endpointId, item]) => `<div class="device-card ${item.connected ? 'connected' : ''}" style="display:flex;align-items:center;text-align:left;gap:12px">
      <div class="device-icon">📱</div><div style="flex:1"><b>${escapeHtml(item.name)}</b><small>${item.connected ? 'Connected • Nearby' : 'Found • Nearby'}</small></div>
      <button type="button" class="primary nearby-connect-btn" data-nearby-endpoint="${escapeHtml(endpointId)}" data-nearby-name="${escapeHtml(item.name)}">${item.connected ? 'Connected' : 'Connect'}</button>
    </div>`).join('');
  }

  function setNearbyStatus(text) {
    const node = $('nearbyStatusText');
    if (node) node.textContent = text;
  }

  function requestNativePermissions() {
    if (!nativeAvailable() || typeof window.IndooneNative.requestNearbyPermissions !== 'function') return false;
    window.IndooneNative.requestNearbyPermissions();
    return true;
  }

  function startAdvertising() {
    if (!nativeAvailable()) {
      toast('Nearby Connect works in the Android app.');
      return;
    }
    showNearbyStatus('This Phone', 'Making this device discoverable');
    requestNativePermissions();
    window.IndooneNative.startNearbyAdvertising(localDeviceName());
    nearby.advertising = true;
    setNearbyStatus('Advertising started. Keep this screen open for the other phone to discover you.');
  }

  function startDiscovery() {
    if (!nativeAvailable()) {
      toast('Nearby Connect works in the Android app.');
      return;
    }
    showNearbyStatus('Nearby Devices', 'Searching for Indoone devices');
    requestNativePermissions();
    window.IndooneNative.startNearbyDiscovery();
    nearby.discovering = true;
    setNearbyStatus('Searching… Keep the other phone nearby and advertising.');
  }

  function stopNearby() {
    if (nativeAvailable() && typeof window.IndooneNative.stopNearby === 'function') window.IndooneNative.stopNearby();
    nearby.advertising = false;
    nearby.discovering = false;
    nearby.endpoints.clear();
    closeModal();
  }

  function showConnectionTarget(type) {
    const label = type === 'phone' ? 'Phone' : 'Laptop / PC';
    const icon = type === 'phone' ? '📱' : '💻';
    const targetHtml = type === 'phone'
      ? `<button class="primary" data-nearby-mode="advertise">Make My Phone Discoverable</button>
         <button class="secondary" data-nearby-mode="discover">Find Nearby Phone</button>`
      : `<button class="primary" data-nearby-mode="discover">Find Nearby Computer</button>`;
    openModal(`<div class="modal-head"><h2>Connect ${label}</h2><button class="close-btn" data-close>×</button></div>
      <div class="device-summary"><div class="device-icon large">${icon}</div><div><b>Nearby connection</b><small>Connection and data access are handled separately.</small></div></div>
      <div class="connect-empty" style="margin-top:14px">Choose how this device should participate in the nearby connection.</div>
      ${targetHtml}
      <button class="secondary" onclick="showConnectQr()">Show My QR</button>`);
  }

  window.showConnectChoice = function () {
    closeModal();
    $('connectChoicePage')?.removeAttribute('hidden');
    $('connectHomePage')?.setAttribute('hidden', '');
  };

  window.showConnectHome = function () {
    stopNearby();
    $('connectChoicePage')?.setAttribute('hidden', '');
    $('connectHomePage')?.removeAttribute('hidden', '');
  };

  function renderDevices() {
    const devices = loadDevices();
    if (!devices.length) {
      return '<div class="connect-empty">No connected devices yet.<br>Start a nearby connection to add one.</div>';
    }
    return devices.map(device => `<button type="button" class="device-card ${device.connected ? 'connected' : ''}" data-device-name="${escapeHtml(device.name)}"><div class="device-icon">${device.type === 'computer' ? '💻' : '📱'}</div><div><b>${escapeHtml(device.name)}</b><small>${device.connected ? 'Connected • Nearby' : 'Disconnected'}${device.trusted ? ' • Trusted' : ''}</small></div>${device.connected ? '<span class="device-dot"></span>' : ''}</button>`).join('');
  }

  window.showConnectDevices = function () {
    openModal(`<div class="modal-head"><h2>Devices</h2><button class="close-btn" data-close>×</button></div>
      <div id="connectDeviceList">${renderDevices()}</div>
      <p class="connect-muted">Only devices you explicitly connect appear here. Access permissions are stored per device.</p>
      <button class="primary" data-close>Done</button>`);
  };

  window.openConnectedDevice = function (name) {
    const device = getDevice(name);
    if (!device) return showConnectDevices();
    openModal(`<div class="modal-head"><h2>${escapeHtml(device.name)}</h2><button class="close-btn" data-close>×</button></div>
      <div class="device-summary"><div class="device-icon large">${device.type === 'computer' ? '💻' : '📱'}</div><div><b>${device.connected ? 'Connected' : 'Disconnected'}</b><small>Nearby • ${device.trusted ? 'Trusted device' : 'Not trusted yet'}</small></div></div>
      <div class="connect-grid">
        <button type="button" class="connect-tile" data-connect-data-action="files"><strong>📁</strong><span>Shared files</span><small>Browse approved data</small></button>
        <button type="button" class="connect-tile" data-connect-data-action="media"><strong>📸</strong><span>Photos & videos</span><small>View approved media</small></button>
        <button type="button" class="connect-tile" data-connect-data-action="send"><strong>📤</strong><span>Send files</span><small>Transfer to device</small></button>
        <button type="button" class="connect-tile" data-connect-access="${escapeHtml(device.name)}"><strong>🔐</strong><span>Access settings</span><small>Manage permissions</small></button>
      </div>
      <button class="secondary" data-connect-access="${escapeHtml(device.name)}">Manage access permissions</button>
      <button class="primary" data-close>Close</button>`);
  };

  window.showAccessSettings = function (name) {
    const device = getDevice(name);
    if (!device) return showConnectDevices();
    const p = { ...defaultPermissions(), ...(device.permissions || {}) };
    openModal(`<div class="modal-head"><h2>Access Settings</h2><button class="close-btn" data-close>×</button></div>
      <div class="device-summary"><div class="device-icon large">${device.type === 'computer' ? '💻' : '📱'}</div><div><b>${escapeHtml(device.name)}</b><small>${p.alwaysAllow ? 'Always Allow enabled' : 'Permission required'} </small></div></div>
      <div class="access-banner"><div><b>Always Allow</b><small>Use saved permissions for this trusted device.</small></div><button type="button" class="toggle ${p.alwaysAllow ? 'on' : ''}" data-connect-toggle-allow aria-label="Always allow"></button></div>
      ${[['photos','Photos','View shared photos'],['videos','Videos','View shared videos'],['documents','Documents','View shared documents'],['downloads','Downloads','Allow file downloads'],['upload','Upload files','Allow this device to send files'],['delete','Delete files','Allow deletion of shared data']].map(([key,title,desc]) => `<label class="settings-row"><span>${title}<small>${desc}</small></span><input type="checkbox" data-connect-permission="${key}" ${p[key] ? 'checked' : ''}></label>`).join('')}
      <button class="primary" data-save-permissions="${escapeHtml(device.name)}">Save Permissions</button>`);
  };

  function savePermissions(name) {
    const devices = loadDevices();
    const device = devices.find(item => item.name === name);
    if (!device) return;
    const permission = key => !!document.querySelector(`[data-connect-permission="${key}"]`)?.checked;
    device.permissions = {
      alwaysAllow: !!document.querySelector('[data-connect-toggle-allow]')?.classList.contains('on'),
      photos: permission('photos'),
      videos: permission('videos'),
      documents: permission('documents'),
      downloads: permission('downloads'),
      upload: permission('upload'),
      delete: permission('delete'),
    };
    device.trusted = device.permissions.alwaysAllow;
    device.updatedAt = Date.now();
    saveDevices(devices);
    toast('Permissions saved.');
    window.showConnectDevices();
  }

  function handleDataAction(action) {
    const labels = { files: 'Shared files', media: 'Photos & videos', send: 'Send files' };
    toast(`${labels[action] || 'Connect'} will use the native transfer engine after pairing.`);
  }

  function handleNearbyEvent(event) {
    const detail = event.detail || {};
    const type = detail.type;
    const message = detail.message || '';
    const endpointId = detail.endpointId || '';
    if (type === 'permissions') {
      toast(message === 'granted' ? 'Nearby permissions granted.' : 'Nearby permissions denied.');
      return;
    }
    if (type === 'advertisingStarted') {
      nearby.advertising = true;
      setNearbyStatus('Your phone is discoverable now.');
      return;
    }
    if (type === 'discoveryStarted') {
      nearby.discovering = true;
      setNearbyStatus('Searching for nearby Indoone devices…');
      return;
    }
    if (type === 'endpointFound') {
      const item = { name: message || 'Nearby Indoone Device', connected: false };
      nearby.endpoints.set(endpointId, item);
      renderNearbyEndpoints();
      setNearbyStatus('Nearby device found. Tap Connect to pair.');
      return;
    }
    if (type === 'endpointLost') {
      nearby.endpoints.delete(message || endpointId);
      renderNearbyEndpoints();
      return;
    }
    if (type === 'connectionInitiated') {
      const item = nearby.endpoints.get(endpointId) || { name: message || 'Nearby Indoone Device', connected: false };
      item.name = message || item.name;
      nearby.endpoints.set(endpointId, item);
      renderNearbyEndpoints();
      setNearbyStatus(`${item.name} is requesting a connection.`);
      return;
    }
    if (type === 'connectionResult') {
      const item = nearby.endpoints.get(endpointId);
      if (message === 'connected') {
        if (item) item.connected = true;
        const name = item?.name || 'Nearby Indoone Device';
        const device = upsertNearbyDevice(name, endpointId, deviceType() === 'computer' ? 'phone' : 'phone', true);
        nearby.connectedEndpoint = endpointId;
        renderNearbyEndpoints();
        setNearbyStatus(`${device.name} connected successfully.`);
        toast(`${device.name} connected.`);
      } else {
        setNearbyStatus('Connection was rejected or could not be completed.');
      }
      return;
    }
    if (type === 'disconnected') {
      markEndpoint(message || endpointId, false);
      nearby.connectedEndpoint = null;
      const item = nearby.endpoints.get(message || endpointId);
      if (item) item.connected = false;
      renderNearbyEndpoints();
      setNearbyStatus('Nearby device disconnected.');
      return;
    }
    if (type === 'error') {
      setNearbyStatus(message || 'Nearby connection error.');
      toast(message || 'Nearby connection error.');
    }
  }

  window.addEventListener('indoone-nearby', handleNearbyEvent);

  document.addEventListener('click', event => {
    const el = event.target.closest('[data-connect-action]');
    if (el) {
      event.preventDefault();
      event.stopPropagation();
      const action = el.dataset.connectAction;
      if (action === 'qr') showConnectQr();
      else if (action === 'scanner') showConnectScanner();
      else if (action === 'connect') showConnectChoice();
      else if (action === 'devices') showConnectDevices();
      else if (action === 'home') showConnectHome();
      return;
    }

    const choice = event.target.closest('.connect-choice button');
    if (choice) {
      event.preventDefault();
      event.stopPropagation();
      if (choice.querySelector('.choice-icon')?.textContent.includes('📱')) showConnectionTarget('phone');
      else showConnectionTarget('computer');
      return;
    }

    const nearbyMode = event.target.closest('[data-nearby-mode]');
    if (nearbyMode) {
      event.preventDefault();
      event.stopPropagation();
      if (nearbyMode.dataset.nearbyMode === 'advertise') startAdvertising();
      else startDiscovery();
      return;
    }

    const nearbyConnect = event.target.closest('[data-nearby-endpoint]');
    if (nearbyConnect) {
      event.preventDefault();
      event.stopPropagation();
      const endpointId = nearbyConnect.dataset.nearbyEndpoint;
      const name = nearbyConnect.dataset.nearbyName || 'Nearby Indoone Device';
      if (nearbyConnect.textContent.includes('Connected')) return;
      if (nativeAvailable()) {
        setNearbyStatus(`Connecting to ${name}…`);
        window.IndooneNative.connectNearbyEndpoint(endpointId, localDeviceName());
      } else {
        toast('Nearby Connect works in the Android app.');
      }
      return;
    }

    if (event.target.closest('[data-nearby-stop]')) {
      event.preventDefault();
      event.stopPropagation();
      stopNearby();
      return;
    }

    const access = event.target.closest('[data-connect-access]');
    if (access) {
      event.preventDefault();
      event.stopPropagation();
      window.showAccessSettings(access.dataset.connectAccess);
      return;
    }

    const dataAction = event.target.closest('[data-connect-data-action]');
    if (dataAction) {
      event.preventDefault();
      event.stopPropagation();
      handleDataAction(dataAction.dataset.connectDataAction);
      return;
    }

    if (event.target.closest('[data-connect-toggle-allow]')) {
      event.preventDefault();
      event.stopPropagation();
      event.target.closest('[data-connect-toggle-allow]').classList.toggle('on');
      return;
    }

    const save = event.target.closest('[data-save-permissions]');
    if (save) {
      event.preventDefault();
      event.stopPropagation();
      savePermissions(save.dataset.savePermissions);
      return;
    }

    const card = event.target.closest('[data-device-name]');
    if (card) {
      event.preventDefault();
      event.stopPropagation();
      openConnectedDevice(card.dataset.deviceName);
    }
  });

  $('connectNav')?.addEventListener('click', showConnect);
  $('accountsNav')?.addEventListener('click', () => { showMain(); setActive('accounts'); });
  $('settingsNav')?.addEventListener('click', () => { showMain(); setActive('settings'); });
})();
