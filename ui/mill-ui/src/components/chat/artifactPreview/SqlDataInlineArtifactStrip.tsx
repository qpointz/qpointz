import { Stack } from '@mantine/core';
import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  isAnalysisHostExecuting,
  isAnalysisAppliedArtifact,
  setAnalysisAppliedArtifactKey,
  subscribeAnalysisHostExecuting,
  subscribeAnalysisAppliedArtifact,
} from '../../queries/analysisHostState';
import { dispatchInlineHostAction } from './hostIntegrations';
import { SqlReadOnlyPanel } from './SqlReadOnlyPanel';
import { resolveArtifactTreatment } from './chatArtifactTreatments';
import { InlineArtifactStripActionBar } from './InlineArtifactStripActionBar';
import { InlineArtifactPillStrip } from './InlineArtifactPillStrip';
import { inlineSqlHeadline } from './inlineArtifactHeadline';
import {
  popoverActionsForInline,
  stripActionsForInline,
} from './inlineArtifactActionPlacement';
import { sqlStripDescription } from './inlineArtifactStripLabels';
import { resolveSqlArtifactApplyKey } from './inlineSqlArtifactKey';
import type { ArtifactPreviewContext } from './types';

/** Compact SQL proposal strip for inline Analysis copilot. */
export function SqlDataInlineArtifactStrip(props: ArtifactPreviewContext) {
  const { chatType, group, chatTitle, message } = props;
  if (group.kind !== 'sql-data-composite') {
    return null;
  }

  const sqlArtifact = group.sql;
  const sql = sqlArtifact?.sql ?? group.data?.sql ?? '';
  if (!sql.trim()) {
    return null;
  }

  const treatment = resolveArtifactTreatment(chatType, 'sql-data-composite');
  const treatmentActions = treatment.actions ?? [];
  const stripActions = stripActionsForInline('sql-data-composite', treatmentActions);
  const popoverActions = popoverActionsForInline('sql-data-composite', treatmentActions);
  const headline = inlineSqlHeadline(sqlArtifact, chatTitle);
  const description = sqlStripDescription(sqlArtifact);

  const applyKey = useMemo(
    () => resolveSqlArtifactApplyKey(message.id, sqlArtifact ?? { kind: 'sql', sql }, {
      messageArtifacts: message.artifacts,
    }),
    [message.artifacts, message.id, sql, sqlArtifact],
  );

  const [copyCopied, setCopyCopied] = useState(false);
  const [applied, setApplied] = useState(() => isAnalysisAppliedArtifact(applyKey));
  const [disableApplyAndRun, setDisableApplyAndRun] = useState(() => isAnalysisHostExecuting());

  useEffect(() => subscribeAnalysisHostExecuting(() => {
    setDisableApplyAndRun(isAnalysisHostExecuting());
  }), []);

  useEffect(() => {
    const syncApplied = () => setApplied(isAnalysisAppliedArtifact(applyKey));
    syncApplied();
    return subscribeAnalysisAppliedArtifact(syncApplied);
  }, [applyKey]);

  const artifact = sqlArtifact ?? { kind: 'sql' as const, sql };

  const handleApply = useCallback(() => {
    setAnalysisAppliedArtifactKey(applyKey);
    dispatchInlineHostAction(chatType, { type: 'sql.apply', artifact });
  }, [applyKey, artifact, chatType]);

  const handleApplyAndRun = useCallback(() => {
    setAnalysisAppliedArtifactKey(applyKey);
    dispatchInlineHostAction(chatType, { type: 'sql.applyAndRun', artifact });
  }, [applyKey, artifact, chatType]);

  const handleCopy = useCallback(async () => {
    const handled = dispatchInlineHostAction(chatType, { type: 'sql.copy', artifact });
    if (handled) {
      setCopyCopied(true);
      window.setTimeout(() => setCopyCopied(false), 1500);
      return;
    }
    try {
      await navigator.clipboard.writeText(sql);
      setCopyCopied(true);
      window.setTimeout(() => setCopyCopied(false), 1500);
    } catch {
      /* clipboard unavailable */
    }
  }, [artifact, chatType, sql]);

  return (
    <InlineArtifactPillStrip
      ariaLabel={`SQL proposal ${headline}`}
      typeBadge={{ kind: 'sql-data-composite' }}
      headline={headline}
      applied={applied}
      popoverTitle={headline}
      popoverSubtitle={description}
      stripActions={
        <InlineArtifactStripActionBar
          enabledActions={stripActions}
          onApply={handleApply}
          onApplyAndRun={handleApplyAndRun}
          disableApplyAndRun={disableApplyAndRun}
          applied={applied}
        />
      }
      popoverBody={
        <Stack gap="xs">
          <SqlReadOnlyPanel sql={sql} maxHeight={280} />
        </Stack>
      }
      popoverActions={
        popoverActions.length > 0 ? (
          <InlineArtifactStripActionBar
            enabledActions={popoverActions}
            onCopy={handleCopy}
            copyCopied={copyCopied}
          />
        ) : undefined
      }
    />
  );
}
