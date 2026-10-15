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
    '<p>' +
    'These actions can permanently remove Indoone data. Continue only when yo' +
    'u are sure.' +
    '</p>' +
    '<button type="button" class="settings-row danger" style="width:100%;bord' +
    'er:0;background:#fff;text-align:left" data-open-nested="danger-zone/dele' +
    'te-local-data">' +
    '<span>' +
    'Delete local data' +
    '<small>' +
    'Remove data stored on this device' +
    '</small>' +
    '</span>' +
    '<b>' +
    '›' +
    '</b>' +
    '</button>' +
    '<button type="button" class="settings-row danger" style="width:100%;bord' +
    'er:0;background:#fff;text-align:left" data-open-nested="danger-zone/dele' +
    'te-account">' +
    '<span>' +
    'Delete Indoone account' +
    '<small>' +
    'Permanently delete your Indoone account and cloud data' +
    '</small>' +
    '</span>' +
    '<b>' +
    '›' +
    '</b>' +
    '</button>';
  modal
    .querySelectorAll('[data-open-nested]')
    .forEach(button =>
      button.addEventListener(
        'click',
        () => window.openMenuNested(button.dataset.openNested)
      )
    );
};
