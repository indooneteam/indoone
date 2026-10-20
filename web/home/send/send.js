// Sending: the pending attachments, the user bubble, the call, the reply, and
// the save to the account. HomeScreen's sendMessage() does the same, including
// the three activity labels.
(() => {
  const MAX_ATTACHMENTS = 10;
  let pending = [];
  let sending = false;

  function setActivity(state) {
    window.IndooneMessages?.render(
      document.getElementById('chatList'),
      window.IndooneChatStore?.getMessages?.() || [],
      state
    );
    window.IndooneChatScroll?.toBottom?.();
  }

  function isImage(item) {
    return String(item.file?.type || '').startsWith('image/');
  }

  function extensionOf(name) {
    const parts = String(name || '').split('.');
    const ext = parts.length > 1 ? parts.pop() : '';
    return (ext || 'FILE').slice(0, 4).toUpperCase();
  }

  function renderPending() {
    const host = document.getElementById('pendingAttachments');

    if (!host) {
      return;
    }

    host.textContent = '';

    if (!pending.length) {
      host.setAttribute('hidden', '');
      window.IndooneChatHome?.syncComposer?.();
      return;
    }

    host.removeAttribute('hidden');

    pending.forEach((item, index) => {
      const wrap = document.createElement('div');
      wrap.className = 'pending-item';

      const thumb = document.createElement('div');
      thumb.className = 'pending-thumb';

      if (isImage(item) && item.url) {
        const image = document.createElement('img');
        image.src = item.url;
        image.alt = item.file.name || 'Attached photo preview';
        thumb.appendChild(image);
      } else {
        const badge = document.createElement('span');
        badge.className = 'pending-badge';
        badge.textContent = extensionOf(item.file?.name);
        thumb.appendChild(badge);
      }

      const remove = document.createElement('button');
      remove.type = 'button';
      remove.className = 'pending-remove';
      remove.setAttribute('aria-label', 'Remove attachment');
      remove.textContent = '×';
      remove.disabled = sending;
      remove.addEventListener('click', () => {
        if (sending) {
          return;
        }

        if (item.url) {
          URL.revokeObjectURL(item.url);
        }

        pending.splice(index, 1);
        renderPending();
      });

      thumb.appendChild(remove);

      const name = document.createElement('span');
      name.className = 'pending-name';
      name.textContent = item.file?.name || 'attachment';

      wrap.appendChild(thumb);
      wrap.appendChild(name);
      host.appendChild(wrap);
    });

    window.IndooneChatHome?.syncComposer?.();
  }

  function attach(file) {
    if (!file) {
      return;
    }

    if (pending.length >= MAX_ATTACHMENTS) {
      window.IndooneMessages?.render(
        document.getElementById('chatList'),
        [
          ...(window.IndooneChatStore?.getMessages?.() || []),
          { text: 'You can attach up to 10 files per message.', mine: false }
        ],
        null
      );
      return;
    }

    pending.push({
      file,
      url: isImage({ file }) ? URL.createObjectURL(file) : null
    });
    renderPending();
  }

  // keepUrls holds the object URLs that a sent message still points at. Only
  // the ones nobody kept are revoked.
  function clearPending(keepUrls) {
    pending.forEach(item => {
      if (item.url && !(keepUrls && keepUrls.has(item.url))) {
        URL.revokeObjectURL(item.url);
      }
    });
    pending = [];
    renderPending();
  }

  function setBusy(busy) {
    sending = busy;

    const send = document.getElementById('sendBtn');
    const arrow = send?.querySelector('.send-arrow');
    const spinner = send?.querySelector('.send-spinner');

    if (arrow) {
      arrow.hidden = busy;
    }

    if (spinner) {
      spinner.hidden = !busy;
    }

    window.IndooneChatHome?.syncComposer?.();
  }

  async function send() {
    const input = document.getElementById('chatInput');
    const text = (input?.value || '').trim();
    const attachments = pending.slice();
    const store = window.IndooneChatStore;

    if (!store || sending || (!text && !attachments.length)) {
      return;
    }

    const message = text || (
      'Please inspect the attached file(s) and describe their contents ' +
      'or visible details.'
    );

    // The bubble renders the photo straight from item.url, so the message and
    // clearPending() have to agree on which object URLs are still needed.
    const sentAttachments = attachments.map(item => ({
      name: item.file.name,
      type: item.file.type,
      url: item.url
    }));

    const mine = store.add({
      text,
      mine: true,
      attachments: sentAttachments
    });

    if (input) {
      input.value = '';
      input.style.height = 'auto';
    }

    const conversationId = store.ensureConversationId();

    // Hand the sent photos' URLs to clearPending so it does NOT revoke them.
    // Revoking here is what left the sent photo as a broken image with only its
    // filename showing: the bubble still pointed at the URL we had just killed.
    clearPending(new Set(sentAttachments.map(item => item.url).filter(Boolean)));
    setBusy(true);
    // HomeScreen.kt shows Received for 350ms, then Thinking while the request
    // is in flight, then Preparing for 600ms before the reply.
    setActivity('received');
    await new Promise(resolve => setTimeout(resolve, 350));
    setActivity('thinking');

    // Save the user's side to the account, exactly as the app does.
    void window.IndooneCloudChat
      ?.appendMessage?.(conversationId, 'user', text || message)
      .catch(error => console.warn('Indoone chat save failed:', error));

    try {
      const api = window.IndooneChatApi;

      if (!api) {
        throw new Error('Indoone AI is not reachable right now.');
      }

      const result = await api.send({
        message,
        conversationId,
        attachments
      });

      store.setConversationId(result.conversationId);
      setActivity('preparing');
      await new Promise(resolve => setTimeout(resolve, 600));

      store.add({ text: result.reply, mine: false });

      void window.IndooneCloudChat
        ?.appendMessage?.(result.conversationId, 'assistant', result.reply)
        .catch(error => console.warn('Indoone chat save failed:', error));
    } catch (error) {
      console.error('Indoone chat send failed:', error);

      store.remove(mine);
      store.add({
        // never the raw failure text
        text:
          error?.message &&
          !/undefined|not a function|null/i.test(error.message)
            ? error.message
            : 'Indoone AI could not complete the request.',
        mine: false
      });
    } finally {
      setBusy(false);

      const messages = store.getMessages();

      window.IndooneMessages?.render(
        document.getElementById('chatList'),
        messages,
        null
      );

      // The welcome block goes the moment there is a message. HomeScreen.kt
      // only draws it while messages.isEmpty(), and paintThread() in
      // chat-home.js does the same - but send() renders the thread itself and
      // never went through paintThread, so the welcome stayed on screen after
      // the first send. Same condition as paintThread.
      document
        .getElementById('chatWelcome')
        ?.toggleAttribute('hidden', messages.length > 0);

      window.IndooneChatScroll?.toBottom?.();
      window.IndooneChatHome?.syncComposer?.();
    }
  }

  window.IndooneSend = {
    send,
    attach,
    clearPending,
    getPending: () => pending
  };
})();
