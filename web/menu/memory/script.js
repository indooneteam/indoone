window.initMenuMemory = function () {
  const form = document.getElementById('menuMemoryForm');
  const key = document.getElementById('menuMemoryKey');
  const value = document.getElementById('menuMemoryValue');
  const list = document.getElementById('menuMemoryList');
  const status = document.getElementById('menuMemoryStatus');

  function setStatus(text) {
    if (status) {
      status.textContent = text;
    }
  }

  function row(entryKey, entryValue) {
    const wrap = document.createElement('div');
    wrap.className = 'memory-row';

    const text = document.createElement('span');
    const title = document.createElement('b');
    title.textContent = entryKey;
    const body = document.createElement('small');
    body.textContent = entryValue;
    text.appendChild(title);
    text.appendChild(body);

    const remove = document.createElement('button');
    remove.type = 'button';
    remove.setAttribute('aria-label', `Delete ${entryKey}`);
    remove.textContent = '×';
    remove.addEventListener('click', async () => {
      try {
        await window.IndooneUserData.deleteMemory(entryKey);
        void load();
      } catch (error) {
        setStatus(error?.message || 'Could not delete memory.');
      }
    });

    wrap.appendChild(text);
    wrap.appendChild(remove);
    return wrap;
  }

  async function load() {
    setStatus('Loading…');

    try {
      const memories = await window.IndooneUserData.readMemory();
      const keys = Object.keys(memories);

      if (list) {
        list.textContent = '';

        if (!keys.length) {
          const empty = document.createElement('p');
          empty.className = 'menu-feature-note';
          empty.textContent = 'No memories saved yet.';
          list.appendChild(empty);
        } else {
          keys.forEach(entryKey => {
            const entry = memories[entryKey] || {};
            list.appendChild(row(entryKey, entry.value || ''));
          });
        }
      }

      setStatus('');
    } catch (error) {
      setStatus(error?.message || 'Could not load memory.');
    }
  }

  form?.addEventListener('submit', async event => {
    event.preventDefault();

    const entryKey = (key?.value || '').trim();
    const entryValue = (value?.value || '').trim();

    if (!entryKey || !entryValue) {
      setStatus('A memory needs both a key and a value.');
      return;
    }

    setStatus('Saving…');

    try {
      await window.IndooneUserData.setMemory(entryKey, entryValue);
      if (key) key.value = '';
      if (value) value.value = '';
      void load();
    } catch (error) {
      setStatus(error?.message || 'Could not save memory.');
    }
  });

  void load();
};
