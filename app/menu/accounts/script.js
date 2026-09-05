window.initMenuAccounts = function () {
  closeDrawer();
  document.getElementById('overlay')?.classList.add('hidden');
  document.getElementById('accountsNav')?.click();
  return false;
};
