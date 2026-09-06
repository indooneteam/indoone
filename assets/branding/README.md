# Indoone Branding

The web app keeps the Indoone mark directly in HTML and JavaScript as inline SVG so every screen renders the same geometry without loading an external SVG file.

- `app/shared/branding.js` — single source of truth for the inline web logo, header/drawer placeholders, authentication placeholders, About placeholders, and favicon setup.
- `indoone-logo.png` — supplied final square logo artwork used by Android launcher resources and favicon rendering.
- `indoone-mark.png` — transparent raster mark kept for native Android compatibility where needed.

New web UI must use the shared inline SVG branding code. Do not add new SVG logo files or screen-specific logo assets.

Android launcher and splash resources continue to use the supplied PNG artwork so the native startup experience matches the selected logo.
