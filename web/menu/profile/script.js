window.initMenuProfile = function () {
  const form = document.getElementById('menuProfileForm');
  const name = document.getElementById('menuProfileName');
  const nickname = document.getElementById('menuProfileNickname');
  const profession = document.getElementById('menuProfileProfession');
  const status = document.getElementById('menuProfileStatus');
  const save = document.getElementById('menuProfileSave');

  function setStatus(text) {
    if (status) {
      status.textContent = text;
    }
  }

  async function load() {
    setStatus('Loading…');

    try {
      const profile = await window.IndooneUserData.readProfile();

      if (name) name.value = profile.name || '';
      if (nickname) nickname.value = profile.nickname || '';
      if (profession) profession.value = profile.profession || '';
      setStatus('');
    } catch (error) {
      setStatus(error?.message || 'Could not load your profile.');
    }
  }

  form?.addEventListener('submit', async event => {
    event.preventDefault();

    if (save) {
      save.disabled = true;
    }

    setStatus('Saving…');

    try {
      await window.IndooneUserData.updateProfile({
        name: name?.value || '',
        nickname: nickname?.value || '',
        profession: profession?.value || ''
      });
      setStatus('Profile saved.');
    } catch (error) {
      setStatus(error?.message || 'Could not save your profile.');
    } finally {
      if (save) {
        save.disabled = false;
      }
    }
  });

  void load();
};
