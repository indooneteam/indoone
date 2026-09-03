(() => {
  const DEVICE_KEY = 'indoone_connect_devices_v1';

  function loadMarkup() {
    return fetch(
      `app/connect/devices/index.html?v=${Date.now()}`,
      { cache: 'no-store' }
    ).then(response => {
      if (!response.ok) {
        throw new Error('Devices could not be loaded.');
      }
      return response.text();
    });
  }

  function readDevices() {
    try {
      const value = JSON.parse(
        localStorage.getItem(DEVICE_KEY) || '[]'
      );
      return Array.isArray(value) ? value : [];
    } catch (_) {
      return [];
    }
  }

  function escapeHtml(value) {
    return String(value || '').replace(/[&<>\"']/g, char => ({
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      '\"': '&quot;',
      "'": '&#39;'
    }[char]));
  }

  function renderDevice(device) {
    const icon = device.type === 'computer' ? '💻' : '📱';
    const status = device.connected ? 'Connected' : 'Disconnected';
    const trusted = device.trusted ? ' • Trusted' : '';

    return `
      <button
        type="button"
        class="connected-device-row"
        data-device-name="${escapeHtml(device.name)}"
      >
        <span class="connected-device-icon">${icon}</span>
        <span>
          <span class="connected-device-name">
            ${escapeHtml(device.name)}
          </span>
          <span class="connected-device-status">
            ${status}${trusted}
          </span>
        </span>
        ${device.connected
          ? '<i class="connected-device-dot" aria-label="Connected"></i>'
          : ''}
      </button>
    `;
  }

  function renderList(list, devices) {
    if (!list) return;

    if (!devices.length) {
      list.innerHTML = `
        <div class="connected-device-empty">
          <strong>No connected devices yet</strong>
          <span>
            Connect a nearby Indoone device and it will appear here.
          </span>
        </div>
      `;
      return;
    }

    list.innerHTML = devices.map(renderDevice).join('');
  }

  window.showConnectDevices = async function () {
    try {
      const markup = await loadMarkup();
      window.openModal?.(markup);

      const modal = document.getElementById('modal');
      if (!modal) return;

      const list = modal.querySelector('#connectDevicesList');
      renderList(list, readDevices());

      modal.querySelectorAll('[data-devices-close]').forEach(button => {
        button.addEventListener('click', () => window.closeModal?.());
      });

      modal.querySelectorAll('[data-device-name]').forEach(button => {
        button.addEventListener('click', () => {
          window.openConnectedDevice?.(button.dataset.deviceName);
        });
      });
    } catch (error) {
      window.toast?.(error?.message || 'Could not open Devices.');
    }
  };
})();
