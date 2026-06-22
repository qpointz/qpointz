import type { ChatMessageArtifact } from '../../../types/chat';
import type { ArtifactRenderGroup, ArtefactKind } from './types';

/**
 * Collapses flat message artefacts into render groups.
 * SQL + optional data become one `sql-data-composite` card.
 */
export function groupMessageArtifacts(artifacts: readonly ChatMessageArtifact[] | undefined): ArtifactRenderGroup[] {
  if (!artifacts?.length) return [];

  const groups: ArtifactRenderGroup[] = [];
  let pendingSql: Extract<ChatMessageArtifact, { kind: 'sql' }> | undefined;
  let pendingData: Extract<ChatMessageArtifact, { kind: 'data' }> | undefined;

  const flushSqlComposite = () => {
    if (!pendingSql && !pendingData) return;
    groups.push({
      kind: 'sql-data-composite',
      sql: pendingSql,
      data: pendingData,
    });
    pendingSql = undefined;
    pendingData = undefined;
  };

  for (const artifact of artifacts) {
    if (artifact.kind === 'sql') {
      flushSqlComposite();
      pendingSql = artifact;
      continue;
    }
    if (artifact.kind === 'data') {
      pendingData = artifact;
      if (pendingSql) {
        flushSqlComposite();
      }
      continue;
    }
    if (artifact.kind === 'facet-proposal') {
      flushSqlComposite();
      groups.push({ kind: 'facet-proposal', facet: artifact });
      continue;
    }
  }
  flushSqlComposite();
  return groups;
}

export function groupKindLabel(kind: ArtefactKind): string {
  if (kind === 'sql-data-composite') return 'SQL';
  return 'Facet proposal';
}
