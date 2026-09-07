/*
 * Indoone Safe Layout runtime hook.
 *
 * Android publishes measured inset values through CSS custom properties.
 * This module intentionally stays passive for now; layout responsibilities
 * will be migrated here only after the native/WebView contract is verified.
 */
(function () {
  'use strict';

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
})();
