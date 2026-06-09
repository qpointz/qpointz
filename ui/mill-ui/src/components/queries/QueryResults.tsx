import {
  Box,
  Text,
  Group,
  Badge,
  ScrollArea,
  Menu,
  ActionIcon,
  Tooltip,
  Select,
  useMantineColorScheme,
} from '@mantine/core';
import { useMemo, useCallback, useEffect, type ReactNode } from 'react';
import {
  useReactTable,
  getCoreRowModel,
  getSortedRowModel,
  flexRender,
  createColumnHelper,
  type SortingState,
} from '@tanstack/react-table';
import { useState } from 'react';
import {
  HiOutlineTableCells,
  HiOutlineClock,
  HiChevronUp,
  HiChevronDown,
  HiOutlineArrowDownTray,
  HiOutlineSparkles,
  HiOutlineClipboardDocument,
  HiOutlineTrash,
  HiChevronLeft,
  HiChevronRight,
} from 'react-icons/hi2';
import type { QueryResult } from '../../types/query';
import { useFeatureFlags } from '../../features/FeatureFlagContext';
import { downloadSqlExport, fetchExportFormats } from '../../services/exportService';
import type { ExportFormatInfo } from '../../services/exportHelpers';
import { LARGE_RESULT_PREVIEW_THRESHOLD } from '../../services/queryRowFormat';
import { QUERY_PAGE_SIZE_OPTIONS } from '../../services/queryService';
import { notifications } from '@mantine/notifications';
import { ColumnTypeBadge, isNumericColumnType } from './columnTypeIcon';

interface QueryResultsProps {
  result: QueryResult | null;
  error: string | null;
  isExecuting: boolean;
  isPageLoading?: boolean;
  pageSize?: number;
  onPageSizeChange?: (pageSize: number) => void;
  /** SQL executed by {@code POST /services/export/sql}. */
  currentSql?: string;
  /** Sanitized attachment base name for export downloads. */
  exportAttachmentBaseName?: string;
  onFormatSql?: () => void;
  onCopySql?: () => void;
  onClearSql?: () => void;
  sqlCopied?: boolean;
  onPageChange?: (pageIndex: number) => void;
}

type RowData = Record<string, string | number | boolean | null>;

/** Formats a numeric grid cell without locale thousand separators. */
export function formatQueryResultNumber(value: number): string {
  return String(value);
}

export function formatResultRowLabel(result: QueryResult): string {
  const { page, rowCount } = result;
  if (rowCount === 0) {
    return page.totalResult != null
      ? `0 of ${page.totalResult.toLocaleString()} rows`
      : '0 rows';
  }
  const start = page.pageIndex * page.pageSize + 1;
  const end = start + rowCount - 1;
  if (page.totalResult != null) {
    return `${start.toLocaleString()}–${end.toLocaleString()} of ${page.totalResult.toLocaleString()}`;
  }
  return `${rowCount} ${rowCount === 1 ? 'row' : 'rows'}`;
}

export function QueryResults({
  result,
  error,
  isExecuting,
  isPageLoading = false,
  pageSize,
  onPageSizeChange,
  currentSql = '',
  exportAttachmentBaseName = 'query-results',
  onFormatSql,
  onCopySql,
  onClearSql,
  sqlCopied = false,
  onPageChange,
}: QueryResultsProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const flags = useFeatureFlags();
  const [sorting, setSorting] = useState<SortingState>([]);
  const [exportFormats, setExportFormats] = useState<ExportFormatInfo[]>([]);
  const [exportFormatsLoading, setExportFormatsLoading] = useState(false);
  const [exportFormatsFailed, setExportFormatsFailed] = useState(false);
  const [exportingFormatId, setExportingFormatId] = useState<string | null>(null);

  const hasSql = Boolean(currentSql.trim());
  const iconColor = isDark ? 'gray.4' : 'gray.6';
  const totalPages = result?.page.totalResult != null && result.page.pageSize > 0
    ? Math.max(1, Math.ceil(result.page.totalResult / result.page.pageSize))
    : null;

  useEffect(() => {
    let cancelled = false;
    setExportFormatsLoading(true);
    setExportFormatsFailed(false);
    void fetchExportFormats()
      .then((formats) => {
        if (!cancelled) {
          setExportFormats(formats);
        }
      })
      .catch(() => {
        if (!cancelled) {
          setExportFormatsFailed(true);
          setExportFormats([]);
        }
      })
      .finally(() => {
        if (!cancelled) {
          setExportFormatsLoading(false);
        }
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const runSqlExport = useCallback(async (formatId: string) => {
    const sql = currentSql.trim();
    if (!sql) {
      return;
    }
    const meta = exportFormats.find((format) => format.id.toLowerCase() === formatId.toLowerCase());
    const ext = (meta?.fileExtension?.trim() || formatId).replace(/^\./, '');
    setExportingFormatId(formatId);
    try {
      await downloadSqlExport(sql, formatId, {
        filenameHint: `${exportAttachmentBaseName}.${ext}`,
        attachmentBaseName: exportAttachmentBaseName,
      });
    } catch (error) {
      notifications.show({
        color: 'red',
        title: 'Export failed',
        message: error instanceof Error ? error.message : 'Unknown error',
      });
    } finally {
      setExportingFormatId(null);
    }
  }, [currentSql, exportAttachmentBaseName, exportFormats]);

  const columnHelper = createColumnHelper<RowData>();

  const columns = useMemo(() => {
    if (!result) return [];
    return result.columns.map((col) => {
      const numeric = isNumericColumnType(col.type);
      return columnHelper.accessor((row) => row[col.name], {
        id: col.name,
        meta: { numeric },
        header: () => (
          <Group gap={6} wrap="nowrap" align="center">
            <Text size="xs" fw={600} style={{ whiteSpace: 'nowrap' }}>
              {col.name}
            </Text>
            <ColumnTypeBadge type={col.type} />
          </Group>
        ),
        cell: (info) => {
          const val = info.getValue();
          if (val === null) {
            return (
              <Text size="xs" c="dimmed" fs="italic" ta={numeric ? 'right' : 'left'}>
                NULL
              </Text>
            );
          }
          if (typeof val === 'number') {
            return (
              <Text
                size="xs"
                ff="monospace"
                ta="right"
                style={{ whiteSpace: 'nowrap' }}
              >
                {formatQueryResultNumber(val)}
              </Text>
            );
          }
          return (
            <Text size="xs" style={{ whiteSpace: 'nowrap' }} ta={numeric ? 'right' : 'left'}>
              {String(val)}
            </Text>
          );
        },
      });
    });
  }, [result, columnHelper]);

  const table = useReactTable({
    data: result?.rows ?? [],
    columns,
    state: { sorting },
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
  });

  const borderColor = 'var(--mantine-color-default-border)';
  const headerBg = isDark ? 'var(--mantine-color-dark-8)' : 'var(--mantine-color-gray-0)';
  const evenRowBg = isDark ? 'rgba(255,255,255,0.02)' : 'rgba(0,0,0,0.015)';
  const hoverBg = isDark ? 'rgba(255,255,255,0.04)' : 'rgba(0,0,0,0.03)';

  const statusBar = (
    <Group
      justify="space-between"
      px="sm"
      py={6}
      style={{
        borderBottom: `1px solid ${borderColor}`,
        backgroundColor: headerBg,
        flexShrink: 0,
      }}
    >
      <Group gap="xs" wrap="nowrap">
        <Text size="xs" fw={600} c={isDark ? 'gray.4' : 'gray.5'} tt="uppercase" lts={0.5}>
          Results
        </Text>
        {onPageSizeChange && pageSize != null && (
          <Select
            size="xs"
            w={88}
            data={QUERY_PAGE_SIZE_OPTIONS.map((size) => ({
              value: String(size),
              label: String(size),
            }))}
            value={String(pageSize)}
            onChange={(value) => {
              if (value) {
                onPageSizeChange(Number(value));
              }
            }}
            disabled={isPageLoading || isExecuting}
            aria-label="Rows per page"
            comboboxProps={{ withinPortal: true }}
          />
        )}
        {result && (
          <>
            <Badge size="xs" variant="light" color={isDark ? 'cyan' : 'teal'}>
              {formatResultRowLabel(result)}
            </Badge>
            {result.page.totalResult != null && result.page.totalResult > LARGE_RESULT_PREVIEW_THRESHOLD && (
              <Tooltip
                label="The grid loads one page at a time. Use Export for the full result set."
                withArrow
              >
                <Text size="xs" c="dimmed" style={{ cursor: 'default' }}>
                  Preview only
                </Text>
              </Tooltip>
            )}
            {onPageChange && (result.page.hasPrevious || result.page.hasNext) && (
              <Group gap={2} wrap="nowrap">
                <Tooltip label="Previous page" withArrow>
                  <ActionIcon
                    variant="subtle"
                    size="sm"
                    color={iconColor}
                    disabled={!result.page.hasPrevious || isPageLoading || isExecuting}
                    onClick={() => onPageChange(result.page.pageIndex - 1)}
                    aria-label="Previous page"
                  >
                    <HiChevronLeft size={14} />
                  </ActionIcon>
                </Tooltip>
                <Text size="xs" c="dimmed" style={{ minWidth: 48, textAlign: 'center' }}>
                  {totalPages != null
                    ? `Page ${result.page.pageIndex + 1} / ${totalPages}`
                    : `Page ${result.page.pageIndex + 1}`}
                </Text>
                <Tooltip label="Next page" withArrow>
                  <ActionIcon
                    variant="subtle"
                    size="sm"
                    color={iconColor}
                    disabled={!result.page.hasNext || isPageLoading || isExecuting}
                    onClick={() => onPageChange(result.page.pageIndex + 1)}
                    aria-label="Next page"
                  >
                    <HiChevronRight size={14} />
                  </ActionIcon>
                </Tooltip>
              </Group>
            )}
          </>
        )}
      </Group>
      <Group gap="sm">
        <Group gap={4}>
          {flags.analysisFormatSql && onFormatSql && (
            <Tooltip label="Format SQL" withArrow>
              <ActionIcon
                variant="subtle"
                size="sm"
                color={iconColor}
                onClick={onFormatSql}
                disabled={!hasSql}
              >
                <HiOutlineSparkles size={14} />
              </ActionIcon>
            </Tooltip>
          )}
          {flags.analysisCopySql && onCopySql && (
            <Tooltip label={sqlCopied ? 'Copied!' : 'Copy SQL'} withArrow>
              <ActionIcon
                variant="subtle"
                size="sm"
                color={sqlCopied ? 'teal' : iconColor}
                onClick={onCopySql}
                disabled={!hasSql}
              >
                <HiOutlineClipboardDocument size={14} />
              </ActionIcon>
            </Tooltip>
          )}
          {flags.analysisClearSql && onClearSql && (
            <Tooltip label="Clear" withArrow>
              <ActionIcon
                variant="subtle"
                size="sm"
                color={iconColor}
                onClick={onClearSql}
                disabled={!hasSql}
              >
                <HiOutlineTrash size={14} />
              </ActionIcon>
            </Tooltip>
          )}
        </Group>
        {result && (
          <Group gap="xs">
            <HiOutlineClock size={12} color={isDark ? 'var(--mantine-color-gray-4)' : 'var(--mantine-color-gray-5)'} />
            <Text size="xs" c="dimmed">
              {result.executionTimeMs}ms
            </Text>
          </Group>
        )}
        <Menu shadow="md" width={200} position="bottom-end" withArrow>
          <Menu.Target>
            <Tooltip
              label={
                exportFormatsFailed
                  ? 'Export formats unavailable'
                  : exportFormatsLoading
                    ? 'Loading export formats...'
                    : 'Export query via server'
              }
              withArrow
            >
              <ActionIcon
                variant="subtle"
                size="sm"
                color={iconColor}
                disabled={
                  !hasSql
                  || exportFormatsLoading
                  || exportFormats.length === 0
                  || exportingFormatId != null
                }
                loading={exportFormatsLoading || exportingFormatId != null}
              >
                <HiOutlineArrowDownTray size={14} />
              </ActionIcon>
            </Tooltip>
          </Menu.Target>
          <Menu.Dropdown>
            <Menu.Label>Export as</Menu.Label>
            {exportFormatsFailed ? (
              <Menu.Item disabled>Could not load formats</Menu.Item>
            ) : exportFormats.length === 0 ? (
              <Menu.Item disabled>No formats available</Menu.Item>
            ) : (
              exportFormats.map((format) => (
                <Menu.Item
                  key={format.id}
                  onClick={() => { void runSqlExport(format.id); }}
                  disabled={!hasSql || exportingFormatId != null}
                >
                  {format.id}
                  {' '}
                  (
                  {format.fileExtension}
                  )
                </Menu.Item>
              ))
            )}
          </Menu.Dropdown>
        </Menu>
      </Group>
    </Group>
  );

  let content: ReactNode;

  if (isExecuting) {
    content = (
      <Box
        style={{
          flex: 1,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        <Text size="sm" c="dimmed">
          Executing query...
        </Text>
      </Box>
    );
  } else if (isPageLoading) {
    content = (
      <Box
        style={{
          flex: 1,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        <Text size="sm" c="dimmed">
          Loading page...
        </Text>
      </Box>
    );
  } else if (error) {
    content = (
      <Box
        style={{
          flex: 1,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          padding: 24,
        }}
      >
        <Text size="sm" c="red" ta="center" ff="monospace">
          {error}
        </Text>
      </Box>
    );
  } else if (!result) {
    content = (
      <Box
        style={{
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        <Box
          style={{
            width: 56,
            height: 56,
            borderRadius: '50%',
            backgroundColor: isDark ? 'var(--mantine-color-cyan-9)' : 'var(--mantine-color-teal-1)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            marginBottom: 16,
          }}
        >
          <HiOutlineTableCells
            size={28}
            color={isDark ? 'var(--mantine-color-cyan-4)' : 'var(--mantine-color-teal-6)'}
          />
        </Box>
        <Text size="sm" c="dimmed" ta="center" maw={280}>
          Run a query to see results here
        </Text>
      </Box>
    );
  } else {
    content = (
      <ScrollArea style={{ flex: 1, minHeight: 0 }} type="auto">
        <table
          style={{
            width: '100%',
            borderCollapse: 'collapse',
            tableLayout: 'auto',
            fontSize: 13,
          }}
        >
          <thead>
            {table.getHeaderGroups().map((headerGroup) => (
              <tr key={headerGroup.id}>
                {headerGroup.headers.map((header, colIndex) => {
                  const isLast = colIndex === headerGroup.headers.length - 1;
                  const meta = header.column.columnDef.meta as { numeric?: boolean } | undefined;
                  const numeric = meta?.numeric ?? false;
                  return (
                    <th
                      key={header.id}
                      onClick={header.column.getToggleSortingHandler()}
                      style={{
                        padding: '8px 12px',
                        textAlign: numeric ? 'right' : 'left',
                        borderBottom: `2px solid ${borderColor}`,
                        backgroundColor: headerBg,
                        position: 'sticky',
                        top: 0,
                        zIndex: 1,
                        cursor: header.column.getCanSort() ? 'pointer' : 'default',
                        userSelect: 'none',
                        whiteSpace: isLast ? 'normal' : 'nowrap',
                        width: isLast ? undefined : '1%',
                        verticalAlign: 'bottom',
                      }}
                    >
                      <Group
                        gap={4}
                        wrap="nowrap"
                        justify={numeric ? 'flex-end' : 'flex-start'}
                        align="center"
                      >
                        {flexRender(header.column.columnDef.header, header.getContext())}
                        {header.column.getIsSorted() === 'asc' && <HiChevronUp size={12} />}
                        {header.column.getIsSorted() === 'desc' && <HiChevronDown size={12} />}
                      </Group>
                    </th>
                  );
                })}
              </tr>
            ))}
          </thead>
          <tbody>
            {table.getRowModel().rows.map((row, idx) => (
              <tr
                key={row.id}
                style={{
                  backgroundColor: idx % 2 === 1 ? evenRowBg : 'transparent',
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.backgroundColor = hoverBg;
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.backgroundColor = idx % 2 === 1 ? evenRowBg : 'transparent';
                }}
              >
                {row.getVisibleCells().map((cell, colIndex) => {
                  const isLast = colIndex === row.getVisibleCells().length - 1;
                  const meta = cell.column.columnDef.meta as { numeric?: boolean } | undefined;
                  const numeric = meta?.numeric ?? false;
                  return (
                    <td
                      key={cell.id}
                      style={{
                        padding: '6px 12px',
                        borderBottom: `1px solid ${borderColor}`,
                        whiteSpace: isLast ? 'normal' : 'nowrap',
                        width: isLast ? undefined : '1%',
                        textAlign: numeric ? 'right' : 'left',
                        verticalAlign: 'top',
                        maxWidth: isLast ? undefined : 420,
                        overflow: isLast ? undefined : 'hidden',
                        textOverflow: isLast ? undefined : 'ellipsis',
                      }}
                    >
                      {flexRender(cell.column.columnDef.cell, cell.getContext())}
                    </td>
                  );
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </ScrollArea>
    );
  }

  return (
    <Box style={{ display: 'flex', flexDirection: 'column', height: '100%', minHeight: 0 }}>
      {statusBar}
      <Box style={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column' }}>
        {content}
      </Box>
    </Box>
  );
}
