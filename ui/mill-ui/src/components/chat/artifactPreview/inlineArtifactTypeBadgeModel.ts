import type { ArtefactKind } from './types';

export type InlineArtifactTypeCode = 'SQL' | 'DQ' | 'C' | 'AI' | 'M';

export interface InlineArtifactTypeBadgeInput {
  kind: ArtefactKind;
  facetTypeKey?: string;
  facetCategory?: string | null;
}

/** Mantine color token per indicative type code. */
export const INLINE_ARTIFACT_TYPE_COLORS: Record<InlineArtifactTypeCode, string> = {
  SQL: 'teal',
  DQ: 'orange',
  C: 'grape',
  AI: 'violet',
  M: 'gray',
};

const TYPE_LABELS: Record<InlineArtifactTypeCode, string> = {
  SQL: 'SQL',
  DQ: 'Data quality',
  C: 'Concept',
  AI: 'AI',
  M: 'Metadata',
};

/** Full tooltip label for an inline artifact type badge. */
export function inlineArtifactTypeLabel(code: InlineArtifactTypeCode): string {
  return TYPE_LABELS[code];
}

function normalizeSlug(value: string): string {
  return value.trim().toLowerCase();
}

/**
 * Resolves a short indicative badge code for inline artifact strips.
 * First match wins; documented priority for extensibility.
 */
export function resolveInlineArtifactTypeBadge(
  input: InlineArtifactTypeBadgeInput,
): InlineArtifactTypeCode {
  if (input.kind === 'sql-data-composite') {
    return 'SQL';
  }

  const slug = normalizeSlug(input.facetTypeKey ?? '');
  const category = normalizeSlug(input.facetCategory ?? '');

  if (slug === 'concept' || slug.endsWith(':concept') || slug.includes('concept')) {
    return 'C';
  }
  if (category === 'data-quality' || slug.startsWith('dq-') || slug.includes('data-quality')) {
    return 'DQ';
  }
  if (category === 'ai' || slug.startsWith('ai-') || slug.includes(':ai')) {
    return 'AI';
  }
  return 'M';
}
