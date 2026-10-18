(() => {

  // The web counterpart of android/settings/SettingsScreen.kt.
  //
  // The rows below are read from the terminal branch, because that is the
  // branch the installed test app is built from, and it does not match main.
  //
  // terminal draws, in this order, with no section heading and no eyebrow
  // above the title:
  //
  //   SettingsMenuItem(ProfileIcon,       "Profile",       ...)
  //   SettingsMenuItem(LockIcon,          "App Lock")
  //   SettingsMenuToggle(BiometricIcon,   "Biometric Unlock", ...)
  //   SettingsMenuItem(TimerIcon,         "Auto-Lock")
  //   SettingsMenuItem(NotificationsIcon, "Notifications", ...)
  //   SettingsMenuAction(InfoIcon, "Version", versionName, "Check for updates")
  //   SettingsMenuItem(DangerZoneIcon,    "Danger Zone")
  //   SettingsMenuItem(LogoutIcon,        "Log out")
  //
  // main replaces that Version row with SettingsMenuItem(InfoIcon, "About
  // Indoone"). The installed app has no About row here at all - About Indoone
  // is a drawer row, under SECURITY & INFORMATION - so the web does not draw
  // one either. The settings/about screen still exists and the drawer still
  // opens it; it is simply not listed here.
  //
  // Three of the app's rows are deliberately absent: App Lock, Biometric
  // Unlock and Auto-Lock are device features with nothing to do on the web,
  // and the Version row is absent for the same reason - a browser tab does not
  // have an installed version to report.
  //
  // Danger Zone and Log out are menu features whose folders live under
  // settings/ (see featurePaths in menu/script.js). They are opened through
  // openMenuFeature, which loads the fragment, its stylesheet and its script -
  // the same way the drawer opens them. That is why these two call
  // openMenuFeature and not a window.show* of their own.
  const SETTINGS = [
    {
      id: 'profile',
      title: 'Profile',
      icon: 'profile',
      // Opened through the menu loader, exactly like Danger Zone and Log out,
      // so Profile is a full page rather than a dimmed card over the app.
      action: () => window.openMenuFeature?.('profile')
    },
    {
      id: 'notifications',
      title: 'Notifications',
      icon: 'info',
      action: () => window.IndooneNotifications?.show?.()
    },
    {
      id: 'danger-zone',
      title: 'Danger Zone',
      icon: 'info',
      action: () => window.openMenuFeature?.('danger-zone')
    },
    {
      id: 'logout',
      title: 'Log out',
      icon: 'info',
      action: () => window.openMenuFeature?.('logout')
    }
  ];

  const ICONS = {
    profile: `
      <circle cx="12" cy="8" r="3.1"></circle>
      <path d="M5.5 19.5c.8-3.1 3.1-4.8 6.5-4.8s5.7 1.7 6.5 4.8"></path>
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

  function rowMarkup(item) {
    const subtitle =
      typeof item.subtitle === 'function'
        ? item.subtitle()
        : item.subtitle;

    const icon = makeIcon(item.icon);

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
    // The app's SettingsMenuItem takes subtitle: String? = null and every
    // call leaves it out, so a row with no subtitle shows only its title.
    text.innerHTML = subtitle
      ? `
      <b>${item.title}</b>
      <small>${subtitle}</small>
    `
      : `
      <b>${item.title}</b>
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
    const search = document.getElementById('searchWrap');
    const add = document.getElementById('addBtn');

    content?.setAttribute('hidden', '');
    search?.setAttribute('hidden', '');
    add?.setAttribute('hidden', '');
    page?.removeAttribute('hidden');

    document
      .getElementById('settingsNav')
      ?.classList.add('active');
    document
      .getElementById('homeNav')
      ?.classList.remove('active');
    document
      .getElementById('lobbyNav')
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

    page.innerHTML = '';

    // The app's head is the title and one line under it. There is no eyebrow
    // above it and no section heading under it.
    const head = document.createElement('div');
    head.className = 'settings-page-head';
    head.innerHTML = `
      <h1>Settings</h1>
      <p>Manage your account, security and app preferences.</p>
    `;
    page.appendChild(head);

    // Every row is drawn and every row is wired. This used to render only
    // SETTINGS[0] and attach a click to profile alone, so the other four
    // never appeared on the screen at all.
    SETTINGS.forEach(item => {
      const row = rowMarkup(item);
      row.addEventListener('click', () => item.action());
      page.appendChild(row);
    });

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

  document.addEventListener('click', event => {
    const target = event.target;

    if (target.closest('#settingsNav')) {
      return;
    }

    if (
      target.closest(
        '#homeNav, #lobbyNav, .drawer-item'
      )
    ) {
      hideSettingsPage();
    }
  });
})();
