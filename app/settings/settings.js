(() => {
  let appVersion = 'Loading…';
  let appVersionLoaded = false;

  async function loadAppVersion() {
    if (appVersionLoaded) return appVersion;

    appVersionLoaded = true;

    try {
      const response = await fetch(
        'update.json',
        { cache: 'no-store' }
      );

      if (!response.ok) {
        throw new Error('Version metadata unavailable');
      }

      const metadata = await response.json();
      const version = String(metadata.versionName || '').trim();

      if (version) {
        appVersion = version;
      }
    } catch (error) {
      console.warn(
        'Indoone version metadata load failed:',
        error
      );
    }

    return appVersion;
  }

  loadAppVersion().then(version => {
    document
      .querySelectorAll('[data-app-version]')
      .forEach(node => {
        node.textContent = version;
      });
  });

  const SETTINGS = [
    {
      id: 'profile',
      title: 'Profile',
      subtitle: 'Email & mobile number',
      icon: 'profile',
      action: () => window.showProfile?.()
    },
    {
      id: 'app-lock',
      title: 'App Lock',
      subtitle: 'PIN',
      icon: 'lock',
      action: () => window.showAppLock?.()
    },
    {
      id: 'biometric',
      title: 'Biometric Unlock',
      subtitle: 'Fingerprint / device credential',
      icon: 'biometric',
      toggle: true
    },
    {
      id: 'auto-lock',
      title: 'Auto-Lock',
      icon: 'timer',
      subtitle: () => {
        const value = window.IndooneAutoLock?.minutes?.() ?? 0;
        return value
          ? `After ${value} minute${value === 1 ? '' : 's'}`
          : 'Never';
      },
      action: () => window.showAutoLock?.()
    },
    {
      id: 'about',
      title: 'About Indoone',
      subtitle: 'Version loading… · Updates',
      icon: 'info',
      action: () => window.showAboutSettings?.()
    }
  ];

  const ICONS = {
    profile: `
      <circle cx="12" cy="8" r="3.1"></circle>
      <path d="M5.5 19.5c.8-3.1 3.1-4.8 6.5-4.8s5.7 1.7 6.5 4.8"></path>
    `,
    lock: `
      <rect x="5" y="10" width="14" height="10" rx="2"></rect>
      <path d="M8 10V7a4 4 0 0 1 8 0v3"></path>
    `,
    biometric: `
      <path d="M8 7.5A4.5 4.5 0 0 1 12.5 3 4.5 4.5 0 0 1 17 7.5"></path>
      <path d="M6 10a6 6 0 0 1 12 0"></path>
      <path d="M8.5 12.5a3.5 3.5 0 0 1 7 0"></path>
      <path d="M10.5 15v1.5a1.5 1.5 0 0 1-3 0v-1.2"></path>
      <path d="M13.5 15v3a1.5 1.5 0 0 1-3 0v-1.5"></path>
      <path d="M16 13v4a2 2 0 0 1-4 0v-1"></path>
    `,
    timer: `
      <circle cx="12" cy="13" r="7"></circle>
      <path d="M12 13V9"></path>
      <path d="M9.5 3.5h5"></path>
      <path d="M12 6v-2.5"></path>
      <path d="M18 7l1.5-1.5"></path>
    `,
    info: `
      <circle cx="12" cy="12" r="8"></circle>
      <path d="M12 10.5v5"></path>
      <path d="M12 7.5h.01"></path>
    `,
    next: `
      <path d="m9 5 7 7-7 7"></path>
    `
  };

  function makeIcon(type) {
    const svg = document.createElementNS(
      'http://www.w3.org/2000/svg',
      'svg'
    );

    svg.setAttribute('class', 'settings-option-icon');
    svg.setAttribute('viewBox', '0 0 24 24');
    svg.setAttribute('aria-hidden', 'true');
    svg.setAttribute('focusable', 'false');
    svg.setAttribute('fill', 'none');
    svg.setAttribute('stroke', 'currentColor');
    svg.setAttribute('stroke-width', '1.8');
    svg.setAttribute('stroke-linecap', 'round');
    svg.setAttribute('stroke-linejoin', 'round');
    svg.innerHTML = ICONS[type] || ICONS.info;

    return svg;
  }

  function rowMarkup(item, biometricOn) {
    const subtitle =
      typeof item.subtitle === 'function'
        ? item.subtitle()
        : item.subtitle;

    const icon = makeIcon(item.icon);

    if (item.toggle) {
      const row = document.createElement('div');
      row.className = 'settings-row settings-row-toggle';
      row.dataset.settingsItem = item.id;

      const iconWrap = document.createElement('span');
      iconWrap.className = 'settings-option-icon-wrap';
      iconWrap.appendChild(icon);

      const text = document.createElement('span');
      text.className = 'settings-row-text';
      text.innerHTML = `
        <b>${item.title}</b>
        <small>${subtitle}</small>
      `;

      const button = document.createElement('button');
      button.type = 'button';
      button.className = `toggle settings-toggle ${
        biometricOn ? 'on' : ''
      }`;
      button.id = 'biometricToggle';
      button.setAttribute(
        'aria-label',
        'Toggle biometric unlock'
      );

      row.append(iconWrap, text, button);
      return row;
    }

    const button = document.createElement('button');
    button.type = 'button';
    button.className =
      'settings-row settings-row-button settings-option-row';
    button.dataset.settingsAction = item.id;

    const iconWrap = document.createElement('span');
    iconWrap.className = 'settings-option-icon-wrap';
    iconWrap.appendChild(icon);

    const text = document.createElement('span');
    text.className = 'settings-row-text';
    text.innerHTML = `
      <b>${item.title}</b>
      <small>${subtitle}</small>
    `;

    const arrow = document.createElement('span');
    arrow.className = 'settings-arrow';
    arrow.setAttribute('aria-hidden', 'true');
    arrow.appendChild(makeIcon('next'));

    button.append(iconWrap, text, arrow);
    return button;
  }

  function hideSettingsPage() {
    document
      .getElementById('settingsContent')
      ?.setAttribute('hidden', '');
  }

  function showSettingsPage() {
    const page = document.getElementById('settingsContent');
    const content = document.getElementById('content');
    const connect = document.getElementById('connectContent');
    const search = document.getElementById('searchWrap');
    const add = document.getElementById('addBtn');

    content?.setAttribute('hidden', '');
    connect?.setAttribute('hidden', '');
    search?.setAttribute('hidden', '');
    add?.setAttribute('hidden', '');
    page?.removeAttribute('hidden');

    document
      .getElementById('settingsNav')
      ?.classList.add('active');
    document
      .getElementById('accountsNav')
      ?.classList.remove('active');
    document
      .getElementById('lobbyNav')
      ?.classList.remove('active');
    document
      .getElementById('connectNav')
      ?.classList.remove('active');
  }

  function renderSettingsPage() {
    let page = document.getElementById('settingsContent');

    if (!page) {
      page = document.createElement('section');
      page.className = 'settings-content';
      page.id = 'settingsContent';
      page.hidden = true;

      const app = document.getElementById('app');
      const nav = document.querySelector('.bottom-nav');

      app?.insertBefore(page, nav || null);
    }

    const biometricOn =
      window.IndooneBiometric?.enabled?.() || false;

    page.innerHTML = '';

    const head = document.createElement('div');
    head.className = 'settings-page-head';
    head.innerHTML = `
      <p class="eyebrow">PREFERENCES &amp; SECURITY</p>
      <h1>Settings</h1>
      <p>Manage your account, security and app preferences.</p>
    `;
    page.appendChild(head);

    const sections = [
      {
        label: 'Account',
        items: [SETTINGS[0]]
      },
      {
        label: 'Security',
        items: [
          SETTINGS[1],
          SETTINGS[2],
          SETTINGS[3]
        ]
      },
      {
        label: 'App',
        items: [SETTINGS[4]]
      }
    ];

    sections.forEach(section => {
      const label = document.createElement('div');
      label.className = 'settings-section-label';
      label.textContent = section.label;
      page.appendChild(label);

      section.items.forEach(item => {
        const row = rowMarkup(item, biometricOn);
        page.appendChild(row);
      });
    });

    page
      .querySelectorAll('[data-settings-action="profile"]')
      .forEach(button => {
        button.addEventListener(
          'click',
          () => window.showProfile?.()
        );
      });

    page
      .querySelectorAll('[data-settings-action="app-lock"]')
      .forEach(button => {
        button.addEventListener(
          'click',
          () => window.showAppLock?.()
        );
      });

    page
      .querySelectorAll('[data-settings-action="auto-lock"]')
      .forEach(button => {
        button.addEventListener(
          'click',
          () => window.showAutoLock?.()
        );
      });

    page
      .querySelectorAll('[data-settings-action="about"]')
      .forEach(button => {
        button.addEventListener(
          'click',
          () => window.showAboutSettings?.()
        );
      });

    page
      .querySelector('#biometricToggle')
      ?.addEventListener(
        'click',
        event =>
          toggleBiometricSetting(
            event,
            event.currentTarget
          )
      );

    return page;
  }

  window.showSettings = function ({ remember = true } = {}) {
    if (remember) {
      window.IndoonePageState?.set('settings');
    }

    closeModal?.();
    renderSettingsPage();
    showSettingsPage();
  };

  window.toggleBiometricSetting = function (
    event,
    button
  ) {
    event?.stopPropagation?.();

    const enabled = button.classList.contains('on');

    if (enabled) {
      IndooneBiometric.disable();
      button.classList.remove('on');
      toast('Biometric unlock disabled');
      return;
    }

    if (!window.IndooneBiometric?.supported?.()) {
      toast(
        'Biometric unlock is available in the Android app'
      );
      return;
    }

    IndooneBiometric.enableForCurrentVault(
      () => {
        button.classList.add('on');
        toast('Biometric unlock enabled');
      },
      message =>
        toast(
          message ||
            'Biometric authentication cancelled'
        )
    );
  };

  window.showAboutSettings = function () {
    openModal(`
      <div class="modal-head">
        <h2>About Indoone</h2>
        <button class="close-btn" data-close>×</button>
      </div>

      <div class="about-mark">I</div>
      <h3 class="about-title">Indoone Authenticator</h3>
      <p class="about-copy">
        Private authenticator with cloud sync and secure device pairing.
        Updates are checked automatically when the app opens.
      </p>

      <div class="about-meta">
        <span>Version</span>
        <b data-app-version>${appVersion}</b>
      </div>

      <div class="about-meta">
        <span>Updates</span>
        <b>Automatic</b>
      </div>

      <button class="primary" data-close>Done</button>
    `);

    loadAppVersion().then(version => {
      document
        .querySelector('[data-app-version]')
        ?.replaceChildren(document.createTextNode(version));
    });
  };

  document.addEventListener('click', event => {
    const target = event.target;

    if (target.closest('#settingsNav')) {
      return;
    }

    if (
      target.closest(
        '#accountsNav, #lobbyNav, #connectNav, .drawer-item'
      )
    ) {
      hideSettingsPage();
    }
  });
})();
