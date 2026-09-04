(() => {
  function showLobbyStatus() {
    if (typeof window.openModal !== 'function') return;
    window.openModal(`
      <section class="lobby-status-page" aria-label="Lobby">
        <div class="lobby-status-icon" aria-hidden="true"><span class="lobby-status-icon-source"></span></div>
        <p class="eyebrow">INDOONE LOBBY</p>
        <h2>Lobby is under development</h2>
        <p>Lobby features are being built now. This area will be available in a future update.</p>
        <button type="button" class="primary" data-close>Done</button>
      </section>
    `);
  }

  window.showLobby = showLobbyStatus;
})();
