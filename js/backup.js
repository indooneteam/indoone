window.showBackup = function () {
  openModal(`<h2>Encrypted Backup</h2><p>Create an encrypted local backup of your authenticator accounts. This demo does not upload anything.</p><button class="primary" onclick="createBackup()">Create Backup</button><button class="primary" onclick="importBackup()">Import Backup</button>`);
};
window.createBackup = function () { toast('Encrypted backup created'); };
window.importBackup = function () { toast('Backup import opened'); };
