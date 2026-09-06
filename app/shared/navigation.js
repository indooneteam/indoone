window.showFavorites = async function () {
  closeModal?.();

  try {
    if (window.IndooneCloudAccounts?.load) {
      await window.IndooneCloudAccounts.load();
    }

    const favorites = (
      window.indooneState?.accounts || []
    ).filter(a => a.favorite);

    const rows = favorites.length
      ? favorites
          .map(
            a => `
              <button
                type="button"
                class="settings-row"
                style="width:100%;text-align:left;border:0;background:#fff;"
                onclick="closeModal();openAccount(${Number(a.id)})"
              >
                <span>
                  ${a.name}
                  <small>${a.email || 'Authenticator account'}</small>
                </span>
                <b>${a.code || '------'}</b>
              </button>
            `
          )
          .join('')
      : `<p style="text-align:center;padding:28px 0">No favorite accounts yet.</p>`;

    openModal(`
      <div class="modal-head">
        <h2>Favorites</h2>
        <button class="close-btn" data-close>×</button>
      </div>
      <div id="favoritesList">${rows}</div>
      <button class="primary" data-close>Done</button>
    `);
  } catch (error) {
    toast(error?.message || 'Could not load favorites');
  }
};

window.showTrash = function () {
  openModal(`
    <div class="modal-head">
      <h2>Trash</h2>
      <button class="close-btn" data-close>×</button>
    </div>
    <p style="text-align:center;padding:28px 0">
      Deleted accounts are permanently removed from Firebase. Nothing is kept in Trash.
    </p>
    <button class="primary" data-close>Done</button>
  `);
};

window.showSecurity = function () {
  openModal(`
    <h2>Security</h2>
    <div class="settings-row">
      <span>
        Firebase account storage
        <small>
          Authenticator accounts synced to your user account
        </small>
      </span>
      <b>✓</b>
    </div>
    <div class="settings-row">
      <span>
        Local account storage
        <small>
          Not used for permanent account data
        </small>
      </span>
      <b>✓</b>
    </div>
    <button class="primary" data-close>Done</button>
  `);
};

window.showAbout = function () {
  openModal(`
    <h2>About Indoone</h2>
    <p>
      Indoone Authenticator — a login-based cloud-synced TOTP authenticator.
    </p>
    <button class="primary" data-close>Done</button>
  `);
};

// Keep Lock App packed with the menu items instead of pinning it to the bottom.
document.querySelector('.lock-item')?.style.setProperty('margin-top', '0');

// Keep a clear, consistent space between the Indoone brand and the menu button.
function applyTopbarBrandSpacing() {
  const topbarLeft = document.querySelector('.topbar-left');

  if (topbarLeft) {
    topbarLeft.style.gap = '14px';
  }
}

// Replace drawer Unicode glyphs with one consistent SVG icon family.
const DRAWER_ICONS = {
  accounts:
    '<rect x="4" y="4" width="6" height="6" rx="1.5"></rect>' +
    '<rect x="14" y="4" width="6" height="6" rx="1.5"></rect>' +
    '<rect x="4" y="14" width="6" height="6" rx="1.5"></rect>' +
    '<rect x="14" y="14" width="6" height="6" rx="1.5"></rect>',
  favorites:
    '<path d="m12 4 2.5 5.1 5.6.8-4 4 1 5.6-5.1-2.7-5.1 2.7 1-5.6-4-4 5.6-.8L12 4Z"></path>',
  trash:
    '<path d="M5 7h14M9 7V5h6v2M8 10v7M12 10v7M16 10v7M7 7l1 13h8l1-13"></path>',
  security:
    '<path d="M12 3.5 19 6v5.2c0 4.4-2.8 7.5-7 9.3-4.2-1.8-7-4.9-7-9.3V6l7-2.5Z"></path>' +
    '<path d="m9 12 2 2 4-4"></path>',
  about:
    '<circle cx="12" cy="12" r="8"></circle><path d="M12 10.5v5M12 7.5h.01"></path>',
  lock:
    '<rect x="5" y="10" width="14" height="10" rx="2"></rect><path d="M8 10V7a4 4 0 0 1 8 0v3"></path>',
  'danger-zone':
    '<path d="m12 4 9 16H3L12 4Z"></path><path d="M12 9v5M12 17h.01"></path>',
  logout:
    '<path d="M10 5H6a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h4M14 8l4 4-4 4M18 12H9"></path>'
};

function applyDrawerIcons() {
  document
    .querySelectorAll('.drawer-item[data-action]')
    .forEach(item => {
      if (item.querySelector('.drawer-icon')) return;

      const action = item.dataset.action;
      const paths = DRAWER_ICONS[action];
      if (!paths) return;

      const icon = document.createElementNS(
        'http://www.w3.org/2000/svg',
        'svg'
      );

      icon.setAttribute('class', 'drawer-icon');
      icon.setAttribute('viewBox', '0 0 24 24');
      icon.setAttribute('aria-hidden', 'true');
      icon.setAttribute('focusable', 'false');
      icon.setAttribute('width', '22');
      icon.setAttribute('height', '22');
      icon.setAttribute('fill', 'none');
      icon.setAttribute('stroke', 'currentColor');
      icon.setAttribute('stroke-width', '1.8');
      icon.setAttribute('stroke-linecap', 'round');
      icon.setAttribute('stroke-linejoin', 'round');
      icon.style.cssText =
        'width:22px;height:22px;display:block;flex:none;';
      icon.innerHTML = paths;

      item.insertBefore(icon, item.firstChild);

      const firstText = Array.from(item.childNodes).find(
        node =>
          node.nodeType === Node.TEXT_NODE &&
          node.textContent.trim()
      );

      if (firstText) firstText.remove();
    });
}

// Main UI glyphs: keep their existing meaning and position, but render them as SVGs.
const MAIN_SVG_ICONS = {
  menu:
    '<path d="M4 7h16M4 12h16M4 17h16"></path>',
  search:
    '<circle cx="11" cy="11" r="6.5"></circle><path d="m16 16 4 4"></path>',
  close:
    '<path d="m7 7 10 10M17 7 7 17"></path>',
  sort:
    '<path d="M7 5v14M4 8l3-3 3 3M17 19V5M14 16l3 3 3-3"></path>',
  empty:
    '<path d="M4 12h16M12 4v16"></path>',
  pair:
    '<path d="M8.5 8.5 6.7 10.3a4 4 0 0 0 5.7 5.7l1.8-1.8M15.5 15.5l1.8-1.8a4 4 0 0 0-5.7-5.7L9.8 9.8"></path>' +
    '<path d="m9.3 14.7 5.4-5.4"></path>',
  connect:
    '<path d="M5 12h14M8 7l-5 5 5 5M16 7l5 5-5 5"></path>',
  devices:
    '<rect x="3" y="5" width="13" height="10" rx="1.5"></rect>' +
    '<path d="M7 19h5M9.5 15v4M19 9h2v10h-7V9h2M16.5 11h3"></path>',
  status:
    '<path d="M4 17a11.5 11.5 0 0 1 16 0M7 14a7.4 7.4 0 0 1 10 0M10 11a3.4 3.4 0 0 1 4 0"></path>',
  phone:
    '<rect x="6.5" y="3.5" width="11" height="17" rx="2"></rect><path d="M10 17.5h4"></path>',
  laptop:
    '<rect x="5" y="5" width="14" height="10" rx="1.5"></rect><path d="M3 18h18M9 18l-1 2h8l-1-2"></path>',
  back:
    '<path d="m14 5-7 7 7 7"></path>',
  next:
    '<path d="m10 5 7 7-7 7"></path>'
};

function makeMainSvg(
  type,
  className = 'ui-svg-icon'
) {
  const paths = MAIN_SVG_ICONS[type];
  if (!paths) return null;

  const svg = document.createElementNS(
    'http://www.w3.org/2000/svg',
    'svg'
  );

  svg.setAttribute('class', className);
  svg.setAttribute('viewBox', '0 0 24 24');
  svg.setAttribute('aria-hidden', 'true');
  svg.setAttribute('focusable', 'false');
  svg.setAttribute('fill', 'none');
  svg.setAttribute('stroke', 'currentColor');
  svg.setAttribute('stroke-width', '1.8');
  svg.setAttribute('stroke-linecap', 'round');
  svg.setAttribute('stroke-linejoin', 'round');
  svg.innerHTML = paths;

  return svg;
}

function replaceTextNodeIcon(element, iconType) {
  if (
    !element ||
    element.querySelector('.ui-svg-icon')
  ) {
    return;
  }

  const icon = makeMainSvg(iconType);
  if (!icon) return;

  element.insertBefore(icon, element.firstChild);

  const textNodes = Array.from(element.childNodes).filter(
    node =>
      node.nodeType === Node.TEXT_NODE &&
      node.textContent.trim()
  );

  if (textNodes.length) {
    textNodes[0].textContent = textNodes[0].textContent
      .replace(
        /^[\s\u2302\u2315\u2300\u2195\u2194\u2303\u203a\u2039\u2302\u2022]+/u,
        ''
      )
      .trimStart();
  }
}

function applyMainSvgIcons() {
  const menu = document.getElementById('menuBtn');

  if (
    menu &&
    !menu.querySelector('.ui-svg-icon')
  ) {
    const icon = makeMainSvg(
      'menu',
      'ui-svg-icon topbar-svg-icon'
    );
    menu.textContent = '';
    menu.appendChild(icon);
  }

  const search = document.getElementById('searchBtn');

  if (
    search &&
    !search.querySelector('.ui-svg-icon')
  ) {
    const icon = makeMainSvg(
      'search',
      'ui-svg-icon topbar-svg-icon'
    );
    search.textContent = '';
    search.appendChild(icon);
  }

  const searchBoxIcon = document.querySelector(
    '.search-box > span:first-child'
  );

  if (
    searchBoxIcon &&
    !searchBoxIcon.querySelector('.ui-svg-icon')
  ) {
    searchBoxIcon.textContent = '';
    searchBoxIcon.appendChild(
      makeMainSvg('search')
    );
  }

  const sort = document.getElementById('sortBtn');

  if (
    sort &&
    !sort.querySelector('.ui-svg-icon')
  ) {
    sort.textContent = 'Sort ';
    sort.appendChild(makeMainSvg('sort'));
  }

  const empty = document.querySelector('.empty-icon');

  if (
    empty &&
    !empty.querySelector('.ui-svg-icon')
  ) {
    empty.textContent = '';
    empty.appendChild(makeMainSvg('empty'));
  }

  document
    .querySelectorAll('[data-connect-action="pair"] strong')
    .forEach(el => {
      if (!el.querySelector('.ui-svg-icon')) {
        el.textContent = '';
        el.appendChild(makeMainSvg('pair'));
      }
    });

  document
    .querySelectorAll('[data-connect-action="connect"] strong')
    .forEach(el => {
      if (!el.querySelector('.ui-svg-icon')) {
        el.textContent = '';
        el.appendChild(makeMainSvg('connect'));
      }
    });

  document
    .querySelectorAll('[data-connect-action="devices"] strong')
    .forEach(el => {
      if (!el.querySelector('.ui-svg-icon')) {
        el.textContent = '';
        el.appendChild(makeMainSvg('devices'));
      }
    });

  const status = document.querySelector(
    '.connect-status-icon'
  );

  if (
    status &&
    !status.querySelector('.ui-svg-icon')
  ) {
    status.textContent = '';
    status.appendChild(makeMainSvg('status'));
  }

  document
    .querySelectorAll('.connect-back')
    .forEach(el => {
      if (!el.querySelector('.ui-svg-icon')) {
        el.textContent = '';
        el.appendChild(makeMainSvg('back'));
        el.appendChild(
          document.createTextNode('Back')
        );
      }
    });

  document
    .querySelectorAll('.connect-choice .choice-icon')
    .forEach((el, index) => {
      if (el.querySelector('.ui-svg-icon')) return;
      el.textContent = '';
      el.appendChild(
        makeMainSvg(index === 0 ? 'phone' : 'laptop')
      );
    });

  document
    .querySelectorAll('.connect-choice .arrow')
    .forEach(el => {
      if (!el.querySelector('.ui-svg-icon')) {
        el.textContent = '';
        el.appendChild(makeMainSvg('next'));
      }
    });
}

function applySvgIconStyles() {
  if (document.getElementById('svg-icon-runtime-style')) {
    return;
  }

  const style = document.createElement('style');
  style.id = 'svg-icon-runtime-style';
  style.textContent = `
    .ui-svg-icon {
      width: 22px;
      height: 22px;
      display: block;
      flex: none;
      color: currentColor;
    }

    .topbar-svg-icon {
      width: 21px;
      height: 21px;
      margin: auto;
    }

    .search-box .ui-svg-icon {
      width: 18px;
      height: 18px;
    }

    #sortBtn {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
    }

    #sortBtn .ui-svg-icon {
      width: 15px;
      height: 15px;
    }

    .empty-icon .ui-svg-icon {
      width: 27px;
      height: 27px;
    }

    .connect-action strong {
      display: grid;
      place-items: center;
      font-size: 0;
    }

    .connect-action strong .ui-svg-icon {
      width: 25px;
      height: 25px;
    }

    .connect-status-icon .ui-svg-icon {
      width: 23px;
      height: 23px;
    }

    .connect-choice .choice-icon {
      display: grid;
      place-items: center;
      font-size: 0;
    }

    .connect-choice .choice-icon .ui-svg-icon {
      width: 22px;
      height: 22px;
    }

    .connect-choice .arrow .ui-svg-icon {
      width: 18px;
      height: 18px;
    }

    .connect-back {
      display: inline-flex !important;
      align-items: center;
      gap: 6px;
    }

    .connect-back::before {
      content: none !important;
    }

    .connect-back .ui-svg-icon {
      width: 16px;
      height: 16px;
    }
  `;
  document.head.appendChild(style);
}

function applyAllSvgIcons() {
  applyTopbarBrandSpacing();
  applyDrawerIcons();
  applySvgIconStyles();
  applyMainSvgIcons();
}

if (document.readyState === 'loading') {
  document.addEventListener(
    'DOMContentLoaded',
    applyAllSvgIcons,
    { once: true }
  );
} else {
  applyAllSvgIcons();
}

// Pages such as Pair/Connect/Logout are loaded into the modal after startup.
// Re-apply only when the DOM actually changes so dynamically inserted controls use the same SVG set.
if (typeof MutationObserver !== 'undefined') {
  let iconObserverQueued = false;

  const iconObserver = new MutationObserver(() => {
    if (iconObserverQueued) return;

    iconObserverQueued = true;
    queueMicrotask(() => {
      iconObserverQueued = false;
      applyAllSvgIcons();
    });
  });

  iconObserver.observe(document.body, {
    childList: true,
    subtree: true
  });
}
