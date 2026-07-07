import { Box, Stack, Tabs } from '@mantine/core';
import { useCallback, useMemo, useState } from 'react';
import { HiOutlineTableCells } from 'react-icons/hi2';
import { TbChartBar, TbFileTypeSql } from 'react-icons/tb';
import { useFeatureFlags } from '../../../features/FeatureFlagContext';
import { sanitizeExportAttachmentBaseName } from '../../../services/exportHelpers';
import { mergeDataArtifactIntoMessage } from '../../../services/chatSqlExecution';
import { QueryDataView } from '../../data/QueryDataView';
import { ChartRenderer } from '../../charts/ChartRenderer';
import { toChartSnapshotRows, filterChartVisualizationsForColumns } from '../../charts/chartData';
import { ArtifactIconTab, useArtifactTabsStyles } from '../artifactPreview/ArtifactIconTab';
import { ChatArtifactActionBar } from '../artifactPreview/ChatArtifactActionBar';
import { ArtifactTabsRail, ChatArtifactCard } from '../artifactPreview/ChatArtifactCard';
import { ArtifactToolbarIcon } from '../artifactPreview/ArtifactToolbarIcon';
import { SqlArtifactPagination } from '../artifactPreview/SqlArtifactPagination';
import { SqlArtifactTabPane } from '../artifactPreview/SqlArtifactTabPane';
import { SqlArtifactToolbarRow } from '../artifactPreview/SqlArtifactToolbarRow';
import { resolveArtifactTreatment } from '../artifactPreview/chatArtifactTreatments';
import { deriveExpandTitle, useOpenInAnalysis } from '../artifactPreview/useOpenInAnalysis';
import { SqlReadOnlyPanel } from '../artifactPreview/SqlReadOnlyPanel';
import { useSqlArtifactQueryData } from '../artifactPreview/useSqlArtifactQueryData';
import type { ChatMessageArtifact } from '../../../types/chat';
import { ExpandHeader } from './ExpandHeader';
import type { ChatExpandPayload } from './useChatExpand';

interface SqlDataExpandedViewProps {
  payload: ChatExpandPayload;
  onClose: () => void;
}

export function SqlDataExpandedView({ payload, onClose }: SqlDataExpandedViewProps) {
  const flags = useFeatureFlags();
  const { styles: tabsStyles, classNames: tabsClassNames } = useArtifactTabsStyles();
  const openInAnalysis = useOpenInAnalysis();
  const treatment = resolveArtifactTreatment(payload.chatType, 'sql-data-composite');
  const [copyCopied, setCopyCopied] = useState(false);
  const exportBaseName = sanitizeExportAttachmentBaseName(payload.chatTitle ?? 'chat-query');
  const expandTitle = deriveExpandTitle(payload.message, payload.precedingUserQuestion);
  const sqlArtifact = payload.artifacts?.find((a): a is Extract<ChatMessageArtifact, { kind: 'sql' }> => a.kind === 'sql');
  const [activeTab, setActiveTab] = useState<string | null>(
    filterChartVisualizationsForColumns(
      sqlArtifact?.visualizations ?? [],
      sqlArtifact?.schema?.map((column) => column.name) ?? [],
    ).length
      ? 'chart'
      : 'data',
  );
  const [selectedChartKey, setSelectedChartKey] = useState(
    sqlArtifact?.visualizations?.[0]?.key ?? 'default',
  );

  const mergeDataArtifact = useCallback(
    (data: Extract<ChatMessageArtifact, { kind: 'data' }>) => {
      payload.onArtifactsChange?.(mergeDataArtifactIntoMessage(payload.artifacts, data));
    },
    [payload],
  );

  const queryData = useSqlArtifactQueryData({
    sql: payload.sql,
    conversationId: payload.message.conversationId,
    messageId: payload.message.id,
    parentArtifactId: payload.parentArtifactId,
    storedExecutionId: payload.executionId,
    lazyEnabled: activeTab === 'data' || activeTab === 'chart',
    autoRunLive: !payload.executionId,
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
  const selectedChart = useMemo(
    () => chartVisualizations.find((c) => c.key === selectedChartKey) ?? chartVisualizations[0],
    [chartVisualizations, selectedChartKey],
  );

  const chartRows = useMemo(
    () => toChartSnapshotRows(queryData.displayResult?.rows),
    [queryData.displayResult],
  );
  const chartTruncated =
    payload.message.artifacts?.find((artifact) => artifact.kind === 'data')?.truncated === true ||
    (queryData.displayResult != null &&
      queryData.displayResult.page.totalResult != null &&
      queryData.displayResult.rowCount < queryData.displayResult.page.totalResult);

  const handleCopy = useCallback(async () => {
    await navigator.clipboard.writeText(payload.sql);
    setCopyCopied(true);
    window.setTimeout(() => setCopyCopied(false), 1500);
  }, [payload.sql]);

  const actions = flags.chatSqlExecute
    ? treatment.actions ?? []
    : (treatment.actions ?? []).filter((a) => a !== 'run' && a !== 'export');

  return (
    <Stack gap={0} style={{ height: '100%', minHeight: 0 }}>
      <ExpandHeader title={expandTitle} onBack={onClose} />
      <Box px="md" py="sm" ref={queryData.containerRef} style={{ flex: 1, minHeight: 0, width: '100%' }}>
        <ChatArtifactCard
          p="xs"
          style={{ height: '100%', display: 'flex', flexDirection: 'column' }}
        >
          <Tabs
            keepMounted
            value={activeTab}
            onChange={setActiveTab}
            style={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column' }}
            styles={{
              ...tabsStyles,
              root: {
                ...tabsStyles.root,
                display: 'flex',
                flexDirection: 'column',
                flex: 1,
                minHeight: 0,
              },
              panel: {
                ...tabsStyles.panel,
                flex: 1,
                minHeight: 0,
                display: 'flex',
                flexDirection: 'column',
                overflow: 'hidden',
              },
            }}
            classNames={tabsClassNames}
          >
            <SqlArtifactToolbarRow
              actions={(
                <ChatArtifactActionBar
                  enabledActions={actions}
                  onRun={() => void queryData.handleRun()}
                  onCopy={handleCopy}
                  exportSql={payload.sql}
                  exportAttachmentBaseName={exportBaseName}
                  onOpenInAnalysis={() =>
                    openInAnalysis({
                      sql: payload.sql,
                      message: payload.message,
                      precedingUserQuestion: payload.precedingUserQuestion,
                      chatTitle: payload.chatTitle,
                    })}
                  isRunning={queryData.isRunning}
                  copyCopied={copyCopied}
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
            <Stack gap="xs" style={{ flex: 1, minHeight: 0, minWidth: 0, display: 'flex', flexDirection: 'column' }}>
              {hasCharts ? (
                <Tabs.Panel
                  value="chart"
                  style={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}
                >
                  <SqlArtifactTabPane variant="expanded">
                    {chartVisualizations.length > 1 ? (
                      <Tabs value={selectedChartKey} onChange={(v) => v && setSelectedChartKey(v)} mb="sm">
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
              <Tabs.Panel
                value="data"
                style={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}
              >
                <SqlArtifactTabPane variant="expanded">
                  <QueryDataView
                    mode="expanded"
                    result={queryData.displayResult}
                    error={queryData.displayError}
                    isExecuting={queryData.isRunning}
                    isPageLoading={queryData.isPageLoading}
                    currentSql={payload.sql}
                    exportAttachmentBaseName={exportBaseName}
                    showExport={false}
                    deferErrorMs={2500}
                    onPageChange={(pageIndex) => void queryData.handlePageChange(pageIndex)}
                  />
                </SqlArtifactTabPane>
              </Tabs.Panel>
              <Tabs.Panel
                value="sql"
                style={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}
              >
                <SqlArtifactTabPane variant="expanded">
                  <SqlReadOnlyPanel sql={payload.sql} fill />
                </SqlArtifactTabPane>
              </Tabs.Panel>
            </Stack>
          </Tabs>
        </ChatArtifactCard>
      </Box>
    </Stack>
  );
}
