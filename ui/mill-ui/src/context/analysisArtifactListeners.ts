import type { ChatMessageArtifact } from '../types/chat';
import type { AnalysisCopilotSettings } from '../types/inlineChat';

export type AnalysisArtifactListener = (
  artifact: Extract<ChatMessageArtifact, { kind: 'sql' }>,
  meta: {
    sessionId: string;
    messageId: string;
    proposalIndex: number;
    settings: AnalysisCopilotSettings;
  },
) => void;

const listeners = new Map<string, Set<AnalysisArtifactListener>>();

function contextKey(contextId: string): string {
  return `analysis:${contextId}`;
}

/** Subscribe to SQL artifacts streamed for an Analysis inline chat context. */
export function registerAnalysisArtifactListener(
  contextId: string,
  listener: AnalysisArtifactListener,
): void {
  const key = contextKey(contextId);
  if (!listeners.has(key)) {
    listeners.set(key, new Set());
  }
  listeners.get(key)!.add(listener);
}

/** Unsubscribe from Analysis SQL artifact events. */
export function unregisterAnalysisArtifactListener(
  contextId: string,
  listener: AnalysisArtifactListener,
): void {
  const set = listeners.get(contextKey(contextId));
  if (!set) return;
  set.delete(listener);
  if (set.size === 0) {
    listeners.delete(contextKey(contextId));
  }
}

/** Notifies listeners when a SQL artifact is appended for an Analysis context. */
export function notifyAnalysisSqlArtifact(
  contextId: string,
  artifact: Extract<ChatMessageArtifact, { kind: 'sql' }>,
  meta: {
    sessionId: string;
    messageId: string;
    proposalIndex: number;
    settings: AnalysisCopilotSettings;
  },
): void {
  const set = listeners.get(contextKey(contextId));
  if (!set) return;
  set.forEach((listener) => {
    try {
      listener(artifact, meta);
    } catch {
      /* listener errors must not break chat */
    }
  });
}
