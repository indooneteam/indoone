#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');
const brandingDir = path.join(root, 'assets/branding');

function read(relativePath) {
  const file = path.join(root, relativePath);
  if (!fs.existsSync(file)) {
    throw new Error(`Missing required file: ${relativePath}`);
  }
  return fs.readFileSync(file, 'utf8');
}

function assert(condition, message) {
  if (!condition) throw new Error(message);
}

const brandingJs = read('app/shared/branding.js');
assert(
  brandingJs.includes("logoSource: 'inline-svg'"),
  'Shared branding connector must use inline SVG logo markup.'
);
assert(
  brandingJs.includes("viewBox', '0 0 48 48'"),
  'Shared branding connector is missing the compact logo viewBox.'
);
assert(
  /<circle\s+cx="24"\s+cy="24"\s+r="17\.5"/.test(brandingJs),
  'Shared branding connector is missing the premium ring.'
);
assert(
  /<circle\s+cx="24"\s+cy="16\.25"\s+r="3\.35"/.test(brandingJs),
  'Shared branding connector is missing the logo dot.'
);
assert(
  /<rect\s+x="20\.15"\s+y="22"\s+width="7\.7"\s+height="14\.2"/.test(brandingJs),
  'Shared branding connector is missing the compact rounded stem.'
);
assert(
  brandingJs.includes('20260906-logo-10'),
  'Shared branding connector is not using the current logo cache version.'
);
assert(
  !brandingJs.includes('assets/branding/indoone-logo.png'),
  'Shared branding connector still references the old PNG logo asset.'
);

const index = read('index.html');
assert(index.includes('class="brand-mark"'), 'index.html is missing the shared brand placeholder.');

const login = read('app/auth/login/login.js');
const signup = read('app/auth/signup/signup.js');
assert(login.includes('class="auth-mark"'), 'Login is missing the shared auth logo placeholder.');
assert(signup.includes('class="auth-mark"'), 'Signup is missing the shared auth logo placeholder.');
assert(!login.includes('indooneLoginGradient'), 'Login still contains an obsolete duplicate logo definition.');
assert(!signup.includes('indooneSignupGradient'), 'Signup still contains an obsolete duplicate logo definition.');
assert(!login.includes('assets/branding/'), 'Login still references an external branding asset.');
assert(!signup.includes('assets/branding/'), 'Signup still references an external branding asset.');

const settingsAbout = read('app/settings/about/index.html');
assert(settingsAbout.includes('class="about-mark"'), 'Settings About is missing the shared logo placeholder.');
assert(!settingsAbout.includes('indooneSettingsAboutGradient'), 'Settings About still contains an obsolete duplicate logo definition.');
assert(!settingsAbout.includes('assets/branding/'), 'Settings About still references an external branding asset.');

const menuAbout = read('app/menu/about/script.js');
assert(menuAbout.includes('class="token-icon"'), 'Menu About is missing the shared logo placeholder.');
assert(!menuAbout.includes('indooneMenuAboutGradient'), 'Menu About still contains an obsolete duplicate logo definition.');
assert(!menuAbout.includes('assets/branding/'), 'Menu About still references an external branding asset.');

const brandingFiles = fs.readdirSync(brandingDir);
const svgFiles = brandingFiles.filter(file => file.toLowerCase().endsWith('.svg'));
assert(svgFiles.length === 0, `External branding SVG files must be deleted: ${svgFiles.join(', ')}`);

const oldRasterFiles = [
  'assets/branding/indoone-logo.png',
  'assets/branding/indoone-mark.png',
  'android/app/src/main/res/drawable-nodpi/indoone_logo.png',
  'android/app/src/main/res/drawable-nodpi/indoone_mark.png'
];
for (const relativePath of oldRasterFiles) {
  assert(!fs.existsSync(path.join(root, relativePath)), `Obsolete logo asset still exists: ${relativePath}`);
}

const gradle = read('android/app/build.gradle');
assert(!gradle.includes('exact-logo'), 'Android Gradle still contains stale exact-logo references.');
assert(!gradle.includes('exact-logo-v2'), 'Android Gradle still contains stale exact-logo-v2 references.');

read('android/app/src/main/AndroidManifest.xml');
const launcher = read('android/app/src/main/res/drawable/ic_launcher_foreground.xml');
const splashLogo = read('android/app/src/main/res/drawable/indoone_splash_logo.xml');
const splashGlow = read('android/app/src/main/res/drawable/indoone_splash_glow.xml');
const splashScreen = read('android/app/src/main/res/drawable/splash_screen.xml');
const splashColors = read('android/app/src/main/res/values/colors.xml');
assert(launcher.includes('<vector '), 'Android launcher logo must be a direct vector drawable.');
assert(splashLogo.includes('<vector '), 'Android splash logo must be a direct vector drawable.');
assert(splashGlow.includes('<vector '), 'Android splash glow must be a direct vector drawable.');
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
  'assets/branding/exact-logo/',
  'assets/branding/exact-logo-v2/',
  'assets/branding/exact-logo-v3.txt'
];
for (const relativePath of forbidden) {
  assert(!fs.existsSync(path.join(root, relativePath)), `Stale branding artifact still exists: ${relativePath}`);
}

console.log('Branding validation passed. Indoone uses one compact premium ring logo source across web UI, with a dedicated Design 2 glow splash composition and direct vector Android assets.');
