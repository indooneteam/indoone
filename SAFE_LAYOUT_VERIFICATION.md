# Indoone Safe Layout — Final Verification

## Architecture now in place

- Android `WindowInsetsCompat` is converted in one place: `SafeLayoutContract`.
- `MainActivity` consumes those shared safe inset values and publishes them to the WebView.
- Web safe-area variables are centralized under `app/safe-layout/`.
- Header visual rules are centralized in `app/safe-layout/header.css`.
- The old `topbar-layout-fix.css` path is retained only as a compatibility shim so existing references do not break.

## Verification matrix

The final device verification should be performed on a real Android 14 device and a real Android 15 device, plus both navigation modes where available.

| Environment | Expected result |
|---|---|
| Android 14 | Existing header and bottom navigation remain visually correct |
| Android 15 | Header/action area stays in the header; no system UI overlap |
| Gesture navigation | Bottom navigation stays above the gesture safe area |
| 3-button navigation | Bottom navigation stays above the navigation buttons |
| Display cutout/notch | Header content remains inside the safe top area |
| Compact width | Header remains on one row and content remains usable |
| Large width | 430px app shell remains centered |
| Scroll | Content does not appear in the system-navigation gap |

## Scope protection

No Firebase, TOTP, Nearby, biometric, camera permission, update, or authentication functionality is intentionally changed by the safe-layout migration.
