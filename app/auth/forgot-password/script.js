window.IndooneForgotPassword = (() => {
  const MARKUP_URL = 'app/auth/forgot-password/index.html?v=20260904a';

  async function load(root) {
    if (!root) return;
    const response = await fetch(MARKUP_URL, { cache: 'no-store' });
    if (!response.ok) throw new Error('Forgot Password page could not be loaded.');
    root.innerHTML = await response.text();
  }

  return { load };
})();
