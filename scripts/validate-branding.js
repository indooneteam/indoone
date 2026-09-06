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
const branding = read('app/shared/branding.js');
const launcher = read('android/app/src/main/res/drawable/ic_launcher_foreground.xml');
const splashLogo = read('android/app/src/main/res/drawable/indoone_splash_logo.xml');
const splashGlow = read('android/app/src/main/res/drawable/indoone_splash_glow.xml');
const splashScreen = read('android/app/src/main/res/drawable/splash_screen.xml');
const splashAndroid12Icon = read('android/app/src/main/res/drawable/indoone_android12_splash_icon.xml');
const splashColors = read('android/app/src/main/res/values/colors.xml');
const splashAndroid12Style = read('android/app/src/main/res/values-v31/styles.xml');

for (const [name, content] of Object.entries({ branding, launcher, splashLogo })) {
  assert(content.includes('#C15CFF'), `${name} is missing the Spark 06 pink-purple palette.`);
  assert(content.includes('#7C3AED'), `${name} is missing the Spark 06 purple palette.`);
  assert(content.includes('#22C7FF'), `${name} is missing the Spark 06 cyan palette.`);
}

assert(branding.includes('20260906-logo-06-spark'), 'Shared branding version is not Spark 06.');
assert(branding.includes('M24 14 L27.2 20.8 L34 24'), 'Web Spark 06 geometry is missing.');
assert(branding.includes('M24 20.8 L25.2 22.8 L27.2 24'), 'Web Spark 06 center sparkle is missing.');
assert(branding.includes('viewBox'), 'Web branding must remain inline SVG.');
assert(branding.includes("logoSource: 'inline-svg'"), 'Web branding must remain inline SVG.');

assert(launcher.includes('<vector '), 'Android launcher logo must be a direct vector drawable.');
assert(splashLogo.includes('<vector '), 'Android splash logo must be a direct vector drawable.');
assert(splashGlow.includes('<vector '), 'Android splash glow must be a direct vector drawable.');
assert(splashAndroid12Icon.includes('@drawable/indoone_splash_glow') && splashAndroid12Icon.includes('@drawable/indoone_splash_logo'), 'Android 12+ splash icon must include the Spark 06 glow and logo.');
assert(launcher.includes('android:rotation="45"'), 'Android launcher must use the Spark 06 rotated-square silhouette.');
assert(splashLogo.includes('android:rotation="45"'), 'Android splash must use the Spark 06 rotated-square silhouette.');
assert(launcher.includes('M54,31.5 L61.2,46.2 L75.9,54'), 'Android launcher Spark 06 geometry is missing.');
assert(splashLogo.includes('M54,31.5 L61.2,46.2 L75.9,54'), 'Android splash Spark 06 geometry is missing.');
assert(launcher.includes('M54,46.1 L56.6,51.4 L61.9,54'), 'Android launcher center sparkle is missing.');
assert(splashLogo.includes('M54,46.1 L56.6,51.4 L61.9,54'), 'Android splash center sparkle is missing.');
assert(!launcher.includes('M54,14.625'), 'Android launcher still contains the old ring geometry.');
assert(!splashLogo.includes('M54,14.625'), 'Android splash still contains the old ring geometry.');
assert(!splashGlow.includes('strokeColor'), 'Android splash glow must not use a ring stroke.');

assert(
  (splashScreen.includes('#05030A') || splashScreen.includes('@color/indoone_splash_background')) &&
  splashScreen.includes('@drawable/indoone_splash_glow') &&
  splashScreen.includes('@drawable/indoone_splash_logo') &&
  splashColors.includes('name="indoone_splash_background"') &&
  splashColors.includes('#05030A'),
  'Android splash screen is missing the dark Spark 06 composition.'
);
assert(splashAndroid12Style.includes('@color/indoone_splash_background'), 'Android 12+ splash background must use the dark splash background.');
assert(splashAndroid12Style.includes('@drawable/indoone_android12_splash_icon'), 'Android 12+ splash must use the Spark 06 composite icon.');

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

console.log('Branding validation passed. Indoone uses Spark 06 consistently across web UI, favicon, Android launcher, and Android splash.');
