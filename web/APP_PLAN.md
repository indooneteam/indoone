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

## Global Feature Folder Rule
This rule applies to the **entire app**, not only Home or Add Account.

- Every major/important user-facing feature gets its own dedicated folder.
- Every major feature folder must contain exactly these three primary files:
  - `index.html`
  - `style.css`
  - `script.js`
- When a major feature contains another major/important independent flow, that sub-feature gets its **own folder inside the parent feature folder**.
- The same three-file structure applies recursively to those nested sub-features.
- A parent feature owns the navigation and shared coordination for its child features; child feature logic, markup, and styling stay inside the child folder.
- Keep small actions inside their parent feature. Do not create a folder for tiny actions such as Sort, Clear, Refresh, Copy, Toggle, or similar controls unless the action grows into a substantial independent user flow.
- Feature-specific code must stay inside the closest relevant feature folder.
- Shared app-level services/utilities may remain outside feature folders when they are genuinely reused by multiple features.
- Do not duplicate shared services merely to make folders look independent.
- Do not move working code into a new folder until the replacement is wired and verified.
- After modularization, obsolete duplicate files must be removed so there is one clear source of truth for each feature.
- This modular structure must be followed for all future features added anywhere in the app, including Home, Lobby, Connect, Settings, Authentication, and their future sub-features.

### Generic Feature Pattern
Use this pattern for every major feature:

```text
app/<feature>/
├── index.html
├── style.css
└── script.js
```

When that feature has important sub-features:

```text
app/<feature>/
├── index.html
├── style.css
├── script.js
│
├── <sub-feature-a>/
│   ├── index.html
│   ├── style.css
│   └── script.js
│
├── <sub-feature-b>/
│   ├── index.html
│   ├── style.css
│   └── script.js
│
└── <sub-feature-c>/
    ├── index.html
    ├── style.css
    └── script.js
```

This hierarchy should be repeated wherever the app grows a new major feature or a major nested flow.

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

Home-specific exception:
- Home currently uses `home.html`, `home.css`, and `home.js` as its feature entry files because Home is integrated with the existing app shell.
- The same three-file principle still applies: Home owns its own markup, styling, and behavior, while its important independent flows live in nested feature folders.

## Home Major Features
Major Home flows to isolate:
- Add Account
- Add Account > QR
- Add Account > Manual
- Add Account > Import
- Search
- Account details/actions

Small Home actions such as Sort remain inside Home until they become substantial enough to justify a separate feature.

## Modularity and Future Editing Rule
The folder boundary is also a **change-isolation boundary**.

- Future manual edits should normally touch only the files belonging to the feature being changed.
- Future AI edits should normally touch only the relevant feature folder plus explicitly required shared modules.
- Do not rewrite unrelated feature folders while implementing a new feature.
- Avoid putting unrelated UI logic into shared files just for convenience.
- Keep feature entry points and child-feature boundaries obvious so another developer or AI can understand where a change belongs without scanning the whole app.

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
- Moving to a feature page must not destroy unrelated parent-feature state unnecessarily.
- Parent → child navigation should remain clear and predictable for nested feature folders.

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
Start Home from the base structure. Fix Home navigation/back behavior first, then modularize the important Home flows one by one without breaking existing account functionality. Apply the same folder hierarchy, readability, isolation, and testing rules to every feature that comes after Home.
