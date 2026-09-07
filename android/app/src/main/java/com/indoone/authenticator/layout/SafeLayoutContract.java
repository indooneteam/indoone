package com.indoone.authenticator.layout;

import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;

/**
 * Single place for converting Android WindowInsets into Indoone's
 * device-agnostic safe-layout values.
 *
 * This class is intentionally side-effect free. MainActivity owns the
 * WebView/lifecycle while this class owns only inset selection.
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
        Insets systemGestures = insets.getInsets(
                WindowInsetsCompat.Type.systemGestures()
        );
        Insets mandatoryGestures = insets.getInsets(
                WindowInsetsCompat.Type.mandatorySystemGestures()
        );

        return Insets.of(
                max(systemBars.left, cutout.left, systemGestures.left, mandatoryGestures.left),
                max(systemBars.top, cutout.top),
                max(systemBars.right, cutout.right, systemGestures.right, mandatoryGestures.right),
                max(systemBars.bottom, tappable.bottom, systemGestures.bottom, mandatoryGestures.bottom)
        );
    }

    private static int max(int... values) {
        int result = 0;
        for (int value : values) {
            result = Math.max(result, value);
        }
        return result;
    }
}
