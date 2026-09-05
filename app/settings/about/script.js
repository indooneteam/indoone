window.showAboutSettings = async function () {
  try {
    const response = await fetch(
      'app/settings/about/index.html?v=20260922a',
      { cache: 'no-store' }
    );

    if (!response.ok) {
      throw new Error('About page could not be loaded.');
    }

    const modal = document.getElementById('modal');

    if (!modal) {
      throw new Error('Settings modal is unavailable.');
    }

    modal.innerHTML = await response.text();
  } catch (error) {
    window.toast?.(
      error?.message ||
        'Could not open About Indoone'
    );
  }
};
