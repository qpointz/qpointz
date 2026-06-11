import type { ChatMessageArtifact } from '../types/chat';

const STRUCTURED_PRESENTATION = 'structured';
/** Mirrors [V1_CONVERSATION_PRESENTATION] — keep literal to avoid importing chatTransport (cycles). */
const V1_CONVERSATION_PRESENTATION = 'conversation' as const;

const KNOWN_STRUCTURED_PART_TYPES = new Set(['sql', 'facet-proposal', 'schema-capture']);

function isRecord(value: unknown): value is Record<string, unknown> {
  return value !== null && typeof value === 'object' && !Array.isArray(value);
}

/**
 * True when `content` looks like the JSON object string we put on the wire for structured artefacts
 * (avoids appending it to the V1 markdown bubble if `presentation` / `partType` were lost in transit).
 */
export function sseItemPartContentLooksLikeStructuredArtifact(content: string): boolean {
  const t = content.trim();
  if (!t.startsWith('{')) return false;
  try {
    const parsed = JSON.parse(t) as unknown;
    if (!isRecord(parsed)) return false;
    return inferPayloadKind(parsed) !== null;
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

function inferPayloadKind(o: Record<string, unknown>): ChatMessageArtifact['kind'] | null {
  if (typeof o.sql === 'string' && o.sql.trim().length > 0) return 'sql';
  if (typeof o.facetTypeKey === 'string' && typeof o.metadataEntityId === 'string') return 'facet-proposal';
  if (typeof o.captureType === 'string' && o.captureType === 'facet_assignment' && typeof o.metadataEntityId === 'string') {
    return 'facet-proposal';
  }
  if (typeof o.captureType === 'string' && typeof o.targetEntityId === 'string') return 'schema-capture';
  return null;
}

function payloadFromObject(o: Record<string, unknown>): unknown {
  if ('serializedPayload' in o) return o.serializedPayload;
  if ('payload' in o) return o.payload;
  return o;
}

function titleForUnknown(o: Record<string, unknown>, partType: string): string {
  if (typeof o.artifactType === 'string' && o.artifactType.trim()) return o.artifactType;
  if (typeof o.captureType === 'string' && o.captureType.trim()) return o.captureType;
  if (partType.trim()) return partType;
  return 'Structured artifact';
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
  if (!isRecord(parsed)) return null;

  const presentation = wirePresentation(evt);
  const declaredPartType = wirePartType(evt);
  const inferredKind = inferPayloadKind(parsed);

  const effectivePartType =
    KNOWN_STRUCTURED_PART_TYPES.has(declaredPartType) ? declaredPartType : inferredKind ?? declaredPartType;

  const presentationOk =
    presentation === STRUCTURED_PRESENTATION || presentation === '' || presentation === V1_CONVERSATION_PRESENTATION;
  if (!presentationOk) return null;

  const allowByShape =
    presentation === STRUCTURED_PRESENTATION || sseItemPartContentLooksLikeStructuredArtifact(rawContent);
  if (!allowByShape) return null;

  if (effectivePartType === 'sql' || inferredKind === 'sql') {
    const sql = typeof parsed.sql === 'string' ? parsed.sql : '';
    if (!sql.trim()) return null;
    return {
      kind: 'sql',
      sql,
      dialectId: typeof parsed.dialectId === 'string' ? parsed.dialectId : undefined,
    };
  }

  if (effectivePartType === 'facet-proposal' || inferredKind === 'facet-proposal') {
    const facetTypeKey = typeof parsed.facetTypeKey === 'string' ? parsed.facetTypeKey : '';
    const metadataEntityId = typeof parsed.metadataEntityId === 'string' ? parsed.metadataEntityId : '';
    if (!facetTypeKey || !metadataEntityId) return null;
    return {
      kind: 'facet-proposal',
      facetTypeKey,
      metadataEntityId,
      payload: payloadFromObject(parsed),
    };
  }

  if (effectivePartType === 'schema-capture' || inferredKind === 'schema-capture') {
    const captureType = typeof parsed.captureType === 'string' ? parsed.captureType : '';
    const targetEntityId = typeof parsed.targetEntityId === 'string' ? parsed.targetEntityId : '';
    if (!captureType || !targetEntityId) return null;
    return {
      kind: 'schema-capture',
      captureType,
      targetEntityId,
      targetEntityType: typeof parsed.targetEntityType === 'string' ? parsed.targetEntityType : undefined,
      payload: payloadFromObject(parsed),
    };
  }

  if (presentation === STRUCTURED_PRESENTATION) {
    const partType = declaredPartType || 'unknown';
    return {
      kind: 'unknown',
      partType,
      title: titleForUnknown(parsed, partType),
      payload: parsed,
    };
  }

  return null;
}
