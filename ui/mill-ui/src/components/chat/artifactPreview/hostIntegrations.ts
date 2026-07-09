import type { ReactNode } from 'react';
import type { ChatMessageArtifact } from '../../../types/chat';
import type { InlineChatContextType } from '../../../types/inlineChat';
import type { ChatType } from './types';

export type HostApplyHandler = (artifact: Extract<ChatMessageArtifact, { kind: 'sql' }>) => void;

export type InlineHostSqlAction =
  | { type: 'sql.apply'; artifact: Extract<ChatMessageArtifact, { kind: 'sql' }> }
  | { type: 'sql.applyAndRun'; artifact: Extract<ChatMessageArtifact, { kind: 'sql' }> }
  | { type: 'sql.copy'; artifact: Extract<ChatMessageArtifact, { kind: 'sql' }> };

/** Discriminated host actions dispatched from inline artifact strips. */
export type InlineHostAction = InlineHostSqlAction;

export type InlineHostActionHandler = (action: InlineHostAction) => boolean;

export function resolveInlineChatType(contextType: InlineChatContextType): ChatType {
  return `inline-${contextType}` as ChatType;
}

const inlineHostHandlers: Partial<Record<ChatType, InlineHostActionHandler>> = {};

/** Registers the host handler for a chat type (one handler per type). */
export function registerInlineHostHandler(chatType: ChatType, handler: InlineHostActionHandler): void {
  inlineHostHandlers[chatType] = handler;
}

/** Dispatches a typed host action; returns whether a handler consumed it. */
export function dispatchInlineHostAction(chatType: ChatType, action: InlineHostAction): boolean {
  const handler = inlineHostHandlers[chatType];
  if (!handler) return false;
  return handler(action);
}

/**
 * @deprecated Use {@link registerInlineHostHandler} with `sql.apply` actions.
 */
export function registerHostApplyHandler(chatType: ChatType, handler: HostApplyHandler): void {
  registerInlineHostHandler(chatType, (action) => {
    if (action.type === 'sql.apply') {
      handler(action.artifact);
      return true;
    }
    return false;
  });
}

/**
 * @deprecated Use {@link dispatchInlineHostAction}.
 */
export function applyArtifactToHost(chatType: ChatType, artifact: ChatMessageArtifact): boolean {
  if (artifact.kind !== 'sql') return false;
  return dispatchInlineHostAction(chatType, { type: 'sql.apply', artifact });
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
