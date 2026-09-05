# Indoone Branding

This folder is the single source of truth for the Indoone visual identity.

- `indoone-mark.svg` — shared in-app logo for the top bar, drawer, login, signup, and other normal light-background surfaces.
- `indoone-mark-white.svg` — white mark for dark or purple surfaces.
- `indoone-splash.svg` — dedicated splash artwork.
- `indoone-app-icon.svg` — master artwork for the Android launcher icon.
- `branding.js` — runtime connector that replaces legacy `.brand-mark` and `.auth-mark` text placeholders with the shared mark asset.

Android launcher and splash resources use platform-native vector resources derived from the same visual design so the APK does not depend on runtime asset loading during startup.
