// The Notification Center, built from NotificationCenterScreen.kt.
//
// The app's notifications arrive as push messages and are kept on the device,
// so there is nothing to read for an account yet. This reads
// users/<uid>/notifications, so the moment anything writes there it appears,
// and until then the app's own empty state is what shows.
(() => {
  const CATEGORIES = {
    security: { title: 'Security', icon: '⛨' },
    ai: { title: 'AI & Chat', icon: '✦' },
    vibe: { title: 'Vibe', icon: '◍' },
    accounts: { title: 'Accounts', icon: '▦' },
    integrations: { title: 'Integrations', icon: '⌘' },
    updates: { title: 'Updates', icon: '↻' }
  };

  let host = null;

  function when(receivedAt) {
    const delta = Date.now() - Number(receivedAt || 0);

    if (delta < 60000) return 'Just now';
    if (delta < 3600000) return `${Math.floor(delta / 60000)} min ago`;
    if (delta < 86400000) return `${Math.floor(delta / 3600000)} h ago`;

    return new Date(Number(receivedAt)).toLocaleDateString();
  }

  function row(item) {
    const category = CATEGORIES[item.category] || CATEGORIES.updates;
    const wrap = document.createElement('div');
    wrap.className = item.isRead ? 'notification-row' : 'notification-row unread';

    const icon = document.createElement('span');
    icon.className = 'notification-icon';
    icon.textContent = category.icon;

    const body = document.createElement('span');
    body.className = 'notification-body';

    const title = document.createElement('b');
    title.textContent = item.title || category.title;

    const text = document.createElement('span');
    text.textContent = item.body || '';

    const stamp = document.createElement('i');
    stamp.textContent = when(item.receivedAt);

    body.appendChild(title);
    body.appendChild(text);
    body.appendChild(stamp);
    wrap.appendChild(icon);
    wrap.appendChild(body);
    return wrap;
  }

  async function load() {
    const list = document.getElementById('notificationsList');
    const empty = document.getElementById('notificationsEmpty');

    if (!list) return;

    list.textContent = '';
    let items = [];

    try {
      const user = window.IndooneFirebase?.auth?.().currentUser;

      if (user) {
        const snapshot = await window.IndooneFirebase
          .database()
          .ref('users')
          .child(user.uid)
          .child('notifications')
          .get();
        const value = snapshot.val() || {};

        items = Object.entries(value)
          .map(([id, entry]) => ({ id, ...entry }))
          .sort((a, b) => Number(b.receivedAt || 0) - Number(a.receivedAt || 0));
      }
    } catch (error) {
      console.warn('Indoone notifications could not be read:', error);
    }

    items.forEach(item => list.appendChild(row(item)));
    empty?.toggleAttribute('hidden', items.length > 0);
  }

  async function markAllRead() {
    try {
      const user = window.IndooneFirebase?.auth?.().currentUser;

      if (!user) return;

      const ref = window.IndooneFirebase
        .database()
        .ref('users')
        .child(user.uid)
        .child('notifications');
      const snapshot = await ref.get();
      const value = snapshot.val() || {};
      const updates = {};

      Object.keys(value).forEach(id => {
        updates[`${id}/isRead`] = true;
      });

      if (Object.keys(updates).length) {
        await ref.update(updates);
      }
    } catch (error) {
      console.warn('Indoone notifications could not be marked read:', error);
    }

    void load();
  }

  // The app treats this as its own page, so the chat page goes away while it
  // is up. It used to leave the chat in place, and the chat covered it.
  function show() {
    if (!host) return;

    document.getElementById('homeContent')?.setAttribute('hidden', '');
    document.getElementById('content')?.setAttribute('hidden', '');
    document.getElementById('connectContent')?.setAttribute('hidden', '');
    document.getElementById('addBtn')?.setAttribute('hidden', '');
    host.hidden = false;
    void load();
    void paintBell();
  }

  function hide() {
    if (host) {
      host.hidden = true;
    }

    window.showHome?.();
  }

  // The app fills the bell when something is unread and leaves it outlined
  // when nothing is.
  async function paintBell() {
    const button = document.getElementById('notificationsBtn');

    if (!button) return;

    let unread = false;

    try {
      const user = window.IndooneFirebase?.auth?.().currentUser;

      if (user) {
        const snapshot = await window.IndooneFirebase
          .database()
          .ref('users')
          .child(user.uid)
          .child('notifications')
          .get();
        const value = snapshot.val() || {};

        unread = Object.values(value).some(entry => !entry?.isRead);
      }
    } catch (error) {
      unread = false;
    }

    const holder = button.querySelector('[data-app-icon]') ||
      button.querySelector('.app-icon');

    if (holder) {
      holder.dataset.appIcon = unread ? 'notifications' : 'notificationsNone';
      holder.dataset.appIconDone = 'false';
      window.IndooneIcons?.apply?.(button);
    }
  }

  function init() {
    host = document.querySelector('[data-notifications-page]');

    document
      .getElementById('notificationsBack')
      ?.addEventListener('click', hide);
    document
      .getElementById('notificationsMark')
      ?.addEventListener('click', markAllRead);

    host?.setAttribute('hidden', '');
  }

  window.IndooneNotifications = { init, show, hide, paintBell };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init, { once: true });
  } else {
    init();
  }
})();
