// Renders the message list, the way the app draws it:
//   - an AI reply has the Indoone mark to its left
//   - a user reply is a violet bubble, right aligned
//   - sent photos stack, sent files show a badge
//   - while a reply is coming, the mark sits next to a spinner and a label
(() => {
  const ACTIVITY_LABELS = {
    received: 'Received',
    thinking: 'Thinking…',
    preparing: 'Preparing your answer…'
  };

  function logo() {
    const mark = document.createElement('span');
    mark.className = 'chat-logo';
    mark.setAttribute('aria-hidden', 'true');
    return mark;
  }

  function extensionOf(name) {
    const parts = String(name || '').split('.');
    const ext = parts.length > 1 ? parts.pop() : '';
    return (ext || 'FILE').slice(0, 4).toUpperCase();
  }

  function isImage(item) {
    const file = item.file;
    const type = file?.type || item.type || '';
    return type.startsWith('image/');
  }

  function sentAttachments(attachments) {
    const wrap = document.createElement('div');
    wrap.className = 'chat-attachments';

    attachments.forEach(item => {
      if (isImage(item) && item.url) {
        const image = document.createElement('img');
        image.className = 'chat-attachment-photo';
        image.src = item.url;
        image.alt = item.name || 'Sent photo';
        wrap.appendChild(image);
        return;
      }

      const badge = document.createElement('span');
      badge.className = 'chat-attachment-file';
      badge.textContent = extensionOf(item.name);
      wrap.appendChild(badge);
    });

    return wrap;
  }

  function bubble(message) {
    const row = document.createElement('div');
    row.className = message.mine ? 'chat-row mine' : 'chat-row theirs';

    if (!message.mine) {
      row.appendChild(logo());
    }

    const column = document.createElement('div');
    column.className = 'chat-column';

    if (message.attachments && message.attachments.length) {
      column.appendChild(sentAttachments(message.attachments));
    }

    if (message.text) {
      const body = document.createElement('div');
      body.className = 'chat-bubble';
      const text = document.createElement('span');
      text.className = 'chat-text';
      text.textContent = message.text;
      body.appendChild(text);
      column.appendChild(body);
    }

    row.appendChild(column);
    return row;
  }

  function activityBubble(state) {
    const row = document.createElement('div');
    row.className = 'chat-row theirs';
    row.appendChild(logo());

    const column = document.createElement('div');
    column.className = 'chat-column';

    const body = document.createElement('div');
    body.className = 'chat-bubble chat-activity';

    const spinner = document.createElement('span');
    spinner.className = 'chat-spinner';

    const label = document.createElement('span');
    label.textContent = ACTIVITY_LABELS[state] || ACTIVITY_LABELS.thinking;

    body.appendChild(spinner);
    body.appendChild(label);
    column.appendChild(body);
    row.appendChild(column);
    return row;
  }

  function render(list, messages, activity) {
    if (!list) return;

    list.textContent = '';

    messages.forEach(message => list.appendChild(bubble(message)));

    if (activity) {
      list.appendChild(activityBubble(activity));
    }
  }

  window.IndooneMessages = { render };
})();
