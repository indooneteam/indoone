// The Vibe screen, wired the way VibeScreen.kt wires it.
//
// The microphone control is not a separate switch in the app: it shows the
// listening state. Listening means it reads "Mute" and is lit; not listening
// means "Unmute" and it is dim. The speaker control is its own switch.
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

  // The microphone button follows the listening state, as the app's does.
  function paintMute() {
    if (!mute) {
      return;
    }

    const icon = document.getElementById('vibeMuteIcon');
    const label = document.getElementById('vibeMuteLabel');

    mute.classList.toggle('on', listening);

    if (label) {
      label.textContent = listening ? 'Mute' : 'Unmute';
    }

    if (icon) {
      icon.dataset.appIcon = listening ? 'mic' : 'micOff';
      icon.dataset.appIconDone = 'false';
      window.IndooneIcons?.apply?.(mute);
    }
  }

  function paintSpeaker() {
    if (!speaker) {
      return;
    }

    const icon = document.getElementById('vibeSpeakerIcon');
    const label = document.getElementById('vibeSpeakerLabel');

    speaker.classList.toggle('on', speakerOn);

    if (label) {
      label.textContent = speakerOn ? 'Speaker' : 'Muted';
    }

    if (icon) {
      icon.dataset.appIcon = speakerOn ? 'volumeUp' : 'volumeOff';
      icon.dataset.appIconDone = 'false';
      window.IndooneIcons?.apply?.(speaker);
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

    try {
      const result = await window.IndooneChatApi.send({
        message: spoken,
        conversationId: store.getConversationId()
      });

      store.setConversationId(result.conversationId);
      store.add({ text: result.reply, mine: false });
      setTranscript(result.reply);
      setState(listening ? 'Listening' : 'Paused');
      speak(result.reply);
    } catch (error) {
      console.error('Indoone vibe failed:', error);
      setState('Paused');
      setTranscript(error.message || 'Could not reach Indoone AI.');
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
      paintMute();
      setState('Paused');
    };

    recognition.onend = () => {
      listening = false;
      paintMute();
      setState('Paused');

      const said = String(heard || '').trim();

      if (said) {
        void ask(said);
      }
    };

    listening = true;
    paintMute();
    setState('Listening');
    setHint('Speak naturally');
    recognition.start();
  }

  function stop() {
    listening = false;
    paintMute();
    recognition?.stop?.();
    window.speechSynthesis?.cancel?.();
  }

  function open() {
    screen?.removeAttribute('hidden');
    setTranscript('');
    setState('Paused');
    setHint('Speak naturally');
    paintMute();
    paintSpeaker();
    listen();
  }

  function close() {
    stop();
    screen?.setAttribute('hidden', '');
    setState('Paused');
  }

  function init() {
    screen = document.getElementById('vibeScreen');
    orb = document.getElementById('vibeOrb');
    state = document.getElementById('vibeState');
    hint = document.getElementById('vibeHint');
    transcript = document.getElementById('vibeTranscript');
    mute = document.getElementById('vibeMute');
    speaker = document.getElementById('vibeSpeaker');

    // Tapping the orb starts or stops listening, as the app's mic button does.
    orb?.addEventListener('click', () => {
      if (listening) {
        stop();
        setState('Paused');
      } else {
        listen();
      }
    });

    document.getElementById('vibeEnd')?.addEventListener('click', close);

    // In the app this is the microphone switch, and its look follows the
    // listening state, so tapping it simply turns listening on or off.
    mute?.addEventListener('click', () => {
      if (listening) {
        stop();
        setState('Paused');
      } else {
        listen();
      }
    });

    speaker?.addEventListener('click', () => {
      speakerOn = !speakerOn;
      paintSpeaker();

      if (!speakerOn) {
        window.speechSynthesis?.cancel?.();
      }
    });
  }

  window.IndooneVibe = { init, open, close };
})();
