(() => {
  const LOGO_SOURCE = 'assets/branding/indoone-master.svg';

  function createLogo(className, alt = 'Indoone logo') {
    const image = document.createElement('img');

    image.src = `${LOGO_SOURCE}?v=20260906-logo-1`;
    image.alt = alt;
    image.className = `${className} branding-image`;
    image.draggable = false;
    image.decoding = 'async';

    return image;
  }

  function wire(root = document) {
    root
      .querySelectorAll?.('.brand-mark, .auth-mark')
      .forEach(element => {
        if (element.tagName === 'IMG') return;

        const className = element.classList.contains('auth-mark')
          ? 'auth-mark'
          : 'brand-mark';

        element.replaceWith(createLogo(className));
      });
  }

  function installStyles() {
    if (document.getElementById('indoone-branding-styles')) return;

    const style = document.createElement('style');
    style.id = 'indoone-branding-styles';
    style.textContent = `
      .branding-image {
        display: block !important;
        flex: 0 0 auto !important;
        object-fit: contain !important;
        object-position: center !important;
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

  function observe() {
    if (typeof MutationObserver === 'undefined') return;

    const observer = new MutationObserver(records => {
      records.forEach(record => {
        record.addedNodes.forEach(node => {
          if (node.nodeType === Node.ELEMENT_NODE) wire(node);
        });
      });
    });

    observer.observe(document.body, {
      childList: true,
      subtree: true
    });
  }

  function init() {
    installStyles();
    wire(document);
    observe();
  }

  window.IndooneBranding = {
    init,
    logoSource: LOGO_SOURCE
  };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init, { once: true });
  } else {
    init();
  }
})();
