window.showAdd = function () {
  openModal(`<h2>Add Account</h2><div class="tabs"><button class="selected" onclick="showAdd()">Scan QR Code</button><button onclick="showManual()">Enter Setup Key</button></div><div class="qr">▦</div><p style="text-align:center">Scan the QR code provided by your account.</p><button class="primary" onclick="showManual()">Enter Setup Key Instead</button>`);
};
