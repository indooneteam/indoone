(() => {
  const THRESHOLD = 72;
  let startX = 0;
  let startY = 0;
  let activeWrap = null;
  let swiping = false;

  async function runAction(wrap, direction) {
    const id = Number(
      wrap.querySelector('.account')?.dataset.id
    );

    if (!id) return;

    wrap.classList.remove('swiping-left', 'swiping-right');
    wrap
      .querySelector('.account')
      ?.classList.add('swipe-success');

    try {
      if (direction === 'left') {
        await window.toggleFavorite(id);
      } else {
        const account = window.indooneState?.accounts?.find(
          a => Number(a.id) === id
        );

        if (!account) return;

        await window.IndooneCloudAccounts.moveToTrash(account);

        const index = window.indooneState.accounts.findIndex(
          a => Number(a.id) === id
        );

        if (index >= 0) {
          window.indooneState.accounts.splice(index, 1);
        }

        window.renderAccounts();
        window.toast('Account moved to Trash for 30 days');
      }
    } catch (error) {
      window.toast(error?.message || 'Action failed');
      window.renderAccounts();
    }
  }

  function reset(wrap) {
    if (!wrap) return;

    wrap.classList.remove('swiping-left', 'swiping-right');
    wrap
      .querySelector('.account')
      ?.classList.remove('swipe-active', 'swipe-success');
  }

  document.addEventListener(
    'touchstart',
    event => {
      const wrap = event.target.closest('.account-swipe-wrap');

      if (!wrap || event.touches.length !== 1) return;

      activeWrap = wrap;
      swiping = false;
      startX = event.touches[0].clientX;
      startY = event.touches[0].clientY;
    },
    { passive: true }
  );

  document.addEventListener(
    'touchmove',
    event => {
      if (!activeWrap || event.touches.length !== 1) return;

      const dx = event.touches[0].clientX - startX;
      const dy = event.touches[0].clientY - startY;

      if (Math.abs(dx) < 10 || Math.abs(dx) < Math.abs(dy)) {
        return;
      }

      swiping = true;

      const card = activeWrap.querySelector('.account');

      if (!card) return;

      card.classList.add('swipe-active');
      activeWrap.classList.toggle('swiping-left', dx < -12);
      activeWrap.classList.toggle('swiping-right', dx > 12);
    },
    { passive: true }
  );

  document.addEventListener(
    'touchend',
    async event => {
      if (!activeWrap) return;

      const wrap = activeWrap;
      activeWrap = null;
      const dx =
        event.changedTouches?.[0]?.clientX - startX;
      const direction =
        dx < -THRESHOLD
          ? 'left'
          : dx > THRESHOLD
            ? 'right'
            : '';

      if (!swiping || !direction) {
        reset(wrap);
        return;
      }

      if (direction === 'left') {
        await runAction(wrap, 'left');
      } else {
        await runAction(wrap, 'right');
      }

      swiping = false;
    },
    { passive: true }
  );

  document.addEventListener(
    'touchcancel',
    () => {
      reset(activeWrap);
      activeWrap = null;
      swiping = false;
    },
    { passive: true }
  );

  document.addEventListener(
    'click',
    event => {
      if (!swiping) return;

      event.preventDefault();
      event.stopPropagation();
      swiping = false;
    },
    true
  );
})();
