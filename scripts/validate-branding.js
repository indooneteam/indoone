#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');

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

const master = read('assets/branding/indoone-master.svg');
const aligned = [
  'assets/branding/indoone-mark.svg',
  'assets/branding/indoone-app-icon.svg',
  'assets/branding/indoone-splash.svg'
];

assert(master.includes('<svg '), 'Canonical logo is not a valid SVG document.');
assert(master.includes('viewBox="0 0 512 512"'), 'Canonical logo viewBox changed unexpectedly.');
assert(!master.includes('filter='), 'Canonical logo contains an unsupported filter effect.');

for (const relativePath of aligned) {
  const content = read(relativePath);
  assert(content === master, `${relativePath} must match indoone-master.svg exactly.`);
}

const brandingJs = read('app/shared/branding.js');
assert(
  brandingJs.includes("assets/branding/indoone-master.svg"),
  'Shared branding connector does not point to the canonical master logo.'
);
assert(
  brandingJs.includes('20260906-logo-3'),
  'Shared branding connector is not using the current logo cache version.'
);

const index = read('index.html');
assert(index.includes('class="brand-mark"'), 'index.html is missing the shared brand placeholder.');

for (const relativePath of ['app/auth/login/login.js', 'app/auth/signup/signup.js']) {
  const content = read(relativePath);
  assert(
    content.includes('assets/branding/indoone-master.svg'),
    `${relativePath} does not use the canonical master logo.`
  );
  assert(
    content.includes('20260906-logo-3'),
    `${relativePath} is not using the current logo cache version.`
  );
}

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

console.log('Branding validation passed. Canonical web SVGs, shared references, current cache version, and Android branding inputs are present and consistent.');
