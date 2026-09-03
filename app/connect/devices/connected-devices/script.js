(() => {
  const LIST_ID = 'connectedDevicesList';

  window.showConnectedDevices = async () => {
    const list = document.getElementById(LIST_ID);
    if (!list) return;

    list.innerHTML = `
      <div class="connected-device-empty">
        <strong>Connected devices</strong>
        <span>Devices connected to this phone will appear here.</span>
      </div>
    `;
  };
})();
