import type { AssistantReplySegment, ChatMessageArtifact, Message } from '../types/chat';
import { groupMessageArtifacts } from '../components/chat/artifactPreview/artifactGroups';
import type { ArtifactRenderGroup } from '../components/chat/artifactPreview/types';
import { facetEntityCatalogPath } from './metadataEntityDisplay';

function facetPayloadRecord(payload: unknown): Record<string, unknown> | null {
  if (payload === null || typeof payload !== 'object' || Array.isArray(payload)) return null;
  return payload as Record<string, unknown>;
}

function facetCatalogPath(facet: Extract<ChatMessageArtifact, { kind: 'facet-proposal' }>): string {
  return facetEntityCatalogPath(facet.catalogPath, facet.metadataEntityId);
}

function facetTypeLabel(facetTypeKey: string): string {
  const key = facetTypeKey.trim().toLowerCase();
  if (key === 'descriptive') return 'descriptive facet';
  if (key === 'relation') return 'relation facet';
  if (key.endsWith('-check') || key.includes('quality')) return `${facetTypeKey} check`;
  return `${facetTypeKey} facet`;
}

/**
 * Short natural-language lead-in shown above a single artefact group when no streamed text
 * segment was captured for that position.
 */
export function commentaryForArtifactGroup(group: ArtifactRenderGroup): string | null {
  if (group.kind === 'facet-proposal') {
    const facet = group.facet;
    const path = facetCatalogPath(facet);
    const rationale = facet.rationale?.trim();
    if (rationale) {
      return rationale;
    }
    const payload = facetPayloadRecord(facet.payload);
    const description = typeof payload?.description === 'string' ? payload.description.trim() : '';
    const label = facetTypeLabel(facet.facetTypeKey);
    if (description) {
      return `For **${path}**: ${label} with the description "${description}".`;
    }
    return `For **${path}**: ${label}.`;
  }

  if (group.kind === 'sql-data-composite') {
    if (group.sql && group.data) {
      return 'Query results:';
    }
    if (group.sql) {
      return 'Generated SQL:';
    }
    if (group.data) {
      return 'Query results:';
    }
  }

  return null;
}

const BULK_SUMMARY_PATTERN =
  /\b(successfully assigned|following facets?|i(?:'ve| have) assigned|here are the facets)\b/i;

function looksLikeBulkFacetSummary(text: string): boolean {
  const trimmed = text.trim();
  if (!trimmed) return false;
  if (BULK_SUMMARY_PATTERN.test(trimmed)) return true;
  const forMatches = trimmed.match(/\bfor\s+[\w.]+\.[\w.]+/gi) ?? [];
  return forMatches.length >= 2;
}

function extractQuestionTail(text: string): string | null {
  const paragraphs = text
    .split(/\n\s*\n/)
    .map((p) => p.trim())
    .filter(Boolean);
  for (let i = paragraphs.length - 1; i >= 0; i -= 1) {
    if (paragraphs[i]?.includes('?')) {
      return paragraphs.slice(i).join('\n\n');
    }
  }
  return null;
}

/**
 * Returns trailing assistant prose after per-artefact commentary when the main body is a bulk summary.
 */
export function extractTrailingCommentary(
  content: string,
  artifactGroupCount: number,
): string | null {
  const trimmed = content.trim();
  if (!trimmed || artifactGroupCount === 0) {
    return trimmed || null;
  }

  if (!looksLikeBulkFacetSummary(trimmed)) {
    return null;
  }

  const questionTail = extractQuestionTail(trimmed);
  return questionTail;
}

/**
 * True when a structured `data` artefact should not open a new interleaved segment (merged into SQL).
 */
export function shouldAppendArtifactSegment(
  artifact: ChatMessageArtifact,
  artifactsBefore: readonly ChatMessageArtifact[],
): boolean {
  if (artifact.kind !== 'data') return true;
  const groupsBefore = groupMessageArtifacts(artifactsBefore);
  const groupsAfter = groupMessageArtifacts([...artifactsBefore, artifact]);
  return groupsAfter.length > groupsBefore.length;
}

/**
 * Group index for a newly appended artefact after grouping (flat list → render groups).
 */
export function groupIndexForNewArtifact(
  artifact: ChatMessageArtifact,
  allArtifacts: readonly ChatMessageArtifact[],
): number {
  const groups = groupMessageArtifacts(allArtifacts);
  if (artifact.kind === 'data') {
    const match = groups.findIndex(
      (group) =>
        group.kind === 'sql-data-composite' &&
        group.data?.executionId === artifact.executionId,
    );
    if (match >= 0) return match;
  }
  return Math.max(0, groups.length - 1);
}

function deriveReplySegments(message: Message): AssistantReplySegment[] {
  const groups = groupMessageArtifacts(message.artifacts);
  if (!groups.length) {
    const text = message.content.trim();
    return text ? [{ kind: 'text', text }] : [];
  }

  const segments: AssistantReplySegment[] = [];
  groups.forEach((group, groupIndex) => {
    const commentary = commentaryForArtifactGroup(group);
    if (commentary) {
      segments.push({ kind: 'text', text: commentary });
    }
    segments.push({ kind: 'artifact', groupIndex });
  });

  const trailing = extractTrailingCommentary(message.content, groups.length);
  if (trailing) {
    segments.push({ kind: 'text', text: trailing });
  }

  return segments;
}

/**
 * Ordered text and artefact segments for assistant replies (live SSE or GET replay).
 */
export function buildReplySegments(message: Message): AssistantReplySegment[] {
  if (message.replySegments?.length) {
    return [...message.replySegments];
  }
  return deriveReplySegments(message);
}

/**
 * Whether the message should use interleaved artefact layout (structured artefacts present).
 */
export function usesInterleavedArtifactLayout(message: Message): boolean {
  const groups = groupMessageArtifacts(message.artifacts);
  return groups.length > 0 || Boolean(message.replySegments?.length);
}
