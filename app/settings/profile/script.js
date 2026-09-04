(() => {
  function currentUser() { return window.IndooneFirebase?.auth?.currentUser || null; }
  function renderProfile() {
    const user = currentUser();
    const email = document.getElementById('profileEmail');
    const phone = document.getElementById('profilePhone');
    if (email) email.textContent = user?.email || 'Email not available';
    if (phone) phone.textContent = user?.phoneNumber || 'Mobile number not set';
  }

  window.showProfile = function () {
    const modal = document.getElementById('modal');
    if (!modal) return;
    void fetch('app/settings/profile/index.html?v=20260904a', { cache: 'no-store' })
      .then(response => response.ok ? response.text() : Promise.reject(new Error('Profile page could not be loaded.')))
      .then(html => {
        modal.innerHTML = html;
        renderProfile();
        modal.querySelector('[data-profile-action="mobile"]')?.addEventListener('click', () => window.showChangeMobile?.());
        modal.querySelector('[data-profile-action="email"]')?.addEventListener('click', () => window.showChangeEmail?.());
        document.getElementById('overlay')?.classList.remove('hidden');
      })
      .catch(error => window.toast?.(error?.message || 'Could not open Profile'));
  };
})();
