(() => {
  let stream = null;
  let animationFrame = 0;
  let detector = null;
  let scanning = false;

  const MARKUP_URL = 'app/connect/scanner/scanner/index.html?v=20260904a';
  const PENDING_DEVICE_KEY = 'indoone_connect_pending_device_v1';

  async function loadMarkup() {
    const response = await fetch(MARKUP_URL, { cache: 'no-store' });
    if (!response.ok) throw new Error('Scanner could not be loaded.');
    return response.text();
  }

  function stop() {
    scanning = false;
    if (animationFrame) cancelAnimationFrame(animationFrame);
    animationFrame = 0;
    if (stream) stream.getTracks().forEach(track => track.stop());
    stream = null;
    detector = null;
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
      timeoutId = window.setTimeout(() => finish(false), 60000);

      try {
        native.requestNearbyPermissions();
      } catch (_) {
        finish(false);
      }
    });
  }

  function rememberScannedDevice(value, code) {
    let name = 'Connected Indoone Device';
    let deviceType = 'phone';

    try {
      const payload = JSON.parse(value);
      if (payload?.type === 'indoone-connect') {
        name = String(payload.device || name);
        deviceType = /computer|laptop|pc/i.test(name) ? 'computer' : 'phone';
      }
    } catch (_) {
      // Scanned value was not a JSON pairing payload.
    }

    sessionStorage.setItem(PENDING_DEVICE_KEY, JSON.stringify({
      code,
      name,
      type: deviceType,
      discoveredAt: Date.now()
    }));
  }

  async function handle(raw) {
    const value = String(raw || '').trim();
    if (!value) return false;

    let code = /^\d{5}$/.test(value) ? value : '';

    if (!code && value.toUpperCase().startsWith('INDOONE_CONNECT:')) {
      code = value.slice(16).trim();
    }

    if (!code) {
      try {
        const payload = JSON.parse(value);
        if (payload?.type === 'indoone-connect') code = String(payload.code || '');
      } catch (_) {
        // The scanned value was not JSON.
      }
    }

    if (!/^\d{5}$/.test(code)) return false;

    stop();
    rememberScannedDevice(value, code);

    const input = document.getElementById('connectPairingCode');
    if (input) input.value = code;

    const paired = window.IndooneConnectNative?.pairWithCode?.(code);
    if (paired === false) {
      window.toast?.('Could not start Nearby connection.');
      return false;
    }

    window.toast?.('QR matched. Waiting for the real Nearby connection…');
    return true;
  }

  async function loop(video) {
    if (!scanning) return;

    try {
      if (video.readyState >= 2 && detector) {
        const codes = await detector.detect(video);
        if (codes?.[0]?.rawValue) await handle(codes[0].rawValue);
      }
    } catch (_) {
      // Continue scanning after transient camera errors.
    }

    if (scanning) animationFrame = requestAnimationFrame(() => loop(video));
  }

  async function startCamera() {
    const status = document.getElementById('connectFeatureScanStatus');
    const video = document.getElementById('connectFeatureScanVideo');

    if (!navigator.mediaDevices?.getUserMedia || !video) {
      if (status) status.textContent = 'Camera is not available on this device.';
      return;
    }

    try {
      window.IndooneNative?.requestCameraPermission?.();
      stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: { ideal: 'environment' } },
        audio: false
      });
      video.srcObject = stream;
      await video.play();

      if ('BarcodeDetector' in window) {
        try {
          detector = new BarcodeDetector({ formats: ['qr_code'] });
        } catch (_) {
          detector = null;
        }
      }

      scanning = Boolean(detector);
      if (status) status.textContent = detector
        ? 'Camera ready. Point it at an Indoone QR.'
        : 'Camera is on, but QR decoding is not supported here.';

      if (scanning) loop(video);
    } catch (error) {
      if (status) status.textContent = error?.name === 'NotAllowedError'
        ? 'Camera permission denied.'
        : 'Unable to open camera.';
    }
  }

  async function readImage(file) {
    if (!file || !('BarcodeDetector' in window)) {
      window.toast?.('QR image scanning is not supported here.');
      return;
    }

    try {
      const image = new Image();
      image.src = URL.createObjectURL(file);
      await image.decode();
      const imageDetector = new BarcodeDetector({ formats: ['qr_code'] });
      const codes = await imageDetector.detect(image);
      URL.revokeObjectURL(image.src);
      await handle(codes?.[0]?.rawValue);
    } catch (_) {
      window.toast?.('Could not read that QR image.');
    }
  }

  async function show() {
    stop();

    try {
      const ready = await ensureNearbyReady();
      if (!ready) {
        window.toast?.('Turn on Bluetooth and Wi-Fi, then allow Nearby devices access to use the scanner.');
        return;
      }

      window.openModal?.(await loadMarkup());
      const modal = document.getElementById('modal');
      if (!modal) return;

      modal.querySelectorAll('[data-scanner-close]').forEach(button => {
        button.addEventListener('click', () => {
          stop();
          window.closeModal?.();
        });
      });

      modal.querySelector('[data-connect-image]')?.addEventListener('click', () => {
        modal.querySelector('#connectFeatureImageInput')?.click();
      });

      modal.querySelector('#connectFeatureImageInput')?.addEventListener('change', event => {
        readImage(event.target.files?.[0]);
      });

      modal.querySelector('[data-connect-code]')?.addEventListener('click', async () => {
        const code = String(modal.querySelector('#connectPairingCode')?.value || '').replace(/\D/g, '');
        if (!/^\d{5}$/.test(code)) {
          window.toast?.('Enter the 5-digit pairing code.');
          return;
        }
        await handle(code);
      });

      await startCamera();
    } catch (error) {
      window.toast?.(error?.message || 'Could not open scanner.');
    }
  }

  window.IndooneNestedScanner = { show, stop };
})();
