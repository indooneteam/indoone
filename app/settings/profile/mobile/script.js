window.showChangeMobile = function () {
  const modal = document.getElementById('modal');
  if (!modal) return;
  fetch('app/settings/profile/mobile/index.html?v=20260904a', { cache: 'no-store' })
    .then(r => r.ok ? r.text() : Promise.reject(new Error('Mobile page could not be loaded.')))
    .then(html => {
      modal.innerHTML = html;
      document.getElementById('saveMobileNumber')?.addEventListener('click', () => {
        const value = document.getElementById('changeMobileNumber')?.value.trim() || '';
        if (!/^\\+?[0-9 ()-]{8,20}$/.test(value)) return toast('Enter a valid mobile number');
        toast('Mobile number change verification will be added next.');
      });
    })
    .catch(error => toast(error?.message || 'Could not open mobile number'));
};
