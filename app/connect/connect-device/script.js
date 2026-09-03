(() => {
  async function loadMarkup(){const response=await fetch(`app/connect/connect-device/index.html?v=20260917a`,{cache:'no-store'});if(!response.ok)throw new Error('Connect flow could not be loaded.');return response.text();}
  window.showConnectChoice=async function(){try{const markup=await loadMarkup();openModal(markup);const modal=document.getElementById('modal');modal?.querySelectorAll('[data-device-connect-close]').forEach(button=>button.addEventListener('click',()=>window.closeModal?.()));modal?.querySelectorAll('[data-device-target]').forEach(button=>button.addEventListener('click',()=>{window.showConnectTarget?.(button.dataset.deviceTarget);}));}catch(error){window.toast?.(error?.message||'Could not open Connect.');}};
})();
