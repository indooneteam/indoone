(() => {
  const LOGO_SOURCE =
    'assets/branding/indoone-master.svg';

  let masterMarkSrc = '';

  function createImage(className, alt) {
    const image = document.createElement('img');

    image.src = masterMarkSrc;
    image.alt = alt;
    image.className = `${className} branding-image`;
    image.setAttribute('draggable', 'false');
    image.decoding = 'async';

    return image;
  }

  function wireElement(element) {
    if (!element || element.tagName === 'IMG') {
      return;
    }

    const className = element.classList.contains('auth-mark')
      ? 'auth-mark'
      : 'brand-mark';

    element.replaceWith(
      createImage(className, 'Indoone logo')
    );
  }

  function wire(root = document) {
    root
      .querySelectorAll?.('.brand-mark, .auth-mark')
      .forEach(wireElement);
  }

  function injectStyles() {
    if (document.getElementById('indoone-branding-styles')) {
      return;
    }

    const style = document.createElement('style');
    style.id = 'indoone-branding-styles';
    style.textContent = `
      .branding-image {
        display: block !important;
        flex: 0 0 auto !important;
        object-fit: contain !important;
        object-position: center !important;
        width: 38px !important;
        height: 38px !important;
        max-width: none !important;
        max-height: none !important;
        margin: 0 !important;
        padding: 0 !important;
        border: 0 !important;
        background: transparent !important;
        box-shadow: none !important;
      }

      .topbar-left .branding-image {
        width: 38px !important;
        height: 38px !important;
        align-self: center !important;
      }

      .drawer-brand .branding-image {
        width: 40px !important;
        height: 40px !important;
      }

      .auth-mark.branding-image {
        width: 82px !important;
        height: 82px !important;
      }
    `;

    document.head.appendChild(style);
  }

  function startObserver() {
    const observer = new MutationObserver(records => {
      for (const record of records) {
        record.addedNodes.forEach(node => {
          if (node.nodeType === Node.ELEMENT_NODE) {
            wire(node);
          }
        });
      }
    });

    observer.observe(document.body, {
      childList: true,
      subtree: true
    });
  }

  function init() {
    injectStyles();

    masterMarkSrc =
      `${LOGO_SOURCE}?v=20260906-stable-logo-1`;

    wire(document);
    startObserver();
  }

  if (document.readyState === 'loading') {
    document.addEventListener(
      'DOMContentLoaded',
      init,
      { once: true }
    );
  } else {
    init();
  }
})();
