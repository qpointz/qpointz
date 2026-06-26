import { describe, it, expect } from 'vitest';
import { collectChatSqlTargets, mergeDataArtifactIntoMessage, mergeRunAllDataArtifacts } from '../chatSqlExecution';
import type { ChatMessageArtifact, Message } from '../../types/chat';

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

  it('should collect multiple SQL artefacts per turn', () => {
    const messages = [
      assistant('a1', [
        { kind: 'sql', sql: 'select 1', artifactId: 'sql-1' },
        { kind: 'sql', sql: 'select 2', artifactId: 'sql-2' },
      ]),
    ];
    expect(collectChatSqlTargets(messages)).toEqual([
      { messageId: 'a1', sql: 'select 2', parentArtifactId: 'sql-2' },
      { messageId: 'a1', sql: 'select 1', parentArtifactId: 'sql-1' },
    ]);
  });
});

describe('mergeDataArtifactIntoMessage', () => {
  it('should replace existing data artefact for the same SQL text', () => {
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

  it('should replace data artefact bound to parent SQL only', () => {
    const merged = mergeDataArtifactIntoMessage(
      [
        { kind: 'sql', sql: 'select 1', artifactId: 'sql-1' },
        { kind: 'data', executionId: 'old', sql: 'select 1', columns: [], sourceArtifactId: 'sql-1' },
        { kind: 'data', executionId: 'keep', sql: 'select 2', columns: [], sourceArtifactId: 'sql-2' },
      ],
      {
        kind: 'data',
        executionId: 'new',
        sql: 'select 1',
        columns: [],
        sourceArtifactId: 'sql-1',
      },
    );
    expect(merged.filter((a) => a.kind === 'data')).toHaveLength(2);
    const sql1Data = merged.find(
      (a): a is Extract<ChatMessageArtifact, { kind: 'data' }> =>
        a.kind === 'data' && a.sourceArtifactId === 'sql-1',
    );
    const sql2Data = merged.find(
      (a): a is Extract<ChatMessageArtifact, { kind: 'data' }> =>
        a.kind === 'data' && a.sourceArtifactId === 'sql-2',
    );
    expect(sql1Data?.executionId).toBe('new');
    expect(sql2Data?.executionId).toBe('keep');
  });

  it('should keep other SQL data when merging without sourceArtifactId', () => {
    const merged = mergeDataArtifactIntoMessage(
      [
        { kind: 'sql', sql: 'SELECT * FROM aircraft' },
        { kind: 'sql', sql: 'SELECT * FROM aircraft_types' },
        {
          kind: 'data',
          executionId: 'types',
          sql: 'SELECT * FROM aircraft_types',
          columns: [],
        },
      ],
      {
        kind: 'data',
        executionId: 'aircraft',
        sql: 'SELECT * FROM aircraft',
        columns: [],
      },
    );
    expect(merged.filter((a) => a.kind === 'data')).toHaveLength(2);
    expect(
      merged.find(
        (a): a is Extract<ChatMessageArtifact, { kind: 'data' }> =>
          a.kind === 'data' && a.executionId === 'types',
      ),
    ).toBeDefined();
    expect(
      merged.find(
        (a): a is Extract<ChatMessageArtifact, { kind: 'data' }> =>
          a.kind === 'data' && a.executionId === 'aircraft',
      ),
    ).toBeDefined();
  });
});

describe('mergeRunAllDataArtifacts', () => {
  it('should keep both data artefacts when merging run-all results on one turn', () => {
    const merged = mergeRunAllDataArtifacts(
      [
        { kind: 'sql', sql: 'SELECT * FROM aircraft' },
        { kind: 'sql', sql: 'SELECT * FROM aircraft_types' },
      ],
      [
        { kind: 'data', executionId: 'e-types', sql: 'SELECT * FROM aircraft_types', columns: [] },
        { kind: 'data', executionId: 'e-aircraft', sql: 'SELECT * FROM aircraft', columns: [] },
      ],
    );
    expect(merged.filter((a) => a.kind === 'data')).toHaveLength(2);
  });
});
