import { describe, it, expect } from 'vitest';
import type { Message } from '../../types/chat';
import type {
  InlineChatSession,
  InlineChatState,
  InlineChatAction,
} from '../../types/inlineChat';

// Replicate the reducer for unit testing
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
        isDrawerOpen: remaining.length > 0 ? state.isDrawerOpen : false,
      };
    }
    case 'SET_ACTIVE_SESSION':
      return { ...state, activeSessionId: action.payload };
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
          s.id !== sessionId ? s : {
            ...s,
            messages: s.messages.map((m) => m.id === messageId ? { ...m, content } : m),
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
      return { ...state, sessions: [], activeSessionId: null, isDrawerOpen: false };
    case 'OPEN_DRAWER':
      return { ...state, isDrawerOpen: true };
    case 'CLOSE_DRAWER':
      return { ...state, isDrawerOpen: false };
    default:
      return state;
  }
}

const initialState: InlineChatState = {
  sessions: [],
  activeSessionId: null,
  isDrawerOpen: false,
};

function makeSession(id: string, contextId: string): InlineChatSession {
  return {
    id,
    chatId: null,
    contextType: 'model',
    contextId,
    contextLabel: `Session ${id}`,
    messages: [],
    isLoading: false,
    createdAt: Date.now(),
  };
}

function makeMessage(id: string, sessionId: string, role: 'user' | 'assistant', content: string): Message {
  return { id, conversationId: sessionId, role, content, timestamp: Date.now() };
}

describe('inlineChatReducer', () => {
  describe('START_SESSION', () => {
    it('should add session, activate it, and open drawer', () => {
      const session = makeSession('s1', 'sales.customers');
      const state = inlineChatReducer(initialState, { type: 'START_SESSION', payload: session });
      expect(state.sessions).toHaveLength(1);
      expect(state.activeSessionId).toBe('s1');
      expect(state.isDrawerOpen).toBe(true);
    });

    it('should support multiple sessions', () => {
      let state = inlineChatReducer(initialState, { type: 'START_SESSION', payload: makeSession('s1', 'ctx1') });
      state = inlineChatReducer(state, { type: 'START_SESSION', payload: makeSession('s2', 'ctx2') });
      expect(state.sessions).toHaveLength(2);
      expect(state.activeSessionId).toBe('s2');
    });
  });

  describe('CLOSE_SESSION', () => {
    it('should remove session and activate the last remaining', () => {
      let state = inlineChatReducer(initialState, { type: 'START_SESSION', payload: makeSession('s1', 'ctx1') });
      state = inlineChatReducer(state, { type: 'START_SESSION', payload: makeSession('s2', 'ctx2') });
      state = inlineChatReducer(state, { type: 'CLOSE_SESSION', payload: 's2' });
      expect(state.sessions).toHaveLength(1);
      expect(state.activeSessionId).toBe('s1');
    });

    it('should close drawer when last session is closed', () => {
      let state = inlineChatReducer(initialState, { type: 'START_SESSION', payload: makeSession('s1', 'ctx1') });
      state = inlineChatReducer(state, { type: 'CLOSE_SESSION', payload: 's1' });
      expect(state.sessions).toHaveLength(0);
      expect(state.activeSessionId).toBeNull();
      expect(state.isDrawerOpen).toBe(false);
    });

    it('should keep active unchanged when closing non-active session', () => {
      let state = inlineChatReducer(initialState, { type: 'START_SESSION', payload: makeSession('s1', 'ctx1') });
      state = inlineChatReducer(state, { type: 'START_SESSION', payload: makeSession('s2', 'ctx2') });
      // Manually switch back to s1
      state = inlineChatReducer(state, { type: 'SET_ACTIVE_SESSION', payload: 's1' });
      state = inlineChatReducer(state, { type: 'CLOSE_SESSION', payload: 's2' });
      expect(state.activeSessionId).toBe('s1');
    });
  });

  describe('SET_ACTIVE_SESSION', () => {
    it('should change active session', () => {
      let state = inlineChatReducer(initialState, { type: 'START_SESSION', payload: makeSession('s1', 'ctx1') });
      state = inlineChatReducer(state, { type: 'START_SESSION', payload: makeSession('s2', 'ctx2') });
      state = inlineChatReducer(state, { type: 'SET_ACTIVE_SESSION', payload: 's1' });
      expect(state.activeSessionId).toBe('s1');
    });
  });

  describe('ADD_MESSAGE', () => {
    it('should add a message to the correct session', () => {
      let state = inlineChatReducer(initialState, { type: 'START_SESSION', payload: makeSession('s1', 'ctx1') });
      const msg = makeMessage('m1', 's1', 'user', 'Hello');
      state = inlineChatReducer(state, { type: 'ADD_MESSAGE', payload: { sessionId: 's1', message: msg } });
      expect(state.sessions[0]!.messages).toHaveLength(1);
      expect(state.sessions[0]!.messages[0]!.content).toBe('Hello');
    });
  });

  describe('UPDATE_MESSAGE', () => {
    it('should update message content in the correct session', () => {
      const session = makeSession('s1', 'ctx1');
      session.messages = [makeMessage('m1', 's1', 'assistant', 'part')];
      let state: InlineChatState = { sessions: [session], activeSessionId: 's1', isDrawerOpen: true };
      state = inlineChatReducer(state, {
        type: 'UPDATE_MESSAGE',
        payload: { sessionId: 's1', messageId: 'm1', content: 'partial complete' },
      });
      expect(state.sessions[0]!.messages[0]!.content).toBe('partial complete');
    });
  });

  describe('SET_LOADING', () => {
    it('should set loading for a specific session', () => {
      let state = inlineChatReducer(initialState, { type: 'START_SESSION', payload: makeSession('s1', 'ctx1') });
      state = inlineChatReducer(state, { type: 'SET_LOADING', payload: { sessionId: 's1', isLoading: true } });
      expect(state.sessions[0]!.isLoading).toBe(true);
    });
  });

  describe('CLOSE_ALL_SESSIONS', () => {
    it('should remove all sessions and close drawer', () => {
      let state = inlineChatReducer(initialState, { type: 'START_SESSION', payload: makeSession('s1', 'ctx1') });
      state = inlineChatReducer(state, { type: 'START_SESSION', payload: makeSession('s2', 'ctx2') });
      state = inlineChatReducer(state, { type: 'CLOSE_ALL_SESSIONS' });
      expect(state.sessions).toHaveLength(0);
      expect(state.activeSessionId).toBeNull();
      expect(state.isDrawerOpen).toBe(false);
    });
  });

  describe('OPEN_DRAWER / CLOSE_DRAWER', () => {
    it('should toggle drawer state', () => {
      const opened = inlineChatReducer(initialState, { type: 'OPEN_DRAWER' });
      expect(opened.isDrawerOpen).toBe(true);

      const closed = inlineChatReducer(opened, { type: 'CLOSE_DRAWER' });
      expect(closed.isDrawerOpen).toBe(false);
    });
  });
});
