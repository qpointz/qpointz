import { describe, expect, it } from 'vitest';
import { buildChatScrollSignature } from '../chatMessageHelpers';
import type { Message } from '../../../types/chat';

describe('buildChatScrollSignature', () => {
  it('should ignore facet status changes', () => {
    const facet = {
      kind: 'facet-proposal' as const,
      facetTypeKey: 'descriptive',
      metadataEntityId: 'sales.orders',
      artifactId: 'art-1',
      status: 'active',
      payload: {},
    };
    const baseMessage: Message = {
      id: 'm1',
      conversationId: 'c1',
      role: 'assistant',
      content: '',
      timestamp: 0,
      artifacts: [facet],
    };
    const base: Message[] = [baseMessage];
    const rejected: Message[] = [
      {
        ...baseMessage,
        artifacts: [{ ...facet, status: 'rejected' }],
      },
    ];

    expect(buildChatScrollSignature(base)).toBe(buildChatScrollSignature(rejected));
  });

  it('should change when assistant content streams in', () => {
    const emptyMessage: Message = {
      id: 'm1',
      conversationId: 'c1',
      role: 'assistant',
      content: '',
      timestamp: 0,
    };
    const empty: Message[] = [emptyMessage];
    const partial: Message[] = [{ ...emptyMessage, content: 'Hello' }];
    expect(buildChatScrollSignature(empty)).not.toBe(buildChatScrollSignature(partial));
  });
});
