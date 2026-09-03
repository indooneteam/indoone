(() => {
  function show() {
    const wrap = document.getElementById('searchWrap');
    const input = document.getElementById('search');

    if (!wrap || !input) {
      return;
    }

    wrap.hidden = false;
    input.focus();
  }

  function clear() {
    const input = document.getElementById('search');

    if (!input) {
      return;
    }

    input.value = '';
    window.indooneState.search = '';

    if (typeof window.renderAccounts === 'function') {
      window.renderAccounts();
    }

    input.focus();
  }

  window.IndooneHomeSearch = {
    show,
    clear
  };
})();
