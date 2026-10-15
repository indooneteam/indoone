// The Connect screen, as android/connect/ConnectScreen.kt draws it.
//
// The app's screen is BUSINESS INTEGRATIONS: a heading, a balance card, and
// five platform cards. WhatsApp and Instagram open a setup screen of their own;
// App, Website and Telegram open a dialog. The balance card's ADD CREDITS opens
// the credits dialog.
//
// What was here before was a different feature entirely - nearby device
// sharing, with Pair / Connect / Devices. The app has no such thing.

(() => {
  const get = id => document.getElementById(id);

  // ConnectState.kt
  const STATE = {
    balance: '₹0.00',
    usage: '0',
    usageLimit: '10,000'
  };

  // The app names the three that open a dialog, and the two that open a screen.
  const SCREENS = { whatsapp: 'whatsapp', instagram: 'instagram' };

  const DIALOG_LINE =
    'Connect this service to Indoone AI to automate customer conversations and keep replies on the same channel.';

  const CREDIT_PACKAGES = [
    { text: '₹99  •  1,000 credits', highlighted: false },
    { text: '₹499 •  5,500 credits', highlighted: true },
    { text: '₹999 •  12,000 credits', highlighted: false }
  ];

  const CREDIT_NOTE =
    'Prepaid balance for automation and AI usage. Choose a package to continue.';

  function paintBalance() {
    const value = get('connectBalance');
    const usage = get('connectUsage');

    if (value) {
      value.textContent = STATE.balance;
    }

    if (usage) {
      usage.textContent = `${STATE.usage} / ${STATE.usageLimit}`;
    }
  }

  function setActive(tab) {
    get('homeNav')?.classList.toggle('active', tab === 'accounts');
    get('lobbyNav')?.classList.toggle('active', tab === 'lobby');
    get('connectNav')?.classList.toggle('active', tab === 'connect');
    get('settingsNav')?.classList.toggle('active', tab === 'settings');
  }

  // -------------------------------------------------------------- the dialogs

  function openIntegrationDialog(name) {
    window.openModal?.(`
      <section class="connect-dialog" aria-label="${name}">
        <h2 class="connect-dialog-title">${name}</h2>
        <p class="connect-dialog-text">${DIALOG_LINE}</p>
        <div class="connect-dialog-actions">
          <button type="button" class="connect-dialog-plain" data-close>
            CANCEL
          </button>
          <button type="button" class="connect-dialog-plain" data-close>
            SETUP
          </button>
        </div>
      </section>
    `);
  }

  function openCreditsDialog() {
    const options = CREDIT_PACKAGES.map(
      pkg => `
        <div class="connect-credit${pkg.highlighted ? ' connect-credit-on' : ''}">
          ${pkg.text}
        </div>
      `
    ).join('');

    window.openModal?.(`
      <section class="connect-dialog" aria-label="Add Indoone Credits">
        <h2 class="connect-dialog-title">Add Indoone Credits</h2>
        <p class="connect-dialog-note">${CREDIT_NOTE}</p>
        <div class="connect-credit-list">${options}</div>
        <div class="connect-dialog-actions">
          <button type="button" class="connect-dialog-plain" data-close>
            CANCEL
          </button>
          <button type="button" class="connect-dialog-solid" data-close>
            CONTINUE TO PAYMENT
          </button>
        </div>
      </section>
    `);
  }

  // ------------------------------------------------------------- the switching

  function setConnectPage(page) {
    get('connectHomePage')?.toggleAttribute('hidden', page !== 'home');
    get('connectSetupPage')?.toggleAttribute('hidden', page !== 'setup');
  }

  function showConnectContent() {
    get('content')?.setAttribute('hidden', '');
    get('addBtn')?.setAttribute('hidden', '');
    get('searchWrap')?.setAttribute('hidden', '');
    get('homeContent')?.setAttribute('hidden', '');
    get('lobbyContent')?.setAttribute('hidden', '');
    get('connectContent')?.removeAttribute('hidden');
    paintBalance();
  }

  function showConnectHome({ remember = true } = {}) {
    if (remember) {
      window.IndoonePageState?.set('connect');
    }

    setConnectPage('home');
    paintBalance();
  }

  function openConnect() {
    window.closeModal?.();
    window.closeDrawer?.();
    showConnectContent();
    showConnectHome({ remember: false });
    setActive('connect');
  }

  // ----------------------------------------------------------- what each card does

  function openSetup(key) {
    const platform = window.IndooneIntegrations?.platforms?.[key];

    if (!platform) {
      return;
    }

    window.closeModal?.();
    showConnectContent();
    setConnectPage('setup');
    setActive('connect');
    window.IndooneIntegrations?.render?.(key);
  }

  window.showConnect = function ({ remember = true } = {}) {
    if (remember) {
      window.IndoonePageState?.set('connect');
    }

    openConnect();
  };

  window.showConnectHome = function ({ remember = true } = {}) {
    if (remember) {
      window.IndoonePageState?.set('connect');
    }

    openConnect();
  };

  document.addEventListener('click', event => {
    const action = event.target.closest('[data-connect-action]');

    if (!action) {
      return;
    }

    const name = action.dataset.connectAction;

    if (SCREENS[name]) {
      event.preventDefault();
      openSetup(SCREENS[name]);
      return;
    }

    if (name === 'credits') {
      event.preventDefault();
      openCreditsDialog();
      return;
    }

    if (name === 'home') {
      event.preventDefault();
      window.showConnectHome?.();
      return;
    }

    if (name === 'app' || name === 'website' || name === 'telegram') {
      event.preventDefault();
      openIntegrationDialog(
        action.querySelector('.connect-card-title')?.textContent || name
      );
    }
  });

  get('connectNav')?.addEventListener('click', event => {
    event.preventDefault();
    window.showConnect();
  });

  paintBalance();
})();
