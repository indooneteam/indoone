# Indoone

Indoone is a native Android AI application built around a central AI Engine, Firebase identity/profile services, server-side chat storage, long-term memory, tools/research, and a continuously validated AI/model pipeline.

## Current branch workflow

- `main` = stable/current baseline.
- `develop` = active development branch.
- New feature work starts on `develop`, is tested, and is merged to `main` only after verification.
- Do not introduce web-technology architecture into the native Android app. The app remains Kotlin + Jetpack Compose.

## Existing authentication — DO NOT CHANGE

The existing account/authentication flow is intentionally kept as-is:

- Account creation uses the existing email + mobile + password + OTP flow.
- Email/mobile identity checks and the existing mobile-to-email lookup remain unchanged.
- Login accepts email or mobile number, resolves the account, verifies the existing password, and completes the existing OTP verification flow.
- Existing TOTP/account functionality remains unchanged.
- Existing Firebase Authentication and Realtime Database structures must not be broken while new AI/chat features are added.

## Existing Firebase Realtime Database — keep existing data intact

Current account data already lives under the user's Firebase UID, including existing profile/account information and the mobile index. These existing paths are not to be renamed or removed.

New AI-related data should be added alongside the existing structure, for example:

```text
users/
  <uid>/
    profile/          # existing account/profile data
    accounts/        # existing TOTP/account data
    memory/          # new long-term AI memory
    state/           # new small realtime user state
mobileIndex/         # existing mobile → account lookup
```

## Product data architecture

### 1. Firebase Authentication

Source of identity.

- Firebase UID is the stable user identifier.
- Email/mobile/password/OTP/TOTP authentication stays on the existing system.
- Chat, memory and profile ownership are tied to the Firebase UID.

### 2. Firebase Realtime Database

Used for small, important, realtime user data.

- Existing profile/account data.
- User-controlled profile values.
- Long-term AI memory.
- Small realtime state required by the app.
- Multi-device realtime memory/profile updates.

Do not put full large chat history into this database by default.

### 3. Cloud Firestore

Used as the chat database layer.

Target structure:

```text
conversations/
  <conversationId>/
    userId
    status
    createdAt
    updatedAt
    messageCount

messages/
  <messageId>/
    conversationId
    userId
    role
    content
    createdAt
```

A conversation is limited to 50 messages. When the 50-message limit is reached, the app automatically closes that conversation, informs the user, and opens a new conversation. Retention/cleanup must follow the approved 7-day inactive-chat policy.

The same Firebase UID allows a user to see the same server-side chat data from another device.

### 4. Backblaze B2 / object storage

Used for large files, model artifacts, backups, and optional archived conversation files where appropriate. It is not the primary live chat query database.

### 5. GitHub

Stores source code, tests, static project assets, training/evaluation source data, and documentation. It must not be used as live user-chat or user-memory storage.

## Profile vs Memory vs Chat History

These are three separate concepts and must remain separate in the implementation.

### Profile

User-controlled stable information.

Example:

```text
name
profession
preferred language
```

Important rule: the AI must not casually overwrite the user's real name. Profile values are changed explicitly by the user through the profile UI (or by a dedicated trusted profile update flow).

### Long-term Memory

AI-managed important information that is useful across future conversations and devices.

Example:

```text
nickname = Bro
preference = concise answers
interest = AI development
```

Only important/stable information should be persisted as long-term memory. Temporary statements should remain ordinary chat context and must not automatically become permanent memory.

### Chat History

The conversation record. Chat history and long-term memory are independent.

A chat can be deleted after its retention period while an important memory extracted from that chat remains.

## AI Memory rules

The AI must never blindly write every message into memory.

Target flow:

```text
User message
    ↓
Central AI Engine
    ↓
Memory Analyzer
    ↓
Is this important and stable?
    ↓
Does it create/update a known memory?
    ↓
Validate source + confidence + conflict
    ↓
Write/update memory
    ↓
Realtime sync to the user's other devices
```

Examples:

```text
“My name is Yashwanth.”
→ profile/name only through explicit profile handling.

“Call me Darling.”
→ nickname = Darling.

“Darling beda, Bro anta kareyiri.”
→ remove/replace nickname Darling → Bro.
```

Latest clear user instruction wins for mutable memory such as nickname/preferences. The AI must not treat temporary events as permanent facts without sufficient confidence.

Every memory record should support provenance and update metadata such as source, importance/confidence, createdAt and updatedAt.

## Memory screen

Add a native Android `Memory` entry in the app menu.

The user must be able to:

- View what Indoone remembers.
- Edit appropriate memory/profile values.
- Delete memories.
- Understand that profile data and AI memory are different.

The memory UI is backed by the same user UID, so updates made on one device propagate to the user's other logged-in devices.

## Multi-device synchronization

The server is the source of truth. Local Android storage is only a cache/fallback where appropriate.

```text
Device A
   ↓
Backend / Firebase
   ↓
Server-side data
   ↓
Realtime update
   ↓
Device B
```

A new device must be able to sign in with the same account and fetch existing chat/profile/memory data. New changes made from either device should synchronize to the other device.

## Central Indoone AI Engine

The AI model is not the whole product. The central engine is responsible for deciding what information is needed before generating an answer.

Target flow:

```text
User request
   ↓
Identity / user context
   ↓
Relevant chat history
   ↓
Relevant long-term memory
   ↓
Files / knowledge / research / tools as required
   ↓
Intent + planning
   ↓
Context builder
   ↓
AI model
   ↓
Verification / language / safety checks
   ↓
Final response
```

The model should receive only the context relevant to the current request instead of blindly receiving every historical record.

## A-to-Z implementation roadmap

The roadmap is intentionally incremental. Each phase must be implemented, tested, and verified before moving to the next phase.

1. **Authentication stability** — preserve the existing account/login/OTP/TOTP system; no unnecessary auth rewrite.
2. **Firebase data safety** — preserve current RTDB account/profile structures and security rules.
3. **Chat data layer** — connect the Android chat flow to the approved server-side chat database.
4. **Conversation model** — implement users → conversations → messages ownership and indexes.
5. **50-message lifecycle** — enforce 50 messages per conversation, automatic close, user notification, and automatic new-chat creation.
6. **Multi-device chat sync** — fetch server-side history on login and propagate realtime changes between logged-in devices.
7. **Chat retention** — implement the approved 7-day inactive-chat cleanup policy without deleting independent long-term memory.
8. **Profile layer** — provide explicit user-controlled profile fields such as name/profession without letting general AI memory overwrite the real name.
9. **Memory schema** — implement stable memory records keyed by Firebase UID.
10. **Memory Analyzer** — detect important/stable information from conversation content.
11. **Memory update engine** — replace obsolete mutable values and keep the latest valid user instruction.
12. **Memory security** — ensure a user can only access and modify their own memory.
13. **Memory UI** — add Menu → Memory with view/edit/delete controls.
14. **Memory retrieval** — retrieve only relevant memories for each question.
15. **Central AI Engine** — create the orchestrator that coordinates profile, memory, chat, knowledge, research, and tools.
16. **Intent detection** — classify what the user is asking and what information/tools are required.
17. **Context builder** — assemble a compact, relevant context for the AI model.
18. **Verification** — validate language, formatting, groundedness, and response quality before returning the answer.
19. **Knowledge/RAG layer** — add trusted knowledge sources and retrieval.
20. **Research layer** — add controlled web/research providers only when required by the request.
21. **Tool system** — add calculator, file search, web search and other safe built-in tools through a registry/executor.
22. **File system** — support user documents, images, and other files with proper storage and retrieval.
23. **Streaming** — move chat responses toward realtime token/response streaming.
24. **Voice** — add speech input/output after text chat is stable.
25. **Vision** — support image understanding and multimodal context.
26. **Learning data pipeline** — collect approved training/evaluation data separately from live user memory.
27. **Evaluation** — maintain behavioral, language, safety, regression and benchmark tests.
28. **Model training** — improve the custom Indoone model using validated training/evaluation pipelines.
29. **Model promotion** — only promote models that pass frozen behavioral/quality gates.
30. **Production hardening** — rate limiting, observability, backups, failure handling, security review and end-to-end tests.
31. **Infrastructure scaling** — migrate storage/database components to owned or more scalable infrastructure when usage and cost justify it, without changing the user-facing data model.
32. **Long-term product expansion** — continue improving the AI Engine, tools, memory, research, knowledge, voice and vision while preserving backward compatibility.

## Non-negotiable design rules

- Do not mix authentication, profile, memory, chat history, files, and model artifacts into one storage system without a clear reason.
- Do not let the AI silently overwrite protected profile identity fields such as the user's real name.
- Do not store every chat message as permanent long-term memory.
- Do not use GitHub as live user data storage.
- Do not make web technology the architecture of the native Android application.
- Do not replace working authentication/TOTP behavior while implementing chat/memory features unless a future change explicitly requires it.
- Every major change is developed and tested on `develop` before promotion to `main`.
