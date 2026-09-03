(() => {
  const MARKUP_URL = 'app/connect/scanner/scanner/permission/index.html?v=20260903a';
  const PERMISSION_KEY = 'indoone_connect_pending_permissions_v1';
  const DEVICE_KEY = 'indoone_connect_devices_v1';
  const PENDING_DEVICE_KEY = 'indoone_connect_pending_device_v1';

  function readJson(key, fallback) {
    try {
      const value = JSON.parse(localStorage.getItem(key) || '');
      return value ?? fallback;
    } catch (_) {
      return fallback;
    }
  }

  function readPendingDevice() {
    try {
      const value = JSON.parse(sessionStorage.getItem(PENDING_DEVICE_KEY) || '');
      return value && typeof value === 'object' ? value : null;
    } catch (_) {
      return null;
    }
  }

  function saveDevice(permissions) {
    const pending = readPendingDevice() || {};
    const code = String(pending.code || '');
    const name = String(pending.name || 'Connected Indoone Device');
    const type = pending.type === 'computer' ? 'computer' : 'phone';
    const endpointId = code ? `pairing-${code}` : `scanner-${Date.now()}`;
    const devices = readJson(DEVICE_KEY, []);
    const list = Array.isArray(devices) ? devices : [];

    let device = list.find(item =>
      (code && item.pairingCode === code) ||
      item.endpointId === endpointId ||
      item.name === name
    );

    if (!device) {
      device = {
        id: `scanner-${Date.now()}`,
        name,
        endpointId,
        pairingCode: code,
        type,
        connected: true,
        trusted: false,
        direction: 'access-to-my-device',
        color: 'red',
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
      list.push(device);
    }

    device.name = name;
    device.endpointId = endpointId;
    device.pairingCode = code;
    device.type = type;
    device.connected = true;
    device.direction = 'access-to-my-device';
    device.color = 'red';
    device.permissions = {
      ...(device.permissions || {}),
      ...permissions
    };
    device.updatedAt = Date.now();

    localStorage.setItem(DEVICE_KEY, JSON.stringify(list));
    sessionStorage.removeItem(PENDING_DEVICE_KEY);

    window.dispatchEvent(new CustomEvent('indoone-device-saved', {
      detail: { device }
    }));

    return device;
  }

  async function show() {
    try {
      const response = await fetch(MARKUP_URL, { cache: 'no-store' });
      if (!response.ok) throw new Error('Permission page could not be loaded.');

      window.openModal?.(await response.text());

      const modal = document.getElementById('modal');
      if (!modal) return;

      modal.querySelector('[data-permission-close]')?.addEventListener('click', () => {
        window.closeModal?.();
      });

      const save = allowed => {
        const permissions = {
          photos: false,
          videos: false,
          documents: false,
          files: false
        };

        allowed.forEach(name => {
          permissions[name] = true;
        });

        const device = saveDevice(permissions);

        localStorage.setItem(PERMISSION_KEY, JSON.stringify({
          permissions,
          deviceId: device.id,
          savedAt: Date.now()
        }));

        window.dispatchEvent(new CustomEvent('indoone-permissions-saved', {
          detail: { permissions, device }
        }));

        const status = modal.querySelector('#scannerPermissionStatus');
        if (status) {
          status.textContent = allowed.length
            ? 'Selected access has been saved for this device.'
            : 'All file access is denied.';
        }

        window.toast?.(allowed.length ? 'Permissions saved.' : 'All permissions denied.');

        window.closeModal?.();
      };

      modal.querySelector('[data-permission-save]')?.addEventListener('click', event => {
        event.preventDefault();
        const allowed = [...modal.querySelectorAll('[data-permission]:checked')]
          .map(input => input.dataset.permission)
          .filter(Boolean);
        save(allowed);
      });

      modal.querySelector('[data-permission-deny]')?.addEventListener('click', event => {
        event.preventDefault();
        modal.querySelectorAll('[data-permission]').forEach(input => {
          input.checked = false;
        });
        save([]);
      });
    } catch (error) {
      window.toast?.(error?.message || 'Could not open permissions.');
    }
  }

  window.IndooneScannerPermission = { show };
})();
