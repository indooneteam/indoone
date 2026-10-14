// The voice-first screen, the same idea as VibeScreen native-side: speak, the
// reply is spoken back, and the whole exchange lands in the chat thread.
(() => {
  const SpeechRecognition =
    window.SpeechRecognition || window.webkitSpeechRecognition;

  let screen = null;
  let orb = null;
  let status = null;
  let transcript = null;
  let recognition = null;
  let listening = false;

  function speak(text) {
    if (!('speechSynthesis' in window) || !text) {
      return;
    }

    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = navigator.language || 'en-IN';
    window.speechSynthesis.speak(utterance);
  }

  function setStatus(text) {
    if (status) {
      status.textContent = text;
    }
  }

  function setTranscript(text) {
    if (transcript) {
      transcript.textContent = text;
    }
  }

  function paint() {
    orb?.classList.toggle('listening', listening);
  }

  async function ask(spoken) {
    const store = window.IndooneChatStore;

    if (!store || !spoken) {
      return;
    }

    store.add({ text: spoken, mine: true });
    setStatus('Thinking…');
    setTranscript(spoken);

    try {
      const result = await window.IndooneChatApi.send({
        message: spoken,
        conversationId: store.getConversationId()
      });

      store.setConversationId(result.conversationId);
      store.add({ text: result.reply, mine: false });
      setStatus('Tap the orb and speak.');
      setTranscript(result.reply);
      speak(result.reply);
    } catch (error) {
      console.error('Indoone vibe failed:', error);
      setStatus('Could not reach Indoone AI.');
      setTranscript(error.message || '');
    }
  }

  function listen() {
    if (!SpeechRecognition) {
      window.toast?.('This browser cannot take voice input.');
      return;
    }

    if (listening) {
      return;
    }

    recognition = new SpeechRecognition();
    recognition.lang = navigator.language || 'en-IN';
    recognition.continuous = false;
    recognition.interimResults = true;

    recognition.onresult = event => {
      let spoken = '';

      for (let i = event.resultIndex; i < event.results.length; i += 1) {
        spoken += event.results[i][0].transcript;
      }

      setTranscript(spoken.trim());
    };

    recognition.onerror = () => {
      listening = false;
      paint();
      setStatus('Tap the orb and speak.');
    };

    recognition.onend = () => {
      listening = false;
      paint();

      const heard = (transcript?.textContent || '').trim();

      if (heard) {
        void ask(heard);
      }
    };

    listening = true;
    paint();
    setStatus('Listening…');
    recognition.start();
  }

  function open() {
    screen?.removeAttribute('hidden');
  }

  function close() {
    screen?.setAttribute('hidden', '');
    recognition?.stop?.();
    window.speechSynthesis?.cancel?.();
  }

  function init() {
    screen = document.getElementById('vibeScreen');
    orb = document.getElementById('vibeOrb');
    status = document.getElementById('vibeStatus');
    transcript = document.getElementById('vibeTranscript');

    orb?.addEventListener('click', listen);
    document
      .getElementById('vibeClose')
      ?.addEventListener('click', close);
  }

  window.IndooneVibe = { init, open, close };
})();
