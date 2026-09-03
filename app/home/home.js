(() => {
  const CONTENT_ID = 'content';
  const ADD_HASH = '#home/add-account';
  let homeView = null;
  let subPage = null;
  let initialized = false;

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
    if (window.location.hash === ADD_HASH) {
      window.IndooneHome.showAddAccount({ push: false });
    }
  }

  function restoreAccountView() {
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
      window.IndooneHome.showAddAccount({ push: false });
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
    if (window.IndooneAddAccount?.render) {
      await window.IndooneAddAccount.render(subPage);
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
  }

  window.IndooneHome = { init, showAddAccount, backToHome, restoreAccountView };
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init, { once: true });
  else init();
})();
