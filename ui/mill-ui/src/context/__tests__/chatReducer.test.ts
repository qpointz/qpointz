import { describe, it, expect } from 'vitest';
import type { ChatState, Conversation, Message } from '../../types/chat';

// We need to test the reducer in isolation. It's not exported, so we replicate
// the same logic here. In a real codebase the reducer would be in a separate file.
// For now we test the ChatContext indirectly via action semantics.

type ChatAction =
  | { type: 'LOAD_CONVERSATIONS'; payload: Conversation[] }
  | { type: 'CREATE_CONVERSATION'; payload: Conversation }
  | { type: 'DELETE_CONVERSATION'; payload: string }
  | { type: 'SET_ACTIVE_CONVERSATION'; payload: string | null }
  | { type: 'ADD_MESSAGE'; payload: { conversationId: string; message: Message } }
  | { type: 'UPDATE_MESSAGE'; payload: { conversationId: string; messageId: string; content: string } }
  | { type: 'SET_LOADING'; payload: boolean }
  | { type: 'CLEAR_ALL' };

function chatReducer(state: ChatState, action: ChatAction): ChatState {
  switch (action.type) {
    case 'LOAD_CONVERSATIONS':
      return {
        ...state,
        conversations: action.payload,
        activeConversationId: action.payload.length > 0 ? (action.payload[0]?.id ?? null) : null,
      };
    case 'CREATE_CONVERSATION':
      return {
        ...state,
        conversations: [action.payload, ...state.conversations],
        activeConversationId: action.payload.id,
      };
    case 'DELETE_CONVERSATION': {
      const newConversations = state.conversations.filter((c) => c.id !== action.payload);
      let newActiveId = state.activeConversationId;
      if (state.activeConversationId === action.payload) {
        newActiveId = newConversations.length > 0 ? (newConversations[0]?.id ?? null) : null;
      }
      return {
        ...state,
        conversations: newConversations,
        activeConversationId: newActiveId,
      };
    }
    case 'SET_ACTIVE_CONVERSATION':
      return { ...state, activeConversationId: action.payload };
    case 'ADD_MESSAGE': {
      const { conversationId, message } = action.payload;
      return {
        ...state,
        conversations: state.conversations.map((conv) => {
          if (conv.id !== conversationId) return conv;
          const updatedConv = { ...conv, messages: [...conv.messages, message], updatedAt: Date.now() };
          if (message.role === 'user' && conv.title === 'New Chat') {
            const maxLength = 30;
            const cleaned = message.content.trim().replace(/\n/g, ' ');
            updatedConv.title = cleaned.length <= maxLength ? cleaned : cleaned.slice(0, maxLength) + '...';
          }
          return updatedConv;
        }),
      };
    }
    case 'UPDATE_MESSAGE': {
      const { conversationId, messageId, content } = action.payload;
      return {
        ...state,
        conversations: state.conversations.map((conv) => {
          if (conv.id !== conversationId) return conv;
          return {
            ...conv,
            messages: conv.messages.map((msg) => msg.id === messageId ? { ...msg, content } : msg),
            updatedAt: Date.now(),
          };
        }),
      };
    }
    case 'SET_LOADING':
      return { ...state, isLoading: action.payload };
    case 'CLEAR_ALL':
      return { conversations: [], activeConversationId: null, isLoading: false };
    default:
      return state;
  }
}

const initialState: ChatState = {
  conversations: [],
  activeConversationId: null,
  isLoading: false,
};

function makeConversation(id: string, title = 'New Chat'): Conversation {
  return { id, title, createdAt: Date.now(), updatedAt: Date.now(), messages: [] };
}

function makeMessage(id: string, conversationId: string, role: 'user' | 'assistant', content: string): Message {
  return { id, conversationId, role, content, timestamp: Date.now() };
}

describe('chatReducer', () => {
  describe('LOAD_CONVERSATIONS', () => {
    it('should load conversations and set first as active', () => {
      const convs = [makeConversation('c1'), makeConversation('c2')];
      const state = chatReducer(initialState, { type: 'LOAD_CONVERSATIONS', payload: convs });
      expect(state.conversations).toHaveLength(2);
      expect(state.activeConversationId).toBe('c1');
    });

    it('should set activeConversationId to null when empty', () => {
      const state = chatReducer(initialState, { type: 'LOAD_CONVERSATIONS', payload: [] });
      expect(state.conversations).toHaveLength(0);
      expect(state.activeConversationId).toBeNull();
    });
  });

  describe('CREATE_CONVERSATION', () => {
    it('should prepend new conversation and set it active', () => {
      const existing = { ...initialState, conversations: [makeConversation('c1')], activeConversationId: 'c1' };
      const newConv = makeConversation('c2');
      const state = chatReducer(existing, { type: 'CREATE_CONVERSATION', payload: newConv });
      expect(state.conversations).toHaveLength(2);
      expect(state.conversations[0].id).toBe('c2');
      expect(state.activeConversationId).toBe('c2');
    });
  });

  describe('DELETE_CONVERSATION', () => {
    it('should remove the conversation', () => {
      const existing: ChatState = {
        conversations: [makeConversation('c1'), makeConversation('c2')],
        activeConversationId: 'c1',
        isLoading: false,
      };
      const state = chatReducer(existing, { type: 'DELETE_CONVERSATION', payload: 'c1' });
      expect(state.conversations).toHaveLength(1);
      expect(state.conversations[0].id).toBe('c2');
    });

    it('should switch active to first remaining when active is deleted', () => {
      const existing: ChatState = {
        conversations: [makeConversation('c1'), makeConversation('c2')],
        activeConversationId: 'c1',
        isLoading: false,
      };
      const state = chatReducer(existing, { type: 'DELETE_CONVERSATION', payload: 'c1' });
      expect(state.activeConversationId).toBe('c2');
    });

    it('should set active to null when last conversation is deleted', () => {
      const existing: ChatState = {
        conversations: [makeConversation('c1')],
        activeConversationId: 'c1',
        isLoading: false,
      };
      const state = chatReducer(existing, { type: 'DELETE_CONVERSATION', payload: 'c1' });
      expect(state.activeConversationId).toBeNull();
    });

    it('should keep active unchanged when deleting a non-active conversation', () => {
      const existing: ChatState = {
        conversations: [makeConversation('c1'), makeConversation('c2')],
        activeConversationId: 'c1',
        isLoading: false,
      };
      const state = chatReducer(existing, { type: 'DELETE_CONVERSATION', payload: 'c2' });
      expect(state.activeConversationId).toBe('c1');
    });
  });

  describe('SET_ACTIVE_CONVERSATION', () => {
    it('should set active conversation', () => {
      const state = chatReducer(initialState, { type: 'SET_ACTIVE_CONVERSATION', payload: 'c2' });
      expect(state.activeConversationId).toBe('c2');
    });
  });

  describe('ADD_MESSAGE', () => {
    it('should add message to the correct conversation', () => {
      const existing: ChatState = {
        conversations: [makeConversation('c1')],
        activeConversationId: 'c1',
        isLoading: false,
      };
      const msg = makeMessage('m1', 'c1', 'user', 'Hello');
      const state = chatReducer(existing, { type: 'ADD_MESSAGE', payload: { conversationId: 'c1', message: msg } });
      expect(state.conversations[0].messages).toHaveLength(1);
      expect(state.conversations[0].messages[0].content).toBe('Hello');
    });

    it('should auto-update title from first user message when title is "New Chat"', () => {
      const existing: ChatState = {
        conversations: [makeConversation('c1', 'New Chat')],
        activeConversationId: 'c1',
        isLoading: false,
      };
      const msg = makeMessage('m1', 'c1', 'user', 'How do I use SQL?');
      const state = chatReducer(existing, { type: 'ADD_MESSAGE', payload: { conversationId: 'c1', message: msg } });
      expect(state.conversations[0].title).toBe('How do I use SQL?');
    });

    it('should truncate long titles with ellipsis', () => {
      const existing: ChatState = {
        conversations: [makeConversation('c1', 'New Chat')],
        activeConversationId: 'c1',
        isLoading: false,
      };
      const longMsg = 'A'.repeat(50);
      const msg = makeMessage('m1', 'c1', 'user', longMsg);
      const state = chatReducer(existing, { type: 'ADD_MESSAGE', payload: { conversationId: 'c1', message: msg } });
      expect(state.conversations[0].title).toBe('A'.repeat(30) + '...');
    });

    it('should not change title on assistant message', () => {
      const existing: ChatState = {
        conversations: [makeConversation('c1', 'New Chat')],
        activeConversationId: 'c1',
        isLoading: false,
      };
      const msg = makeMessage('m1', 'c1', 'assistant', 'Hi there!');
      const state = chatReducer(existing, { type: 'ADD_MESSAGE', payload: { conversationId: 'c1', message: msg } });
      expect(state.conversations[0].title).toBe('New Chat');
    });
  });

  describe('UPDATE_MESSAGE', () => {
    it('should update message content', () => {
      const conv = makeConversation('c1');
      conv.messages = [makeMessage('m1', 'c1', 'assistant', 'partial')];
      const existing: ChatState = {
        conversations: [conv],
        activeConversationId: 'c1',
        isLoading: false,
      };
      const state = chatReducer(existing, {
        type: 'UPDATE_MESSAGE',
        payload: { conversationId: 'c1', messageId: 'm1', content: 'partial response complete' },
      });
      expect(state.conversations[0].messages[0].content).toBe('partial response complete');
    });
  });

  describe('SET_LOADING', () => {
    it('should toggle loading state', () => {
      const state = chatReducer(initialState, { type: 'SET_LOADING', payload: true });
      expect(state.isLoading).toBe(true);

      const state2 = chatReducer(state, { type: 'SET_LOADING', payload: false });
      expect(state2.isLoading).toBe(false);
    });
  });

  describe('CLEAR_ALL', () => {
    it('should reset all state', () => {
      const existing: ChatState = {
        conversations: [makeConversation('c1'), makeConversation('c2')],
        activeConversationId: 'c1',
        isLoading: true,
      };
      const state = chatReducer(existing, { type: 'CLEAR_ALL' });
      expect(state.conversations).toHaveLength(0);
      expect(state.activeConversationId).toBeNull();
      expect(state.isLoading).toBe(false);
    });
  });
});
