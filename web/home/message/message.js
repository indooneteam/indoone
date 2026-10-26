// Renders the message list, the way the app draws it:
//   - an AI reply has the Indoone mark to its left
//   - a user reply is a violet bubble, right aligned
//   - sent photos stack, sent files show a badge
//   - a URL in a reply gets its own row with a copy button
//   - while a reply is coming, the mark sits next to a spinner and a label
(() => {
  const ACTIVITY_LABELS = {
    received: 'Received',
    thinking: 'Thinking…',
    preparing: 'Preparing your answer…'
  };

  // HomeScreen.kt: REPLY_LINK_PATTERN = Regex("""https?://\S+""") - an http or
  // https URL, running to the next space.
  const REPLY_LINK_PATTERN = /https?:\/\/\S+/g;

  function logo() {
    const mark = document.createElement('span');
    mark.className = 'chat-logo';
    mark.setAttribute('aria-hidden', 'true');
    return mark;
  }

  function isImage(item) {
    const file = item.file;
    const type = file?.type || item.type || '';
    return type.startsWith('image/');
  }

  // The app's splitReplyIntoParts: the prose, then one part per URL, then the
  // tail. The prose keeps its own line breaks, so the bubble's pre-wrap still
  // does its job.
  function replyParts(text) {
    const source = String(text || '');
    const parts = [];
    let last = 0;
    let match;

    REPLY_LINK_PATTERN.lastIndex = 0;

    while ((match = REPLY_LINK_PATTERN.exec(source)) !== null) {
      const prose = source.slice(last, match.index);

      if (prose) {
        parts.push({ prose });
      }

      parts.push({ url: match[0] });
      last = match.index + match[0].length;
    }

    const tail = source.slice(last);

    if (tail) {
      parts.push({ prose: tail });
    }

    return parts;
  }

  function copyText(text) {
    if (navigator.clipboard?.writeText) {
      return navigator.clipboard.writeText(text);
    }

    // A tab served over plain http has no clipboard API, so fall back to the
    // old selection trick rather than leaving the button doing nothing.
    return new Promise((resolve, reject) => {
      const field = document.createElement('textarea');
      field.value = text;
      field.setAttribute('readonly', '');
      field.style.position = 'fixed';
      field.style.opacity = '0';
      document.body.appendChild(field);
      field.select();

      try {
        document.execCommand('copy') ? resolve() : reject(new Error('copy refused'));
      } catch (error) {
        reject(error);
      } finally {
        field.remove();
      }
    });
  }

  // The app's ReplyLinkRow: the URL in violet on one line, ellipsis if it is
  // long, and a copy button at the end of the row. The button puts the whole
  // URL on the clipboard and says so - a tap with no visible result reads as
  // though it did nothing.
  function linkRow(url, isFirst) {
    const row = document.createElement('div');
    row.className = isFirst ? 'chat-link-row' : 'chat-link-row chat-gap';

    const link = document.createElement('span');
    link.className = 'chat-link-url';
    link.textContent = url;

    const copy = document.createElement('button');
    copy.type = 'button';
    copy.className = 'chat-link-copy';
    copy.setAttribute('aria-label', 'Copy link');
    copy.innerHTML = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"'
      + ' stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">'
      + '<rect x="9" y="9" width="11" height="11" rx="2"></rect>'
      + '<path d="M5.5 15H5a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1h9a1 1 0 0 1 1 1v.5"></path></svg>';

    copy.addEventListener('click', () => {
      copyText(url)
        .then(() => window.toast?.('Link copied'))
        .catch(() => window.toast?.('Could not copy the link'));
    });

    row.appendChild(link);
    row.appendChild(copy);
    return row;
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

      // HomeScreen.kt draws a sent file as the attachment's own name in a
      // violet pill - Surface(widthIn(max = 220.dp), RoundedCornerShape(9.dp),
      // color = 0xFF8055DC, border = 0xFFB69AEF) with white 11sp text on two
      // lines at most. The web used to show a 48px tile holding the file's
      // extension instead, which is not what the app shows.
      const pill = document.createElement('span');
      pill.className = 'chat-attachment-file';
      pill.textContent = item.name || 'Attachment';
      wrap.appendChild(pill);
    });

    return wrap;
  }

  // Copy, and Share where the browser offers it.
  //
  // Share is `navigator.share`, which phones have and desktop Chrome does not - so the
  // button is only added when the API exists, rather than shown and failing. Copy is
  // always there, and says so when it works: a tap with no visible result reads as a
  // broken button.
  // ---- speech ---------------------------------------------------------------
  // One utterance at a time. Starting a second while the first is speaking talks over
  // itself, so whatever is playing is cancelled first, and the button that started it
  // goes back to "Listen".
  let speaking = null;

  function stopSpeaking() {
    if (!('speechSynthesis' in window)) {
      return;
    }

    window.speechSynthesis.cancel();

    const previous = speaking;
    speaking = null;

    if (previous && previous.button) {
      previous.button.textContent = 'Listen';
    }
  }

  function toggleSpeech(text, button) {
    if (!('speechSynthesis' in window)) {
      return;
    }

    if (speaking && speaking.text === text) {
      stopSpeaking();
      return;
    }

    stopSpeaking();

    const utterance = new SpeechSynthesisUtterance(text);
    const entry = { text, button };

    const settle = () => {
      if (speaking === entry) {
        speaking = null;
        button.textContent = 'Listen';
      }
    };

    utterance.onend = settle;
    utterance.onerror = settle;

    speaking = entry;
    button.textContent = 'Stop';
    window.speechSynthesis.speak(utterance);
  }

  // ---- feedback -------------------------------------------------------------
  // Local only for now. There is no endpoint that accepts a rating, and a button that
  // pretends to send one is worse than no button: the ratings are kept here so they can
  // go to the backend the day it has somewhere to put them.
  const FEEDBACK_KEY = 'indoone_feedback_v1';

  function readFeedback() {
    try {
      const parsed = JSON.parse(localStorage.getItem(FEEDBACK_KEY) || '[]');
      return Array.isArray(parsed) ? parsed : [];
    } catch (error) {
      return [];
    }
  }

  function recordFeedback(text, rating) {
    const kept = readFeedback().filter(item => item && item.text !== text);
    kept.push({ text: String(text).slice(0, 2000), rating, at: Date.now() });

    try {
      localStorage.setItem(FEEDBACK_KEY, JSON.stringify(kept.slice(-200)));
    } catch (error) {
      // A full quota must not break the thread.
    }
  }

  function ratingFor(text) {
    const match = readFeedback().find(item => item && item.text === text);
    return match ? match.rating : '';
  }

  // ---- retry ----------------------------------------------------------------
  // Re-ask the question behind the last reply. The trailing exchange is removed first -
  // the reply and the question it answered - because send() adds the question back. Both
  // are in the store, so the thread does not grow a duplicate turn.
  function retryLast(messages) {
    const store = window.IndooneChatStore;
    const send = window.IndooneSend;

    if (!store || !send || !Array.isArray(messages) || !messages.length) {
      return;
    }

    let question = '';

    for (let index = messages.length - 1; index >= 0; index -= 1) {
      if (messages[index].mine) {
        question = String(messages[index].text || '').trim();
        break;
      }
    }

    if (!question) {
      return;
    }

    const next = messages.slice();

    while (next.length && !next[next.length - 1].mine) {
      next.pop();
    }

    if (next.length && next[next.length - 1].mine) {
      next.pop();
    }

    store.replace(next);

    const input = document.getElementById('chatInput');

    if (input) {
      input.value = question;
      input.dispatchEvent(new Event('input', { bubbles: true }));
    }

    send.send();
  }

  function actionRow(message, options) {
    const text = String(message.text || '').trim();

    if (!text) {
      return null;
    }

    const row = document.createElement('div');
    row.className = 'chat-actions';

    const copy = document.createElement('button');
    copy.type = 'button';
    copy.className = 'chat-action';
    copy.textContent = 'Copy';
    copy.addEventListener('click', () => {
      copyText(text)
        .then(() => window.toast?.('Copied'))
        .catch(() => window.toast?.('Could not copy'));
    });
    row.appendChild(copy);

    if (typeof navigator.share === 'function') {
      const share = document.createElement('button');
      share.type = 'button';
      share.className = 'chat-action';
      share.textContent = 'Share';
      share.addEventListener('click', () => {
        // A dismissed share sheet rejects; that is not an error to report.
        Promise.resolve(navigator.share({ text })).catch(() => {});
      });
      row.appendChild(share);
    }

    if (!message.mine) {
      if ('speechSynthesis' in window) {
        const listen = document.createElement('button');
        listen.type = 'button';
        listen.className = 'chat-action';
        listen.textContent = 'Listen';
        listen.addEventListener('click', () => toggleSpeech(text, listen));
        row.appendChild(listen);
      }

      const rating = ratingFor(text);

      ['up', 'down'].forEach(kind => {
        const thumb = document.createElement('button');
        thumb.type = 'button';
        thumb.className = 'chat-action chat-rating' + (rating === kind ? ' is-on' : '');
        thumb.textContent = kind === 'up' ? 'Good' : 'Bad';
        thumb.setAttribute('aria-pressed', rating === kind ? 'true' : 'false');
        thumb.addEventListener('click', () => {
          recordFeedback(text, kind);
          row.querySelectorAll('.chat-rating').forEach(node => {
            node.classList.remove('is-on');
            node.setAttribute('aria-pressed', 'false');
          });
          thumb.classList.add('is-on');
          thumb.setAttribute('aria-pressed', 'true');
        });
        row.appendChild(thumb);
      });

      if (options && options.isLastAssistant && options.onRetry) {
        const retry = document.createElement('button');
        retry.type = 'button';
        retry.className = 'chat-action';
        retry.textContent = 'Retry';
        retry.addEventListener('click', options.onRetry);
        row.appendChild(retry);
      }
    }

    return row;
  }

  function bubble(message, options) {
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

      // A reply that carries links reads better as its prose and then one row
      // per link, each with a copy button, than as one run of text with the
      // URLs buried inside it.
      replyParts(message.text).forEach((part, index) => {
        if (part.url) {
          body.appendChild(linkRow(part.url, index === 0));
          return;
        }

        const text = document.createElement('span');
        text.className = index === 0 ? 'chat-text' : 'chat-text chat-gap';
        text.textContent = part.prose;
        body.appendChild(text);
      });

      column.appendChild(body);
    }

    const actions = actionRow(message, options);

    if (actions) {
      column.appendChild(actions);
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

    const lastAssistant = messages.reduce(
      (found, message, index) => (message.mine ? found : index),
      -1
    );

    messages.forEach((message, index) => {
      list.appendChild(
        bubble(message, {
          isLastAssistant: index === lastAssistant,
          onRetry: () => retryLast(messages)
        })
      );
    });

    if (activity) {
      list.appendChild(activityBubble(activity));
    }
  }

  window.IndooneMessages = { render };
})();
