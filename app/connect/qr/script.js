(() => {
  const loadMarkup = async () => {
    const response = await fetch(
      `app/connect/qr/index.html?v=${Date.now()}`,
      { cache: 'no-store' }
    );

    if (!response.ok) {
      throw new Error('QR feature could not be loaded.');
    }

    return response.text();
  };

  function deviceName() {
    const saved = localStorage.getItem('indoone_connect_device_name');
    if (saved) return saved;

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
  }

  function pairingCode() {
    const base = deviceName()
      .replace(/[^A-Z0-9]/gi, '')
      .toUpperCase()
      .slice(-8);
    const host = location.hostname
      .replace(/[^A-Z0-9]/gi, '')
      .slice(-4)
      .toUpperCase() || 'LOCAL';

    return `${base}-${host}`.slice(0, 16);
  }

  function createVisual(value) {
    let seed = 0;

    for (let index = 0; index < value.length; index += 1) {
      seed += value.charCodeAt(index) * (index + 3);
    }

    const cells = [];

    for (let y = 0; y < 21; y += 1) {
      for (let x = 0; x < 21; x += 1) {
        const finder =
          (x < 7 && y < 7) ||
          (x >= 14 && y < 7) ||
          (x < 7 && y >= 14);
        const on = finder
          ? ((x % 6 === 0 || y % 6 === 0) ||
            (x > 1 && x < 5 && y > 1 && y < 5))
          : ((seed + x * 17 + y * 31 + x * y * 7) % 11 < 5);

        cells.push(
          `<i class="connect-qr-cell ${on ? '' : 'off'}"></i>`
        );
      }
    }

    return `<div class="connect-qr-grid">${cells.join('')}</div>`;
  }

  async function copyCode(value) {
    try {
      await navigator.clipboard.writeText(value);
      window.toast?.('Pairing code copied.');
    } catch (_) {
      window.toast?.(value);
    }
  }

  window.showConnectQr = async function () {
    try {
      const markup = await loadMarkup();
      window.openModal?.(markup);

      const modal = document.getElementById('modal');
      if (!modal) return;

      const name = deviceName();
      const code = pairingCode();
      const visual = modal.querySelector('#connectQrVisual');
      const nameNode = modal.querySelector('#connectQrDeviceName');
      const smallCode = modal.querySelector('#connectQrCode');
      const largeCode = modal.querySelector('#connectQrCodeLarge');

      if (visual) visual.innerHTML = createVisual(code);
      if (nameNode) nameNode.textContent = name;
      if (smallCode) smallCode.textContent = `Pairing code: ${code}`;
      if (largeCode) largeCode.textContent = code;

      modal.querySelectorAll('[data-connect-copy]').forEach(button => {
        button.addEventListener('click', () => copyCode(code));
      });

      modal.querySelectorAll('[data-connect-close]').forEach(button => {
        button.addEventListener('click', () => window.closeModal?.());
      });
    } catch (error) {
      window.toast?.(error?.message || 'Could not open QR code.');
    }
  };
})();
