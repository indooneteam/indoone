// Single source of truth for the four bottom-navigation SVG icons and Lobby status icon.
(() => {
  const ICONS = {
    accounts: '<rect x="4" y="4" width="6" height="6" rx="1.5"></rect><rect x="14" y="4" width="6" height="6" rx="1.5"></rect><rect x="4" y="14" width="6" height="6" rx="1.5"></rect><rect x="14" y="14" width="6" height="6" rx="1.5"></rect>',
    lobby: '<path d="M12 3.5 20.5 12 12 20.5 3.5 12 12 3.5Z"></path><circle cx="12" cy="12" r="2.4"></circle>',
    connect: '<path d="M5 12h14M8 7l-5 5 5 5M16 7l5 5-5 5"></path>',
    settings: '<path d="M5 7h14M5 17h14"></path><circle cx="10" cy="7" r="2"></circle><circle cx="15" cy="17" r="2"></circle>'
  };

  function makeIcon(type, className) {
    const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    svg.setAttribute('class', className);
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
    const navMap = { accountsNav: 'accounts', lobbyNav: 'lobby', connectNav: 'connect', settingsNav: 'settings' };
    Object.entries(navMap).forEach(([id, type]) => {
      const button = document.getElementById(id);
      if (!button) return;
      button.querySelectorAll('.nav-icon').forEach(node => node.remove());
      button.insertBefore(makeIcon(type, 'nav-icon'), button.firstChild);
    });

    document.querySelectorAll('.lobby-status-icon').forEach(el => {
      el.textContent = '';
      el.appendChild(makeIcon('lobby', 'ui-svg-icon lobby-svg-icon'));
    });
  }

  window.IndooneNavigationIcons = { apply, icons: ICONS };
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', apply, { once: true });
  else apply();

  if (typeof MutationObserver !== 'undefined') {
    let queued = false;
    const observer = new MutationObserver(() => {
      if (queued) return;
      queued = true;
      queueMicrotask(() => { queued = false; apply(); });
    });
    observer.observe(document.body, { childList: true, subtree: true });
  }
})();
