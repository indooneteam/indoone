package com.indoone.authenticator.layout;

import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;

/**
 * Single place for converting Android WindowInsets into Indoone's
 * device-agnostic safe-layout values.
 *
 * This class is intentionally side-effect free. MainActivity can continue to
 * own the WebView and lifecycle while this class owns only inset selection.
 */
public final class SafeLayoutContract {
    private SafeLayoutContract() {
        // Utility class.
    }

    public static Insets getSafeInsets(WindowInsetsCompat insets) {
        Insets systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
        );
        Insets tappable = insets.getInsets(
                WindowInsetsCompat.Type.tappableElement()
        );
        Insets cutout = insets.getInsets(
                WindowInsetsCompat.Type.displayCutout()
        );

        return Insets.of(
                Math.max(systemBars.left, cutout.left),
                Math.max(systemBars.top, cutout.top),
                Math.max(systemBars.right, cutout.right),
                Math.max(systemBars.bottom, tappable.bottom)
        );
    }
}
