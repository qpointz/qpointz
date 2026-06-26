import type { ChatMessageArtifact } from '../../../types/chat';
import type { ArtifactRenderGroup, ArtefactKind } from './types';

function normalizedSql(sql: string | undefined): string {
  return (sql ?? '').trim();
}

function dataMatchesSql(
  data: Extract<ChatMessageArtifact, { kind: 'data' }>,
  sql: Extract<ChatMessageArtifact, { kind: 'sql' }> | undefined,
): boolean {
  if (!sql) return false;
  if (data.sourceArtifactId && sql.artifactId) {
    return data.sourceArtifactId === sql.artifactId;
  }
  if (data.sourceArtifactId || sql.artifactId) {
    return false;
  }
  return normalizedSql(data.sql) === normalizedSql(sql.sql);
}

/**
 * Collapses flat message artefacts into render groups.
 * SQL + optional data become one `sql-data-composite` card (paired by `sourceArtifactId` or SQL text).
 */
export function groupMessageArtifacts(artifacts: readonly ChatMessageArtifact[] | undefined): ArtifactRenderGroup[] {
  if (!artifacts?.length) return [];

  const groups: ArtifactRenderGroup[] = [];
  const sqlComposites: Extract<ArtifactRenderGroup, { kind: 'sql-data-composite' }>[] = [];

  for (const artifact of artifacts) {
    if (artifact.kind === 'sql') {
      const composite: Extract<ArtifactRenderGroup, { kind: 'sql-data-composite' }> = {
        kind: 'sql-data-composite',
        sql: artifact,
      };
      sqlComposites.push(composite);
      groups.push(composite);
      continue;
    }
    if (artifact.kind === 'data') {
      const target = [...sqlComposites].reverse().find(
        (composite) => composite.sql && !composite.data && dataMatchesSql(artifact, composite.sql),
      );
      if (target) {
        target.data = artifact;
      } else {
        groups.push({ kind: 'sql-data-composite', data: artifact });
      }
      continue;
    }
    if (artifact.kind === 'facet-proposal') {
      groups.push({ kind: 'facet-proposal', facet: artifact });
      continue;
    }
  }

  return groups;
}

export function groupKindLabel(kind: ArtefactKind): string {
  if (kind === 'sql-data-composite') return 'SQL';
  return 'Facet proposal';
}
