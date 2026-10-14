// The conversation, kept in memory and mirrored to localStorage so a reload
// does not lose the thread. Mirrors what CloudChatRepository keeps native-side.
(() => {
  const KEY = 'indoone_chat_thread_v1';
  const MAX_MESSAGES = 200;

  let conversationId = null;
  let messages = [];

  function load() {
    try {
      const raw = localStorage.getItem(KEY);

      if (!raw) {
        return;
      }

      const saved = JSON.parse(raw);

      conversationId = saved.conversationId || null;
      messages = Array.isArray(saved.messages) ? saved.messages : [];
    } catch (error) {
      console.warn('Indoone chat thread could not be read:', error);
    }
  }

  function persist() {
    try {
      localStorage.setItem(
        KEY,
        JSON.stringify({ conversationId, messages })
      );
    } catch (error) {
      console.warn('Indoone chat thread could not be saved:', error);
    }
  }

  function add(message) {
    messages.push(message);

    if (messages.length > MAX_MESSAGES) {
      messages = messages.slice(-MAX_MESSAGES);
    }

    persist();
    return message;
  }

  function remove(message) {
    const index = messages.indexOf(message);

    if (index >= 0) {
      messages.splice(index, 1);
      persist();
    }
  }

  function clear() {
    conversationId = null;
    messages = [];
    persist();
  }

  window.IndooneChatStore = {
    load,
    add,
    remove,
    clear,
    persist,
    getMessages: () => messages,
    getConversationId: () => conversationId,
    setConversationId: id => {
      conversationId = id;
      persist();
    }
  };
})();
