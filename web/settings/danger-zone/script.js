// The web counterpart of android/settings/danger-zone/DangerZoneScreen.kt.
//
// The app draws one row and one note:
//
//   DangerRow("Delete Indoone account",
//             "Permanently delete your Indoone account, cloud data and local files",
//             onDeleteAccount)
//
//   "This permanently removes your Indoone account, your cloud data, and
//    everything Indoone keeps on this device. Continue only when you are sure."
//
// The web used to draw a second row, "Delete local data", which the app has
// never had. It is gone, and the note and the subtitle are now the app's own
// words. The markup here has to match index.html - the loader fetches the
// fragment first, so this only runs when the fragment could not be fetched.
window.initMenuDangerZone = function () {
  const modal = document.getElementById('modal');
  modal.innerHTML =
    '<div class="modal-head">' +
    '<h2>' +
    'Danger Zone' +
    '</h2>' +
    '<button class="close-btn" data-close>' +
    '×' +
    '</button>' +
    '</div>' +
    '<p class="menu-feature-note">' +
    'This permanently removes your Indoone account, your cloud data, and ' +
    'everything Indoone keeps on this device. Continue only when you are sure.' +
    '</p>' +
    '<div class="menu-feature-list">' +
    '<button type="button" class="settings-row danger menu-feature-row" ' +
    'data-open-nested="danger-zone/delete-account">' +
    '<span>' +
    'Delete Indoone account' +
    '<small>' +
    'Permanently delete your Indoone account, cloud data and local files' +
    '</small>' +
    '</span>' +
    '<b>' +
    '›' +
    '</b>' +
    '</button>' +
    '</div>';
  modal
    .querySelectorAll('[data-open-nested]')
    .forEach(button =>
      button.addEventListener(
        'click',
        () => window.openMenuNested(button.dataset.openNested)
      )
    );
};
