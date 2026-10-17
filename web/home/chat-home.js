// The chat home, the web counterpart of android/home/HomeScreen.kt.
(() => {
  const MODULES = [
    // chat-api.js was written but never listed here, so window.IndooneChatApi
    // was undefined and both the composer and Vibe threw the moment they tried
    // to send. It goes first, because the others call it.
    'home/message/chat-api.js?v=20261025a',
    'home/message/cloud-chat.js?v=20261031e',
    'home/message/chat-store.js?v=20261031b',
    'home/message/message.js?v=20261015a',
    'home/send/send.js?v=20261015a',
    'home/voice/voice.js?v=20261015a',
    'home/plus/plus.js?v=20261015a',
    'home/plus/camera/camera.js?v=20261015a',
    'home/plus/photos/photos.js?v=20261015a',
    'home/plus/files/files.js?v=20261015a',
    'vibe/vibe-live.js?v=20261029a',
    'vibe/vibe.js?v=20261029a'
  ];

  // The app alternates these two, every two seconds.
  const WELCOME_LINES = [
    'I am Indoone AI. How can I help you today?',
    'Ask me anything.'
  ];

  let ready = null;
  let welcomeTimer = null;
  let welcomeIndex = 0;

  function loadScript(src) {
    return new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = src;
      script.onload = resolve;
      script.onerror = () => reject(new Error(`Could not load ${src}`));
      document.head.appendChild(script);
    });
  }

  function loadModules() {
    if (!ready) {
      ready = Promise.all(MODULES.map(loadScript));
    }

    return ready;
  }

  function showHistorySpinner(on) {
    const list = document.getElementById('chatList');
    const welcome = document.getElementById('chatWelcome');

    if (!list) return;

    if (on) {
      welcome?.setAttribute('hidden', '');
      list.textContent = '';
      const row = document.createElement('div');
      row.className = 'chat-loading';
      row.appendChild(document.createElement('span'));
      list.appendChild(row);
    } else {
      list.textContent = '';
    }
  }

  function scrollToBottom() {
    const scroller = document.getElementById('chatScroll');

    if (scroller) {
      scroller.scrollTop = scroller.scrollHeight;
    }
  }

  function paintWelcome() {
    const line = document.getElementById('welcomeLine');

    if (line) {
      line.textContent = WELCOME_LINES[welcomeIndex % WELCOME_LINES.length];
    }
  }

  function startWelcomeLoop() {
    if (welcomeTimer) {
      return;
    }

    welcomeTimer = setInterval(() => {
      welcomeIndex += 1;
      paintWelcome();
    }, 2000);
  }

  function stopWelcomeLoop() {
    if (welcomeTimer) {
      clearInterval(welcomeTimer);
      welcomeTimer = null;
    }
  }

  function paintThread() {
    const list = document.getElementById('chatList');
    const store = window.IndooneChatStore;
    const welcome = document.getElementById('chatWelcome');
    const messages = store ? store.getMessages() : [];

    window.IndooneMessages?.render(list, messages, null);
    welcome?.toggleAttribute('hidden', messages.length > 0);
    scrollToBottom();

    if (messages.length) {
      stopWelcomeLoop();
    } else {
      startWelcomeLoop();
    }
  }

  function bindQuickPrompts() {
    document.querySelectorAll('.quick-chip').forEach(chip => {
      if (chip.dataset.promptBound === 'true') {
        return;
      }

      chip.dataset.promptBound = 'true';

      chip.addEventListener('click', () => {
        const input = document.getElementById('chatInput');

        if (input) {
          input.value = chip.dataset.prompt || chip.textContent.trim();
          input.dispatchEvent(new Event('input', { bubbles: true }));
          input.focus();
        }
      });
    });
  }

  // The app shows Send only when there is text or an attachment; with an empty
  // box the same spot holds the Vibe button.
  function syncComposer() {
    const input = document.getElementById('chatInput');
    const send = document.getElementById('sendBtn');
    const vibe = document.getElementById('vibeBtn');
    const listening = document.getElementById('listeningCard');
    const mic = document.getElementById('voiceBtn');
    const pending = window.IndooneSend?.getPending?.() || [];
    const hasText = Boolean((input?.value || '').trim());
    const showSend = hasText || pending.length > 0;
    const isListening = Boolean(listening && !listening.hasAttribute('hidden'));

    if (send) {
      send.hidden = !showSend;
      send.disabled = isListening;
    }

    if (vibe) {
      vibe.hidden = showSend || isListening;
    }

    mic?.classList.toggle('listening', isListening);
  }

  function autoGrow(event) {
    const input = event.target;
    input.style.height = 'auto';
    input.style.height = `${Math.min(input.scrollHeight, 120)}px`;
  }

  // home/home.js loads first and publishes IndooneHome with its own init, and
  // events.js calls that one. This file's init - the one that loads chat-api,
  // cloud-chat, chat-store and the rest of the chat - was never published and
  // never called, so none of those modules ever loaded. Nothing could send, and
  // nothing reached the history. It runs itself now.
  async function init() {
    const page = document.getElementById('homeContent');

    if (!page || page.dataset.ready === 'true') {
      return;
    }

    page.dataset.ready = 'true';

    try {
      await loadModules();
    } catch (error) {
      console.error('Indoone chat home could not load:', error);
      // Even then the composer must be in the right state: no Vibe button and
      // no account "+" left on screen.
      syncComposer();
      return;
    }

    window.IndooneChatScroll = { toBottom: scrollToBottom };
    window.IndoonePlusSheet?.init(document.getElementById('plusSheet'));
    window.IndooneVibe?.init();

    bindQuickPrompts();
    paintWelcome();
    startWelcomeLoop();

    document
      .getElementById('plusBtn')
      ?.addEventListener('click', () => {
        const sheet = window.IndoonePlusSheet;

        if (sheet?.isOpen?.()) {
          sheet.close();
        } else {
          sheet?.open?.();
        }
      });

    document
      .getElementById('voiceBtn')
      ?.addEventListener('click', () => window.IndooneVoice?.toggle?.());

    document
      .getElementById('sendBtn')
      ?.addEventListener('click', () => window.IndooneSend?.send?.());

    document
      .getElementById('vibeBtn')
      ?.addEventListener('click', () => window.IndooneVibe?.open?.());

    const vibe = document.getElementById('vibeBtn');

    if (vibe && !vibe.querySelector('.vibe-orbit')) {
      const shine = document.createElement('span');
      shine.className = 'vibe-shine';

      const orbit = document.createElement('span');
      orbit.className = 'vibe-orbit';
      orbit.appendChild(document.createElement('i'));

      vibe.appendChild(shine);
      vibe.appendChild(orbit);
    }

    const input = document.getElementById('chatInput');

    input?.addEventListener('input', event => {
      autoGrow(event);
      syncComposer();
    });

    input?.addEventListener('keydown', event => {
      if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault();
        void window.IndooneSend?.send?.();
      }
    });

    // The app shows a spinner while it reads the saved conversation.
    showHistorySpinner(true);

    try {
      await window.IndooneChatStore?.load?.();
    } finally {
      showHistorySpinner(false);
    }

    paintThread();
    syncComposer();
  }

  function refresh() {
    paintThread();
    syncComposer();
  }

  window.IndooneChatHome = { init, refresh, syncComposer, scrollToBottom };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => { void init(); }, { once: true });
  } else {
    void init();
  }
})();
