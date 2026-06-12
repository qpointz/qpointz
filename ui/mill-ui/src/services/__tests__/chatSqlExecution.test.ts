import { describe, it, expect } from 'vitest';
import { collectChatSqlTargets, mergeDataArtifactIntoMessage } from '../chatSqlExecution';
import type { Message } from '../../types/chat';

function assistant(id: string, artifacts: Message['artifacts']): Message {
  return {
    id,
    conversationId: 'c1',
    role: 'assistant',
    content: '',
    timestamp: 0,
    artifacts,
  };
}

describe('collectChatSqlTargets', () => {
  it('should collect SQL from assistant turns bottom-up', () => {
    const messages = [
      assistant('a1', [{ kind: 'sql', sql: 'select 1' }]),
      assistant('a2', [{ kind: 'sql', sql: 'select 2' }]),
    ];
    expect(collectChatSqlTargets(messages)).toEqual([
      { messageId: 'a2', sql: 'select 2' },
      { messageId: 'a1', sql: 'select 1' },
    ]);
  });

  it('should fall back to data artefact SQL', () => {
    const messages = [
      assistant('a1', [{ kind: 'data', executionId: 'e1', sql: 'select old', columns: [] }]),
    ];
    expect(collectChatSqlTargets(messages)).toEqual([{ messageId: 'a1', sql: 'select old' }]);
  });

  it('should skip turns without SQL', () => {
    const messages = [
      assistant('a1', [
        { kind: 'facet-proposal', facetTypeKey: 'descriptive', metadataEntityId: 'e1', payload: {} },
      ]),
    ];
    expect(collectChatSqlTargets(messages)).toEqual([]);
  });
});

describe('mergeDataArtifactIntoMessage', () => {
  it('should replace existing data artefact', () => {
    const merged = mergeDataArtifactIntoMessage(
      [
        { kind: 'sql', sql: 'select 1' },
        { kind: 'data', executionId: 'old', sql: 'select 1', columns: [] },
      ],
      { kind: 'data', executionId: 'new', sql: 'select 1', columns: [{ name: 'x', type: 'INT' }] },
    );
    expect(merged.filter((a) => a.kind === 'data')).toHaveLength(1);
    expect(merged.find((a) => a.kind === 'data')?.executionId).toBe('new');
  });
});
