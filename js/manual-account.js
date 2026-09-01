window.showManual = function () {
  openModal(`<h2>Add Account</h2><div class="tabs"><button onclick="showAdd()">Scan QR Code</button><button class="selected">Enter Setup Key</button></div><div class="field"><label>ACCOUNT NAME</label><input id="accountName" placeholder="e.g. Google"></div><div class="field"><label>EMAIL / USERNAME</label><input id="accountUser" placeholder="you@example.com"></div><div class="field"><label>SECRET KEY</label><input id="secretKey" placeholder="Enter setup key" autocomplete="off"></div><div class="field"><label>TYPE</label><select><option>Time based (TOTP)</option></select></div><button class="primary" onclick="saveAccount()">Save Account</button>`);
};

window.saveAccount = function () {
  const name = document.getElementById('accountName')?.value.trim();
  const secret = document.getElementById('secretKey')?.value.trim();
  if (!name || !secret) return toast('Enter account name and secret key');
  toast('Demo account saved');
  closeModal();
};
