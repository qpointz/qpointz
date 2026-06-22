import type { ChatMessageArtifact } from '../types/chat';
import { parseFacetProposalArtifact } from './facetWireNormalize';

const STRUCTURED_PRESENTATION = 'structured';
/** Mirrors [V1_CONVERSATION_PRESENTATION] — keep literal to avoid importing chatTransport (cycles). */
const V1_CONVERSATION_PRESENTATION = 'conversation' as const;

const KNOWN_STRUCTURED_PART_TYPES = new Set(['sql', 'data', 'facet-proposal', 'schema-capture']);

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
  if (typeof o.executionId === 'string' && o.executionId.trim().length > 0) return 'data';
  if (typeof o.sql === 'string' && o.sql.trim().length > 0) return 'sql';
  if (parseFacetProposalArtifact(o)) return 'facet-proposal';
  return null;
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

  if (effectivePartType === 'data' || inferredKind === 'data') {
    const executionId = typeof parsed.executionId === 'string' ? parsed.executionId : '';
    if (!executionId) return null;
    return {
      kind: 'data',
      executionId,
      sql: typeof parsed.sql === 'string' ? parsed.sql : undefined,
      rowCount: typeof parsed.rowCount === 'number' ? parsed.rowCount : undefined,
      truncated: typeof parsed.truncated === 'boolean' ? parsed.truncated : undefined,
      columns: Array.isArray(parsed.columns)
        ? parsed.columns
            .map((entry) => {
              if (!entry || typeof entry !== 'object') return null;
              const row = entry as Record<string, unknown>;
              const name = typeof row.name === 'string' ? row.name : '';
              const type = typeof row.type === 'string' ? row.type : 'unknown';
              return name ? { name, type } : null;
            })
            .filter((column): column is { name: string; type: string } => column !== null)
        : undefined,
    };
  }

  if (
    effectivePartType === 'facet-proposal' ||
    effectivePartType === 'schema-capture' ||
    inferredKind === 'facet-proposal'
  ) {
    return parseFacetProposalArtifact(parsed);
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
