# Indoone Authenticator

Private, offline-first TOTP authenticator prototype.

## Security

- TOTP codes are generated locally with Web Crypto.
- Vault data is encrypted locally with AES-GCM.
- The PIN is used only to derive the vault key via PBKDF2 and is not stored.
- No application server or account database is required.

This repository currently contains the browser demo/prototype. Native Android camera/biometric integration is a separate implementation step.
