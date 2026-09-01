window.showBackup = function () {
  openModal(`<div class="modal-head"><h2>Recovery Center</h2><button class="close-btn" data-close>×</button></div>
  <div class="settings-row"><span>Encrypted Local Vault<small>Primary on-device account storage</small></span><b>${IndoonePersistence.hasVault()?'READY':'NOT SET'}</b></div>
  <div class="settings-row"><span>Android Auto Backup<small>Uses Android system backup when enabled</small></span><b>READY</b></div>
  <div class="settings-row"><span>Recovery PDF<small>${IndooneRecoveryPdf.isReady()?'Saved / ready':'Not created yet'}</small></span><b>${IndooneRecoveryPdf.isReady()?'READY':'SETUP'}</b></div>
  <button class="primary" data-create-recovery>Create / Refresh Recovery PDF</button>
  <button class="secondary" data-restore-recovery>Restore from Recovery PDF</button>
  <button class="secondary danger" data-delete-recovery>Delete Recovery PDF Marker</button>`);
};

window.createBackup = async function () {
  if (!IndoonePersistence.isUnlocked()) return toast('Unlock your vault first');
  const pin = IndooneSecureSession.getPin();
  if (!pin) return toast('Unlock your vault first');
  try { await IndooneRecoveryPdf.create(indooneState.accounts, pin); toast('Encrypted recovery PDF prepared'); }
  catch (e) { toast(e?.message || 'Recovery PDF failed'); }
};

window.importBackup = function () { document.getElementById('recoveryFileInput')?.click(); };

window.deleteRecoveryPdf = function () {
  const pin = prompt('Enter your PIN to remove the recovery marker');
  if (!pin) return;
  IndooneRecoveryPdf.verifyPin?.(pin).then(ok => {
    if (!ok) return toast('Incorrect PIN');
    IndooneRecoveryPdf.clearReady();
    toast('Recovery PDF marker removed');
    closeModal();
  }).catch(()=>toast('Incorrect PIN'));
};

window.restoreRecoveryPdf = function () { document.getElementById('recoveryFileInput')?.click(); };

const recoveryInput = document.getElementById('recoveryFileInput');
if (recoveryInput) recoveryInput.addEventListener('change', async () => {
  const file = recoveryInput.files?.[0]; if (!file) return;
  const pin = prompt('Enter the recovery PIN'); if (!pin) return;
  try {
    const accounts = await IndooneRecoveryPdf.restoreFromFile(file, pin);
    await IndoonePersistence.save(accounts, pin);
    IndooneSecureSession.unlock(pin);
    indooneState.accounts = accounts;
    renderAccounts();
    toast('Accounts restored successfully');
  } catch (_) { toast('Could not restore: wrong PIN or invalid recovery PDF'); }
  recoveryInput.value='';
});
