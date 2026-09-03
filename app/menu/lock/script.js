window.initMenuLock = function () {
  closeDrawer();
  closeModal();
  if (typeof lockIndoone === 'function') lockIndoone();
  else toast('Lock is unavailable.');
};