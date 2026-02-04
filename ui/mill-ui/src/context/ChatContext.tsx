import { createContext, useContext, useReducer, useCallback, useEffect, type ReactNode } from 'react';
import type { Conversation, Message, ChatState } from '../types/chat';
import { chatService } from '../services/api';

const STORAGE_KEY = 'chat-conversations';

function generateId(): string {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 11)}`;
}

function generateTitle(message: string): string {
  const maxLength = 30;
  const cleaned = message.trim().replace(/\n/g, ' ');
  if (cleaned.length <= maxLength) return cleaned;
  return cleaned.slice(0, maxLength) + '...';
}

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
      return {
        ...state,
        activeConversationId: action.payload,
      };

    case 'ADD_MESSAGE': {
      const { conversationId, message } = action.payload;
      return {
        ...state,
        conversations: state.conversations.map((conv) => {
          if (conv.id !== conversationId) return conv;
          const updatedConv = {
            ...conv,
            messages: [...conv.messages, message],
            updatedAt: Date.now(),
          };
          // Update title from first user message if it's the default
          if (message.role === 'user' && conv.title === 'New Chat') {
            updatedConv.title = generateTitle(message.content);
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
            messages: conv.messages.map((msg) =>
              msg.id === messageId ? { ...msg, content } : msg
            ),
            updatedAt: Date.now(),
          };
        }),
      };
    }

    case 'SET_LOADING':
      return {
        ...state,
        isLoading: action.payload,
      };

    case 'CLEAR_ALL':
      return {
        conversations: [],
        activeConversationId: null,
        isLoading: false,
      };

    default:
      return state;
  }
}

interface ChatContextValue {
  state: ChatState;
  activeConversation: Conversation | null;
  createConversation: () => void;
  deleteConversation: (id: string) => void;
  setActiveConversation: (id: string) => void;
  sendMessage: (content: string) => Promise<void>;
  clearAllConversations: () => void;
}

const ChatContext = createContext<ChatContextValue | null>(null);

const initialState: ChatState = {
  conversations: [],
  activeConversationId: null,
  isLoading: false,
};

export function ChatProvider({ children }: { children: ReactNode }) {
  const [state, dispatch] = useReducer(chatReducer, initialState);

  // Load conversations from localStorage on mount
  useEffect(() => {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored) {
        const conversations = JSON.parse(stored) as Conversation[];
        dispatch({ type: 'LOAD_CONVERSATIONS', payload: conversations });
      }
    } catch (e) {
      console.error('Failed to load conversations from localStorage:', e);
    }
  }, []);

  // Save conversations to localStorage when they change
  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(state.conversations));
    } catch (e) {
      console.error('Failed to save conversations to localStorage:', e);
    }
  }, [state.conversations]);

  const activeConversation = state.conversations.find(
    (c) => c.id === state.activeConversationId
  ) || null;

  const createConversation = useCallback(() => {
    const newConversation: Conversation = {
      id: generateId(),
      title: 'New Chat',
      createdAt: Date.now(),
      updatedAt: Date.now(),
      messages: [],
    };
    dispatch({ type: 'CREATE_CONVERSATION', payload: newConversation });
  }, []);

  const deleteConversation = useCallback((id: string) => {
    dispatch({ type: 'DELETE_CONVERSATION', payload: id });
  }, []);

  const setActiveConversation = useCallback((id: string) => {
    dispatch({ type: 'SET_ACTIVE_CONVERSATION', payload: id });
  }, []);

  const sendMessage = useCallback(async (content: string) => {
    if (!state.activeConversationId || state.isLoading) return;

    const conversationId = state.activeConversationId;

    // Add user message
    const userMessage: Message = {
      id: generateId(),
      conversationId,
      role: 'user',
      content,
      timestamp: Date.now(),
    };
    dispatch({ type: 'ADD_MESSAGE', payload: { conversationId, message: userMessage } });

    // Create assistant message placeholder
    const assistantMessage: Message = {
      id: generateId(),
      conversationId,
      role: 'assistant',
      content: '',
      timestamp: Date.now(),
    };
    dispatch({ type: 'ADD_MESSAGE', payload: { conversationId, message: assistantMessage } });
    dispatch({ type: 'SET_LOADING', payload: true });

    try {
      // Stream the response
      let fullContent = '';
      for await (const chunk of chatService.sendMessage(conversationId, content)) {
        fullContent += chunk;
        dispatch({
          type: 'UPDATE_MESSAGE',
          payload: { conversationId, messageId: assistantMessage.id, content: fullContent },
        });
      }
    } catch (error) {
      console.error('Failed to get response:', error);
      dispatch({
        type: 'UPDATE_MESSAGE',
        payload: {
          conversationId,
          messageId: assistantMessage.id,
          content: 'Sorry, I encountered an error. Please try again.',
        },
      });
    } finally {
      dispatch({ type: 'SET_LOADING', payload: false });
    }
  }, [state.activeConversationId, state.isLoading]);

  const clearAllConversations = useCallback(() => {
    dispatch({ type: 'CLEAR_ALL' });
  }, []);

  const value: ChatContextValue = {
    state,
    activeConversation,
    createConversation,
    deleteConversation,
    setActiveConversation,
    sendMessage,
    clearAllConversations,
  };

  return <ChatContext.Provider value={value}>{children}</ChatContext.Provider>;
}

export function useChat() {
  const context = useContext(ChatContext);
  if (!context) {
    throw new Error('useChat must be used within a ChatProvider');
  }
  return context;
}
