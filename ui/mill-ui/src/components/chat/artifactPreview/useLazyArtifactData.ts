import { useCallback, useEffect, useRef, useState } from 'react';
import { queryService, readStoredQueryPageSize } from '../../../services/queryService';
import { isRecoverableQuerySessionError } from '../../../services/queryErrors';
import type { QueryResult } from '../../../types/query';

interface UseLazyArtifactDataOptions {
  executionId: string | undefined;
  enabled: boolean;
  pageSize?: number;
  /** Invoked when the stored execution id is no longer valid — caller should re-run SQL. */
  onExecutionExpired?: () => void;
}

/**
 * Lazy-hydrates query result pages when enabled and the card is in viewport.
 */
export function useLazyArtifactData({
  executionId,
  enabled,
  pageSize = readStoredQueryPageSize(),
  onExecutionExpired,
}: UseLazyArtifactDataOptions) {
  const [result, setResult] = useState<QueryResult | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const containerRef = useRef<HTMLDivElement | null>(null);
  const [inViewport, setInViewport] = useState(false);
  const fetchedRef = useRef<string | null>(null);
  const resultRef = useRef<QueryResult | null>(null);
  resultRef.current = result;

  useEffect(() => {
    const el = containerRef.current;
    if (!el) return;
    const observer = new IntersectionObserver(
      ([entry]) => setInViewport(entry?.isIntersecting ?? false),
      { rootMargin: '40px' },
    );
    observer.observe(el);
    return () => observer.disconnect();
  }, []);

  useEffect(() => {
    setResult(null);
    setError(null);
    fetchedRef.current = null;
  }, [executionId]);

  const shouldFetch = enabled && inViewport && Boolean(executionId);

  useEffect(() => {
    if (!shouldFetch || !executionId) return;
    if (fetchedRef.current === executionId && result) return;

    let cancelled = false;
    setIsLoading(true);
    setError(null);

    void (async () => {
      try {
        const page = await queryService.fetchQueryPage({
          executionId,
          pageIndex: 0,
          pageSize,
          epoch: 0,
        });
        if (cancelled) return;
        setResult(page);
        fetchedRef.current = executionId;
      } catch (e) {
        if (cancelled) return;
        if (isRecoverableQuerySessionError(e)) {
          fetchedRef.current = null;
          setResult(null);
          setError(null);
          onExecutionExpired?.();
          return;
        }
        setError(e instanceof Error ? e.message : 'Failed to load data');
      } finally {
        if (!cancelled) setIsLoading(false);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [shouldFetch, executionId, pageSize, result, onExecutionExpired]);

  const fetchPage = useCallback(async (pageIndex: number) => {
    const current = resultRef.current;
    if (!executionId || !current) return;
    setIsLoading(true);
    setError(null);
    try {
      const page = await queryService.fetchQueryPage({
        executionId,
        pageIndex,
        pageSize: current.page.pageSize,
        epoch: current.page.epoch,
      });
      setResult({
        ...page,
        executionTimeMs: current.executionTimeMs,
      });
    } catch (e) {
      if (isRecoverableQuerySessionError(e)) {
        setResult(null);
        setError(null);
        fetchedRef.current = null;
        onExecutionExpired?.();
        return;
      }
      setError(e instanceof Error ? e.message : 'Failed to load page');
    } finally {
      setIsLoading(false);
    }
  }, [executionId, onExecutionExpired]);

  const reset = useCallback(() => {
    setResult(null);
    setError(null);
    fetchedRef.current = null;
  }, []);

  return { containerRef, result, error, isLoading, fetchPage, reset, setResult, setError };
}
