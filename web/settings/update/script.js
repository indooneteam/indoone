/* Check for updates, opened the same way as every other settings screen: set
   the page state, fetch the fragment into #modal, wire the inner parts, then
   unhide the overlay. data-close on the × and on Done is handled once,
   globally, by shared/events.js.

   The app reads the same place this does - the repository's releases, looking
   for a terminal-build-* tag with indoone-terminal.apk attached. A browser
   cannot install an APK, so this screen reports what is published and hands
   over the download link instead of installing anything. */

(() => {
  const RELEASES_URL =
    'https://api.github.com/repos/indooneteam/indoone/releases';
  const DOWNLOAD_URL =
    'https://github.com/indooneteam/indoone/releases/latest';

  function setStatus(node, text, kind) {
    if (!node) {
      return;
    }
    node.textContent = text;
    node.classList.toggle('is-new', kind === 'new');
    node.classList.toggle('is-error', kind === 'error');
  }

  async function currentVersion() {
    try {
      const response = await fetch('update.json', { cache: 'no-store' });

      if (!response.ok) {
        return null;
      }

      const metadata = await response.json();
      const name = String(metadata.versionName || '').trim();
      const code = String(metadata.versionCode || '').trim();

      if (!name) {
        return null;
      }

      return code ? `${name} (${code})` : name;
    } catch (_) {
      return null;
    }
  }

  async function latestBuild() {
    const response = await fetch(
      RELEASES_URL,
      { headers: { Accept: 'application/vnd.github+json' } }
    );

    if (!response.ok) {
      throw new Error(`The release list could not be read (${response.status}).`);
    }

    const releases = await response.json();

    if (!Array.isArray(releases)) {
      throw new Error('The release list came back in an unexpected shape.');
    }

    const build = releases.find(release =>
      String(release.tag_name || '').startsWith('terminal-build-')
    );

    if (!build) {
      return null;
    }

    const apk = (build.assets || []).find(asset =>
      String(asset.name || '') === 'indoone-terminal.apk'
    );

    return {
      tag: String(build.tag_name),
      number: String(build.tag_name).replace('terminal-build-', ''),
      url: (apk && apk.browser_download_url) || build.html_url || DOWNLOAD_URL,
      published: String(build.published_at || '').slice(0, 10),
    };
  }

  window.showUpdate = function ({ remember = true } = {}) {
    if (remember) {
      window.IndoonePageState?.set('update');
    }

    const modal = document.getElementById('modal');

    if (!modal) {
      return;
    }

    void fetch(
      'settings/update/index.html?v=20261018c',
      { cache: 'no-store' }
    )
      .then(response =>
        response.ok
          ? response.text()
          : Promise.reject(new Error('The update page could not be loaded.'))
      )
      .then(html => {
        modal.innerHTML = html;

        const status = modal.querySelector('[data-update-status]');
        const current = modal.querySelector('[data-update-current]');
        const latest = modal.querySelector('[data-update-latest]');
        const download = modal.querySelector('[data-update-download]');

        document
          .getElementById('overlay')
          ?.classList.remove('hidden');

        void (async () => {
          const here = await currentVersion();

          if (current) {
            current.textContent = here || 'Not stated';
          }

          try {
            const build = await latestBuild();

            if (!build) {
              setStatus(status, 'No build has been published yet.', 'error');
              return;
            }

            if (latest) {
              latest.textContent = `Build ${build.number}`;
            }

            if (download) {
              download.href = build.url;
              download.removeAttribute('hidden');
            }

            setStatus(
              status,
              build.published
                ? `The newest build is ${build.number}, published ${build.published}. Download it below to move to it.`
                : `The newest build is ${build.number}. Download it below to move to it.`,
              'new'
            );
          } catch (error) {
            setStatus(
              status,
              'The update list could not be reached. Open the releases page to see the newest build.',
              'error'
            );

            if (download) {
              download.href = DOWNLOAD_URL;
              download.removeAttribute('hidden');
            }
          }
        })();
      })
      .catch(error =>
        window.toast?.(
          error?.message || 'Could not open Check for updates'
        )
      );
  };
})();
