import { useCallback, useRef } from 'react';
import { executeChatSqlArtifact } from '../../../services/chatSqlExecution';
import { queryService } from '../../../services/api';
import type { ChatMessageArtifact } from '../../../types/chat';
import type { QueryResult } from '../../../types/query';

interface UseChatArtifactRunOptions {
  conversationId: string;
  messageId: string;
  sql: string;
  onDataArtifact: (artifact: Extract<ChatMessageArtifact, { kind: 'data' }>) => void;
}

export function useChatArtifactRun({
  conversationId,
  messageId,
  sql,
  onDataArtifact,
}: UseChatArtifactRunOptions) {
  const activeExecutionRef = useRef<string | null>(null);

  const closePriorSession = useCallback(async () => {
    const prior = activeExecutionRef.current;
    if (!prior) return;
    activeExecutionRef.current = null;
    await queryService.closeQuerySession(prior).catch(() => undefined);
  }, []);

  const run = useCallback(async (): Promise<QueryResult | null> => {
    const trimmed = sql.trim();
    if (!trimmed) return null;

    await closePriorSession();
    const { dataArtifact, result } = await executeChatSqlArtifact(conversationId, messageId, trimmed);
    activeExecutionRef.current = dataArtifact.executionId;
    onDataArtifact(dataArtifact);
    return result;
  }, [closePriorSession, conversationId, messageId, onDataArtifact, sql]);

  return { run, closePriorSession };
}
