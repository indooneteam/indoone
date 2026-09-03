// Compatibility bridge: Logout now lives under app/menu/logout/.
window.showLogoutOptions = function () {
  if (typeof window.openMenuFeature === 'function') return window.openMenuFeature('logout');
  toast('Logout options are unavailable.');
};
