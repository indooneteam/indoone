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

$('menuBtn')?.addEventListener('click', toggleMenu);
$('drawerBackdrop')?.addEventListener('click', e => {
  const x = e.clientX;
  const y = e.clientY;
  closeDrawer();
  requestAnimationFrame(() => {
    const target = document.elementFromPoint(x, y);
    if (target && !target.closest('#drawer')) target.click();
  });
});
$('brandBtn')?.addEventListener('click', () => { closeDrawer(); closeModal(); $('accountsNav')?.click(); });
$('searchBtn')?.addEventListener('click', () => { $('searchWrap').hidden = false; $('search').focus(); });
$('clearSearch')?.addEventListener('click', () => { $('search').value = ''; indooneState.search = ''; renderAccounts(); });
$('search')?.addEventListener('input', e => { indooneState.search = e.target.value.toLowerCase().trim(); renderAccounts(); });

window.handleSortAccounts = function () {
  $('sortBtn').textContent = 'Sort ↕';
  indooneState.newestFirst = !indooneState.newestFirst;
  renderAccounts();
  toast(indooneState.newestFirst ? 'Newest first' : 'Name A–Z');
};

$('addBtn')?.addEventListener('click', showAdd);
$('emptyAddBtn')?.addEventListener('click', showAdd);
$('accountsNav')?.addEventListener('click', () => { closeModal(); $('accountsNav').classList.add('active'); $('settingsNav')?.classList.remove('active'); renderAccounts(); if (typeof refreshAccountCodes === 'function') refreshAccountCodes(); });
$('settingsNav')?.addEventListener('click', () => { closeModal(); $('settingsNav').classList.add('active'); $('accountsNav')?.classList.remove('active'); showSettings(); });

document.querySelector('.drawer')?.addEventListener('click', e => {
  const item = e.target.closest('[data-action]');
  if (!item) return;
  const action = item.dataset.action;
  if (action === 'accounts') $('accountsNav')?.click();
  if (action === 'favorites') showFavorites();
  if (action === 'trash') showTrash();
  if (action === 'security') showSecurity();
  if (action === 'backup') showBackup();
  if (action === 'settings') showSettings();
  if (action === 'about') showAbout();
  if (action === 'lock') lockIndoone();
  if (action === 'danger-zone' && typeof showDangerZone === 'function') showDangerZone();
  if (action === 'logout' && typeof showLogout === 'function') showLogout();
});

document.querySelector('.account-list')?.addEventListener('click', e => {
  const fav = e.target.closest('[data-fav]');
  if (fav) { e.stopPropagation(); toggleFavorite(fav.dataset.fav); return; }
  const card = e.target.closest('.account');
  if (card) openAccount(card.dataset.id);
});
document.querySelector('.account-list')?.addEventListener('keydown', e => {
  const card = e.target.closest('.account');
  if (card && (e.key === 'Enter' || e.key === ' ')) openAccount(card.dataset.id);
});
overlay?.addEventListener('click', e => {
  if (e.target === overlay) { closeModal(); return; }
  if (e.target.closest('[data-close]')) { if (window.IndooneQrScanner) IndooneQrScanner.stop(); closeModal(); return; }
  if (e.target.closest('[data-tab]')) { const tab = e.target.closest('[data-tab]').dataset.tab; if (window.IndooneQrScanner) IndooneQrScanner.stop(); if (tab === 'manual') showManual(); else showAdd(); return; }
  if (e.target.closest('[data-import-uri]')) { importOtpUri(); return; }
  if (e.target.closest('[data-create-recovery]')) { createBackup(); return; }
  if (e.target.closest('[data-restore-recovery]')) { restoreRecoveryPdf(); return; }
  if (e.target.closest('[data-delete-recovery]')) { deleteRecoveryPdf(); return; }
  if (e.target.closest('[data-camera]')) { IndooneQrScanner.start(); return; }
  if (e.target.closest('[data-save-account]')) { saveAccount(); return; }
  if (e.target.closest('[data-copy]')) { copyCurrentCode(); return; }
  if (e.target.closest('[data-delete]')) { deleteCurrent(); return; }
  if (e.target.closest('[data-edit]')) { editCurrent(); return; }
  if (e.target.closest('[data-toggle]')) { e.target.closest('[data-toggle]').classList.toggle('on'); toast('Setting updated'); return; }
  if (e.target.closest('[data-pin]')) { showAppLock('setup'); return; }
  if (e.target.closest('[data-backup]')) { showBackup(); return; }
  if (e.target.closest('[data-about]')) { showAbout(); return; }
  if (e.target.closest('[data-security]')) { showSecurity(); return; }
  if (e.target.closest('[data-sort-modal]')) { window.handleSortAccounts(); closeModal(); return; }
  if (e.target.closest('[data-stop-scan]')) { IndooneQrScanner.stop(); closeModal(); return; }
});

document.addEventListener('click', e => {
  if (!overlay?.classList.contains('hidden') && e.target === overlay) closeModal();
}, true);

async function loadFirebaseAccounts() {
  if (!window.IndooneCloudAccounts?.load) throw new Error('Firebase account storage is unavailable.');
  await window.IndooneCloudAccounts.load();
}

window.loadFirebaseAccounts = loadFirebaseAccounts;

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
    await loadFirebaseAccounts();
    if (typeof startDemoTimers === 'function') startDemoTimers();
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

(function fixBottomNavigationIcons() {
  const rebuild = () => {
    const accounts = document.getElementById('accountsNav');
    const settings = document.getElementById('settingsNav');
    if (accounts) {
      accounts.innerHTML = '<svg class="nav-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false"><rect x="4" y="4" width="6" height="6" rx="1.5" fill="none" stroke="currentColor" stroke-width="1.7"></rect><rect x="14" y="4" width="6" height="6" rx="1.5" fill="none" stroke="currentColor" stroke-width="1.7"></rect><rect x="4" y="14" width="6" height="6" rx="1.5" fill="none" stroke="currentColor" stroke-width="1.7"></rect><rect x="14" y="14" width="6" height="6" rx="1.5" fill="none" stroke="currentColor" stroke-width="1.7"></rect></svg><span>Accounts</span>';
    }
    if (settings) {
      settings.innerHTML = '<svg class="nav-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false"><path d="M12 3.5l1.1 1.9a6.9 6.9 0 0 1 1.7.7l2.2-.5 1.7 2.9-1.1 1.9c.2.6.4 1.1.4 1.7l1.1 1.6-1.7 2.9-2.2-.5a6.9 6.9 0 0 1-1.7.7L12 20.5l-3.4-.1-1.1-1.9a6.9 6.9 0 0 1-1.7-.7l-2.2.5-1.7-2.9 1.1-1.9c-.2-.6-.4-1.1-.4-1.7L1.5 10.2l1.7-2.9 2.2.5c.5-.3 1.1-.6 1.7-.7L8.6 5.4 12 3.5Z" transform="translate(1.5 0) scale(.9)" fill="none" stroke="currentColor" stroke-width="1.65" stroke-linejoin="round"></path><circle cx="12" cy="12" r="3.2" fill="none" stroke="currentColor" stroke-width="1.65"></circle></svg><span>Settings</span>';
    }
  };
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', rebuild, { once: true });
  else rebuild();
})();
