const drawerEl=document.getElementById('drawer');
const drawerPanel=drawerEl?.querySelector('.drawer-panel');
window.toggleMenu=function(){const open=drawerEl.classList.toggle('open');drawerEl.setAttribute('aria-hidden',String(!open));};
window.closeDrawer=function(){drawerEl.classList.remove('open');drawerEl.setAttribute('aria-hidden','true');};

// Close the drawer whenever the user clicks/taps anywhere outside the drawer panel.
drawerEl?.addEventListener('pointerdown',event=>{
  if(drawerEl.classList.contains('open')&&drawerPanel&&!drawerPanel.contains(event.target)) closeDrawer();
});

window.showFavorites=function(){closeDrawer();const old=indooneState.search;indooneState.search='';document.getElementById('searchWrap').hidden=false;document.getElementById('search').value='';const list=indooneState.accounts.filter(a=>a.favorite);document.getElementById('accountList').innerHTML=list.map(a=>`<article class="account" data-id="${a.id}"><div class="service-icon ${a.cls}">${a.icon}</div><div class="account-info"><b>${a.name}</b><span>${a.email}</span><strong>${a.code}</strong></div><div class="timer">${a.seconds}</div></article>`).join('');document.getElementById('emptyState').hidden=true;toast('Favorites');indooneState.search=old;};
window.showTrash=function(){closeDrawer();openModal(`<div class="modal-head"><h2>Trash</h2><button class="close-btn" data-close>×</button></div><div class="empty-state" style="padding:40px 10px"><div class="empty-icon">♙</div><h3>${indooneState.trash.length?'Recently deleted':'No accounts in trash'}</h3><p>${indooneState.trash.length?'Deleted accounts are kept here in this demo.':'Deleted demo accounts will appear here.'}</p><button class="secondary" data-close>Close</button></div>`) };
window.showSecurity=function(){closeDrawer();openModal(`<div class="modal-head"><h2>Security</h2><button class="close-btn" data-close>×</button></div><div class="settings-row"><span>Local storage<small>Accounts stay on this device</small></span><b>ON</b></div><div class="settings-row"><span>Cloud sync<small>No server connection</small></span><b>OFF</b></div><div class="settings-row"><span>Network access<small>Not required for TOTP</small></span><b>OFF</b></div>`) };
window.showAbout=function(){closeDrawer();openModal(`<div class="modal-head"><h2>About Indoone</h2><button class="close-btn" data-close>×</button></div><div class="token-icon">I</div><p style="text-align:center"><b>Indoone Authenticator</b><br>Private, simple and offline-first.</p><div class="detail"><small>VERSION</small><b>1.0.0 Demo</b></div>`) };
