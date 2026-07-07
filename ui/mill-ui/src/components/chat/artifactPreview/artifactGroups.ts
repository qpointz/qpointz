import type { ChatMessageArtifact } from '../../../types/chat';
import type { ArtifactRenderGroup, ArtefactKind, SqlDataCompositeGroup } from './types';

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

function dataArtifactsMatch(
  left: Extract<ChatMessageArtifact, { kind: 'data' }>,
  right: Extract<ChatMessageArtifact, { kind: 'data' }>,
): boolean {
  if (left.sourceArtifactId && right.sourceArtifactId) {
    return left.sourceArtifactId === right.sourceArtifactId;
  }
  if (left.sourceArtifactId || right.sourceArtifactId) {
    return false;
  }
  const leftSql = normalizedSql(left.sql);
  const rightSql = normalizedSql(right.sql);
  return Boolean(leftSql) && leftSql === rightSql;
}

/**
 * Collapses flat message artefacts into render groups.
 * SQL + optional data become one `sql-data-composite` card (paired by `sourceArtifactId` or SQL text).
 * Repeated `data` rows for the same SQL (e.g. multiple attach/auto-run results) keep the latest only.
 */
export function groupMessageArtifacts(artifacts: readonly ChatMessageArtifact[] | undefined): ArtifactRenderGroup[] {
  if (!artifacts?.length) return [];

  const groups: ArtifactRenderGroup[] = [];
  const sqlComposites: SqlDataCompositeGroup[] = [];

  for (const artifact of artifacts) {
    if (artifact.kind === 'sql') {
      const composite: SqlDataCompositeGroup = {
        kind: 'sql-data-composite',
        sql: artifact,
      };
      sqlComposites.push(composite);
      groups.push(composite);
      continue;
    }
    if (artifact.kind === 'data') {
      const target = [...sqlComposites].reverse().find(
        (composite) => composite.sql && dataMatchesSql(artifact, composite.sql),
      );
      if (target) {
        // Latest data wins — do not open extra "Query results:" cards for the same SQL.
        target.data = artifact;
        continue;
      }
      const orphan = [...groups].reverse().find(
        (group): group is SqlDataCompositeGroup =>
          group.kind === 'sql-data-composite' &&
          !group.sql &&
          group.data != null &&
          dataArtifactsMatch(group.data, artifact),
      );
      if (orphan) {
        orphan.data = artifact;
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
