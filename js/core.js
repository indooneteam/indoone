const overlay = document.getElementById('overlay');
const modal = document.getElementById('modal');

window.openModal = function (html) {
  modal.innerHTML = html;
  overlay.classList.remove('hidden');
};

window.closeOverlay = function (event) {
  if (event.target === overlay) overlay.classList.add('hidden');
};

window.closeModal = function () {
  overlay.classList.add('hidden');
};

window.showHome = function () {
  overlay.classList.add('hidden');
  document.querySelectorAll('.bottom-nav button').forEach(b => b.classList.remove('active'));
  document.querySelector('.bottom-nav button:first-child')?.classList.add('active');
};

window.toast = function (message) {
  let t = document.getElementById('toast');
  if (!t) {
    t = document.createElement('div');
    t.id = 'toast';
    t.style.cssText = 'position:fixed;left:50%;bottom:88px;transform:translateX(-50%);background:#19151f;color:#fff;padding:11px 16px;border-radius:12px;font-size:13px;z-index:9999;box-shadow:0 8px 30px #0003;';
    document.body.appendChild(t);
  }
  t.textContent = message;
  t.style.display = 'block';
  clearTimeout(window.__toastTimer);
  window.__toastTimer = setTimeout(() => { t.style.display = 'none'; }, 1800);
};
