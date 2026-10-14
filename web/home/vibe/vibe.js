// The voice-first screen, laid out as VibeScreen.kt lays it out: the split
// title, the VIBE label, the state, Speak naturally, the live transcript, and
// the three controls.
(() => {
  const SpeechRecognition =
    window.SpeechRecognition || window.webkitSpeechRecognition;

  let screen = null;
  let orb = null;
  let state = null;
  let hint = null;
  let transcript = null;
  let mute = null;
  let speaker = null;
  let recognition = null;
  let listening = false;
  let speakerOn = true;
  let heard = '';

  function setState(text) {
    if (state) {
      state.textContent = text;
    }
  }

  function setHint(text) {
    if (hint) {
      hint.textContent = text;
    }
  }

  function setTranscript(text) {
    heard = text;

    if (transcript) {
      transcript.textContent = text;
    }
  }

  function speak(text) {
    if (!speakerOn || !('speechSynthesis' in window) || !text) {
      return;
    }

    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = navigator.language || 'en-IN';
    window.speechSynthesis.speak(utterance);
  }

  async function ask(spoken) {
    const store = window.IndooneChatStore;

    if (!store || !spoken) {
      return;
    }

    store.add({ text: spoken, mine: true });
    setState('Thinking');
    setHint('One moment…');

    try {
      const result = await window.IndooneChatApi.send({
        message: spoken,
        conversationId: store.getConversationId()
      });

      store.setConversationId(result.conversationId);
      store.add({ text: result.reply, mine: false });
      setTranscript(result.reply);
      setState(listening ? 'Listening' : 'Paused');
      setHint('Speak naturally');
      speak(result.reply);
    } catch (error) {
      console.error('Indoone vibe failed:', error);
      setState('Paused');
      setHint('Could not reach Indoone AI.');
      setTranscript(error.message || '');
    }
  }

  function listen() {
    if (!SpeechRecognition) {
      setHint('This browser cannot take voice input.');
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
      screen?.classList.remove('listening');
      setState('Paused');
      setHint('Speak naturally');
    };

    recognition.onend = () => {
      listening = false;
      screen?.classList.remove('listening');
      setState('Paused');

      const said = String(heard || '').trim();

      if (said) {
        void ask(said);
      }
    };

    listening = true;
    screen?.classList.add('listening');
    setState('Listening');
    setHint('Speak naturally');
    recognition.start();
  }

  function stop() {
    listening = false;
    screen?.classList.remove('listening');
    recognition?.stop?.();
    window.speechSynthesis?.cancel?.();
  }

  function open() {
    screen?.removeAttribute('hidden');
    setTranscript('');
    setState('Paused');
    setHint('Tap the orb and speak.');
    listen();
  }

  function close() {
    stop();
    screen?.setAttribute('hidden', '');
  }

  function init() {
    screen = document.getElementById('vibeScreen');
    orb = document.getElementById('vibeOrb');
    state = document.getElementById('vibeState');
    hint = document.getElementById('vibeHint');
    transcript = document.getElementById('vibeTranscript');
    mute = document.getElementById('vibeMute');
    speaker = document.getElementById('vibeSpeaker');

    orb?.addEventListener('click', () => {
      if (listening) {
        stop();
        setState('Paused');
      } else {
        listen();
      }
    });

    document.getElementById('vibeEnd')?.addEventListener('click', close);

    // The app flips both the icon and the word.
    mute?.addEventListener('click', () => {
      const nowMuted = mute.classList.toggle('on');
      const icon = document.getElementById('vibeMuteIcon');
      const label = document.getElementById('vibeMuteLabel');

      if (label) {
        label.textContent = nowMuted ? 'Unmute' : 'Mute';
      }

      if (icon) {
        icon.dataset.appIcon = nowMuted ? 'micOff' : 'mic';
        icon.dataset.appIconDone = 'false';
        window.IndooneIcons?.apply?.(mute);
      }

      recognition?.stop?.();
    });

    speaker?.addEventListener('click', () => {
      speakerOn = !speakerOn;
      speaker.classList.toggle('on', speakerOn);

      const icon = document.getElementById('vibeSpeakerIcon');
      const label = document.getElementById('vibeSpeakerLabel');

      if (label) {
        label.textContent = speakerOn ? 'Speaker' : 'Muted';
      }

      if (icon) {
        icon.dataset.appIcon = speakerOn ? 'volumeUp' : 'volumeOff';
        icon.dataset.appIconDone = 'false';
        window.IndooneIcons?.apply?.(speaker);
      }

      if (!speakerOn) {
        window.speechSynthesis?.cancel?.();
      }
    });
  }

  window.IndooneVibe = { init, open, close };
})();
