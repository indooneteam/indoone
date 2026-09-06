const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');

function read(relative) {
  return fs.readFileSync(path.join(root, relative), 'utf8');
}

function assert(condition, message) {
  if (!condition) throw new Error(message);
}

const gradle = read('android/app/build.gradle');
assert(!gradle.includes('exact-logo'), 'Android Gradle still contains stale exact-logo references.');
assert(!gradle.includes('exact-logo-v2'), 'Android Gradle still contains stale exact-logo-v2 references.');

read('android/app/src/main/AndroidManifest.xml');
const launcher = read('android/app/src/main/res/drawable/ic_launcher_foreground.xml');
const splashLogo = read('android/app/src/main/res/drawable/indoone_splash_logo.xml');
const splashGlow = read('android/app/src/main/res/drawable/indoone_splash_glow.xml');
const splashScreen = read('android/app/src/main/res/drawable/splash_screen.xml');
const splashAndroid12Icon = read('android/app/src/main/res/drawable/indoone_android12_splash_icon.xml');
const splashColors = read('android/app/src/main/res/values/colors.xml');
const splashAndroid12Style = read('android/app/src/main/res/values-v31/styles.xml');
assert(launcher.includes('<vector '), 'Android launcher logo must be a direct vector drawable.');
assert(splashLogo.includes('<vector '), 'Android splash logo must be a direct vector drawable.');
assert(splashGlow.includes('<vector '), 'Android splash glow must be a direct vector drawable.');
assert(splashAndroid12Icon.includes('@drawable/indoone_splash_glow') && splashAndroid12Icon.includes('@drawable/indoone_splash_logo'), 'Android 12+ splash icon must include the Design 2 glow and logo.');
assert(launcher.includes('#A855F7') && launcher.includes('#7C3AED') && launcher.includes('#5B21B6'), 'Android launcher logo is missing the premium gradient palette.');
assert(splashLogo.includes('#A855F7') && splashLogo.includes('#FFFFFF'), 'Android splash logo is missing the Design 2 purple ring and white center.');
assert(splashGlow.includes('#60A5FA') && splashGlow.includes('#A855F7'), 'Android splash glow is missing the purple-blue glow palette.');
assert(
  (splashScreen.includes('#05030A') || splashScreen.includes('@color/indoone_splash_background')) &&
  splashScreen.includes('@drawable/indoone_splash_glow') &&
  splashScreen.includes('@drawable/indoone_splash_logo') &&
  splashColors.includes('name="indoone_splash_background"') &&
  splashColors.includes('#05030A'),
  'Android splash screen is missing the Design 2 dark glow composition.'
);
assert(splashAndroid12Style.includes('@color/indoone_splash_background'), 'Android 12+ splash background must use the Design 2 dark background.');
assert(splashAndroid12Style.includes('@drawable/indoone_android12_splash_icon'), 'Android 12+ splash must use the Design 2 composite splash icon.');
assert(launcher.includes('M54,14.625'), 'Android launcher ring geometry is missing the compact premium ring.');
assert(splashLogo.includes('M54,14.625'), 'Android splash ring geometry is missing the compact premium ring.');
assert(launcher.includes('M46.4625,36.5625'), 'Android launcher dot geometry is missing.');
assert(splashLogo.includes('M46.4625,36.5625'), 'Android splash dot geometry is missing.');
assert(launcher.includes('M45.3375,49.5'), 'Android launcher stem geometry is missing.');
assert(splashLogo.includes('M45.3375,49.5'), 'Android splash stem geometry is missing.');
assert(!launcher.includes('indoone_mark'), 'Android launcher still references the old PNG logo.');
assert(!splashLogo.includes('indoone_logo'), 'Android splash still references the old PNG logo.');

const forbidden = [
  'assets/branding/indoone-exact.svg',
  'assets/branding/branding.js',
  'assets/branding/indoone-mark-white.svg',
  'assets/branding/indoone-mark.svg',
  'assets/branding/indoone-logo.svg',
  'assets/branding/indoone-mark.png',
  'assets/branding/indoone-logo.png',
  'android/app/src/main/res/drawable-nodpi/indoone_mark.png',
  'android/app/src/main/res/drawable-nodpi/indoone_logo.png',
];
for (const item of forbidden) {
  assert(!fs.existsSync(path.join(root, item)), `Forbidden legacy branding asset still exists: ${item}`);
}

console.log('Branding validation passed. Indoone uses one compact premium ring logo source across web UI, with a dedicated Design 2 glow splash composition and direct vector Android assets.');
