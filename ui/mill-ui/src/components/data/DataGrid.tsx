import { Box, ScrollArea, Text, useMantineColorScheme } from '@mantine/core';
import type { QueryResult } from '../../types/query';

interface DataGridProps {
  result: QueryResult;
  maxHeight?: number;
}

export function DataGrid({ result, maxHeight }: DataGridProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const borderColor = 'var(--mantine-color-default-border)';
  const headerBg = isDark ? 'var(--mantine-color-dark-8)' : 'var(--mantine-color-gray-0)';
  const evenRowBg = isDark ? 'rgba(255,255,255,0.02)' : 'rgba(0,0,0,0.015)';

  const cellBase = {
    padding: '6px 12px',
    borderBottom: `1px solid ${borderColor}`,
    whiteSpace: 'nowrap' as const,
    verticalAlign: 'middle' as const,
    lineHeight: 1.25,
    maxWidth: 480,
    overflow: 'hidden',
    textOverflow: 'ellipsis',
  };

  return (
    <ScrollArea style={{ flex: 1, minHeight: 0, maxHeight }} type="auto" offsetScrollbars>
      <table
        style={{
          width: 'max-content',
          minWidth: '100%',
          borderCollapse: 'collapse',
          tableLayout: 'auto',
          fontSize: 13,
        }}
      >
        <thead>
          <tr>
            {result.columns.map((col) => (
              <th
                key={col.name}
                style={{
                  ...cellBase,
                  padding: '8px 12px',
                  borderBottom: `2px solid ${borderColor}`,
                  backgroundColor: headerBg,
                  position: 'sticky',
                  top: 0,
                  zIndex: 1,
                  textAlign: 'left',
                }}
              >
                <Text size="xs" fw={600} style={{ whiteSpace: 'nowrap' }}>
                  {col.name}
                </Text>
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {result.rows.map((row, rowIndex) => (
            <tr
              key={`row-${rowIndex}`}
              style={{
                backgroundColor: rowIndex % 2 === 1 ? evenRowBg : 'transparent',
                height: 32,
              }}
            >
              {result.columns.map((col) => {
                const value = row[col.name];
                return (
                  <td key={`${col.name}-${rowIndex}`} style={cellBase}>
                    {value === null ? (
                      <Text size="xs" c="dimmed" fs="italic" style={{ whiteSpace: 'nowrap' }}>
                        NULL
                      </Text>
                    ) : (
                      <Text size="xs" style={{ whiteSpace: 'nowrap' }}>
                        {String(value)}
                      </Text>
                    )}
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

interface DataStatePanelProps {
  message: string;
  color?: string;
  compact?: boolean;
}

export function DataStatePanel({ message, color = 'dimmed', compact = false }: DataStatePanelProps) {
  return (
    <Box
      style={{
        flex: compact ? undefined : 1,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: compact ? '8px 4px' : 24,
      }}
    >
      <Text size="sm" c={color}>
        {message}
      </Text>
    </Box>
  );
}
