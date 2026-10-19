window.initMenuChatHistory = function () {
  const list = document.getElementById('menuChatHistoryList');
  const status = document.getElementById('menuChatHistoryStatus');

  function setStatus(text) {
    if (status) {
      status.textContent = text;
    }
  }

  function row(chat) {
    const wrap = document.createElement('div');
    wrap.className = 'chat-history-row';

    const text = document.createElement('span');
    const title = document.createElement('b');
    // ChatHistoryMenuScreen.kt shows the conversation's own title.
    title.textContent = chat.title || 'Conversation';
    const when = document.createElement('small');
    when.textContent = chat.savedAt
      ? new Date(Number(chat.savedAt)).toLocaleString()
      : chat.id;
    text.appendChild(title);
    text.appendChild(when);

    const actions = document.createElement('span');
    actions.className = 'chat-history-actions';

    const open = document.createElement('button');
    open.type = 'button';
    open.textContent = 'Open';
    open.addEventListener('click', async () => {
      window.closeModal?.();

      try {
        const conversation = await window.IndooneCloudChat?.loadConversation?.(
          chat.id
        );

        if (conversation) {
          window.IndooneChatStore?.setConversationId?.(chat.id);
          window.IndooneChatStore?.replace?.(
            conversation.messages.map(item => ({
              text: item.content,
              mine: item.role === 'user'
            }))
          );
        }
      } catch (error) {
        console.warn('Indoone chat could not be opened:', error);
      }

      window.showHome?.();
    });

    const remove = document.createElement('button');
    remove.type = 'button';
    remove.className = 'danger';
    remove.textContent = 'Delete';
    remove.addEventListener('click', async () => {
      try {
        await window.IndooneUserData.deleteChat(chat.id);
        void load();
      } catch (error) {
        setStatus(error?.message || 'Chat could not be deleted.');
      }
    });

    actions.appendChild(open);
    actions.appendChild(remove);
    wrap.appendChild(text);
    wrap.appendChild(actions);
    return wrap;
  }

  async function load() {
    setStatus('Loading chats…');

    try {
      const chats = await window.IndooneUserData.listChats();

      if (list) {
        list.textContent = '';

        if (!chats.length) {
          const empty = document.createElement('p');
          empty.className = 'menu-feature-note';
          empty.textContent = 'No saved chats yet.';
          list.appendChild(empty);
        } else {
          chats.forEach(chat => list.appendChild(row(chat)));
        }
      }

      setStatus('');
    } catch (error) {
      setStatus(error?.message || 'Chat history could not be loaded right now.');
    }
  }

  void load();
};
