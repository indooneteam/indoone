(() => {
  const CONTENT_ID = 'content';
  const ADD_HASH = '#home/add-account';
  const ADD_SCRIPT = 'app/home/add-account/script.js?v=20260916a';
  const SCREENSHOT_SCRIPT = 'app/home/screenshot-protection.js?v=20260904b';

  let homeView = null;
  let subPage = null;
  let initialized = false;
  let addFeatureReady = null;
  let accountLoadPromise = null;
  let screenshotFeatureReady = null;

  window.showAdd = () => {
    window.IndooneHome?.showAddAccount?.({
      push: true
    });
  };

  function setupViews() {
    const page = document.getElementById(CONTENT_ID);

    if (!page || initialized) {
      return;
    }

    initialized = true;
    page.dataset.feature = 'home';
    page.classList.add('home-page');

    homeView = document.createElement('div');
    homeView.className = 'home-view';

    while (page.firstChild) {
      homeView.appendChild(page.firstChild);
    }

    page.appendChild(homeView);

    subPage = document.createElement('div');
    subPage.id = 'homeSubPage';
    subPage.className = 'home-sub-page';
    subPage.hidden = true;
    page.appendChild(subPage);

    window.addEventListener('popstate', handleHistory);
    window.addEventListener('pageshow', () => {
      void ensureAccountsLoaded();
      window.IndooneHomeScreenshot?.sync?.();
    });
    document.addEventListener('visibilitychange', () => {
      if (!document.hidden) {
        void ensureAccountsLoaded();
        window.IndooneHomeScreenshot?.sync?.();
      }
    });
  }

  function loadScreenshotFeature() {
    if (window.IndooneHomeScreenshot?.sync) {
      return Promise.resolve();
    }

    if (screenshotFeatureReady) {
      return screenshotFeatureReady;
    }

    screenshotFeatureReady = new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = SCREENSHOT_SCRIPT;
      script.onload = resolve;
      script.onerror = () => {
        reject(new Error('Home screenshot protection feature could not be loaded.'));
      };
      document.head.appendChild(script);
    });

    return screenshotFeatureReady;
  }

  async function ensureAccountsLoaded() {
    if (!homeView || !window.IndooneCloudAccounts?.load) return;
    if (window.location.hash === ADD_HASH) return;

    const user = window.IndooneFirebase?.auth?.currentUser;
    if (!user) return;

    if (accountLoadPromise) return accountLoadPromise;

    accountLoadPromise = (async () => {
      try {
        await window.IndooneCloudAccounts.load();
        if (typeof renderAccounts === 'function') renderAccounts();
        if (typeof refreshAccountCodes === 'function') await refreshAccountCodes();
      } catch (error) {
        console.warn('Indoone Home account load failed:', error);
      } finally {
        accountLoadPromise = null;
      }
    })();

    return accountLoadPromise;
  }

  function loadAddFeature() {
    if (window.IndooneAddAccount?.render) {
      return Promise.resolve();
    }

    if (addFeatureReady) {
      return addFeatureReady;
    }

    addFeatureReady = new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = ADD_SCRIPT;

      script.onload = resolve;
      script.onerror = () => {
        reject(new Error('Add Account feature could not be loaded.'));
      };

      document.head.appendChild(script);
    });

    return addFeatureReady;
  }

  function restoreHome() {
    setupViews();

    if (!homeView) {
      return;
    }

    homeView.hidden = false;

    if (subPage) {
      subPage.hidden = true;
    }

    if (window.location.hash === ADD_HASH) {
      history.replaceState({}, '', window.location.pathname + window.location.search);
    }

    window.IndoonePageState?.set('home');
    document.getElementById('addBtn')?.removeAttribute('hidden');
    document.body.classList.remove('home-subpage-open');

    if (typeof renderAccounts === 'function') {
      renderAccounts();
    }

    if (typeof refreshAccountCodes === 'function') {
      refreshAccountCodes();
    }

    void ensureAccountsLoaded();
    void loadScreenshotFeature().then(() => window.IndooneHomeScreenshot?.sync?.()).catch(() => {});
  }

  function openAddHistory() {
    if (window.location.hash !== ADD_HASH) {
      history.pushState(
        {
          indoonePage: 'add-account'
        },
        '',
        ADD_HASH
      );
    }
  }

  function handleHistory() {
    if (window.location.hash === ADD_HASH) {
      showAddAccount({
        push: false
      });
      return;
    }

    restoreHome();
  }

  async function showAddAccount({ push = true } = {}) {
    setupViews();

    if (!subPage) {
      return;
    }

    window.IndoonePageState?.set('add-account');

    if (push) {
      openAddHistory();
    }

    homeView.hidden = true;
    subPage.hidden = false;
    document.getElementById('addBtn')?.setAttribute('hidden', '');
    document.body.classList.add('home-subpage-open');
    window.IndooneHomeScreenshot?.sync?.();

    try {
      await loadAddFeature();
      await window.IndooneAddAccount.render(subPage);
    } catch (error) {
      subPage.innerHTML = `
        <section class="add-account-page">
          <button
            type="button"
            class="add-account-back"
            data-add-action="back"
          >
            <span class="back-icon">‹</span>
            <span>Back</span>
          </button>
          <h1>Add Account</h1>
          <p>Unable to load this page.</p>
        </section>
      `;

      console.error('Indoone Add Account feature failed:', error);

      subPage
        .querySelector('[data-add-action="back"]')
        ?.addEventListener('click', backToHome, {
          once: true
        });
    }
  }

  function backToHome() {
    if (window.history.state?.indoonePage === 'add-account') {
      history.back();
      return;
    }

    history.replaceState(
      {},
      '',
      window.location.pathname + window.location.search
    );

    restoreHome();
  }

  function init() {
    setupViews();

    void loadScreenshotFeature().then(() => window.IndooneHomeScreenshot?.sync?.()).catch(error => {
      console.warn('Indoone Home screenshot protection unavailable:', error);
    });

    if (window.location.hash === ADD_HASH) {
      showAddAccount({
        push: false
      });
      return;
    }

    void ensureAccountsLoaded();
  }

  window.IndooneHome = {
    init,
    showAddAccount,
    backToHome,
    restoreHome,
    ensureAccountsLoaded
  };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init, {
      once: true
    });
  } else {
    init();
  }
})();
