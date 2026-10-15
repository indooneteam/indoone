// Plugins talk to the same backend the app does. Each provider exposes the same
// four calls the native plugin APIs use: status, connect, probe, disconnect.
(() => {
  const PROVIDERS = [
    {
      id: 'github',
      title: 'GitHub',
      note: 'Repositories, issues and pull requests',
      status: 'api/integrations/github/status',
      connect: 'api/integrations/github/connect',
      disconnect: 'api/integrations/github/disconnect'
    },
    {
      id: 'zoho-mail',
      title: 'Zoho Mail',
      note: 'Read and send mail through Zoho',
      status: 'api/integrations/zoho-mail/connection',
      connect: 'api/integrations/zoho-mail/start',
      disconnect: 'api/integrations/zoho-mail/disconnect'
    },
    {
      id: 'gmail',
      title: 'Email',
      note: 'Read and send mail through Gmail',
      status: 'api/integrations/gmail/status',
      connect: 'api/integrations/gmail/auth-config',
      disconnect: 'api/integrations/gmail/disconnect'
    }
  ];

  const BACKEND_URL = 'https://indooneai.duckdns.org';

  async function token() {
    const user = window.IndooneFirebase?.auth?.currentUser;

    if (!user) {
      throw new Error('Please sign in again.');
    }

    return user.getIdToken(false);
  }

  async function call(path, options = {}) {
    const idToken = await token();
    const response = await fetch(`${BACKEND_URL}/${path}`, {
      ...options,
      headers: {
        'Authorization': `Bearer ${idToken}`,
        'Content-Type': 'application/json',
        ...(options.headers || {})
      }
    });

    if (!response.ok) {
      throw new Error(`Indoone backend returned ${response.status}`);
    }

    return response.json().catch(() => ({}));
  }

  async function statusOf(provider) {
    const user = window.IndooneFirebase?.auth?.currentUser;
    const id = encodeURIComponent(user?.uid || '');
    return call(`${provider.status}?user_id=${id}`);
  }

  window.IndoonePlugins = { PROVIDERS, call, statusOf };
})();

window.initMenuPlugins = function () {
  const list = document.getElementById('menuPluginsList');
  const status = document.getElementById('menuPluginsStatus');

  function setStatus(text) {
    if (status) {
      status.textContent = text;
    }
  }

  function row(provider, connected) {
    const wrap = document.createElement('div');
    wrap.className = 'plugin-row';

    const text = document.createElement('span');
    const title = document.createElement('b');
    title.textContent = provider.title;
    const note = document.createElement('small');
    note.textContent = connected ? 'Connected' : provider.note;
    text.appendChild(title);
    text.appendChild(note);

    const button = document.createElement('button');
    button.type = 'button';
    button.textContent = connected ? 'Disconnect' : 'Connect';

    if (connected) {
      button.classList.add('disconnect');
    }

    button.addEventListener('click', async () => {
      button.disabled = true;

      try {
        if (connected) {
          await window.IndoonePlugins.call(provider.disconnect, {
            method: 'POST',
            body: JSON.stringify({})
          });
          setStatus(`${provider.title} disconnected.`);
        } else {
          const result = await window.IndoonePlugins.call(provider.connect, {
            method: 'POST',
            body: JSON.stringify({})
          });

          if (result?.url) {
            window.open(result.url, '_blank', 'noopener');
            setStatus(`Approve Indoone in ${provider.title}.`);
          } else {
            setStatus(`${provider.title} connection started.`);
          }
        }

        void load();
      } catch (error) {
        setStatus(error?.message || `Could not reach ${provider.title}.`);
      } finally {
        button.disabled = false;
      }
    });

    wrap.appendChild(text);
    wrap.appendChild(button);
    return wrap;
  }

  async function load() {
    setStatus('Checking connections…');

    if (list) {
      list.textContent = '';
    }

    const results = await Promise.all(
      window.IndoonePlugins.PROVIDERS.map(provider =>
        window.IndoonePlugins
          .statusOf(provider)
          .catch(() => ({ connected: false }))
      )
    );

    window.IndoonePlugins.PROVIDERS.forEach((provider, index) => {
      const connected = Boolean(
        results[index]?.connected || results[index]?.authorized
      );
      list?.appendChild(row(provider, connected));
    });

    setStatus('');
  }

  void load();
};
