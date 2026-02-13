import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import type { ReactNode } from 'react';
import { ChatProvider, useChat } from '../ChatContext';

// Mock the chat service to yield a single chunk instantly
vi.mock('../../services/api', () => ({
  chatService: {
    async createChat() {
      return { chatId: crypto.randomUUID(), chatName: 'New Chat' };
    },
    async *sendMessage(_conversationId: string, _message: string) {
      yield 'Mock AI response';
    },
  },
}));

// Prevent localStorage noise between tests
beforeEach(() => {
  localStorage.clear();
});

function wrapper({ children }: { children: ReactNode }) {
  return <ChatProvider>{children}</ChatProvider>;
}

describe('ChatContext', () => {
  describe('useChat outside provider', () => {
    it('should throw when used outside ChatProvider', () => {
      // Suppress console.error for this test
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
      expect(() => {
        renderHook(() => useChat());
      }).toThrow('useChat must be used within a ChatProvider');
      consoleSpy.mockRestore();
    });
  });

  describe('initial state', () => {
    it('should start with empty conversations and no active conversation', () => {
      const { result } = renderHook(() => useChat(), { wrapper });
      expect(result.current.state.conversations).toHaveLength(0);
      expect(result.current.state.activeConversationId).toBeNull();
      expect(result.current.state.isLoading).toBe(false);
      expect(result.current.activeConversation).toBeNull();
    });
  });

  describe('createConversation', () => {
    it('should create a new conversation and set it active', async () => {
      const { result } = renderHook(() => useChat(), { wrapper });

      await act(async () => {
        await result.current.createConversation();
      });

      expect(result.current.state.conversations).toHaveLength(1);
      expect(result.current.state.activeConversationId).toBe(
        result.current.state.conversations[0]!.id,
      );
      expect(result.current.activeConversation).not.toBeNull();
      expect(result.current.activeConversation!.title).toBe('New Chat');
      expect(result.current.activeConversation!.messages).toHaveLength(0);
    });

    it('should create multiple conversations and set latest as active', async () => {
      const { result } = renderHook(() => useChat(), { wrapper });

      await act(async () => {
        await result.current.createConversation();
      });
      const firstId = result.current.state.conversations[0]!.id;

      await act(async () => {
        await result.current.createConversation();
      });

      expect(result.current.state.conversations).toHaveLength(2);
      // Newest is prepended, so active should be the new one
      expect(result.current.state.activeConversationId).not.toBe(firstId);
    });
  });

  describe('deleteConversation', () => {
    it('should delete a conversation', async () => {
      const { result } = renderHook(() => useChat(), { wrapper });

      await act(async () => {
        await result.current.createConversation();
      });
      const id = result.current.state.conversations[0]!.id;

      act(() => {
        result.current.deleteConversation(id);
      });

      expect(result.current.state.conversations).toHaveLength(0);
      expect(result.current.state.activeConversationId).toBeNull();
    });

    it('should switch active to another conversation when active is deleted', async () => {
      const { result } = renderHook(() => useChat(), { wrapper });

      await act(async () => {
        await result.current.createConversation();
      });
      await act(async () => {
        await result.current.createConversation();
      });

      const activeId = result.current.state.activeConversationId!;
      act(() => {
        result.current.deleteConversation(activeId);
      });

      expect(result.current.state.conversations).toHaveLength(1);
      expect(result.current.state.activeConversationId).not.toBeNull();
      expect(result.current.state.activeConversationId).not.toBe(activeId);
    });
  });

  describe('setActiveConversation', () => {
    it('should switch the active conversation', async () => {
      const { result } = renderHook(() => useChat(), { wrapper });

      await act(async () => {
        await result.current.createConversation();
      });
      await act(async () => {
        await result.current.createConversation();
      });

      const firstId = result.current.state.conversations[1]!.id; // oldest is at index 1
      act(() => {
        result.current.setActiveConversation(firstId);
      });

      expect(result.current.state.activeConversationId).toBe(firstId);
    });
  });

  describe('renameConversation', () => {
    it('should rename a conversation', async () => {
      const { result } = renderHook(() => useChat(), { wrapper });

      await act(async () => {
        await result.current.createConversation();
      });
      const id = result.current.state.conversations[0]!.id;

      act(() => {
        result.current.renameConversation(id, 'My Custom Title');
      });

      expect(result.current.activeConversation!.title).toBe('My Custom Title');
    });
  });

  describe('sendMessage', () => {
    it('should add user message and receive AI response', async () => {
      const { result } = renderHook(() => useChat(), { wrapper });

      await act(async () => {
        await result.current.createConversation();
      });

      await act(async () => {
        await result.current.sendMessage('Hello, AI!');
      });

      const conv = result.current.activeConversation!;
      expect(conv.messages.length).toBeGreaterThanOrEqual(2);

      // First message is user
      expect(conv.messages[0]!.role).toBe('user');
      expect(conv.messages[0]!.content).toBe('Hello, AI!');

      // Second message is assistant with mocked response
      expect(conv.messages[1]!.role).toBe('assistant');
      expect(conv.messages[1]!.content).toBe('Mock AI response');
    });

    it('should update conversation title from first user message', async () => {
      const { result } = renderHook(() => useChat(), { wrapper });

      await act(async () => {
        await result.current.createConversation();
      });

      expect(result.current.activeConversation!.title).toBe('New Chat');

      await act(async () => {
        await result.current.sendMessage('How does SQL work?');
      });

      expect(result.current.activeConversation!.title).toBe('How does SQL work?');
    });

    it('should auto-create a conversation when sending with no active conversation', async () => {
      const { result } = renderHook(() => useChat(), { wrapper });

      // No conversation created explicitly
      expect(result.current.state.conversations).toHaveLength(0);

      await act(async () => {
        await result.current.sendMessage('Hello');
      });

      // A conversation should have been auto-created
      expect(result.current.state.conversations).toHaveLength(1);
      expect(result.current.activeConversation).not.toBeNull();

      // The temp ID should have been replaced with a real UUID (no "temp-" prefix)
      const conv = result.current.activeConversation!;
      expect(conv.id).not.toMatch(/^temp-/);

      // All messages should reference the real conversation ID
      for (const msg of conv.messages) {
        expect(msg.conversationId).toBe(conv.id);
      }

      // The user message + assistant response should be in the new conversation
      expect(conv.messages.length).toBeGreaterThanOrEqual(2);
      expect(conv.messages[0]!.content).toBe('Hello');
    });

    it('should create a new conversation when newConversation option is set, even if one is active', async () => {
      const { result } = renderHook(() => useChat(), { wrapper });

      // Create a conversation and send a message to it
      await act(async () => {
        await result.current.createConversation();
      });
      const firstConvId = result.current.state.activeConversationId;
      expect(firstConvId).not.toBeNull();

      await act(async () => {
        await result.current.sendMessage('First message');
      });
      expect(result.current.state.conversations).toHaveLength(1);

      // Now send with newConversation: true — should create a second conversation
      await act(async () => {
        await result.current.sendMessage('Search query from global search', { newConversation: true });
      });

      expect(result.current.state.conversations).toHaveLength(2);
      // Active conversation should be the new one, not the original
      expect(result.current.state.activeConversationId).not.toBe(firstConvId);
      // The new conversation should have the search query message
      const newConv = result.current.activeConversation!;
      expect(newConv.messages[0]!.content).toBe('Search query from global search');
    });

    it('should set loading during message processing', async () => {
      const { result } = renderHook(() => useChat(), { wrapper });

      await act(async () => {
        await result.current.createConversation();
      });

      // After sendMessage resolves, loading should be false
      await act(async () => {
        await result.current.sendMessage('Test');
      });

      expect(result.current.state.isLoading).toBe(false);
    });
  });

  describe('clearAllConversations', () => {
    it('should remove all conversations', async () => {
      const { result } = renderHook(() => useChat(), { wrapper });

      await act(async () => {
        await result.current.createConversation();
      });
      await act(async () => {
        await result.current.createConversation();
      });
      await act(async () => {
        await result.current.createConversation();
      });

      expect(result.current.state.conversations).toHaveLength(3);

      act(() => {
        result.current.clearAllConversations();
      });

      expect(result.current.state.conversations).toHaveLength(0);
      expect(result.current.state.activeConversationId).toBeNull();
      expect(result.current.state.isLoading).toBe(false);
    });
  });

  describe('localStorage persistence', () => {
    it('should save conversations to localStorage', async () => {
      const { result } = renderHook(() => useChat(), { wrapper });

      await act(async () => {
        await result.current.createConversation();
      });

      const stored = localStorage.getItem('chat-conversations');
      expect(stored).not.toBeNull();
      const parsed = JSON.parse(stored!);
      expect(parsed).toHaveLength(1);
    });
  });
});
