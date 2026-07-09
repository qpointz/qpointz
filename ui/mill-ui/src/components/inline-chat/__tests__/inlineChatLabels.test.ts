import { describe, expect, it } from 'vitest';
import type { InlineChatSession } from '../../../types/inlineChat';
import {
  getInlineChatDrawerSubtitle,
  getInlineChatDrawerTitle,
  getInlineChatEmptyState,
  inlineChatShowsEmptyState,
} from '../inlineChatLabels';

function makeSession(overrides: Partial<InlineChatSession> = {}): InlineChatSession {
  return {
    id: 's1',
    chatId: null,
    contextType: 'analysis',
    contextId: '__analysis__',
    contextLabel: 'Top orders',
    messages: [],
    isLoading: false,
    thinkingMessage: null,
    createdAt: 0,
    settings: { 'automation.mode': 'manual' },
    ...overrides,
  };
}

describe('inlineChatLabels', () => {
  it('should use Analysis copilot title for analysis sessions', () => {
    expect(getInlineChatDrawerTitle(makeSession())).toBe('Analysis copilot');
    expect(getInlineChatDrawerSubtitle(makeSession())).toBe('Top orders');
  });

  it('should expose analysis starter prompts', () => {
    const empty = getInlineChatEmptyState(makeSession());
    expect(empty.suggestions).toContain('Optimize this query');
    expect(empty.suggestions.length).toBeGreaterThan(0);
  });

  it('should show empty state until first user message', () => {
    expect(inlineChatShowsEmptyState(makeSession({ messages: [] }))).toBe(true);
    expect(
      inlineChatShowsEmptyState(
        makeSession({
          messages: [
            {
              id: 'g',
              conversationId: 's1',
              role: 'assistant',
              content: 'Hello',
              timestamp: 0,
            },
          ],
        }),
      ),
    ).toBe(true);
    expect(
      inlineChatShowsEmptyState(
        makeSession({
          messages: [
            {
              id: 'u',
              conversationId: 's1',
              role: 'user',
              content: 'Hi',
              timestamp: 0,
            },
          ],
        }),
      ),
    ).toBe(false);
  });
});
