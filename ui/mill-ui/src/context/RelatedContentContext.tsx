import {
  createContext,
  useContext,
  useCallback,
  useRef,
  useState,
  useEffect,
  type ReactNode,
} from 'react';
import type { RelatedContentRef } from '../types/relatedContent';
import { relatedContentService } from '../services/api';
import { useFeatureFlags } from '../features/FeatureFlagContext';

interface RelatedContentContextValue {
  /**
   * Get related content refs for a context object.
   * Returns cached result synchronously if available, otherwise triggers a fetch.
   */
  getRefsForContextId: (contextId: string) => RelatedContentRef[];

  /**
   * Batch-prefetch refs for multiple context IDs (used by sidebar/layout components).
   */
  prefetchRefs: (contextType: string, contextIds: string[]) => void;

  /**
   * Fetch refs for a single context object. Populates the cache and triggers re-render.
   */
  fetchRefsForContext: (contextType: string, contextId: string) => void;
}

const RelatedContentContext = createContext<RelatedContentContextValue | null>(null);

export function RelatedContentProvider({ children }: { children: ReactNode }) {
  const flags = useFeatureFlags();

  // Cache: contextKey -> RelatedContentRef[]
  const cacheRef = useRef<Map<string, RelatedContentRef[]>>(new Map());
  // Track in-flight requests to avoid duplicates
  const pendingRef = useRef<Set<string>>(new Set());
  // Bump to trigger re-renders when cache updates
  const [, setVersion] = useState(0);

  const fetchSingle = useCallback(
    async (contextType: string, contextId: string) => {
      if (!flags.relatedContentEnabled) return;

      const key = `${contextType}:${contextId}`;
      if (cacheRef.current.has(key) || pendingRef.current.has(key)) return;

      pendingRef.current.add(key);
      try {
        const refs = await relatedContentService.getRelatedContent(contextType, contextId);
        cacheRef.current.set(key, refs);
        setVersion((v) => v + 1);
      } catch (err) {
        console.error('Failed to fetch related content:', err);
        cacheRef.current.set(key, []);
      } finally {
        pendingRef.current.delete(key);
      }
    },
    [flags.relatedContentEnabled],
  );

  const getRefsForContextId = useCallback(
    (contextId: string): RelatedContentRef[] => {
      // Look up in cache by contextId (search all context types)
      for (const [key, refs] of cacheRef.current.entries()) {
        if (key.endsWith(`:${contextId}`)) {
          return refs;
        }
      }
      return [];
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps -- intentionally re-creates when version bumps
    [],
  );

  const prefetchRefs = useCallback(
    (contextType: string, contextIds: string[]) => {
      if (!flags.relatedContentEnabled) return;
      for (const id of contextIds) {
        fetchSingle(contextType, id);
      }
    },
    [flags.relatedContentEnabled, fetchSingle],
  );

  const fetchRefsForContext = useCallback(
    (contextType: string, contextId: string) => {
      fetchSingle(contextType, contextId);
    },
    [fetchSingle],
  );

  const value: RelatedContentContextValue = {
    getRefsForContextId,
    prefetchRefs,
    fetchRefsForContext,
  };

  return (
    <RelatedContentContext.Provider value={value}>
      {children}
    </RelatedContentContext.Provider>
  );
}

export function useRelatedContentContext() {
  const context = useContext(RelatedContentContext);
  if (!context) {
    throw new Error('useRelatedContentContext must be used within a RelatedContentProvider');
  }
  return context;
}

/**
 * Hook to fetch and return related content refs for a specific context object.
 * All data flows through the RelatedContentContext cache -- no direct service calls.
 */
export function useRelatedContent(
  contextType: string | null | undefined,
  contextId: string | null | undefined,
): { refs: RelatedContentRef[]; isLoading: boolean } {
  const flags = useFeatureFlags();
  const ctx = useRelatedContentContext();

  useEffect(() => {
    if (!contextType || !contextId || !flags.relatedContentEnabled) return;

    // Check per-context flags
    if (contextType === 'model' && !flags.relatedContentModelContext) return;
    if (contextType === 'knowledge' && !flags.relatedContentKnowledgeContext) return;
    if (contextType === 'analysis' && !flags.relatedContentAnalysisContext) return;

    // Trigger fetch through context (cache-aware, deduplicates)
    ctx.fetchRefsForContext(contextType, contextId);
  }, [contextType, contextId, flags, ctx]);

  // Read from cache synchronously
  const refs = contextId ? ctx.getRefsForContextId(contextId) : [];
  const isLoading = false; // Cache-based: data arrives via re-render when cache updates

  return { refs, isLoading };
}
