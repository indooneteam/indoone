window.initMenuAbout = function () {
  const modal = document.getElementById('modal');

  modal.innerHTML = `
    <div
      class="modal-head"
    >
      <h2>
        About Indoone
      </h2>
      <button
        class="close-btn"
        data-close
      >
        ×
      </button>
    </div>

    <div
      style="text-align:center"
    >
      <div
        class="token-icon branding-image"
        role="img"
        aria-label="Indoone logo"
      >
        <svg
          viewBox="0 0 512 512"
          aria-hidden="true"
          focusable="false"
        >
          <defs>
            <linearGradient id="indooneMenuAboutGradient" x1="0" y1="1" x2="1" y2="0">
              <stop offset="0" stop-color="#32106F"></stop>
              <stop offset="0.5" stop-color="#6526C7"></stop>
              <stop offset="1" stop-color="#B277FF"></stop>
            </linearGradient>
          </defs>
          <path d="M126 154 C176 138 258 145 344 115 C382 102 407 81 421 52 L421 112 C411 142 388 161 353 173 C264 203 190 181 126 206 Z" fill="url(#indooneMenuAboutGradient)"></path>
          <path d="M126 235 C188 211 258 224 350 193 C387 181 408 162 421 137 L421 198 C411 226 388 245 351 257 C263 286 190 260 126 285 Z" fill="url(#indooneMenuAboutGradient)"></path>
          <path d="M126 304 C190 279 258 296 349 265 C386 253 409 235 421 210 L421 270 C411 301 387 323 350 336 C273 364 205 342 166 360 C136 374 126 397 128 419 C130 444 147 457 169 457 C190 457 205 445 205 421 L205 364 C253 378 304 372 351 351 C390 334 413 309 421 281 C418 344 383 389 326 407 C274 423 238 427 208 445 C185 459 154 458 136 440 C114 418 109 387 112 357 C114 336 119 320 126 304 Z" fill="url(#indooneMenuAboutGradient)"></path>
        </svg>
      </div>
      <h3
        style="margin:8px 0 2px"
      >
        Indoone Authenticator
      </h3>
      <p
        style="margin:0;color:#8a8492;font-size:12px"
      >
        Secure • Private • Cloud Synced
      </p>
    </div>

    <div
      class="settings-row"
    >
      <span>
        <b>
          About Indoone
        </b>
        <small>
          Securely generate and manage time-based one-time passwords for your accounts.
        </small>
      </span>
    </div>

    <div
      class="settings-row"
    >
      <span>
        <b>
          Sync
        </b>
        <small>
          Secure cloud sync across your signed-in devices
        </small>
      </span>
    </div>

    <div
      class="settings-row"
    >
      <span>
        <b>
          OTP Standard
        </b>
        <small>
          TOTP • 6/8 digits • SHA-1 / SHA-256 / SHA-512
        </small>
      </span>
    </div>

    <div
      class="settings-row"
    >
      <span>
        <b>
          Account Storage
        </b>
        <small>
          Cloud synced with your Indoone account
        </small>
      </span>
    </div>

    <div
      class="settings-row"
    >
      <span>
        <b>
          Legal
        </b>
        <small>
          Privacy Policy • Terms of Service
        </small>
      </span>
    </div>

    <div
      class="settings-row"
    >
      <span>
        <b>
          Licenses
        </b>
        <small>
          Open Source Licenses
        </small>
      </span>
    </div>

    <p
      style="text-align:center;margin:12px 0 2px;color:#8a8492;font-size:10px"
    >
      © 2026 Indoone
    </p>
  `;
};
