(() => {
  const MARKUP_URL = 'app/home/add-account/index.html?v=20260916a';
  const STYLE_URL = 'app/home/add-account/style.css?v=20260916a';

  function loadStyle() {
    if (document.querySelector(`link[href^="${STYLE_URL.split('?')[0]}"]`)) {
      return;
    }

    const link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = STYLE_URL;
    document.head.appendChild(link);
  }

  async function loadMarkup() {
    const response = await fetch(MARKUP_URL, {
      cache: 'no-store'
    });

    if (!response.ok) {
      throw new Error('Add Account page could not be loaded.');
    }

    return response.text();
  }

  function importOtpAuth(root) {
    const input = root.querySelector('#homeOtpUri');
    const value = input?.value.trim();

    if (!value) {
      toast('Enter an otpauth URI');
      return;
    }

    try {
      const parsed = TOTP.parseOtpAuth(value);

      window.showManual?.({
        name: parsed.issuer || parsed.label || 'Account',
        email: parsed.label || '',
        secret: parsed.secret,
        digits: parsed.digits,
        period: parsed.period,
        algorithm: parsed.algorithm
      });

      toast('OTP URI imported');
    } catch (_) {
      toast('Invalid TOTP QR data');
    }
  }

  function bind(root) {
    root.querySelectorAll('[data-add-action]').forEach(button => {
      if (button.dataset.bound === 'true') {
        return;
      }

      button.dataset.bound = 'true';

      button.addEventListener('click', event => {
        event.preventDefault();

        const action = button.dataset.addAction;

        if (action === 'back') {
          window.IndooneHome?.backToHome();
          return;
        }

        if (action === 'scan') {
          window.IndooneQrScanner?.start();
          return;
        }

        if (action === 'manual') {
          window.showManual?.();
          return;
        }

        if (action === 'import') {
          importOtpAuth(root);
        }
      });
    });
  }

  async function render(mount) {
    loadStyle();

    try {
      mount.innerHTML = await loadMarkup();
    } catch (error) {
      mount.innerHTML = `
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
          <p>Unable to load this page. Please try again.</p>
        </section>
      `;

      console.error('Indoone Add Account load failed:', error);
    }

    bind(mount);
  }

  window.IndooneAddAccount = {
    render
  };
})();
