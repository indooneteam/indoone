(() => {
  const MARKUP_URL = 'app/connect/qr/index.html?v=20260917a';
  const QR_LIBRARY_URL = 'https://cdn.jsdelivr.net/npm/qrcode-generator@1.4.4/qrcode.js';

  let qrLibraryPromise = null;
  let pairingCode = '';
  let pairingPermissionHandler = null;

  const loadMarkup = async () => {
    const response = await fetch(MARKUP_URL, { cache: 'no-store' });

    if (!response.ok) {
      throw new Error('QR feature could not be loaded.');
    }

    return response.text();
  };

  const loadQrLibrary = () => {
    if (typeof window.qrcode === 'function') {
      return Promise.resolve(window.qrcode);
    }

    if (qrLibraryPromise) {
      return qrLibraryPromise;
    }

    qrLibraryPromise = new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = QR_LIBRARY_URL;
      script.async = true;

      script.onload = () => {
        if (typeof window.qrcode === 'function') {
          resolve(window.qrcode);
          return;
        }

        reject(new Error('QR generator could not be initialized.'));
      };

      script.onerror = () => {
        reject(new Error('QR generator could not be loaded.'));
      };

      document.head.appendChild(script);
    });

    return qrLibraryPromise;
  };

  const deviceName = () => {
    const saved = localStorage.getItem('indoone_connect_device_name');
    if (saved) {
      return saved;
    }

    const uid = window.IndooneFirebase?.auth?.currentUser?.uid || '';
    const suffix = uid
      ? uid.slice(-4).toUpperCase()
      : Math.random().toString(36).slice(2, 6).toUpperCase();
    const type = /Mobi|Android/i.test(navigator.userAgent)
      ? 'Phone'
      : 'Computer';
    const name = `Indoone ${type} ${suffix}`;

    localStorage.setItem('indoone_connect_device_name', name);
    return name;
  };

  const createPairingCode = () => {
    const code = String(Math.floor(10000 + Math.random() * 90000));
    sessionStorage.setItem('indoone_connect_pairing_code', code);
    return code;
  };

  const pairingPayload = code => JSON.stringify({
    v: 1,
    type: 'indoone-connect',
    code,
    device: deviceName()
  });

  async function renderQr(target, payload) {
    const qr = await loadQrLibrary();
    const generator = qr(0, 'M');
    generator.addData(payload);
    generator.make();
    target.innerHTML = generator.createSvgTag(5, 0);

    const svg = target.querySelector('svg');
    if (svg) {
      svg.setAttribute('role', 'img');
      svg.setAttribute('aria-label', 'Indoone pairing QR code');
      svg.setAttribute('focusable', 'false');
      svg.style.width = '100%';
      svg.style.height = '100%';
      svg.style.maxWidth = '320px';
      svg.style.display = 'block';
      svg.style.margin = '0 auto';
    }
  }

  function stopAdvertising() {
    if (pairingPermissionHandler) {
      window.removeEventListener('indoone-nearby', pairingPermissionHandler);
      pairingPermissionHandler = null;
    }

    window.IndooneNative?.stopNearby?.();
  }

  function startAdvertising(code) {
    const native = window.IndooneNative;

    if (!native?.startNearbyAdvertising) {
      return false;
    }

    stopAdvertising();

    const advertisedName = `${deviceName()} [${code}]`;

    pairingPermissionHandler = event => {
      const detail = event.detail || {};

      if (detail.type !== 'permissions') {
        return;
      }

      if (detail.message !== 'granted') {
        stopAdvertising();
        window.toast?.('Nearby permission is required for device pairing.');
        return;
      }

      try {
        native.startNearbyAdvertising(advertisedName);
      } catch (_) {
        window.toast?.('Nearby connection is unavailable on this device.');
      }
    };

    window.addEventListener('indoone-nearby', pairingPermissionHandler);

    try {
      native.requestNearbyPermissions?.();
    } catch (_) {
      stopAdvertising();
      return false;
    }

    return true;
  }

  window.showConnectQr = async () => {
    stopAdvertising();

    try {
      const markup = await loadMarkup();
      openModal(markup);

      const modal = document.getElementById('modal');
      if (!modal) {
        return;
      }

      const name = deviceName();
      pairingCode = createPairingCode();
      const payload = pairingPayload(pairingCode);
      const visualNode = modal.querySelector('#connectQrVisual');

      if (!visualNode) {
        return;
      }

      await renderQr(visualNode, payload);

      modal.querySelector('#connectQrDeviceName').textContent = name;
      modal.querySelector('#connectQrCode').textContent = `Pairing code: ${pairingCode}`;
      modal.querySelector('#connectQrCodeLarge').textContent = pairingCode;

      startAdvertising(pairingCode);

      modal.querySelectorAll('[data-connect-copy]').forEach(button => {
        button.addEventListener('click', async () => {
          try {
            await navigator.clipboard.writeText(pairingCode);
            window.toast?.('Pairing code copied.');
          } catch (_) {
            window.toast?.(pairingCode);
          }
        });
      });

      modal.querySelectorAll('[data-connect-close]').forEach(button => {
        button.addEventListener('click', () => {
          stopAdvertising();
          window.closeModal?.();
        });
      });
    } catch (error) {
      stopAdvertising();
      window.toast?.(error?.message || 'Could not open QR code.');
    }
  };
})();
