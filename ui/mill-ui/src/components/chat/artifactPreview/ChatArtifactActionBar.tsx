import { ActionIcon, Group, Menu, Tooltip } from '@mantine/core';
import { useCallback, useEffect, useState } from 'react';
import {
  HiOutlineArrowsPointingOut,
  HiOutlineArrowTopRightOnSquare,
  HiOutlineArrowDownTray,
  HiOutlineClipboardDocument,
  HiOutlinePlay,
} from 'react-icons/hi2';
import { notifications } from '@mantine/notifications';
import { downloadSqlExport, fetchExportFormats, type ExportFormatInfo } from '../../../services/api';
import type { ArtifactActionId } from './types';

export interface ChatArtifactActionBarProps {
  enabledActions: ArtifactActionId[];
  onRun?: () => void;
  onCopy?: () => void;
  onExpand?: () => void;
  onOpenInAnalysis?: () => void;
  isRunning?: boolean;
  copyCopied?: boolean;
  disableRun?: boolean;
  /** SQL to export when the export action is enabled. */
  exportSql?: string;
  exportAttachmentBaseName?: string;
}

export function ChatArtifactActionBar({
  enabledActions,
  onRun,
  onCopy,
  onExpand,
  onOpenInAnalysis,
  isRunning = false,
  copyCopied = false,
  disableRun = false,
  exportSql = '',
  exportAttachmentBaseName = 'chat-query',
}: ChatArtifactActionBarProps) {
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

  return (
    <Group gap={4} justify="flex-end" wrap="nowrap">
      {show('run') && onRun ? (
        <Tooltip label="Run query" withArrow>
          <ActionIcon variant="subtle" size="sm" onClick={onRun} loading={isRunning} disabled={disableRun}>
            <HiOutlinePlay size={14} />
          </ActionIcon>
        </Tooltip>
      ) : null}
      {show('copy') && onCopy ? (
        <Tooltip label={copyCopied ? 'Copied!' : 'Copy SQL'} withArrow>
          <ActionIcon variant="subtle" size="sm" color={copyCopied ? 'teal' : undefined} onClick={onCopy}>
            <HiOutlineClipboardDocument size={14} />
          </ActionIcon>
        </Tooltip>
      ) : null}
      {show('export') ? (
        <Menu shadow="md" width={200} position="bottom-end" withArrow>
          <Menu.Target>
            <Tooltip label="Export" withArrow>
              <ActionIcon
                variant="subtle"
                size="sm"
                disabled={!hasExportSql || exportFormatsLoading || exportFormats.length === 0 || exportingFormatId != null}
                loading={exportFormatsLoading || exportingFormatId != null}
              >
                <HiOutlineArrowDownTray size={14} />
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
          <ActionIcon variant="subtle" size="sm" onClick={onExpand}>
            <HiOutlineArrowsPointingOut size={14} />
          </ActionIcon>
        </Tooltip>
      ) : null}
      {show('open-in-analysis') && onOpenInAnalysis ? (
        <Tooltip label="Open in Analysis" withArrow>
          <ActionIcon variant="subtle" size="sm" onClick={onOpenInAnalysis}>
            <HiOutlineArrowTopRightOnSquare size={14} />
          </ActionIcon>
        </Tooltip>
      ) : null}
    </Group>
  );
}
