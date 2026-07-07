import { Box, Loader, Text, useMantineColorScheme } from '@mantine/core';
import { useLayoutEffect, useRef, useState } from 'react';
import type { QueryResult } from '../../types/query';

/** Space reserved when a horizontal scrollbar would cover the last visible row. */
const HORIZONTAL_SCROLLBAR_RESERVE_PX = 14;

interface DataGridProps {
  result: QueryResult;
  maxHeight?: number;
  /** When true, keeps the grid layout and shows a loading overlay (paged fetch). */
  loading?: boolean;
}

export function DataGrid({ result, maxHeight, loading = false }: DataGridProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const borderColor = 'var(--mantine-color-default-border)';
  const headerBg = isDark ? 'var(--mantine-color-dark-8)' : 'var(--mantine-color-gray-0)';
  const evenRowBg = isDark ? 'rgba(255,255,255,0.02)' : 'rgba(0,0,0,0.015)';
  const scrollRef = useRef<HTMLDivElement>(null);
  const [reserveHorizontalScrollbarSpace, setReserveHorizontalScrollbarSpace] = useState(false);

  useLayoutEffect(() => {
    const element = scrollRef.current;
    if (!element) return undefined;

    const updateScrollbarReserve = () => {
      setReserveHorizontalScrollbarSpace(element.scrollWidth > element.clientWidth);
    };

    updateScrollbarReserve();
    const observer = new ResizeObserver(updateScrollbarReserve);
    observer.observe(element);
    const table = element.querySelector('table');
    if (table) observer.observe(table);
    return () => observer.disconnect();
  }, [result]);

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
    <Box
      style={{
        flex: 1,
        minHeight: 0,
        maxHeight,
        position: 'relative',
        display: 'flex',
        flexDirection: 'column',
      }}
    >
      <Box
        ref={scrollRef}
        style={{
          flex: 1,
          minHeight: 0,
          overflow: 'auto',
          boxSizing: 'border-box',
          scrollbarGutter: 'stable',
          paddingBottom: reserveHorizontalScrollbarSpace ? HORIZONTAL_SCROLLBAR_RESERVE_PX : undefined,
          opacity: loading ? 0.45 : 1,
          transition: 'opacity 120ms ease',
          pointerEvents: loading ? 'none' : 'auto',
        }}
      >
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
      </Box>
      {loading ? (
        <Box
          style={{
            position: 'absolute',
            inset: 0,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 2,
            pointerEvents: 'none',
          }}
        >
          <Loader size="sm" type="dots" />
        </Box>
      ) : null}
    </Box>
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
        minHeight: compact ? 140 : 220,
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
