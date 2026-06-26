import { Group, Stack, Tabs } from '@mantine/core';
import { useCallback, useEffect, useState } from 'react';
import type { ChatMessageArtifact } from '../../../types/chat';
import { useFeatureFlags } from '../../../features/FeatureFlagContext';
import { sanitizeExportAttachmentBaseName } from '../../../services/exportHelpers';
import { mergeDataArtifactIntoMessage } from '../../../services/chatSqlExecution';
import { QueryDataView } from '../../data/QueryDataView';
import { useChatExpand } from '../expand/useChatExpand';
import { ChatArtifactActionBar } from './ChatArtifactActionBar';
import { ChatArtifactCard } from './ChatArtifactCard';
import { resolveArtifactTreatment, treatmentAllowsExpand } from './chatArtifactTreatments';
import { SqlReadOnlyPanel } from './SqlReadOnlyPanel';
import type { ArtifactPreviewContext } from './types';
import { useOpenInAnalysis } from './useOpenInAnalysis';
import { useSqlArtifactQueryData } from './useSqlArtifactQueryData';

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
  const openInAnalysis = useOpenInAnalysis();
  const { openExpand, runAllTick } = useChatExpand();
  const sqlArtifact = group.sql;
  const sql = sqlArtifact?.sql ?? group.data?.sql ?? '';
  const treatment = resolveArtifactTreatment(chatType, 'sql-data-composite');
  const isReplayed = message.restReplay === true;
  const exportBaseName = sanitizeExportAttachmentBaseName(chatTitle ?? 'chat-query');

  const [activeTab, setActiveTab] = useState<string | null>(isReplayed ? 'sql' : 'data');
  const [copyCopied, setCopyCopied] = useState(false);

  const mergeDataArtifact = useCallback(
    (data: Extract<ChatMessageArtifact, { kind: 'data' }>) => {
      onArtifactsChange?.(mergeDataArtifactIntoMessage(message.artifacts, data));
    },
    [message.artifacts, onArtifactsChange],
  );

  const queryData = useSqlArtifactQueryData({
    sql,
    conversationId,
    messageId: message.id,
    parentArtifactId: sqlArtifact?.artifactId,
    storedExecutionId: group.data?.executionId,
    lazyEnabled: activeTab === 'data',
    isReplayed,
    autoRunLive: true,
    onDataArtifact: mergeDataArtifact,
  });

  const handleRun = useCallback(async () => {
    setActiveTab('data');
    await queryData.handleRun();
  }, [queryData]);

  useEffect(() => {
    if (queryData.isRunning || queryData.displayResult) {
      setActiveTab('data');
    }
  }, [queryData.displayResult, queryData.isRunning]);

  useEffect(() => {
    if (runAllTick === 0) return;
    setActiveTab('data');
  }, [runAllTick]);

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
      <Stack gap={4} ref={queryData.containerRef}>
        <Tabs value={activeTab} onChange={setActiveTab}>
          <Group justify="space-between" align="center" wrap="nowrap" gap={4}>
            <Tabs.List style={{ flexWrap: 'nowrap' }}>
              <Tabs.Tab value="data">Data</Tabs.Tab>
              <Tabs.Tab value="sql">SQL</Tabs.Tab>
            </Tabs.List>
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
          </Group>
          <Tabs.Panel value="data" pt={4}>
            <div style={{ maxHeight: 280, display: 'flex', flexDirection: 'column' }}>
              <QueryDataView
                mode="condensed"
                result={queryData.displayResult}
                error={queryData.displayError}
                isExecuting={queryData.isRunning}
                isPageLoading={queryData.isPageLoading}
                currentSql={sql}
                exportAttachmentBaseName={exportBaseName}
                showExport={false}
                onPageChange={(pageIndex) => void queryData.handlePageChange(pageIndex)}
              />
            </div>
          </Tabs.Panel>
          <Tabs.Panel value="sql" pt={4}>
            <SqlReadOnlyPanel sql={sql} maxHeight={160} />
          </Tabs.Panel>
        </Tabs>
      </Stack>
    </ChatArtifactCard>
  );
}
