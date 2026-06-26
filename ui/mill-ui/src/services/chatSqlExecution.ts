import type { Message, ChatMessageArtifact } from '../types/chat';
import type { QueryResult } from '../types/query';
import { chatService, queryService } from './api';
import { isRestChatBackendActive } from './chatService';
import { readStoredQueryPageSize } from './queryService';

export interface ChatSqlTarget {
  messageId: string;
  sql: string;
  /** SQL artefact id on the wire — binds attached `data` via `parentArtifactId`. */
  parentArtifactId?: string;
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

    const sqlArtifacts = (message.artifacts ?? []).filter(
      (artifact): artifact is Extract<ChatMessageArtifact, { kind: 'sql' }> => artifact.kind === 'sql',
    );
    if (sqlArtifacts.length > 0) {
      for (const sqlArtifact of [...sqlArtifacts].reverse()) {
        const sql = (sqlArtifact.sql ?? '').trim();
        if (!sql) continue;
        const dedupeKey = `${message.id}:${sqlArtifact.artifactId ?? sql}`;
        if (seen.has(dedupeKey)) continue;
        seen.add(dedupeKey);
        targets.push({
          messageId: message.id,
          sql,
          parentArtifactId: sqlArtifact.artifactId,
        });
      }
      continue;
    }

    const dataArtifact = message.artifacts?.find(
      (artifact): artifact is Extract<ChatMessageArtifact, { kind: 'data' }> => artifact.kind === 'data',
    );
    const sql = (dataArtifact?.sql ?? '').trim();
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
  parentArtifactId?: string,
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
    sourceArtifactId: parentArtifactId,
  };

  if (isRestChatBackendActive()) {
    await chatService
      .attachExecutionResult(conversationId, messageId, {
        executionId: result.page.executionId,
        columns: result.columns,
        rowCount: result.rowCount,
        truncated: dataArtifact.truncated,
        sql: trimmed,
        parentArtifactId,
      })
      .catch(() => undefined);
  }

  return { dataArtifact, result };
}

function normalizedSql(sql: string | undefined): string {
  return (sql ?? '').trim();
}

function dataConflictsWithIncoming(
  existing: Extract<ChatMessageArtifact, { kind: 'data' }>,
  incoming: Extract<ChatMessageArtifact, { kind: 'data' }>,
): boolean {
  if (incoming.sourceArtifactId) {
    return existing.sourceArtifactId === incoming.sourceArtifactId;
  }
  if (existing.sourceArtifactId) {
    return false;
  }
  return normalizedSql(existing.sql) === normalizedSql(incoming.sql);
}

/**
 * Merges a fresh `data` artefact into an assistant message artefact list.
 */
export function mergeDataArtifactIntoMessage(
  artifacts: readonly ChatMessageArtifact[] | undefined,
  data: Extract<ChatMessageArtifact, { kind: 'data' }>,
): ChatMessageArtifact[] {
  const prev = artifacts ?? [];
  return [
    ...prev.filter((artifact) => artifact.kind !== 'data' || !dataConflictsWithIncoming(artifact, data)),
    data,
  ];
}

/**
 * Merges multiple `data` artefacts into one message (e.g. Run all on a multi-SQL turn).
 */
export function mergeRunAllDataArtifacts(
  base: readonly ChatMessageArtifact[] | undefined,
  dataArtifacts: readonly Extract<ChatMessageArtifact, { kind: 'data' }>[],
): ChatMessageArtifact[] {
  return dataArtifacts.reduce(
    (acc, data) => mergeDataArtifactIntoMessage(acc, data),
    [...(base ?? [])],
  );
}
