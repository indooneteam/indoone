(() => {
  const EXACT_LOGO_PARTS = [
    'assets/branding/exact-logo-v2/part-01.txt',
    'assets/branding/exact-logo-v2/part-02.txt'
  ];

  const LOGO_PADDING = 8;
  const MIN_ALPHA = 8;
  const MIN_COMPONENT_SIZE = 4;

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

    image.width = className === 'brand-mark' ? 38 : 76;
    image.height = className === 'brand-mark' ? 38 : 76;

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

  function cleanupLogoPixels(sourceCanvas) {
    const context = sourceCanvas.getContext('2d', {
      willReadFrequently: true
    });

    if (!context) return sourceCanvas;

    const { width, height } = sourceCanvas;
    const imageData = context.getImageData(0, 0, width, height);
    const pixels = imageData.data;
    const visited = new Uint8Array(width * height);
    const keep = new Uint8Array(width * height);
    const indexFor = (x, y) => y * width + x;

    for (let y = 0; y < height; y += 1) {
      for (let x = 0; x < width; x += 1) {
        const start = indexFor(x, y);
        if (visited[start]) continue;

        visited[start] = 1;

        if (pixels[start * 4 + 3] < MIN_ALPHA) {
          continue;
        }

        const queue = [start];
        const component = [];

        while (queue.length) {
          const current = queue.pop();
          component.push(current);

          const cx = current % width;
          const cy = Math.floor(current / width);
          const neighbors = [
            [cx - 1, cy],
            [cx + 1, cy],
            [cx, cy - 1],
            [cx, cy + 1]
          ];

          for (const [nx, ny] of neighbors) {
            if (
              nx < 0 ||
              ny < 0 ||
              nx >= width ||
              ny >= height
            ) {
              continue;
            }

            const next = indexFor(nx, ny);
            if (visited[next]) continue;

            visited[next] = 1;

            if (pixels[next * 4 + 3] >= MIN_ALPHA) {
              queue.push(next);
            }
          }
        }

        if (component.length >= MIN_COMPONENT_SIZE) {
          component.forEach(index => {
            keep[index] = 1;
          });
        }
      }
    }

    for (let i = 0; i < width * height; i += 1) {
      if (!keep[i]) {
        pixels[i * 4 + 3] = 0;
      }
    }

    context.putImageData(imageData, 0, 0);
    return sourceCanvas;
  }

  function cleanAndPadLogo(dataUri) {
    return new Promise((resolve, reject) => {
      const image = new Image();

      image.onload = () => {
        try {
          const sourceCanvas = document.createElement('canvas');
          sourceCanvas.width = image.naturalWidth;
          sourceCanvas.height = image.naturalHeight;

          const sourceContext = sourceCanvas.getContext('2d');
          if (!sourceContext) {
            resolve(dataUri);
            return;
          }

          sourceContext.clearRect(
            0,
            0,
            sourceCanvas.width,
            sourceCanvas.height
          );
          sourceContext.drawImage(image, 0, 0);
          cleanupLogoPixels(sourceCanvas);

          const outputCanvas = document.createElement('canvas');
          outputCanvas.width = image.naturalWidth + LOGO_PADDING * 2;
          outputCanvas.height = image.naturalHeight + LOGO_PADDING * 2;

          const outputContext = outputCanvas.getContext('2d');
          if (!outputContext) {
            resolve(dataUri);
            return;
          }

          outputContext.clearRect(
            0,
            0,
            outputCanvas.width,
            outputCanvas.height
          );
          outputContext.drawImage(
            sourceCanvas,
            LOGO_PADDING,
            LOGO_PADDING
          );

          resolve(outputCanvas.toDataURL('image/png'));
        } catch (error) {
          reject(error);
        }
      };

      image.onerror = reject;
      image.src = dataUri;
    });
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

    const sourceDataUri = `data:image/png;base64,${responses
      .join('')
      .replace(/\s+/g, '')}`;

    masterMarkSrc = await cleanAndPadLogo(sourceDataUri);
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
