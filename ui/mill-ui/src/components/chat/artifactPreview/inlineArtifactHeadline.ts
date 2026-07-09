import type { ChatMessageArtifact } from '../../../types/chat';
import { facetStripTitle, sqlStripTitle } from './inlineArtifactStripLabels';

const DEFAULT_HEADLINE_MAX = 40;

function truncateHeadline(text: string, max = DEFAULT_HEADLINE_MAX): string {
  const trimmed = text.trim();
  if (trimmed.length <= max) return trimmed;
  return `${trimmed.slice(0, max - 1)}…`;
}

function readPayloadName(payload: unknown): string | undefined {
  if (!payload || typeof payload !== 'object' || !('name' in payload)) return undefined;
  const name = (payload as { name?: unknown }).name;
  return typeof name === 'string' && name.trim() ? name.trim() : undefined;
}

/** Single-line SQL headline for inline pill strips (no `.sql` suffix). */
export function inlineSqlHeadline(
  sqlArtifact: Extract<ChatMessageArtifact, { kind: 'sql' }> | undefined,
  chatTitle?: string,
): string {
  const raw = sqlStripTitle(sqlArtifact, chatTitle);
  const withoutSuffix = raw.replace(/\.sql$/i, '');
  return truncateHeadline(withoutSuffix);
}

/** Single-line facet headline for inline pill strips. */
export function inlineFacetHeadline(
  facet: Extract<ChatMessageArtifact, { kind: 'facet-proposal' }>,
  facetTypeTitle?: string,
): string {
  const conceptName = readPayloadName(facet.payload);
  if (conceptName) return truncateHeadline(conceptName);

  const entity = facet.catalogPath?.trim() || facet.metadataEntityId?.trim();
  if (entity) {
    const tail = entity.includes('.') ? entity.split('.').pop() ?? entity : entity;
    return truncateHeadline(tail);
  }

  const fallback = facetStripTitle(facet, facetTypeTitle);
  return truncateHeadline(fallback);
}
