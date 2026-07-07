import { describe, expect, it } from 'vitest';
import type { Conversation } from '../../types/chat';
import { conversationNeedsTranscriptReplay } from '../chatTranscriptHydration';

function makeConversation(overrides: Partial<Conversation> = {}): Conversation {
  return {
    id: 'chat-1',
    title: 'Test',
    createdAt: 0,
    updatedAt: 0,
    messages: [],
    ...overrides,
  };
}

describe('conversationNeedsTranscriptReplay', () => {
  it('shouldRequestFetch_whenTranscriptNotHydrated', () => {
    const conv = makeConversation({ transcriptHydrated: false });
    expect(conversationNeedsTranscriptReplay(conv, false)).toBe(true);
  });

  it('shouldNotRequestFetch_whenTranscriptHydratedEvenWithHollowAssistant', () => {
    const conv = makeConversation({
      transcriptHydrated: true,
      messages: [
        {
          id: 'u1',
          conversationId: 'chat-1',
          role: 'user',
          content: 'hello',
          timestamp: 1,
        },
        {
          id: 'a1',
          conversationId: 'chat-1',
          role: 'assistant',
          content: '',
          timestamp: 2,
        },
      ],
    });
    expect(conversationNeedsTranscriptReplay(conv, false)).toBe(false);
  });

  it('shouldNotRequestFetch_whileAssistantStreamIsLoading', () => {
    const conv = makeConversation({ transcriptHydrated: false });
    expect(conversationNeedsTranscriptReplay(conv, true)).toBe(false);
  });
});
