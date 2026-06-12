import { describe, it, expect } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import type { ReactNode } from 'react';
import { ChatExpandProvider, useChatExpand } from '../useChatExpand';
import type { Message } from '../../../../types/chat';

function wrapper({ children }: { children: ReactNode }) {
  return <ChatExpandProvider>{children}</ChatExpandProvider>;
}

const message: Message = {
  id: 'msg-1',
  conversationId: 'conv-1',
  role: 'assistant',
  content: 'Here is your query',
  timestamp: Date.now(),
};

describe('useChatExpand', () => {
  it('should open and close expand payload', () => {
    const { result } = renderHook(() => useChatExpand(), { wrapper });

    expect(result.current.expand).toBeNull();

    act(() => {
      result.current.openExpand({
        messageId: 'msg-1',
        turnId: 'turn-1',
        chatType: 'general',
        kind: 'sql-data-composite',
        sql: 'SELECT 1',
        message,
      });
    });

    expect(result.current.expand?.sql).toBe('SELECT 1');

    act(() => {
      result.current.closeExpand();
    });

    expect(result.current.expand).toBeNull();
  });
});
