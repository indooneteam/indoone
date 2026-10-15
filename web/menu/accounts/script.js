(() => {
  function goToAccounts() {
    window.closeModal?.();
    window.closeDrawer?.();
    // the accounts page, not the Home tab: Home is the chat page now, so
    // clicking it put this row on the wrong screen
    window.showAccounts?.({ remember: true });
    return false;
  }

  window.initMenuAccounts = function () {
    const button = document.getElementById('menuAccountsOpen');
    button?.addEventListener('click', goToAccounts);
    return false;
  };
})();