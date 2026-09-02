(() => {
  const native = window.IndooneNative;
  const DEVICE_KEY = 'indoone_connect_devices_v1';
  const state = { mode: null, endpoints: new Map(), pending: new Map(), activeEndpointId: '' };

  if (!native) return;

  const esc = value => String(value || '').replace(/[&<>'"]/g, c => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', "'":'&#39;', '"':'&quot;' }[c]));
  const getName = () => {
    const saved = localStorage.getItem('indoone_connect_device_name');
    if (saved) return saved;
    const uid = window.IndooneFirebase?.auth?.currentUser?.uid || Math.random().toString(36).slice(2);
    const name = `Indoone Phone ${uid.slice(-4).toUpperCase()}`;
    localStorage.setItem('indoone_connect_device_name', name);
    return name;
  };
  const load = () => { try { const v = JSON.parse(localStorage.getItem(DEVICE_KEY) || '[]'); return Array.isArray(v) ? v : []; } catch (_) { return []; } };
  const save = v => localStorage.setItem(DEVICE_KEY, JSON.stringify(v));
  const toastSafe = msg => { if (typeof toast === 'function') toast(msg); };

  function rememberConnected(name, endpointId) {
    const devices = load();
    let d = devices.find(x => x.endpointId === endpointId || x.name === name);
    if (!d) d = { id: `nearby-${Date.now()}`, name, type: /computer|laptop|pc/i.test(name) ? 'computer' : 'phone', permissions: { photos:true, videos:true, documents:false, downloads:true, upload:false, delete:false, alwaysAllow:false }, trusted:false };
    d.name = name; d.endpointId = endpointId; d.connected = true; d.updatedAt = Date.now();
    save([...devices.filter(x => x !== d && x.endpointId !== endpointId), d]);
  }

  function markDisconnected(endpointId) {
    const devices = load().map(d => d.endpointId === endpointId ? { ...d, connected:false, updatedAt:Date.now() } : d);
    save(devices);
  }

  function openNearbyModal(title, body) {
    if (typeof openModal !== 'function') return;
    openModal(`<div class="modal-head"><h2>${esc(title)}</h2><button class="close-btn" data-close>×</button></div>${body}`);
  }

  function renderDiscovery() {
    const rows = [...state.endpoints.values()].map(item => `
      <button type="button" class="device-card connected" data-nearby-endpoint="${esc(item.endpointId)}">
        <div class="device-icon">${/computer|laptop|pc/i.test(item.name) ? '💻' : '📱'}</div>
        <div><b>${esc(item.name)}</b><small>Nearby • Ready to connect</small></div><span class="device-dot"></span>
      </button>`).join('');
    const empty = '<div class="connect-empty" id="nearbyEmpty">No nearby Indoone devices found yet.<br>Keep the other device advertising and stay nearby.</div>';
    return `<div id="nearbyDiscoveryList">${rows || empty}</div><div class="connect-muted" id="nearbyStatus">${state.mode === 'advertising' ? 'Advertising this device…' : 'Scanning for nearby devices…'}</div><button class="secondary" data-nearby-stop>Stop</button>`;
  }

  function start(mode) {
    state.mode = mode;
    state.endpoints.clear();
    native.requestNearbyPermissions();
    openNearbyModal(mode === 'advertising' ? 'Waiting for connection' : 'Nearby devices', mode === 'advertising'
      ? `<div class="device-summary"><div class="device-icon large">📱</div><div><b>${esc(getName())}</b><small>Advertising for nearby Indoone devices</small></div></div><div class="connect-empty">On the other phone, open Connect and scan for nearby devices. A connection request will appear here for approval.</div><div class="connect-muted" id="nearbyStatus">Requesting Nearby permissions…</div><button class="secondary" data-nearby-stop>Stop</button>`
      : renderDiscovery());
    setTimeout(() => { try { mode === 'advertising' ? native.startNearbyAdvertising(getName()) : native.startNearbyDiscovery(); } catch (e) { toastSafe('Nearby connection is unavailable on this device.'); } }, 450);
  }

  function replaceChoice() {
    window.showConnectChoice = function () {
      closeModal?.();
      document.getElementById('connectChoicePage')?.removeAttribute('hidden');
      document.getElementById('connectHomePage')?.setAttribute('hidden','');
    };
    document.addEventListener('click', event => {
      const choice = event.target.closest('.connect-choice button');
      if (!choice) return;
      event.preventDefault(); event.stopImmediatePropagation();
      const isPhone = choice.querySelector('.choice-icon')?.textContent.includes('📱');
      openNearbyModal(isPhone ? 'Connect another phone' : 'Connect laptop / PC', `<div class="device-summary"><div class="device-icon large">${isPhone ? '📱' : '💻'}</div><div><b>Choose connection mode</b><small>Nearby direct connection</small></div></div><button class="primary" data-nearby-mode="discover">Find nearby device</button><button class="secondary" data-nearby-mode="advertise">Make this device discoverable</button>`);
    }, true);
  }

  document.addEventListener('click', event => {
    const mode = event.target.closest('[data-nearby-mode]');
    if (mode) { event.preventDefault(); event.stopPropagation(); start(mode.dataset.nearbyMode); return; }
    const endpoint = event.target.closest('[data-nearby-endpoint]');
    if (endpoint) { event.preventDefault(); event.stopPropagation(); const item = state.endpoints.get(endpoint.dataset.nearbyEndpoint); if (item) { state.activeEndpointId = item.endpointId; native.connectNearbyEndpoint(item.endpointId, getName()); toastSafe(`Connection request sent to ${item.name}.`); } return; }
    if (event.target.closest('[data-nearby-accept]')) { const id = event.target.closest('[data-nearby-accept]').dataset.nearbyAccept; native.acceptNearbyConnection(id); return; }
    if (event.target.closest('[data-nearby-reject]')) { const id = event.target.closest('[data-nearby-reject]').dataset.nearbyReject; native.rejectNearbyConnection(id); closeModal?.(); return; }
    if (event.target.closest('[data-nearby-stop]')) { native.stopNearby(); closeModal?.(); return; }
  });

  window.addEventListener('indoone-nearby', event => {
    const d = event.detail || {};
    if (d.type === 'permissions') {
      const el = document.getElementById('nearbyStatus');
      if (el) el.textContent = d.message === 'granted' ? 'Nearby permissions granted.' : 'Nearby permissions denied.';
      if (d.message !== 'granted') toastSafe('Nearby permissions are required.');
      return;
    }
    if (d.type === 'endpointFound') {
      state.endpoints.set(d.endpointId, { endpointId:d.endpointId, name:d.message });
      const list = document.getElementById('nearbyDiscoveryList');
      if (list) list.innerHTML = renderDiscovery().match(/<div id="nearbyDiscoveryList">([\s\S]*?)<\/div>/)?.[1] || '';
      return;
    }
    if (d.type === 'endpointLost') { state.endpoints.delete(d.message || d.endpointId); return; }
    if (d.type === 'requestSent') return;
    if (d.type === 'connectionInitiated') {
      const digits = d.authenticationDigits || '----';
      if (d.incoming) {
        openNearbyModal('Connection request', `<div class="device-summary"><div class="device-icon large">📱</div><div><b>${esc(d.message)}</b><small>Wants to connect to this device</small></div></div><div class="connect-empty"><b>Verification code: ${esc(digits)}</b><br>Check that the code matches on the other device before accepting.</div><button class="primary" data-nearby-accept="${esc(d.endpointId)}">Accept connection</button><button class="secondary" data-nearby-reject="${esc(d.endpointId)}">Reject</button>`);
      } else {
        openNearbyModal('Confirm connection', `<div class="device-summary"><div class="device-icon large">📱</div><div><b>${esc(d.message)}</b><small>Confirm the verification code</small></div></div><div class="connect-empty"><b>Verification code: ${esc(digits)}</b><br>Have the other device confirm the same code, then accept.</div><button class="secondary" data-close>Waiting for approval…</button>`);
      }
      return;
    }
    if (d.type === 'connectionResult') {
      if (d.message === 'connected') {
        state.activeEndpointId = d.endpointId;
        const name = state.endpoints.get(d.endpointId)?.name || 'Nearby device';
        rememberConnected(name, d.endpointId);
        toastSafe(`Connected to ${name}.`);
        openNearbyModal('Connected', `<div class="device-summary"><div class="device-icon large">📱</div><div><b>${esc(name)}</b><small>Connected securely • Nearby</small></div></div><div class="connect-empty">Connection is ready. Data access remains separate and requires the permissions you approve.</div><button class="primary" data-close>Done</button>`);
      } else toastSafe('Nearby connection was rejected.');
      return;
    }
    if (d.type === 'disconnected') { markDisconnected(d.endpointId); toastSafe('Nearby device disconnected.'); return; }
    if (d.type === 'error') toastSafe(`Nearby error: ${d.message}`);
  });

  replaceChoice();
})();
