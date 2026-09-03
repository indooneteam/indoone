window.focusSearch = function () {
  const input = document.getElementById('search');

  if (!input) {
    return;
  }

  input.focus();
};

window.filterAccounts = function () {
  const input = document.getElementById('search');

  if (!input) {
    return;
  }

  const value = input.value.toLowerCase().trim();
  window.indooneState.search = value;

  if (typeof window.renderAccounts === 'function') {
    window.renderAccounts();
  }
};

window.clearSearchAccounts = function () {
  const input = document.getElementById('search');
  const wrap = document.getElementById('searchWrap');

  if (!input) {
    return;
  }

  input.value = '';
  window.indooneState.search = '';

  if (typeof window.renderAccounts === 'function') {
    window.renderAccounts();
  }

  input.focus();

  if (wrap) {
    wrap.hidden = false;
  }
};

function bindSearchControls() {
  let input = document.getElementById('search');
  let clear = document.getElementById('clearSearch');

  if (!input || !clear || input.dataset.indooneSearchBound === 'true') {
    return;
  }

  input = input.cloneNode(true);
  clear = clear.cloneNode(true);

  document.getElementById('search')?.replaceWith(input);
  document.getElementById('clearSearch')?.replaceWith(clear);

  input.dataset.indooneSearchBound = 'true';
  input.setAttribute('type', 'text');

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

  filterAccounts();
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', bindSearchControls, {
    once: true
  });
} else {
  bindSearchControls();
}
