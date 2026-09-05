# Indoone Branding

`indoone-master.svg` is the single source of truth for the final Indoone logo.

- `indoone-master.svg` — final master artwork: dark premium purple circle, white `I`, and two lavender leaves above the circle.
- `indoone-mark.svg` — compatibility copy kept aligned with the master artwork.
- `indoone-mark-white.svg` — legacy dark-surface variant kept for compatibility.
- `indoone-splash.svg` — compatibility copy kept aligned with the master artwork.
- `indoone-app-icon.svg` — compatibility copy kept aligned with the master artwork.
- `branding.js` — runtime connector that replaces legacy `.brand-mark` and `.auth-mark` text placeholders with the master logo.

Android launcher and splash resources use native vector resources that reproduce the same master artwork for reliable startup rendering. The Android launcher may still apply the platform's adaptive-icon mask outside the master artwork.
