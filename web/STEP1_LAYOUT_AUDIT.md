# Indoone Safe Layout — Step 1 Audit

## Goal
Move Indoone toward a device-agnostic, big-app-style safe-layout architecture using Android WindowInsets as the runtime source of truth, without changing unrelated functionality.

## Current layout layers inspected

### `desktop-layout-fix.css`
- Keeps the app shell at a maximum width of 430px and full dynamic viewport height.
- Rebuilds the topbar as a 3-column CSS grid: menu / centered brand / actions.
- Applies device-agnostic `env(safe-area-inset-top)` and `env(safe-area-inset-bottom)` values.
- Calculates topbar, bottom navigation, FAB, drawer, modal, and content viewport dimensions from those safe-area variables.
- Uses different base header/bottom-nav sizes for screens below 380px and 340px.

### `topbar-layout-fix.css`
- Forces the topbar to a fixed 76px height and fixed padding.
- Contains a comment specifically intended to prevent double compensation from native Android insets.
- Defines its own `.topbar-left` flex layout while `desktop-layout-fix.css` changes `.topbar-left` to `display: contents` and makes the topbar a grid.
- Therefore, the two files currently have overlapping topbar responsibilities and must be consolidated carefully in a later step.

### Android native inset layer
- `MainActivity.java` already receives Android `WindowInsets` and passes runtime inset values into the WebView through CSS variables.
- The current native calculation uses system-bar, tappable-element, and display-cutout information rather than Android-version-specific constants.
- This is the correct foundation for a single runtime safe-area source, but the injected CSS and stylesheet rules still need to be consolidated so the same inset is not compensated twice.

## Identified Step 1 findings

1. **No Android-version-specific layout should be added.** Runtime WindowInsets should remain the source of device differences.
2. **Topbar has two competing layout systems.** `desktop-layout-fix.css` uses grid/contents, while `topbar-layout-fix.css` defines flex/fixed-height behavior.
3. **Inset compensation exists in both native-injected CSS and stylesheet CSS.** This creates a risk of double compensation or conflicting precedence.
4. **The final architecture should have one safe-root calculation**, with header/content/bottom navigation positioned inside that safe layout instead of each component independently guessing system-bar space.
5. **Android 14 behavior must remain visually unchanged** while Android 15 edge-to-edge behavior is corrected through the same runtime mechanism.

## Step 1 scope

This audit intentionally makes **no UI or Android behavior changes**. It only records the current architecture and the conflicts that Step 2 must resolve.

## Planned next steps

1. Consolidate the runtime inset source.
2. Establish one WebView safe-area/root layout contract.
3. Refactor header positioning so the camera/action area cannot shift sideways from competing rules.
4. Refactor content and bottom navigation around the same safe layout.
5. Remove obsolete/duplicate inset compensation only after equivalent behavior is verified.
6. Verify Android 14, Android 15, gesture navigation, 3-button navigation, cutouts, and compact screens.
