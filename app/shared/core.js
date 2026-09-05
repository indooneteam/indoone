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

  // Home is a canonical root route. Never leave an Add Account child hash
  // behind when switching back to Home, because that hash would be restored
  // on the next refresh.
  clearHomeSubRoute();
  window.IndoonePageState?.set('home');
  overlay?.classList.add('hidden');
  document
    .getElementById('connectContent')
    ?.setAttribute('hidden', '');
  document
    .getElementById('content')
    ?.removeAttribute('hidden');
  document
    .getElementById('addBtn')
    ?.removeAttribute('hidden');
  document
    .getElementById('searchWrap')
    ?.removeAttribute('hidden');

  document
    .querySelectorAll('.bottom-nav button')
    .forEach(button => button.classList.remove('active'));

  document
    .getElementById('accountsNav')
    ?.classList.add('active');

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

// Load the centralized branding connector after the base DOM helpers are ready.
// It replaces every .brand-mark and .auth-mark with the shared logo asset,
// including auth modals that are created later at runtime.
(() => {
  const existing = document.querySelector('script[data-indoone-branding]');

  if (existing) return;

  const script = document.createElement('script');
  script.src = 'assets/branding/branding.js?v=20260906a';
  script.async = false;
  script.dataset.indooneBranding = 'true';
  document.head.appendChild(script);
})();
