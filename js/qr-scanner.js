window.IndooneQrScanner = (() => {
  let stream = null;
  let video = null;
  let detector = null;
  let timer = null;

  async function start() {
    if (!navigator.mediaDevices?.getUserMedia) {
      toast('Camera is not available in this browser');
      return;
    }
    if (!('BarcodeDetector' in window)) {
      toast('QR camera scanning needs a supported browser');
      return;
    }

    openModal(`<div class="modal-head"><h2>Scan QR Code</h2><button class="close-btn" data-stop-scan>×</button></div>
      <div class="scanner"><video id="qrVideo" autoplay playsinline muted></video><div class="scan-frame"></div></div>
      <p style="text-align:center">Align the QR code inside the frame.</p>
      <button class="secondary" data-stop-scan>Cancel</button>`);

    video = document.getElementById('qrVideo');
    detector = new BarcodeDetector({formats:['qr_code']});
    try {
      stream = await navigator.mediaDevices.getUserMedia({video:{facingMode:{ideal:'environment'}},audio:false});
      video.srcObject = stream;
      await video.play();
      timer = setInterval(scanFrame, 350);
    } catch (error) {
      stop();
      toast(error?.name === 'NotAllowedError' ? 'Camera permission denied' : 'Unable to open camera');
    }
  }

  async function scanFrame() {
    if (!video || video.readyState < 2) return;
    try {
      const codes = await detector.detect(video);
      const raw = codes?.[0]?.rawValue;
      if (!raw) return;
      const parsed = TOTP.parseOtpAuth(raw);
      if (!parsed.secret) throw new Error('QR code has no secret');
      stop();
      showManual({name: parsed.label || parsed.issuer || 'Account', email:'', secret:parsed.secret, issuer:parsed.issuer, algorithm:parsed.algorithm, digits:parsed.digits, period:parsed.period});
      toast('QR code detected');
    } catch (_) {
      // Ignore frames that do not contain a supported TOTP QR code.
    }
  }

  function stop() {
    if (timer) clearInterval(timer);
    timer = null;
    if (stream) stream.getTracks().forEach(track => track.stop());
    stream = null;
    video = null;
    detector = null;
  }

  return { start, stop };
})();
