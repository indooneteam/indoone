(() => {
  const BRAND_VERSION = '20260906-logo-10';
  let logoInstance = 0;

  function createLogo(className, alt = 'Indoone logo') {
    logoInstance += 1;

    const svg = document.createElementNS(
      'http://www.w3.org/2000/svg',
      'svg'
    );

    const gradientId = `indooneLogoGradient${logoInstance}`;

    svg.setAttribute('class', `${className} branding-image`);
    svg.setAttribute('viewBox', '0 0 48 48');
    svg.setAttribute('role', 'img');
    svg.setAttribute('aria-label', alt);
    svg.setAttribute('focusable', 'false');
    svg.setAttribute('preserveAspectRatio', 'xMidYMid meet');
    svg.dataset.indooneBranding = 'true';
    svg.draggable = false;

    svg.innerHTML = `
      <defs>
        <linearGradient id="${gradientId}" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stop-color="#A855F7"></stop>
          <stop offset="0.52" stop-color="#7C3AED"></stop>
          <stop offset="1" stop-color="#5B21B6"></stop>
        </linearGradient>
      </defs>
      <circle
        cx="24"
        cy="24"
        r="17.5"
        fill="none"
        stroke="url(#${gradientId})"
        stroke-width="3.5"
      ></circle>
      <circle
        cx="24"
        cy="16.25"
        r="3.35"
        fill="url(#${gradientId})"
      ></circle>
      <rect
        x="20.15"
        y="22"
        width="7.7"
        height="14.2"
        rx="3.85"
        fill="url(#${gradientId})"
      ></rect>
    `;

    return svg;
  }

  function installFavicon() {
    const faviconSvg = `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48">
        <defs>
          <linearGradient id="indooneFaviconGradient" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0" stop-color="#A855F7"></stop>
            <stop offset="0.52" stop-color="#7C3AED"></stop>
            <stop offset="1" stop-color="#5B21B6"></stop>
          </linearGradient>
        </defs>
        <circle
          cx="24"
          cy="24"
          r="17.5"
          fill="none"
          stroke="url(#indooneFaviconGradient)"
          stroke-width="3.5"
        ></circle>
        <circle
          cx="24"
          cy="16.25"
          r="3.35"
          fill="url(#indooneFaviconGradient)"
        ></circle>
        <rect
          x="20.15"
          y="22"
          width="7.7"
          height="14.2"
          rx="3.85"
          fill="url(#indooneFaviconGradient)"
        ></rect>
      </svg>
    `;

    let icon = document.querySelector('link[data-indoone-favicon="true"]');

    if (!icon) {
      icon = document.createElement('link');
      icon.rel = 'icon';
      icon.type = 'image/svg+xml';
      icon.dataset.indooneFavicon = 'true';
      document.head.appendChild(icon);
    }

    icon.href = `data:image/svg+xml;charset=utf-8,${encodeURIComponent(faviconSvg)}`;
  }

  function wire(root = document) {
    root
      .querySelectorAll?.('.brand-mark, .auth-mark, .about-mark, .token-icon')
      .forEach(element => {
        if (element.dataset.indooneBranding === 'true') return;

        const className = element.classList.contains('auth-mark')
          ? 'auth-mark'
          : element.classList.contains('about-mark')
            ? 'about-mark'
            : element.classList.contains('token-icon')
              ? 'token-icon'
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
        width: 38px !important;
        height: 38px !important;
        object-fit: contain !important;
        object-position: center !important;
        max-width: none !important;
        max-height: none !important;
        margin: 0 !important;
        padding: 0 !important;
        border: 0 !important;
        border-radius: 0 !important;
        background: transparent !important;
        box-shadow: none !important;
        color: transparent !important;
        font-size: 0 !important;
        overflow: visible !important;
      }

      .topbar-left .branding-image {
        width: 34px !important;
        height: 34px !important;
        align-self: center !important;
      }

      .drawer-brand .branding-image {
        width: 36px !important;
        height: 36px !important;
      }

      .auth-mark.branding-image {
        width: 72px !important;
        height: 72px !important;
      }

      .about-mark.branding-image {
        width: 48px !important;
        height: 48px !important;
        margin: 8px auto 12px !important;
      }

      .token-icon.branding-image {
        width: 48px !important;
        height: 48px !important;
        margin: 0 auto !important;
      }
    `;

    document.head.appendChild(style);
  }

  function observe() {
    if (typeof MutationObserver === 'undefined' || !document.body) return;

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
    installFavicon();
    wire(document);
    observe();
  }

  window.IndooneBranding = {
    init,
    logoSource: 'inline-svg',
    version: BRAND_VERSION
  };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init, { once: true });
  } else {
    init();
  }
})();
