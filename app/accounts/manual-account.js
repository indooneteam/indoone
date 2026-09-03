window.showManualLegacy = function (prefill = {}) {
  window.__indooneAccountPrefill = prefill || {};

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
        onclick="showAdd()"
      >
        ‹ Back
      </button>

      <p class="eyebrow">ACCOUNT DETAILS</p>
      <h1>${prefill.id ? 'Edit Account' : 'Enter Setup Key'}</h1>

      <div class="field">
        <label>ACCOUNT NAME</label>
        <input
          id="accountName"
          value="${prefill.name || ''}"
          placeholder="e.g. Google"
        >
      </div>

      <div class="field">
        <label>EMAIL / USERNAME</label>
        <input
          id="accountUser"
          value="${prefill.email || ''}"
          placeholder="you@example.com"
        >
      </div>

      <div class="field">
        <label>SECRET KEY</label>
        <input
          id="secretKey"
          value="${prefill.secret || ''}"
          placeholder="Base32 secret key"
          autocomplete="off"
        >
      </div>

      <div class="field">
        <label>DIGITS</label>
        <select id="accountDigits">
          <option ${Number(prefill.digits || 6) === 6 ? 'selected' : ''}>6</option>
          <option ${Number(prefill.digits || 6) === 8 ? 'selected' : ''}>8</option>
        </select>
      </div>

      <div class="field">
        <label>PERIOD</label>
        <select id="accountPeriod">
          <option ${Number(prefill.period || 30) === 30 ? 'selected' : ''}>30</option>
          <option ${Number(prefill.period || 30) === 60 ? 'selected' : ''}>60</option>
        </select>
      </div>

      <div class="field">
        <label>ALGORITHM</label>
        <select id="accountAlgorithm">
          <option ${String(prefill.algorithm || 'SHA1') === 'SHA1' ? 'selected' : ''}>SHA1</option>
          <option ${String(prefill.algorithm || '') === 'SHA256' ? 'selected' : ''}>SHA256</option>
          <option ${String(prefill.algorithm || '') === 'SHA512' ? 'selected' : ''}>SHA512</option>
        </select>
      </div>

      <button
        class="primary"
        data-save-account
        data-edit-id="${prefill.id || ''}"
        onclick="saveAccount()"
      >
        ${prefill.id ? 'Save Changes' : 'Save Account'}
      </button>
    </div>
  `;
};

window.showManual = function (prefill = {}) {
  if (window.IndooneAddAccount?.showManual) {
    return window.IndooneAddAccount.showManual({
      push: true,
      id: Number(prefill.id || 0),
      prefill: prefill || {}
    });
  }

  return window.showManualLegacy?.(prefill);
};

window.saveAccount = async function () {
  const name = document.getElementById('accountName')?.value.trim();
  const email = document.getElementById('accountUser')?.value.trim();
  const secret = document
    .getElementById('secretKey')?.value.trim()
    .replace(/[\s-]/g, '')
    .toUpperCase();
  const digits = Number(document.getElementById('accountDigits')?.value || 6);
  const period = Number(document.getElementById('accountPeriod')?.value || 30);
  const algorithm = document.getElementById('accountAlgorithm')?.value || 'SHA1';
  const editId = Number(
    document.querySelector('[data-save-account]')?.dataset.editId || 0
  );
  const prefill = window.__indooneAccountPrefill || {};

  if (!name || !secret) {
    return toast('Enter account name and secret key');
  }

  if (!window.IndooneFirebase?.auth?.currentUser) {
    return toast('Please login first');
  }

  if (!window.IndooneCloudAccounts) {
    return toast('Cloud storage is unavailable');
  }

  try {
    TOTP.base32ToBytes(secret);
    await TOTP.generate(
      secret,
      Math.floor(Date.now() / 1000 / period),
      digits,
      algorithm
    );
  } catch (_) {
    return toast('Invalid TOTP secret');
  }

  try {
    if (editId) {
      const account = indooneState.accounts.find(item => item.id === editId);

      if (!account) {
        return toast('Account not found');
      }

      Object.assign(account, {
        name,
        email,
        secret,
        digits,
        period,
        algorithm,
        updatedAt: Date.now()
      });

      await IndooneCloudAccounts.save(account);
      showAccountsPage();
      await IndooneTotpController.refreshAll();
      toast('Account updated and synced');
      return;
    }

    const zoho = window.IndooneZoho?.detect(
      prefill.provider || '',
      prefill.email || email
    ) || null;

    const account = {
      id: Date.now(),
      name,
      email,
      secret,
      digits,
      period,
      algorithm,
      provider: prefill.provider || zoho?.provider || '',
      service: prefill.service || zoho?.service || '',
      favorite: false,
      icon: name.charAt(0).toUpperCase(),
      cls: (prefill.provider === 'Zoho' || zoho) ? 'zoho' : 'google',
      updatedAt: Date.now(),
      seconds: period,
      code: '------'
    };

    await IndooneCloudAccounts.save(account);
    indooneState.accounts.push(account);
    showAccountsPage();
    await IndooneTotpController.refreshAll();
    toast('Account saved to Firebase');
  } catch (error) {
    toast(error?.message || 'Could not save account');
  }
};