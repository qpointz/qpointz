import type { ChatMessageArtifact } from '../../../types/chat';

type SqlArtifact = Extract<ChatMessageArtifact, { kind: 'sql' }>;

/**
 * Stable UI key for “this SQL proposal was applied” — never derived from SQL text alone.
 * Prefers durable `artifactId`, then message-local SQL ordinal, then stream proposal index.
 */
export function resolveSqlArtifactApplyKey(
  messageId: string,
  artifact: SqlArtifact,
  options?: {
    messageArtifacts?: readonly ChatMessageArtifact[];
    proposalIndex?: number;
  },
): string {
  const artifactId = artifact.artifactId?.trim();
  if (artifactId) {
    return artifactId;
  }

  const artifacts = options?.messageArtifacts;
  if (artifacts?.length) {
    let sqlOrdinal = 0;
    for (const entry of artifacts) {
      if (entry === artifact) {
        return `${messageId}:sql:${sqlOrdinal}`;
      }
      if (entry.kind === 'sql') {
        sqlOrdinal += 1;
      }
    }
  }

  const proposalIndex = options?.proposalIndex;
  if (proposalIndex != null && proposalIndex >= 0) {
    return `${messageId}:sql:${proposalIndex}`;
  }

  return `${messageId}:sql:0`;
}
