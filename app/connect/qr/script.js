(() => {
  function escapeHtml(value){return String(value||'').replace(/[&<>\"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','\"':'&quot;',"'":'&#39;'}[c]));}
  function deviceName(){const saved=localStorage.getItem('indoone_connect_device_name');if(saved)return saved;const uid=window.IndooneFirebase?.auth?.currentUser?.uid||'';const suffix=uid?uid.slice(-4).toUpperCase():Math.random().toString(36).slice(2,6).toUpperCase();const mobile=/Mobi|Android/i.test(navigator.userAgent);const name=`Indoone ${mobile?'Phone':'Computer'} ${suffix}`;localStorage.setItem('indoone_connect_device_name',name);return name;}
  function code(){const base=deviceName().replace(/[^A-Z0-9]/gi,'').toUpperCase().slice(-8);const host=location.hostname.replace(/[^A-Z0-9]/gi,'').slice(-4).toUpperCase()||'LOCAL';return `${base}-${host}`.slice(0,16);}
  function visual(value){let seed=0;for(let i=0;i<value.length;i++)seed+=value.charCodeAt(i)*(i+3);const cells=[];for(let y=0;y<21;y++)for(let x=0;x<21;x++){const finder=(x<7&&y<7)||(x>=14&&y<7)||(x<7&&y>=14);const on=finder?((x%6===0||y%6===0)||(x>1&&x<5&&y>1&&y<5)):((seed+x*17+y*31+x*y*7)%11<5);cells.push(`<i class="connect-qr-cell ${on?'':'off'}"></i>`)}return `<div class="connect-qr-grid">${cells.join('')}</div>`;}
  async function copy(value){try{await navigator.clipboard.writeText(value);window.toast?.('Pairing code copied.')}catch(_){window.toast?.(value)}}
  window.showConnectQr=function(){
    if(typeof openModal!=='function')return;
    openModal(document.getElementById('connectQrTemplate')?.innerHTML||'');
    const name=deviceName(), value=code();
    const modal=document.getElementById('modal');
    if(!modal)return;
    modal.querySelector('#connectQrVisual').innerHTML=visual(value);
    modal.querySelector('#connectQrDeviceName').textContent=name;
    modal.querySelector('#connectQrCode').textContent=`Pairing code: ${value}`;
    modal.querySelector('#connectQrCodeLarge').textContent=value;
    modal.querySelectorAll('[data-connect-copy]').forEach(button=>button.addEventListener('click',()=>copy(value)));
    modal.querySelectorAll('[data-connect-close]').forEach(button=>button.addEventListener('click',()=>window.closeModal?.()));
  };
  function init(){const existing=document.getElementById('connectQrTemplate');if(existing)return;const template=document.createElement('template');template.id='connectQrTemplate';template.innerHTML=`${document.querySelector('.qr-feature')?.outerHTML||''}`;document.body.appendChild(template.content.cloneNode(true));}
  if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',init);else init();
})();
