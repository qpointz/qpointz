import type { Message } from '../../types/chat';

/** Assistant turn still waiting for structured reply content or artefacts. */
export function isPendingAssistantReply(
  message: Message,
  index: number,
  messages: readonly Message[],
  isLoading: boolean,
): boolean {
  if (!isLoading || index !== messages.length - 1) return false;
  if (message.role !== 'assistant') return false;
  if (message.content.trim()) return false;
  if (message.artifacts?.length) return false;
  return true;
}

/**
 * Stable transcript signature for auto-scroll — excludes facet accept/reject status so
 * lifecycle actions do not yank the viewport to the bottom.
 */
export function buildChatScrollSignature(messages: readonly Message[]): string {
  return messages
    .map((message) => {
      const artifactParts =
        message.artifacts?.map((artifact) => {
          if (artifact.kind === 'sql') {
            return `sql:${artifact.sql.length}:${artifact.info?.title ?? ''}`;
          }
          if (artifact.kind === 'facet-proposal') {
            return `facet:${artifact.artifactId ?? artifact.facetTypeKey}:${artifact.metadataEntityId}`;
          }
          if (artifact.kind === 'data') {
            return `data:${artifact.rowCount ?? 0}:${artifact.executionId ?? ''}`;
          }
          return artifact.kind;
        }) ?? [];
      return `${message.id}|${message.role}|${message.content.length}|${artifactParts.join(';')}`;
    })
    .join('||');
}
