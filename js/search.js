window.focusSearch = function () {
  document.getElementById('search')?.focus();
};

window.filterAccounts = function () {
  const input = document.getElementById('search');
  if (!input) return;
  indooneState.search = input.value.toLowerCase().trim();
  if (typeof renderAccounts === 'function') renderAccounts();
};

window.clearSearchAccounts = function () {
  const input = document.getElementById('search');
  const wrap = document.getElementById('searchWrap');
  if (!input) return;
  input.value = '';
  indooneState.search = '';
  if (typeof renderAccounts === 'function') renderAccounts();
  input.focus();
  if (wrap) wrap.hidden = false;
};

function bindSearchControls() {
  const input = document.getElementById('search');
  const clear = document.getElementById('clearSearch');
  if (!input || !clear || input.dataset.indooneSearchBound === 'true') return;

  input.dataset.indooneSearchBound = 'true';
  input.addEventListener('input', filterAccounts);
  input.addEventListener('keydown', event => {
    if (event.key === 'Enter') {
      event.preventDefault();
      filterAccounts();
    }
  });
  clear.addEventListener('click', event => {
    event.preventDefault();
    event.stopPropagation();
    clearSearchAccounts();
  });
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', bindSearchControls, { once: true });
} else {
  bindSearchControls();
}
