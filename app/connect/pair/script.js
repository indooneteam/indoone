(() => {
  const MARKUP_URL = 'app/connect/pair/index.html?v=20260904a';
  const STYLE_URL = 'app/connect/pair/style.css?v=20260904b';
  let markupPromise = null;

  function loadStyle(){
    const base = STYLE_URL.split('?')[0];
    if(document.querySelector(`link[href^="${base}"]`)) return;
    const link = document.createElement('link');
    link.rel='stylesheet';
    link.href=STYLE_URL;
    document.head.appendChild(link);
  }

  async function loadMarkup(){
    if(markupPromise) return markupPromise;
    markupPromise = fetch(MARKUP_URL,{cache:'no-store'}).then(response=>{
      if(!response.ok) throw new Error('Pair page could not be loaded.');
      return response.text();
    }).catch(error=>{ markupPromise=null; throw error; });
    return markupPromise;
  }

  function bind(root){
    root.querySelectorAll('[data-pair-action]').forEach(button=>{
      button.addEventListener('click', event=>{
        event.preventDefault();
        const action = button.dataset.pairAction;
        if(action==='home') return window.showConnectHome?.();
        if(action==='qr') return window.showConnectQr?.();
        if(action==='scanner') return window.showConnectScanner?.();
      });
    });
  }

  async function show({ remember = true } = {}){
    const mount = document.getElementById('connectContent');
    const home = document.getElementById('connectHomePage');
    const choice = document.getElementById('connectChoicePage');
    if(!mount) return;

    if (remember) window.IndoonePageState?.set('pair');
    home?.setAttribute('hidden','');
    choice?.setAttribute('hidden','');

    loadStyle();
    try{
      mount.querySelector('.connect-pair-page')?.remove();
      mount.insertAdjacentHTML('beforeend', await loadMarkup());
      bind(mount.querySelector('.connect-pair-page'));
    }catch(error){
      const existing = mount.querySelector('.connect-pair-page');
      if(existing) existing.remove();
      mount.insertAdjacentHTML('beforeend', `<section class="connect-pair-page"><button type="button" class="connect-back" data-pair-action="home">‹ Back</button><p class="eyebrow">DEVICE PAIRING</p><h2>Pair</h2><p class="connect-pair-copy">Pair page could not be loaded.</p></section>`);
      bind(mount.querySelector('.connect-pair-page'));
      console.error('Indoone Pair page failed:', error);
    }
  }

  window.showConnectPair = show;
})();
