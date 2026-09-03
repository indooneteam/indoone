(() => {
  const src = 'app/menu/script.js?v=20260903b';
  if ([...document.scripts].some(script => script.src.includes('/app/menu/script.js'))) return;
  const script = document.createElement('script');
  script.src = src;
  document.body.appendChild(script);
})();
