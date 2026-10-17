/* About Indoone, opened the same way every other settings screen is: set the
   page state, fetch the fragment into #modal, wire the inner actions, then
   unhide the overlay. data-close on the × and on Done is handled once,
   globally, by shared/events.js. */

(() => {
  window.showAbout = function ({ remember = true } = {}) {
    if (remember) {
      window.IndoonePageState?.set('about');
    }

    const modal = document.getElementById('modal');

    if (!modal) {
      return;
    }

    void fetch(
      'settings/about/index.html?v=20261018a',
      { cache: 'no-store' }
    )
      .then(response =>
        response.ok
          ? response.text()
          : Promise.reject(
              new Error('About page could not be loaded.')
            )
      )
      .then(html => {
        modal.innerHTML = html;

        modal
          .querySelector('[data-about-action="updates"]')
          ?.addEventListener('click', () => {
            if (typeof window.showUpdate === 'function') {
              window.showUpdate();
              return;
            }

            window.toast?.(
              'Updates are not available on the web yet.'
            );
          });

        document
          .getElementById('overlay')
          ?.classList.remove('hidden');
      })
      .catch(error =>
        window.toast?.(
          error?.message || 'Could not open About'
        )
      );
  };
})();
