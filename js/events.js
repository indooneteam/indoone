const $ = id => document.getElementById(id);
const overlay = $('overlay');

let authResolved = false;
let authGateRunning = false;
let authStateResolved = false;

function markAuthReady() {
  if (authResolved) return;
  authResolved = true;
  document.body.classList.add('auth-ready');
  document.body.classList.remove('auth-boot');
}

function renderLoginNow() {
  try {
    if (window.IndooneAuthUI && typeof window.IndooneAuthUI.showLogin === 'function') {
      window.IndooneAuthUI.showLogin();
      markAuthReady();
      return true;
    }
    return false;
  } catch (error) {
    console.error('Indoone login UI error:', error);
    return false;
  }
}

if (!localStorage.getItem('indoone_otp_verified_uid')) renderLoginNow();

$('menuBtn').addEventListener('click', toggleMenu);
$('drawerBackdrop').addEventListener('click', e => {
  const x = e.clientX;
  const y = e.clientY;
  closeDrawer();
  requestAnimationFrame(() => {
    const target = document.elementFromPoint(x, y);
    if (target && !target.closest('#drawer')) target.click();
  });
});
$('brandBtn').addEventListener('click', () => { closeDrawer(); closeModal(); $('accountsNav').click(); });
$('searchBtn').addEventListener('click', () => { $('searchWrap').hidden = false; $('search').focus(); });
$('clearSearch').addEventListener('click', () => { $('search').value = ''; indooneState.search = ''; renderAccounts(); });
$('search').addEventListener('input', e => { indooneState.search = e.target.value.toLowerCase().trim(); renderAccounts(); });

window.handleSortAccounts = function () {
  $('sortBtn').textContent = 'Sort ↕';
  indooneState.newestFirst = !indooneState.newestFirst;
  renderAccounts();
  toast(indooneState.newestFirst ? 'Newest first' : 'Name A–Z');
};

$('addBtn').addEventListener('click', showAdd);
$('emptyAddBtn').addEventListener('click', showAdd);
$('accountsNav').addEventListener('click', () => { closeModal(); $('accountsNav').classList.add('active'); $('settingsNav').classList.remove('active'); renderAccounts(); if (typeof refreshAccountCodes === 'function') refreshAccountCodes(); });
$('settingsNav').addEventListener('click', () => { closeModal(); $('settingsNav').classList.add('active'); $('accountsNav').classList.remove('active'); showSettings(); });
$('moreBtn').addEventListener('click', () => openModal(`<div class="more-options-modal" onclick="if(event.target===this)closeModal()"><div class="modal-head"><h2>More Options</h2><button type="button" class="close-btn" data-close>×</button></div><button type="button" class="settings-row" style="width:100%;" data-sort-modal><span>Sort accounts<small>${indooneState.newestFirst ? 'Newest first' : 'Name A–Z'}</small></span><b>›</b></button><button type="button" class="settings-row" style="width:100%;" data-security><span>Security<small>Review privacy settings</small></span><b>›</b></button><button type="button" class="settings-row" style="width:100%;" data-about><span>About Indoone<small>Version 1.0.0</small></span><b>›</b></button></div>`));

document.querySelector('.drawer').addEventListener('click', e => { const item = e.target.closest('[data-action]'); if (!item) return; const a = item.dataset.action; if (a === 'accounts') $('accountsNav').click(); if (a === 'favorites') showFavorites(); if (a === 'trash') showTrash(); if (a === 'security') showSecurity(); if (a === 'backup') showBackup(); if (a === 'settings') showSettings(); if (a === 'about') showAbout(); if (a === 'lock') lockIndoone(); });
document.querySelector('.account-list').addEventListener('click', e => { const fav = e.target.closest('[data-fav]'); if (fav) { e.stopPropagation(); toggleFavorite(fav.dataset.fav); return; } const card = e.target.closest('.account'); if (card) openAccount(card.dataset.id); });
document.querySelector('.account-list').addEventListener('keydown', e => { const card = e.target.closest('.account'); if (card && (e.key === 'Enter' || e.key === ' ')) openAccount(card.dataset.id); });
overlay.addEventListener('click', e => { if (e.target === overlay) { closeModal(); return; } if (e.target.closest('[data-close]')) { if (window.IndooneQrScanner) IndooneQrScanner.stop(); closeModal(); return; } if (e.target.closest('[data-tab]')) { const tab = e.target.closest('[data-tab]').dataset.tab; if (window.IndooneQrScanner) IndooneQrScanner.stop(); if (tab === 'manual') showManual(); else showAdd(); return; } if (e.target.closest('[data-import-uri]')) { importOtpUri(); return; } if (e.target.closest('[data-create-recovery]')) { createBackup(); return; } if (e.target.closest('[data-restore-recovery]')) { restoreRecoveryPdf(); return; } if (e.target.closest('[data-delete-recovery]')) { deleteRecoveryPdf(); return; } if (e.target.closest('[data-camera]')) { IndooneQrScanner.start(); return; } if (e.target.closest('[data-save-account]')) { saveAccount(); return; } if (e.target.closest('[data-copy]')) { copyCurrentCode(); return; } if (e.target.closest('[data-delete]')) { deleteCurrent(); return; } if (e.target.closest('[data-edit]')) { editCurrent(); return; } if (e.target.closest('[data-toggle]')) { e.target.closest('[data-toggle]').classList.toggle('on'); toast('Setting updated'); return; } if (e.target.closest('[data-pin]')) { showAppLock('setup'); return; } if (e.target.closest('[data-backup]')) { showBackup(); return; } if (e.target.closest('[data-about]')) { showAbout(); return; } if (e.target.closest('[data-security]')) { showSecurity(); return; } if (e.target.closest('[data-sort-modal]')) { window.handleSortAccounts(); closeModal(); return; } if (e.target.closest('[data-stop-scan]')) { IndooneQrScanner.stop(); closeModal(); return; } });

document.addEventListener('click', e => {
  if (!overlay.classList.contains('hidden') && e.target === overlay) closeModal();
}, true);

async function showAuthOrHome(user) {
  if (authGateRunning) return;
  authGateRunning = true;
  try {
    if (window.__indooneAuthPending) return;

    const persistedUid = localStorage.getItem('indoone_otp_verified_uid');

    if (!user) {
      if (persistedUid && !authStateResolved) return;
      localStorage.removeItem('indoone_otp_verified_uid');
      localStorage.removeItem('indoone_authenticated_uid');
      sessionStorage.removeItem('indoone_otp_verified_uid');
      sessionStorage.removeItem('indoone_authenticated_uid');
      renderLoginNow();
      return;
    }

    authStateResolved = true;

    if (!persistedUid || persistedUid !== user.uid) {
      localStorage.removeItem('indoone_otp_verified_uid');
      localStorage.removeItem('indoone_authenticated_uid');
      sessionStorage.removeItem('indoone_otp_verified_uid');
      sessionStorage.removeItem('indoone_authenticated_uid');
      await window.IndooneFirebase?.auth?.signOut?.().catch(() => {});
      renderLoginNow();
      return;
    }

    sessionStorage.setItem('indoone_otp_verified_uid', persistedUid);
    sessionStorage.setItem('indoone_authenticated_uid', persistedUid);

    if (IndoonePersistence.hasVault()) {
      indooneState.accounts = [];
      renderAccounts();
      showAppLock('unlock');
    } else {
      renderAccounts();
      if (typeof startDemoTimers === 'function') startDemoTimers();
    }
  } catch (error) {
    console.error('Indoone auth gate error:', error);
    renderLoginNow();
  } finally {
    authGateRunning = false;
    markAuthReady();
  }
}

window.addEventListener('load', async () => {
  const auth = window.IndooneFirebase?.auth;
  window.__indooneLoginOtp = null;
  window.__indooneSignupDraft = null;
  window.__indooneAuthPending = false;

  if (!auth) {
    renderLoginNow();
    return;
  }

  try {
    await (window.IndooneFirebase?.persistenceReady || Promise.resolve());
  } catch (error) {
    console.warn('Indoone Firebase persistence initialization failed:', error);
  }

  if (typeof auth.onAuthStateChanged === 'function') {
    auth.onAuthStateChanged(user => showAuthOrHome(user));
    setTimeout(() => {
      if (!authStateResolved && !auth.currentUser) {
        const persistedUid = localStorage.getItem('indoone_otp_verified_uid');
        if (!persistedUid) renderLoginNow();
      }
    }, 5000);
  } else {
    renderLoginNow();
  }
});
