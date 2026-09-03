(() => {
  const MARKUP_URL = 'app/home/add-account/manual/index.html?v=20260903b';
  const STYLE_URL = 'app/home/add-account/manual/style.css?v=20260903b';

  function loadStyle() {
    const baseUrl = STYLE_URL.split('?')[0];
    if (document.querySelector(`link[href^="${baseUrl}"]`)) return;
    const link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = STYLE_URL;
    document.head.appendChild(link);
  }

  async function loadMarkup() {
    const response = await fetch(MARKUP_URL, { cache: 'no-store' });
    if (!response.ok) throw new Error('Manual page could not be loaded.');
    return response.text();
  }

  function setValue(id, value) {
    const field = document.getElementById(id);
    if (field) field.value = value ?? '';
  }

  async function save(event, options) {
    event.preventDefault();

    const name = document.getElementById('accountName')?.value.trim();
    const email = document.getElementById('accountUser')?.value.trim();
    const secret = document
      .getElementById('secretKey')?.value.trim()
      .replace(/[\s-]/g, '')
      .toUpperCase();
    const digits = Number(document.getElementById('accountDigits')?.value || 6);
    const period = Number(document.getElementById('accountPeriod')?.value || 30);
    const algorithm = document.getElementById('accountAlgorithm')?.value || 'SHA1';

    if (!name || !secret) {
      toast('Enter account name and secret key');
      return;
    }

    if (!window.IndooneFirebase?.auth?.currentUser) {
      toast('Please login first');
      return;
    }

    if (!window.IndooneCloudAccounts) {
      toast('Cloud storage is unavailable');
      return;
    }

    try {
      TOTP.base32ToBytes(secret);
      await TOTP.generate(
        secret,
        Math.floor(Date.now() / 1000 / period),
        digits,
        algorithm
      );
    } catch (_) {
      toast('Invalid TOTP secret');
      return;
    }

    try {
      if (options.id) {
        const account = indooneState.accounts.find(item => item.id === options.id);
        if (!account) {
          toast('Account not found');
          return;
        }

        Object.assign(account, {
          name,
          email,
          secret,
          digits,
          period,
          algorithm,
          updatedAt: Date.now()
        });

        await IndooneCloudAccounts.save(account);
        await IndooneTotpController.refreshAll();
        window.IndooneAddAccount?.goHome?.();
        toast('Account updated and synced');
        return;
      }

      const prefill = options.prefill || {};
      const zoho = window.IndooneZoho?.detect(
        prefill.provider || '',
        prefill.email || email
      ) || null;

      const account = {
        id: Date.now(),
        name,
        email,
        secret,
        digits,
        period,
        algorithm,
        provider: prefill.provider || zoho?.provider || '',
        service: prefill.service || zoho?.service || '',
        favorite: false,
        icon: name.charAt(0).toUpperCase(),
        cls: prefill.provider === 'Zoho' || zoho ? 'zoho' : 'google',
        updatedAt: Date.now(),
        seconds: period,
        code: '------'
      };

      await IndooneCloudAccounts.save(account);
      indooneState.accounts.push(account);
      await IndooneTotpController.refreshAll();
      window.IndooneAddAccount?.goHome?.();
      toast('Account saved to Firebase');
    } catch (error) {
      toast(error?.message || 'Could not save account');
    }
  }

  function bind(root, options) {
    root.querySelector('[data-manual-action="back"]')?.addEventListener('click', event => {
      event.preventDefault();
      window.IndooneAddAccount?.showMenu?.({ push: true });
    });

    root.querySelector('#manualAccountForm')?.addEventListener('submit', event => {
      save(event, options);
    });
  }

  async function render(mount, options = {}) {
    loadStyle();

    try {
      mount.innerHTML = await loadMarkup();

      const prefill = options.prefill || {};
      setValue('accountName', prefill.name || '');
      setValue('accountUser', prefill.email || '');
      setValue('secretKey', prefill.secret || '');
      setValue('accountDigits', String(prefill.digits || 6));
      setValue('accountPeriod', String(prefill.period || 30));
      setValue('accountAlgorithm', String(prefill.algorithm || 'SHA1'));

      const title = document.getElementById('manualAccountTitle');
      const button = document.getElementById('saveAccountButton');
      const id = Number(options.id || 0);

      if (title) title.textContent = id ? 'Edit Account' : 'Enter Setup Key';
      if (button) button.textContent = id ? 'Save Changes' : 'Save Account';

      bind(mount, { id, prefill });
    } catch (error) {
      mount.innerHTML = `
        <section class="add-account-child manual-page">
          <button type="button" class="page-back" data-manual-action="back">
            <span aria-hidden="true">‹</span>
            <span>Back</span>
          </button>
          <h1>Enter Setup Key</h1>
          <p>Unable to load the manual setup page.</p>
        </section>
      `;
      bind(mount, options);
      console.error('Indoone Add Account Manual feature failed:', error);
    }
  }

  window.IndooneAddAccountManual = { render };
})();
