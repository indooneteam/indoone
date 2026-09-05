// Single source of truth for the four bottom-navigation SVG icons and Lobby status icon.
(() => {
  const ICONS = {
    accounts: [
      '<circle cx="9" cy="8" r="3"></circle>',
      '<path d="M4.5 19c.6-3 2.1-4.5 4.5-4.5s3.9 1.5 4.5 4.5"></path>',
      '<circle cx="16" cy="9" r="2.5"></circle>',
      '<path d="M13.5 18.5c.5-2.2 1.7-3.4 3.5-3.4 1.7 0 2.9 1.2 3.5 3.4"></path>'
    ].join(''),
    lobby: [
      '<path d="M12 3.5 20.5 12 12 20.5 3.5 12 12 3.5Z"></path>',
      '<circle cx="12" cy="12" r="2.4"></circle>'
    ].join(''),
    connect:
      '<path d="M5 12h14M8 7l-5 5 5 5M16 7l5 5-5 5"></path>',
    settings: [
      '<path d="M5 7h14M5 17h14"></path>',
      '<circle cx="10" cy="7" r="2"></circle>',
      '<circle cx="15" cy="17" r="2"></circle>'
    ].join('')
  };

  function makeIcon(type, className) {
    const svg = document.createElementNS(
      'http://www.w3.org/2000/svg',
      'svg'
    );

    svg.setAttribute('class', className);
    svg.setAttribute('data-shared-nav-icon', type);
    svg.setAttribute('viewBox', '0 0 24 24');
    svg.setAttribute('aria-hidden', 'true');
    svg.setAttribute('focusable', 'false');
    svg.setAttribute('fill', 'none');
    svg.setAttribute('stroke', 'currentColor');
    svg.setAttribute('stroke-width', '1.8');
    svg.setAttribute('stroke-linecap', 'round');
    svg.setAttribute('stroke-linejoin', 'round');
    svg.innerHTML = ICONS[type];

    return svg;
  }

  function apply() {
    const navMap = {
      accountsNav: 'accounts',
      lobbyNav: 'lobby',
      connectNav: 'connect',
      settingsNav: 'settings'
    };

    Object.entries(navMap).forEach(([id, type]) => {
      const button = document.getElementById(id);
      if (!button) return;

      const current = button.querySelector(
        '[data-shared-nav-icon]'
      );

      if (!current) {
        button
          .querySelectorAll('.nav-icon')
          .forEach(node => node.remove());

        button.insertBefore(
          makeIcon(type, 'nav-icon'),
          button.firstChild
        );
      }
    });

    document
      .querySelectorAll('.lobby-status-icon')
      .forEach(el => {
        if (
          el.querySelector(
            '[data-shared-nav-icon="lobby"]'
          )
        ) {
          return;
        }

        el.textContent = '';
        el.appendChild(
          makeIcon('lobby', 'ui-svg-icon lobby-svg-icon')
        );
      });
  }

  window.IndooneNavigationIcons = {
    apply,
    icons: ICONS
  };

  if (document.readyState === 'loading') {
    document.addEventListener(
      'DOMContentLoaded',
      apply,
      { once: true }
    );
  } else {
    apply();
  }

  if (typeof MutationObserver !== 'undefined') {
    let queued = false;

    const observer = new MutationObserver(() => {
      if (queued) return;

      queued = true;
      queueMicrotask(() => {
        queued = false;
        apply();
      });
    });

    observer.observe(document.body, {
      childList: true,
      subtree: true
    });
  }

  // Re-clicking the currently active bottom-navigation tab refreshes only
  // that section. It does not create a new page/history state.
  document.addEventListener(
    'click',
    async event => {
      const button = event.target.closest(
        '#accountsNav, #lobbyNav, #connectNav, #settingsNav'
      );

      if (!button || !button.classList.contains('active')) {
        return;
      }

      event.preventDefault();
      event.stopImmediatePropagation();

      try {
        switch (button.id) {
          case 'accountsNav':
            if (window.IndooneCloudAccounts?.load) {
              await window.IndooneCloudAccounts.load();
            }

            if (window.refreshAccountCodes) {
              await window.refreshAccountCodes();
            }
            break;

          case 'lobbyNav':
            await window.showLobby?.({
              remember: false,
              refresh: true
            });
            break;

          case 'connectNav':
            await window.showConnect?.({
              remember: false,
              refresh: true
            });
            break;

          case 'settingsNav':
            await window.showSettings?.({
              remember: false,
              refresh: true
            });
            break;
        }
      } catch (error) {
        console.warn(
          'Indoone navigation refresh failed:',
          error
        );
      }
    },
    true
  );
})();
