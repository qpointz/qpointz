import { useCallback, useEffect, useRef, useState } from 'react';
import type { ChatMessageArtifact } from '../../../types/chat';
import type { QueryResult } from '../../../types/query';
import { useFeatureFlags } from '../../../features/FeatureFlagContext';
import { queryService } from '../../../services/api';
import { isQuerySessionNotFound } from '../../../services/queryErrors';
import { useChatArtifactRun } from './useChatArtifactRun';
import { useLazyArtifactData } from './useLazyArtifactData';

interface UseSqlArtifactQueryDataOptions {
  sql: string;
  conversationId: string;
  messageId: string;
  storedExecutionId?: string;
  /** When false, stored execution pages are not fetched (e.g. SQL tab active). */
  lazyEnabled: boolean;
  isReplayed: boolean;
  autoRunLive: boolean;
  onDataArtifact: (artifact: Extract<ChatMessageArtifact, { kind: 'data' }>) => void;
}

export function useSqlArtifactQueryData({
  sql,
  conversationId,
  messageId,
  storedExecutionId,
  lazyEnabled,
  isReplayed,
  autoRunLive,
  onDataArtifact,
}: UseSqlArtifactQueryDataOptions) {
  const flags = useFeatureFlags();
  const [liveResult, setLiveResult] = useState<QueryResult | null>(null);
  const [runError, setRunError] = useState<string | null>(null);
  const [isRunning, setIsRunning] = useState(false);
  const [isPageLoading, setIsPageLoading] = useState(false);
  const autoRunDoneRef = useRef(false);
  const silentRerunInFlightRef = useRef(false);

  const { run } = useChatArtifactRun({
    conversationId,
    messageId,
    sql,
    onDataArtifact,
  });

  const handleRun = useCallback(
    async (options?: { silent?: boolean }): Promise<QueryResult | null> => {
      if (!flags.chatSqlExecute) return null;
      setIsRunning(true);
      if (!options?.silent) {
        setRunError(null);
      }
      try {
        const result = await run();
        setLiveResult(result);
        return result;
      } catch (e) {
        if (!options?.silent) {
          setRunError(e instanceof Error ? e.message : 'Query failed');
        }
        return null;
      } finally {
        setIsRunning(false);
      }
    },
    [flags.chatSqlExecute, run],
  );

  const refreshExpiredExecution = useCallback(async () => {
    if (silentRerunInFlightRef.current) return;
    silentRerunInFlightRef.current = true;
    setRunError(null);
    try {
      await handleRun({ silent: true });
    } finally {
      silentRerunInFlightRef.current = false;
    }
  }, [handleRun]);

  const lazy = useLazyArtifactData({
    executionId: liveResult ? undefined : storedExecutionId,
    enabled: lazyEnabled && !liveResult,
    onExecutionExpired: () => {
      void refreshExpiredExecution();
    },
  });

  useEffect(() => {
    setLiveResult(null);
    setRunError(null);
  }, [storedExecutionId]);

  const displayResult = liveResult ?? lazy.result;
  const lazyError =
    lazy.error && !isQuerySessionNotFound(lazy.error) ? lazy.error : null;
  const displayError = runError ?? lazyError;

  const handlePageChange = useCallback(
    async (pageIndex: number) => {
      const current = liveResult ?? lazy.result;
      if (!current?.page || isPageLoading || isRunning) return;
      if (pageIndex === current.page.pageIndex) return;

      setIsPageLoading(true);
      if (!liveResult) {
        setRunError(null);
      }
      try {
        const nextPage = await queryService.fetchQueryPage({
          executionId: current.page.executionId,
          pageIndex,
          pageSize: current.page.pageSize,
          epoch: current.page.epoch,
        });
        const merged: QueryResult = {
          ...nextPage,
          executionTimeMs: current.executionTimeMs,
        };
        if (liveResult) {
          setLiveResult(merged);
        } else {
          lazy.setResult(merged);
        }
      } catch (e) {
        if (isQuerySessionNotFound(e)) {
          void refreshExpiredExecution();
          return;
        }
        setRunError(e instanceof Error ? e.message : 'Failed to load page');
      } finally {
        setIsPageLoading(false);
      }
    },
    [isPageLoading, isRunning, lazy, liveResult, refreshExpiredExecution],
  );

  useEffect(() => {
    if (!autoRunLive || isReplayed || !flags.chatSqlExecute || !sql.trim()) return;
    if (autoRunDoneRef.current) return;
    if (liveResult || storedExecutionId) return;
    autoRunDoneRef.current = true;
    void handleRun();
  }, [
    autoRunLive,
    flags.chatSqlExecute,
    handleRun,
    isReplayed,
    liveResult,
    sql,
    storedExecutionId,
  ]);

  return {
    containerRef: lazy.containerRef,
    displayResult,
    displayError,
    isRunning,
    isPageLoading: isPageLoading || lazy.isLoading,
    handleRun: () => handleRun(),
    handlePageChange,
  };
}
