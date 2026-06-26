import { useCallback, useMemo, useState } from 'react';
import type { ChatMessageArtifact, Conversation } from '../../types/chat';
import {
  collectChatSqlTargets,
  executeChatSqlArtifact,
  mergeDataArtifactIntoMessage,
} from '../../services/chatSqlExecution';

interface UseRunAllChatQueriesOptions {
  conversation: Conversation | null;
  chatSqlExecuteEnabled: boolean;
  isLoading: boolean;
  updateMessageArtifacts: (
    conversationId: string,
    messageId: string,
    artifacts: readonly ChatMessageArtifact[],
  ) => void;
  onRunAllComplete?: () => void;
}

export function useRunAllChatQueries({
  conversation,
  chatSqlExecuteEnabled,
  isLoading,
  updateMessageArtifacts,
  onRunAllComplete,
}: UseRunAllChatQueriesOptions) {
  const [runAllLoading, setRunAllLoading] = useState(false);

  const sqlTargets = useMemo(
    () => collectChatSqlTargets(conversation?.messages ?? []),
    [conversation?.messages],
  );

  const runAllDisabled =
    !conversation ||
    !chatSqlExecuteEnabled ||
    isLoading ||
    runAllLoading ||
    sqlTargets.length === 0;

  const runAllQueries = useCallback(async () => {
    if (!conversation || !chatSqlExecuteEnabled || sqlTargets.length === 0) return;

    setRunAllLoading(true);
    try {
      const artifactsByMessage = new Map<string, ChatMessageArtifact[]>();

      for (const target of sqlTargets) {
        const message = conversation.messages.find((entry) => entry.id === target.messageId);
        if (!message) continue;

        const baseArtifacts = artifactsByMessage.get(target.messageId) ?? message.artifacts ?? [];

        try {
          const { dataArtifact } = await executeChatSqlArtifact(
            conversation.id,
            target.messageId,
            target.sql,
            target.parentArtifactId,
          );
          const merged = mergeDataArtifactIntoMessage(baseArtifacts, dataArtifact);
          artifactsByMessage.set(target.messageId, merged);
          updateMessageArtifacts(conversation.id, target.messageId, merged);
        } catch (error) {
          console.error(`Failed to refresh SQL for message ${target.messageId}:`, error);
        }
      }
    } finally {
      setRunAllLoading(false);
      onRunAllComplete?.();
    }
  }, [chatSqlExecuteEnabled, conversation, onRunAllComplete, sqlTargets, updateMessageArtifacts]);

  return {
    sqlQueryCount: sqlTargets.length,
    runAllDisabled,
    runAllLoading,
    runAllQueries,
  };
}
