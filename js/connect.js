(() => {
  const $ = id => document.getElementById(id);

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
    closeModal?.();
    closeDrawer?.();
    showConnectContent();
    setActive('connect');
  };

  function makeQrVisual() {
    const cells = [
      '1111111001011111111',
      '1000001010011000001',
      '1011101001111011101',
      '1011101110101011101',
      '1011101001101011101',
      '1000001010111000001',
      '1111111010101111111',
      '0000000011010000000',
      '1101011100111010111',
      '0010110111001101000',
      '1110001010110111101',
      '0101110101100010010',
      '1011001110011101011',
      '0000000010110101000',
      '1111111011001101011',
      '1000001010110110010',
      '1011101011011101111',
      '1011101000100010010',
      '1011101111001110111',
      '1000001001101001000',
      '1111111010111011111'
    ];
    return `<div class="connect-qr" aria-label="Indoone pairing QR code">${cells.map(row => [...row].map(cell => `<i class="${cell === '1' ? 'on' : ''}"></i>`).join('')).join('')}</div>`;
  }

  window.showConnectQr = function () {
    openModal(`<div class="modal-head"><h2>My QR Code</h2><button class="close-btn" data-close>×</button></div>
      <div class="connect-qr-wrap">${makeQrVisual()}</div>
      <div class="connect-identity"><div class="connect-avatar">I</div><div><b>Indoone Phone</b><small>Ready to connect nearby</small></div></div>
      <p class="connect-muted">Scan this QR code from another Indoone device to start a secure pairing request.</p>
      <button class="primary" data-close>Done</button>`);
  };

  window.showConnectScanner = function () {
    openModal(`<div class="modal-head"><h2>Scan to Connect</h2><button class="close-btn" data-close>×</button></div>
      <div class="connect-scanner"><div class="connect-scan-corners"></div><div class="connect-scan-icon">⌁</div></div>
      <p class="connect-muted center">Place the other device's Indoone QR inside the frame.</p>
      <button class="secondary" data-connect-gallery>Choose QR image</button>
      <button class="primary" data-close>Cancel</button>`);
  };

  window.showConnectChoice = function () {
    closeModal?.();
    $('connectChoicePage')?.removeAttribute('hidden');
    $('connectHomePage')?.setAttribute('hidden', '');
  };

  window.showConnectHome = function () {
    $('connectChoicePage')?.setAttribute('hidden', '');
    $('connectHomePage')?.removeAttribute('hidden');
  };

  window.showConnectDevices = function () {
    openModal(`<div class="modal-head"><h2>Devices</h2><button class="close-btn" data-close>×</button></div>
      <div class="device-card connected"><div class="device-icon">📱</div><div><b>Rahul's Phone</b><small>Connected • Nearby</small></div><span class="device-dot"></span></div>
      <div class="device-card connected"><div class="device-icon">💻</div><div><b>My Laptop</b><small>Connected • Nearby</small></div><span class="device-dot"></span></div>
      <div class="device-card"><div class="device-icon">📱</div><div><b>My Tablet</b><small>Not connected</small></div></div>
      <p class="connect-muted">Connected devices will appear here. Open a device to manage its access permissions.</p>
      <button class="primary" data-close>Done</button>`);
  };

  window.openConnectedDevice = function (name = "Rahul's Phone") {
    openModal(`<div class="modal-head"><h2>${name}</h2><button class="close-btn" data-close>×</button></div>
      <div class="device-summary"><div class="device-icon large">📱</div><div><b>Connected</b><small>Nearby • Trusted device</small></div></div>
      <div class="connect-grid">
        <button type="button" class="connect-tile"><strong>📁</strong><span>Shared files</span><small>Browse approved data</small></button>
        <button type="button" class="connect-tile"><strong>📸</strong><span>Photos & videos</span><small>View approved media</small></button>
        <button type="button" class="connect-tile"><strong>📤</strong><span>Send files</span><small>Transfer to device</small></button>
        <button type="button" class="connect-tile" onclick="showAccessSettings('${name.replace(/'/g, "\\'")}')"><strong>🔐</strong><span>Access settings</span><small>Manage permissions</small></button>
      </div>
      <button class="secondary" onclick="showAccessSettings('${name.replace(/'/g, "\\'")}')">Manage access permissions</button>
      <button class="primary" data-close>Close</button>`);
  };

  window.showAccessSettings = function (name = "Rahul's Phone") {
    openModal(`<div class="modal-head"><h2>Access Settings</h2><button class="close-btn" data-close>×</button></div>
      <div class="device-summary"><div class="device-icon large">📱</div><div><b>${name}</b><small>Pre-approved device</small></div></div>
      <div class="access-banner"><div><b>Always Allow</b><small>Use saved permissions for this trusted device.</small></div><button type="button" class="toggle on" aria-label="Always allow"></button></div>
      <div class="settings-row"><span>Photos<small>View shared photos</small></span><input type="checkbox" checked></div>
      <div class="settings-row"><span>Videos<small>View shared videos</small></span><input type="checkbox" checked></div>
      <div class="settings-row"><span>Documents<small>View shared documents</small></span><input type="checkbox"></div>
      <div class="settings-row"><span>Downloads<small>Allow file downloads</small></span><input type="checkbox" checked></div>
      <div class="settings-row"><span>Upload files<small>Allow this device to send files</small></span><input type="checkbox"></div>
      <div class="settings-row"><span>Delete files<small>Allow deletion of shared data</small></span><input type="checkbox"></div>
      <button class="primary" data-close>Save Permissions</button>`);
  };

  document.addEventListener('click', event => {
    const el = event.target.closest('[data-connect-action]');
    if (!el) return;
    const action = el.dataset.connectAction;
    if (action === 'qr') showConnectQr();
    if (action === 'scanner') showConnectScanner();
    if (action === 'connect') showConnectChoice();
    if (action === 'devices') showConnectDevices();
    if (action === 'home') showConnectHome();
  });

  $('connectNav')?.addEventListener('click', showConnect);
  $('accountsNav')?.addEventListener('click', () => { showMain(); setActive('accounts'); });
  $('settingsNav')?.addEventListener('click', () => { showMain(); setActive('settings'); });

  document.addEventListener('click', event => {
    const card = event.target.closest('[data-device-name]');
    if (card) openConnectedDevice(card.dataset.deviceName);
  });
})();
