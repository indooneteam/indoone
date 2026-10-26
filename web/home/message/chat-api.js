// Talks to the same backend the native app uses. The native client sends a
// Firebase ID token as a bearer token; this does the same, so one account
// works across the app and the web.
(() => {
  const BACKEND_URL = 'https://indooneai.duckdns.org';
  const READ_TIMEOUT_MS = 300000;
  const MAX_ATTACHMENTS = 10;
  const MAX_IMAGE_BYTES = 5000000;
  const MAX_DOCUMENT_BYTES = 8000000;
  const MAX_TOTAL_BYTES = 20000000;

  function firebaseUser() {
    return window.firebase?.auth?.().currentUser || null;
  }

  async function authToken() {
    const user = firebaseUser();

    if (!user) {
      throw new Error('Please sign in again.');
    }

    return user.getIdToken(false);
  }

  function bytesToBase64(bytes) {
    let binary = '';
    const chunk = 0x8000;

    for (let i = 0; i < bytes.length; i += chunk) {
      binary += String.fromCharCode.apply(
        null,
        bytes.subarray(i, i + chunk)
      );
    }

    return btoa(binary);
  }

  async function readFileBytes(file) {
    return new Uint8Array(await file.arrayBuffer());
  }

  function limitFor(mimeType) {
    return mimeType.startsWith('image/')
      ? MAX_IMAGE_BYTES
      : MAX_DOCUMENT_BYTES;
  }

  async function buildMediaPayload(attachments) {
    if (!attachments.length) {
      return null;
    }

    if (attachments.length > MAX_ATTACHMENTS) {
      throw new Error('You can attach up to 10 files per message.');
    }

    let totalBytes = 0;
    const media = [];

    for (const item of attachments) {
      const bytes = await readFileBytes(item.file);
      const mimeType = (item.file.type || 'application/octet-stream')
        .trim()
        .toLowerCase();
      const limit = limitFor(mimeType);

      if (bytes.length > limit) {
        // ChatApi.kt's own wording.
        throw new Error(
          mimeType.startsWith('image/')
            ? 'This attachment exceeds the 5 MB limit.'
            : 'This attachment exceeds the 8 MB limit.'
        );
      }

      totalBytes += bytes.length;

      if (totalBytes > MAX_TOTAL_BYTES) {
        throw new Error(
          'Attachments together exceed the 20 MB upload limit.'
        );
      }

      media.push({
        filename: item.file.name || 'attachment',
        mime_type: mimeType,
        content_base64: bytesToBase64(bytes)
      });
    }

    return media;
  }

  async function postJson(path, payload, signal) {
    const token = await authToken();
    const controller = new AbortController();
    const timer = setTimeout(
      () => controller.abort(),
      READ_TIMEOUT_MS
    );

    if (signal) {
      signal.addEventListener('abort', () => controller.abort());
    }

    try {
      const response = await fetch(`${BACKEND_URL}${path}`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload),
        signal: controller.signal
      });

      if (!response.ok) {
        const detail = await response.text().catch(() => '');

        throw new Error(
          `Indoone backend returned ${response.status}` +
            (detail ? `: ${detail.slice(0, 200)}` : '')
        );
      }

      return response.json();
    } catch (error) {
      // A fetch that never reached the server throws a TypeError with no
      // status. That is what a blocked cross-origin call looks like, and it
      // was being flattened into "could not reach" - which tells nobody
      // anything. Say which it is.
      if (error instanceof TypeError) {
        throw new Error(
          'The server did not accept the call from this page ' +
            '(a cross-origin block, or no network).'
        );
      }

      throw error;
    } finally {
      clearTimeout(timer);
    }
  }

  async function send({ message, conversationId, attachments = [] }) {
    const media = await buildMediaPayload(attachments);
    const payload = { message };

    if (conversationId) {
      payload.conversation_id = conversationId;
    }

    // The key is "attachments", not "media_attachments". ChatApi.kt builds the
    // same payload and names the array "attachments" (ChatApi.kt: put("attachments",
    // attachmentArray)); the backend reads that name. This page used to send
    // media_attachments, which the backend did not know, so a photo went up and
    // was ignored - the reply came back as if no photo had been sent.
    if (media && media.length) {
      payload.attachments = media;
    }

    const json = await postJson('/api/chat', payload);

    if (!json.conversation_id) {
      throw new Error('Indoone backend returned no conversation id.');
    }

    if (!json.reply) {
      throw new Error('Indoone backend returned an empty reply.');
    }

    return {
      conversationId: json.conversation_id,
      reply: json.reply,
      // Follow-ups the backend offered with this reply. Empty for most answers; a held
      // write carries Confirm/Cancel so the user presses a button instead of typing a
      // word the reply had to spell out.
      chips: Array.isArray(json.action_chips) ? json.action_chips : []
    };
  }

  async function uploadFile(file) {
    const bytes = await readFileBytes(file);
    const json = await postJson('/api/files', {
      filename: file.name || 'file',
      content_base64: bytesToBase64(bytes)
    });

    return {
      fileId: json.file_id,
      filename: json.filename,
      bytes: json.bytes,
      textPreview: json.text_preview
    };
  }

  window.IndooneChatApi = {
    send,
    uploadFile,
    backendUrl: BACKEND_URL
  };
})();
