(() => {
  const CONTENT_ID = 'content';
  const ADD_HASH = '#home/add-account';
  const ADD_SCRIPT = 'app/home/add-account/add-account.js?v=20260915b';
  let homeView = null;
  let subPage = null;
  let initialized = false;
  let addFeatureReady = null;

  // Install the click handler before shared/events.js captures showAdd().
  window.showAdd = () => window.IndooneHome?.showAddAccount?.({ push: true });

  function setupViews() {
    const page = document.getElementById(CONTENT_ID);
    if (!page || initialized) return;
    initialized = true;
    page.dataset.feature = 'home';
    page.classList.add('home-page');

    homeView = document.createElement('div');
    homeView.className = 'home-view';
    while (page.firstChild) homeView.appendChild(page.firstChild);
    page.appendChild(homeView);

    subPage = document.createElement('div');
    subPage.id = 'homeSubPage';
    subPage.className = 'home-sub-page';
    subPage.hidden = true;
    page.appendChild(subPage);

    window.addEventListener('popstate', handleHistory);
  }

  function loadAddFeature() {
    if (window.IndooneAddAccount?.render) return Promise.resolve();
    if (addFeatureReady) return addFeatureReady;
    addFeatureReady = new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = ADD_SCRIPT;
      script.onload = () => resolve();
      script.onerror = () => reject(new Error('Add Account feature could not be loaded.'));
      document.head.appendChild(script);
    });
    return addFeatureReady;
  }

  function restoreAccountView() {
    setupViews();
    if (!homeView) return;
    homeView.hidden = false;
    if (subPage) subPage.hidden = true;
    document.getElementById('addBtn')?.removeAttribute('hidden');
    document.body.classList.remove('home-subpage-open');
    if (typeof renderAccounts === 'function') renderAccounts();
    if (typeof refreshAccountCodes === 'function') refreshAccountCodes();
  }

  function openAddHistory() {
    if (window.location.hash !== ADD_HASH) {
      history.pushState({ indoonePage: 'add-account' }, '', ADD_HASH);
    }
  }

  function handleHistory() {
    if (window.location.hash === ADD_HASH) {
      showAddAccount({ push: false });
      return;
    }
    restoreAccountView();
  }

  async function showAddAccount({ push = true } = {}) {
    setupViews();
    if (!subPage) return;
    if (push) openAddHistory();
    homeView.hidden = true;
    subPage.hidden = false;
    document.getElementById('addBtn')?.setAttribute('hidden', '');
    document.body.classList.add('home-subpage-open');
    try {
      await loadAddFeature();
      await window.IndooneAddAccount.render(subPage);
    } catch (error) {
      subPage.innerHTML = `<section class="add-account-page"><button type="button" class="add-account-back" data-add-action="back"><span class="back-icon">‹</span><span>Back</span></button><h1>Add Account</h1><p>Unable to load this page.</p></section>`;
      console.error('Indoone Add Account feature failed:', error);
      subPage.querySelector('[data-add-action="back"]')?.addEventListener('click', () => backToHome(), { once: true });
    }
  }

  function backToHome() {
    if (window.history.state?.indoonePage === 'add-account') {
      history.back();
    } else {
      history.replaceState({}, '', window.location.pathname + window.location.search);
      restoreAccountView();
    }
  }

  function init() {
    setupViews();
    if (window.location.hash === ADD_HASH) showAddAccount({ push: false });
  }

  window.IndooneHome = { init, showAddAccount, backToHome, restoreAccountView };
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init, { once: true });
  else init();
})();
