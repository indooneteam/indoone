(() => {
  window.showAccessToMyDevice = async () => {
    const content = document.getElementById('deviceContent');
    if (!content) return;

    content.innerHTML = '<div>Device detail is ready.</div>';
  };
})();
