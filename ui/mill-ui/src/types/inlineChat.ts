import type { Message } from './chat';

export type InlineChatContextType = 'model' | 'knowledge' | 'analysis';

export interface InlineChatSession {
  id: string;
  /** Backend-assigned chat ID (null until createChat resolves) */
  chatId: string | null;
  contextType: InlineChatContextType;
  contextId: string;           // e.g. "sales.customers.customer_id" or concept id
  contextLabel: string;        // e.g. "customer_id" or concept name
  contextEntityType?: string;  // 'SCHEMA' | 'TABLE' | 'ATTRIBUTE' | category name
  messages: Message[];
  isLoading: boolean;
  createdAt: number;
}

export interface InlineChatState {
  sessions: InlineChatSession[];
  activeSessionId: string | null;
  isDrawerOpen: boolean;
}

export type InlineChatAction =
  | { type: 'START_SESSION'; payload: InlineChatSession }
  | { type: 'CLOSE_SESSION'; payload: string }
  | { type: 'SET_ACTIVE_SESSION'; payload: string }
  | { type: 'SET_SESSION_CHAT_ID'; payload: { sessionId: string; chatId: string } }
  | { type: 'ADD_MESSAGE'; payload: { sessionId: string; message: Message } }
  | { type: 'UPDATE_MESSAGE'; payload: { sessionId: string; messageId: string; content: string } }
  | { type: 'SET_LOADING'; payload: { sessionId: string; isLoading: boolean } }
  | { type: 'CLOSE_ALL_SESSIONS' }
  | { type: 'OPEN_DRAWER' }
  | { type: 'CLOSE_DRAWER' };
