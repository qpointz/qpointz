import { describe, it, expect } from 'vitest';
import {
  deriveSuggestedDescription,
  deriveSuggestedName,
} from '../useOpenInAnalysis';
import type { Message } from '../../../../types/chat';

function makeMessage(content: string): Message {
  return {
    id: 'm1',
    conversationId: 'c1',
    role: 'assistant',
    content,
    timestamp: Date.now(),
  };
}

describe('useOpenInAnalysis helpers', () => {
  it('should prefer preceding user question for suggested name', () => {
    const name = deriveSuggestedName('SELECT 1', makeMessage(''), 'Top customers by revenue');
    expect(name).toBe('Top customers by revenue');
  });

  it('should truncate long suggested names', () => {
    const longQuestion = 'x'.repeat(100);
    const name = deriveSuggestedName('SELECT 1', makeMessage(''), longQuestion);
    expect(name.length).toBeLessThanOrEqual(80);
    expect(name.endsWith('…')).toBe(true);
  });

  it('should derive description from question and chat title when distinct from name', () => {
    const description = deriveSuggestedDescription(
      'Show revenue',
      'Sales chat',
      'Other title',
    );
    expect(description).toBe('Show revenue — from Sales chat');
  });

  it('should omit description when question is empty', () => {
    const description = deriveSuggestedDescription(undefined, 'Sales', 'Show revenue');
    expect(description).toBeUndefined();
  });
});
