import { describe, expect, it, vi, beforeEach } from 'vitest';
import { renderHook, act, waitFor } from '@testing-library/react';
import type { ReactNode } from 'react';
import { ChatProvider, useChat } from '../ChatContext';

const CHAT_A = '449cc26a-108d-4170-b0b6-6fecf09c2ce0';
const CHAT_B = '550dd37b-219e-5281-c1c7-7fed1a0d3df1';

const getChatDetailMock = vi.fn();

vi.mock('../../services/chatService', () => ({
  isRestChatBackendActive: () => true,
}));

vi.mock('../../services/api', () => ({
  chatService: {
    async listChats() {
      return [
        {
          chatId: CHAT_A,
          chatName: 'Countries',
          updatedAt: Date.now(),
        },
        {
          chatId: CHAT_B,
          chatName: 'Cities',
          updatedAt: Date.now() - 1000,
        },
      ];
    },
    async listAgentProfiles() {
      return [{ id: 'data-analysis', capabilityIds: ['sql.query'] }];
    },
    getChatDetail: (...args: unknown[]) => getChatDetailMock(...args),
    async createChat() {
      return { chatId: crypto.randomUUID(), chatName: 'New Chat' };
    },
    async *sendMessage() {
      yield 'ok';
    },
  },
}));

function wrapper({ children }: { children: ReactNode }) {
  return <ChatProvider>{children}</ChatProvider>;
}

function detailFor(chatId: string, chatName: string, userText: string) {
  return {
    chat: {
      chatId,
      chatName,
      updatedAt: new Date().toISOString(),
      profileId: 'data-analysis',
    },
    messages: [
      {
        turnId: `t-user-${chatId}`,
        role: 'user',
        text: userText,
        createdAt: new Date().toISOString(),
        artifacts: [],
      },
      {
        turnId: `t-assistant-${chatId}`,
        role: 'assistant',
        text: '',
        createdAt: new Date().toISOString(),
        artifacts: [
          {
            artifactId: `sql-${chatId}`,
            kind: 'sql.generated',
            payload: { sql: { text: 'SELECT 1' } },
          },
        ],
      },
    ],
  };
}

describe('ChatContext transcript hydration', () => {
  beforeEach(() => {
    getChatDetailMock.mockReset();
    getChatDetailMock.mockImplementation(async (chatId: string) => {
      if (chatId === CHAT_B) {
        return detailFor(CHAT_B, 'Cities', 'top cities');
      }
      return detailFor(CHAT_A, 'Countries', 'top countries');
    });
  });

  it('shouldFetchTranscriptOnce_whenActiveChatNeedsHydration', async () => {
    const { result } = renderHook(() => useChat(), { wrapper });

    await waitFor(() => {
      expect(result.current.initialized).toBe(true);
    });

    await act(async () => {
      result.current.setActiveConversation(CHAT_A);
    });

    await waitFor(() => {
      expect(getChatDetailMock).toHaveBeenCalledTimes(1);
    });

    await act(async () => {
      result.current.updateMessageArtifacts(CHAT_A, `t-assistant-${CHAT_A}`, [
        { kind: 'data', executionId: 'exec-1', sql: 'SELECT 1', rowCount: 1 },
      ]);
    });

    await act(async () => {
      await new Promise((resolve) => window.setTimeout(resolve, 50));
    });

    expect(getChatDetailMock).toHaveBeenCalledTimes(1);
  });

  it('shouldLoadTranscript_whenSelectingAnotherChatByName', async () => {
    const { result } = renderHook(() => useChat(), { wrapper });

    await waitFor(() => {
      expect(result.current.initialized).toBe(true);
    });

    await act(async () => {
      result.current.setActiveConversation(CHAT_A);
    });

    await waitFor(() => {
      expect(result.current.activeConversation?.messages.some((m) => m.content === 'top countries')).toBe(
        true,
      );
    });

    await act(async () => {
      result.current.setActiveConversation(CHAT_B);
    });

    await waitFor(() => {
      expect(result.current.activeConversation?.id).toBe(CHAT_B);
      expect(result.current.activeConversation?.messages.some((m) => m.content === 'top cities')).toBe(
        true,
      );
    });

    expect(getChatDetailMock).toHaveBeenCalledWith(CHAT_B);
  });

  it('shouldLoadTranscript_whenPriorFetchWasCancelledByReselect', async () => {
    let releaseFirst: (() => void) | undefined;
    const firstGate = new Promise<void>((resolve) => {
      releaseFirst = resolve;
    });

    getChatDetailMock.mockImplementation(async (chatId: string) => {
      if (chatId === CHAT_A) {
        await firstGate;
        return detailFor(CHAT_A, 'Countries', 'top countries');
      }
      return detailFor(CHAT_B, 'Cities', 'top cities');
    });

    const { result } = renderHook(() => useChat(), { wrapper });

    await waitFor(() => {
      expect(result.current.initialized).toBe(true);
    });

    await act(async () => {
      result.current.setActiveConversation(CHAT_A);
    });

    await waitFor(() => {
      expect(getChatDetailMock).toHaveBeenCalledWith(CHAT_A);
    });

    await act(async () => {
      result.current.setActiveConversation(CHAT_B);
    });

    await waitFor(() => {
      expect(result.current.activeConversation?.messages.some((m) => m.content === 'top cities')).toBe(
        true,
      );
    });

    await act(async () => {
      releaseFirst?.();
    });

    await act(async () => {
      result.current.setActiveConversation(CHAT_A);
    });

    await waitFor(() => {
      expect(result.current.activeConversation?.id).toBe(CHAT_A);
      expect(result.current.activeConversation?.messages.some((m) => m.content === 'top countries')).toBe(
        true,
      );
    });
  });
});
