window.focusSearch = function () {
  document.getElementById('search')?.focus();
};

window.filterAccounts = function () {
  const q = document.getElementById('search').value.toLowerCase().trim();
  document.querySelectorAll('.account').forEach(account => {
    account.style.display = account.dataset.name.toLowerCase().includes(q) ? 'flex' : 'none';
  });
};
