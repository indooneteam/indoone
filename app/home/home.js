(() => {
  function init() {
    const page = document.getElementById('content');
    if (!page) return;
    page.dataset.feature = 'home';
    page.classList.add('home-page');
  }

  window.IndooneHome = { init };
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init, { once: true });
  else init();
})();
