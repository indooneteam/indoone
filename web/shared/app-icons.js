// The app's icons. Same shapes as the Material icons the app draws, so the
// web and the app show the same thing.
(() => {
  const ICONS = {
    // Icons.Outlined.Mic
    mic: [
      '<path d="M12 14.5a3.2 3.2 0 0 0 3.2-3.2V5.6a3.2 3.2 0 0 0-6.4 0v5.7a3.2 3.2 0 0 0 3.2 3.2Z"></path>',
      '<path d="M17.6 11.3a5.6 5.6 0 0 1-11.2 0H5a7 7 0 0 0 6 6.93V21h2v-2.77A7 7 0 0 0 19 11.3Z"></path>'
    ].join(''),

    // Icons.Outlined.MicOff
    micOff: [
      '<path d="M3.3 2 2 3.3l6.8 6.8v1.2a3.2 3.2 0 0 0 4.9 2.75l1 1a4.6 4.6 0 0 1-6.3-4.35H6.4a5.6 5.6 0 0 0 4.6 5.5V21h2v-4.77c.6-.09 1.16-.26 1.68-.5L20.7 21 22 19.7Z"></path>',
      '<path d="M15.2 10.4V5.6a3.2 3.2 0 0 0-5.9-1.7l5.7 5.7Z"></path>'
    ].join(''),

    // Icons.Outlined.PhotoCamera
    photoCamera: [
      '<path d="M9.1 4 7.4 6H4a2 2 0 0 0-2 2v9.6a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-3.4L14.9 4Z"></path>',
      '<circle cx="12" cy="13" r="3.4"></circle>'
    ].join(''),

    // Icons.Outlined.Photo
    photo: [
      '<path d="M4.2 4h15.6A1.2 1.2 0 0 1 21 5.2v13.6a1.2 1.2 0 0 1-1.2 1.2H4.2A1.2 1.2 0 0 1 3 18.8V5.2A1.2 1.2 0 0 1 4.2 4Z"></path>',
      '<path d="M5.4 17.4 9.6 11l3 4 2.4-3 3.6 5.4Z"></path>',
      '<circle cx="8.4" cy="8.4" r="1.4"></circle>'
    ].join(''),

    // Icons.Outlined.AttachFile
    attachFile: [
      '<path d="M16.6 6.3v10.2a4.6 4.6 0 0 1-9.2 0V7.1a3 3 0 0 1 6 0v9.4a1.4 1.4 0 0 1-2.8 0V7.6"></path>'
    ].join(''),

    // Icons.Outlined.VolumeUp
    volumeUp: [
      '<path d="M4 9.4h3.4L12 5.3v13.4L7.4 14.6H4Z"></path>',
      '<path d="M15.6 9a4.4 4.4 0 0 1 0 6"></path>',
      '<path d="M18 6.4a7.6 7.6 0 0 1 0 11.2"></path>'
    ].join(''),

    // Icons.Outlined.VolumeOff
    volumeOff: [
      '<path d="M4 9.4h3.4L12 5.3v13.4L7.4 14.6H4Z"></path>',
      '<path d="m16.4 9.6 4.2 4.2"></path>',
      '<path d="m20.6 9.6-4.2 4.2"></path>'
    ].join(''),

    // Icons.Outlined.Close
    close: [
      '<path d="M18.3 5.71 16.89 4.29 12 9.17 7.11 4.29 5.7 5.71 10.59 10.59 5.7 15.47 7.11 16.89 12 12l4.89 4.89 1.41-1.42L13.41 10.59Z"></path>'
    ].join(''),

    // Icons.Outlined.NotificationsNone
    notificationsNone: [
      '<path d="M12 3.6a5.4 5.4 0 0 0-5.4 5.4v3.4L5.2 16h13.6l-1.4-3.6V9A5.4 5.4 0 0 0 12 3.6Z"></path>',
      '<path d="M9.8 19a2.2 2.2 0 0 0 4.4 0"></path>'
    ].join(''),

    // Icons.Filled.Notifications
    notifications: [
      '<path d="M12 3.2A5.7 5.7 0 0 0 6.3 8.9v3.5L4.8 16h14.4l-1.5-3.6V8.9A5.7 5.7 0 0 0 12 3.2Z" fill="currentColor" stroke="none"></path>',
      '<path d="M9.8 19a2.2 2.2 0 0 0 4.4 0"></path>'
    ].join(''),

    // Icons.AutoMirrored.Outlined.ArrowBack
    arrowBack: [
      '<path d="M19 11H7.8l4.6-4.6L11 5l-7 7 7 7 1.4-1.4L7.8 13H19Z"></path>'
    ].join('')
  };

  const FILLED = new Set(['notifications']);

  function make(name, className = 'app-icon') {
    const svg = document.createElementNS(
      'http://www.w3.org/2000/svg',
      'svg'
    );

    svg.setAttribute('class', className);
    svg.setAttribute('data-app-icon', name);
    svg.setAttribute('viewBox', '0 0 24 24');
    svg.setAttribute('aria-hidden', 'true');
    svg.setAttribute('focusable', 'false');
    svg.setAttribute('fill', 'none');
    svg.setAttribute('stroke', 'currentColor');
    svg.setAttribute('stroke-width', '1.7');
    svg.setAttribute('stroke-linecap', 'round');
    svg.setAttribute('stroke-linejoin', 'round');
    svg.innerHTML = ICONS[name] || '';

    if (FILLED.has(name)) {
      svg.setAttribute('stroke-width', '1.7');
    }

    return svg;
  }

  // Any element carrying data-app-icon gets its icon, and keeps getting it as
  // the page changes.
  function apply(root = document) {
    root.querySelectorAll('[data-app-icon]').forEach(element => {
      if (element.dataset.appIconDone === 'true') {
        return;
      }

      element.dataset.appIconDone = 'true';
      element.textContent = '';
      element.appendChild(
        make(element.dataset.appIcon, element.dataset.appIconClass || 'app-icon')
      );
    });
  }

  window.IndooneIcons = { make, apply, names: Object.keys(ICONS) };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => apply(), { once: true });
  } else {
    apply();
  }

  if (typeof MutationObserver !== 'undefined') {
    let queued = false;

    const observer = new MutationObserver(() => {
      if (queued) return;

      queued = true;
      queueMicrotask(() => {
        queued = false;
        apply();
      });
    });

    observer.observe(document.body, { childList: true, subtree: true });
  }
})();
