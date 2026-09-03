(() => {
  let stream = null;
  let raf = 0;
  let detector = null;
  let scanning = false;

  const load = async () => {
    const response = await fetch(
      `app/connect/scanner/index.html?v=20260917a`,
      { cache: 'no-store' }
    );

    if (!response.ok) {
      throw new Error('Scanner could not be loaded.');
    }

    return response.text();
  };

  const stop = () => {
    scanning = false;

    if (raf) {
      cancelAnimationFrame(raf);
    }

    raf = 0;

    if (stream) {
      stream.getTracks().forEach(track => track.stop());
    }

    stream = null;
    detector = null;
  };

  const handle = raw => {
    const value = String(raw || '').trim();
    if (!value) {
      return false;
    }

    const status = document.getElementById('connectFeatureScanStatus');

    let code = '';

    if (value.toUpperCase().startsWith('INDOONE_CONNECT:')) {
      code = value.slice(16).trim();
    } else {
      try {
        const payload = JSON.parse(value);
        if (payload?.type === 'indoone-connect' && /^\d{5}$/.test(String(payload.code || ''))) {
          code = String(payload.code);
        }
      } catch (_) {
        // Not JSON; continue below.
      }
    }

    if (/^\d{5}$/.test(code)) {
      const input = document.getElementById('connectPairingCode');

      if (input) {
        input.value = code;
      }

      if (status) {
        status.textContent = 'Pairing code detected. Tap Connect.';
      }

      return true;
    }

    if (status) {
      status.textContent = 'QR detected. This code is not an Indoone pairing code.';
    }

    return false;
  };

  async function loop(video) {
    if (!scanning) {
      return;
    }

    try {
      if (video.readyState >= 2 && detector) {
        const codes = await detector.detect(video);
        const raw = codes?.[0]?.rawValue;

        if (raw && handle(raw)) {
          stop();
          return;
        }
      }
    } catch (_) {
      // Continue scanning after transient detector errors.
    }

    raf = requestAnimationFrame(() => loop(video));
  }

  async function camera() {
    const status = document.getElementById('connectFeatureScanStatus');
    const video = document.getElementById('connectFeatureScanVideo');

    if (!navigator.mediaDevices?.getUserMedia) {
      if (status) {
        status.textContent = 'Camera is not available on this device.';
      }
      return;
    }

    try {
      if (window.IndooneNative?.requestCameraPermission) {
        window.IndooneNative.requestCameraPermission();
      }

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

      if (status) {
        status.textContent = detector
          ? 'Camera ready. Point it at an Indoone QR.'
          : 'Camera is on, but QR decoding is not supported here.';
      }

      if (scanning) {
        loop(video);
      }
    } catch (error) {
      if (status) {
        status.textContent =
          error?.name === 'NotAllowedError'
            ? 'Camera permission denied.'
            : 'Unable to open camera.';
      }
    }
  }

  async function image(file) {
    if (!file) {
      return;
    }

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
      handle(codes?.[0]?.rawValue);

      URL.revokeObjectURL(image.src);
    } catch (_) {
      window.toast?.('Could not read that QR image.');
    }
  }

  window.showConnectScanner = async () => {
    stop();

    try {
      openModal(await load());

      const modal = document.getElementById('modal');

      modal?.querySelectorAll('[data-scanner-close]').forEach(button => {
        button.addEventListener('click', () => {
          stop();
          window.closeModal?.();
        });
      });

      modal?.querySelector('[data-connect-image]')?.addEventListener('click', () => {
        modal.querySelector('#connectFeatureImageInput')?.click();
      });

      modal?.querySelector('#connectFeatureImageInput')?.addEventListener(
        'change',
        event => image(event.target.files?.[0])
      );

      modal?.querySelector('[data-connect-code]')?.addEventListener('click', () => {
        const code = modal.querySelector('#connectPairingCode')?.value.trim();

        if (!/^\d{5}$/.test(code)) {
          window.toast?.('Enter the 5-digit pairing code.');
          return;
        }

        window.IndooneConnectNative?.pairWithCode?.(code);
        window.toast?.('Pairing code ready for nearby connection.');
      });

      await camera();
    } catch (error) {
      window.toast?.(error?.message || 'Could not open scanner.');
    }
  };
})();
