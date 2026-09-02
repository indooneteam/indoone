window.persistIndoone = function () {
  window.IndooneTotpController?.persist();
};

window.addEventListener('beforeunload', persistIndoone);
