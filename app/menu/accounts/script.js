(() => {
  function goToAccounts() {
    window.closeModal?.();
    window.closeDrawer?.();
    document.getElementById('accountsNav')?.click();
    return false;
  }

  window.initMenuAccounts = function () {
    const button = document.getElementById('menuAccountsOpen');
    button?.addEventListener('click', goToAccounts);
    return false;
  };
})();
