(() => {
  let stream = null;
  let animationFrame = 0;
  let detector = null;
  let scanning = false;

  const MARKUP_URL = 'app/connect/scanner/scanner/index.html?v=20260903a';
  const PERMISSION_SCRIPT = 'app/connect/scanner/scanner/permission/script.js?v=20260903a';
  const PERMISSION_STYLE = 'app/connect/scanner/scanner/permission/style.css?v=20260903a';

  async function loadPermissionFeature() {
    if (!document.querySelector(`link[href^="${PERMISSION_STYLE}"]`)) {
      const link = document.createElement('link');
      link.rel = 'stylesheet';
      link.href = PERMISSION_STYLE;
      document.head.appendChild(link);
    }

    if (window.IndooneScannerPermission) return;

    await new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = PERMISSION_SCRIPT;
      script.onload = resolve;
      script.onerror = () => reject(new Error('Scanner permissions could not be loaded.'));
      document.head.appendChild(script);
    });
  }

  async function showPermissions() {
    await loadPermissionFeature();
    await window.IndooneScannerPermission?.show?.();
  }

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

  async function handle(raw) {
    const value = String(raw || '').trim();
    if (!value) return false;

    let code = '';

    if (value.toUpperCase().startsWith('INDOONE_CONNECT:')) {
      code = value.slice(16).trim();
    } else {
      try {
        const payload = JSON.parse(value);
        if (payload?.type === 'indoone-connect') code = String(payload.code || '');
      } catch (_) {
        // The scanned value was not JSON.
      }
    }

    if (!/^\d{5}$/.test(code)) return false;

    stop();

    const input = document.getElementById('connectPairingCode');
    if (input) input.value = code;

    window.IndooneConnectNative?.pairWithCode?.(code);
    const status = document.getElementById('connectFeatureScanStatus');
    if (status) status.textContent = 'Pairing started. Choose permissions.';

    try {
      await showPermissions();
    } catch (error) {
      window.toast?.(error?.message || 'Could not open permissions.');
    }

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

  window.showConnectScanner = async () => {
    stop();

    try {
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
  };
})();
