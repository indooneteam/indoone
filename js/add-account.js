window.showAdd = function () {
  openModal(`<div class="modal-head"><h2>Add Account</h2><button class="close-btn" data-close>×</button></div><div class="tabs"><button class="selected" data-tab="qr">Scan QR Code</button><button data-tab="manual">Enter Setup Key</button></div><div class="qr">▦</div><p style="text-align:center">Scan a TOTP QR code from your account security settings.</p><button class="primary" onclick="IndooneQrScanner.start()">Open Camera Scanner</button><div class="field"><label>OR PASTE OTPAUTH URI</label><input id="otpUri" placeholder="otpauth://totp/..." autocomplete="off"></div><button class="secondary" data-import-uri>Import OTP URI</button><button class="secondary" data-tab="manual">Enter Setup Key Instead</button>`);
};

window.importOtpUri = function () {
  const raw = document.getElementById('otpUri')?.value.trim();
  if (!raw) return toast('Enter an otpauth URI');
  try {
    const parsed = TOTP.parseOtpAuth(raw);
    showManual({name: parsed.issuer || parsed.label || 'Account', email: parsed.label || '', secret: parsed.secret, digits: parsed.digits, period: parsed.period, algorithm: parsed.algorithm});
    toast('OTP URI imported');
  } catch (_) { toast('Invalid TOTP QR data'); }
};
