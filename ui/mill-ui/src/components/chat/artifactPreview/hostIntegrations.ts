import type { ReactNode } from 'react';
import type { ChatMessageArtifact } from '../../../types/chat';
import type { InlineChatContextType } from '../../../types/inlineChat';
import type { ChatType } from './types';

export type HostApplyHandler = (artifact: Extract<ChatMessageArtifact, { kind: 'sql' }>) => void;

export function resolveInlineChatType(contextType: InlineChatContextType): ChatType {
  return `inline-${contextType}` as ChatType;
}

const hostApplyHandlers: Partial<Record<ChatType, HostApplyHandler>> = {};

export function registerHostApplyHandler(chatType: ChatType, handler: HostApplyHandler): void {
  hostApplyHandlers[chatType] = handler;
}

export function applyArtifactToHost(chatType: ChatType, artifact: ChatMessageArtifact): boolean {
  if (artifact.kind !== 'sql') return false;
  const handler = hostApplyHandlers[chatType];
  if (!handler) return false;
  handler(artifact);
  return true;
}

export function HostIntegrationRegistrar({
  chatType,
  onSql,
}: {
  chatType: ChatType;
  onSql: HostApplyHandler;
}) {
  registerHostApplyHandler(chatType, onSql);
  return null as ReactNode;
}
