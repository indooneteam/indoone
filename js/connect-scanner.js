(() => {
  let stream = null;
  let video = null;
  let detector = null;
  let raf = 0;
  let scanning = false;
  let permissionWaiter = null;

  function escapeHtml(value) {
    return String(value || '').replace(/[&<>\"']/g, char => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', '\"':'&quot;', "'":'&#39;' }[char]));
  }

  function requestCameraPermission() {
    if (!window.IndooneNative?.requestCameraPermission) return Promise.resolve(true);
    return new Promise(resolve => {
      permissionWaiter = resolve;
      window.IndooneNative.requestCameraPermission();
    });
  }

  window.addEventListener('indoone-camera-permission', event => {
    const resolve = permissionWaiter;
    permissionWaiter = null;
    resolve?.(Boolean(event.detail?.success));
  });

  function stopScanner() {
    scanning = false;
    if (raf) cancelAnimationFrame(raf);
    raf = 0;
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

  window.stopConnectScanner = function () {
    stopScanner();
    if (typeof closeModal === 'function') closeModal();
  };

  function showScanResult(raw) {
    stopScanner();
    if (typeof closeModal === 'function') closeModal();

    const value = String(raw || '').trim();
    if (value.toLowerCase().startsWith('otpauth://')) {
      if (window.TOTP?.parseOtpAuth && typeof window.showManual === 'function') {
        const parsed = window.TOTP.parseOtpAuth(value);
        if (!parsed.secret) return toast('QR code does not contain a valid secret');
        const zoho = window.IndooneZoho?.detect(parsed.issuer, parsed.label) || null;
        showManual({
          name: zoho?.service || parsed.issuer || parsed.label || 'Account',
          email: parsed.label || '',
          secret: parsed.secret,
          algorithm: parsed.algorithm,
          digits: parsed.digits,
          period: parsed.period,
          provider: zoho?.provider || '',
          service: zoho?.service || ''
        });
        toast('QR code detected');
        return;
      }
    }

    if (value.toUpperCase().startsWith('INDOONE_CONNECT:')) {
      const payload = value.slice('INDOONE_CONNECT:'.length);
      toast(`Indoone pairing QR detected: ${payload || 'device'}`);
      return;
    }

    toast('QR detected, but it is not an Indoone pairing code.');
  }

  async function scanLoop() {
    if (!scanning || !video || !detector) return;
    try {
      if (video.readyState >= 2) {
        const codes = await detector.detect(video);
        const raw = codes?.[0]?.rawValue?.trim();
        if (raw) {
          showScanResult(raw);
          return;
        }
      }
    } catch (_) {}
    raf = requestAnimationFrame(scanLoop);
  }

  async function startCamera() {
    if (!navigator.mediaDevices?.getUserMedia) {
      toast('Camera is not available on this device');
      return;
    }
    const granted = await requestCameraPermission();
    if (!granted) {
      toast('Camera permission is required');
      return;
    }

    video = document.getElementById('connectScanVideo');
    if (!video) return;

    try {
      stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: { ideal: 'environment' } },
        audio: false
      });
      video.srcObject = stream;
      await video.play();
      scanning = true;

      if ('BarcodeDetector' in window) {
        detector = new BarcodeDetector({ formats: ['qr_code'] });
        scanLoop();
      } else {
        const status = document.getElementById('connectScanStatus');
        if (status) status.textContent = 'Camera is on. QR decoding is not supported in this browser.';
      }
    } catch (error) {
      stopScanner();
      toast(error?.name === 'NotAllowedError' ? 'Camera permission denied' : 'Unable to open camera');
    }
  }

  function scanImage(file) {
    if (!file) return;
    if (!('BarcodeDetector' in window)) return toast('Image QR scanning is not supported here');
    const image = new Image();
    image.onload = async () => {
      try {
        const imageDetector = new BarcodeDetector({ formats: ['qr_code'] });
        const codes = await imageDetector.detect(image);
        const raw = codes?.[0]?.rawValue?.trim();
        if (raw) showScanResult(raw);
        else toast('No QR code found in that image');
      } catch (_) {
        toast('Could not read that QR image');
      } finally {
        URL.revokeObjectURL(image.src);
      }
    };
    image.src = URL.createObjectURL(file);
  }

  window.showConnectScanner = function () {
    stopScanner();
    openModal(`<div class="modal-head">
        <h2>Scan to Connect</h2>
        <button type="button" class="close-btn" aria-label="Close scanner" onclick="event.preventDefault(); event.stopPropagation(); stopConnectScanner();">×</button>
      </div>
      <div class="connect-scanner" style="background:#000;position:relative">
        <video id="connectScanVideo" autoplay playsinline muted style="width:100%;height:100%;object-fit:cover;display:block"></video>
        <div class="connect-scan-corners"></div>
        <div style="position:absolute;left:50%;bottom:16px;transform:translateX(-50%);padding:7px 10px;border-radius:999px;background:#0009;color:#fff;font-size:10px;z-index:3">Point camera at an Indoone QR</div>
      </div>
      <p id="connectScanStatus" class="connect-muted center">Starting camera…</p>
      <input id="connectScanImageInput" type="file" accept="image/*" hidden>
      <button type="button" class="secondary" onclick="document.getElementById('connectScanImageInput')?.click()">Choose QR image</button>
      <button type="button" class="primary" onclick="event.preventDefault(); event.stopPropagation(); stopConnectScanner();">Cancel</button>`);

    const input = document.getElementById('connectScanImageInput');
    input?.addEventListener('change', event => scanImage(event.target.files?.[0]));
    startCamera();
  };
})();
