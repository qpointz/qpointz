import {
  createContext,
  useContext,
  useReducer,
  useCallback,
  useRef,
  useEffect,
  type ReactNode,
  type Dispatch,
} from 'react';
import type { ChatMessageArtifact, Message } from '../types/chat';
import type {
  InlineChatSession,
  InlineChatState,
  InlineChatAction,
  InlineChatContextType,
} from '../types/inlineChat';
import { chatService } from '../services/api';
import { resolveGeneralChatAgentProfileId } from '../features/chatPreferences';
import { useFeatureFlags } from '../features/FeatureFlagContext';
import { parseChatStructuredPart } from '../utils/chatArtifactParse';
import { parseWireArtifacts } from '../utils/artifactWireParse';
import { deriveAssistantReplyView } from '../utils/assistantReplyView';
import { StreamingReplySegmentTracker } from '../utils/streamingReplySegments';
import type { TurnResponseWire } from '../types/chatWire';

/**
 * Inline chats share `chatService` with General Chat. Structured artefacts (SQL / data / facet)
 * use the same streaming `onNonTextPartUpdated` path as {@link ChatContext}.
 */
function generateId(): string {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 11)}`;
}

function turnToInlineMessage(turn: TurnResponseWire, conversationId: string): Message {
  const role = turn.role === 'user' ? 'user' : 'assistant';
  const artifacts = parseWireArtifacts(turn.artifacts);
  const view =
    role === 'assistant'
      ? deriveAssistantReplyView(artifacts)
      : undefined;
  return {
    id: turn.turnId,
    conversationId,
    role,
    content: turn.text ?? '',
    timestamp: Date.parse(turn.createdAt),
    restReplay: true,
    ...(artifacts.length ? { artifacts } : {}),
    ...(view ? { assistantReplyView: view } : {}),
  };
}

async function hydrateSessionTranscript(
  dispatch: Dispatch<InlineChatAction>,
  sessionId: string,
  contextType: InlineChatContextType,
  contextId: string,
): Promise<void> {
  try {
    const chatId = await chatService.getChatByContext(contextType, contextId);
    if (!chatId) return;
    const detail = await chatService.getChatDetail(chatId);
    const messages = detail.messages.map((turn) => turnToInlineMessage(turn, chatId));
    if (!messages.length) return;
    dispatch({
      type: 'MERGE_SESSION_TRANSCRIPT',
      payload: { sessionId, chatId, messages },
    });
  } catch {
    /* tolerate missing contextual chat */
  }
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

    case 'SET_SESSION_THINKING': {
      const { sessionId, message } = action.payload;
      return {
        ...state,
        sessions: state.sessions.map((s) =>
          s.id !== sessionId ? s : { ...s, thinkingMessage: message },
        ),
      };
    }

    case 'APPEND_MESSAGE_ARTIFACT': {
      const { sessionId, messageId, artifact } = action.payload;
      return {
        ...state,
        sessions: state.sessions.map((s) => {
          if (s.id !== sessionId) return s;
          return {
            ...s,
            messages: s.messages.map((m) => {
              if (m.id !== messageId) return m;
              const artifacts = [...(m.artifacts ?? []), artifact];
              return {
                ...m,
                artifacts,
                assistantReplyView: deriveAssistantReplyView(artifacts),
              };
            }),
          };
        }),
      };
    }

    case 'SET_MESSAGE_ARTIFACTS': {
      const { sessionId, messageId, artifacts } = action.payload;
      return {
        ...state,
        sessions: state.sessions.map((s) => {
          if (s.id !== sessionId) return s;
          return {
            ...s,
            messages: s.messages.map((m) => {
              if (m.id !== messageId) return m;
              return {
                ...m,
                artifacts,
                assistantReplyView: deriveAssistantReplyView(artifacts),
              };
            }),
          };
        }),
      };
    }

    case 'SET_MESSAGE_REPLY_SEGMENTS': {
      const { sessionId, messageId, replySegments } = action.payload;
      return {
        ...state,
        sessions: state.sessions.map((s) => {
          if (s.id !== sessionId) return s;
          return {
            ...s,
            messages: s.messages.map((m) =>
              m.id === messageId ? { ...m, replySegments } : m,
            ),
          };
        }),
      };
    }

    case 'FINALIZE_ASSISTANT_REPLY_VIEW': {
      const { sessionId, messageId, completionPresentation, completionPartType } = action.payload;
      return {
        ...state,
        sessions: state.sessions.map((s) => {
          if (s.id !== sessionId) return s;
          return {
            ...s,
            messages: s.messages.map((m) => {
              if (m.id !== messageId) return m;
              return {
                ...m,
                assistantReplyView: deriveAssistantReplyView(m.artifacts, {
                  presentation: completionPresentation,
                  partType: completionPartType,
                }),
              };
            }),
          };
        }),
      };
    }

    case 'MERGE_SESSION_TRANSCRIPT': {
      const { sessionId, chatId, messages } = action.payload;
      return {
        ...state,
        sessions: state.sessions.map((s) =>
          s.id !== sessionId ? s : { ...s, chatId, messages },
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
  updateMessageArtifacts: (
    sessionId: string,
    messageId: string,
    artifacts: readonly ChatMessageArtifact[],
  ) => void;
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
        if (contextEntityType === 'COLUMN' && !flags.inlineChatModelColumn) return;
      }
      if (contextType === 'knowledge' && !flags.inlineChatKnowledgeContext) return;
      if (contextType === 'analysis' && !flags.inlineChatAnalysisContext) return;

      // If a session for this context already exists, just activate it
      const existing = state.sessions.find((s) => s.contextId === contextId);
      if (existing) {
        dispatch({ type: 'SET_ACTIVE_SESSION', payload: existing.id });
        dispatch({ type: 'OPEN_DRAWER' });
        if (!existing.chatId || existing.messages.length <= 1) {
          void hydrateSessionTranscript(dispatch, existing.id, contextType, contextId);
        }
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
        thinkingMessage: null,
        createdAt: Date.now(),
      };

      dispatch({ type: 'START_SESSION', payload: session });
      void hydrateSessionTranscript(dispatch, sessionId, contextType, contextId);
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

  const updateMessageArtifacts = useCallback(
    (sessionId: string, messageId: string, artifacts: readonly ChatMessageArtifact[]) => {
      dispatch({
        type: 'SET_MESSAGE_ARTIFACTS',
        payload: { sessionId, messageId, artifacts: [...artifacts] },
      });
    },
    [],
  );

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
        restReplay: false,
      };
      dispatch({ type: 'ADD_MESSAGE', payload: { sessionId, message: assistantMessage } });
      dispatch({ type: 'SET_LOADING', payload: { sessionId, isLoading: true } });

      try {
        // Ensure we have a backend chatId -- create on first message
        let chatId = session.chatId;
        if (!chatId) {
          const result = await chatService.createChat({
            profileId: resolveGeneralChatAgentProfileId(),
            contextType: session.contextType,
            contextId: session.contextId,
            contextLabel: session.contextLabel,
            contextEntityType: session.contextEntityType,
          });
          chatId = result.chatId;
          dispatch({ type: 'SET_SESSION_CHAT_ID', payload: { sessionId, chatId } });
        }

        dispatch({
          type: 'SET_SESSION_THINKING',
          payload: { sessionId, message: null },
        });
        let fullContent = '';
        const segmentTracker = new StreamingReplySegmentTracker();
        for await (const chunk of chatService.sendMessage(chatId, content, {
          onProgress: (evt) => {
            if (evt.kind === 'diagnostic') {
              dispatch({
                type: 'SET_SESSION_THINKING',
                payload: { sessionId, message: evt.message },
              });
            } else if (evt.kind === 'tool') {
              dispatch({
                type: 'SET_SESSION_THINKING',
                payload: { sessionId, message: evt.line },
              });
            } else if (evt.kind === 'clear-wait') {
              dispatch({
                type: 'SET_SESSION_THINKING',
                payload: { sessionId, message: null },
              });
            }
          },
          onNonTextPartUpdated: (evt) => {
            const artifact = parseChatStructuredPart(evt);
            if (!artifact) return;
            dispatch({
              type: 'APPEND_MESSAGE_ARTIFACT',
              payload: { sessionId, messageId: assistantMessage.id, artifact },
            });
            const replySegments = [...segmentTracker.onArtifact(artifact)];
            dispatch({
              type: 'SET_MESSAGE_REPLY_SEGMENTS',
              payload: { sessionId, messageId: assistantMessage.id, replySegments },
            });
          },
          onItemCompleted: (payload) => {
            dispatch({
              type: 'FINALIZE_ASSISTANT_REPLY_VIEW',
              payload: {
                sessionId,
                messageId: assistantMessage.id,
                completionPresentation: payload.presentation,
                completionPartType: payload.partType,
              },
            });
          },
        })) {
          fullContent += chunk;
          segmentTracker.setPendingText(fullContent);
          dispatch({
            type: 'UPDATE_MESSAGE',
            payload: { sessionId, messageId: assistantMessage.id, content: fullContent },
          });
        }
        const replySegments = [...segmentTracker.finalize()];
        if (replySegments.length > 0) {
          dispatch({
            type: 'SET_MESSAGE_REPLY_SEGMENTS',
            payload: { sessionId, messageId: assistantMessage.id, replySegments },
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
        dispatch({ type: 'SET_SESSION_THINKING', payload: { sessionId, message: null } });
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
    updateMessageArtifacts,
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
