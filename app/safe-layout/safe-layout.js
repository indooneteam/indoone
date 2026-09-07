/*
 * Indoone Safe Layout runtime hook.
 *
 * Android publishes measured inset values through CSS custom properties.
 * This module keeps component geometry in CSS. The native WebView bridge may
 * still create its legacy runtime stylesheet; remove it so old hardcoded
 * header/bottom-nav dimensions cannot override the central safe-layout
 * contract.
 */
(function () {
  'use strict';

  var LEGACY_STYLE_ID = 'indoone-native-insets';

  window.IndooneSafeLayout = window.IndooneSafeLayout || {};

  window.IndooneSafeLayout.getInsets = function () {
    var root = document.documentElement;
    var styles = window.getComputedStyle(root);

    return {
      top: styles.getPropertyValue('--indoone-safe-top').trim(),
      right: styles.getPropertyValue('--indoone-safe-right').trim(),
      bottom: styles.getPropertyValue('--indoone-safe-bottom').trim(),
      left: styles.getPropertyValue('--indoone-safe-left').trim()
    };
  };

  function removeLegacyNativeStyle() {
    var legacyStyle = document.getElementById(LEGACY_STYLE_ID);

    if (legacyStyle) {
      legacyStyle.remove();
    }
  }

  removeLegacyNativeStyle();

  if (typeof MutationObserver !== 'undefined') {
    new MutationObserver(function () {
      removeLegacyNativeStyle();
    }).observe(document.documentElement, {
      childList: true,
      subtree: true
    });
  }
})();
