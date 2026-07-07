import { ActionIcon, Group, Menu, Tooltip, useMantineColorScheme } from '@mantine/core';
import { useCallback, useEffect, useState } from 'react';
import {
  HiOutlineArrowsPointingOut,
  HiOutlineArrowTopRightOnSquare,
  HiOutlineArrowDownTray,
  HiOutlineCheck,
  HiOutlineClipboardDocument,
  HiOutlinePlay,
  HiOutlineXMark,
} from 'react-icons/hi2';
import { notifications } from '@mantine/notifications';
import { downloadSqlExport, fetchExportFormats, type ExportFormatInfo } from '../../../services/api';
import type { ArtifactActionId } from './types';
import {
  artifactToolbarActionIconProps,
  artifactToolbarIconColor,
} from './artifactToolbar';
import { ArtifactToolbarIcon } from './ArtifactToolbarIcon';

export interface ChatArtifactActionBarProps {
  enabledActions: ArtifactActionId[];
  onRun?: () => void;
  onCopy?: () => void;
  onExpand?: () => void;
  onOpenInAnalysis?: () => void;
  onOpenInModel?: () => void;
  onAccept?: () => void;
  onReject?: () => void;
  isRunning?: boolean;
  isLifecycleBusy?: boolean;
  copyCopied?: boolean;
  copyTooltip?: string;
  disableRun?: boolean;
  /** SQL to export when the export action is enabled. */
  exportSql?: string;
  exportAttachmentBaseName?: string;
  /** When true and no actions are enabled, reserve action-bar width for layout parity with SQL artefacts. */
  reserveLayout?: boolean;
}

export function ChatArtifactActionBar({
  enabledActions,
  onRun,
  onCopy,
  onExpand,
  onOpenInAnalysis,
  onOpenInModel,
  onAccept,
  onReject,
  isRunning = false,
  isLifecycleBusy = false,
  copyCopied = false,
  copyTooltip = 'Copy SQL',
  disableRun = false,
  exportSql = '',
  exportAttachmentBaseName = 'chat-query',
  reserveLayout = false,
}: ChatArtifactActionBarProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const idleColor = artifactToolbarIconColor(isDark);
  const actionIconProps = artifactToolbarActionIconProps;
  const show = (id: ArtifactActionId) => enabledActions.includes(id);
  const [exportFormats, setExportFormats] = useState<ExportFormatInfo[]>([]);
  const [exportFormatsLoading, setExportFormatsLoading] = useState(false);
  const [exportingFormatId, setExportingFormatId] = useState<string | null>(null);

  useEffect(() => {
    if (!show('export')) return;
    let cancelled = false;
    setExportFormatsLoading(true);
    void fetchExportFormats()
      .then((formats) => {
        if (!cancelled) setExportFormats(formats);
      })
      .catch(() => {
        if (!cancelled) setExportFormats([]);
      })
      .finally(() => {
        if (!cancelled) setExportFormatsLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [enabledActions.join(',')]);

  const runExport = useCallback(
    async (formatId: string) => {
      const sql = exportSql.trim();
      if (!sql) return;
      const meta = exportFormats.find((f) => f.id.toLowerCase() === formatId.toLowerCase());
      const ext = (meta?.fileExtension?.trim() || formatId).replace(/^\./, '');
      setExportingFormatId(formatId);
      try {
        await downloadSqlExport(sql, formatId, {
          filenameHint: `${exportAttachmentBaseName}.${ext}`,
          attachmentBaseName: exportAttachmentBaseName,
        });
      } catch (e) {
        notifications.show({
          color: 'red',
          title: 'Export failed',
          message: e instanceof Error ? e.message : 'Unknown error',
        });
      } finally {
        setExportingFormatId(null);
      }
    },
    [exportAttachmentBaseName, exportFormats, exportSql],
  );

  const hasExportSql = Boolean(exportSql.trim());
  const hasVisibleActions =
    (show('run') && onRun) ||
    (show('copy') && onCopy) ||
    show('export') ||
    (show('expand') && onExpand) ||
    (show('open-in-analysis') && onOpenInAnalysis) ||
    (show('open-in-model') && onOpenInModel) ||
    (show('accept') && onAccept) ||
    (show('reject') && onReject);

  if (reserveLayout && !hasVisibleActions) {
    return <Group gap={4} justify="flex-start" wrap="nowrap" style={{ minWidth: 140, minHeight: 28 }} aria-hidden />;
  }

  return (
    <Group gap={4} justify="flex-start" wrap="nowrap">
      {show('run') && onRun ? (
        <Tooltip label="Run query" withArrow>
          <ActionIcon {...actionIconProps} color={idleColor} onClick={onRun} loading={isRunning} disabled={disableRun}>
            <ArtifactToolbarIcon icon={HiOutlinePlay} />
          </ActionIcon>
        </Tooltip>
      ) : null}
      {show('copy') && onCopy ? (
        <Tooltip label={copyCopied ? 'Copied!' : copyTooltip} withArrow>
          <ActionIcon
            {...actionIconProps}
            color={copyCopied ? 'teal' : idleColor}
            onClick={onCopy}
            aria-label={copyTooltip}
          >
            <ArtifactToolbarIcon icon={HiOutlineClipboardDocument} />
          </ActionIcon>
        </Tooltip>
      ) : null}
      {show('reject') && onReject ? (
        <Tooltip label="Reject facet" withArrow>
          <ActionIcon
            {...actionIconProps}
            color="red"
            onClick={onReject}
            loading={isLifecycleBusy}
            aria-label="Reject"
          >
            <ArtifactToolbarIcon icon={HiOutlineXMark} />
          </ActionIcon>
        </Tooltip>
      ) : null}
      {show('accept') && onAccept ? (
        <Tooltip label="Accept facet" withArrow>
          <ActionIcon
            {...actionIconProps}
            color="teal"
            onClick={onAccept}
            loading={isLifecycleBusy}
            aria-label="Accept"
          >
            <ArtifactToolbarIcon icon={HiOutlineCheck} />
          </ActionIcon>
        </Tooltip>
      ) : null}
      {show('export') ? (
        <Menu shadow="md" width={200} position="bottom-end" withArrow>
          <Menu.Target>
            <Tooltip label="Export" withArrow>
              <ActionIcon
                {...actionIconProps}
                color={idleColor}
                disabled={!hasExportSql || exportFormatsLoading || exportFormats.length === 0 || exportingFormatId != null}
                loading={exportFormatsLoading || exportingFormatId != null}
              >
                <ArtifactToolbarIcon icon={HiOutlineArrowDownTray} />
              </ActionIcon>
            </Tooltip>
          </Menu.Target>
          <Menu.Dropdown>
            <Menu.Label>Export as</Menu.Label>
            {exportFormats.length === 0 ? <Menu.Item disabled>No formats available</Menu.Item> : null}
            {exportFormats.map((format) => (
              <Menu.Item
                key={format.id}
                onClick={() => void runExport(format.id)}
                disabled={!hasExportSql || exportingFormatId != null}
              >
                {format.id}
                {' '}
                (
                {format.fileExtension}
                )
              </Menu.Item>
            ))}
          </Menu.Dropdown>
        </Menu>
      ) : null}
      {show('expand') && onExpand ? (
        <Tooltip label="Expand" withArrow>
          <ActionIcon {...actionIconProps} color={idleColor} onClick={onExpand}>
            <ArtifactToolbarIcon icon={HiOutlineArrowsPointingOut} />
          </ActionIcon>
        </Tooltip>
      ) : null}
      {show('open-in-analysis') && onOpenInAnalysis ? (
        <Tooltip label="Open in Analysis" withArrow>
          <ActionIcon {...actionIconProps} color={idleColor} onClick={onOpenInAnalysis}>
            <ArtifactToolbarIcon icon={HiOutlineArrowTopRightOnSquare} />
          </ActionIcon>
        </Tooltip>
      ) : null}
      {show('open-in-model') && onOpenInModel ? (
        <Tooltip label="Open in model" withArrow>
          <ActionIcon {...actionIconProps} color={idleColor} onClick={onOpenInModel} aria-label="Open in model">
            <ArtifactToolbarIcon icon={HiOutlineArrowTopRightOnSquare} />
          </ActionIcon>
        </Tooltip>
      ) : null}
    </Group>
  );
}
