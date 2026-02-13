export interface Message {
  id: string;
  conversationId: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: number;
}

export interface Conversation {
  id: string;
  title: string;
  createdAt: number;
  updatedAt: number;
  messages: Message[];
}

export interface ChatState {
  conversations: Conversation[];
  activeConversationId: string | null;
  isLoading: boolean;
  /** Transient thinking status text sent by the backend via SSE (null = not thinking) */
  thinkingMessage: string | null;
}

export interface CreateChatParams {
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
}

export interface ChatService {
  /**
   * Create a new chat on the backend. Returns the backend-assigned chatId and initial name.
   * Pass context params for inline/contextual chats; omit for general chat.
   */
  createChat(params?: CreateChatParams): Promise<CreateChatResult>;

  /** Send a user message to the given chat and stream the response chunks. */
  sendMessage(chatId: string, message: string): AsyncGenerator<string, void, unknown>;

  /**
   * List general (non-contextual) chats for the sidebar.
   * Inline/contextual chats are NOT included -- they are retrieved by context.
   */
  listChats(): Promise<ChatSummary[]>;

  /**
   * Retrieve the chatId for an inline/contextual chat by its context.
   * Returns null if no chat exists yet for this context.
   * (Retrieval mechanism TBD -- may become a search or dedicated endpoint.)
   */
  getChatByContext(contextType: string, contextId: string): Promise<string | null>;
}
