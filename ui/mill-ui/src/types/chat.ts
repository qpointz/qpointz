import type { AgentProfileResponseWire, ChatDetailResponseWire, ChatResponseWire } from './chatWire';
import type { QueryColumn } from './query';

export type {
  AgentProfileResponseWire,
  ArtifactResponseWire,
  ChatDetailResponseWire,
  ChatResponseWire,
  CreateChatRequestWire,
  SendMessageRequestWire,
  TurnResponseWire,
  UpdateChatRequestWire,
} from './chatWire';

/** Normalized durable slices attached to an assistant message (from structured SSE parts). */
export type ChatMessageArtifact =
  | { kind: 'sql'; sql: string; dialectId?: string }
  | {
      kind: 'data';
      executionId: string;
      sql?: string;
      rowCount?: number;
      truncated?: boolean;
      columns?: QueryColumn[];
    }
  | {
      kind: 'facet-proposal';
      facetTypeKey: string;
      metadataEntityId: string;
      payload: unknown;
    }
  | {
      kind: 'unknown';
      partType: string;
      title: string;
      payload: unknown;
    };

/** Layout routing for assistant replies (SSE + GET transcript). */
export type AssistantReplyView =
  | 'conversation'
  | 'sql-primary'
  | 'facet-primary'
  | 'schema-primary'
  | 'artifact-primary';

export interface Message {
  id: string;
  conversationId: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: number;
  /**
   * Derived from artefacts and/or `item.completed` summary — optional on GET until persistence fills
   * [TurnResponseWire.assistantReplyView].
   */
  assistantReplyView?: AssistantReplyView;
  /** Structured artefacts (SQL, facet proposals, query results) from SSE or GET replay. */
  artifacts?: readonly ChatMessageArtifact[];
  /**
   * When `true`, message was loaded from REST transcript replay — SQL preview stays on the SQL tab
   * and does not auto-execute. Live streaming turns omit this flag (or set `false`).
   */
  restReplay?: boolean;
}

export interface Conversation {
  id: string;
  title: string;
  createdAt: number;
  updatedAt: number;
  messages: Message[];
  /** Agent profile id from server metadata (GET chat detail) — optional display. */
  profileId?: string;
  /**
   * When using REST, `false` until `getChatDetail` has populated `messages`.
   * Local/mock persistence treats missing as already hydrated.
   */
  transcriptHydrated?: boolean;
}

export interface ChatState {
  conversations: Conversation[];
  activeConversationId: string | null;
  isLoading: boolean;
  /** Transient thinking status text sent by the backend via SSE (null = not thinking) */
  thinkingMessage: string | null;
}

export interface CreateChatParams {
  /**
   * Optional agent profile locked for the chat lifecycle (sent as `profileId` on POST create).
   * When omitted, the UI resolves `data-analysis` (or `VITE_MILL_AI_PROFILE` / session picker when set).
   */
  profileId?: string;
  /** Context type for inline chat sessions (omit for general chat) */
  contextType?: 'model' | 'knowledge' | 'analysis';
  /** Context entity ID (e.g. "sales.customers.customer_id", concept id, query id) */
  contextId?: string;
  /** Human-readable label for the context entity */
  contextLabel?: string;
  /** Entity sub-type (e.g. 'SCHEMA', 'TABLE', 'ATTRIBUTE') */
  contextEntityType?: string;
}

export interface CreateChatResult {
  chatId: string;
  chatName: string;
}

/** Summary returned when listing general chats (no messages included). */
export interface ChatSummary {
  chatId: string;
  chatName: string;
  updatedAt: number;
  profileId?: string;
}

/** Wait-state payloads while an assistant reply is streamed (SSE). */
export type ChatSendProgress =
  | { kind: 'diagnostic'; code: string; message: string }
  | { kind: 'tool'; line: string }
  /** Clear transient wait UI (thinking / tool hints) once real text arrives or the turn ends. */
  | { kind: 'clear-wait' };

/** End-of-turn SSE row passed to [ChatSendOptions.onItemCompleted]. */
export interface ChatItemCompletedPayload {
  readonly itemId: string;
  readonly presentation: string;
  readonly partType: string;
  readonly content: string | null;
}

/** Options passed to REST/mock streaming send implementations. */
export interface ChatSendOptions {
  /** Invoked from `item.diagnostic`, tame `item.tool.*` lines, and `clear-wait` boundaries. */
  onProgress?: (event: ChatSendProgress) => void;
  /**
   * Non-conversation / non-text `item.part.updated` payloads (forward-compatible).
   * Full reducer wiring lands in WI-231.
   */
  onNonTextPartUpdated?: (payload: Record<string, unknown>) => void;
  /**
   * Invoked for each `item.completed`. When structured parts were streamed, [ChatItemCompletedPayload.presentation]
   * / [ChatItemCompletedPayload.partType] repeat the last structured summary from the server.
   */
  onItemCompleted?: (payload: ChatItemCompletedPayload) => void;
}

export interface AttachExecutionResultRequest {
  executionId: string;
  columns?: QueryColumn[];
  rowCount?: number;
  truncated?: boolean;
  sql?: string;
}

export interface ChatService {
  /**
   * Create a new chat on the backend. Returns the backend-assigned chatId and initial name.
   * Pass context params for inline/contextual chats; omit for general chat.
   */
  createChat(params?: CreateChatParams): Promise<CreateChatResult>;

  /**
   * Send a user message and stream incremental assistant text deltas.
   * Progress for wait UX flows through optional `options.onProgress`.
   */
  sendMessage(
    chatId: string,
    message: string,
    options?: ChatSendOptions,
  ): AsyncGenerator<string, void, unknown>;

  /**
   * List general (non-contextual) chats for the sidebar.
   * Inline/contextual chats are NOT included — they are retrieved by context lookup.
   */
  listChats(): Promise<ChatSummary[]>;

  /**
   * Full chat envelope plus durable transcript turns (REST: GET `/chats/{id}`).
   */
  getChatDetail(chatId: string): Promise<ChatDetailResponseWire>;

  /** Deletes a chat and its transcript (REST: DELETE `/chats/{id}`). */
  deleteChat(chatId: string): Promise<void>;

  /** Updates display name (`chatName`) and returns the refreshed metadata envelope. */
  renameChat(chatId: string, chatName: string): Promise<ChatResponseWire>;
  /** PATCH agent profile for an existing general chat. */
  updateChatProfile(chatId: string, profileId: string): Promise<ChatResponseWire>;

  /**
   * Agent profiles advertised by the host (`GET /api/v1/ai/profiles`).
   */
  listAgentProfiles(): Promise<AgentProfileResponseWire[]>;

  /**
   * Resolve the chat id for an inline/contextual singleton, when it already exists.
   * REST: GET `/chats/context-types/{contextType}/contexts/{contextId}` (`404` → `null`).
   */
  getChatByContext(contextType: string, contextId: string): Promise<string | null>;

  /**
   * Attach query execution metadata to an assistant turn for durable artifact replay.
   */
  attachExecutionResult(
    chatId: string,
    turnId: string,
    request: AttachExecutionResultRequest,
  ): Promise<ChatMessageArtifact | null>;
}
