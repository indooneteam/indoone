# Indoone Integrations Architecture

This package is the Android entry point for business integrations.

## Current V1 boundary

- Connect screen: platform selection only.
- Platform screens: Instagram and WhatsApp setup architecture.
- IntegrationApi: backend contract for account listing, authorization start and disconnect.
- Provider credentials and webhook handling stay on the backend.
- Every connection is scoped to the signed-in Indoone user and the provider account ID.

## Instagram flow

Indoone user -> Instagram authorization -> connected account -> posts/Reels -> automation rule -> webhook comment -> exact media/comment mapping -> fixed public reply and/or DM -> outbound audit.

## WhatsApp flow

Indoone user -> WhatsApp Business authorization -> connected business account/phone -> webhook message -> exact connection mapping -> automation/AI action -> outbound message -> delivery audit.

## Invariant

Never choose an integration account from an unscoped provider event alone. Resolve the Indoone user/connection first, then the platform object, then the automation rule.

## Next implementation boundary

IntegrationApi.startConnection(...) becomes the only Android entry point for Meta authorization. The UI should not be changed when the backend OAuth endpoint is ready.
