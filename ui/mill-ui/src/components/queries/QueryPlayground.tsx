import { Box, useMantineColorScheme, Text, Badge, ActionIcon, Tooltip } from '@mantine/core';
import { useState, useCallback, useEffect, useRef } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router';
import { format as formatSQL } from 'sql-formatter';
import { HiOutlineCommandLine, HiOutlineBeaker, HiOutlinePlus } from 'react-icons/hi2';
import { QuerySidebar } from './QuerySidebar';
import { QueryEditor } from './QueryEditor';
import { QueryDataView } from '../data/QueryDataView';
import { ExplorerSplitLayout } from '../layout/ExplorerSplitLayout';
import { queryService } from '../../services/api';
import {
  normalizeQueryPageSize,
  readStoredQueryPageSize,
  storeQueryPageSize,
} from '../../services/queryService';
import { VerticalSplitPane } from '../layout/VerticalSplitPane';
import { useFeatureFlags } from '../../features/FeatureFlagContext';
import { useInlineChatListener } from '../../context/InlineChatContext';
import type { SavedQuery, QueryResult } from '../../types/query';
import { resolveSqlToExecute } from './resolveSqlToExecute';
import { sanitizeExportAttachmentBaseName } from '../../services/exportHelpers';
import { registerHostApplyHandler } from '../chat/artifactPreview/hostIntegrations';
import type { ChatHandoffState } from '../chat/artifactPreview/useOpenInAnalysis';

/**
 * Extract the first SQL code block from markdown content.
 * Looks for ```sql ... ``` fenced blocks.
 */
function extractSqlFromMarkdown(content: string): string | null {
  const match = content.match(/```sql\s*\n([\s\S]*?)```/i);
  return match?.[1] ? match[1].trim() : null;
}

let newQueryCounter = 0;

function applyLoadedQuery(
  query: SavedQuery,
  setters: {
    setSql: (sql: string) => void;
    setSavedSnapshot: (sql: string) => void;
    setSavedNameSnapshot: (name: string) => void;
    setActiveQueryId: (id: string) => void;
    setActiveQueryName: (name: string) => void;
    setActiveQueryDescription: (description: string | null) => void;
    setResult: (result: QueryResult | null) => void;
    setError: (error: string | null) => void;
    setIsDirty: (dirty: boolean) => void;
    onSessionCleared?: () => void;
  },
) {
  setters.setSql(query.sql);
  setters.setSavedSnapshot(query.sql);
  setters.setSavedNameSnapshot(query.name);
  setters.setActiveQueryId(query.id);
  setters.setActiveQueryName(query.name);
  setters.setActiveQueryDescription(query.description ?? null);
  setters.setResult(null);
  setters.setError(null);
  setters.setIsDirty(false);
  setters.onSessionCleared?.();
}

function computeIsDirty(sql: string, savedSql: string, name: string | null, savedName: string): boolean {
  return sql !== savedSql || (name ?? '') !== savedName;
}

export function QueryPlayground() {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const flags = useFeatureFlags();
  const navigate = useNavigate();
  const location = useLocation();
  const params = useParams<{ queryId?: string }>();

  const [sql, setSql] = useState('');
  const [savedSnapshot, setSavedSnapshot] = useState('');
  const [savedNameSnapshot, setSavedNameSnapshot] = useState('');
  const [isDirty, setIsDirty] = useState(false);
  const [sqlCopied, setSqlCopied] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [isExecuting, setIsExecuting] = useState(false);
  const [result, setResult] = useState<QueryResult | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isPageLoading, setIsPageLoading] = useState(false);
  const [queryPageSize, setQueryPageSize] = useState(readStoredQueryPageSize);
  const activeExecutionIdRef = useRef<string | null>(null);
  const [activeQueryId, setActiveQueryId] = useState<string | null>(null);
  const [activeQueryName, setActiveQueryName] = useState<string | null>(null);
  const [activeQueryDescription, setActiveQueryDescription] = useState<string | null>(null);
  const [savedQueries, setSavedQueries] = useState<SavedQuery[]>([]);
  const savedSnapshotRef = useRef(savedSnapshot);
  const savedNameSnapshotRef = useRef(savedNameSnapshot);
  const activeQueryNameRef = useRef(activeQueryName);
  const chatHandoffProcessedRef = useRef(false);

  savedSnapshotRef.current = savedSnapshot;
  savedNameSnapshotRef.current = savedNameSnapshot;
  activeQueryNameRef.current = activeQueryName;

  useEffect(() => {
    registerHostApplyHandler('inline-analysis', (artifact) => {
      const nextSql = artifact.sql.trim();
      if (!nextSql) return;
      setSql(nextSql);
      setIsDirty(
        computeIsDirty(
          nextSql,
          savedSnapshotRef.current,
          activeQueryNameRef.current,
          savedNameSnapshotRef.current,
        ),
      );
    });
  }, []);

  const loadSavedQueries = useCallback(async () => {
    try {
      const queries = await queryService.getSavedQueries();
      setSavedQueries(queries);
      return queries;
    } catch {
      setSavedQueries([]);
      return [];
    }
  }, []);

  const closeActiveSession = useCallback(async () => {
    const executionId = activeExecutionIdRef.current;
    if (!executionId) {
      return;
    }
    activeExecutionIdRef.current = null;
    await queryService.closeQuerySession(executionId).catch(() => {
      // Session may already be evicted server-side.
    });
  }, []);

  const clearActiveSessionRef = useCallback(() => {
    activeExecutionIdRef.current = null;
  }, []);

  const querySetters = {
    setSql,
    setSavedSnapshot,
    setSavedNameSnapshot,
    setActiveQueryId,
    setActiveQueryName,
    setActiveQueryDescription,
    setResult,
    setError,
    setIsDirty,
    onSessionCleared: clearActiveSessionRef,
  };

  // Load saved queries on mount
  useEffect(() => {
    void loadSavedQueries();
  }, [loadSavedQueries]);

  // Chat → Analysis handoff: always create a fresh saved query with the generated SQL.
  useEffect(() => {
    const handoff = (location.state as ChatHandoffState | null)?.chatHandoff;
    if (!handoff?.sql?.trim() || chatHandoffProcessedRef.current) {
      return;
    }
    chatHandoffProcessedRef.current = true;

    void (async () => {
      try {
        await closeActiveSession();
        newQueryCounter += 1;
        const name = handoff.suggestedName?.trim() || `New Query ${newQueryCounter}`;
        const created = await queryService.createSavedQuery({
          name,
          sql: handoff.sql.trim(),
          description: handoff.suggestedDescription,
        });
        setSavedQueries((prev) => [created, ...prev]);
        applyLoadedQuery(created, querySetters);
        navigate(`/analysis/${created.id}`, { replace: true, state: null });
      } catch (err) {
        chatHandoffProcessedRef.current = false;
        setError(err instanceof Error ? err.message : 'Failed to open query from chat');
      }
    })();
  }, [location.state, navigate, closeActiveSession]);

  // Open the first catalog entry when visiting bare /analysis (restores Save + named query context).
  useEffect(() => {
    if ((location.state as ChatHandoffState | null)?.chatHandoff) {
      return;
    }
    if (params.queryId || activeQueryId || savedQueries.length === 0) {
      return;
    }
    const first = savedQueries[0];
    if (!first) {
      return;
    }
    void closeActiveSession();
    applyLoadedQuery(first, querySetters);
    navigate(`/analysis/${first.id}`, { replace: true });
  }, [params.queryId, activeQueryId, savedQueries, navigate, closeActiveSession, location.state]);

  useEffect(() => () => {
    void closeActiveSession();
  }, [closeActiveSession]);

  // Sync URL params to selected query (skip when already active to preserve unsaved edits)
  useEffect(() => {
    if (!params.queryId || activeQueryId === params.queryId) {
      return;
    }

    const localQuery = savedQueries.find((q) => q.id === params.queryId);
    if (localQuery) {
      void closeActiveSession();
      applyLoadedQuery(localQuery, querySetters);
      return;
    }

    queryService.getSavedQueryById(params.queryId).then((query) => {
      if (query) {
        void closeActiveSession();
        applyLoadedQuery(query, querySetters);
        setSavedQueries((prev) => (prev.some((q) => q.id === query.id) ? prev : [query, ...prev]));
      }
    }).catch(() => {
      // Query not found -- ignore
    });
  }, [params.queryId, savedQueries, activeQueryId, closeActiveSession]);

  // Listen to inline chat AI responses -- extract SQL and update editor
  useInlineChatListener(activeQueryId ?? '__analysis__', (content) => {
    const extracted = extractSqlFromMarkdown(content);
    if (extracted) {
      setSql(extracted);
      setIsDirty(computeIsDirty(extracted, savedSnapshot, activeQueryName, savedNameSnapshot));
    }
  });

  const handleSelectQuery = useCallback((query: SavedQuery) => {
    void closeActiveSession();
    applyLoadedQuery(query, querySetters);
    navigate(`/analysis/${query.id}`);
  }, [navigate, closeActiveSession]);

  const handleSqlChange = useCallback((newSql: string) => {
    setSql(newSql);
    setIsDirty(computeIsDirty(newSql, savedSnapshot, activeQueryName, savedNameSnapshot));
  }, [savedSnapshot, activeQueryName, savedNameSnapshot]);

  const handleQueryNameChange = useCallback((name: string) => {
    setActiveQueryName(name);
    setIsDirty(computeIsDirty(sql, savedSnapshot, name, savedNameSnapshot));
  }, [sql, savedSnapshot, savedNameSnapshot]);

  const handleFormatSql = useCallback(() => {
    try {
      const formatted = formatSQL(sql, {
        language: 'sql',
        tabWidth: 2,
        keywordCase: 'upper',
        linesBetweenQueries: 2,
      });
      setSql(formatted);
      setIsDirty(computeIsDirty(formatted, savedSnapshot, activeQueryName, savedNameSnapshot));
    } catch {
      // Leave SQL unchanged when formatting fails.
    }
  }, [sql, savedSnapshot, activeQueryName, savedNameSnapshot]);

  const handleCopySql = useCallback(async () => {
    try {
      await navigator.clipboard.writeText(sql);
      setSqlCopied(true);
      setTimeout(() => setSqlCopied(false), 2000);
    } catch {
      // Clipboard API not available.
    }
  }, [sql]);

  const handleClearSql = useCallback(() => {
    setSql('');
    setIsDirty(computeIsDirty('', savedSnapshot, activeQueryName, savedNameSnapshot));
  }, [savedSnapshot, activeQueryName, savedNameSnapshot]);

  const handleExecute = useCallback(async (sqlFragment?: string) => {
    const sqlToRun = resolveSqlToExecute(sql, sqlFragment);
    if (!sqlToRun || isExecuting) return;

    setIsExecuting(true);
    setError(null);
    setResult(null);

    try {
      await closeActiveSession();
      const queryResult = await queryService.executeQuery(sqlToRun, { pageSize: queryPageSize });
      activeExecutionIdRef.current = queryResult.page.executionId;
      setResult(queryResult);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An unknown error occurred');
    } finally {
      setIsExecuting(false);
    }
  }, [sql, isExecuting, closeActiveSession, queryPageSize]);

  const handlePageSizeChange = useCallback(async (nextPageSize: number) => {
    const normalized = normalizeQueryPageSize(nextPageSize);
    setQueryPageSize(normalized);
    storeQueryPageSize(normalized);

    if (!result?.page || isExecuting || isPageLoading) {
      return;
    }

    setIsPageLoading(true);
    setError(null);
    try {
      const nextPage = await queryService.fetchQueryPage({
        executionId: result.page.executionId,
        pageIndex: 0,
        pageSize: normalized,
        epoch: result.page.epoch,
      });
      setResult({
        ...nextPage,
        executionTimeMs: result.executionTimeMs,
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to change page size');
    } finally {
      setIsPageLoading(false);
    }
  }, [result, isExecuting, isPageLoading]);

  const handlePageChange = useCallback(async (pageIndex: number) => {
    if (!result?.page || isPageLoading || isExecuting) {
      return;
    }
    if (pageIndex === result.page.pageIndex) {
      return;
    }

    setIsPageLoading(true);
    setError(null);
    try {
      const nextPage = await queryService.fetchQueryPage({
        executionId: result.page.executionId,
        pageIndex,
        pageSize: result.page.pageSize,
        epoch: result.page.epoch,
      });
      setResult({
        ...nextPage,
        executionTimeMs: result.executionTimeMs,
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load query page');
    } finally {
      setIsPageLoading(false);
    }
  }, [result, isPageLoading, isExecuting]);

  const handleSave = useCallback(async () => {
    if (isSaving) {
      return;
    }

    setIsSaving(true);
    setError(null);
    try {
      if (!activeQueryId) {
        if (!sql.trim()) {
          return;
        }
        newQueryCounter += 1;
        const name = activeQueryName?.trim() || `New Query ${newQueryCounter}`;
        const created = await queryService.createSavedQuery({
          name,
          sql,
          description: activeQueryDescription ?? undefined,
        });
        setSavedQueries((prev) => [created, ...prev]);
        applyLoadedQuery(created, querySetters);
        navigate(`/analysis/${created.id}`);
        return;
      }

      if (!activeQueryName || !isDirty) {
        return;
      }

      const saved = await queryService.updateSavedQuery(activeQueryId, {
        name: activeQueryName,
        description: activeQueryDescription ?? undefined,
        sql,
        tags: savedQueries.find((q) => q.id === activeQueryId)?.tags,
      });
      setSavedQueries((prev) => [saved, ...prev.filter((q) => q.id !== saved.id)]);
      setSavedSnapshot(saved.sql);
      setSavedNameSnapshot(saved.name);
      setIsDirty(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save query');
    } finally {
      setIsSaving(false);
    }
  }, [
    activeQueryId,
    activeQueryName,
    activeQueryDescription,
    isSaving,
    isDirty,
    sql,
    savedQueries,
    navigate,
  ]);

  const handleNewQuery = useCallback(async () => {
    newQueryCounter += 1;
    const name = `New Query ${newQueryCounter}`;
    try {
      const created = await queryService.createSavedQuery({ name, sql: '' });
      setSavedQueries((prev) => [created, ...prev]);
      applyLoadedQuery(created, querySetters);
      navigate(`/analysis/${created.id}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create query');
    }
  }, [navigate]);

  const handleDeleteQuery = useCallback(async (queryId: string) => {
    try {
      await queryService.deleteSavedQuery(queryId);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete query');
      return;
    }

    const remaining = savedQueries.filter((q) => q.id !== queryId);
    setSavedQueries(remaining);

    if (activeQueryId !== queryId) {
      return;
    }

    const currentIndex = savedQueries.findIndex((q) => q.id === queryId);
    const nextQuery = remaining[Math.min(currentIndex, remaining.length - 1)];

    if (nextQuery) {
      void closeActiveSession();
      applyLoadedQuery(nextQuery, querySetters);
      navigate(`/analysis/${nextQuery.id}`);
    } else {
      void closeActiveSession();
      setSql('');
      setSavedSnapshot('');
      setSavedNameSnapshot('');
      setActiveQueryId(null);
      setActiveQueryName(null);
      setActiveQueryDescription(null);
      setResult(null);
      setError(null);
      setIsDirty(false);
      navigate('/analysis');
    }
  }, [activeQueryId, savedQueries, navigate]);

  const exportAttachmentBaseName = sanitizeExportAttachmentBaseName(activeQueryName ?? 'query-results');

  // Check if there's content (SQL or results) to show
  const hasContent = sql.trim() || result || error || activeQueryId;

  return (
    <ExplorerSplitLayout
      icon={HiOutlineBeaker}
      title="Saved Queries"
      sidebarHeaderRight={
        <>
          {flags.sidebarAnalysisBadge && (
            <Badge size="xs" variant="light" color={isDark ? 'cyan' : 'teal'}>
              {savedQueries.length}
            </Badge>
          )}
          <Tooltip label="New query" withArrow>
            <ActionIcon
              size="sm"
              variant="subtle"
              color={isDark ? 'cyan' : 'teal'}
              onClick={() => { void handleNewQuery(); }}
            >
              <HiOutlinePlus size={14} />
            </ActionIcon>
          </Tooltip>
        </>
      }
      sidebarBody={
        <QuerySidebar
          queries={savedQueries}
          activeQueryId={activeQueryId}
          onSelectQuery={handleSelectQuery}
          onDeleteQuery={(queryId) => { void handleDeleteQuery(queryId); }}
        />
      }
      main={
        hasContent ? (
          flags.analysisQueryResults ? (
            <VerticalSplitPane
              storageKey="mill-ui.analysis.editorSplitPercent"
              initialTopPercent={45}
              minTopPx={160}
              minBottomPx={120}
              top={(
                <QueryEditor
                  sql={sql}
                  onChange={handleSqlChange}
                  onExecute={(sqlFragment) => { void handleExecute(sqlFragment); }}
                  onSave={() => { void handleSave(); }}
                  onQueryNameChange={handleQueryNameChange}
                  isExecuting={isExecuting}
                  isSaving={isSaving}
                  isDirty={isDirty}
                  queryId={activeQueryId}
                  queryName={activeQueryName}
                  queryDescription={activeQueryDescription}
                />
              )}
              bottom={(
                <QueryDataView
                  mode="playground"
                  result={result}
                  error={error}
                  isExecuting={isExecuting}
                  isPageLoading={isPageLoading}
                  currentSql={sql}
                  exportAttachmentBaseName={exportAttachmentBaseName}
                  pageSize={queryPageSize}
                  onPageSizeChange={(size) => { void handlePageSizeChange(size); }}
                  onFormatSql={handleFormatSql}
                  onCopySql={handleCopySql}
                  onClearSql={handleClearSql}
                  sqlCopied={sqlCopied}
                  onPageChange={(pageIndex) => { void handlePageChange(pageIndex); }}
                />
              )}
            />
          ) : (
            <Box style={{ height: '100%', minHeight: 0, display: 'flex', flexDirection: 'column' }}>
              <QueryEditor
                sql={sql}
                onChange={handleSqlChange}
                onExecute={(sqlFragment) => { void handleExecute(sqlFragment); }}
                onSave={() => { void handleSave(); }}
                onQueryNameChange={handleQueryNameChange}
                isExecuting={isExecuting}
                isSaving={isSaving}
                isDirty={isDirty}
                queryId={activeQueryId}
                queryName={activeQueryName}
                queryDescription={activeQueryDescription}
              />
            </Box>
          )
        ) : (
          /* Empty state */
          <Box
            style={{
              height: '100%',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <Box
              style={{
                width: 80,
                height: 80,
                borderRadius: '50%',
                backgroundColor: isDark ? 'var(--mantine-color-cyan-9)' : 'var(--mantine-color-teal-1)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                marginBottom: 24,
              }}
            >
              <HiOutlineCommandLine
                size={36}
                color={isDark ? 'var(--mantine-color-cyan-4)' : 'var(--mantine-color-teal-6)'}
              />
            </Box>
            <Text size="xl" fw={600} c={isDark ? 'gray.1' : 'gray.8'} mb="xs">
              Query Playground
            </Text>
            <Text size="sm" c="dimmed" ta="center" maw={400}>
              Select a saved query from the sidebar or create a new one to explore your data model.
            </Text>
          </Box>
        )
      }
    />
  );
}
