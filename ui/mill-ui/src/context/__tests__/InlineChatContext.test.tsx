import { describe, it, expect, vi } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import type { ReactNode } from 'react';
import { InlineChatProvider, useInlineChat } from '../InlineChatContext';
import { FeatureFlagProvider } from '../../features/FeatureFlagContext';
import { defaultFeatureFlags } from '../../features/defaults';

// Mock the unified chat service (inline chat now uses chatService)
vi.mock('../../services/api', () => ({
  chatService: {
    async createChat() {
      return { chatId: 'mock-chat-id', chatName: 'Mock Chat' };
    },
    async *sendMessage() {
      yield 'Inline mock response';
    },
  },
  featureService: {
    async getFlags() {
      // Return defaults so the FeatureFlagProvider resolves quickly
      return { ...defaultFeatureFlags };
    },
  },
}));

function wrapper({ children }: { children: ReactNode }) {
  return (
    <FeatureFlagProvider>
      <InlineChatProvider>{children}</InlineChatProvider>
    </FeatureFlagProvider>
  );
}

describe('InlineChatContext', () => {
  describe('useInlineChat outside provider', () => {
    it('should throw when used outside InlineChatProvider', () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
      expect(() => {
        renderHook(() => useInlineChat());
      }).toThrow('useInlineChat must be used within an InlineChatProvider');
      consoleSpy.mockRestore();
    });
  });

  describe('initial state', () => {
    it('should start with no sessions, drawer closed', () => {
      const { result } = renderHook(() => useInlineChat(), { wrapper });
      expect(result.current.state.sessions).toHaveLength(0);
      expect(result.current.state.activeSessionId).toBeNull();
      expect(result.current.state.isDrawerOpen).toBe(false);
      expect(result.current.activeSession).toBeNull();
      expect(result.current.hasAnySessions()).toBe(false);
    });
  });

  describe('startSession', () => {
    it('should create a session and open the drawer', () => {
      const { result } = renderHook(() => useInlineChat(), { wrapper });

      act(() => {
        result.current.startSession('model', 'sales.customers', 'customers', 'TABLE');
      });

      expect(result.current.state.sessions).toHaveLength(1);
      expect(result.current.state.isDrawerOpen).toBe(true);
      expect(result.current.activeSession).not.toBeNull();
      expect(result.current.activeSession!.contextId).toBe('sales.customers');
      expect(result.current.activeSession!.contextType).toBe('model');
      expect(result.current.activeSession!.contextLabel).toBe('customers');
      expect(result.current.hasAnySessions()).toBe(true);
    });

    it('should include greeting message when flag is enabled', () => {
      const { result } = renderHook(() => useInlineChat(), { wrapper });

      act(() => {
        result.current.startSession('model', 'sales.customers', 'customers');
      });

      // Default flags have inlineChatGreeting: true
      expect(result.current.activeSession!.messages.length).toBeGreaterThanOrEqual(1);
      const greeting = result.current.activeSession!.messages[0]!;
      expect(greeting.role).toBe('assistant');
      expect(greeting.content).toContain('customers');
    });

    it('should reuse existing session for the same context', () => {
      const { result } = renderHook(() => useInlineChat(), { wrapper });

      act(() => {
        result.current.startSession('model', 'sales.customers', 'customers');
      });
      const firstId = result.current.activeSession!.id;

      act(() => {
        result.current.startSession('model', 'sales.customers', 'customers');
      });

      // Should still have 1 session, same ID
      expect(result.current.state.sessions).toHaveLength(1);
      expect(result.current.activeSession!.id).toBe(firstId);
    });

    it('should create multiple sessions for different contexts', () => {
      const { result } = renderHook(() => useInlineChat(), { wrapper });

      act(() => {
        result.current.startSession('model', 'sales.customers', 'customers');
      });
      act(() => {
        result.current.startSession('knowledge', 'customer-lifetime-value', 'CLV');
      });

      expect(result.current.state.sessions).toHaveLength(2);
    });
  });

  describe('closeSession', () => {
    it('should remove a session', () => {
      const { result } = renderHook(() => useInlineChat(), { wrapper });

      act(() => {
        result.current.startSession('model', 'sales.customers', 'customers');
      });
      const sessionId = result.current.activeSession!.id;

      act(() => {
        result.current.closeSession(sessionId);
      });

      expect(result.current.state.sessions).toHaveLength(0);
      expect(result.current.state.activeSessionId).toBeNull();
      expect(result.current.hasAnySessions()).toBe(false);
    });

    it('should close drawer when last session is closed', () => {
      const { result } = renderHook(() => useInlineChat(), { wrapper });

      act(() => {
        result.current.startSession('model', 'sales.customers', 'customers');
      });
      expect(result.current.state.isDrawerOpen).toBe(true);

      act(() => {
        result.current.closeSession(result.current.activeSession!.id);
      });

      expect(result.current.state.isDrawerOpen).toBe(false);
    });

    it('should activate another session when active is closed', () => {
      const { result } = renderHook(() => useInlineChat(), { wrapper });

      act(() => {
        result.current.startSession('model', 'sales.customers', 'customers');
      });
      const firstId = result.current.state.sessions[0]!.id;

      act(() => {
        result.current.startSession('knowledge', 'clv', 'CLV');
      });
      const secondId = result.current.activeSession!.id;

      act(() => {
        result.current.closeSession(secondId);
      });

      expect(result.current.state.activeSessionId).toBe(firstId);
      expect(result.current.state.isDrawerOpen).toBe(true);
    });
  });

  describe('setActiveSession', () => {
    it('should switch the active session', () => {
      const { result } = renderHook(() => useInlineChat(), { wrapper });

      act(() => {
        result.current.startSession('model', 'ctx1', 'Entity 1');
      });
      const firstId = result.current.state.sessions[0]!.id;

      act(() => {
        result.current.startSession('knowledge', 'ctx2', 'Concept 1');
      });

      // Active should be the second
      expect(result.current.activeSession!.contextId).toBe('ctx2');

      act(() => {
        result.current.setActiveSession(firstId);
      });

      expect(result.current.activeSession!.contextId).toBe('ctx1');
    });
  });

  describe('sendMessage', () => {
    it('should add user message and receive AI response', async () => {
      const { result } = renderHook(() => useInlineChat(), { wrapper });

      act(() => {
        result.current.startSession('model', 'sales.customers', 'customers');
      });
      const sessionId = result.current.activeSession!.id;
      const initialMsgCount = result.current.activeSession!.messages.length;

      await act(async () => {
        await result.current.sendMessage(sessionId, 'Tell me about this table');
      });

      const session = result.current.activeSession!;
      // Should have added user + assistant messages
      expect(session.messages.length).toBe(initialMsgCount + 2);

      const userMsg = session.messages[initialMsgCount]!;
      expect(userMsg.role).toBe('user');
      expect(userMsg.content).toBe('Tell me about this table');

      const aiMsg = session.messages[initialMsgCount + 1]!;
      expect(aiMsg.role).toBe('assistant');
      expect(aiMsg.content).toBe('Inline mock response');
    });

    it('should set loading to false after message completes', async () => {
      const { result } = renderHook(() => useInlineChat(), { wrapper });

      act(() => {
        result.current.startSession('model', 'sales.customers', 'customers');
      });
      const sessionId = result.current.activeSession!.id;

      await act(async () => {
        await result.current.sendMessage(sessionId, 'Hello');
      });

      expect(result.current.activeSession!.isLoading).toBe(false);
    });
  });

  describe('openDrawer / closeDrawer', () => {
    it('should toggle drawer state', () => {
      const { result } = renderHook(() => useInlineChat(), { wrapper });

      act(() => {
        result.current.openDrawer();
      });
      expect(result.current.state.isDrawerOpen).toBe(true);

      act(() => {
        result.current.closeDrawer();
      });
      expect(result.current.state.isDrawerOpen).toBe(false);
    });
  });

  describe('closeAllSessions', () => {
    it('should remove all sessions and close drawer', () => {
      const { result } = renderHook(() => useInlineChat(), { wrapper });

      act(() => {
        result.current.startSession('model', 'ctx1', 'Entity 1');
        result.current.startSession('knowledge', 'ctx2', 'Concept 1');
      });

      expect(result.current.state.sessions.length).toBeGreaterThanOrEqual(1);

      act(() => {
        result.current.closeAllSessions();
      });

      expect(result.current.state.sessions).toHaveLength(0);
      expect(result.current.state.activeSessionId).toBeNull();
      expect(result.current.state.isDrawerOpen).toBe(false);
    });
  });

  describe('getSessionByContextId', () => {
    it('should find session by context ID', () => {
      const { result } = renderHook(() => useInlineChat(), { wrapper });

      act(() => {
        result.current.startSession('model', 'sales.customers', 'customers');
      });

      const found = result.current.getSessionByContextId('sales.customers');
      expect(found).toBeDefined();
      expect(found!.contextLabel).toBe('customers');
    });

    it('should return undefined for unknown context ID', () => {
      const { result } = renderHook(() => useInlineChat(), { wrapper });

      const found = result.current.getSessionByContextId('nonexistent');
      expect(found).toBeUndefined();
    });
  });

  describe('listener registration', () => {
    it('should register and unregister listeners without error', () => {
      const { result } = renderHook(() => useInlineChat(), { wrapper });

      const callback = vi.fn();

      act(() => {
        result.current.registerListener('ctx1', callback);
      });

      // Unregister should not throw
      act(() => {
        result.current.unregisterListener('ctx1', callback);
      });
    });

    it('should invoke listener when sendMessage completes', async () => {
      const { result } = renderHook(() => useInlineChat(), { wrapper });

      const callback = vi.fn();

      act(() => {
        result.current.startSession('model', 'sales.customers', 'customers');
        result.current.registerListener('sales.customers', callback);
      });

      const sessionId = result.current.activeSession!.id;

      await act(async () => {
        await result.current.sendMessage(sessionId, 'Test');
      });

      expect(callback).toHaveBeenCalledWith('Inline mock response');

      // Clean up
      act(() => {
        result.current.unregisterListener('sales.customers', callback);
      });
    });
  });
});
