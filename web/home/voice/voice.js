// Voice input through the Web Speech API. Native uses the platform speech
// recogniser; this is the browser's equivalent, and it degrades quietly where
// the API is missing.
(() => {
  const SpeechRecognition =
    window.SpeechRecognition || window.webkitSpeechRecognition;

  let recognition = null;
  let listening = false;
  let base = '';

  function available() {
    return Boolean(SpeechRecognition);
  }

  function button() {
    return document.getElementById('voiceBtn');
  }

  function paint() {
    button()?.classList.toggle('listening', listening);
  }

  function stop() {
    listening = false;
    paint();
    recognition?.stop?.();
  }

  function start() {
    if (!available()) {
      window.toast?.('This browser cannot take voice input.');
      return;
    }

    if (listening) {
      stop();
      return;
    }

    const input = document.getElementById('chatInput');
    base = input?.value || '';

    recognition = new SpeechRecognition();
    recognition.lang = navigator.language || 'en-IN';
    recognition.continuous = false;
    recognition.interimResults = true;

    recognition.onresult = event => {
      let spoken = '';

      for (let i = event.resultIndex; i < event.results.length; i += 1) {
        spoken += event.results[i][0].transcript;
      }

      if (input) {
        input.value = base ? `${base} ${spoken}`.trim() : spoken.trim();
      }
    };

    recognition.onerror = error => {
      console.warn('Indoone voice input failed:', error);
      listening = false;
      paint();
    };

    recognition.onend = () => {
      listening = false;
      paint();
    };

    listening = true;
    paint();
    recognition.start();
  }

  window.IndooneVoice = { start, stop, available };
})();
