import type { AssistantReplyView, ChatMessageArtifact } from '../types/chat';

/**
 * Parses optional GET-transcript field from [TurnResponseWire.assistantReplyView].
 */
export function assistantReplyViewFromWire(raw: string | null | undefined): AssistantReplyView | undefined {
  if (
    raw === 'conversation' ||
    raw === 'sql-primary' ||
    raw === 'facet-primary' ||
    raw === 'schema-primary' ||
    raw === 'artifact-primary'
  ) {
    return raw;
  }
  return undefined;
}

/**
 * Derives the layout for an assistant message from structured artefacts and/or the end-of-turn
 * `item.completed` summary (presentation/partType) emitted by [ChatRuntimeEventToSseMapper].
 */
export function deriveAssistantReplyView(
  artifacts: readonly ChatMessageArtifact[] | undefined,
  completionHint?: { readonly presentation: string; readonly partType: string } | null,
): AssistantReplyView {
  const arts = artifacts ?? [];
  if (arts.some((a) => a.kind === 'facet-proposal')) return 'facet-primary';
  if (arts.some((a) => a.kind === 'schema-capture')) return 'schema-primary';
  if (arts.some((a) => a.kind === 'sql')) return 'sql-primary';
  if (arts.some((a) => a.kind === 'unknown')) return 'artifact-primary';
  if (completionHint?.presentation === 'structured') {
    if (completionHint.partType === 'sql') return 'sql-primary';
    if (completionHint.partType === 'facet-proposal') return 'facet-primary';
    if (completionHint.partType === 'schema-capture') return 'schema-primary';
    if (completionHint.partType && completionHint.partType !== 'text') return 'artifact-primary';
  }
  return 'conversation';
}

/** Section label for structured assistant replies. */
export function structuredReplySectionTitle(view: AssistantReplyView): string | null {
  switch (view) {
    case 'sql-primary':
      return 'SQL';
    case 'facet-primary':
      return 'Facet proposal';
    case 'schema-primary':
      return 'Schema capture';
    case 'artifact-primary':
      return 'Artifact';
    default:
      return null;
  }
}
