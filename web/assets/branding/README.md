# Indoone Branding

The Indoone logo is now defined directly in code so every screen uses the same compact purple minimal-dot mark without external logo files.

- `app/shared/branding.js` — single source of truth for the inline web logo, header/drawer placeholders, authentication placeholders, About placeholders, and favicon.
- Android launcher and splash — direct vector drawable code using the same minimal-dot logo geometry.
- `assets/branding/` — intentionally contains no logo SVG or raster assets.

New UI must use the shared inline branding code. Do not add screen-specific logo files or alternate logo assets.
