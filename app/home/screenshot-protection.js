(() => {
  const KEY = 'indoone_home_screenshot_protection_v1';
  let button = null;

  function isEnabled() {
    return sessionStorage.getItem(KEY) === '1';
  }

  function setNative(enabled) {
    try {
      window.IndooneNative?.setSecureScreen?.(!!enabled);
    } catch (error) {
      console.warn('Indoone screenshot protection native bridge unavailable:', error);
    }
  }

  function isHomeAccountsVisible() {
    const content = document.getElementById('content');
    const subPage = document.getElementById('homeSubPage');
    const accountsNav = document.getElementById('accountsNav');
    return !!content && !content.hidden && !!accountsNav?.classList.contains('active') && !subPage?.hidden;
  }

  function sync() {
    setNative(isHomeAccountsVisible() && isEnabled());
    updateButton();
  }

  function updateButton() {
    if (!button) return;
    const enabled = isEnabled();
    button.classList.toggle('on', enabled);
    button.setAttribute('aria-pressed', String(enabled));
    button.setAttribute('aria-label', enabled ? 'Disable screenshot protection' : 'Enable screenshot protection');
    button.title = enabled ? 'Screenshot protection on' : 'Screenshot protection off';
    button.innerHTML = `<span aria-hidden="true">${enabled ? '▣' : '□'}</span>`;
  }

  function toggle() {
    const next = !isEnabled();
    if (next) sessionStorage.setItem(KEY, '1');
    else sessionStorage.removeItem(KEY);
    sync();
    window.toast?.(next ? 'Screenshot protection enabled for Accounts' : 'Screenshot protection disabled');
  }

  function injectStyles() {
    if (document.getElementById('indoone-home-screenshot-style')) return;
    const style = document.createElement('style');
    style.id = 'indoone-home-screenshot-style';
    style.textContent = `
      .home-page .page-heading-tools{display:flex;align-items:center;gap:7px;flex:none}
      .home-page .home-screenshot-btn{width:36px;height:34px;display:grid;place-items:center;padding:0;border:1px solid #e5dff0;border-radius:10px;background:#fff;color:#77707f;font-size:15px;cursor:pointer;transition:all .15s ease}
      .home-page .home-screenshot-btn:hover{border-color:#cfc0e8;color:#6330db;background:#fbf9ff}
      .home-page .home-screenshot-btn.on{border-color:#d7c8f3;background:#f3edff;color:#6330db}
      .home-page .home-screenshot-btn span{line-height:1}
      @media(max-width:380px){.home-page .page-heading-tools{gap:5px}.home-page .home-screenshot-btn{width:34px;height:32px}}
    `;
    document.head.appendChild(style);
  }

  function mountButton() {
    const heading = document.querySelector('.home-page .page-heading');
    if (!heading || button?.isConnected) return;

    const sortButton = document.getElementById('sortBtn');
    if (!sortButton) return;

    const tools = document.createElement('div');
    tools.className = 'page-heading-tools';
    sortButton.parentNode.insertBefore(tools, sortButton);
    tools.appendChild(sortButton);

    button = document.createElement('button');
    button.type = 'button';
    button.className = 'home-screenshot-btn';
    button.addEventListener('click', toggle);
    tools.appendChild(button);
    updateButton();
  }

  function observeNavigation() {
    const observer = new MutationObserver(sync);
    ['accountsNav', 'lobbyNav', 'connectNav', 'settingsNav'].forEach(id => {
      const node = document.getElementById(id);
      if (node) observer.observe(node, { attributes: true, attributeFilter: ['class'] });
    });
    const content = document.getElementById('content');
    if (content) observer.observe(content, { attributes: true, attributeFilter: ['hidden'] });
  }

  function init() {
    injectStyles();
    mountButton();
    observeNavigation();
    sync();
  }

  window.IndooneHomeScreenshot = { sync, toggle, enable: () => { sessionStorage.setItem(KEY, '1'); sync(); }, disable: () => { sessionStorage.removeItem(KEY); sync(); } };

  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init, { once: true });
  else init();
})();
