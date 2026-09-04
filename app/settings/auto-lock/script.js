window.IndooneAutoLock = (() => {
  const KEY = 'indoone.settings.auto_lock_minutes.v1';
  const DEFAULT = 0;
  let lastActivity = Date.now();
  let lockInProgress = false;

  function minutes() {
    const value = Number.parseInt(localStorage.getItem(KEY) || String(DEFAULT), 10);
    return Number.isFinite(value) && [0, 1, 5, 15].includes(value) ? value : DEFAULT;
  }
  function setMinutes(value) {
    const next = Number.parseInt(value, 10);
    localStorage.setItem(KEY, String([0, 1, 5, 15].includes(next) ? next : DEFAULT));
    touch();
  }
  function touch() { lastActivity = Date.now(); }
  function check() {
    const limit = minutes();
    if (!limit || lockInProgress || window.IndooneSecureSession?.isLocked?.()) return;
    if (Date.now() - lastActivity < limit * 60000) return;
    lockInProgress = true;
    window.lockIndoone?.();
    setTimeout(() => { lockInProgress = false; touch(); }, 1000);
  }

  ['pointerdown', 'keydown', 'touchstart', 'click', 'scroll'].forEach(type => {
    document.addEventListener(type, touch, { passive: true });
  });
  document.addEventListener('visibilitychange', () => { if (!document.hidden) touch(); });
  setInterval(check, 5000);

  window.showAutoLock = function () {
    const current = minutes();
    openModal(`<div class="modal-head"><h2>Auto-Lock</h2><button class="close-btn" data-close>×</button></div><p>Automatically lock the encrypted vault when Indoone has been inactive.</p><div class="settings-choice-list">${[0,1,5,15].map(value => `<button type="button" class="settings-choice ${current === value ? 'selected' : ''}" data-auto-lock="${value}"><span>${value === 0 ? 'Never' : `After ${value} minute${value === 1 ? '' : 's'}`}</span><b>${current === value ? '✓' : '›'}</b></button>`).join('')}</div>`);
    document.querySelectorAll('[data-auto-lock]').forEach(button => button.addEventListener('click', () => {
      setMinutes(button.dataset.autoLock);
      closeModal();
      toast(minutes() ? `Auto-Lock set to ${minutes()} minute${minutes() === 1 ? '' : 's'}` : 'Auto-Lock disabled');
    }));
  };

  return { minutes, setMinutes, touch };
})();