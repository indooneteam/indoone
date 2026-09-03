// Compatibility bridge: Danger Zone now lives under app/menu/danger-zone/.
window.showDangerZone = function () {
  if (typeof window.openMenuFeature === 'function') return window.openMenuFeature('danger-zone');
  toast('Danger Zone is unavailable.');
};
