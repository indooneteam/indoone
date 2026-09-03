window.showAddLegacy = function () {
  const content = document.getElementById('content');

  if (!content) {
    return;
  }

  closeModal?.();
  document.getElementById('addBtn')?.setAttribute('hidden', '');

  content.innerHTML = `
    <div class="add-account-page">
      <button
        type="button"
        class="page-back"
        onclick="showAccountsPage()"
      >
        ‹ Back
      </button>

      <div class="page-heading">
        <div>
          <p class="eyebrow">NEW AUTHENTICATOR</p>
          <h1>Add Account</h1>
          <p>Choose how you want to add your TOTP account.</p>
        </div>
      </div>

      <div class="add-account-options">
        <button
          type="button"
          class="add-account-option"
          onclick="IndooneQrScanner.start()"
        >
          <span>▦</span>
          <strong>Scan QR Code</strong>
          <small>Use your camera to scan a TOTP QR code.</small>
        </button>

        <button
          type="button"
          class="add-account-option"
          onclick="showManual()"
        >
          <span>⌨</span>
          <strong>Enter Setup Key</strong>
          <small>Enter the secret key and account details manually.</small>
        </button>
      </div>

      <div class="field add-account-import">
        <label for="otpUri">PASTE OTPAUTH URI</label>
        <input
          id="otpUri"
          placeholder="otpauth://totp/..."
          autocomplete="off"
        />
        <button
          type="button"
          class="secondary"
          data-import-uri
        >
          Import OTP URI
        </button>
      </div>
    </div>
  `;
};

window.showAccountsPage = function () {
  document.getElementById('addBtn')?.removeAttribute('hidden');
  document.getElementById('accountsNav')?.click();
};

window.importOtpUri = function () {
  const raw = document.getElementById('otpUri')?.value.trim();

  if (!raw) {
    toast('Enter an otpauth URI');
    return;
  }

  try {
    const parsed = TOTP.parseOtpAuth(raw);

    showManual({
      name: parsed.issuer || parsed.label || 'Account',
      email: parsed.label || '',
      secret: parsed.secret,
      digits: parsed.digits,
      period: parsed.period,
      algorithm: parsed.algorithm
    });

    toast('OTP URI imported');
  } catch (_) {
    toast('Invalid TOTP QR data');
  }
};
