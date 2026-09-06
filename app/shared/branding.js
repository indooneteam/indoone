(() => {
  const APP_ICON_SOURCE = 'assets/branding/indoone-logo.png';
  const BRAND_VERSION = '20260906-logo-6';
  let logoInstance = 0;

  function createLogo(className, alt = 'Indoone logo') {
    logoInstance += 1;
    const gradientId = `indooneBrandGradient${logoInstance}`;

    const svg = document.createElementNS(
      'http://www.w3.org/2000/svg',
      'svg'
    );

    svg.setAttribute('class', `${className} branding-image`);
    svg.setAttribute('viewBox', '0 0 512 512');
    svg.setAttribute('role', 'img');
    svg.setAttribute('aria-label', alt);
    svg.setAttribute('focusable', 'false');
    svg.setAttribute('preserveAspectRatio', 'xMidYMid meet');
    svg.innerHTML = `
      <defs>
        <linearGradient id="${gradientId}" x1="0" y1="1" x2="1" y2="0">
          <stop offset="0" stop-color="#32106F"></stop>
          <stop offset="0.5" stop-color="#6526C7"></stop>
          <stop offset="1" stop-color="#B277FF"></stop>
        </linearGradient>
      </defs>
      <path d="M126 154 C176 138 258 145 344 115 C382 102 407 81 421 52 L421 112 C411 142 388 161 353 173 C264 203 190 181 126 206 Z" fill="url(#${gradientId})"></path>
      <path d="M126 235 C188 211 258 224 350 193 C387 181 408 162 421 137 L421 198 C411 226 388 245 351 257 C263 286 190 260 126 285 Z" fill="url(#${gradientId})"></path>
      <path d="M126 304 C190 279 258 296 349 265 C386 253 409 235 421 210 L421 270 C411 301 387 323 350 336 C273 364 205 342 166 360 C136 374 126 397 128 419 C130 444 147 457 169 457 C190 457 205 445 205 421 L205 364 C253 378 304 372 351 351 C390 334 413 309 421 281 C418 344 383 389 326 407 C274 423 238 427 208 445 C185 459 154 458 136 440 C114 418 109 387 112 357 C114 336 119 320 126 304 Z" fill="url(#${gradientId})"></path>
    `;

    svg.dataset.indooneBranding = 'true';
    svg.draggable = false;
    return svg;
  }

  function installFavicon() {
    const href = `${APP_ICON_SOURCE}?v=${BRAND_VERSION}`;
    let icon = document.querySelector('link[data-indoone-favicon="true"]');

    if (!icon) {
      icon = document.createElement('link');
      icon.rel = 'icon';
      icon.type = 'image/png';
      icon.dataset.indooneFavicon = 'true';
      document.head.appendChild(icon);
    }

    icon.href = href;
  }

  function wire(root = document) {
    root
      .querySelectorAll?.('.brand-mark, .auth-mark, .about-mark, .token-icon')
      .forEach(element => {
        if (element.dataset.indooneBranding === 'true') return;
        if (element.tagName === 'IMG') return;

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

      .about-mark.branding-image {
        width: 52px !important;
        height: 52px !important;
        margin: 8px auto 12px !important;
      }

      .token-icon.branding-image {
        width: 52px !important;
        height: 52px !important;
        margin: 0 auto !important;
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
    installFavicon();
    wire(document);
    observe();
  }

  window.IndooneBranding = {
    init,
    logoSource: 'inline-svg',
    appIconSource: APP_ICON_SOURCE,
    version: BRAND_VERSION
  };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init, { once: true });
  } else {
    init();
  }
})();
