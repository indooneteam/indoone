(() => {
  const MARKUP_URL = 'app/home/add-account/qr/index.html?v=20260903b';
  const STYLE_URL = 'app/home/add-account/qr/style.css?v=20260903b';

  let stream = null;
  let detector = null;
  let video = null;
  let animationFrame = 0;
  let permissionWaiter = null;
  let active = false;

  function loadStyle() {
    const baseUrl = STYLE_URL.split('?')[0];
    if (document.querySelector(`link[href^="${baseUrl}"]`)) return;
    const link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = STYLE_URL;
    document.head.appendChild(link);
  }

  async function loadMarkup() {
    const response = await fetch(MARKUP_URL, { cache: 'no-store' });
    if (!response.ok) throw new Error('QR page could not be loaded.');
    return response.text();
  }

  function setStatus(message) {
    const status = document.getElementById('addAccountQrStatus');
    if (status) status.textContent = message;
  }

  function requestCameraPermission() {
    if (!window.IndooneNative?.requestCameraPermission) {
      return Promise.resolve(true);
    }
    return new Promise(resolve => {
      permissionWaiter = resolve;
      window.IndooneNative.requestCameraPermission();
    });
  }

  function handleCameraPermission(event) {
    const resolve = permissionWaiter;
    permissionWaiter = null;
    resolve?.(Boolean(event.detail?.success));
  }

  function backToMenu() {
    stop();
    window.IndooneAddAccount?.showMenu?.({ push: true });
  }

  async function scanLoop() {
    if (!active || !video || !detector) return;

    try {
      if (video.readyState >= 2) {
        const codes = await detector.detect(video);
        const rawValue = codes?.[0]?.rawValue?.trim();

        if (rawValue?.toLowerCase().startsWith('otpauth://')) {
          const parsed = TOTP.parseOtpAuth(rawValue);
          if (!parsed.secret) throw new Error('Missing TOTP secret.');

          const zoho = window.IndooneZoho?.detect(
            parsed.issuer,
            parsed.label
          ) || null;

          stop();
          window.IndooneAddAccount?.showManual?.({
            push: true,
            prefill: {
              name: zoho?.service || parsed.issuer || parsed.label || 'Account',
              email: parsed.label || '',
              secret: parsed.secret,
              algorithm: parsed.algorithm,
              digits: parsed.digits,
              period: parsed.period,
              provider: zoho?.provider || '',
              service: zoho?.service || ''
            }
          });
          return;
        }
      }
    } catch (error) {
      setStatus(error?.message || 'Unable to read this QR code.');
    }

    animationFrame = requestAnimationFrame(scanLoop);
  }

  async function start() {
    setStatus('Checking camera…');

    if (!navigator.mediaDevices?.getUserMedia) {
      setStatus('Camera is not available on this device.');
      return;
    }

    if (!('BarcodeDetector' in window)) {
      setStatus('QR scanning is not supported in this browser.');
      return;
    }

    const permissionGranted = await requestCameraPermission();
    if (!permissionGranted) {
      setStatus('Camera permission is required to scan a QR code.');
      return;
    }

    video = document.getElementById('addAccountQrVideo');
    if (!video) return;

    try {
      detector = new BarcodeDetector({ formats: ['qr_code'] });
      stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: { ideal: 'environment' } },
        audio: false
      });
      video.srcObject = stream;
      await video.play();
      active = true;
      setStatus('Scanning for a TOTP QR code…');
      animationFrame = requestAnimationFrame(scanLoop);
    } catch (error) {
      stop();
      setStatus(
        error?.name === 'NotAllowedError'
          ? 'Camera permission was denied.'
          : 'Unable to open the camera.'
      );
    }
  }

  function stop() {
    active = false;
    if (animationFrame) cancelAnimationFrame(animationFrame);
    animationFrame = 0;
    if (stream) stream.getTracks().forEach(track => track.stop());
    stream = null;
    if (video) video.srcObject = null;
    video = null;
    detector = null;
    if (permissionWaiter) {
      permissionWaiter(false);
      permissionWaiter = null;
    }
  }

  function bind(root) {
    root.querySelectorAll('[data-qr-action]').forEach(button => {
      button.addEventListener('click', event => {
        event.preventDefault();
        const action = button.dataset.qrAction;
        if (action === 'back' || action === 'cancel') backToMenu();
      });
    });
  }

  async function render(mount) {
    loadStyle();
    stop();
    try {
      mount.innerHTML = await loadMarkup();
      bind(mount);
      await start();
    } catch (error) {
      mount.innerHTML = `
        <section class="add-account-child qr-page">
          <button type="button" class="page-back" data-qr-action="back">
            <span aria-hidden="true">‹</span>
            <span>Back</span>
          </button>
          <h1>Scan QR Code</h1>
          <p>Unable to load the QR scanner.</p>
        </section>
      `;
      bind(mount);
      console.error('Indoone Add Account QR feature failed:', error);
    }
  }

  document.addEventListener('indoone-camera-permission', handleCameraPermission);

  window.IndooneAddAccountQr = { render, stop };
})();
