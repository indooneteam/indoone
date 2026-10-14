window.initMenuDataPrivacy = function () {
  document
    .querySelectorAll('[data-privacy-open]')
    .forEach(button => {
      button.addEventListener('click', () => {
        window.openMenuFeature?.(button.dataset.privacyOpen);
      });
    });
};
