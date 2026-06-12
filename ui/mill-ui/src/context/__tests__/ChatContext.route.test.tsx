import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import type { ReactNode } from 'react';
import { ChatProvider, useChat } from '../ChatContext';

vi.mock('../../services/api', () => ({
  chatService: {
    async createChat() {
      return { chatId: crypto.randomUUID(), chatName: 'New Chat' };
    },
    async listAgentProfiles() {
      return [
        { id: 'data-analysis', capabilityIds: ['sql.query'] },
        { id: 'hello-world', capabilityIds: ['conversation.general'] },
      ];
    },
    async *sendMessage(_conversationId: string, _message: string) {
      yield 'Mock AI response';
    },
  },
}));

beforeEach(() => {
  localStorage.clear();
});

function wrapper({ children }: { children: ReactNode }) {
  return <ChatProvider>{children}</ChatProvider>;
}

describe('ChatContext route selection', () => {
  it('createConversation should set the newest chat active without route churn', async () => {
    const { result } = renderHook(() => useChat(), { wrapper });

    await act(async () => {
      await result.current.createConversation();
    });
    await act(async () => {
      await result.current.createConversation();
    });

    const newestId = result.current.state.conversations[0]!.id;
    expect(result.current.state.activeConversationId).toBe(newestId);
  });
});
