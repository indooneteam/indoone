// The setup screen both platforms share, as
// android/connect/integrations/IntegrationSetupScaffold.kt draws it.
//
// A BACK button and the platform's name across the top, then the platform's
// title and description, an ACCOUNT BOUNDARY card, the FLOW with a numbered
// card per step, a PLATFORM RULE card, a SECURITY BOUNDARY card, and a closing
// line about the Meta authorization action.
//
// The two lines that are the same for every platform live here, not in the
// platform data, because that is where the app keeps them.

(() => {
  const ACCOUNT_NOTE =
    'Each connection belongs to the signed-in Indoone user and is resolved again on the backend before any automation runs.';

  const SECURITY_NOTE =
    'Provider access tokens stay on the backend. The Android app keeps only Indoone session state and the integration UI.';

  const FOOT_NOTE =
    'The Meta authorization action will plug into IntegrationApi.startConnection(...) without changing the Connect screen or platform-specific UI.';

  const escape = value =>
    String(value || '').replace(
      /[&<>"']/g,
      char =>
        ({
          '&': '&amp;',
          '<': '&lt;',
          '>': '&gt;',
          '"': '&quot;',
          "'": '&#39;'
        })[char]
    );

  function render(key) {
    const host = document.getElementById('connectSetupPage');
    const platform = window.IndooneIntegrationPlatforms?.[key];

    if (!host || !platform) {
      return;
    }

    const steps = platform.steps
      .map(
        (step, index) => `
          <div class="connect-step">
            <span class="connect-step-number">${index + 1}</span>
            <span class="connect-step-text">${escape(step)}</span>
          </div>
        `
      )
      .join('');

    host.innerHTML = `
      <div class="connect-setup-bar">
        <button
          type="button"
          class="connect-setup-back"
          data-connect-action="home"
        >
          BACK
        </button>
        <span class="connect-setup-bar-title">${escape(platform.title)}</span>
      </div>

      <p class="connect-setup-eyebrow">BUSINESS INTEGRATION ARCHITECTURE</p>
      <h1 class="connect-setup-title">${escape(platform.title)}</h1>
      <p class="connect-setup-desc">${escape(platform.description)}</p>

      <div class="connect-setup-card">
        <p class="connect-setup-label">ACCOUNT BOUNDARY</p>
        <p class="connect-setup-value">${escape(platform.accountLabel)}</p>
        <p class="connect-setup-note">${escape(ACCOUNT_NOTE)}</p>
      </div>

      <p class="connect-setup-flow">FLOW</p>

      ${steps}

      <div class="connect-setup-card connect-setup-card-rule">
        <p class="connect-setup-label connect-setup-label-accent">PLATFORM RULE</p>
        <p class="connect-setup-rule">${escape(platform.note)}</p>
      </div>

      <div class="connect-setup-card connect-setup-card-plain">
        <p class="connect-setup-label">SECURITY BOUNDARY</p>
        <p class="connect-setup-note">${escape(SECURITY_NOTE)}</p>
      </div>

      <p class="connect-setup-foot">${escape(FOOT_NOTE)}</p>
    `;
  }

  window.IndooneIntegrations = {
    platforms: window.IndooneIntegrationPlatforms,
    render
  };
})();
