import type { AssistantReplyView, ChatMessageArtifact } from '../types/chat';

/**
 * Parses optional GET-transcript field from [TurnResponseWire.assistantReplyView].
 */
export function assistantReplyViewFromWire(raw: string | null | undefined): AssistantReplyView | undefined {
  if (raw === 'conversation' || raw === 'sql-primary' || raw === 'facet-primary') return raw;
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
  if (arts.some((a) => a.kind === 'sql')) return 'sql-primary';
  if (completionHint?.presentation === 'structured') {
    if (completionHint.partType === 'sql') return 'sql-primary';
    if (completionHint.partType === 'facet-proposal') return 'facet-primary';
  }
  return 'conversation';
}
