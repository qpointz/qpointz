import type { ChatMessageArtifact } from '../types/chat';

const STRUCTURED_PRESENTATION = 'structured';
/** Mirrors [V1_CONVERSATION_PRESENTATION] — keep literal to avoid importing chatTransport (cycles). */
const V1_CONVERSATION_PRESENTATION = 'conversation' as const;

/**
 * True when `content` looks like the JSON object string we put on the wire for SQL / facet artefacts
 * (avoids appending it to the V1 markdown bubble if `presentation` / `partType` were lost in transit).
 */
export function sseItemPartContentLooksLikeStructuredArtifact(content: string): boolean {
  const t = content.trim();
  if (!t.startsWith('{')) return false;
  try {
    const parsed = JSON.parse(t) as unknown;
    if (parsed === null || typeof parsed !== 'object') return false;
    const o = parsed as Record<string, unknown>;
    if (typeof o.sql === 'string' && o.sql.trim().length > 0) return true;
    if (typeof o.facetTypeKey === 'string' && typeof o.metadataEntityId === 'string') return true;
    return false;
  } catch {
    return false;
  }
}

function wirePartType(evt: Record<string, unknown>): string {
  if (typeof evt.partType === 'string') return evt.partType;
  if (typeof evt.part_type === 'string') return evt.part_type;
  return '';
}

function wirePresentation(evt: Record<string, unknown>): string {
  if (typeof evt.presentation === 'string') return evt.presentation;
  return '';
}

/**
 * Maps a non–V1 `item.part.updated` SSE row to a normalized in-chat artifact, if recognized.
 */
export function parseChatStructuredPart(evt: Record<string, unknown>): ChatMessageArtifact | null {
  const rawContent = typeof evt.content === 'string' ? evt.content : '';
  if (!rawContent) return null;

  let parsed: unknown;
  try {
    parsed = JSON.parse(rawContent) as unknown;
  } catch {
    return null;
  }
  if (parsed === null || typeof parsed !== 'object') return null;
  const o = parsed as Record<string, unknown>;

  const presentation = wirePresentation(evt);
  const declaredPartType = wirePartType(evt);
  const fromPayloadShape =
    (typeof o.sql === 'string' && o.sql.trim() ? 'sql' : '') ||
    (typeof o.facetTypeKey === 'string' && typeof o.metadataEntityId === 'string' ? 'facet-proposal' : '');
  /** Declared sql/facet wins; generic `text` / empty defers to JSON shape when allowed below. */
  const effectivePartType =
    declaredPartType === 'sql' || declaredPartType === 'facet-proposal' ? declaredPartType : fromPayloadShape;

  const presentationOk =
    presentation === STRUCTURED_PRESENTATION || presentation === '' || presentation === V1_CONVERSATION_PRESENTATION;
  if (!presentationOk) return null;

  const allowByShape =
    presentation === STRUCTURED_PRESENTATION ||
    sseItemPartContentLooksLikeStructuredArtifact(rawContent);

  if (!allowByShape) return null;

  if (effectivePartType === 'sql' || declaredPartType === 'sql') {
    const sql = typeof o.sql === 'string' ? o.sql : '';
    if (!sql.trim()) return null;
    return {
      kind: 'sql',
      sql,
      dialectId: typeof o.dialectId === 'string' ? o.dialectId : undefined,
    };
  }

  if (effectivePartType === 'facet-proposal' || declaredPartType === 'facet-proposal') {
    const facetTypeKey = typeof o.facetTypeKey === 'string' ? o.facetTypeKey : '';
    const metadataEntityId = typeof o.metadataEntityId === 'string' ? o.metadataEntityId : '';
    if (!facetTypeKey || !metadataEntityId) return null;
    const payload =
      'serializedPayload' in o ? o.serializedPayload : 'payload' in o ? o.payload : undefined;
    return {
      kind: 'facet-proposal',
      facetTypeKey,
      metadataEntityId,
      payload,
    };
  }

  return null;
}
