import type { Message, ChatMessageArtifact } from '../types/chat';
import type { QueryResult } from '../types/query';
import { chatService, queryService } from './api';
import { isRestChatBackendActive } from './chatService';
import { readStoredQueryPageSize } from './queryService';

export interface ChatSqlTarget {
  messageId: string;
  sql: string;
}

export interface ChatSqlExecutionResult {
  dataArtifact: Extract<ChatMessageArtifact, { kind: 'data' }>;
  result: QueryResult;
}

/**
 * Collects executable SQL from assistant turns, ordered from most recent (bottom) upward.
 */
export function collectChatSqlTargets(messages: readonly Message[]): ChatSqlTarget[] {
  const targets: ChatSqlTarget[] = [];
  const seen = new Set<string>();

  for (let i = messages.length - 1; i >= 0; i -= 1) {
    const message = messages[i];
    if (message?.role !== 'assistant') continue;

    const sqlArtifact = message.artifacts?.find(
      (artifact): artifact is Extract<ChatMessageArtifact, { kind: 'sql' }> => artifact.kind === 'sql',
    );
    const dataArtifact = message.artifacts?.find(
      (artifact): artifact is Extract<ChatMessageArtifact, { kind: 'data' }> => artifact.kind === 'data',
    );
    const sql = (sqlArtifact?.sql ?? dataArtifact?.sql ?? '').trim();
    if (!sql) continue;

    const dedupeKey = `${message.id}:${sql}`;
    if (seen.has(dedupeKey)) continue;
    seen.add(dedupeKey);
    targets.push({ messageId: message.id, sql });
  }

  return targets;
}

/**
 * Executes SQL for a chat turn and returns the wire `data` artefact (also attaches on REST backend).
 */
export async function executeChatSqlArtifact(
  conversationId: string,
  messageId: string,
  sql: string,
): Promise<ChatSqlExecutionResult> {
  const trimmed = sql.trim();
  if (!trimmed) {
    throw new Error('SQL is empty');
  }

  const pageSize = readStoredQueryPageSize();
  const result = await queryService.executeQuery(trimmed, { pageSize });

  const dataArtifact: Extract<ChatMessageArtifact, { kind: 'data' }> = {
    kind: 'data',
    executionId: result.page.executionId,
    sql: trimmed,
    rowCount: result.rowCount,
    truncated: result.page.totalResult != null && result.rowCount < result.page.totalResult,
    columns: result.columns,
  };

  if (isRestChatBackendActive()) {
    await chatService
      .attachExecutionResult(conversationId, messageId, {
        executionId: result.page.executionId,
        columns: result.columns,
        rowCount: result.rowCount,
        truncated: dataArtifact.truncated,
        sql: trimmed,
      })
      .catch(() => undefined);
  }

  return { dataArtifact, result };
}

/**
 * Merges a fresh `data` artefact into an assistant message artefact list.
 */
export function mergeDataArtifactIntoMessage(
  artifacts: readonly ChatMessageArtifact[] | undefined,
  data: Extract<ChatMessageArtifact, { kind: 'data' }>,
): ChatMessageArtifact[] {
  const prev = artifacts ?? [];
  return [...prev.filter((artifact) => artifact.kind !== 'data'), data];
}
