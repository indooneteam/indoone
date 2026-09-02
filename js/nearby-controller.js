(() => {
  const native = window.IndooneNative;
  const endpoints = new Map();

  function showNearbyModal() {
    openModal(`<div class="modal-head"><h2>Nearby Devices</h2><button class="close-btn" data-close>×</button></div><div id="nearbyState" class="connect-empty">Ready to search for nearby Indoone devices.</div><div id="nearbyList" style="margin-top:12px"></div><button class="primary" data-nearby-start>Find Nearby Devices</button><button class="secondary" data-close>Close</button>`);
  }

  function render() {
    const list = document.getElementById('nearbyList');
    if (!list) return;
    if (!endpoints.size) { list.innerHTML = '<div class="connect-empty">Searching… Keep the other Indoone device nearby.</div>'; return; }
    list.innerHTML = [...endpoints.entries()].map(([id, name]) => `<button type="button" class="device-card connected" data-nearby-id="${escapeHtml(id)}" data-nearby-name="${escapeHtml(name)}"><div class="device-icon">📱</div><div><b>${escapeHtml(name)}</b><small>Found nearby • Tap to connect</small></div><span class="arrow">›</span></button>`).join('');
  }

  function escapeHtml(value) { return String(value || '').replace(/[&<>'"]/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[c])); }

  window.showNearbyDevices = showNearbyModal;

  window.addEventListener('indoone-nearby', event => {
    const detail = event.detail || {};
    if (detail.type === 'endpointFound') { endpoints.set(detail.endpointId, detail.message); render(); }
    else if (detail.type === 'endpointLost') { endpoints.delete(detail.message || detail.endpointId); render(); }
    else if (detail.type === 'discoveryStarted') { const state = document.getElementById('nearbyState'); if (state) state.textContent = 'Discovery active. Waiting for nearby devices…'; }
    else if (detail.type === 'advertisingStarted') { const state = document.getElementById('nearbyState'); if (state) state.textContent = 'This device is discoverable. Searching for other devices…'; }
    else if (detail.type === 'connectionInitiated') toast(`${detail.message} connection request received.`);
    else if (detail.type === 'connectionResult') { if (detail.message === 'connected') toast('Nearby device connected.'); else toast('Connection was not completed.'); }
    else if (detail.type === 'error') toast(`Nearby error: ${detail.message}`);
  });

  document.addEventListener('click', event => {
    if (event.target.closest('[data-nearby-open]')) { event.preventDefault(); showNearbyModal(); return; }
    if (event.target.closest('[data-nearby-start]')) {
      event.preventDefault(); endpoints.clear(); render();
      if (!native?.startNearbyAdvertising || !native?.startNearbyDiscovery) { toast('Use the Android app for nearby connection.'); return; }
      native.startNearbyAdvertising(localStorage.getItem('indoone_connect_device_name') || 'Indoone Device');
      native.startNearbyDiscovery();
      return;
    }
    const endpoint = event.target.closest('[data-nearby-id]');
    if (endpoint) {
      event.preventDefault();
      native?.connectNearbyEndpoint?.(endpoint.dataset.nearbyId, endpoint.dataset.nearbyName);
      toast(`Connecting to ${endpoint.dataset.nearbyName}…`);
    }
  });
})();
