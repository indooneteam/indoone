window.openAccount = function (name, email, code) {
  openModal(`<h2>${name}</h2><p>${email}</p><div style="text-align:center;padding:24px 0"><div style="font-size:42px;font-weight:800;color:#6735df;letter-spacing:3px">${code}</div><p>Code expires in <b>18s</b></p></div><div class="settings-row"><span>Type<small>TOTP</small></span><b>30 sec</b></div><div class="settings-row"><span>Algorithm<small>SHA-1</small></span><b>6 digits</b></div><button class="primary" onclick="copyCode('${code}')">Copy Code</button><button class="primary" onclick="favoriteAccount('${name}')">Add to Favorites</button>`);
};

window.copyCode = async function (code) {
  try { await navigator.clipboard.writeText(code.replace(/\s/g, '')); } catch (_) {}
  toast('Code copied');
};
