# Indoone App Development Plan

## Purpose
This file is the source of truth for the app structure, feature boundaries, development order, and coding rules. Read this before making structural changes.

## Development Branch Rules
- Develop only on `develop`.
- Do not modify `main` during development.
- Finish a feature, verify it, then push.
- Avoid unnecessary tiny commits/pushes.
- Preserve existing working behavior unless the feature explicitly requires a change.

## App Navigation
Main navigation:
- Home
- Lobby
- Connect
- Settings

Authentication (Login and Signup) remains an app-level flow and will be finalized last.

## Home Architecture
Home is the first area to refactor and build cleanly.

Base structure:
```text
app/home/
├── home.html
├── home.css
├── home.js
│
├── add-account/
│   ├── index.html
│   ├── style.css
│   ├── script.js
│   ├── qr/
│   │   ├── index.html
│   │   ├── style.css
│   │   └── script.js
│   ├── manual/
│   │   ├── index.html
│   │   ├── style.css
│   │   └── script.js
│   └── import/
│       ├── index.html
│       ├── style.css
│       └── script.js
│
├── search/
│   ├── index.html
│   ├── style.css
│   └── script.js
│
└── account/
    ├── index.html
    ├── style.css
    └── script.js
```

## Modularity Rule
- Major user flow/feature gets its own folder.
- Each major feature folder uses exactly `index.html`, `style.css`, and `script.js`.
- Do not create separate folders for tiny actions such as Sort, Clear, or Refresh unless they grow into an independent user flow.
- Feature-specific code stays inside the feature folder.
- Common services/utilities stay in shared app-level modules.

## Code Readability Rule
- Code must be line-by-line readable.
- Avoid giant one-line HTML, CSS, or JavaScript.
- Keep functions small and single-purpose.
- Use clear names.
- Prefer explicit code over compressed/minified-looking code during development.
- Make future manual editing and AI editing easy.

## Navigation and Page Behavior
- Important flows should open as real pages/sub-pages, not unnecessary popups.
- Back buttons must be visible, consistent, and deterministic.
- Browser/device back should return to the correct previous page.
- Moving to a feature page must not destroy unrelated Home state unnecessarily.

## Home Major Features
Major Home flows to isolate:
- Add Account
- Add Account > QR
- Add Account > Manual
- Add Account > Import
- Search
- Account details/actions

Small Home actions such as Sort remain inside Home until they become substantial enough to justify a separate feature.

## Firebase Architecture
- Keep one Firebase project/backend.
- Organize database/data models by feature or domain instead of creating separate Firebase projects for each feature.
- Example domains may include users, profile, accounts, settings, connect, and lobby.
- Shared Firebase services should remain reusable and stable.
- Feature-specific Firebase operations should stay close to the feature when practical.
- Changing a shared Firebase service can affect multiple features, so shared changes require extra verification.

## Planned Development Order
1. Home
2. Menu
3. Lobby
4. Connect
5. Settings
6. Login
7. Signup
8. Full integration and regression testing

## Connect Direction
Connect is a nearby device feature.
- Phone ↔ Phone
- Phone ↔ Laptop/PC
- Nearby direct connection
- High-speed file transfer
- Permission-controlled file browsing/access
- Photo/video/file upload, download, and send
- Trusted devices
- Saved permissions / Always Allow
- QR and scanner pairing flows
- Connection and data access are separate concepts

## Important Existing Behavior to Preserve
- Firebase authentication and session persistence
- Firebase Realtime Database account sync
- TOTP generation and account management
- QR/OTPAUTH import and manual setup
- Search/sort/favorites/trash behavior
- App lock/PIN and biometric support
- Recovery features
- Android native bridges and nearby connection foundation

## Testing Rule
After a feature is implemented:
1. Check the changed files for syntax/structure issues.
2. Run the relevant GitHub Actions build/deploy checks.
3. Verify the feature in the `develop` deployment when available.
4. Do not move to `main` until development and verification are complete.

## Current Priority
Start Home from the base structure. Fix Home navigation/back behavior first, then modularize the important Home flows one by one without breaking existing account functionality.
