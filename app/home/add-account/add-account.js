(() => {
  async function loadMarkup() {
    const response = await fetch('app/home/add-account/add-account.html?v=20260915b', { cache: 'no-store' });
    if (!response.ok) throw new Error('Add Account page could not be loaded.');
    return response.text();
  }

  function bind(root) {
    root.querySelectorAll('[data-add-action]').forEach(button => {
      if (button.dataset.bound === 'true') return;
      button.dataset.bound = 'true';
      button.addEventListener('click', event => {
        event.preventDefault();
        const action = button.dataset.addAction;
        if (action === 'back') return window.IndooneHome?.backToHome();
        if (action === 'scan') return window.IndooneQrScanner?.start();
        if (action === 'manual') return window.showManual?.();
        if (action === 'import') {
          const input = root.querySelector('#homeOtpUri');
          const value = input?.value.trim();
          if (!value) return toast('Enter an otpauth URI');
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
      });
    });
  }

  async function render(mount) {
    try {
      mount.innerHTML = await loadMarkup();
    } catch (error) {
      mount.innerHTML = '<section class="add-account-page"><button type="button" class="add-account-back" data-add-action="back"><span class="back-icon">‹</span><span>Back</span></button><h1>Add Account</h1><p>Unable to load this page. Please try again.</p></section>';
      console.error('Indoone Add Account load failed:', error);
    }
    bind(mount);
  }

  window.IndooneAddAccount = { render };
})();
