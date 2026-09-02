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

  function ensureDevice(name, type = 'phone') {
    const devices = loadDevices();
    let device = devices.find(item => item.name === name);
    if (!device) {
      device = {
        id: `${type}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
        name,
        type,
        connected: true,
        trusted: false,
        createdAt: Date.now(),
        permissions: defaultPermissions(),
      };
      devices.push(device);
      saveDevices(devices);
    }
    return device;
  }

  function escapeHtml(value) {
    return String(value || '').replace(/[&<>'"]/g, char => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', "'":'&#39;', '"':'&quot;' }[char]));
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
    return `<div class="connect-qr" aria-label="Indoone pairing code visual">${rows.map(row => [...row].map(cell => `<i class="${cell === '1' ? 'on' : ''}"></i>`).join('')).join('')}</div>`;
  }

  window.showConnectQr = function () {
    const name = localDeviceName();
    const code = pairingCode();
    openModal(`<div class="modal-head"><h2>My QR Code</h2><button class="close-btn" data-close>×</button></div>
      <div class="connect-qr-wrap">${makePairingVisual(code)}</div>
      <div class="connect-identity"><div class="connect-avatar">I</div><div><b>${escapeHtml(name)}</b><small>Pairing code: ${escapeHtml(code)}</small></div></div>
      <p class="connect-muted">This is the current device pairing identity. The nearby transport will be attached in the native Connect engine.</p>
      <button class="primary" data-close>Done</button>`);
  };

  window.showConnectScanner = function () {
    openModal(`<div class="modal-head"><h2>Scan to Connect</h2><button class="close-btn" data-close>×</button></div>
      <div class="connect-scanner"><div class="connect-scan-corners"></div><div class="connect-scan-icon">⌁</div></div>
      <p class="connect-muted center">Scanner UI is ready. Native camera/QR pairing will use this entry point.</p>
      <button class="secondary" data-connect-gallery>Choose QR image</button>
      <button class="primary" data-close>Cancel</button>`);
  };

  function showConnectionTarget(type) {
    const label = type === 'phone' ? 'Phone' : 'Laptop / PC';
    const icon = type === 'phone' ? '📱' : '💻';
    openModal(`<div class="modal-head"><h2>Connect ${label}</h2><button class="close-btn" data-close>×</button></div>
      <div class="device-summary"><div class="device-icon large">${icon}</div><div><b>Ready to pair</b><small>Nearby ${type === 'phone' ? 'phone-to-phone' : 'phone-to-computer'} connection</small></div></div>
      <div class="connect-empty" style="margin-top:14px">Bring the other device nearby and use its Indoone QR code. Connection and data access will be requested separately.</div>
      <button class="primary" onclick="showConnectQr()">Show My QR</button>
      <button class="secondary" data-connect-action="scanner">Scan Device QR</button>`);
  }

  window.showConnectChoice = function () {
    closeModal();
    $('connectChoicePage')?.removeAttribute('hidden');
    $('connectHomePage')?.setAttribute('hidden', '');
  };

  window.showConnectHome = function () {
    $('connectChoicePage')?.setAttribute('hidden', '');
    $('connectHomePage')?.removeAttribute('hidden', '');
  };

  function renderDevices() {
    const devices = loadDevices();
    if (!devices.length) {
      return '<div class="connect-empty">No trusted devices yet.<br>Connect a nearby device to see it here.</div>';
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
    const device = getDevice(name) || ensureDevice(name);
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
    const device = getDevice(name) || ensureDevice(name);
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

  function createLocalDevice(type) {
    const requestedName = type === 'phone' ? 'Nearby Phone' : 'Nearby Computer';
    const device = ensureDevice(requestedName, type);
    device.connected = true;
    saveDevices(loadDevices().map(item => item.id === device.id ? device : item));
    toast(`${type === 'phone' ? 'Phone' : 'Laptop / PC'} connection placeholder is ready.`);
    window.openConnectedDevice(device.name);
  }

  function handleDataAction(action) {
    const labels = { files: 'Shared files', media: 'Photos & videos', send: 'Send files' };
    toast(`${labels[action] || 'Connect'} will use the native transfer engine after pairing.`);
  }

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
