(() => {
  const LOBBY_ICON_PATH = 'app/lobby/icon.svg';

  function createLobbyIcon(className = 'lobby-nav-icon') {
    const icon = document.createElement('span');
    icon.className = className;
    icon.setAttribute('aria-hidden', 'true');
    icon.setAttribute('data-lobby-icon', LOBBY_ICON_PATH);
    return icon;
  }

  function applyLobbyNavIcon() {
    const button = document.getElementById('lobbyNav');
    if (!button) return;

    const current = button.querySelector('[data-lobby-icon]');
    if (current) return;

    const existing = button.querySelector('.nav-icon');
    if (existing) existing.replaceWith(createLobbyIcon());
    else button.insertBefore(createLobbyIcon(), button.firstChild);
  }

  function init() {
    applyLobbyNavIcon();
  }

  window.IndooneLobby = {
    init,
    iconPath: LOBBY_ICON_PATH
  };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init, { once: true });
  } else {
    init();
  }
})();
