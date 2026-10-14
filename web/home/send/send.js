// Owns sending: the pending attachments, the optimistic user bubble, the call,
// and the reply. Native does the same in HomeScreen's sendMessage().
(() => {
  let pending = [];

  function pendingHost() {
    return document.getElementById('pendingAttachments');
  }

  function renderPending() {
    const host = pendingHost();

    if (!host) {
      return;
    }

    host.textContent = '';

    if (!pending.length) {
      host.setAttribute('hidden', '');
      return;
    }

    host.removeAttribute('hidden');

    pending.forEach((item, index) => {
      const chip = document.createElement('span');
      chip.className = 'pending-chip';

      const name = document.createElement('b');
      name.textContent = item.file.name || 'attachment';

      const remove = document.createElement('button');
      remove.type = 'button';
      remove.setAttribute('aria-label', 'Remove attachment');
      remove.textContent = '×';
      remove.addEventListener('click', () => {
        pending.splice(index, 1);
        renderPending();
      });

      chip.appendChild(name);
      chip.appendChild(remove);
      host.appendChild(chip);
    });
  }

  function attach(file) {
    if (!file) {
      return;
    }

    if (pending.length >= 10) {
      window.toast?.('You can attach up to 10 files per message.');
      return;
    }

    pending.push({ file });
    renderPending();
  }

  function clearPending() {
    pending = [];
    renderPending();
  }

  function setBusy(busy) {
    const send = document.getElementById('sendBtn');
    const input = document.getElementById('chatInput');

    if (send) {
      send.disabled = busy;
    }

    if (input) {
      input.disabled = busy;
    }
  }

  async function send() {
    const input = document.getElementById('chatInput');
    const text = (input?.value || '').trim();
    const attachments = pending.slice();
    const store = window.IndooneChatStore;

    if (!store || (!text && !attachments.length)) {
      return;
    }

    const message = text || (
      'Please inspect the attached file(s) and describe their contents ' +
      'or visible details.'
    );

    const mine = store.add({
      text,
      mine: true,
      attachments: attachments.map(item => ({ name: item.file.name }))
    });

    if (input) {
      input.value = '';
      input.style.height = 'auto';
    }

    clearPending();
    setBusy(true);
    window.IndooneMessages?.render(
      document.getElementById('chatList'),
      store.getMessages(),
      'Thinking…'
    );
    window.IndooneChatScroll?.toBottom?.();

    try {
      const result = await window.IndooneChatApi.send({
        message,
        conversationId: store.getConversationId(),
        attachments
      });

      store.setConversationId(result.conversationId);
      store.add({ text: result.reply, mine: false });
    } catch (error) {
      console.error('Indoone chat send failed:', error);

      store.remove(mine);
      store.add({
        text: `I could not reach Indoone AI. ${error.message || ''}`.trim(),
        mine: false
      });
    } finally {
      setBusy(false);
      window.IndooneMessages?.render(
        document.getElementById('chatList'),
        store.getMessages(),
        null
      );
      window.IndooneChatScroll?.toBottom?.();
    }
  }

  window.IndooneSend = {
    send,
    attach,
    clearPending,
    getPending: () => pending
  };
})();
