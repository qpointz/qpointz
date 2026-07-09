import { describe, expect, it } from 'vitest';
import { resolveSqlArtifactApplyKey } from '../inlineSqlArtifactKey';
import type { ChatMessageArtifact } from '../../../../types/chat';

describe('resolveSqlArtifactApplyKey', () => {
  it('should prefer durable artifactId', () => {
    const artifact: ChatMessageArtifact = {
      kind: 'sql',
      sql: 'SELECT 1',
      artifactId: 'sql-42',
    };
    expect(resolveSqlArtifactApplyKey('msg-1', artifact)).toBe('sql-42');
  });

  it('should distinguish identical SQL in different messages by ordinal', () => {
    const first: ChatMessageArtifact = { kind: 'sql', sql: 'SELECT 1' };
    const second: ChatMessageArtifact = { kind: 'sql', sql: 'SELECT 1' };
    const artifacts = [first, second];

    expect(resolveSqlArtifactApplyKey('msg-a', first, { messageArtifacts: artifacts })).toBe('msg-a:sql:0');
    expect(resolveSqlArtifactApplyKey('msg-b', first, { messageArtifacts: artifacts })).toBe('msg-b:sql:0');
    expect(resolveSqlArtifactApplyKey('msg-a', second, { messageArtifacts: artifacts })).toBe('msg-a:sql:1');
  });

  it('should fall back to proposal index for auto-apply', () => {
    const artifact: ChatMessageArtifact = { kind: 'sql', sql: 'SELECT 1' };
    expect(resolveSqlArtifactApplyKey('msg-1', artifact, { proposalIndex: 2 })).toBe('msg-1:sql:2');
  });
});
