(() => {
  const MENU_MARKUP_URL = 'app/home/add-account/index.html?v=20260903c';
  const MENU_STYLE_URL = 'app/home/add-account/style.css?v=20260903c';

  const FEATURES = {
    qr: {
      script: 'app/home/add-account/qr/script.js?v=20260903b',
      globalName: 'IndooneAddAccountQr'
    },
    manual: {
      script: 'app/home/add-account/manual/script.js?v=20260903b',
      globalName: 'IndooneAddAccountManual'
    },
    import: {
      script: 'app/home/add-account/import/script.js?v=20260903b',
      globalName: 'IndooneAddAccountImport'
    }
  };

  const featurePromises = {};
  let menuMarkup = null;

  function loadStyle() {
    const baseUrl = MENU_STYLE_URL.split('?')[0];
    if (document.querySelector(`link[href^="${baseUrl}"]`)) return;

    const link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = MENU_STYLE_URL;
    document.head.appendChild(link);
  }

  async function loadMenuMarkup() {
    if (menuMarkup !== null) return menuMarkup;

    const response = await fetch(MENU_MARKUP_URL, { cache: 'no-store' });
    if (!response.ok) throw new Error('Add Account menu could not be loaded.');

    menuMarkup = await response.text();
    return menuMarkup;
  }

  function loadFeature(name) {
    const feature = FEATURES[name];
    if (!feature) return Promise.reject(new Error(`Unknown Add Account feature: ${name}`));

    if (window[feature.globalName]?.render) {
      return Promise.resolve(window[feature.globalName]);
    }

    if (featurePromises[name]) return featurePromises[name];

    featurePromises[name] = new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = feature.script;

      script.onload = () => {
        const loaded = window[feature.globalName];
        if (!loaded?.render) {
          reject(new Error(`${name} feature did not initialize.`));
          return;
        }
        resolve(loaded);
      };

      script.onerror = () => {
        reject(new Error(`${name} feature could not be loaded.`));
      };

      document.head.appendChild(script);
    });

    return featurePromises[name];
  }

  function baseHash() {
    return '#home/add-account';
  }

  function featureHash(name) {
    return `${baseHash()}/${name}`;
  }

  function pushHash(hash, stateName) {
    if (window.location.hash === hash) return;
    history.pushState({ indoonePage: stateName }, '', hash);
  }

  function backToHome() {
    if (window.location.hash.startsWith(baseHash())) {
      history.back();
      return;
    }
    window.IndooneHome?.backToHome();
  }

  function bindMenu(root) {
    root.querySelectorAll('[data-add-action]').forEach(button => {
      button.addEventListener('click', event => {
        event.preventDefault();

        const action = button.dataset.addAction;

        if (action === 'back') {
          backToHome();
          return;
        }

        if (FEATURES[action]) {
          showFeature(action, { push: true });
        }
      });
    });
  }

  async function showMenu({ push = false } = {}) {
    const mount = document.getElementById('homeSubPage');
    if (!mount) return;

    window.IndooneAddAccountQr?.stop?.();

    if (push) {
      pushHash(baseHash(), 'add-account');
    }

    loadStyle();

    try {
      mount.innerHTML = await loadMenuMarkup();
      bindMenu(mount);
    } catch (error) {
      mount.innerHTML = `
        <section class="add-account-page">
          <button type="button" class="add-account-back" data-add-action="back">
            <span class="back-icon" aria-hidden="true">‹</span>
            <span>Back</span>
          </button>
          <h1>Add Account</h1>
          <p>Unable to load this page. Please try again.</p>
        </section>
      `;
      bindMenu(mount);
      console.error('Indoone Add Account menu failed:', error);
    }
  }

  async function showFeature(name, options = {}) {
    const mount = document.getElementById('homeSubPage');
    if (!mount) return;

    if (options.push !== false) {
      pushHash(featureHash(name), name);
    }

    if (name !== 'qr') {
      window.IndooneAddAccountQr?.stop?.();
    }

    try {
      const feature = await loadFeature(name);
      await feature.render(mount, options.renderOptions || {});
    } catch (error) {
      mount.innerHTML = `
        <section class="add-account-page">
          <button type="button" class="add-account-back" data-add-action="back">
            <span class="back-icon" aria-hidden="true">‹</span>
            <span>Back</span>
          </button>
          <h1>Add Account</h1>
          <p>Unable to load this feature.</p>
        </section>
      `;
      mount.querySelector('[data-add-action="back"]')?.addEventListener('click', backToHome, { once: true });
      console.error(`Indoone Add Account ${name} feature failed:`, error);
    }
  }

  function handleHistory() {
    const hash = window.location.hash;
    const prefix = `${baseHash()}/`;

    if (hash === baseHash()) {
      showMenu({ push: false });
      return;
    }

    if (!hash.startsWith(prefix)) {
      return;
    }

    const featureName = hash.slice(prefix.length).split('/')[0];

    if (FEATURES[featureName]) {
      showFeature(featureName, { push: false });
    } else {
      showMenu({ push: false });
    }
  }

  async function render(mount) {
    if (!mount) return;

    mount.id = 'homeSubPage';
    mount.classList.add('home-sub-page');
    mount.hidden = false;

    if (window.location.hash === baseHash()) {
      await showMenu({ push: false });
      return;
    }

    const prefix = `${baseHash()}/`;
    if (window.location.hash.startsWith(prefix)) {
      handleHistory();
      return;
    }

    await showMenu({ push: false });
  }

  window.IndooneAddAccount = {
    render,
    showMenu,
    showFeature,
    showManual(options = {}) {
      showFeature('manual', {
        push: options.push !== false,
        renderOptions: {
          id: options.id || 0,
          prefill: options.prefill || {}
        }
      });
    },
    backToHome,
    handleHistory
  };

  window.addEventListener('popstate', handleHistory);
  window.addEventListener('hashchange', handleHistory);
})();
