import {
  createContext,
  useContext,
  useReducer,
  useCallback,
  useEffect,
  useRef,
  useState,
  type ReactNode,
} from 'react';
import type { AgentProfileResponseWire, Conversation, Message, ChatState, ChatSummary, ChatMessageArtifact } from '../types/chat';
import type { TurnResponseWire } from '../types/chatWire';
import { chatService } from '../services/api';
import { isRestChatBackendActive } from '../services/chatService';
import { useFeatureFlags } from '../features/FeatureFlagContext';
import {
  DEFAULT_GENERAL_CHAT_AGENT_PROFILE_ID,
  readStoredGeneralChatProfileId,
  resolveGeneralChatAgentProfileId,
  writeStoredGeneralChatProfileId,
} from '../features/chatPreferences';
import { parseChatStructuredPart } from '../utils/chatArtifactParse';
import { assistantReplyViewFromWire, deriveAssistantReplyView } from '../utils/assistantReplyView';

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

function summaryToConversation(summary: ChatSummary): Conversation {
  return {
    id: summary.chatId,
    title: summary.chatName,
    createdAt: summary.updatedAt,
    updatedAt: summary.updatedAt,
    messages: [],
    transcriptHydrated: false,
  };
}

function turnToMessage(turn: TurnResponseWire, conversationId: string): Message {
  const role = turn.role === 'user' ? 'user' : 'assistant';
  const view = role === 'assistant' ? assistantReplyViewFromWire(turn.assistantReplyView) : undefined;
  return {
    id: turn.turnId,
    conversationId,
    role,
    content: turn.text ?? '',
    timestamp: Date.parse(turn.createdAt),
    ...(view ? { assistantReplyView: view } : {}),
  };
}

type ChatAction =
  | { type: 'LOAD_CONVERSATIONS'; payload: Conversation[] }
  | { type: 'CREATE_CONVERSATION'; payload: Conversation }
  | { type: 'DELETE_CONVERSATION'; payload: string }
  | { type: 'SET_ACTIVE_CONVERSATION'; payload: string | null }
  | {
      type: 'ENSURE_ACTIVE_CONVERSATION';
      payload: { id: string; title: string; transcriptHydrated: boolean };
    }
  | { type: 'RENAME_CONVERSATION'; payload: { conversationId: string; title: string } }
  | { type: 'REPLACE_CONVERSATION_ID'; payload: { oldId: string; newId: string; title?: string } }
  | { type: 'ADD_MESSAGE'; payload: { conversationId: string; message: Message } }
  | { type: 'UPDATE_MESSAGE'; payload: { conversationId: string; messageId: string; content: string } }
  | { type: 'SET_LOADING'; payload: boolean }
  | { type: 'SET_THINKING'; payload: string | null }
  | { type: 'CLEAR_ALL' }
  | {
      type: 'MERGE_SERVER_TRANSCRIPT';
      payload: {
        conversationId: string;
        messages: Message[];
        title: string;
        updatedAtMs: number;
        profileId: string;
      };
    }
  | {
      type: 'APPEND_MESSAGE_ARTIFACT';
      payload: { conversationId: string; messageId: string; artifact: ChatMessageArtifact };
    }
  | {
      type: 'FINALIZE_ASSISTANT_REPLY_VIEW';
      payload: {
        conversationId: string;
        messageId: string;
        completionPresentation: string;
        completionPartType: string;
      };
    };

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

    case 'ENSURE_ACTIVE_CONVERSATION': {
      const { id, title, transcriptHydrated } = action.payload;
      if (state.conversations.some((c) => c.id === id)) {
        return {
          ...state,
          activeConversationId: id,
        };
      }
      const stub: Conversation = {
        id,
        title,
        createdAt: Date.now(),
        updatedAt: Date.now(),
        messages: [],
        transcriptHydrated,
      };
      return {
        ...state,
        conversations: [stub, ...state.conversations],
        activeConversationId: id,
      };
    }

    case 'RENAME_CONVERSATION': {
      const { conversationId, title } = action.payload;
      return {
        ...state,
        conversations: state.conversations.map((conv) =>
          conv.id === conversationId ? { ...conv, title, updatedAt: Date.now() } : conv,
        ),
      };
    }

    case 'REPLACE_CONVERSATION_ID': {
      const { oldId, newId, title } = action.payload;
      return {
        ...state,
        activeConversationId: state.activeConversationId === oldId ? newId : state.activeConversationId,
        conversations: state.conversations.map((conv) => {
          if (conv.id !== oldId) return conv;
          return {
            ...conv,
            id: newId,
            ...(title ? { title } : {}),
            updatedAt: Date.now(),
            messages: conv.messages.map((msg) => ({ ...msg, conversationId: newId })),
          };
        }),
      };
    }

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
              msg.id === messageId ? { ...msg, content } : msg,
            ),
            updatedAt: Date.now(),
          };
        }),
      };
    }

    case 'MERGE_SERVER_TRANSCRIPT': {
      const { conversationId, messages, title, updatedAtMs, profileId } = action.payload;
      return {
        ...state,
        conversations: state.conversations.map((conv) =>
          conv.id === conversationId
            ? {
                ...conv,
                title,
                updatedAt: updatedAtMs,
                profileId,
                messages,
                transcriptHydrated: true,
              }
            : conv,
        ),
      };
    }

    case 'APPEND_MESSAGE_ARTIFACT': {
      const { conversationId, messageId, artifact } = action.payload;
      return {
        ...state,
        conversations: state.conversations.map((conv) => {
          if (conv.id !== conversationId) return conv;
          return {
            ...conv,
            updatedAt: Date.now(),
            messages: conv.messages.map((msg) => {
              if (msg.id !== messageId) return msg;
              const prev = msg.artifacts ?? [];
              const artifacts = [...prev, artifact];
              return {
                ...msg,
                artifacts,
                assistantReplyView: deriveAssistantReplyView(artifacts),
              };
            }),
          };
        }),
      };
    }

    case 'FINALIZE_ASSISTANT_REPLY_VIEW': {
      const { conversationId, messageId, completionPresentation, completionPartType } = action.payload;
      return {
        ...state,
        conversations: state.conversations.map((conv) => {
          if (conv.id !== conversationId) return conv;
          return {
            ...conv,
            updatedAt: Date.now(),
            messages: conv.messages.map((msg) => {
              if (msg.id !== messageId) return msg;
              return {
                ...msg,
                assistantReplyView: deriveAssistantReplyView(msg.artifacts, {
                  presentation: completionPresentation,
                  partType: completionPartType,
                }),
              };
            }),
          };
        }),
      };
    }

    case 'SET_LOADING':
      return {
        ...state,
        isLoading: action.payload,
        ...(action.payload === false ? { thinkingMessage: null } : {}),
      };

    case 'SET_THINKING':
      return {
        ...state,
        thinkingMessage: action.payload,
      };

    case 'CLEAR_ALL':
      return {
        conversations: [],
        activeConversationId: null,
        isLoading: false,
        thinkingMessage: null,
      };

    default:
      return state;
  }
}

interface ChatContextValue {
  state: ChatState;
  /** True once initial store hydration or first REST list fetch completes */
  initialized: boolean;
  activeConversation: Conversation | null;
  createConversation: () => Promise<string | undefined>;
  deleteConversation: (id: string) => Promise<void>;
  setActiveConversation: (id: string | null) => void;
  /** Ensures `id` is in the sidebar (stub if missing) and selects it — used for `/chat/:chatId` deep links. */
  ensureActiveConversation: (id: string) => void;
  /**
   * Lets {@link ChatRouteSync} report the current `:conversationId` route param so deep-link
   * suppression after delete does not leak across navigations.
   */
  syncChatRouteConversationParam: (routeConversationId: string | undefined) => void;
  renameConversation: (id: string, title: string) => Promise<void>;
  /** Set the transient thinking status text (null to clear). Driven by backend SSE events. */
  setThinking: (message: string | null) => void;
  sendMessage: (content: string, options?: { newConversation?: boolean }) => Promise<void>;
  clearAllConversations: () => void;
  /** Profiles for the optional General Chat agent picker (`chatAgentPicker` flag). */
  agentProfiles: AgentProfileResponseWire[];
  agentProfilesLoading: boolean;
  /** Profile applied to *new* general chats (backed by sessionStorage). */
  selectedAgentProfileId: string | null;
  setSelectedAgentProfileId: (id: string | null) => void;
}

const ChatContext = createContext<ChatContextValue | null>(null);

const initialState: ChatState = {
  conversations: [],
  activeConversationId: null,
  isLoading: false,
  thinkingMessage: null,
};

export function ChatProvider({ children }: { children: ReactNode }) {
  const [state, dispatch] = useReducer(chatReducer, initialState);
  /**
   * When the user deletes a chat, the URL may still be `/chat/:id` for one frame. Deep-link
   * sync must not recreate a "Loading..." stub for that id before the route clears.
   */
  const skipDeepLinkEnsureForChatIdRef = useRef<string | null>(null);
  const [initialized, setInitialized] = useState(false);
  const flags = useFeatureFlags();
  const [agentProfiles, setAgentProfiles] = useState<AgentProfileResponseWire[]>([]);
  const [agentProfilesLoading, setAgentProfilesLoading] = useState(false);
  const [selectedAgentProfileId, setSelectedAgentProfileIdState] = useState<string | null>(() =>
    readStoredGeneralChatProfileId() ?? DEFAULT_GENERAL_CHAT_AGENT_PROFILE_ID,
  );

  const setSelectedAgentProfileId = useCallback((id: string | null) => {
    writeStoredGeneralChatProfileId(id);
    setSelectedAgentProfileIdState(id);
  }, []);

  const resolveProfile = useCallback(
    () => resolveGeneralChatAgentProfileId({ selectedId: selectedAgentProfileId }),
    [selectedAgentProfileId],
  );

  useEffect(() => {
    if (!flags.chatAgentPicker) {
      setAgentProfiles([]);
      setAgentProfilesLoading(false);
      return;
    }
    let cancelled = false;
    setAgentProfilesLoading(true);
    chatService
      .listAgentProfiles()
      .then((list) => {
        if (!cancelled) setAgentProfiles(list);
      })
      .catch(() => {
        if (!cancelled) setAgentProfiles([]);
      })
      .finally(() => {
        if (!cancelled) setAgentProfilesLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [flags.chatAgentPicker]);

  useEffect(() => {
    const rest = isRestChatBackendActive();

    if (!rest) {
      try {
        const stored = localStorage.getItem(STORAGE_KEY);
        if (stored) {
          const conversations = JSON.parse(stored) as Conversation[];
          const normalized = conversations.map((c) => ({
            ...c,
            transcriptHydrated: c.transcriptHydrated ?? true,
          }));
          dispatch({ type: 'LOAD_CONVERSATIONS', payload: normalized });
        }
      } catch (e) {
        console.error('Failed to load conversations from localStorage:', e);
      }
      setInitialized(true);
      return;
    }

    let cancelled = false;

    async function hydrateRest() {
      try {
        localStorage.removeItem(STORAGE_KEY);
      } catch {
        /* ignore */
      }
      try {
        const summaries = await chatService.listChats();
        if (cancelled) return;
        const convs = summaries.map(summaryToConversation);
        dispatch({ type: 'LOAD_CONVERSATIONS', payload: convs });
      } catch (e) {
        console.error('Failed to load chats from server:', e);
        if (!cancelled) {
          dispatch({ type: 'CLEAR_ALL' });
        }
      }
      if (!cancelled) setInitialized(true);
    }

    void hydrateRest();

    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    if (isRestChatBackendActive()) {
      return;
    }
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(state.conversations));
    } catch (e) {
      console.error('Failed to save conversations to localStorage:', e);
    }
  }, [state.conversations]);

  useEffect(() => {
    const id = state.activeConversationId;
    if (!id || id.startsWith('temp-')) {
      return;
    }
    if (!isRestChatBackendActive()) {
      return;
    }
    const conv = state.conversations.find((c) => c.id === id);
    if (!conv || conv.transcriptHydrated !== false) {
      return;
    }

    let cancelled = false;

    void (async () => {
      try {
        const detail = await chatService.getChatDetail(id);
        if (cancelled) return;
        dispatch({
          type: 'MERGE_SERVER_TRANSCRIPT',
          payload: {
            conversationId: id,
            messages: detail.messages.map((t) => turnToMessage(t, id)),
            title: detail.chat.chatName,
            updatedAtMs: Date.parse(detail.chat.updatedAt),
            profileId: detail.chat.profileId,
          },
        });
      } catch (e) {
        if (!cancelled) {
          console.error('Failed to load chat transcript:', e);
          dispatch({
            type: 'MERGE_SERVER_TRANSCRIPT',
            payload: {
              conversationId: id,
              messages: [],
              title: 'Chat unavailable',
              updatedAtMs: Date.now(),
              profileId: conv.profileId ?? '',
            },
          });
        }
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [state.activeConversationId, state.conversations]);

  const activeConversation =
    state.conversations.find((c) => c.id === state.activeConversationId) ?? null;

  const createConversation = useCallback(async () => {
    if (!initialized) return undefined;
    try {
      const profileId = resolveProfile();
      const { chatId, chatName } = await chatService.createChat(profileId ? { profileId } : undefined);
      const newConversation: Conversation = {
        id: chatId,
        title: chatName,
        createdAt: Date.now(),
        updatedAt: Date.now(),
        messages: [],
        transcriptHydrated: true,
      };
      dispatch({ type: 'CREATE_CONVERSATION', payload: newConversation });
      return chatId;
    } catch (error) {
      console.error('Failed to create chat:', error);
      return undefined;
    }
  }, [initialized, resolveProfile]);

  const deleteConversation = useCallback(async (chatId: string) => {
    skipDeepLinkEnsureForChatIdRef.current = chatId;
    if (isRestChatBackendActive()) {
      try {
        await chatService.deleteChat(chatId);
      } catch {
        /* tolerate 404 / network — still drop locally */
      }
    }
    dispatch({ type: 'DELETE_CONVERSATION', payload: chatId });
  }, []);

  const setActiveConversation = useCallback((id: string | null) => {
    dispatch({ type: 'SET_ACTIVE_CONVERSATION', payload: id });
  }, []);

  const ensureActiveConversation = useCallback(
    (id: string) => {
      if (skipDeepLinkEnsureForChatIdRef.current === id) {
        return;
      }
      const isRest = isRestChatBackendActive();
      if (state.conversations.some((c) => c.id === id)) {
        if (state.activeConversationId !== id) {
          dispatch({ type: 'SET_ACTIVE_CONVERSATION', payload: id });
        }
        return;
      }
      dispatch({
        type: 'ENSURE_ACTIVE_CONVERSATION',
        payload: {
          id,
          title: 'Loading...',
          transcriptHydrated: !isRest,
        },
      });
    },
    [state.conversations, state.activeConversationId],
  );

  const syncChatRouteConversationParam = useCallback((routeConversationId: string | undefined) => {
    const skip = skipDeepLinkEnsureForChatIdRef.current;
    if (skip === null) {
      return;
    }
    if (routeConversationId === undefined || routeConversationId !== skip) {
      skipDeepLinkEnsureForChatIdRef.current = null;
    }
  }, []);

  const renameConversation = useCallback(async (id: string, title: string) => {
    if (isRestChatBackendActive()) {
      try {
        const chat = await chatService.renameChat(id, title);
        dispatch({
          type: 'RENAME_CONVERSATION',
          payload: { conversationId: id, title: chat.chatName },
        });
        return;
      } catch (e) {
        console.error('Failed to rename chat on server:', e);
      }
    }
    dispatch({ type: 'RENAME_CONVERSATION', payload: { conversationId: id, title } });
  }, []);

  const setThinking = useCallback((message: string | null) => {
    dispatch({ type: 'SET_THINKING', payload: message });
  }, []);

  const sendMessage = useCallback(
    async (content: string, options?: { newConversation?: boolean }) => {
      if (!initialized || state.isLoading) return;

      let conversationId = options?.newConversation ? null : state.activeConversationId;
      const needsCreate = !conversationId;

      if (needsCreate) {
        const tempId = `temp-${generateId()}`;
        const tempConversation: Conversation = {
          id: tempId,
          title: 'New Chat',
          createdAt: Date.now(),
          updatedAt: Date.now(),
          messages: [],
          transcriptHydrated: true,
        };
        dispatch({ type: 'CREATE_CONVERSATION', payload: tempConversation });
        conversationId = tempId;
      }

      if (!conversationId) return;

      let streamingChatId: string = conversationId;

      const userMessage: Message = {
        id: generateId(),
        conversationId: streamingChatId,
        role: 'user',
        content,
        timestamp: Date.now(),
      };
      dispatch({ type: 'ADD_MESSAGE', payload: { conversationId: streamingChatId, message: userMessage } });

      const assistantMessage: Message = {
        id: generateId(),
        conversationId: streamingChatId,
        role: 'assistant',
        content: '',
        timestamp: Date.now(),
      };
      dispatch({ type: 'ADD_MESSAGE', payload: { conversationId: streamingChatId, message: assistantMessage } });
      dispatch({ type: 'SET_LOADING', payload: true });
      dispatch({ type: 'SET_THINKING', payload: null });

      if (needsCreate) {
        try {
          const profileId = resolveProfile();
          const { chatId, chatName } = await chatService.createChat(profileId ? { profileId } : undefined);
          dispatch({
            type: 'REPLACE_CONVERSATION_ID',
            payload: { oldId: streamingChatId, newId: chatId, title: chatName },
          });
          streamingChatId = chatId;
        } catch (error) {
          console.error('Failed to create chat:', error);
          dispatch({
            type: 'UPDATE_MESSAGE',
            payload: {
              conversationId: streamingChatId,
              messageId: assistantMessage.id,
              content: 'Sorry, failed to create the chat. Please try again.',
            },
          });
          dispatch({ type: 'SET_LOADING', payload: false });
          return;
        }
      }

      try {
        let fullContent = '';
        let firstChunk = true;
        for await (const chunk of chatService.sendMessage(streamingChatId, content, {
          onProgress: (evt) => {
            if (evt.kind === 'diagnostic') {
              dispatch({ type: 'SET_THINKING', payload: evt.message });
            } else if (evt.kind === 'tool') {
              dispatch({ type: 'SET_THINKING', payload: evt.line });
            } else if (evt.kind === 'clear-wait') {
              dispatch({ type: 'SET_THINKING', payload: null });
            }
          },
          onNonTextPartUpdated: (evt) => {
            const artifact = parseChatStructuredPart(evt);
            if (!artifact) return;
            dispatch({
              type: 'APPEND_MESSAGE_ARTIFACT',
              payload: {
                conversationId: streamingChatId,
                messageId: assistantMessage.id,
                artifact,
              },
            });
          },
          onItemCompleted: (payload) => {
            dispatch({
              type: 'FINALIZE_ASSISTANT_REPLY_VIEW',
              payload: {
                conversationId: streamingChatId,
                messageId: assistantMessage.id,
                completionPresentation: payload.presentation,
                completionPartType: payload.partType,
              },
            });
          },
        })) {
          if (firstChunk) {
            dispatch({ type: 'SET_THINKING', payload: null });
            firstChunk = false;
          }
          fullContent += chunk;
          dispatch({
            type: 'UPDATE_MESSAGE',
            payload: { conversationId: streamingChatId, messageId: assistantMessage.id, content: fullContent },
          });
        }
      } catch (error) {
        console.error('Failed to get response:', error);
        dispatch({
          type: 'UPDATE_MESSAGE',
          payload: {
            conversationId: streamingChatId,
            messageId: assistantMessage.id,
            content: 'Sorry, I encountered an error. Please try again.',
          },
        });
      } finally {
        dispatch({ type: 'SET_LOADING', payload: false });
      }
    },
    [state.activeConversationId, state.isLoading, initialized, resolveProfile],
  );

  const clearAllConversations = useCallback(() => {
    dispatch({ type: 'CLEAR_ALL' });
  }, []);

  const value: ChatContextValue = {
    state,
    initialized,
    activeConversation,
    createConversation,
    deleteConversation,
    setActiveConversation,
    ensureActiveConversation,
    syncChatRouteConversationParam,
    renameConversation,
    setThinking,
    sendMessage,
    clearAllConversations,
    agentProfiles,
    agentProfilesLoading,
    selectedAgentProfileId,
    setSelectedAgentProfileId,
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
