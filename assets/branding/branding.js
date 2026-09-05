(() => {
  const MASTER_MARK_SRC = 'assets/branding/indoone-master.svg';

  function createImage(className, alt) {
    const image = document.createElement('img');
    image.src = MASTER_MARK_SRC;
    image.alt = alt;
    image.className = `${className} branding-image`;
    image.setAttribute('aria-hidden', alt ? 'false' : 'true');
    image.setAttribute('draggable', 'false');
    image.decoding = 'async';
    return image;
  }

  function wireElement(element) {
    if (!element || element.tagName === 'IMG') return;

    const className = element.classList.contains('auth-mark')
      ? 'auth-mark'
      : 'brand-mark';

    const image = createImage(className, 'Indoone logo');

    if (className === 'brand-mark') {
      image.width = 38;
      image.height = 38;
    } else {
      image.width = 76;
      image.height = 76;
    }

    element.replaceWith(image);
  }

  function wire(root = document) {
    root.querySelectorAll?.('.brand-mark').forEach(wireElement);
    root.querySelectorAll?.('.auth-mark').forEach(wireElement);
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
        overflow: visible !important;
        background: transparent !important;
        box-shadow: none !important;
      }

      .brand-mark.branding-image {
        width: 38px !important;
        height: 38px !important;
        border-radius: 0 !important;
      }

      .auth-mark.branding-image {
        width: 76px !important;
        height: 76px !important;
        border-radius: 0 !important;
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
      mark: MASTER_MARK_SRC,
      whiteMark: MASTER_MARK_SRC
    };
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init, { once: true });
  } else {
    init();
  }
})();
