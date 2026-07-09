import type { ChatMessageArtifact } from '../../../types/chat';
import type { ArtifactRenderGroup } from './types';

/** Display label for an inline SQL artifact strip. */
export function sqlStripTitle(
  sqlArtifact: Extract<ChatMessageArtifact, { kind: 'sql' }> | undefined,
  chatTitle?: string,
): string {
  const named = sqlArtifact?.info?.title?.trim();
  if (named) return `${named}.sql`;
  const fromChat = chatTitle?.trim();
  if (fromChat) return `${fromChat}.sql`;
  return 'Generated query.sql';
}

/** Optional description shown under the SQL strip title. */
export function sqlStripDescription(
  sqlArtifact: Extract<ChatMessageArtifact, { kind: 'sql' }> | undefined,
): string | undefined {
  const description = sqlArtifact?.info?.description?.trim();
  return description || undefined;
}

/** Lightweight execution metadata for sql-data-composite strips (no grid). */
export function sqlStripExecutionMeta(group: Extract<ArtifactRenderGroup, { kind: 'sql-data-composite' }>): string | undefined {
  const data = group.data;
  if (!data) return undefined;
  const parts: string[] = [];
  if (typeof data.rowCount === 'number') {
    parts.push(`${data.rowCount.toLocaleString()} row${data.rowCount === 1 ? '' : 's'}`);
  }
  if (data.truncated) {
    parts.push('truncated');
  }
  return parts.length ? parts.join(' · ') : undefined;
}

/** Display label for a facet proposal inline strip. */
export function facetStripTitle(
  facet: Extract<ChatMessageArtifact, { kind: 'facet-proposal' }>,
  facetTypeTitle?: string,
): string {
  const typeLabel = facetTypeTitle?.trim() || facet.facetTypeKey || 'Facet';
  const entity = facet.catalogPath?.trim() || facet.metadataEntityId?.trim();
  if (entity) return `${typeLabel}: ${entity}`;
  return typeLabel;
}
