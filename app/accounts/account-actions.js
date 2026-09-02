window.openAccount = function(id){
  const a=indooneState.accounts.find(x=>x.id===Number(id)); if(!a)return;
  openModal(`<div class="modal-head"><button class="close-btn" data-close>‹</button><h2>${a.name}</h2><button class="close-btn" data-edit>✎</button></div><div class="token"><div class="token-icon">${a.icon}</div><p>${a.email || 'Authenticator account'}</p><div class="token-code">${a.code || '------'}</div><div class="progress"><span style="width:${Math.max(5,(a.seconds||30)/(a.period||30)*100)}%"></span></div><div class="countdown">Code expires in <b>${a.seconds ?? 30}s</b></div></div><div class="detail-grid"><div class="detail"><small>TYPE</small><b>TOTP</b></div><div class="detail"><small>PERIOD</small><b>${a.period || 30} seconds</b></div><div class="detail"><small>ALGORITHM</small><b>${a.algorithm || 'SHA1'}</b></div><div class="detail"><small>DIGITS</small><b>${a.digits || 6} digits</b></div></div><button class="primary" data-copy>Copy code</button><button class="secondary danger" data-delete>Delete account</button>`);
  modal.dataset.accountId=a.id;
};

window.toggleFavorite=async function(id){
  const a=indooneState.accounts.find(x=>x.id===Number(id));
  if(!a)return;
  a.favorite=!a.favorite;
  renderAccounts();
  try { await IndooneCloudAccounts.save(a); toast(a.favorite?'Added to favorites':'Removed from favorites'); }
  catch (_) { a.favorite=!a.favorite; renderAccounts(); toast('Could not sync favorite'); }
};

window.deleteCurrent=async function(){
  const id=Number(modal.dataset.accountId);
  const i=indooneState.accounts.findIndex(a=>a.id===id);
  if(i<0)return;
  const removed=indooneState.accounts[i];
  try {
    await IndooneCloudAccounts.moveToTrash(removed);
    indooneState.accounts.splice(i,1);
    renderAccounts();
    closeModal();
    toast('Account moved to Trash for 30 days');
  } catch (error) {
    toast(error?.message || 'Could not move account to Trash');
  }
};

window.editCurrent=function(){const a=indooneState.accounts.find(x=>x.id===Number(modal.dataset.accountId));if(a)showManual(a)};
window.copyCurrentCode=function(){const a=indooneState.accounts.find(x=>x.id===Number(modal.dataset.accountId));if(a){navigator.clipboard?.writeText((a.code||'').replace(/\s/g,''));toast('Code copied')}};
