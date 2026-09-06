(() => {
  const EXACT_LOGO_PARTS = [
    'assets/branding/exact-logo-v2/part-01.txt',
    'assets/branding/exact-logo-v2/part-02.txt'
  ];

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

    image.width = className === 'brand-mark' ? 42 : 82;
    image.height = className === 'brand-mark' ? 42 : 82;

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
        padding: 0 !important;
      }

      .brand-mark.branding-image {
        width: 42px !important;
        height: 42px !important;
        min-width: 42px !important;
        min-height: 42px !important;
        max-width: none !important;
        max-height: none !important;
        border-radius: 0 !important;
        transform: none !important;
      }

      .auth-mark.branding-image {
        width: 82px !important;
        height: 82px !important;
        min-width: 82px !important;
        min-height: 82px !important;
        max-width: none !important;
        max-height: none !important;
        border-radius: 0 !important;
        transform: none !important;
      }

      .topbar-left .brand {
        min-height: 48px !important;
        overflow: visible !important;
        align-items: center !important;
        line-height: 0 !important;
      }
    `;

    document.head.appendChild(style);
  }

  async function loadExactLogo() {
    const responses = await Promise.all(
      EXACT_LOGO_PARTS.map(async part => {
        const response = await fetch(
          `${part}?v=20260906-exact-raster-4`,
          { cache: 'no-store' }
        );

        if (!response.ok) {
          throw new Error(`Logo part failed: ${part}`);
        }

        return response.text();
      })
    );

    masterMarkSrc = `data:image/png;base64,${responses
      .join('')
      .replace(/\s+/g, '')}`;
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
    document.addEventListener('DOMContentLoaded', init, { once: true });
  } else {
    init();
  }
})();
