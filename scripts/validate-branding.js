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
  brandingJs.includes('viewBox', 0),
  'Shared branding connector is missing inline SVG geometry.'
);
assert(
  brandingJs.includes('indooneBrandGradient'),
  'Shared branding connector is missing the Indoone gradient.'
);
assert(
  brandingJs.includes('20260906-logo-6'),
  'Shared branding connector is not using the current branding cache version.'
);

const index = read('index.html');
assert(index.includes('class="brand-mark"'), 'index.html is missing the shared brand placeholder.');

const login = read('app/auth/login/login.js');
const signup = read('app/auth/signup/signup.js');
assert(login.includes('indooneLoginGradient'), 'Login does not contain inline Indoone SVG branding.');
assert(signup.includes('indooneSignupGradient'), 'Signup does not contain inline Indoone SVG branding.');
assert(!login.includes('assets/branding/'), 'Login still references an external branding asset.');
assert(!signup.includes('assets/branding/'), 'Signup still references an external branding asset.');

const settingsAbout = read('app/settings/about/index.html');
assert(settingsAbout.includes('indooneSettingsAboutGradient'), 'Settings About does not contain inline Indoone SVG branding.');
assert(!settingsAbout.includes('assets/branding/'), 'Settings About still references an external branding asset.');

const menuAbout = read('app/menu/about/script.js');
assert(menuAbout.includes('indooneMenuAboutGradient'), 'Menu About does not contain inline Indoone SVG branding.');
assert(!menuAbout.includes('assets/branding/'), 'Menu About still references an external branding asset.');

const brandingFiles = fs.readdirSync(brandingDir);
const svgFiles = brandingFiles.filter(file => file.toLowerCase().endsWith('.svg'));
assert(svgFiles.length === 0, `External branding SVG files must be deleted: ${svgFiles.join(', ')}`);

const gradle = read('android/app/build.gradle');
assert(!gradle.includes('exact-logo'), 'Android Gradle still contains stale exact-logo references.');
assert(!gradle.includes('exact-logo-v2'), 'Android Gradle still contains stale exact-logo-v2 references.');

read('android/app/src/main/AndroidManifest.xml');
read('android/app/src/main/res/drawable/ic_launcher_foreground.xml');
read('android/app/src/main/res/drawable/indoone_splash_logo.xml');

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

console.log('Branding validation passed. Web branding is inline SVG, external branding SVG files are absent, and Android branding inputs remain present.');
