window.IndooneAutoLock = (() => {
  const KEY = 'indoone.settings.auto_lock_minutes.v1';
  const DEFAULT = 5;
  let lastActivity = Date.now();
  let lockInProgress = false;

  function appLockEnabled() {
    return Boolean(window.IndoonePersistence?.hasAppLock?.());
  }

  function minutes() {
    if (!appLockEnabled()) return 0;

    const raw = localStorage.getItem(KEY);
    if (raw === null) {
      localStorage.setItem(KEY, String(DEFAULT));
      return DEFAULT;
    }

    const value = Number.parseInt(raw, 10);

    return Number.isFinite(value) &&
      [1, 5, 15].includes(value)
      ? value
      : DEFAULT;
  }

  function setMinutes(value) {
    const next = Number.parseInt(value, 10);

    if (!appLockEnabled()) {
      localStorage.setItem(KEY, String(DEFAULT));
      touch();
      return;
    }

    const safeValue = [1, 5, 15].includes(next)
      ? next
      : DEFAULT;

    localStorage.setItem(KEY, String(safeValue));
    touch();
  }

  function touch() {
    lastActivity = Date.now();
  }

  function check() {
    const limit = minutes();

    if (
      !limit ||
      !appLockEnabled() ||
      lockInProgress ||
      window.IndooneSecureSession?.isLocked?.()
    ) {
      return;
    }

    if (Date.now() - lastActivity < limit * 60000) {
      return;
    }

    lockInProgress = true;
    window.lockIndoone?.();

    setTimeout(() => {
      lockInProgress = false;
      touch();
    }, 1000);
  }

  [
    'pointerdown',
    'keydown',
    'touchstart',
    'click',
    'scroll'
  ].forEach(type => {
    document.addEventListener(
      type,
      touch,
      { passive: true }
    );
  });

  document.addEventListener(
    'visibilitychange',
    () => {
      if (!document.hidden) {
        touch();
      }
    }
  );

  setInterval(check, 5000);

  window.showAutoLock = function () {
    const current = minutes();

    if (!appLockEnabled()) {
      openModal(`
        <div class="modal-head">
          <h2>Auto-Lock</h2>
          <button class="close-btn" data-close>×</button>
        </div>
        <p>
          Auto-Lock becomes available after App Lock is enabled.
        </p>
      `);
      return;
    }

    openModal(`
      <div class="modal-head">
        <h2>Auto-Lock</h2>
        <button class="close-btn" data-close>×</button>
      </div>
      <p>
        Automatically lock Indoone after it has been inactive.
      </p>
      <div class="settings-choice-list">
        ${
          [1, 5, 15]
            .map(
              value => `
                <button
                  type="button"
                  class="settings-choice ${
                    current === value ? 'selected' : ''
                  }"
                  data-auto-lock="${value}"
                >
                  <span>
                    After ${value} minute${value === 1 ? '' : 's'}
                  </span>
                  <b>
                    ${current === value ? '✓' : '›'}
                  </b>
                </button>
              `
            )
            .join('')
        }
      </div>
    `);

    document
      .querySelectorAll('[data-auto-lock]')
      .forEach(button =>
        button.addEventListener(
          'click',
          () => {
            setMinutes(button.dataset.autoLock);
            closeModal();

            toast(
              `Auto-Lock set to ${minutes()} minute${
                minutes() === 1 ? '' : 's'
              }`
            );
          }
        )
      );
  };

  return {
    minutes,
    setMinutes,
    touch
  };
})();