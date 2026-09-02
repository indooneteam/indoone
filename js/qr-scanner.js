window.IndooneQrScanner = (() => {
  let stream = null;
  let video = null;
  let detector = null;
  let raf = 0;
  let scanning = false;
  let permissionWaiter = null;

  function requestNativeCameraPermission() {
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

  async function start() {
    if (!navigator.mediaDevices?.getUserMedia) return toast('Camera is not available on this device');
    if (!('BarcodeDetector' in window)) return toast('QR scanning is not supported here');

    const permissionGranted = await requestNativeCameraPermission();
    if (!permissionGranted) return toast('Camera permission is required to scan a QR code');

    openModal(`<div class="modal-head"><h2>Scan QR Code</h2><button class="close-btn" data-stop-scan>×</button></div>
      <div class="scanner"><video id="qrVideo" autoplay playsinline muted></video><div class="scan-frame"></div></div>
      <p style="text-align:center">Place the TOTP QR code inside the frame.</p>
      <button class="secondary" data-stop-scan>Cancel</button>`);

    video = document.getElementById('qrVideo');
    detector = new BarcodeDetector({ formats: ['qr_code'] });
    try {
      stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: { ideal: 'environment' } }, audio: false });
      video.srcObject = stream;
      await video.play();
      scanning = true;
      scanLoop();
    } catch (error) {
      stop();
      toast(error?.name === 'NotAllowedError' ? 'Camera permission denied' : 'Unable to open camera');
    }
  }

  async function scanLoop() {
    if (!scanning || !video || !detector) return;
    try {
      if (video.readyState >= 2) {
        const codes = await detector.detect(video);
        const raw = codes?.[0]?.rawValue?.trim();
        if (raw?.toLowerCase().startsWith('otpauth://')) {
          const parsed = TOTP.parseOtpAuth(raw);
          if (!parsed.secret) throw new Error('Missing secret');
          const zoho = window.IndooneZoho?.detect(parsed.issuer, parsed.label) || null;
          stop();
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
          toast(zoho ? `Zoho ${zoho.service === 'Zoho Account' ? 'account' : 'service'} detected` : 'QR code detected');
          return;
        }
      }
    } catch (_) {}
    raf = requestAnimationFrame(scanLoop);
  }

  function stop() {
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

  return { start, stop };
})();
