// The Lobby's words, and the painting of them.
//
// The counterpart of android/lobby/LobbyState.kt and LobbyViewModel.kt, which
// between them hold exactly these two lines and nothing else. The screen
// itself - the top bar, the centred column, the bottom navigation - lives in
// index.html, as the app's lives in LobbyScreen.kt.

(() => {
  // LobbyState.kt, word for word.
  const STATE = {
    title: 'Lobby is under development',
    message:
      'Lobby features are being built now. This area will be available in a future update.'
  };

  function paint() {
    const title = document.getElementById('lobbyTitle');
    const message = document.getElementById('lobbyMessage');

    if (title) {
      title.textContent = STATE.title;
    }

    if (message) {
      message.textContent = STATE.message;
    }
  }

  window.IndooneLobby = {
    state: STATE,
    paint,
    // LobbyViewModel exposes its state as a flow the screen reads; this is the
    // same thing in the web's terms.
    getState: () => ({ ...STATE })
  };

  paint();
})();
