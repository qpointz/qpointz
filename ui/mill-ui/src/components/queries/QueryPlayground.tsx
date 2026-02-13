import { Box, useMantineColorScheme, Text, Badge, ActionIcon, Tooltip } from '@mantine/core';
import { useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router';
import { useEffect } from 'react';
import { HiOutlineCommandLine, HiOutlineBeaker, HiOutlinePlus } from 'react-icons/hi2';
import { QuerySidebar } from './QuerySidebar';
import { QueryEditor } from './QueryEditor';
import { QueryResults } from './QueryResults';
import { CollapsibleSidebar } from '../common/CollapsibleSidebar';
import { queryService } from '../../services/api';
import { useFeatureFlags } from '../../features/FeatureFlagContext';
import { useInlineChatListener } from '../../context/InlineChatContext';
import type { SavedQuery, QueryResult } from '../../types/query';

/**
 * Extract the first SQL code block from markdown content.
 * Looks for ```sql ... ``` fenced blocks.
 */
function extractSqlFromMarkdown(content: string): string | null {
  const match = content.match(/```sql\s*\n([\s\S]*?)```/i);
  return match?.[1] ? match[1].trim() : null;
}

let newQueryCounter = 0;

export function QueryPlayground() {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const flags = useFeatureFlags();
  const navigate = useNavigate();
  const params = useParams<{ queryId?: string }>();

  const [sql, setSql] = useState('');
  const [isExecuting, setIsExecuting] = useState(false);
  const [result, setResult] = useState<QueryResult | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [activeQueryId, setActiveQueryId] = useState<string | null>(null);
  const [activeQueryName, setActiveQueryName] = useState<string | null>(null);
  const [activeQueryDescription, setActiveQueryDescription] = useState<string | null>(null);
  const [userQueries, setUserQueries] = useState<SavedQuery[]>([]);
  const [savedQueries, setSavedQueries] = useState<SavedQuery[]>([]);

  // Load saved queries on mount
  useEffect(() => {
    queryService.getSavedQueries().then(setSavedQueries).catch(() => setSavedQueries([]));
  }, []);

  const allQueries = [...userQueries, ...savedQueries];

  // Sync URL params to selected query
  useEffect(() => {
    if (params.queryId) {
      // Look in user queries first (local state), then fetch from service
      const userQuery = userQueries.find((q) => q.id === params.queryId);
      if (userQuery) {
        setSql(userQuery.sql);
        setActiveQueryId(userQuery.id);
        setActiveQueryName(userQuery.name);
        setActiveQueryDescription(userQuery.description ?? null);
        setResult(null);
        setError(null);
      } else {
        queryService.getSavedQueryById(params.queryId).then((query) => {
          if (query) {
            setSql(query.sql);
            setActiveQueryId(query.id);
            setActiveQueryName(query.name);
            setActiveQueryDescription(query.description ?? null);
            setResult(null);
            setError(null);
          }
        }).catch(() => {
          // Query not found -- ignore
        });
      }
    }
  }, [params.queryId, userQueries]);

  // Listen to inline chat AI responses -- extract SQL and update editor
  useInlineChatListener(activeQueryId ?? '__analysis__', (content) => {
    const extracted = extractSqlFromMarkdown(content);
    if (extracted) {
      setSql(extracted);
    }
  });

  const handleSelectQuery = useCallback((query: SavedQuery) => {
    setSql(query.sql);
    setActiveQueryId(query.id);
    setActiveQueryName(query.name);
    setActiveQueryDescription(query.description ?? null);
    setResult(null);
    setError(null);
    navigate(`/analysis/${query.id}`);
  }, [navigate]);

  const handleSqlChange = useCallback((newSql: string) => {
    setSql(newSql);
    // If user edits the SQL, it's no longer a saved query
    // (but keep the ID for sidebar highlighting)
  }, []);

  const handleExecute = useCallback(async () => {
    if (!sql.trim() || isExecuting) return;

    setIsExecuting(true);
    setError(null);
    setResult(null);

    try {
      const queryResult = await queryService.executeQuery(sql);
      setResult(queryResult);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An unknown error occurred');
    } finally {
      setIsExecuting(false);
    }
  }, [sql, isExecuting]);

  const handleNewQuery = useCallback(() => {
    newQueryCounter += 1;
    const now = Date.now();
    const newQuery: SavedQuery = {
      id: `new-query-${now}`,
      name: `New Query ${newQueryCounter}`,
      sql: '',
      createdAt: now,
      updatedAt: now,
    };
    setUserQueries((prev) => [newQuery, ...prev]);
    setSql('');
    setActiveQueryId(newQuery.id);
    setActiveQueryName(newQuery.name);
    setActiveQueryDescription(null);
    setResult(null);
    setError(null);
    navigate(`/analysis/${newQuery.id}`);
  }, [navigate]);

  const handleDeleteQuery = useCallback((queryId: string) => {
    // Remove from user queries
    setUserQueries((prev) => prev.filter((q) => q.id !== queryId));

    // If the deleted query was active, reset the editor
    if (activeQueryId === queryId) {
      // Find the next query to select (prefer the one after, else before, else nothing)
      const currentIndex = allQueries.findIndex((q) => q.id === queryId);
      const remaining = allQueries.filter((q) => q.id !== queryId);
      const nextQuery = remaining[Math.min(currentIndex, remaining.length - 1)];

      if (nextQuery) {
        setSql(nextQuery.sql);
        setActiveQueryId(nextQuery.id);
        setActiveQueryName(nextQuery.name);
        setActiveQueryDescription(nextQuery.description ?? null);
        setResult(null);
        setError(null);
        navigate(`/analysis/${nextQuery.id}`);
      } else {
        setSql('');
        setActiveQueryId(null);
        setActiveQueryName(null);
        setActiveQueryDescription(null);
        setResult(null);
        setError(null);
        navigate('/analysis');
      }
    }
  }, [activeQueryId, allQueries, navigate]);

  // Check if there's content (SQL or results) to show
  const hasContent = sql.trim() || result || error || activeQueryId;

  return (
    <Box
      style={{
        display: 'flex',
        height: '100%',
        overflow: 'hidden',
      }}
    >
      {/* Sidebar */}
      <CollapsibleSidebar
        icon={HiOutlineBeaker}
        title="Sample Queries"
        headerRight={
          <>
            {flags.sidebarAnalysisBadge && (
              <Badge size="xs" variant="light" color={isDark ? 'cyan' : 'teal'}>
                {allQueries.length}
              </Badge>
            )}
            <Tooltip label="New query" withArrow>
              <ActionIcon
                size="sm"
                variant="subtle"
                color={isDark ? 'cyan' : 'teal'}
                onClick={handleNewQuery}
              >
                <HiOutlinePlus size={14} />
              </ActionIcon>
            </Tooltip>
          </>
        }
      >
        <QuerySidebar
          queries={allQueries}
          activeQueryId={activeQueryId}
          onSelectQuery={handleSelectQuery}
          onDeleteQuery={handleDeleteQuery}
        />
      </CollapsibleSidebar>

      {/* Main Content */}
      <Box
        style={{
          flex: 1,
          backgroundColor: 'var(--mantine-color-body)',
          overflow: 'hidden',
          display: 'flex',
          flexDirection: 'column',
        }}
      >
        {hasContent ? (
          <>
            {/* Editor panel (top) */}
            <Box
              style={{
                height: '45%',
                minHeight: 200,
                display: 'flex',
                flexDirection: 'column',
                borderBottom: `2px solid var(--mantine-color-default-border)`,
              }}
            >
              <QueryEditor
                sql={sql}
                onChange={handleSqlChange}
                onExecute={handleExecute}
                isExecuting={isExecuting}
                queryId={activeQueryId}
                queryName={activeQueryName}
                queryDescription={activeQueryDescription}
              />
            </Box>

            {/* Results panel (bottom) */}
            {flags.analysisQueryResults && (
              <Box
                style={{
                  flex: 1,
                  minHeight: 0,
                  display: 'flex',
                  flexDirection: 'column',
                }}
              >
                <QueryResults
                  result={result}
                  error={error}
                  isExecuting={isExecuting}
                />
              </Box>
            )}
          </>
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
              Select a sample query from the sidebar or start writing SQL to explore your data model.
            </Text>
          </Box>
        )}
      </Box>
    </Box>
  );
}
