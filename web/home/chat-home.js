// The chat home, the web counterpart of android/home/HomeScreen.kt. Loads the
// pieces beside it, wires the composer, and keeps the welcome block and the
// thread in step. The accounts list that used to live here now sits under
// menu/accounts, so its own controller (home.js) is left alone.
(() => {
  const MODULES = [
    'home/message/chat-api.js?v=20261014a',
    'home/message/chat-store.js?v=20261014a',
    'home/message/message.js?v=20261014a',
    'home/send/send.js?v=20261014a',
    'home/voice/voice.js?v=20261014a',
    'home/plus/plus.js?v=20261014a',
    'home/plus/camera/camera.js?v=20261014a',
    'home/plus/photos/photos.js?v=20261014a',
    'home/plus/files/files.js?v=20261014a',
    'home/vibe/vibe.js?v=20261014a'
  ];

  const WELCOME_LINES = [
    'Ask me anything.',
    'What can I help you with?'
  ];

  const QUICK_PROMPTS = [
    'Explain this',
    'Create image',
    'Help me plan',
    'More…'
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

  function buildQuickPrompts() {
    const host = document.getElementById('quickPrompts');

    if (!host || host.childElementCount) {
      return;
    }

    QUICK_PROMPTS.forEach(label => {
      const chip = document.createElement('button');
      chip.type = 'button';
      chip.className = 'quick-chip';
      chip.textContent = label;
      chip.addEventListener('click', () => {
        const input = document.getElementById('chatInput');

        if (input) {
          input.value = label;
          input.focus();
        }
      });
      host.appendChild(chip);
    });
  }

  function autoGrow(event) {
    const input = event.target;
    input.style.height = 'auto';
    input.style.height = `${Math.min(input.scrollHeight, 132)}px`;
  }

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
      return;
    }

    window.IndooneChatStore?.load();
    window.IndooneChatScroll = { toBottom: scrollToBottom };
    window.IndoonePlusSheet?.init(document.getElementById('plusSheet'));
    window.IndooneVibe?.init();

    buildQuickPrompts();
    paintWelcome();
    paintThread();

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
      ?.addEventListener('click', () => window.IndooneVoice?.start?.());

    document
      .getElementById('sendBtn')
      ?.addEventListener('click', () => window.IndooneSend?.send?.());

    const input = document.getElementById('chatInput');

    input?.addEventListener('input', autoGrow);
    input?.addEventListener('keydown', event => {
      if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault();
        void window.IndooneSend?.send?.();
      }
    });
  }

  function refresh() {
    paintThread();
  }

  window.IndooneChatHome = { init, refresh };
})();
