(() => {
  const API_BASE = 'https://indoverification-production.up.railway.app';
  const APP_ID = 'indoone';
  const APP_NAME = 'Indoone';
  // Give the shared SMTP -> Zoho fallback enough time to finish delivery.
  const REQUEST_TIMEOUT_MS = 45000;
  const REQUEST_DEDUPE_MS = 3000;
  const inFlight = new Map();

  async function request(path, body) {
    const payload = { ...(body || {}), appId: APP_ID };
    const requestKey = `${path}:${JSON.stringify(payload)}`;
    const existing = inFlight.get(requestKey);
    if (existing) return existing;

    const promise = (async () => {
      const controller = new AbortController();
      const timer = window.setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);
      try {
        const response = await fetch(`${API_BASE}${path}`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(payload),
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
          throw new Error('Unable to reach the OTP service. Check your connection and try again.');
        }
        throw error;
      } finally {
        window.clearTimeout(timer);
      }
    })();

    inFlight.set(requestKey, promise);
    void promise.then(
      () => window.setTimeout(() => {
        if (inFlight.get(requestKey) === promise) inFlight.delete(requestKey);
      }, REQUEST_DEDUPE_MS),
      () => window.setTimeout(() => {
        if (inFlight.get(requestKey) === promise) inFlight.delete(requestKey);
      }, REQUEST_DEDUPE_MS),
    );
    return promise;
  }

  function requestSignupOtp(email, name = 'Indoone user') {
    return request('/api/auth/signup/request-otp', { email, name });
  }

  function verifySignupOtp({ email, challengeId, otp, name }) {
    return request('/api/auth/signup/verify-otp', { email, challengeId, otp, name });
  }

  function sendSignupWelcome({ email, welcomeToken, name = 'Indoone user' }) {
    return request('/api/auth/signup/welcome', { email, welcomeToken, name });
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
    APP_ID,
    APP_NAME,
    requestSignupOtp,
    verifySignupOtp,
    sendSignupWelcome,
    requestLoginOtp,
    verifyLoginOtp,
    resendOtp
  };
})();
