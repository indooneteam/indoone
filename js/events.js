const $=id=>document.getElementById(id);
const overlay=$('overlay');
$('menuBtn').addEventListener('click',toggleMenu);
$('drawerBackdrop').addEventListener('click',closeDrawer);
$('brandBtn').addEventListener('click',()=>{closeDrawer();closeModal();$('accountsNav').click()});
$('searchBtn').addEventListener('click',()=>{$('searchWrap').hidden=false;$('search').focus()});
$('clearSearch').addEventListener('click',()=>{$('search').value='';indooneState.search='';renderAccounts()});
$('search').addEventListener('input',e=>{indooneState.search=e.target.value.toLowerCase().trim();renderAccounts()});
$('sortBtn').addEventListener('click',()=>{$('sortBtn').textContent='Sort ↕';indooneState.newestFirst=!indooneState.newestFirst;renderAccounts();toast(indooneState.newestFirst?'Newest first':'Name A–Z')});
$('addBtn').addEventListener('click',showAdd);
$('emptyAddBtn').addEventListener('click',showAdd);
$('accountsNav').addEventListener('click',()=>{closeModal();$('accountsNav').classList.add('active');$('settingsNav').classList.remove('active');renderAccounts();if(typeof refreshAccountCodes==='function')refreshAccountCodes()});
$('settingsNav').addEventListener('click',()=>{closeModal();$('settingsNav').classList.add('active');$('accountsNav').classList.remove('active');showSettings()});
$('moreBtn').addEventListener('click',()=>openModal(`<div class="modal-head"><h2>More Options</h2><button class="close-btn" data-close>×</button></div><button class="settings-row" data-sort-modal><span>Sort accounts<small>${indooneState.newestFirst?'Newest first':'Name A–Z'}</small></span><b>›</b></button><button class="settings-row" data-security><span>Security<small>Review privacy settings</small></span><b>›</b></button><button class="settings-row" data-about><span>About Indoone<small>Version 1.0.0</small></span><b>›</b></button>`));
document.querySelector('.drawer').addEventListener('click',e=>{const item=e.target.closest('[data-action]');if(!item)return;const a=item.dataset.action;if(a==='accounts')$('accountsNav').click();if(a==='favorites')showFavorites();if(a==='trash')showTrash();if(a==='security')showSecurity();if(a==='backup')showBackup();if(a==='settings')showSettings();if(a==='about')showAbout();if(a==='lock')lockIndoone()});
document.querySelector('.account-list').addEventListener('click',e=>{const fav=e.target.closest('[data-fav]');if(fav){e.stopPropagation();toggleFavorite(fav.dataset.fav);return}const card=e.target.closest('.account');if(card)openAccount(card.dataset.id)});
document.querySelector('.account-list').addEventListener('keydown',e=>{const card=e.target.closest('.account');if(card&&(e.key==='Enter'||e.key===' '))openAccount(card.dataset.id)});
overlay.addEventListener('click',async e=>{if(e.target===overlay){closeModal();return}if(e.target.closest('[data-close]')){if(window.IndooneQrScanner)IndooneQrScanner.stop();closeModal();return}if(e.target.closest('[data-tab]')){const tab=e.target.closest('[data-tab]').dataset.tab;if(window.IndooneQrScanner)IndooneQrScanner.stop();if(tab==='manual')showManual();else showAdd();return}if(e.target.closest('[data-import-uri]')){importOtpUri();return}if(e.target.closest('[data-create-recovery]')){createBackup();return}if(e.target.closest('[data-restore-recovery]')){restoreRecoveryPdf();return}if(e.target.closest('[data-delete-recovery]')){deleteRecoveryPdf();return}if(e.target.closest('[data-camera]')){IndooneQrScanner.start();return}if(e.target.closest('[data-save-account]')){saveAccount();return}if(e.target.closest('[data-copy]')){copyCurrentCode();return}if(e.target.closest('[data-delete]')){deleteCurrent();return}if(e.target.closest('[data-edit]')){editCurrent();return}if(e.target.closest('[data-toggle]')){e.target.closest('[data-toggle]').classList.toggle('on');toast('Setting updated');return}if(e.target.closest('[data-pin]')){showAppLock('setup');return}if(e.target.closest('[data-backup]')){showBackup();return}if(e.target.closest('[data-about]')){showAbout();return}if(e.target.closest('[data-security]')){showSecurity();return}if(e.target.closest('[data-sort-modal]')){$('sortBtn').click();closeModal();return}if(e.target.closest('[data-stop-scan]')){IndooneQrScanner.stop();closeModal();return}});

function clearPersistentAuthState(){
  sessionStorage.removeItem('indoone_otp_verified_uid');
  sessionStorage.removeItem('indoone_authenticated_uid');
  sessionStorage.removeItem('indoone_signup_identity');
  try{localStorage.removeItem('indoone_otp_verified_uid');localStorage.removeItem('indoone_authenticated_uid')}catch{}
}

function showLoginScreen(){
  clearPersistentAuthState();
  indooneState.accounts=[];
  renderAccounts();
  window.IndooneAuthUI?.showLogin?.();
}

async function showAuthOrHome(user){
  // Firebase Auth persists its own session in the browser. Indoone intentionally
  // starts a fresh auth session on every page load; an authenticated home view
  // is only allowed after this page load completes OTP verification.
  if (!window.__indoonePageAuthReady){
    window.__indoonePageAuthReady=true;
    if(user){
      await window.IndooneFirebase?.auth?.signOut?.().catch?.(()=>{});
      showLoginScreen();
      return;
    }
    showLoginScreen();
    return;
  }

  const verifiedUid=sessionStorage.getItem('indoone_otp_verified_uid');
  if(!user || !verifiedUid || verifiedUid!==user.uid){
    if(user) await window.IndooneFirebase?.auth?.signOut?.().catch?.(()=>{});
    clearPersistentAuthState();
    indooneState.accounts=[];
    renderAccounts();
    window.IndooneAuthUI?.showLogin?.();
    return;
  }

  if(IndoonePersistence.hasVault()){
    indooneState.accounts=[];
    renderAccounts();
    showAppLock('unlock');
  }else{
    renderAccounts();
    if(typeof startDemoTimers==='function') startDemoTimers();
  }
}

window.addEventListener('load',()=>{
  const auth=window.IndooneFirebase?.auth;
  if(auth && typeof auth.onAuthStateChanged==='function') auth.onAuthStateChanged(showAuthOrHome);
  else showLoginScreen();
});
