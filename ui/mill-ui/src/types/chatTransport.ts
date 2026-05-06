/**
 * AI v3 chat SSE transport helpers (browser).
 *
 * @see ../../../../docs/design/agentic/ai-v3-chat-transport-extensions.md — artefact/extension mapping.
 */

import { sseItemPartContentLooksLikeStructuredArtifact } from '../utils/chatArtifactParse';

/** V1 main-bubble frozen `presentation` (conversational markdown/text). */
export const V1_CONVERSATION_PRESENTATION = 'conversation' as const;

/** V1 main-bubble frozen `partType` on `item.part.updated`. */
export const V1_TEXT_PART_TYPE = 'text' as const;

/** Known discriminator strings on SSE JSON today (extras are tolerated). */
export type ChatV3SseTypeKnown =
  | 'item.created'
  | 'item.part.updated'
  | 'item.completed'
  | 'item.failed'
  | 'item.tool.call'
  | 'item.tool.result'
  | 'item.diagnostic';

/** Minimal wire row shape after `JSON.parse` of an SSE `data:` payload. */
export type ChatV3SseWire = Record<string, unknown>;

/** @future Artefact payloads will narrow these stubs (SQL / charts / facets). */
export interface SqlArtifactStub {
  readonly kind: 'sql';
  readonly sqlText: string;
}

export interface DataArtifactStub {
  readonly kind: 'data';
  readonly columns: readonly string[];
}

export interface ChartArtifactStub {
  readonly kind: 'chart';
  readonly chartSpecUri?: string;
}

export type ArtefactDraftStub =
  | SqlArtifactStub
  | DataArtifactStub
  | ChartArtifactStub;

/**
 * Reducer-friendly envelope for forwarded structured `item.part.updated` rows that do **not**
 * participate in the main conversational text bubble.
 */
export type ChatStructuredPartForward = Readonly<{
  itemId: string;
  presentation: string;
  partType: string;
  mode: string;
  payload: ChatV3SseWire;
}>;

/** True when the row should contribute to accumulated assistant markdown/text (WI-229 V1 bubble). */
export function isV1MainConversationTextPart(evt: ChatV3SseWire): boolean {
  const r = evt as Record<string, unknown>;
  const presentationRaw = r.presentation;
  const partTypeRaw = typeof r.partType === 'string' ? r.partType : typeof r.part_type === 'string' ? r.part_type : undefined;

  const presentation =
    typeof presentationRaw === 'string' ? presentationRaw : V1_CONVERSATION_PRESENTATION;
  const partType = partTypeRaw ?? V1_TEXT_PART_TYPE;

  if (presentation !== V1_CONVERSATION_PRESENTATION || partType !== V1_TEXT_PART_TYPE) {
    return false;
  }

  const content = typeof r.content === 'string' ? r.content : '';
  if (sseItemPartContentLooksLikeStructuredArtifact(content)) {
    return false;
  }
  return true;
}

/**
 * Snapshot for artefact/extension pipelines; returns `null` if `itemId` is absent (transport fault).
 */
export function summarizeStructuredPartForward(evt: ChatV3SseWire): ChatStructuredPartForward | null {
  const itemId = typeof evt.itemId === 'string' ? evt.itemId : '';
  if (!itemId) return null;

  const record = evt as Record<string, unknown>;
  const presentationRaw = record.presentation;
  const partTypeRaw =
    typeof record.partType === 'string'
      ? record.partType
      : typeof record.part_type === 'string'
        ? record.part_type
        : undefined;

  const presentation =
    typeof presentationRaw === 'string' ? presentationRaw : V1_CONVERSATION_PRESENTATION;
  const partType = partTypeRaw ?? V1_TEXT_PART_TYPE;
  const mode = typeof evt.mode === 'string' ? evt.mode : 'append';

  return Object.freeze({
    itemId,
    presentation,
    partType,
    mode,
    payload: evt,
  });
}
