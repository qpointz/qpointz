import type { Conversation } from '../types/chat';

/**
 * True when the active chat still needs a one-time `getChatDetail` transcript load.
 * After {@link Conversation.transcriptHydrated} is set, local message/artifact edits must not
 * trigger another full-history fetch.
 */
export function conversationNeedsTranscriptReplay(conv: Conversation, isLoading: boolean): boolean {
  if (isLoading) return false;
  return conv.transcriptHydrated === false;
}
