window.showManual = function (prefill = {}) {
  openModal(`<div class="modal-head"><h2>${prefill.id ? 'Edit Account' : 'Add Account'}</h2><button class="close-btn" data-close>×</button></div><div class="tabs"><button data-tab="qr">Scan QR Code</button><button class="selected" data-tab="manual">Enter Setup Key</button></div><div class="field"><label>ACCOUNT NAME</label><input id="accountName" value="${prefill.name || ''}" placeholder="e.g. Google"></div><div class="field"><label>EMAIL / USERNAME</label><input id="accountUser" value="${prefill.email || ''}" placeholder="you@example.com"></div><div class="field"><label>SECRET KEY</label><input id="secretKey" value="${prefill.secret || ''}" placeholder="Base32 secret key" autocomplete="off"></div><div class="field"><label>DIGITS</label><select id="accountDigits"><option ${Number(prefill.digits || 6) === 6 ? 'selected' : ''}>6</option><option ${Number(prefill.digits || 6) === 8 ? 'selected' : ''}>8</option></select></div><div class="field"><label>PERIOD</label><select id="accountPeriod"><option ${Number(prefill.period || 30) === 30 ? 'selected' : ''}>30</option><option ${Number(prefill.period || 30) === 60 ? 'selected' : ''}>60</option></select></div><div class="field"><label>ALGORITHM</label><select id="accountAlgorithm"><option ${String(prefill.algorithm || 'SHA1') === 'SHA1' ? 'selected' : ''}>SHA1</option><option ${String(prefill.algorithm || '') === 'SHA256' ? 'selected' : ''}>SHA256</option><option ${String(prefill.algorithm || '') === 'SHA512' ? 'selected' : ''}>SHA512</option></select></div><button class="primary" data-save-account data-edit-id="${prefill.id || ''}">${prefill.id ? 'Save Changes' : 'Save Account'}</button>`);
};

window.saveAccount = function () {
  const name = document.getElementById('accountName')?.value.trim();
  const email = document.getElementById('accountUser')?.value.trim();
  const secret = document.getElementById('secretKey')?.value.trim().replace(/[\s-]/g, '').toUpperCase();
  const digits = Number(document.getElementById('accountDigits')?.value || 6);
  const period = Number(document.getElementById('accountPeriod')?.value || 30);
  const algorithm = document.getElementById('accountAlgorithm')?.value || 'SHA1';
  const editId = Number(modal.querySelector('[data-save-account]')?.dataset.editId || 0);
  if (!name || !secret) return toast('Enter account name and secret key');
  try { TOTP.base32ToBytes(secret); } catch (_) { return toast('Invalid Base32 secret'); }
  if (editId) {
    const a = indooneState.accounts.find(x => x.id === editId);
    if (a) Object.assign(a, {name, email, secret, digits, period, algorithm});
    closeModal(); refreshAccountCodes(); toast('Account updated'); return;
  }
  indooneState.accounts.push({id:Date.now(),name,email,secret,digits,period,algorithm,favorite:false,icon:name.charAt(0).toUpperCase(),cls:'google'});
  closeModal(); refreshAccountCodes(); toast('Account added');
};
