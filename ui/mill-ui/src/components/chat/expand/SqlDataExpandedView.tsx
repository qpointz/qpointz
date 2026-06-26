import { Box, Stack } from '@mantine/core';
import { useCallback, useState } from 'react';
import { useFeatureFlags } from '../../../features/FeatureFlagContext';
import { sanitizeExportAttachmentBaseName } from '../../../services/exportHelpers';
import { mergeDataArtifactIntoMessage } from '../../../services/chatSqlExecution';
import { QueryDataView } from '../../data/QueryDataView';
import { ChatArtifactActionBar } from '../artifactPreview/ChatArtifactActionBar';
import { ChatArtifactCard } from '../artifactPreview/ChatArtifactCard';
import { resolveArtifactTreatment } from '../artifactPreview/chatArtifactTreatments';
import { deriveExpandTitle, useOpenInAnalysis } from '../artifactPreview/useOpenInAnalysis';
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
  const openInAnalysis = useOpenInAnalysis();
  const treatment = resolveArtifactTreatment(payload.chatType, 'sql-data-composite');
  const [copyCopied, setCopyCopied] = useState(false);
  const isReplayed = payload.message.restReplay === true;
  const exportBaseName = sanitizeExportAttachmentBaseName(payload.chatTitle ?? 'chat-query');
  const expandTitle = deriveExpandTitle(payload.message, payload.precedingUserQuestion);

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
    lazyEnabled: true,
    isReplayed,
    autoRunLive: !isReplayed && !payload.executionId,
    onDataArtifact: mergeDataArtifact,
  });

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
          <Stack gap="xs" style={{ flex: 1, minHeight: 0 }}>
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
            <Box style={{ flex: 1, minHeight: 240 }}>
              <QueryDataView
                mode="expanded"
                result={queryData.displayResult}
                error={queryData.displayError}
                isExecuting={queryData.isRunning}
                isPageLoading={queryData.isPageLoading}
                currentSql={payload.sql}
                exportAttachmentBaseName={exportBaseName}
                showExport={false}
                onPageChange={(pageIndex) => void queryData.handlePageChange(pageIndex)}
              />
            </Box>
          </Stack>
        </ChatArtifactCard>
      </Box>
    </Stack>
  );
}
