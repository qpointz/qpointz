import {
  createContext,
  useContext,
  useReducer,
  useCallback,
  useRef,
  useEffect,
  type ReactNode,
} from 'react';
import type { Message } from '../types/chat';
import type {
  InlineChatSession,
  InlineChatState,
  InlineChatAction,
  InlineChatContextType,
} from '../types/inlineChat';
import { chatService } from '../services/api';
import { useFeatureFlags } from '../features/FeatureFlagContext';

function generateId(): string {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 11)}`;
}

function inlineChatReducer(
  state: InlineChatState,
  action: InlineChatAction,
): InlineChatState {
  switch (action.type) {
    case 'START_SESSION':
      return {
        ...state,
        sessions: [...state.sessions, action.payload],
        activeSessionId: action.payload.id,
        isDrawerOpen: true,
      };

    case 'CLOSE_SESSION': {
      const remaining = state.sessions.filter((s) => s.id !== action.payload);
      let newActiveId = state.activeSessionId;
      if (state.activeSessionId === action.payload) {
        newActiveId = remaining.length > 0 ? (remaining[remaining.length - 1]?.id ?? null) : null;
      }
      return {
        ...state,
        sessions: remaining,
        activeSessionId: newActiveId,
        // Auto-close drawer when no sessions left
        isDrawerOpen: remaining.length > 0 ? state.isDrawerOpen : false,
      };
    }

    case 'SET_ACTIVE_SESSION':
      return {
        ...state,
        activeSessionId: action.payload,
      };

    case 'SET_SESSION_CHAT_ID': {
      const { sessionId, chatId } = action.payload;
      return {
        ...state,
        sessions: state.sessions.map((s) =>
          s.id !== sessionId ? s : { ...s, chatId },
        ),
      };
    }

    case 'ADD_MESSAGE': {
      const { sessionId, message } = action.payload;
      return {
        ...state,
        sessions: state.sessions.map((s) =>
          s.id !== sessionId ? s : { ...s, messages: [...s.messages, message] },
        ),
      };
    }

    case 'UPDATE_MESSAGE': {
      const { sessionId, messageId, content } = action.payload;
      return {
        ...state,
        sessions: state.sessions.map((s) =>
          s.id !== sessionId
            ? s
            : {
                ...s,
                messages: s.messages.map((m) =>
                  m.id === messageId ? { ...m, content } : m,
                ),
              },
        ),
      };
    }

    case 'SET_LOADING': {
      const { sessionId, isLoading } = action.payload;
      return {
        ...state,
        sessions: state.sessions.map((s) =>
          s.id !== sessionId ? s : { ...s, isLoading },
        ),
      };
    }

    case 'CLOSE_ALL_SESSIONS':
      return {
        ...state,
        sessions: [],
        activeSessionId: null,
        isDrawerOpen: false,
      };

    case 'OPEN_DRAWER':
      return { ...state, isDrawerOpen: true };

    case 'CLOSE_DRAWER':
      return { ...state, isDrawerOpen: false };

    default:
      return state;
  }
}

export type InlineChatMessageListener = (content: string) => void;

interface InlineChatContextValue {
  state: InlineChatState;
  activeSession: InlineChatSession | null;
  startSession: (
    contextType: InlineChatContextType,
    contextId: string,
    contextLabel: string,
    contextEntityType?: string,
  ) => void;
  closeSession: (sessionId: string) => void;
  setActiveSession: (sessionId: string) => void;
  sendMessage: (sessionId: string, content: string) => Promise<void>;
  openDrawer: () => void;
  closeDrawer: () => void;
  closeAllSessions: () => void;
  getSessionByContextId: (contextId: string) => InlineChatSession | undefined;
  hasAnySessions: () => boolean;
  registerListener: (contextId: string, callback: InlineChatMessageListener) => void;
  unregisterListener: (contextId: string, callback: InlineChatMessageListener) => void;
}

const InlineChatContext = createContext<InlineChatContextValue | null>(null);

const initialState: InlineChatState = {
  sessions: [],
  activeSessionId: null,
  isDrawerOpen: false,
};

export function InlineChatProvider({ children }: { children: ReactNode }) {
  const [state, dispatch] = useReducer(inlineChatReducer, initialState);
  const flags = useFeatureFlags();

  // Listener registry: contextId -> Set of callbacks
  const listenersRef = useRef<Map<string, Set<InlineChatMessageListener>>>(new Map());

  const registerListener = useCallback(
    (contextId: string, callback: InlineChatMessageListener) => {
      const map = listenersRef.current;
      if (!map.has(contextId)) {
        map.set(contextId, new Set());
      }
      map.get(contextId)!.add(callback);
    },
    [],
  );

  const unregisterListener = useCallback(
    (contextId: string, callback: InlineChatMessageListener) => {
      const set = listenersRef.current.get(contextId);
      if (set) {
        set.delete(callback);
        if (set.size === 0) {
          listenersRef.current.delete(contextId);
        }
      }
    },
    [],
  );

  const activeSession =
    state.sessions.find((s) => s.id === state.activeSessionId) ?? null;

  const getSessionByContextId = useCallback(
    (contextId: string) => state.sessions.find((s) => s.contextId === contextId),
    [state.sessions],
  );

  const hasAnySessions = useCallback(
    () => state.sessions.length > 0,
    [state.sessions.length],
  );

  const startSession = useCallback(
    (
      contextType: InlineChatContextType,
      contextId: string,
      contextLabel: string,
      contextEntityType?: string,
    ) => {
      // Block if inline chat is globally disabled
      if (!flags.inlineChatEnabled) return;

      // Block if the specific context type is disabled
      if (contextType === 'model') {
        if (!flags.inlineChatModelContext) return;
        // Check entity-type sub-flags
        if (contextEntityType === 'SCHEMA' && !flags.inlineChatModelSchema) return;
        if (contextEntityType === 'TABLE' && !flags.inlineChatModelTable) return;
        if (contextEntityType === 'ATTRIBUTE' && !flags.inlineChatModelColumn) return;
      }
      if (contextType === 'knowledge' && !flags.inlineChatKnowledgeContext) return;
      if (contextType === 'analysis' && !flags.inlineChatAnalysisContext) return;

      // If a session for this context already exists, just activate it
      const existing = state.sessions.find((s) => s.contextId === contextId);
      if (existing) {
        dispatch({ type: 'SET_ACTIVE_SESSION', payload: existing.id });
        dispatch({ type: 'OPEN_DRAWER' });
        return;
      }

      // If multi-session is disabled and there's already a session, close it first
      if (!flags.inlineChatMultiSession && state.sessions.length > 0) {
        dispatch({ type: 'CLOSE_ALL_SESSIONS' });
      }

      const sessionId = generateId();

      // Optionally create the greeting message
      const messages: Message[] = [];
      if (flags.inlineChatGreeting) {
        messages.push({
          id: generateId(),
          conversationId: sessionId,
          role: 'assistant',
          content: `I'm here to help with **${contextLabel}**. What would you like to know?`,
          timestamp: Date.now(),
        });
      }

      const session: InlineChatSession = {
        id: sessionId,
        chatId: null,
        contextType,
        contextId,
        contextLabel,
        contextEntityType,
        messages,
        isLoading: false,
        createdAt: Date.now(),
      };

      dispatch({ type: 'START_SESSION', payload: session });
    },
    [state.sessions, flags],
  );

  const closeSession = useCallback((sessionId: string) => {
    dispatch({ type: 'CLOSE_SESSION', payload: sessionId });
  }, []);

  const setActiveSession = useCallback((sessionId: string) => {
    dispatch({ type: 'SET_ACTIVE_SESSION', payload: sessionId });
  }, []);

  const openDrawer = useCallback(() => {
    dispatch({ type: 'OPEN_DRAWER' });
  }, []);

  const closeDrawer = useCallback(() => {
    dispatch({ type: 'CLOSE_DRAWER' });
  }, []);

  const closeAllSessions = useCallback(() => {
    dispatch({ type: 'CLOSE_ALL_SESSIONS' });
  }, []);

  const sendMessage = useCallback(
    async (sessionId: string, content: string) => {
      const session = state.sessions.find((s) => s.id === sessionId);
      if (!session || session.isLoading) return;

      // Add user message
      const userMessage: Message = {
        id: generateId(),
        conversationId: sessionId,
        role: 'user',
        content,
        timestamp: Date.now(),
      };
      dispatch({ type: 'ADD_MESSAGE', payload: { sessionId, message: userMessage } });

      // Create assistant placeholder
      const assistantMessage: Message = {
        id: generateId(),
        conversationId: sessionId,
        role: 'assistant',
        content: '',
        timestamp: Date.now(),
      };
      dispatch({ type: 'ADD_MESSAGE', payload: { sessionId, message: assistantMessage } });
      dispatch({ type: 'SET_LOADING', payload: { sessionId, isLoading: true } });

      try {
        // Ensure we have a backend chatId -- create on first message
        let chatId = session.chatId;
        if (!chatId) {
          const result = await chatService.createChat({
            contextType: session.contextType,
            contextId: session.contextId,
            contextLabel: session.contextLabel,
            contextEntityType: session.contextEntityType,
          });
          chatId = result.chatId;
          dispatch({ type: 'SET_SESSION_CHAT_ID', payload: { sessionId, chatId } });
        }

        let fullContent = '';
        for await (const chunk of chatService.sendMessage(chatId, content)) {
          fullContent += chunk;
          dispatch({
            type: 'UPDATE_MESSAGE',
            payload: { sessionId, messageId: assistantMessage.id, content: fullContent },
          });
        }

        // Notify registered listeners for this context
        const callbacks = listenersRef.current.get(session.contextId);
        if (callbacks) {
          callbacks.forEach((cb) => {
            try { cb(fullContent); } catch { /* listener errors shouldn't break chat */ }
          });
        }
      } catch (error) {
        console.error('Inline chat error:', error);
        dispatch({
          type: 'UPDATE_MESSAGE',
          payload: {
            sessionId,
            messageId: assistantMessage.id,
            content: 'Sorry, I encountered an error. Please try again.',
          },
        });
      } finally {
        dispatch({ type: 'SET_LOADING', payload: { sessionId, isLoading: false } });
      }
    },
    [state.sessions],
  );

  const value: InlineChatContextValue = {
    state,
    activeSession,
    startSession,
    closeSession,
    setActiveSession,
    sendMessage,
    openDrawer,
    closeDrawer,
    closeAllSessions,
    getSessionByContextId,
    hasAnySessions,
    registerListener,
    unregisterListener,
  };

  return (
    <InlineChatContext.Provider value={value}>
      {children}
    </InlineChatContext.Provider>
  );
}

export function useInlineChat() {
  const context = useContext(InlineChatContext);
  if (!context) {
    throw new Error('useInlineChat must be used within an InlineChatProvider');
  }
  return context;
}

/**
 * Subscribe to completed assistant messages for a specific context.
 * The callback fires once per completed AI response with the full content.
 * Automatically registers on mount and unregisters on unmount.
 */
export function useInlineChatListener(
  contextId: string | null | undefined,
  onAssistantMessage: InlineChatMessageListener,
) {
  const { registerListener, unregisterListener } = useInlineChat();

  // Keep a stable ref to the latest callback so the effect doesn't re-run on every render
  const callbackRef = useRef(onAssistantMessage);
  callbackRef.current = onAssistantMessage;

  useEffect(() => {
    if (!contextId) return;
    const handler: InlineChatMessageListener = (content) => callbackRef.current(content);
    registerListener(contextId, handler);
    return () => unregisterListener(contextId, handler);
  }, [contextId, registerListener, unregisterListener]);
}
