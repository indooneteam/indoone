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

  // VibeSessionService.kt starts here, and VibeScreen.kt shows the connection
  // state until it is Connected.
  const OPENING = 'Connecting';
  const PLACEHOLDER = 'Your live conversation will appear here.';
  let connected = false;
  // Closing has to end the session for good: no late result, and no reply
  // spoken after the screen is gone.
  let closed = false;
  // The app keeps its microphone open for the whole session: it does not stop
  // after one sentence. This is what "listening" means here.
  let wantListening = false;

  // VibeScreen.kt shows the connection state until Connected, then Listening
  // or Paused.
  function setState(text) {
    if (!state) {
      return;
    }

    state.textContent = connected ? text : OPENING;
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
    const api = window.IndooneChatApi;

    if (!store || !spoken) {
      return;
    }

    // The app shows a plain line, never a raw failure.
    if (!api) {
      setState('Paused');
      setTranscript('Indoone AI is not reachable right now.');
      return;
    }

    store.add({ text: spoken, mine: true });
    setState('Thinking');

    try {
      const result = await api.send({
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
      // Show the real reason. The API says whether the server refused the
      // call or returned a status; flattening that into "could not reach"
      // is what made this impossible to diagnose.
      setTranscript(
        error?.message || 'Could not reach Indoone AI.'
      );
    }
  }

  function listen() {
    if (!SpeechRecognition) {
      setHint('This browser cannot take voice input.');
      return;
    }

    wantListening = true;

    if (listening) {
      return;
    }

    recognition = new SpeechRecognition();
    recognition.lang = navigator.language || 'en-IN';
    // Keep the microphone open, as the app's session does. Without this it
    // stopped after every sentence and had to be unmuted again.
    recognition.continuous = true;
    recognition.interimResults = true;

    recognition.onresult = event => {
      let interim = '';
      let settled = '';

      for (let i = event.resultIndex; i < event.results.length; i += 1) {
        const result = event.results[i];
        const text = result[0].transcript;

        if (result.isFinal) {
          settled += text;
        } else {
          interim += text;
        }
      }

      if (settled) {
        // The browser has decided this sentence is finished, so this is the
        // moment to answer. Waiting for the session to end would never come:
        // the microphone stays open for the whole screen.
        heard = '';
        setTranscript(settled.trim());
        void ask(settled.trim());
        return;
      }

      heard = interim;
      setTranscript(interim.trim());
    };

    recognition.onerror = () => {
      listening = false;
      paintMute();

      if (!closed) {
        setState('Paused');
      }
    };

    recognition.onend = () => {
      listening = false;
      paintMute();

      if (closed || !wantListening) {
        setState('Paused');
        return;
      }

      const said = String(heard || '').trim();

      if (said) {
        heard = '';
        void ask(said);
      }

      // The browser ends the session on its own now and then. The app's
      // microphone stays open, so open it again.
      setTimeout(() => {
        if (!closed && wantListening && !listening) {
          listen();
        }
      }, 250);
    };

    connected = true;
    listening = true;
    paintMute();
    setState('Listening');
    setHint('Speak naturally');

    try {
      recognition.start();
    } catch (error) {
      // Desktop Chrome can refuse: no microphone, or no permission. Saying
      // Mute and sitting there would be a lie.
      console.error('Indoone vibe could not start the microphone:', error);
      connected = false;
      listening = false;
      paintMute();
      setState('Paused');
      setHint('This browser would not give the microphone.');
    }
  }

  function stop() {
    wantListening = false;
    listening = false;
    paintMute();

    // abort, not stop: stop still delivers whatever was heard, and that is
    // what kept answering after the screen was closed.
    recognition?.abort?.();
    recognition = null;
    window.speechSynthesis?.cancel?.();
  }

  function open() {
    closed = false;
    screen?.removeAttribute('hidden');
    connected = false;
    setTranscript(PLACEHOLDER);
    setState(OPENING);
    setHint('Speak naturally');
    paintMute();
    paintSpeaker();
    listen();
  }

  function close() {
    closed = true;
    wantListening = false;
    heard = '';
    stop();
    screen?.setAttribute('hidden', '');
    connected = false;
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
      if (listening || wantListening) {
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
      if (listening || wantListening) {
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
