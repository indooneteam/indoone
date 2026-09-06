(() => {
  const EXACT_LOGO_SOURCE =
    'assets/branding/exact-logo-v3.txt';

  let masterMarkSrc = null;

  function createImage(className, alt) {
    if (!masterMarkSrc) return null;

    const image = document.createElement('img');
    image.src = masterMarkSrc;
    image.alt = alt;
    image.className = `${className} branding-image`;
    image.setAttribute('aria-hidden', alt ? 'false' : 'true');
    image.setAttribute('draggable', 'false');
    image.decoding = 'async';

    return image;
  }

  function wireElement(element) {
    if (!element || element.tagName === 'IMG' || !masterMarkSrc) {
      return;
    }

    const className = element.classList.contains('auth-mark')
      ? 'auth-mark'
      : 'brand-mark';

    const image = createImage(className, 'Indoone logo');
    if (!image) return;

    image.width = className === 'brand-mark' ? 40 : 82;
    image.height = className === 'brand-mark' ? 40 : 82;

    element.replaceWith(image);
  }

  function wire(root = document) {
    root.querySelectorAll?.('.brand-mark').forEach(wireElement);
    root.querySelectorAll?.('.auth-mark').forEach(wireElement);
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
        overflow: visible !important;
        background: transparent !important;
        box-shadow: none !important;
      }

      .brand-mark.branding-image {
        width: 40px !important;
        height: 40px !important;
        max-width: none !important;
        max-height: none !important;
        border-radius: 0 !important;
        margin: 0 !important;
      }

      .auth-mark.branding-image {
        width: 82px !important;
        height: 82px !important;
        max-width: none !important;
        max-height: none !important;
        border-radius: 0 !important;
        margin: 0 !important;
      }

      .topbar-left .brand-mark.branding-image {
        flex: 0 0 40px !important;
      }
    `;

    document.head.appendChild(style);
  }

  async function loadExactLogo() {
    const response = await fetch(
      `${EXACT_LOGO_SOURCE}?v=20260906-padded-master-1`,
      { cache: 'no-store' }
    );

    if (!response.ok) {
      throw new Error(`Logo source failed: ${EXACT_LOGO_SOURCE}`);
    }

    const base64 = (await response.text()).replace(/\s+/g, '');
    masterMarkSrc = `data:image/png;base64,${base64}`;
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

  async function init() {
    injectStyles();

    try {
      await loadExactLogo();
      wire(document);
      startObserver();
    } catch (error) {
      console.error(
        '[Indoone branding] exact logo failed to load',
        error
      );
    }

    window.IndooneBranding = {
      wire,
      mark: () => masterMarkSrc,
      whiteMark: () => masterMarkSrc
    };
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
