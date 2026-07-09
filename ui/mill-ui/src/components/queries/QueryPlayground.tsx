import { Box, useMantineColorScheme, Text, Badge, ActionIcon, Tooltip, Modal, Stack, Group, Button } from '@mantine/core';
import { useState, useCallback, useEffect, useRef } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router';
import { format as formatSQL } from 'sql-formatter';
import { HiOutlinePlus } from 'react-icons/hi2';
import { TbFileTypeSql } from 'react-icons/tb';
import { QuerySidebar } from './QuerySidebar';
import { QueryEditor } from './QueryEditor';
import { QueryDataView } from '../data/QueryDataView';
import { ExplorerSplitLayout } from '../layout/ExplorerSplitLayout';
import { queryService, analysisService } from '../../services/api';
import {
  normalizeQueryPageSize,
  readStoredQueryPageSize,
  storeQueryPageSize,
} from '../../services/queryService';
import { VerticalSplitPane } from '../layout/VerticalSplitPane';
import { useFeatureFlags } from '../../features/FeatureFlagContext';
import type { SavedQuery, QueryResult } from '../../types/query';
import { resolveSqlToExecute } from './resolveSqlToExecute';
import { sanitizeExportAttachmentBaseName } from '../../services/exportHelpers';
import { registerInlineHostHandler } from '../chat/artifactPreview/hostIntegrations';
import {
  registerInlineContextSnapshotProvider,
  unregisterInlineContextSnapshotProvider,
} from '../../context/inlineContextSnapshotRegistry';
import { buildAnalysisContextSnapshot } from '../../context/buildAnalysisContextSnapshot';
import {
  registerAnalysisArtifactListener,
  unregisterAnalysisArtifactListener,
  type AnalysisArtifactListener,
} from '../../context/analysisArtifactListeners';
import { useInlineChat } from '../../context/InlineChatContext';
import { useInlineChatHostBinding } from '../inline-chat/useInlineChatHostBinding';
import { resolveInlineChatRouteContextId } from '../inline-chat/inlineChatRouteContext';
import {
  shouldAutoApplyOnArrival,
  shouldAutoRunAfterApply,
} from '../inline-chat/analysisCopilotAutomation';
import type { AnalysisCopilotSettings } from '../../types/inlineChat';
import type { SqlCodeEditorHandle } from './SqlCodeEditor';
import { setAnalysisHostExecuting, setAnalysisAppliedArtifactKey } from './analysisHostState';
import { resolveSqlArtifactApplyKey } from '../chat/artifactPreview/inlineSqlArtifactKey';
import { DEFAULT_QUERY_PAGE_SIZE } from '../../services/queryService';
import type { ChatHandoffState } from '../chat/artifactPreview/useOpenInAnalysis';

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

type PendingQueryNavigation =
  | { kind: 'select'; query: SavedQuery }
  | { kind: 'new' }
  | { kind: 'route'; queryId: string };

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
  const [pendingNavigation, setPendingNavigation] = useState<PendingQueryNavigation | null>(null);
  const savedSnapshotRef = useRef(savedSnapshot);
  const savedNameSnapshotRef = useRef(savedNameSnapshot);
  const activeQueryNameRef = useRef(activeQueryName);
  const chatHandoffProcessedRef = useRef(false);
  const handleExecuteRef = useRef<(sqlFragment?: string) => Promise<void>>(async () => {});
  const sqlEditorRef = useRef<SqlCodeEditorHandle | null>(null);
  const copilotSettingsRef = useRef<AnalysisCopilotSettings>({
    'automation.mode': 'manual',
  });
  const [editorEpoch, setEditorEpoch] = useState(0);
  const { getSessionByContext } = useInlineChat();
  const dialectIdRef = useRef<string | null>(null);
  const analysisSnapshotRef = useRef({
    sql: '',
    dialectId: null as string | null,
    activeQueryId: null as string | null,
    activeQueryName: null as string | null,
    activeQueryDescription: null as string | null,
    isDirty: false,
    isExecuting: false,
    error: null as string | null,
    result: null as QueryResult | null,
    activeExecutionId: null as string | null,
  });

  savedSnapshotRef.current = savedSnapshot;
  savedNameSnapshotRef.current = savedNameSnapshot;
  activeQueryNameRef.current = activeQueryName;
  analysisSnapshotRef.current = {
    sql,
    dialectId: dialectIdRef.current,
    activeQueryId,
    activeQueryName,
    activeQueryDescription,
    isDirty,
    isExecuting,
    error,
    result,
    activeExecutionId: activeExecutionIdRef.current,
  };

  const routeAnalysisContextId = resolveInlineChatRouteContextId(location.pathname);
  const inlineAnalysisContextId =
    activeQueryId ?? routeAnalysisContextId ?? '__analysis__';
  const analysisSession = getSessionByContext('analysis', inlineAnalysisContextId);
  useInlineChatHostBinding({
    contextType: 'analysis',
    contextId: inlineAnalysisContextId,
    enabled: flags.inlineChatEnabled && flags.inlineChatAnalysisContext,
  });
  copilotSettingsRef.current = analysisSession?.settings ?? copilotSettingsRef.current;

  const applySqlToEditor = useCallback((nextSql: string, recordHistory = true) => {
    sqlEditorRef.current?.replaceSql(nextSql, recordHistory);
    setSql(nextSql);
    setIsDirty(
      computeIsDirty(
        nextSql,
        savedSnapshotRef.current,
        activeQueryNameRef.current,
        savedNameSnapshotRef.current,
      ),
    );
  }, []);

  useEffect(() => {
    registerInlineHostHandler('inline-analysis', (action) => {
      if (action.type === 'sql.copy') {
        const text = action.artifact.sql.trim();
        if (!text) return false;
        void navigator.clipboard.writeText(text).catch(() => {
          /* clipboard unavailable */
        });
        return true;
      }

      const nextSql = action.artifact.sql.trim();
      if (!nextSql) return false;

      applySqlToEditor(nextSql, true);

      const settings = copilotSettingsRef.current;
      const shouldRun =
        action.type === 'sql.applyAndRun' ||
        (action.type === 'sql.apply' && shouldAutoRunAfterApply(settings['automation.mode']));

      if (shouldRun) {
        void handleExecuteRef.current(nextSql);
      }
      return true;
    });
  }, [applySqlToEditor]);

  useEffect(() => {
    const listener: AnalysisArtifactListener = (artifact, meta) => {
      if (meta.proposalIndex > 0) return;
      const mode = meta.settings['automation.mode'];
      if (!shouldAutoApplyOnArrival(mode)) return;
      const nextSql = artifact.sql.trim();
      if (!nextSql) return;
      setAnalysisAppliedArtifactKey(
        resolveSqlArtifactApplyKey(meta.messageId, artifact, { proposalIndex: meta.proposalIndex }),
      );
      applySqlToEditor(nextSql, true);
      if (shouldAutoRunAfterApply(mode)) {
        void handleExecuteRef.current(nextSql);
      }
    };
    registerAnalysisArtifactListener(inlineAnalysisContextId, listener);
    return () => unregisterAnalysisArtifactListener(inlineAnalysisContextId, listener);
  }, [inlineAnalysisContextId, applySqlToEditor]);

  useEffect(() => {
    void analysisService.getDialect().then((dialect) => {
      dialectIdRef.current = dialect.id;
    });
  }, []);

  const inlineAnalysisContextIdForSnapshot = activeQueryId ?? '__analysis__';

  useEffect(() => {
    if (!flags.inlineChatAnalysisContext) return;
    registerInlineContextSnapshotProvider('analysis', inlineAnalysisContextIdForSnapshot, () =>
      buildAnalysisContextSnapshot(analysisSnapshotRef.current),
    );
    return () => unregisterInlineContextSnapshotProvider('analysis', inlineAnalysisContextIdForSnapshot);
  }, [inlineAnalysisContextIdForSnapshot, flags.inlineChatAnalysisContext]);

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

  const loadQueryIntoEditor = useCallback(async (query: SavedQuery) => {
    await closeActiveSession();
    applyLoadedQuery(query, querySetters);
  }, [closeActiveSession]);

  const resolveAndLoadQueryById = useCallback(async (queryId: string) => {
    try {
      const localQuery = savedQueries.find((q) => q.id === queryId);
      if (localQuery) {
        await loadQueryIntoEditor(localQuery);
        return;
      }

      const query = await queryService.getSavedQueryById(queryId);
      if (!query) {
        return;
      }
      setSavedQueries((prev) => (prev.some((q) => q.id === query.id) ? prev : [query, ...prev]));
      await loadQueryIntoEditor(query);
    } catch {
      // Query not found or failed to load — ignore.
    }
  }, [savedQueries, loadQueryIntoEditor]);

  const commitNewQuery = useCallback(async () => {
    try {
      newQueryCounter += 1;
      const name = `New Query ${newQueryCounter}`;
      const created = await queryService.createSavedQuery({ name, sql: '' });
      setSavedQueries((prev) => [created, ...prev]);
      await closeActiveSession();
      navigate(`/analysis/${created.id}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create query');
    }
  }, [closeActiveSession, navigate]);

  const navigateForPending = useCallback((pending: PendingQueryNavigation) => {
    switch (pending.kind) {
      case 'select':
        navigate(`/analysis/${pending.query.id}`);
        break;
      case 'route':
        navigate(`/analysis/${pending.queryId}`);
        break;
      case 'new':
        void commitNewQuery();
        break;
    }
  }, [navigate, commitNewQuery]);

  const requestNavigation = useCallback((pending: PendingQueryNavigation) => {
    if (!isDirty) {
      navigateForPending(pending);
      return;
    }
    setPendingNavigation(pending);
  }, [isDirty, navigateForPending]);

  const fulfillPendingNavigation = useCallback((pending: PendingQueryNavigation) => {
    navigateForPending(pending);
  }, [navigateForPending]);

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
        await closeActiveSession();
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
    navigate(`/analysis/${first.id}`, { replace: true });
  }, [params.queryId, activeQueryId, savedQueries, navigate, closeActiveSession, location.state]);

  useEffect(() => () => {
    void closeActiveSession();
  }, [closeActiveSession]);

  // Sync URL params to selected query (prompt when unsaved edits would be lost).
  useEffect(() => {
    if (!params.queryId || activeQueryId === params.queryId) {
      return;
    }

    if (isDirty) {
      requestNavigation({ kind: 'route', queryId: params.queryId });
      if (activeQueryId) {
        navigate(`/analysis/${activeQueryId}`, { replace: true });
      }
      return;
    }

    void resolveAndLoadQueryById(params.queryId);
  }, [
    params.queryId,
    activeQueryId,
    isDirty,
    navigate,
    requestNavigation,
    resolveAndLoadQueryById,
  ]);

  const handleSelectQuery = useCallback((query: SavedQuery) => {
    if (query.id === activeQueryId) {
      return;
    }
    requestNavigation({ kind: 'select', query });
  }, [activeQueryId, requestNavigation]);

  const performSave = useCallback(async (): Promise<boolean> => {
    if (isSaving) {
      return false;
    }

    setIsSaving(true);
    setError(null);
    try {
      if (!activeQueryId) {
        if (!sql.trim()) {
          return false;
        }
        newQueryCounter += 1;
        const name = activeQueryName?.trim() || `New Query ${newQueryCounter}`;
        const created = await queryService.createSavedQuery({
          name,
          sql,
          description: activeQueryDescription ?? undefined,
        });
        setSavedQueries((prev) => [created, ...prev]);
        navigate(`/analysis/${created.id}`);
        return true;
      }

      if (!activeQueryName || !isDirty) {
        return true;
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
      setEditorEpoch((epoch) => epoch + 1);
      return true;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save query');
      return false;
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

  const handleSave = useCallback(async () => {
    await performSave();
  }, [performSave]);

  const handleUnsavedSave = useCallback(async () => {
    const pending = pendingNavigation;
    if (!pending) {
      return;
    }
    const saved = await performSave();
    if (!saved) {
      return;
    }
    setPendingNavigation(null);
    fulfillPendingNavigation(pending);
  }, [pendingNavigation, performSave, fulfillPendingNavigation]);

  const handleUnsavedDiscard = useCallback(() => {
    const pending = pendingNavigation;
    if (!pending) {
      return;
    }
    setPendingNavigation(null);
    setIsDirty(false);
    fulfillPendingNavigation(pending);
  }, [pendingNavigation, fulfillPendingNavigation]);

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
    setAnalysisHostExecuting(true);
    setError(null);
    setResult(null);

    try {
      await closeActiveSession();
      const normalizedPageSize = normalizeQueryPageSize(DEFAULT_QUERY_PAGE_SIZE);
      if (queryPageSize !== normalizedPageSize) {
        setQueryPageSize(normalizedPageSize);
        storeQueryPageSize(normalizedPageSize);
      }
      const queryResult = await queryService.executeQuery(sqlToRun, { pageSize: normalizedPageSize });
      activeExecutionIdRef.current = queryResult.page.executionId;
      setResult(queryResult);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An unknown error occurred');
    } finally {
      setIsExecuting(false);
      setAnalysisHostExecuting(false);
    }
  }, [sql, isExecuting, closeActiveSession, queryPageSize]);

  handleExecuteRef.current = handleExecute;

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

  const handleNewQuery = useCallback(() => {
    requestNavigation({ kind: 'new' });
  }, [requestNavigation]);

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
    <>
    <ExplorerSplitLayout
      icon={TbFileTypeSql}
      title="Experiments"
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
                  editorEpoch={editorEpoch}
                  editorRef={sqlEditorRef}
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
                editorEpoch={editorEpoch}
                editorRef={sqlEditorRef}
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
              <TbFileTypeSql
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
    <Modal
      opened={pendingNavigation !== null}
      onClose={() => setPendingNavigation(null)}
      title="Unsaved changes"
      centered
    >
      <Stack gap="md">
        <Text size="sm" c={isDark ? 'gray.2' : 'gray.7'}>
          This query has unsaved changes. Save before switching?
        </Text>
        <Group justify="flex-end" gap="xs">
          <Button variant="subtle" color="gray" onClick={() => setPendingNavigation(null)}>
            Cancel
          </Button>
          <Button variant="light" color="red" onClick={() => { void handleUnsavedDiscard(); }}>
            Discard
          </Button>
          <Button
            color={isDark ? 'cyan' : 'teal'}
            loading={isSaving}
            onClick={() => { void handleUnsavedSave(); }}
          >
            Save
          </Button>
        </Group>
      </Stack>
    </Modal>
    </>
  );
}
