window.showChangeEmail = function () {
  const modal = document.getElementById('modal');
  if (!modal) return;
  fetch('app/settings/profile/email/index.html?v=20260904a', { cache: 'no-store' })
    .then(r => r.ok ? r.text() : Promise.reject(new Error('Email page could not be loaded.')))
    .then(html => {
      modal.innerHTML = html;
      const user = window.IndooneFirebase?.auth?.currentUser;
      const input = document.getElementById('changeEmail');
      if (input && user?.email) input.value = user.email;
      document.getElementById('saveEmail')?.addEventListener('click', () => {
        const value = input?.value.trim().toLowerCase() || '';
        if (!/^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$/.test(value)) return toast('Enter a valid email address');
        toast('Email change verification will be added next.');
      });
    })
    .catch(error => toast(error?.message || 'Could not open email settings'));
};
