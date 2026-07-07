import { useCallback, useEffect, useRef, useState } from 'react';
import type { ChatMessageArtifact } from '../../../types/chat';
import type { QueryResult } from '../../../types/query';
import { useFeatureFlags } from '../../../features/FeatureFlagContext';
import { queryService } from '../../../services/api';
import { isRecoverableQuerySessionError } from '../../../services/queryErrors';
import { useChatArtifactRun } from './useChatArtifactRun';
import { useLazyArtifactData } from './useLazyArtifactData';

interface UseSqlArtifactQueryDataOptions {
  sql: string;
  conversationId: string;
  messageId: string;
  parentArtifactId?: string;
  storedExecutionId?: string;
  /** When false, stored execution pages are not fetched (e.g. SQL tab active). */
  lazyEnabled: boolean;
  /** When true, execute SQL on mount when there is no stored execution id to hydrate from. */
  autoRunLive: boolean;
  onDataArtifact: (artifact: Extract<ChatMessageArtifact, { kind: 'data' }>) => void;
}

export function useSqlArtifactQueryData({
  sql,
  conversationId,
  messageId,
  parentArtifactId,
  storedExecutionId,
  lazyEnabled,
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
    parentArtifactId,
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
    enabled: lazyEnabled && !liveResult && !isRunning,
    onExecutionExpired: () => {
      void refreshExpiredExecution();
    },
  });

  // Reset only when the SQL target changes — not when a data artifact id is attached after auto-run.
  useEffect(() => {
    setLiveResult(null);
    setRunError(null);
    autoRunDoneRef.current = false;
  }, [sql, parentArtifactId]);

  useEffect(() => {
    setLiveResult((prev) => {
      if (!prev || !storedExecutionId) return prev;
      return prev.page.executionId === storedExecutionId ? prev : null;
    });
  }, [storedExecutionId]);

  const displayResult = liveResult ?? lazy.result;
  const lazyError =
    lazy.error && !isRecoverableQuerySessionError(lazy.error) ? lazy.error : null;
  const displayError = displayResult ? null : runError ?? lazyError;

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
        if (isRecoverableQuerySessionError(e)) {
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
    if (!autoRunLive || !flags.chatSqlExecute || !sql.trim()) return;
    if (autoRunDoneRef.current) return;
    if (liveResult || storedExecutionId) return;
    autoRunDoneRef.current = true;
    void handleRun();
  }, [
    autoRunLive,
    flags.chatSqlExecute,
    handleRun,
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
