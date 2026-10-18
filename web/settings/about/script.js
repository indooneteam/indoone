// The web counterpart of android/menu/about/AboutScreen.kt.
//
// About Indoone is a drawer row, and it opens like every other menu feature:
// menu/script.js fetches the fragment into #modal, calls this initializer, then
// shows the page. It used to fetch the fragment itself and unhide #overlay,
// which dimmed the whole app and shrank the screen to a card - the same bug
// Profile had. It does not touch #overlay any more.
//
// data-close on the × is handled once, globally, by shared/events.js.
window.initMenuAbout = function () {
  const page = document.getElementById('modal');

  if (!page) {
    return false;
  }

  page
    .querySelector('[data-about-action="updates"]')
    ?.addEventListener('click', () => {
      window.openMenuFeature?.('update');
    });

  return true;
};
