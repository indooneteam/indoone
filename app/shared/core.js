const overlay = document.getElementById('overlay');
const modal = document.getElementById('modal');

window.IndoonePageState = window.IndoonePageState || {
  key: 'indoone_current_page',

  set(page) {
    try {
      sessionStorage.setItem(
        this.key,
        String(page || 'home')
      );
    } catch (_) {}
  },

  get() {
    try {
      return sessionStorage.getItem(this.key) || '';
    } catch (_) {
      return '';
    }
  },

  clear() {
    try {
      sessionStorage.removeItem(this.key);
    } catch (_) {}
  }
};

function clearHomeSubRoute() {
  const hash = window.location.hash || '';

  if (!hash.startsWith('#home/add-account')) return;

  history.replaceState(
    {},
    '',
    window.location.pathname + window.location.search
  );
}

function appLockBlocksAccess() {
  return Boolean(
    window.IndoonePersistence?.hasAppLock?.() &&
    !window.IndoonePersistence?.isUnlocked?.()
  );
}

window.openModal = function (html) {
  if (!modal || !overlay) return;

  modal.innerHTML = html;
  overlay.classList.remove('hidden');
};

window.closeOverlay = function (event) {
  if (event.target === overlay) {
    if (appLockBlocksAccess()) return;
    overlay.classList.add('hidden');
  }
};

window.closeModal = function () {
  if (appLockBlocksAccess()) return;
  overlay?.classList.add('hidden');
};

window.showHome = function () {
  if (appLockBlocksAccess()) return;

  clearHomeSubRoute();
  window.IndoonePageState?.set('home');
  overlay?.classList.add('hidden');
  document.getElementById('connectContent')?.setAttribute('hidden', '');
  document.getElementById('content')?.removeAttribute('hidden');
  document.getElementById('addBtn')?.removeAttribute('hidden');
  document.getElementById('searchWrap')?.removeAttribute('hidden');

  document
    .querySelectorAll('.bottom-nav button')
    .forEach(button => button.classList.remove('active'));

  document.getElementById('accountsNav')?.classList.add('active');

  window.IndooneHome?.restoreHome?.();
};

window.toast = function (message) {
  let t = document.getElementById('toast');

  if (!t) {
    t = document.createElement('div');
    t.id = 'toast';
    t.style.cssText =
      'position:fixed;left:50%;bottom:88px;transform:translateX(-50%);background:#19151f;color:#fff;padding:11px 16px;border-radius:12px;font-size:13px;z-index:9999;box-shadow:0 8px 30px #0003;';
    document.body.appendChild(t);
  }

  t.textContent = message;
  t.style.display = 'block';
  clearTimeout(window.__toastTimer);
  window.__toastTimer = setTimeout(
    () => {
      t.style.display = 'none';
    },
    1800
  );
};

(() => {
  const existing = document.querySelector('script[data-indoone-branding]');

  if (existing) return;

  const script = document.createElement('script');
  script.src = 'assets/branding/branding.js?v=20260906-stable-logo-2';
  script.async = false;
  script.dataset.indooneBranding = 'true';
  document.head.appendChild(script);
})();
