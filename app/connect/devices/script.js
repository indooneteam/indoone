(() => {
  const DEVICE_KEY = 'indoone_connect_devices_v1';

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

  const load = async () => {
    const response = await fetch(
      `app/connect/devices/index.html?v=20260917a`,
      { cache: 'no-store' }
    );

    if (!response.ok) {
      throw new Error('Devices could not be loaded.');
    }

    return response.text();
  };

  const read = () => {
    try {
      const devices = JSON.parse(
        localStorage.getItem(DEVICE_KEY) || '[]'
      );
      return Array.isArray(devices) ? devices : [];
    } catch (_) {
      return [];
    }
  };

  const renderDevices = devices => {
    if (!devices.length) {
      return `
        <div class="connected-device-empty">
          <strong>No connected devices yet</strong>
          <span>
            Connect a nearby Indoone device and it will appear here.
          </span>
        </div>
      `;
    }

    return devices
      .map(device => `
        <button
          type="button"
          class="connected-device-row"
          data-device-name="${escapeHtml(device.name)}"
        >
          <span class="connected-device-icon">
            ${device.type === 'computer' ? '💻' : '📱'}
          </span>
          <span>
            <span class="connected-device-name">
              ${escapeHtml(device.name)}
            </span>
            <span class="connected-device-status">
              ${device.connected ? 'Connected' : 'Disconnected'}${
                device.trusted ? ' • Trusted' : ''
              }
            </span>
          </span>
          ${
            device.connected
              ? '<i class="connected-device-dot" aria-label="Connected"></i>'
              : ''
          }
        </button>
      `)
      .join('');
  };

  window.showConnectDevices = async () => {
    try {
      openModal(await load());

      const modal = document.getElementById('modal');
      const list = modal?.querySelector('#connectDevicesList');
      const devices = read();

      if (list) {
        list.innerHTML = renderDevices(devices);
      }

      modal?.querySelectorAll('[data-devices-close]').forEach(button => {
        button.addEventListener('click', () => window.closeModal?.());
      });

      modal?.querySelectorAll('[data-device-name]').forEach(button => {
        button.addEventListener('click', () => {
          window.openConnectedDevice?.(button.dataset.deviceName);
        });
      });
    } catch (error) {
      window.toast?.(error?.message || 'Could not open Devices.');
    }
  };
})();
