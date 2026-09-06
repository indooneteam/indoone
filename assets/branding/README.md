# Indoone Branding

The supplied final Indoone logo is kept in raster form for exact UI and launcher rendering.

- `indoone-logo.png` — supplied final square logo artwork, including its light rounded-square background.
- `indoone-mark.png` — transparent mark extracted from the supplied logo for web headers and authentication UI.
- `indoone-master.svg` — vector compatibility/master artwork retained for existing integrations.
- `indoone-mark.svg`, `indoone-splash.svg`, `indoone-app-icon.svg` — compatibility copies retained for existing integrations.
- `app/shared/branding.js` — app-wide branding connector that uses the supplied mark and installs the supplied app icon as the favicon.

Do not add alternate logo files for individual screens. New UI branding should use the supplied branding assets above.

Android launcher and splash resources use the supplied PNG artwork so the launcher and startup presentation match the selected logo. The Android adaptive-icon mask is still applied by the platform where applicable.
