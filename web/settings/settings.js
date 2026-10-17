(() => {

  const SETTINGS = [
    {
      id: 'profile',
      title: 'Profile',
      icon: 'profile',
      action: () => window.showProfile?.()
    },
    {
      id: 'notifications',
      title: 'Notifications',
      icon: 'info',
      action: () => window.IndooneNotifications?.show?.()
    },
    {
      id: 'about',
      title: 'About Indoone',
      icon: 'info',
      action: () => window.showAbout?.()
    },
    {
      id: 'danger-zone',
      title: 'Danger Zone',
      icon: 'info',
      action: () => window.showDangerZone?.()
    },
    {
      id: 'logout',
      title: 'Log out',
      icon: 'info',
      action: () => window.showLogout?.()
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
      .getElementById('homeNav')
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
      }
    ];

    sections.forEach(section => {
      const label = document.createElement('div');
      label.className = 'settings-section-label';
      label.textContent = section.label;
      page.appendChild(label);

      section.items.forEach(item => {
        const row = rowMarkup(item);
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
        '#homeNav, #lobbyNav, #connectNav, .drawer-item'
      )
    ) {
      hideSettingsPage();
    }
  });
})();
