import type { InlineChatContextType } from '../types/inlineChat';

/** Returns ephemeral turn context values for an inline chat host. */
export type InlineContextSnapshotProvider = () => Record<string, unknown>;

const providers = new Map<string, InlineContextSnapshotProvider>();

function registryKey(contextType: InlineChatContextType, contextId: string): string {
  return `${contextType}:${contextId}`;
}

/** Registers a snapshot provider for a specific inline chat context. */
export function registerInlineContextSnapshotProvider(
  contextType: InlineChatContextType,
  contextId: string,
  provider: InlineContextSnapshotProvider,
): void {
  providers.set(registryKey(contextType, contextId), provider);
}

/** Removes a snapshot provider (call on unmount or context change). */
export function unregisterInlineContextSnapshotProvider(
  contextType: InlineChatContextType,
  contextId: string,
): void {
  providers.delete(registryKey(contextType, contextId));
}

/** Collects the latest snapshot for a turn, if a provider is registered. */
export function collectInlineContextSnapshot(
  contextType: InlineChatContextType,
  contextId: string,
): Record<string, unknown> | undefined {
  const provider = providers.get(registryKey(contextType, contextId));
  if (!provider) return undefined;
  const values = provider();
  return Object.keys(values).length > 0 ? values : undefined;
}
