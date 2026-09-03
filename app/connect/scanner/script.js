(() => {
  let stream = null;
  let raf = 0;
  let detector = null;
  let scanning = false;
  let pairingCode = '';
  let pairingPermissionHandler = null;
  let pairingResultHandler = null;

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

  const stopPairingListeners = () => {
    if (pairingPermissionHandler) {
      window.removeEventListener('indoone-nearby', pairingPermissionHandler);
      pairingPermissionHandler = null;
    }

    if (pairingResultHandler) {
      window.removeEventListener('indoone-nearby', pairingResultHandler);
      pairingResultHandler = null;
    }
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

  const startPairing = code => {
    const normalized = String(code || '').replace(/\D/g, '');
    const status = document.getElementById('connectFeatureScanStatus');

    if (!/^\d{5}$/.test(normalized)) {
      if (status) {
        status.textContent = 'Enter a valid 5-digit pairing code.';
      }
      return false;
    }

    pairingCode = normalized;

    if (!window.IndooneNative?.startNearbyDiscovery) {
      if (status) {
        status.textContent = 'Pairing is available when Indoone Nearby is enabled.';
      }
      return false;
    }

    stopPairingListeners();

    pairingPermissionHandler = event => {
      const detail = event.detail || {};

      if (detail.type !== 'permissions') {
        return;
      }

      if (detail.message !== 'granted') {
        if (status) {
          status.textContent = 'Nearby permission is required for pairing.';
        }
        stopPairingListeners();
        return;
      }

      try {
        window.IndooneNative.startNearbyDiscovery();
        if (status) {
          status.textContent = `Searching for device with code ${pairingCode}…`;
        }
      } catch (_) {
        if (status) {
          status.textContent = 'Nearby connection is unavailable on this device.';
        }
        stopPairingListeners();
      }
    };

    pairingResultHandler = event => {
      const detail = event.detail || {};

      if (detail.type === 'endpointFound') {
        const name = String(detail.message || '');
        const match = name.match(/\[(\d{5})\]\s*$/);

        if (!match || match[1] !== pairingCode) {
          return;
        }

        try {
          window.IndooneNative.connectNearbyEndpoint(
            detail.endpointId,
            window.__indooneConnectDeviceName || 'Indoone Device'
          );
          if (status) {
            status.textContent = 'Pairing request sent. Waiting for approval…';
          }
        } catch (_) {
          if (status) {
            status.textContent = 'Could not start the pairing request.';
          }
        }
        return;
      }

      if (detail.type === 'connectionResult') {
        if (detail.message === 'connected') {
          if (status) {
            status.textContent = 'Connected successfully.';
          }
          stopPairingListeners();
          window.toast?.('Device connected successfully.');
        } else {
          if (status) {
            status.textContent = 'Pairing was rejected or could not be completed.';
          }
        }
        return;
      }

      if (detail.type === 'error') {
        if (status) {
          status.textContent = detail.message || 'Nearby connection error.';
        }
      }
    };

    window.addEventListener('indoone-nearby', pairingPermissionHandler);
    window.addEventListener('indoone-nearby', pairingResultHandler);

    try {
      window.IndooneNative.requestNearbyPermissions?.();
    } catch (_) {
      stopPairingListeners();
      if (status) {
        status.textContent = 'Nearby permission request failed.';
      }
      return false;
    }

    return true;
  };

  const handle = raw => {
    const value = String(raw || '').trim();
    if (!value) {
      return false;
    }

    const status = document.getElementById('connectFeatureScanStatus');
    let code = '';

    try {
      const payload = JSON.parse(value);
      if (
        payload?.type === 'indoone-connect' &&
        /^\d{5}$/.test(String(payload.code || ''))
      ) {
        code = String(payload.code);
      }
    } catch (_) {
      // Allow the legacy plain pairing payload as well.
    }

    if (!code && value.toUpperCase().startsWith('INDOONE_CONNECT:')) {
      code = value.slice(16).trim();
    }

    if (!/^\d{5}$/.test(code)) {
      if (status) {
        status.textContent = 'QR detected. This code is not an Indoone pairing code.';
      }
      return false;
    }

    const input = document.getElementById('connectPairingCode');
    if (input) {
      input.value = code;
    }

    if (status) {
      status.textContent = 'Pairing code detected. Starting connection…';
    }

    stop();
    startPairing(code);
    return true;
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
    stopPairingListeners();

    try {
      openModal(await load());

      const modal = document.getElementById('modal');

      modal?.querySelectorAll('[data-scanner-close]').forEach(button => {
        button.addEventListener('click', () => {
          stop();
          stopPairingListeners();
          window.IndooneNative?.stopNearby?.();
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

        startPairing(code);
      });

      await camera();
    } catch (error) {
      window.toast?.(error?.message || 'Could not open scanner.');
    }
  };
})();
