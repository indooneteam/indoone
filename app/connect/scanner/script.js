(() => {
  let stream = null;
  let frame = 0;
  let detector = null;
  let scanning = false;

  async function loadMarkup() {
    const response = await fetch(
      `app/connect/scanner/index.html?v=${Date.now()}`,
      { cache: 'no-store' }
    );

    if (!response.ok) {
      throw new Error('Scanner could not be loaded.');
    }

    return response.text();
  }

  function stopScanner() {
    scanning = false;

    if (frame) {
      cancelAnimationFrame(frame);
      frame = 0;
    }

    if (stream) {
      stream.getTracks().forEach(track => track.stop());
      stream = null;
    }

    detector = null;
  }

  function handleQr(raw) {
    const value = String(raw || '').trim();
    if (!value) return;

    const status = document.getElementById('connectFeatureScanStatus');

    if (value.toUpperCase().startsWith('INDOONE_CONNECT:')) {
      const code = value.slice('INDOONE_CONNECT:'.length).trim();
      const input = document.getElementById('connectPairingCode');

      if (input) input.value = code;
      if (status) status.textContent = 'Pairing code detected. Tap Connect.';
      return;
    }

    if (status) {
      status.textContent = 'QR detected. This is not an Indoone pairing code.';
    }
  }

  async function scanLoop(video) {
    if (!scanning) return;

    try {
      if (video.readyState >= 2) {
        const codes = await detector.detect(video);
        const raw = codes?.[0]?.rawValue;

        if (raw) {
          handleQr(raw);
          stopScanner();
          return;
        }
      }
    } catch (_) {
      // Keep scanning after a transient detector error.
    }

    frame = requestAnimationFrame(() => scanLoop(video));
  }

  async function startCamera() {
    const status = document.getElementById('connectFeatureScanStatus');
    const video = document.getElementById('connectFeatureScanVideo');

    if (!video) return;

    if (!navigator.mediaDevices?.getUserMedia) {
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

      if (!('BarcodeDetector' in window)) {
        if (status) {
          status.textContent = 'Camera is on, but QR decoding is not supported here.';
        }
        return;
      }

      detector = new BarcodeDetector({ formats: ['qr_code'] });
      scanning = true;

      if (status) status.textContent = 'Camera ready. Point it at an Indoone QR.';
      scanLoop(video);
    } catch (error) {
      if (status) {
        status.textContent = error?.name === 'NotAllowedError'
          ? 'Camera permission denied.'
          : 'Unable to open camera.';
      }
    }
  }

  async function scanImage(file) {
    if (!file) return;

    if (!('BarcodeDetector' in window)) {
      window.toast?.('Image QR scanning is not supported here.');
      return;
    }

    try {
      const image = new Image();
      image.src = URL.createObjectURL(file);
      await image.decode();

      const imageDetector = new BarcodeDetector({ formats: ['qr_code'] });
      const codes = await imageDetector.detect(image);
      handleQr(codes?.[0]?.rawValue);
      URL.revokeObjectURL(image.src);
    } catch (_) {
      window.toast?.('Could not read that QR image.');
    }
  }

  window.showConnectScanner = async function () {
    stopScanner();

    try {
      const markup = await loadMarkup();
      window.openModal?.(markup);

      const modal = document.getElementById('modal');
      if (!modal) return;

      modal.querySelectorAll('[data-scanner-close]').forEach(button => {
        button.addEventListener('click', () => {
          stopScanner();
          window.closeModal?.();
        });
      });

      modal.querySelector('[data-connect-image]')?.addEventListener('click', () => {
        modal.querySelector('#connectFeatureImageInput')?.click();
      });

      modal.querySelector('#connectFeatureImageInput')?.addEventListener(
        'change',
        event => scanImage(event.target.files?.[0])
      );

      modal.querySelector('[data-connect-code]')?.addEventListener('click', () => {
        const code = modal.querySelector('#connectPairingCode')?.value.trim();

        if (!code) {
          window.toast?.('Enter a pairing code.');
          return;
        }

        window.dispatchEvent(new CustomEvent('indoone-connect-code', {
          detail: { code }
        }));

        const status = modal.querySelector('#connectFeatureScanStatus');
        if (status) status.textContent = 'Pairing code ready for nearby connection.';
        window.toast?.('Pairing code ready.');
      });

      await startCamera();
    } catch (error) {
      window.toast?.(error?.message || 'Could not open scanner.');
    }
  };
})();
