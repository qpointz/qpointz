import { ActionIcon, Group, Menu, Select, Text, Tooltip } from '@mantine/core';
import { HiChevronLeft, HiChevronRight, HiOutlineArrowDownTray, HiOutlineClipboardDocument, HiOutlineSparkles, HiOutlineTrash } from 'react-icons/hi2';
import type { ExportFormatInfo } from '../../services/api';
import { QUERY_PAGE_SIZE_OPTIONS } from '../../services/queryService';

interface DataToolbarProps {
  title: string;
  pageSize?: number;
  onPageSizeChange?: (value: number) => void;
  disablePaginationControls: boolean;
  hasPrevious: boolean;
  hasNext: boolean;
  pageLabel: string;
  onPrevPage?: () => void;
  onNextPage?: () => void;
  showSqlActions: boolean;
  hasSql: boolean;
  sqlCopied: boolean;
  onFormatSql?: () => void;
  onCopySql?: () => void;
  onClearSql?: () => void;
  exportFormats: ExportFormatInfo[];
  exportFormatsLoading: boolean;
  exportFormatsFailed: boolean;
  exportingFormatId: string | null;
  onExport: (formatId: string) => void;
  showExport?: boolean;
}

export function DataToolbar({
  title,
  pageSize,
  onPageSizeChange,
  disablePaginationControls,
  hasPrevious,
  hasNext,
  pageLabel,
  onPrevPage,
  onNextPage,
  showSqlActions,
  hasSql,
  sqlCopied,
  onFormatSql,
  onCopySql,
  onClearSql,
  exportFormats,
  exportFormatsLoading,
  exportFormatsFailed,
  exportingFormatId,
  onExport,
  showExport = true,
}: DataToolbarProps) {
  return (
    <Group justify="space-between" px="sm" py={6} style={{ borderBottom: '1px solid var(--mantine-color-default-border)' }}>
      <Group gap="xs">
        <Text size="xs" fw={600} c="dimmed" tt="uppercase">
          {title}
        </Text>
        {onPageSizeChange && pageSize != null ? (
          <Select
            size="xs"
            w={88}
            data={QUERY_PAGE_SIZE_OPTIONS.map((size) => ({ value: String(size), label: String(size) }))}
            value={String(pageSize)}
            onChange={(value) => value && onPageSizeChange(Number(value))}
            disabled={disablePaginationControls}
            aria-label="Rows per page"
            comboboxProps={{ withinPortal: true }}
          />
        ) : null}
        <Group gap={2}>
          <Tooltip label="Previous page" withArrow>
            <ActionIcon variant="subtle" size="sm" disabled={disablePaginationControls || !hasPrevious} onClick={onPrevPage}>
              <HiChevronLeft size={14} />
            </ActionIcon>
          </Tooltip>
          <Text size="xs" c="dimmed" style={{ minWidth: 84, textAlign: 'center' }}>
            {pageLabel}
          </Text>
          <Tooltip label="Next page" withArrow>
            <ActionIcon variant="subtle" size="sm" disabled={disablePaginationControls || !hasNext} onClick={onNextPage}>
              <HiChevronRight size={14} />
            </ActionIcon>
          </Tooltip>
        </Group>
      </Group>
      <Group gap={4}>
        {showSqlActions ? (
          <>
            {onFormatSql ? (
              <Tooltip label="Format SQL" withArrow>
                <ActionIcon variant="subtle" size="sm" onClick={onFormatSql} disabled={!hasSql}>
                  <HiOutlineSparkles size={14} />
                </ActionIcon>
              </Tooltip>
            ) : null}
            {onCopySql ? (
              <Tooltip label={sqlCopied ? 'Copied!' : 'Copy SQL'} withArrow>
                <ActionIcon variant="subtle" size="sm" color={sqlCopied ? 'teal' : undefined} onClick={onCopySql} disabled={!hasSql}>
                  <HiOutlineClipboardDocument size={14} />
                </ActionIcon>
              </Tooltip>
            ) : null}
            {onClearSql ? (
              <Tooltip label="Clear SQL" withArrow>
                <ActionIcon variant="subtle" size="sm" onClick={onClearSql} disabled={!hasSql}>
                  <HiOutlineTrash size={14} />
                </ActionIcon>
              </Tooltip>
            ) : null}
          </>
        ) : null}
        {showExport ? (
          <Menu shadow="md" width={200} position="bottom-end" withArrow>
          <Menu.Target>
            <ActionIcon
              variant="subtle"
              size="sm"
              disabled={!hasSql || exportFormatsLoading || exportFormats.length === 0 || exportingFormatId != null}
              loading={exportFormatsLoading || exportingFormatId != null}
            >
              <HiOutlineArrowDownTray size={14} />
            </ActionIcon>
          </Menu.Target>
          <Menu.Dropdown>
            <Menu.Label>Export as</Menu.Label>
            {exportFormatsFailed ? <Menu.Item disabled>Could not load formats</Menu.Item> : null}
            {!exportFormatsFailed && exportFormats.length === 0 ? <Menu.Item disabled>No formats available</Menu.Item> : null}
            {exportFormats.map((format) => (
              <Menu.Item key={format.id} onClick={() => onExport(format.id)} disabled={!hasSql || exportingFormatId != null}>
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
      </Group>
    </Group>
  );
}
