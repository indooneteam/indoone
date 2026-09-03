(() => {
  const LIST_ID = 'myDevicesList';

  window.showMyDevices = async () => {
    const list = document.getElementById(LIST_ID);
    if (!list) return;

    list.innerHTML = `
      <div class="connected-device-empty">
        <strong>My devices</strong>
        <span>Your own Indoone devices will appear here.</span>
      </div>
    `;
  };
})();
