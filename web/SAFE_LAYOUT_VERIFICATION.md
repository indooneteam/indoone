# Safe Layout Verification

The app now uses one canonical native safe-area pipeline: Android `SafeLayoutContract` selects the effective system/cutout/tappable insets, `MainActivity` publishes the normalized values to the WebView, and the web layer consumes those values through the shared safe-layout variables.

Verification target for the next release:
- small portrait phone
- normal portrait phone
- tall portrait phone
- notch/cutout phone
- gesture navigation
- 3-button navigation
- landscape

Header and bottom-navigation geometry should be treated as component geometry; native safe-area values must not be counted twice as both component height and padding.
