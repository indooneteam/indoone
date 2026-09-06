# Indoone Branding

`indoone-master.svg` is the single source of truth for the final Indoone logo.

- `indoone-master.svg` — final master artwork: minimal flowing purple Indoone mark inspired by a clean F shape.
- `indoone-mark.svg` — compatibility copy kept aligned with the master artwork.
- `indoone-splash.svg` — compatibility copy kept aligned with the master artwork.
- `indoone-app-icon.svg` — compatibility copy kept aligned with the master artwork.
- `app/shared/branding.js` — app-wide runtime connector that replaces legacy `.brand-mark` and `.auth-mark` placeholders with the master logo.

Do not add alternate logo files for individual screens. New UI branding must use `assets/branding/indoone-master.svg`.

Android launcher and splash resources use native vector resources that reproduce the same final artwork for reliable startup rendering. The Android launcher may still apply the platform's adaptive-icon mask outside the master artwork.
