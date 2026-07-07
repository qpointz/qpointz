import { Box, Stack, Tabs } from '@mantine/core';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { HiOutlineTableCells } from 'react-icons/hi2';
import { TbChartBar, TbFileTypeSql } from 'react-icons/tb';
import type { ChatMessageArtifact } from '../../../types/chat';
import { useFeatureFlags } from '../../../features/FeatureFlagContext';
import { sanitizeExportAttachmentBaseName } from '../../../services/exportHelpers';
import { mergeDataArtifactIntoMessage } from '../../../services/chatSqlExecution';
import { QueryDataView } from '../../data/QueryDataView';
import { ChartRenderer } from '../../charts/ChartRenderer';
import { toChartSnapshotRows, filterChartVisualizationsForColumns } from '../../charts/chartData';
import type { ChartVisualizationConfig } from '../../charts/types';
import { useChatExpand } from '../expand/useChatExpand';
import { ArtifactIconTab, useArtifactTabsStyles } from './ArtifactIconTab';
import { ChatArtifactActionBar } from './ChatArtifactActionBar';
import { ArtifactTabsRail, ChatArtifactCard } from './ChatArtifactCard';
import { resolveArtifactTreatment, treatmentAllowsExpand } from './chatArtifactTreatments';
import { SqlReadOnlyPanel } from './SqlReadOnlyPanel';
import type { ArtifactPreviewContext } from './types';
import { useOpenInAnalysis } from './useOpenInAnalysis';
import { useSqlArtifactQueryData } from './useSqlArtifactQueryData';
import { ArtifactToolbarIcon } from './ArtifactToolbarIcon';
import { CONDENSED_ARTIFACT_CONTENT_HEIGHT } from './artifactContentLayout';
import { SqlArtifactPagination } from './SqlArtifactPagination';
import { SqlArtifactTabPane } from './SqlArtifactTabPane';
import { SqlArtifactToolbarRow } from './SqlArtifactToolbarRow';

export function SqlDataCondensedPreview(props: ArtifactPreviewContext) {
  const {
    chatType,
    message,
    group,
    conversationId,
    precedingUserQuestion,
    chatTitle,
    onArtifactsChange,
  } = props;
  if (group.kind !== 'sql-data-composite') {
    return null;
  }
  const flags = useFeatureFlags();
  const { styles: tabsStyles, classNames: tabsClassNames } = useArtifactTabsStyles();
  const openInAnalysis = useOpenInAnalysis();
  const { openExpand, runAllTick } = useChatExpand();
  const sqlArtifact = group.sql;
  const sql = sqlArtifact?.sql ?? group.data?.sql ?? '';
  const treatment = resolveArtifactTreatment(chatType, 'sql-data-composite');
  const isReplayed = message.restReplay === true;
  const exportBaseName = sanitizeExportAttachmentBaseName(chatTitle ?? 'chat-query');

  const mergeDataArtifact = useCallback(
    (data: Extract<ChatMessageArtifact, { kind: 'data' }>) => {
      onArtifactsChange?.(mergeDataArtifactIntoMessage(message.artifacts, data));
    },
    [message.artifacts, onArtifactsChange],
  );

  const initialSchemaColumns = sqlArtifact?.schema?.map((column) => column.name) ?? [];
  const initialCharts = filterChartVisualizationsForColumns(
    sqlArtifact?.visualizations ?? [],
    initialSchemaColumns,
  );

  const [activeTab, setActiveTab] = useState<string | null>(
    isReplayed ? (initialCharts.length ? 'chart' : 'sql') : initialCharts.length ? 'chart' : 'data',
  );
  const [selectedChartKey, setSelectedChartKey] = useState<string>(
    initialCharts[0]?.key ?? 'default',
  );
  const [copyCopied, setCopyCopied] = useState(false);

  const queryData = useSqlArtifactQueryData({
    sql,
    conversationId,
    messageId: message.id,
    parentArtifactId: sqlArtifact?.artifactId,
    storedExecutionId: group.data?.executionId,
    lazyEnabled: activeTab === 'data' || activeTab === 'chart',
    autoRunLive: !group.data?.executionId,
    onDataArtifact: mergeDataArtifact,
  });

  const chartColumnNames = useMemo(() => {
    const fromResult = queryData.displayResult?.columns.map((column) => column.name) ?? [];
    if (fromResult.length) return fromResult;
    return sqlArtifact?.schema?.map((column) => column.name) ?? [];
  }, [queryData.displayResult, sqlArtifact?.schema]);
  const chartVisualizations = useMemo(
    () => filterChartVisualizationsForColumns(sqlArtifact?.visualizations ?? [], chartColumnNames),
    [chartColumnNames, sqlArtifact?.visualizations],
  );
  const hasCharts = chartVisualizations.length > 0;

  const selectedChart: ChartVisualizationConfig | undefined = useMemo(
    () => chartVisualizations.find((chart) => chart.key === selectedChartKey) ?? chartVisualizations[0],
    [chartVisualizations, selectedChartKey],
  );

  useEffect(() => {
    if (!chartVisualizations.some((chart) => chart.key === selectedChartKey)) {
      setSelectedChartKey(chartVisualizations[0]?.key ?? 'default');
    }
  }, [chartVisualizations, selectedChartKey]);

  const handleRun = useCallback(async () => {
    setActiveTab('data');
    await queryData.handleRun();
  }, [queryData]);

  useEffect(() => {
    if (hasCharts) return;
    if (queryData.isRunning || queryData.displayResult) {
      setActiveTab('data');
    }
  }, [hasCharts, queryData.displayResult, queryData.isRunning]);

  useEffect(() => {
    if (runAllTick === 0) return;
    setActiveTab(hasCharts ? 'chart' : 'data');
  }, [runAllTick, hasCharts]);

  const chartRows = useMemo(
    () => toChartSnapshotRows(queryData.displayResult?.rows),
    [queryData.displayResult],
  );
  const chartTruncated =
    group.data?.truncated === true ||
    (queryData.displayResult != null &&
      queryData.displayResult.page.totalResult != null &&
      queryData.displayResult.rowCount < queryData.displayResult.page.totalResult);

  const handleCopy = useCallback(async () => {
    if (!sql.trim()) return;
    await navigator.clipboard.writeText(sql);
    setCopyCopied(true);
    window.setTimeout(() => setCopyCopied(false), 1500);
  }, [sql]);

  if (!sql.trim()) return null;

  const actions = flags.chatSqlExecute
    ? treatment.actions ?? []
    : (treatment.actions ?? []).filter((a) => a !== 'run' && a !== 'export');

  return (
    <ChatArtifactCard p="xs">
      <div ref={queryData.containerRef}>
        <Tabs
          keepMounted
          value={activeTab}
          onChange={setActiveTab}
          styles={{
            ...tabsStyles,
            root: { ...tabsStyles.root, display: 'flex', flexDirection: 'column' },
            panel: { ...tabsStyles.panel, paddingTop: 0 },
          }}
          classNames={tabsClassNames}
        >
          <SqlArtifactToolbarRow
            actions={(
              <ChatArtifactActionBar
                enabledActions={actions}
                onRun={() => void handleRun()}
                onCopy={handleCopy}
                exportSql={sql}
                exportAttachmentBaseName={exportBaseName}
                onExpand={
                  treatmentAllowsExpand(chatType, 'sql-data-composite')
                    ? () =>
                        openExpand({
                          messageId: message.id,
                          turnId: message.id,
                          chatType,
                          kind: 'sql-data-composite',
                          sql,
                          executionId: group.data?.executionId ?? queryData.displayResult?.page.executionId,
                          parentArtifactId: sqlArtifact?.artifactId,
                          message,
                          precedingUserQuestion,
                          chatTitle,
                          artifacts: message.artifacts,
                          onArtifactsChange,
                        })
                    : undefined
                }
                onOpenInAnalysis={() =>
                  openInAnalysis({ sql, message, precedingUserQuestion, chatTitle })}
                isRunning={queryData.isRunning}
                copyCopied={copyCopied}
                disableRun={!sql.trim()}
              />
            )}
            pagination={
              activeTab === 'data' ? (
                <SqlArtifactPagination
                  result={queryData.displayResult}
                  isPageLoading={queryData.isPageLoading}
                  isExecuting={queryData.isRunning}
                  onPageChange={(pageIndex) => void queryData.handlePageChange(pageIndex)}
                />
              ) : null
            }
            tabs={(
              <ArtifactTabsRail>
                <Tabs.List>
                  {hasCharts ? (
                    <ArtifactIconTab
                      value="chart"
                      label="Chart"
                      icon={<ArtifactToolbarIcon icon={TbChartBar} />}
                    />
                  ) : null}
                  <ArtifactIconTab
                    value="data"
                    label="Data"
                    icon={<ArtifactToolbarIcon icon={HiOutlineTableCells} />}
                  />
                  <ArtifactIconTab
                    value="sql"
                    label="SQL"
                    icon={<ArtifactToolbarIcon icon={TbFileTypeSql} />}
                  />
                </Tabs.List>
              </ArtifactTabsRail>
            )}
          />
          <Box
            style={{
              height: CONDENSED_ARTIFACT_CONTENT_HEIGHT,
              minHeight: CONDENSED_ARTIFACT_CONTENT_HEIGHT,
              display: 'flex',
              flexDirection: 'column',
            }}
          >
            <Stack gap={4} style={{ flex: 1, minHeight: 0, minWidth: 0 }}>
              {hasCharts ? (
                <Tabs.Panel value="chart" style={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column' }}>
                  <SqlArtifactTabPane variant="condensed">
                    {chartVisualizations.length > 1 ? (
                      <Tabs
                        value={selectedChartKey}
                        onChange={(value) => value && setSelectedChartKey(value)}
                        mb={4}
                      >
                        <Tabs.List>
                          {chartVisualizations.map((chart) => (
                            <Tabs.Tab key={chart.key} value={chart.key}>
                              {chart.title ?? chart.key}
                            </Tabs.Tab>
                          ))}
                        </Tabs.List>
                      </Tabs>
                    ) : null}
                    {selectedChart ? (
                      <Box style={{ flex: 1, minHeight: 0 }}>
                        <ChartRenderer
                          config={selectedChart}
                          rows={chartRows}
                          truncated={chartTruncated}
                          height="100%"
                        />
                      </Box>
                    ) : null}
                  </SqlArtifactTabPane>
                </Tabs.Panel>
              ) : null}
              <Tabs.Panel value="data" style={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column' }}>
                <SqlArtifactTabPane variant="condensed">
                  <QueryDataView
                    mode="condensed"
                    result={queryData.displayResult}
                    error={queryData.displayError}
                    isExecuting={queryData.isRunning}
                    isPageLoading={queryData.isPageLoading}
                    currentSql={sql}
                    exportAttachmentBaseName={exportBaseName}
                    showExport={false}
                    deferErrorMs={2500}
                    onPageChange={(pageIndex) => void queryData.handlePageChange(pageIndex)}
                  />
                </SqlArtifactTabPane>
              </Tabs.Panel>
              <Tabs.Panel value="sql" style={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column' }}>
                <SqlArtifactTabPane variant="condensed">
                  <SqlReadOnlyPanel sql={sql} fill />
                </SqlArtifactTabPane>
              </Tabs.Panel>
            </Stack>
          </Box>
        </Tabs>
      </div>
    </ChatArtifactCard>
  );
}
