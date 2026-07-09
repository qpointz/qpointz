import type { ChatMessageArtifact, Message, AssistantReplySegment } from './chat';

export type InlineChatContextType = 'model' | 'knowledge' | 'analysis';

/** How the Analysis host reacts when SQL proposals arrive. */
export type AnalysisCopilotAutomationMode = 'manual' | 'apply' | 'run';

/** Per-session Analysis copilot host settings. */
export interface AnalysisCopilotSettings {
  'automation.mode': AnalysisCopilotAutomationMode;
}

export const DEFAULT_ANALYSIS_COPILOT_SETTINGS: AnalysisCopilotSettings = {
  'automation.mode': 'manual',
};

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
  /** Transient SSE diagnostic / tool hint — same contract as General Chat `thinkingMessage`. */
  thinkingMessage: string | null;
  /** Analysis copilot host settings (meaningful when `contextType === 'analysis'`). */
  settings: AnalysisCopilotSettings;
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
  | {
      type: 'SET_SESSION_THINKING';
      payload: { sessionId: string; message: string | null };
    }
  | {
      type: 'APPEND_MESSAGE_ARTIFACT';
      payload: { sessionId: string; messageId: string; artifact: ChatMessageArtifact };
    }
  | {
      type: 'SET_MESSAGE_ARTIFACTS';
      payload: { sessionId: string; messageId: string; artifacts: ChatMessageArtifact[] };
    }
  | {
      type: 'SET_MESSAGE_REPLY_SEGMENTS';
      payload: { sessionId: string; messageId: string; replySegments: AssistantReplySegment[] };
    }
  | {
      type: 'FINALIZE_ASSISTANT_REPLY_VIEW';
      payload: {
        sessionId: string;
        messageId: string;
        completionPresentation: string;
        completionPartType: string;
      };
    }
  | {
      type: 'MERGE_SESSION_TRANSCRIPT';
      payload: { sessionId: string; chatId: string; messages: Message[] };
    }
  | { type: 'CLOSE_ALL_SESSIONS' }
  | { type: 'OPEN_DRAWER' }
  | { type: 'CLOSE_DRAWER' }
  | {
      type: 'SET_SESSION_SETTINGS';
      payload: { sessionId: string; settings: AnalysisCopilotSettings };
    };
