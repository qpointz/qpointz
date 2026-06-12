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
