import { Box, Text, Group, Badge, ScrollArea, Menu, ActionIcon, Tooltip, useMantineColorScheme } from '@mantine/core';
import { useMemo, useCallback } from 'react';
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
  HiOutlineDocumentText,
  HiOutlineTableCells as HiOutlineExcel,
  HiOutlineCodeBracket,
} from 'react-icons/hi2';
import type { QueryResult } from '../../types/query';

interface QueryResultsProps {
  result: QueryResult | null;
  error: string | null;
  isExecuting: boolean;
}

type RowData = Record<string, string | number | boolean | null>;

function downloadFile(content: string, filename: string, mimeType: string) {
  const blob = new Blob([content], { type: mimeType });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  a.click();
  URL.revokeObjectURL(url);
}

export function QueryResults({ result, error, isExecuting }: QueryResultsProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const [sorting, setSorting] = useState<SortingState>([]);

  const exportCsv = useCallback(() => {
    if (!result) return;
    const headers = result.columns.map((c) => c.name);
    const rows = result.rows.map((row) =>
      headers.map((h) => {
        const v = row[h];
        if (v === null) return '';
        const s = String(v);
        return s.includes(',') || s.includes('"') || s.includes('\n') ? `"${s.replace(/"/g, '""')}"` : s;
      }).join(',')
    );
    downloadFile([headers.join(','), ...rows].join('\n'), 'query-results.csv', 'text/csv');
  }, [result]);

  const exportJson = useCallback(() => {
    if (!result) return;
    downloadFile(JSON.stringify(result.rows, null, 2), 'query-results.json', 'application/json');
  }, [result]);

  const exportExcel = useCallback(() => {
    // Export as TSV which Excel can open natively
    if (!result) return;
    const headers = result.columns.map((c) => c.name);
    const rows = result.rows.map((row) =>
      headers.map((h) => (row[h] === null ? '' : String(row[h]))).join('\t')
    );
    downloadFile([headers.join('\t'), ...rows].join('\n'), 'query-results.xls', 'application/vnd.ms-excel');
  }, [result]);

  const columnHelper = createColumnHelper<RowData>();

  const columns = useMemo(() => {
    if (!result) return [];
    return result.columns.map((col) =>
      columnHelper.accessor((row) => row[col.name], {
        id: col.name,
        header: () => (
          <Group gap={4} wrap="nowrap">
            <Text size="xs" fw={600} style={{ whiteSpace: 'nowrap' }}>
              {col.name}
            </Text>
            <Badge size="xs" variant="outline" color="gray" style={{ textTransform: 'lowercase' }}>
              {col.type}
            </Badge>
          </Group>
        ),
        cell: (info) => {
          const val = info.getValue();
          if (val === null) {
            return (
              <Text size="xs" c="dimmed" fs="italic">
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
                {typeof val === 'number' && !Number.isInteger(val)
                  ? val.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
                  : val.toLocaleString()}
              </Text>
            );
          }
          return (
            <Text size="xs" style={{ whiteSpace: 'nowrap' }}>
              {String(val)}
            </Text>
          );
        },
      })
    );
  }, [result, columnHelper]);

  const table = useReactTable({
    data: result?.rows ?? [],
    columns,
    state: { sorting },
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
  });

  // Loading state
  if (isExecuting) {
    return (
      <Box
        style={{
          height: '100%',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        <Text size="sm" c="dimmed">
          Executing query...
        </Text>
      </Box>
    );
  }

  // Error state
  if (error) {
    return (
      <Box
        style={{
          height: '100%',
          display: 'flex',
          flexDirection: 'column',
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
  }

  // Empty state
  if (!result) {
    return (
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
  }

  // Results table
  const borderColor = 'var(--mantine-color-default-border)';
  const headerBg = isDark ? 'var(--mantine-color-dark-8)' : 'var(--mantine-color-gray-0)';
  const evenRowBg = isDark ? 'rgba(255,255,255,0.02)' : 'rgba(0,0,0,0.015)';
  const hoverBg = isDark ? 'rgba(255,255,255,0.04)' : 'rgba(0,0,0,0.03)';

  return (
    <Box style={{ display: 'flex', flexDirection: 'column', height: '100%', minHeight: 0 }}>
      {/* Status bar */}
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
        <Group gap="xs">
          <Text size="xs" fw={600} c={isDark ? 'gray.4' : 'gray.5'} tt="uppercase" lts={0.5}>
            Results
          </Text>
          <Badge size="xs" variant="light" color={isDark ? 'cyan' : 'teal'}>
            {result.rowCount} {result.rowCount === 1 ? 'row' : 'rows'}
          </Badge>
        </Group>
        <Group gap="sm">
          <Group gap="xs">
            <HiOutlineClock size={12} color={isDark ? 'var(--mantine-color-gray-4)' : 'var(--mantine-color-gray-5)'} />
            <Text size="xs" c="dimmed">
              {result.executionTimeMs}ms
            </Text>
          </Group>
          <Menu shadow="md" width={160} position="bottom-end" withArrow>
            <Menu.Target>
              <Tooltip label="Export results" withArrow>
                <ActionIcon
                  variant="subtle"
                  size="sm"
                  color={isDark ? 'gray.4' : 'gray.6'}
                >
                  <HiOutlineArrowDownTray size={14} />
                </ActionIcon>
              </Tooltip>
            </Menu.Target>
            <Menu.Dropdown>
              <Menu.Label>Export as</Menu.Label>
              <Menu.Item
                leftSection={<HiOutlineDocumentText size={14} />}
                onClick={exportCsv}
              >
                CSV
              </Menu.Item>
              <Menu.Item
                leftSection={<HiOutlineExcel size={14} />}
                onClick={exportExcel}
              >
                Excel
              </Menu.Item>
              <Menu.Item
                leftSection={<HiOutlineCodeBracket size={14} />}
                onClick={exportJson}
              >
                JSON
              </Menu.Item>
            </Menu.Dropdown>
          </Menu>
        </Group>
      </Group>

      {/* Table */}
      <ScrollArea style={{ flex: 1, minHeight: 0 }} type="auto">
        <table
          style={{
            width: '100%',
            borderCollapse: 'collapse',
            fontSize: 13,
          }}
        >
          <thead>
            {table.getHeaderGroups().map((headerGroup) => (
              <tr key={headerGroup.id}>
                {headerGroup.headers.map((header) => (
                  <th
                    key={header.id}
                    onClick={header.column.getToggleSortingHandler()}
                    style={{
                      padding: '8px 12px',
                      textAlign: 'left',
                      borderBottom: `2px solid ${borderColor}`,
                      backgroundColor: headerBg,
                      position: 'sticky',
                      top: 0,
                      zIndex: 1,
                      cursor: header.column.getCanSort() ? 'pointer' : 'default',
                      userSelect: 'none',
                      whiteSpace: 'nowrap',
                    }}
                  >
                    <Group gap={4} wrap="nowrap">
                      {flexRender(header.column.columnDef.header, header.getContext())}
                      {header.column.getIsSorted() === 'asc' && <HiChevronUp size={12} />}
                      {header.column.getIsSorted() === 'desc' && <HiChevronDown size={12} />}
                    </Group>
                  </th>
                ))}
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
                {row.getVisibleCells().map((cell) => (
                  <td
                    key={cell.id}
                    style={{
                      padding: '6px 12px',
                      borderBottom: `1px solid ${borderColor}`,
                    }}
                  >
                    {flexRender(cell.column.columnDef.cell, cell.getContext())}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </ScrollArea>
    </Box>
  );
}
