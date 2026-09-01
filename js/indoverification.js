(() => {
  const API_BASE = 'https://indomail-production.up.railway.app';
  const APP_NAME = 'Indoone';
  const REQUEST_TIMEOUT_MS = 15000;

  async function request(path, body) {
    const controller = new AbortController();
    const timer = window.setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);
    try {
      const response = await fetch(`${API_BASE}${path}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Indo-App-Name': APP_NAME
        },
        body: JSON.stringify(body || {}),
        signal: controller.signal
      });
      const result = await response.json().catch(() => ({}));
      if (!response.ok || result?.ok === false) {
        throw new Error(result?.error || `OTP service request failed (${response.status}).`);
      }
      return result;
    } catch (error) {
      if (error?.name === 'AbortError') throw new Error('OTP service request timed out. Please try again.');
      if (error instanceof TypeError) {
        throw new Error('Unable to reach the OTP service. The current service may not allow this app origin yet.');
      }
      throw error;
    } finally {
      window.clearTimeout(timer);
    }
  }

  function requestSignupOtp(email, name = 'Indoone user') {
    return request('/api/auth/signup/request-otp', { email, name });
  }

  function verifySignupOtp({ email, challengeId, otp, name }) {
    return request('/api/auth/signup/verify-otp', { email, challengeId, otp, name });
  }

  function requestLoginOtp(email, name = 'Indoone user') {
    return request('/api/auth/login/request-otp', { email, name });
  }

  function verifyLoginOtp({ email, challengeId, otp, name }) {
    return request('/api/auth/login/verify-otp', { email, challengeId, otp, name });
  }

  function resendOtp(email, purpose) {
    return request('/api/auth/resend-otp', { email, purpose });
  }

  window.IndooneIndoVerification = {
    API_BASE,
    APP_NAME,
    requestSignupOtp,
    verifySignupOtp,
    requestLoginOtp,
    verifyLoginOtp,
    resendOtp
  };
})();
