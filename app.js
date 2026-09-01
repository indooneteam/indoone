const overlay = document.getElementById('overlay');
const modal = document.getElementById('modal');
const drawer = document.getElementById('drawer');
const toastEl = document.getElementById('toast');
const accountList = document.getElementById('accountList');
const emptyState = document.getElementById('emptyState');
const drawerCount = document.getElementById('drawerCount');

let accounts = [
  {id:1,name:'Google',email:'personal@gmail.com',code:'287 463',seconds:18,favorite:true,icon:'G',cls:'google'},
  {id:2,name:'GitHub',email:'user@github.com',code:'192 834',seconds:18,favorite:false,icon:'●',cls:'github'},
  {id:3,name:'Microsoft',email:'account@outlook.com',code:'653 198',seconds:18,favorite:false,icon:'⊞',cls:'microsoft'},
  {id:4,name:'Binance',email:'user@binance.com',code:'312 658',seconds:18,favorite:false,icon:'◆',cls:'binance'},
  {id:5,name:'Dropbox',email:'user@dropbox.com',code:'478 921',seconds:18,favorite:false,icon:'◆',cls:'dropbox'}
];
let searchQuery = '';
let sortNewest = true;
let toastTimer;

function toast(message){
  toastEl.textContent = message;
  toastEl.classList.add('show');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(()=>toastEl.classList.remove('show'),1800);
}

function closeModal(){overlay.classList.add('hidden');modal.innerHTML='';}
function openModal(html){modal.innerHTML=html;overlay.classList.remove('hidden');}
function closeDrawer(){drawer.classList.remove('open');drawer.setAttribute('aria-hidden','true');}
function toggleDrawer(){const open=drawer.classList.toggle('open');drawer.setAttribute('aria-hidden',String(!open));}

function renderAccounts(){
  let list = accounts.filter(a => `${a.name} ${a.email}`.toLowerCase().includes(searchQuery));
  if(sortNewest) list = [...list].sort((a,b)=>b.id-a.id);
  else list = [...list].sort((a,b)=>a.name.localeCompare(b.name));
  accountList.innerHTML = list.map(a => `
    <article class="account" data-id="${a.id}" tabindex="0" aria-label="Open ${a.name}">
      <div class="service-icon ${a.cls}">${a.icon}</div>
      <div class="account-info"><b>${a.name}</b><span>${a.email}</span><strong>${a.code}</strong></div>
      <button class="fav ${a.favorite?'active':''}" data-fav="${a.id}" aria-label="${a.favorite?'Remove from':'Add to'} favorites">${a.favorite?'★':'☆'}</button>
      <div class="timer">${a.seconds}</div>
    </article>`).join('');
  emptyState.hidden = list.length !== 0;
  drawerCount.textContent = accounts.length;
}

function openAccount(id){
  const a=accounts.find(x=>x.id===Number(id)); if(!a)return;
  openModal(`<div class="modal-head"><button class="close-btn" data-close>‹</button><h2>${a.name}</h2><button class="close-btn" data-edit>✎</button></div>
    <div class="token"><div class="token-icon">${a.icon}</div><p>${a.email}</p><div class="token-code">${a.code}</div><div class="progress"><span style="width:${Math.max(5,a.seconds/30*100)}%"></span></div><div class="countdown">Code expires in <b>${a.seconds}s</b></div></div>
    <div class="detail-grid"><div class="detail"><small>TYPE</small><b>TOTP</b></div><div class="detail"><small>PERIOD</small><b>30 seconds</b></div><div class="detail"><small>ALGORITHM</small><b>SHA-1</b></div><div class="detail"><small>DIGITS</small><b>6 digits</b></div></div>
    <button class="primary" data-copy>Copy code</button><button class="secondary danger" data-delete>Delete account</button>`);
  modal.dataset.accountId=a.id;
}

function showAdd(){
  openModal(`<div class="modal-head"><h2>Add Account</h2><button class="close-btn" data-close>×</button></div>
    <div class="tabs"><button class="selected" data-tab="qr">Scan QR Code</button><button data-tab="manual">Enter Setup Key</button></div>
    <div id="addPane"><div class="qr">▦</div><p style="text-align:center">Point your camera at the QR code provided by your account.</p><div class="qr-actions"><button class="secondary" data-camera>Open Camera</button><button class="secondary" data-gallery>Gallery</button></div><button class="primary" data-demo-scan>Demo Scan QR</button></div>`);
}

function showManual(prefill={}){
  openModal(`<div class="modal-head"><h2>${prefill.id?'Edit Account':'Add Account'}</h2><button class="close-btn" data-close>×</button></div>
    <div class="tabs"><button data-tab="qr">Scan QR Code</button><button class="selected" data-tab="manual">Enter Setup Key</button></div>
    <div class="field"><label>ACCOUNT NAME</label><input id="accountName" value="${prefill.name||''}" placeholder="e.g. Google"></div>
    <div class="field"><label>EMAIL / USERNAME</label><input id="accountEmail" value="${prefill.email||''}" placeholder="you@example.com"></div>
    <div class="field"><label>SECRET KEY</label><input id="secretKey" value="${prefill.secret||''}" placeholder="Enter setup key"></div>
    <div class="field"><label>TYPE</label><select><option>Time based (TOTP)</option></select></div>
    <div class="field"><label>DIGITS</label><select><option>6 digits</option><option>8 digits</option></select></div>
    <button class="primary" data-save-account data-edit-id="${prefill.id||''}">${prefill.id?'Save Changes':'Save Account'}</button>`);
}

function showSettings(){
  openModal(`<div class="modal-head"><h2>Settings</h2><button class="close-btn" data-close>×</button></div>
    <div class="settings-row"><span>App Lock<small>Protect Indoone with a PIN</small></span><button class="secondary" style="width:auto;padding:0 14px;margin:0" data-pin>Set PIN</button></div>
    <div class="settings-row"><span>Biometric Unlock<small>Fingerprint / Face unlock</small></span><button class="toggle on" data-toggle aria-label="Toggle biometric"></button></div>
    <div class="settings-row"><span>Hide Codes<small>Hide codes until unlocked</small></span><button class="toggle" data-toggle aria-label="Toggle hide codes"></button></div>
    <div class="settings-row"><span>Vibration<small>Haptic feedback for actions</small></span><button class="toggle on" data-toggle aria-label="Toggle vibration"></button></div>
    <div class="settings-row"><span>Encrypted Backup<small>Export accounts to an encrypted file</small></span><button class="secondary" style="width:auto;padding:0 14px;margin:0" data-backup>Open</button></div>
    <div class="settings-row"><span>About Indoone<small>Private. Offline. Yours.</small></span><button class="secondary" style="width:auto;padding:0 14px;margin:0" data-about>View</button></div>`);
}

function showBackup(){
  openModal(`<div class="modal-head"><h2>Secure Backup</h2><button class="close-btn" data-close>×</button></div><div class="token-icon" style="margin-top:15px">☁</div><p style="text-align:center">Create an encrypted backup of your authenticator accounts. This demo does not upload anything.</p><button class="primary" data-create-backup>Create Backup</button><button class="secondary" data-import-backup>Import Backup</button>`);
}

function showTrash(){
  openModal(`<div class="modal-head"><h2>Trash</h2><button class="close-btn" data-close>×</button></div><div class="empty-state" style="padding:40px 10px"><div class="empty-icon">♙</div><h3>No accounts in trash</h3><p>Deleted demo accounts will appear here.</p><button class="secondary" data-close>Close</button></div>`);
}

function showSecurity(){
  openModal(`<div class="modal-head"><h2>Security</h2><button class="close-btn" data-close>×</button></div><div class="settings-row"><span>Local storage<small>Accounts stay on this device</small></span><b>ON</b></div><div class="settings-row"><span>Cloud sync<small>No server connection in this demo</small></span><b>OFF</b></div><div class="settings-row"><span>App lock<small>Use device security</small></span><b>Ready</b></div>`);
}

function showAbout(){
  openModal(`<div class="modal-head"><h2>About Indoone</h2><button class="close-btn" data-close>×</button></div><div class="token-icon">I</div><p style="text-align:center"><b>Indoone Authenticator</b><br>Private, simple and offline-first.</p><div class="detail"><small>DEMO VERSION</small><b>1.0.0</b></div><button class="primary" data-close>Done</button>`);
}

function showPin(){
  openModal(`<div class="modal-head"><h2>App Lock</h2><button class="close-btn" data-close>×</button></div><p style="text-align:center">Enter a 4-digit demo PIN.</p><div class="pin-dots" id="pinDots"><i></i><i></i><i></i><i></i></div><div class="pin" id="pinPad">${[1,2,3,4,5,6,7,8,9,'⌫',0,'✓'].map(n=>`<button data-pin-key="${n}">${n}</button>`).join('')}</div>`);
  let pin='';
  modal.querySelectorAll('[data-pin-key]').forEach(btn=>btn.addEventListener('click',()=>{
    const key=btn.dataset.pinKey;
    if(key==='⌫')pin=pin.slice(0,-1); else if(key==='✓'){if(pin.length===4){closeModal();toast('PIN saved for demo');}else toast('Enter 4 digits');}else if(pin.length<4)pin+=key;
    modal.querySelectorAll('#pinDots i').forEach((d,i)=>d.classList.toggle('on',i<pin.length));
  }));
}

function toggleFavorite(id){const a=accounts.find(x=>x.id===Number(id));if(a){a.favorite=!a.favorite;renderAccounts();toast(a.favorite?'Added to favorites':'Removed from favorites');}}

function saveAccount(){
  const name=document.getElementById('accountName')?.value.trim();
  const email=document.getElementById('accountEmail')?.value.trim();
  const secret=document.getElementById('secretKey')?.value.trim();
  const editId=Number(modal.querySelector('[data-save-account]')?.dataset.editId||0);
  if(!name||!email||!secret){toast('Fill all account fields');return;}
  if(editId){const a=accounts.find(x=>x.id===editId);if(a){a.name=name;a.email=email;}toast('Account updated');openAccount(editId);return;}
  accounts.push({id:Date.now(),name,email,secret,code:'000 000',seconds:30,favorite:false,icon:name.charAt(0).toUpperCase(),cls:'google'});
  renderAccounts();closeModal();toast('Account added to demo');
}

function editCurrent(){const a=accounts.find(x=>x.id===Number(modal.dataset.accountId));if(a)showManual(a);}
function deleteCurrent(){const id=Number(modal.dataset.accountId);accounts=accounts.filter(a=>a.id!==id);renderAccounts();closeModal();toast('Account moved to trash');}

accountList.addEventListener('click',e=>{
  const fav=e.target.closest('[data-fav]');if(fav){e.stopPropagation();toggleFavorite(fav.dataset.fav);return;}
  const card=e.target.closest('.account');if(card)openAccount(card.dataset.id);
});
accountList.addEventListener('keydown',e=>{if((e.key==='Enter'||e.key===' ')&&e.target.closest('.account'))openAccount(e.target.closest('.account').dataset.id)});

document.getElementById('menuBtn').addEventListener('click',toggleDrawer);
document.getElementById('drawerBackdrop').addEventListener('click',closeDrawer);
document.getElementById('brandBtn').addEventListener('click',()=>{closeDrawer();closeModal();document.getElementById('accountsNav').click()});
document.getElementById('searchBtn').addEventListener('click',()=>{const w=document.getElementById('searchWrap');w.hidden=false;document.getElementById('search').focus()});
document.getElementById('clearSearch').addEventListener('click',()=>{document.getElementById('search').value='';searchQuery='';renderAccounts()});
document.getElementById('moreBtn').addEventListener('click',()=>openModal(`<div class="modal-head"><h2>More Options</h2><button class="close-btn" data-close>×</button></div><button class="settings-row" data-sort-modal><span>Sort accounts<small>${sortNewest?'Newest first':'Name A–Z'}</small></span><b>›</b></button><button class="settings-row" data-security><span>Security<small>Review privacy settings</small></span><b>›</b></button><button class="settings-row" data-about><span>About Indoone<small>Version 1.0.0</small></span><b>›</b></button>`));
document.getElementById('search').addEventListener('input',e=>{searchQuery=e.target.value.toLowerCase().trim();renderAccounts()});
document.getElementById('sortBtn').addEventListener('click',()=>{sortNewest=!sortNewest;renderAccounts();toast(sortNewest?'Newest first':'Name A–Z')});
document.getElementById('addBtn').addEventListener('click',showAdd);
document.getElementById('emptyAddBtn').addEventListener('click',showAdd);
document.getElementById('accountsNav').addEventListener('click',()=>{closeModal();document.getElementById('accountsNav').classList.add('active');document.getElementById('settingsNav').classList.remove('active');toast('Accounts')});
document.getElementById('settingsNav').addEventListener('click',()=>{document.getElementById('settingsNav').classList.add('active');document.getElementById('accountsNav').classList.remove('active');showSettings()});

drawer.addEventListener('click',e=>{const item=e.target.closest('[data-action]');if(!item)return;closeDrawer();const action=item.dataset.action;if(action==='accounts'){document.getElementById('accountsNav').click()}else if(action==='settings'){showSettings()}else if(action==='backup'){showBackup()}else if(action==='trash'){showTrash()}else if(action==='security'){showSecurity()}else if(action==='about'){showAbout()}else if(action==='favorites'){searchQuery='';document.getElementById('searchWrap').hidden=false;document.getElementById('search').value='';accountList.innerHTML=accounts.filter(a=>a.favorite).map(a=>`<article class="account" data-id="${a.id}"><div class="service-icon ${a.cls}">${a.icon}</div><div class="account-info"><b>${a.name}</b><span>${a.email}</span><strong>${a.code}</strong></div><div class="timer">${a.seconds}</div></article>`).join('');emptyState.hidden=true;toast('Favorites') }else if(action==='lock'){showPin()}});

overlay.addEventListener('click',e=>{if(e.target===overlay)closeModal();const el=e.target.closest('[data-close]');if(el){closeModal();return}if(e.target.closest('[data-tab]')){const tab=e.target.closest('[data-tab]').dataset.tab;if(tab==='manual')showManual();else showAdd();return}if(e.target.closest('[data-demo-scan]')){showManual({name:'Google',email:'personal@gmail.com',secret:'DEMO-SECRET-KEY'});toast('Demo QR detected');return}if(e.target.closest('[data-camera]')){toast('Camera permission is demo-only');return}if(e.target.closest('[data-gallery]')){toast('Gallery picker is demo-only');return}if(e.target.closest('[data-save-account]')){saveAccount();return}if(e.target.closest('[data-copy]')){const a=accounts.find(x=>x.id===Number(modal.dataset.accountId));if(a&&navigator.clipboard){navigator.clipboard.writeText(a.code).catch(()=>{});}toast('Code copied');return}if(e.target.closest('[data-delete]')){deleteCurrent();return}if(e.target.closest('[data-edit]')){editCurrent();return}if(e.target.closest('[data-toggle]')){e.target.closest('[data-toggle]').classList.toggle('on');toast('Setting updated');return}if(e.target.closest('[data-pin]')){showPin();return}if(e.target.closest('[data-backup]')){showBackup();return}if(e.target.closest('[data-create-backup]')){toast('Encrypted backup created (demo)');return}if(e.target.closest('[data-import-backup]')){toast('Backup picker opened (demo)');return}if(e.target.closest('[data-about]')){showAbout();return}if(e.target.closest('[data-security]')){showSecurity();return}if(e.target.closest('[data-sort-modal]')){sortNewest=!sortNewest;renderAccounts();closeModal();toast(sortNewest?'Newest first':'Name A–Z')}});

setInterval(()=>{accounts.forEach(a=>{a.seconds--;if(a.seconds<=0){a.seconds=30;a.code=String(Math.floor(Math.random()*1000000)).padStart(6,'0').replace(/(\d{3})(\d{3})/,'$1 $2')}});renderAccounts()},1000);
renderAccounts();
