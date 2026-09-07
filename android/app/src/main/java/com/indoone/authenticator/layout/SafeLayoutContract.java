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

        /*
         * Use only regions that can actually occupy or protect the app's
         * layout. System-gesture insets describe gesture touch regions and
         * can be larger than the visible navigation area. Treating them as
         * layout insets creates unnecessary gaps on gesture-navigation
         * devices.
         *
         * This same rule works for gesture navigation, two-button
         * navigation, three-button navigation, cutouts, and rotation
         * without identifying a specific device or Android version.
         */
        return Insets.of(
                max(systemBars.left, cutout.left),
                max(systemBars.top, cutout.top),
                max(systemBars.right, cutout.right),
                max(systemBars.bottom, tappable.bottom, cutout.bottom)
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
