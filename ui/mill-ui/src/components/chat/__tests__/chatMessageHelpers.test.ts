import { describe, it, expect } from 'vitest';
import { isPendingAssistantReply } from '../chatMessageHelpers';
import type { Message } from '../../../types/chat';

function msg(overrides: Partial<Message> & Pick<Message, 'id' | 'role'>): Message {
  return {
    conversationId: 'c1',
    content: '',
    timestamp: 0,
    ...overrides,
  };
}

describe('isPendingAssistantReply', () => {
  it('should be true for empty loading assistant tail', () => {
    const messages = [
      msg({ id: 'u1', role: 'user', content: 'Hi' }),
      msg({ id: 'a1', role: 'assistant', content: '' }),
    ];
    expect(isPendingAssistantReply(messages[1]!, 1, messages, true)).toBe(true);
  });

  it('should be false once assistant content arrives', () => {
    const messages = [
      msg({ id: 'u1', role: 'user', content: 'Hi' }),
      msg({ id: 'a1', role: 'assistant', content: 'Done' }),
    ];
    expect(isPendingAssistantReply(messages[1]!, 1, messages, true)).toBe(false);
  });

  it('should be false once assistant artefacts arrive', () => {
    const messages = [
      msg({ id: 'u1', role: 'user', content: 'Hi' }),
      msg({
        id: 'a1',
        role: 'assistant',
        content: '',
        artifacts: [{ kind: 'sql', sql: 'select 1' }],
      }),
    ];
    expect(isPendingAssistantReply(messages[1]!, 1, messages, true)).toBe(false);
  });
});
