window.IndooneForgotPassword = (() => {
  const MARKUP_URL = 'app/auth/forgot-password/index.html?v=20260904a';

  function setStatus(root, message = '', error = false) {
    const status = root
      ?.closest('.auth-page')
      ?.querySelector('#authStatus');

    if (!status) return;

    status.textContent = message;
    status.hidden = !message;
    status.dataset.error = error ? 'true' : 'false';
  }

  function errorMessage(error) {
    const map = {
      'auth/invalid-email': 'Enter a valid email address.',
      'auth/user-not-found': 'No Indoone account was found.',
      'auth/too-many-requests': 'Too many attempts. Please try again later.',
      'auth/network-request-failed': 'Network error. Check your connection and try again.'
    };

    return map[error?.code] || error?.message || 'Unable to send the password reset email.';
  }

  async function sendResetEmail(root, button) {
    const input = root.querySelector('#forgotPasswordEmail');
    const email = String(input?.value || '').trim().toLowerCase();

    if (!email || !email.includes('@')) {
      setStatus(root, 'Enter a valid email address.', true);
      input?.focus();
      return;
    }

    const auth = window.IndooneFirebase?.auth;

    if (!auth) {
      setStatus(
        root,
        'Authentication service is still loading. Please try again.',
        true
      );
      return;
    }

    if (button.disabled) return;

    const previousText = button.textContent;
    button.disabled = true;
    button.setAttribute('aria-busy', 'true');
    button.textContent = 'Sending…';
    setStatus(root);

    try {
      await auth.sendPasswordResetEmail(email);
      setStatus(
        root,
        'Password reset email sent. Check your inbox and follow the link to create a new password.'
      );
    } catch (error) {
      setStatus(root, errorMessage(error), true);
      console.error('Forgot Password reset request failed:', error);
    } finally {
      if (button.isConnected) {
        button.disabled = false;
        button.removeAttribute('aria-busy');
        button.textContent = previousText;
      }
    }
  }

  function bind(root) {
    if (!root || root.dataset.forgotPasswordBound === 'true') return;

    const button = root.querySelector('#forgotPasswordContinue');
    if (!button) return;

    root.dataset.forgotPasswordBound = 'true';

    button.addEventListener('click', event => {
      event.preventDefault();
      event.stopPropagation();
      void sendResetEmail(root, button);
    });

    const input = root.querySelector('#forgotPasswordEmail');
    input?.addEventListener('keydown', event => {
      if (event.key !== 'Enter') return;
      event.preventDefault();
      void sendResetEmail(root, button);
    });
  }

  async function load(root) {
    if (!root) return;

    const response = await fetch(MARKUP_URL, { cache: 'no-store' });
    if (!response.ok) {
      throw new Error('Forgot Password page could not be loaded.');
    }

    root.innerHTML = await response.text();
    bind(root);
  }

  return { load };
})();
