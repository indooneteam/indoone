window.showAppLock = function (mode = 'unlock') {
  const hasVault = IndoonePersistence.hasVault();
  const title = mode === 'setup' ? 'Create App PIN' : (hasVault ? 'Unlock Indoone' : 'Create App PIN');
  openModal(`<div class="modal-head"><h2>${title}</h2><button class="close-btn" data-close>×</button></div><p>${hasVault && mode !== 'setup' ? 'Enter your PIN to unlock your encrypted vault.' : 'Your PIN protects the encrypted local authenticator vault.'}</p><div class="field"><label>PIN</label><input id="vaultPin" type="password" inputmode="numeric" maxlength="12" autocomplete="off" placeholder="4–12 digits"></div><button class="primary" id="vaultPinAction">${hasVault && mode !== 'setup' ? 'Unlock Vault' : 'Create Secure Vault'}</button>`);
  document.getElementById('vaultPinAction').addEventListener('click', async () => {
    const value = document.getElementById('vaultPin')?.value || '';
    if (!/^\d{4,12}$/.test(value)) return toast('PIN must be 4–12 digits');
    try {
      if (hasVault && mode !== 'setup') {
        const ok = await IndoonePersistence.unlock(value);
        if (!ok) return toast('Incorrect PIN');
        closeModal();
        renderAccounts();
        if (typeof startTOTPRefresh === 'function') startTOTPRefresh();
        toast('Vault unlocked');
      } else {
        IndooneSecureSession.unlock(value);
        await IndoonePersistence.save(indooneState.accounts, value);
        closeModal();
        toast('Encrypted vault created');
      }
    } catch (error) { toast(error?.message || 'Vault operation failed'); }
  });
};

window.lockIndoone = function () {
  IndoonePersistence.lock();
  openModal(`<div class="modal-head"><h2>Indoone Locked</h2></div><div class="token-icon">🔒</div><p style="text-align:center">Your accounts are locked and encrypted locally.</p><button class="primary" data-unlock>Unlock</button>`);
  modal.querySelector('[data-unlock]').addEventListener('click', () => showAppLock('unlock'));
};
