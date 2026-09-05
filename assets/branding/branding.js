(() => {
  const MARK_SRC = 'assets/branding/indoone-mark.svg';
  const WHITE_MARK_SRC = 'assets/branding/indoone-mark-white.svg';

  function createImage(source, className, alt) {
    const image = document.createElement('img');
    image.src = source;
    image.alt = alt;
    image.className = `${className} branding-image`;
    image.setAttribute('aria-hidden', alt ? 'false' : 'true');
    image.setAttribute('draggable', 'false');
    image.decoding = 'async';
    return image;
  }

  function wireElement(element, source, alt) {
    if (!element || element.tagName === 'IMG') return;

    const className = element.classList.contains('auth-mark')
      ? 'auth-mark'
      : 'brand-mark';

    const image = createImage(source, className, alt);

    if (className === 'brand-mark') {
      image.width = 34;
      image.height = 34;
    } else {
      image.width = 58;
      image.height = 58;
    }

    element.replaceWith(image);
  }

  function wire(root = document) {
    root.querySelectorAll?.('.brand-mark').forEach(element => {
      wireElement(element, MARK_SRC, 'Indoone logo');
    });

    root.querySelectorAll?.('.auth-mark').forEach(element => {
      wireElement(element, MARK_SRC, 'Indoone logo');
    });
  }

  function injectStyles() {
    if (document.getElementById('indoone-branding-styles')) return;

    const style = document.createElement('style');
    style.id = 'indoone-branding-styles';
    style.textContent = `
      .branding-image {
        display: block !important;
        flex: 0 0 auto !important;
        object-fit: contain !important;
        object-position: center !important;
        overflow: hidden !important;
      }

      .brand-mark.branding-image {
        width: 34px !important;
        height: 34px !important;
        border-radius: 11px !important;
        background: transparent !important;
        box-shadow: none !important;
      }

      .auth-mark.branding-image {
        width: 58px !important;
        height: 58px !important;
        border-radius: 17px !important;
        background: transparent !important;
        box-shadow: none !important;
      }
    `;

    document.head.appendChild(style);
  }

  function init() {
    injectStyles();
    wire(document);

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

    window.IndooneBranding = {
      wire,
      mark: MARK_SRC,
      whiteMark: WHITE_MARK_SRC
    };
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init, { once: true });
  } else {
    init();
  }
})();
