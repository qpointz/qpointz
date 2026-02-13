import {
  createContext,
  useContext,
  useCallback,
  useRef,
  useState,
  useEffect,
  type ReactNode,
} from 'react';
import type { ConversationRef } from '../types/chatReferences';
import { chatReferencesService } from '../services/api';
import { useFeatureFlags } from '../features/FeatureFlagContext';

interface ChatReferencesContextValue {
  /**
   * Get related conversation refs for a context object.
   * Returns cached result synchronously if available, otherwise triggers a fetch.
   */
  getRefsForContextId: (contextId: string) => ConversationRef[];

  /**
   * Batch-prefetch refs for multiple context IDs (used by sidebar/layout components).
   */
  prefetchRefs: (contextType: string, contextIds: string[]) => void;

  /**
   * Fetch refs for a single context object. Populates the cache and triggers re-render.
   */
  fetchRefsForContext: (contextType: string, contextId: string) => void;
}

const ChatReferencesContext = createContext<ChatReferencesContextValue | null>(null);

export function ChatReferencesProvider({ children }: { children: ReactNode }) {
  const flags = useFeatureFlags();

  // Cache: contextKey -> ConversationRef[]
  const cacheRef = useRef<Map<string, ConversationRef[]>>(new Map());
  // Track in-flight requests to avoid duplicates
  const pendingRef = useRef<Set<string>>(new Set());
  // Bump to trigger re-renders when cache updates
  const [, setVersion] = useState(0);

  const fetchSingle = useCallback(
    async (contextType: string, contextId: string) => {
      if (!flags.chatReferencesEnabled) return;

      const key = `${contextType}:${contextId}`;
      if (cacheRef.current.has(key) || pendingRef.current.has(key)) return;

      pendingRef.current.add(key);
      try {
        const refs = await chatReferencesService.getConversationsForContext(contextType, contextId);
        cacheRef.current.set(key, refs);
        setVersion((v) => v + 1);
      } catch (err) {
        console.error('Failed to fetch chat references:', err);
        cacheRef.current.set(key, []);
      } finally {
        pendingRef.current.delete(key);
      }
    },
    [flags.chatReferencesEnabled],
  );

  const getRefsForContextId = useCallback(
    (contextId: string): ConversationRef[] => {
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
      if (!flags.chatReferencesEnabled) return;
      for (const id of contextIds) {
        fetchSingle(contextType, id);
      }
    },
    [flags.chatReferencesEnabled, fetchSingle],
  );

  const fetchRefsForContext = useCallback(
    (contextType: string, contextId: string) => {
      fetchSingle(contextType, contextId);
    },
    [fetchSingle],
  );

  const value: ChatReferencesContextValue = {
    getRefsForContextId,
    prefetchRefs,
    fetchRefsForContext,
  };

  return (
    <ChatReferencesContext.Provider value={value}>
      {children}
    </ChatReferencesContext.Provider>
  );
}

export function useChatReferencesContext() {
  const context = useContext(ChatReferencesContext);
  if (!context) {
    throw new Error('useChatReferencesContext must be used within a ChatReferencesProvider');
  }
  return context;
}

/**
 * Hook to fetch and return related conversation refs for a specific context object.
 * All data flows through the ChatReferencesContext cache -- no direct service calls.
 */
export function useChatReferences(
  contextType: string | null | undefined,
  contextId: string | null | undefined,
): { refs: ConversationRef[]; isLoading: boolean } {
  const flags = useFeatureFlags();
  const ctx = useChatReferencesContext();

  useEffect(() => {
    if (!contextType || !contextId || !flags.chatReferencesEnabled) return;

    // Check per-context flags
    if (contextType === 'model' && !flags.chatReferencesModelContext) return;
    if (contextType === 'knowledge' && !flags.chatReferencesKnowledgeContext) return;
    if (contextType === 'analysis' && !flags.chatReferencesAnalysisContext) return;

    // Trigger fetch through context (cache-aware, deduplicates)
    ctx.fetchRefsForContext(contextType, contextId);
  }, [contextType, contextId, flags, ctx]);

  // Read from cache synchronously
  const refs = contextId ? ctx.getRefsForContextId(contextId) : [];
  const isLoading = false; // Cache-based: data arrives via re-render when cache updates

  return { refs, isLoading };
}
