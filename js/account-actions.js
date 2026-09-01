window.openAccount = function(id){
  const a=indooneState.accounts.find(x=>x.id===Number(id)); if(!a)return;
  openModal(`<div class="modal-head"><button class="close-btn" data-close>‹</button><h2>${a.name}</h2><button class="close-btn" data-edit>✎</button></div><div class="token"><div class="token-icon">${a.icon}</div><p>${a.email}</p><div class="token-code">${a.code}</div><div class="progress"><span style="width:${Math.max(5,a.seconds/30*100)}%"></span></div><div class="countdown">Code expires in <b>${a.seconds}s</b></div></div><div class="detail-grid"><div class="detail"><small>TYPE</small><b>TOTP</b></div><div class="detail"><small>PERIOD</small><b>30 seconds</b></div><div class="detail"><small>ALGORITHM</small><b>SHA-1</b></div><div class="detail"><small>DIGITS</small><b>6 digits</b></div></div><button class="primary" data-copy>Copy code</button><button class="secondary danger" data-delete>Delete account</button>`);
  modal.dataset.accountId=a.id;
};
window.toggleFavorite=function(id){const a=indooneState.accounts.find(x=>x.id===Number(id));if(a){a.favorite=!a.favorite;renderAccounts();toast(a.favorite?'Added to favorites':'Removed from favorites')}};
window.deleteCurrent=function(){const id=Number(modal.dataset.accountId);const i=indooneState.accounts.findIndex(a=>a.id===id);if(i>=0){indooneState.trash.push(indooneState.accounts.splice(i,1)[0]);renderAccounts();closeModal();toast('Account moved to trash')}};
window.editCurrent=function(){const a=indooneState.accounts.find(x=>x.id===Number(modal.dataset.accountId));if(a)showManual(a)};
window.copyCurrentCode=function(){const a=indooneState.accounts.find(x=>x.id===Number(modal.dataset.accountId));if(a){navigator.clipboard?.writeText(a.code.replace(/\s/g,''));toast('Code copied')}};
