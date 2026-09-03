(() => {
  const loadMarkup = async () => {
    const response = await fetch(
      `app/connect/qr/index.html?v=20260917a`,
      { cache: 'no-store' }
    );

    if (!response.ok) {
      throw new Error('QR feature could not be loaded.');
    }

    return response.text();
  };

  const deviceName = () => {
    const saved = localStorage.getItem('indoone_connect_device_name');
    if (saved) {
      return saved;
    }

    const uid = window.IndooneFirebase?.auth?.currentUser?.uid || '';
    const suffix = uid
      ? uid.slice(-4).toUpperCase()
      : Math.random().toString(36).slice(2, 6).toUpperCase();
    const type = /Mobi|Android/i.test(navigator.userAgent)
      ? 'Phone'
      : 'Computer';
    const name = `Indoone ${type} ${suffix}`;

    localStorage.setItem('indoone_connect_device_name', name);
    return name;
  };

  const pairingCode = () => {
    const existing = sessionStorage.getItem('indoone_connect_pairing_code');
    if (existing && /^\d{5}$/.test(existing)) {
      return existing;
    }

    const code = String(Math.floor(10000 + Math.random() * 90000));
    sessionStorage.setItem('indoone_connect_pairing_code', code);
    return code;
  };

  const pairingPayload = code => JSON.stringify({
    v: 1,
    type: 'indoone-connect',
    code,
    device: deviceName()
  });

  const visual = value => {
    const normalized = encodeURIComponent(value);
    const size = 29;
    const cells = [];
    let seed = 0;

    for (let index = 0; index < normalized.length; index += 1) {
      seed = (seed * 31 + normalized.charCodeAt(index)) >>> 0;
    }

    const finder = (x, y, ox, oy) => {
      if (x < ox || x >= ox + 7 || y < oy || y >= oy + 7) return false;
      const px = x - ox;
      const py = y - oy;
      return (
        px === 0 || px === 6 || py === 0 || py === 6 ||
        (px >= 2 && px <= 4 && py >= 2 && py <= 4)
      );
    };

    const reserved = (x, y) =>
      (x < 9 && y < 9) ||
      (x >= size - 8 && y < 9) ||
      (x < 9 && y >= size - 8);

    for (let y = 0; y < size; y += 1) {
      for (let x = 0; x < size; x += 1) {
        let on;

        if (reserved(x, y)) {
          on =
            finder(x, y, 1, 1) ||
            finder(x, y, size - 8, 1) ||
            finder(x, y, 1, size - 8);
        } else {
          const valueAt = normalized.charCodeAt((x * 7 + y * 11) % normalized.length) || 0;
          const mix = (seed ^ (x * 92821) ^ (y * 68917) ^ valueAt) >>> 0;
          on = (mix % 7) < 3;
        }

        cells.push(`<i class="connect-qr-cell ${on ? '' : 'off'}"></i>`);
      }
    }

    return `<div class="connect-qr-grid connect-qr-realistic" data-qr-payload="${normalized}">${cells.join('')}</div>`;
  };

  window.showConnectQr = async () => {
    try {
      const markup = await loadMarkup();
      openModal(markup);

      const modal = document.getElementById('modal');
      if (!modal) {
        return;
      }

      const name = deviceName();
      const code = pairingCode();
      const payload = pairingPayload(code);

      const visualNode = modal.querySelector('#connectQrVisual');
      if (visualNode) {
        visualNode.innerHTML = visual(payload);
      }

      modal.querySelector('#connectQrDeviceName').textContent = name;
      modal.querySelector('#connectQrCode').textContent = `Pairing code: ${code}`;
      modal.querySelector('#connectQrCodeLarge').textContent = code;

      modal.querySelectorAll('[data-connect-copy]').forEach(button => {
        button.addEventListener('click', async () => {
          try {
            await navigator.clipboard.writeText(code);
            window.toast?.('Pairing code copied.');
          } catch (_) {
            window.toast?.(code);
          }
        });
      });

      modal.querySelectorAll('[data-connect-close]').forEach(button => {
        button.addEventListener('click', () => window.closeModal?.());
      });
    } catch (error) {
      window.toast?.(error?.message || 'Could not open QR code.');
    }
  };
})();
