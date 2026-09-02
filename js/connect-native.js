(() => {
  const native = window.IndooneNative;
  const DEVICE_KEY = 'indoone_connect_devices_v1';
  const state = { mode:null, endpoints:new Map(), pendingMode:null, activeEndpointId:'' };
  if (!native) return;

  const esc = value => String(value || '').replace(/[&<>'"]/g, c => ({ '&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;' }[c]));
  const getName = () => { const saved=localStorage.getItem('indoone_connect_device_name'); if(saved)return saved; const uid=window.IndooneFirebase?.auth?.currentUser?.uid||Math.random().toString(36); const n=`Indoone Phone ${uid.slice(-4).toUpperCase()}`; localStorage.setItem('indoone_connect_device_name',n); return n; };
  const load = () => { try { const v=JSON.parse(localStorage.getItem(DEVICE_KEY)||'[]'); return Array.isArray(v)?v:[]; } catch(e){ return []; } };
  const save = v => localStorage.setItem(DEVICE_KEY,JSON.stringify(v));
  const say = m => typeof toast==='function' && toast(m);
  const open = (title,body) => typeof openModal==='function' && openModal(`<div class="modal-head"><h2>${esc(title)}</h2><button class="close-btn" data-close>×</button></div>${body}`);

  function rememberConnected(name, id) {
    const ds=load(); let d=ds.find(x=>x.endpointId===id||x.name===name);
    if(!d)d={id:`nearby-${Date.now()}`,name,type:/computer|laptop|pc/i.test(name)?'computer':'phone',permissions:{photos:true,videos:true,documents:false,downloads:true,upload:false,delete:false,alwaysAllow:false},trusted:false};
    d.name=name; d.endpointId=id; d.connected=true; d.updatedAt=Date.now();
    save([...ds.filter(x=>x!==d&&x.endpointId!==id),d]);
  }

  function startTransport() {
    if(!state.pendingMode) return;
    const mode=state.pendingMode; state.pendingMode=null; state.mode=mode;
    try { mode==='advertise' ? native.startNearbyAdvertising(getName()) : native.startNearbyDiscovery(); }
    catch(e) { say('Nearby connection is unavailable on this device.'); }
    const status=document.getElementById('nearbyStatus'); if(status)status.textContent=mode==='advertise'?'Advertising nearby…':'Scanning for nearby Indoone devices…';
  }

  function begin(mode) {
    state.endpoints.clear(); state.pendingMode=mode; state.mode=mode;
    native.requestNearbyPermissions();
    open(mode==='advertise'?'Waiting for connection':'Nearby devices', mode==='advertise'
      ? `<div class="device-summary"><div class="device-icon large">📱</div><div><b>${esc(getName())}</b><small>Waiting for a nearby connection</small></div></div><div class="connect-empty">Keep this phone open. Another Indoone device can discover it and send a connection request.</div><div id="nearbyStatus" class="connect-muted">Requesting Nearby permissions…</div><button class="secondary" data-nearby-stop>Stop</button>`
      : `<div id="nearbyDiscoveryList"><div class="connect-empty">Scanning for nearby Indoone devices…</div></div><div id="nearbyStatus" class="connect-muted">Requesting Nearby permissions…</div><button class="secondary" data-nearby-stop>Stop</button>`);
  }

  window.showConnectChoice = function(){ closeModal?.(); document.getElementById('connectChoicePage')?.removeAttribute('hidden'); document.getElementById('connectHomePage')?.setAttribute('hidden',''); };

  document.addEventListener('click', event => {
    const choice=event.target.closest('.connect-choice button');
    if(choice){ event.preventDefault(); event.stopImmediatePropagation(); const phone=choice.querySelector('.choice-icon')?.textContent.includes('📱'); open(phone?'Connect another phone':'Connect laptop / PC',`<div class="device-summary"><div class="device-icon large">${phone?'📱':'💻'}</div><div><b>Nearby connection</b><small>Choose a connection mode</small></div></div><button class="primary" data-nearby-mode="discover">Find nearby device</button><button class="secondary" data-nearby-mode="advertise">Make this device discoverable</button>`); return; }
    const mode=event.target.closest('[data-nearby-mode]'); if(mode){event.preventDefault();begin(mode.dataset.nearbyMode==='advertise'?'advertise':'discover');return;}
    const ep=event.target.closest('[data-nearby-endpoint]'); if(ep){event.preventDefault();const item=state.endpoints.get(ep.dataset.nearbyEndpoint);if(item){state.activeEndpointId=item.endpointId;native.connectNearbyEndpoint(item.endpointId,getName());say(`Connection request sent to ${item.name}.`);}return;}
    const accept=event.target.closest('[data-nearby-accept]');if(accept){native.acceptNearbyConnection(accept.dataset.nearbyAccept);return;}
    const reject=event.target.closest('[data-nearby-reject]');if(reject){native.rejectNearbyConnection(reject.dataset.nearbyReject);closeModal?.();return;}
    if(event.target.closest('[data-nearby-stop]')){native.stopNearby();closeModal?.();}
  },true);

  window.addEventListener('indoone-nearby', event => {
    const d=event.detail||{};
    if(d.type==='permissions'){
      const s=document.getElementById('nearbyStatus'); if(s)s.textContent=d.message==='granted'?'Nearby permissions granted.':'Nearby permissions denied.';
      if(d.message==='granted') startTransport(); else { state.pendingMode=null; say('Nearby permissions are required.'); }
      return;
    }
    if(d.type==='endpointFound'){
      state.endpoints.set(d.endpointId,{endpointId:d.endpointId,name:d.message});
      const list=document.getElementById('nearbyDiscoveryList');
      if(list)list.innerHTML=[...state.endpoints.values()].map(x=>`<button type="button" class="device-card connected" data-nearby-endpoint="${esc(x.endpointId)}"><div class="device-icon">📱</div><div><b>${esc(x.name)}</b><small>Nearby • Ready to connect</small></div><span class="device-dot"></span></button>`).join('');
      return;
    }
    if(d.type==='endpointLost'){state.endpoints.delete(d.endpointId);return;}
    if(d.type==='connectionInitiated'&&d.incoming){
      open('Connection request',`<div class="device-summary"><div class="device-icon large">📱</div><div><b>${esc(d.message)}</b><small>Wants to connect to this device</small></div></div><div class="connect-empty"><b>Verification code: ${esc(d.authenticationDigits||'----')}</b><br>Confirm that it matches on both devices before accepting.</div><button class="primary" data-nearby-accept="${esc(d.endpointId)}">Accept connection</button><button class="secondary" data-nearby-reject="${esc(d.endpointId)}">Reject</button>`);
      return;
    }
    if(d.type==='connectionInitiated'&&!d.incoming){say(`Waiting for ${d.message} to approve the connection.`);return;}
    if(d.type==='connectionResult'){
      if(d.message==='connected'){
        const n=state.endpoints.get(d.endpointId)?.name||'Nearby device'; rememberConnected(n,d.endpointId); state.activeEndpointId=d.endpointId;
        say(`Connected to ${n}.`); open('Connected',`<div class="device-summary"><div class="device-icon large">📱</div><div><b>${esc(n)}</b><small>Connected • Nearby</small></div></div><div class="connect-empty">Connection is ready. Data access remains separate and controlled by permissions.</div><button class="primary" data-close>Done</button>`);
      } else say('Nearby connection was rejected.');
      return;
    }
    if(d.type==='disconnected'){
      save(load().map(x=>x.endpointId===d.endpointId?{...x,connected:false,updatedAt:Date.now()}:x)); say('Nearby device disconnected.'); return;
    }
    if(d.type==='error')say(`Nearby error: ${d.message}`);
  });
})();
