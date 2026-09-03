(() => {
  const MARKUP_URL = 'app/connect/qr/qr/index.html?v=20260903b';
  const QR_LIBRARY_URL = 'https://cdn.jsdelivr.net/npm/qrcode-generator@1.4.4/qrcode.js';

  let qrLibraryPromise = null;
  let pairingCode = '';
  let permissionHandler = null;

  async function loadMarkup() {
    const response = await fetch(MARKUP_URL, { cache: 'no-store' });
    if (!response.ok) throw new Error('QR feature could not be loaded.');
    return response.text();
  }

  function loadQrLibrary() {
    if (typeof window.qrcode === 'function') return Promise.resolve(window.qrcode);
    if (qrLibraryPromise) return qrLibraryPromise;

    qrLibraryPromise = new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = QR_LIBRARY_URL;
      script.async = true;
      script.onload = () => typeof window.qrcode === 'function'
        ? resolve(window.qrcode)
        : reject(new Error('QR generator could not be initialized.'));
      script.onerror = () => reject(new Error('QR generator could not be loaded.'));
      document.head.appendChild(script);
    });

    return qrLibraryPromise;
  }

  function deviceName() {
    const saved = localStorage.getItem('indoone_connect_device_name');
    if (saved) return saved;

    const uid = window.IndooneFirebase?.auth?.currentUser?.uid || '';
    const suffix = uid
      ? uid.slice(-4).toUpperCase()
      : Math.random().toString(36).slice(2, 6).toUpperCase();
    const type = /Mobi|Android/i.test(navigator.userAgent) ? 'Phone' : 'Computer';
    const name = `Indoone ${type} ${suffix}`;

    localStorage.setItem('indoone_connect_device_name', name);
    return name;
  }

  function createPairingCode() {
    const code = String(Math.floor(10000 + Math.random() * 90000));
    sessionStorage.setItem('indoone_connect_pairing_code', code);
    return code;
  }

  function stopAdvertising() {
    if (permissionHandler) {
      window.removeEventListener('indoone-nearby', permissionHandler);
      permissionHandler = null;
    }
    window.IndooneNative?.stopNearby?.();
  }

  function ensureNearbyReady() {
    const native = window.IndooneNative;
    if (!native?.requestNearbyPermissions) {
      return Promise.reject(new Error('Nearby device support is unavailable.'));
    }

    return new Promise(resolve => {
      let settled = false;
      let timeoutId = 0;

      const finish = ready => {
        if (settled) return;
        settled = true;
        if (timeoutId) window.clearTimeout(timeoutId);
        window.removeEventListener('indoone-nearby', handler);
        resolve(ready);
      };

      const handler = event => {
        const detail = event.detail || {};
        if (detail.type !== 'permissions') return;
        finish(detail.message === 'granted');
      };

      window.addEventListener('indoone-nearby', handler);
      timeoutId = window.setTimeout(() => finish(false), 15000);

      try {
        native.requestNearbyPermissions();
      } catch (_) {
        finish(false);
      }
    });
  }

  function startAdvertising(code) {
    const native = window.IndooneNative;
    if (!native?.startNearbyAdvertising) return;

    stopAdvertising();
    permissionHandler = event => {
      const detail = event.detail || {};
      if (detail.type !== 'permissions') return;

      if (detail.message !== 'granted') {
        stopAdvertising();
        window.toast?.('Nearby device permission is required for QR pairing.');
        return;
      }

      native.startNearbyAdvertising(`${deviceName()} [${code}]`);
    };

    window.addEventListener('indoone-nearby', permissionHandler);
    native.requestNearbyPermissions?.();
  }

  async function renderQr(target, value) {
    const qr = await loadQrLibrary();
    const generator = qr(0, 'M');
    generator.addData(value);
    generator.make();
    target.innerHTML = generator.createSvgTag(5, 0);

    const svg = target.querySelector('svg');
    if (svg) {
      svg.setAttribute('role', 'img');
      svg.setAttribute('aria-label', 'Indoone pairing QR code');
      svg.style.width = '100%';
      svg.style.height = '100%';
      svg.style.maxWidth = '320px';
      svg.style.display = 'block';
      svg.style.margin = '0 auto';
    }
  }

  async function show() {
    stopAdvertising();

    try {
      const ready = await ensureNearbyReady();
      if (!ready) {
        window.toast?.('Turn on Bluetooth and allow Nearby devices access to show the QR.');
        return;
      }

      window.openModal?.(await loadMarkup());
      const modal = document.getElementById('modal');
      if (!modal) return;

      pairingCode = createPairingCode();
      const payload = JSON.stringify({
        v: 1,
        type: 'indoone-connect',
        code: pairingCode,
        device: deviceName()
      });

      await renderQr(modal.querySelector('#connectQrVisual'), payload);
      modal.querySelector('#connectQrDeviceName').textContent = deviceName();
      modal.querySelector('#connectQrCode').textContent = `Pairing code: ${pairingCode}`;
      modal.querySelector('#connectQrCodeLarge').textContent = pairingCode;

      startAdvertising(pairingCode);

      modal.querySelector('[data-qr-copy]')?.addEventListener('click', async () => {
        try {
          await navigator.clipboard.writeText(pairingCode);
          window.toast?.('Pairing code copied.');
        } catch (_) {
          window.toast?.(pairingCode);
        }
      });

      modal.querySelectorAll('[data-qr-close]').forEach(button => {
        button.addEventListener('click', () => {
          stopAdvertising();
          window.closeModal?.();
        });
      });
    } catch (error) {
      stopAdvertising();
      window.toast?.(error?.message || 'Could not open QR code.');
    }
  }

  window.IndooneNestedQr = { show, stopAdvertising };
})();
