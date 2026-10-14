// Renders the message list. Text is set with textContent so a reply can never
// inject markup into the page.
(() => {
  function bubble(message) {
    const row = document.createElement('div');
    row.className = message.mine ? 'chat-row mine' : 'chat-row theirs';

    const body = document.createElement('div');
    body.className = 'chat-bubble';

    if (message.attachments && message.attachments.length) {
      const names = document.createElement('div');
      names.className = 'chat-attachment-note';
      names.textContent = message.attachments
        .map(item => item.file?.name || item.name || 'attachment')
        .join(', ');
      body.appendChild(names);
    }

    if (message.text) {
      const text = document.createElement('div');
      text.className = 'chat-text';
      text.textContent = message.text;
      body.appendChild(text);
    }

    row.appendChild(body);
    return row;
  }

  function render(list, messages, pendingLabel) {
    list.textContent = '';

    messages.forEach(message => list.appendChild(bubble(message)));

    if (pendingLabel) {
      const waiting = document.createElement('div');
      waiting.className = 'chat-row theirs';

      const body = document.createElement('div');
      body.className = 'chat-bubble chat-pending';
      body.textContent = pendingLabel;

      waiting.appendChild(body);
      list.appendChild(waiting);
    }
  }

  window.IndooneMessages = { render };
})();
