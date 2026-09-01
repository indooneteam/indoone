window.indooneState = {
  accounts: [
    {id:1,name:'Google',email:'personal@gmail.com',code:'287 463',seconds:18,favorite:true,icon:'G',cls:'google'},
    {id:2,name:'GitHub',email:'user@github.com',code:'192 834',seconds:18,favorite:false,icon:'●',cls:'github'},
    {id:3,name:'Microsoft',email:'account@outlook.com',code:'653 198',seconds:18,favorite:false,icon:'⊞',cls:'microsoft'},
    {id:4,name:'Binance',email:'user@binance.com',code:'312 658',seconds:18,favorite:false,icon:'◆',cls:'binance'},
    {id:5,name:'Dropbox',email:'user@dropbox.com',code:'478 921',seconds:18,favorite:false,icon:'◆',cls:'dropbox'}
  ],
  search: '',
  newestFirst: true,
  trash: []
};

window.renderAccounts = function () {
  const {accounts, search, newestFirst} = indooneState;
  let list = accounts.filter(a => `${a.name} ${a.email}`.toLowerCase().includes(search));
  list = [...list].sort(newestFirst ? (a,b)=>b.id-a.id : (a,b)=>a.name.localeCompare(b.name));
  const listEl = document.getElementById('accountList');
  listEl.innerHTML = list.map(a => `<article class="account" data-id="${a.id}" tabindex="0"><div class="service-icon ${a.cls}">${a.icon}</div><div class="account-info"><b>${a.name}</b><span>${a.email}</span><strong>${a.code}</strong></div><button class="fav ${a.favorite?'active':''}" data-fav="${a.id}" aria-label="Favorite">${a.favorite?'★':'☆'}</button><div class="timer">${a.seconds}</div></article>`).join('');
  document.getElementById('emptyState').hidden = list.length > 0;
  document.getElementById('drawerCount').textContent = accounts.length;
};

window.startDemoTimers = function () {
  setInterval(() => {
    indooneState.accounts.forEach(a => { a.seconds--; if(a.seconds < 1) a.seconds = 30; });
    renderAccounts();
  }, 1000);
};
