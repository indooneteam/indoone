const drawerEl=document.getElementById('drawer');
const drawerPanel=drawerEl?.querySelector('.drawer-panel');
window.toggleMenu=function(){const open=drawerEl.classList.toggle('open');drawerEl.setAttribute('aria-hidden',String(!open));};
window.closeDrawer=function(){drawerEl.classList.remove('open');drawerEl.setAttribute('aria-hidden','true');};

// Close the drawer whenever the user clicks/taps anywhere outside the drawer panel.
drawerEl?.addEventListener('pointerdown',event=>{
  if(drawerEl.classList.contains('open')&&drawerPanel&&!drawerPanel.contains(event.target)) closeDrawer();
});

window.showFavorites=async function(){
  closeDrawer();
  try {
    if (window.IndooneCloudAccounts?.load) await window.IndooneCloudAccounts.load();
    const list=indooneState.accounts.filter(a=>a.favorite);
    const rows=list.length
      ? list.map(a=>`<button type="button" class="settings-row" style="width:100%;text-align:left;border:0;background:#fff;" onclick="closeModal();openAccount(${Number(a.id)})"><span>${a.name}<small>${a.email||'Authenticator account'}</small></span><b>${a.code||'------'}</b></button>`).join('')
      : `<p style="text-align:center;padding:28px 0">No favorite accounts yet.</p>`;
    openModal(`<div class="modal-head"><h2>Favorites</h2><button class="close-btn" data-close>×</button></div><div id="favoritesList">${rows}</div><button class="primary" data-close>Done</button>`);
  } catch(error) {
    toast(error?.message||'Could not load favorites');
  }
};

window.showTrash=function(){
  closeDrawer();
  openModal(`<div class="modal-head"><h2>Trash</h2><button class="close-btn" data-close>×</button></div><div class="empty-state" style="padding:40px 10px"><div class="empty-icon">♙</div><h3>No accounts in trash</h3><p>Deleted accounts are permanently removed from Firebase. Nothing is kept here.</p></div><button class="secondary" data-close>Close</button>`);
};

window.showSecurity=function(){closeDrawer();openModal(`<div class="modal-head"><h2>Security</h2><button class="close-btn" data-close>×</button></div><div class="settings-row"><span>Local storage<small>Accounts stay on this device</small></span><b>ON</b></div><div class="settings-row"><span>Cloud sync<small>No server connection</small></span><b>OFF</b></div><div class="settings-row"><span>Network access<small>Not required for TOTP</small></span><b>OFF</b></div>`) };
window.showAbout=function(){closeDrawer();openModal(`<div class="modal-head"><h2>About Indoone</h2><button class="close-btn" data-close>×</button></div><div class="token-icon">I</div><p style="text-align:center"><b>Indoone Authenticator</b><br>Private, simple and offline-first.</p><div class="detail"><small>VERSION</small><b>1.0.0 Demo</b></div>`) };