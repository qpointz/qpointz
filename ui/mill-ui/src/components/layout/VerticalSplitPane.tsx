import { Box, useMantineColorScheme } from '@mantine/core';
import { useCallback, useEffect, useRef, useState, type ReactNode, type PointerEvent as ReactPointerEvent } from 'react';

const DIVIDER_HEIGHT_PX = 6;

export interface VerticalSplitPaneProps {
  top: ReactNode;
  bottom: ReactNode;
  /** Initial top pane share (percent of usable height). */
  initialTopPercent?: number;
  minTopPx?: number;
  minBottomPx?: number;
  /** When set, split ratio is persisted in `localStorage`. */
  storageKey?: string;
}

function readStoredPercent(storageKey: string, fallback: number): number {
  try {
    const saved = localStorage.getItem(storageKey);
    if (!saved) {
      return fallback;
    }
    const value = Number(saved);
    if (Number.isFinite(value) && value >= 15 && value <= 85) {
      return value;
    }
  } catch {
    // Ignore storage errors.
  }
  return fallback;
}

/**
 * Vertical split layout with a draggable divider between top and bottom panes.
 */
export function VerticalSplitPane({
  top,
  bottom,
  initialTopPercent = 45,
  minTopPx = 160,
  minBottomPx = 120,
  storageKey,
}: VerticalSplitPaneProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const containerRef = useRef<HTMLDivElement>(null);
  const draggingRef = useRef(false);
  const topPercentRef = useRef(initialTopPercent);

  const [topPercent, setTopPercent] = useState(() =>
    storageKey ? readStoredPercent(storageKey, initialTopPercent) : initialTopPercent,
  );
  topPercentRef.current = topPercent;

  const persistPercent = useCallback((value: number) => {
    if (!storageKey) {
      return;
    }
    try {
      localStorage.setItem(storageKey, String(value));
    } catch {
      // Ignore storage errors.
    }
  }, [storageKey]);

  const updateFromPointer = useCallback((clientY: number) => {
    const container = containerRef.current;
    if (!container) {
      return;
    }
    const rect = container.getBoundingClientRect();
    const usable = rect.height - DIVIDER_HEIGHT_PX;
    if (usable <= 0) {
      return;
    }
    const minTopPercent = (minTopPx / usable) * 100;
    const maxTopPercent = 100 - (minBottomPx / usable) * 100;
    const offsetY = clientY - rect.top - DIVIDER_HEIGHT_PX / 2;
    const rawPercent = (offsetY / usable) * 100;
    const next = Math.min(maxTopPercent, Math.max(minTopPercent, rawPercent));
    setTopPercent(next);
    topPercentRef.current = next;
  }, [minBottomPx, minTopPx]);

  const endDrag = useCallback(() => {
    if (!draggingRef.current) {
      return;
    }
    draggingRef.current = false;
    document.body.style.cursor = '';
    document.body.style.userSelect = '';
    persistPercent(topPercentRef.current);
  }, [persistPercent]);

  useEffect(() => {
    const onPointerMove = (event: PointerEvent) => {
      if (!draggingRef.current) {
        return;
      }
      updateFromPointer(event.clientY);
    };
    window.addEventListener('pointermove', onPointerMove);
    window.addEventListener('pointerup', endDrag);
    window.addEventListener('pointercancel', endDrag);
    return () => {
      window.removeEventListener('pointermove', onPointerMove);
      window.removeEventListener('pointerup', endDrag);
      window.removeEventListener('pointercancel', endDrag);
    };
  }, [endDrag, updateFromPointer]);

  const onDividerPointerDown = (event: ReactPointerEvent<HTMLDivElement>) => {
    event.preventDefault();
    draggingRef.current = true;
    event.currentTarget.setPointerCapture(event.pointerId);
    document.body.style.cursor = 'row-resize';
    document.body.style.userSelect = 'none';
    updateFromPointer(event.clientY);
  };

  const bottomPercent = 100 - topPercent;
  const dividerColor = isDark ? 'var(--mantine-color-dark-4)' : 'var(--mantine-color-gray-3)';
  const dividerHoverColor = isDark ? 'var(--mantine-color-cyan-7)' : 'var(--mantine-color-teal-4)';

  return (
    <Box
      ref={containerRef}
      style={{
        height: '100%',
        minHeight: 0,
        display: 'flex',
        flexDirection: 'column',
      }}
    >
      <Box
        style={{
          flex: `${topPercent} 1 0`,
          minHeight: minTopPx,
          minWidth: 0,
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden',
        }}
      >
        {top}
      </Box>
      <Box
        role="separator"
        aria-orientation="horizontal"
        aria-valuenow={Math.round(topPercent)}
        onPointerDown={onDividerPointerDown}
        style={{
          flex: `0 0 ${DIVIDER_HEIGHT_PX}px`,
          cursor: 'row-resize',
          backgroundColor: dividerColor,
          transition: 'background-color 120ms ease',
        }}
        onMouseEnter={(event) => {
          event.currentTarget.style.backgroundColor = dividerHoverColor;
        }}
        onMouseLeave={(event) => {
          if (!draggingRef.current) {
            event.currentTarget.style.backgroundColor = dividerColor;
          }
        }}
      />
      <Box
        style={{
          flex: `${bottomPercent} 1 0`,
          minHeight: minBottomPx,
          minWidth: 0,
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden',
        }}
      >
        {bottom}
      </Box>
    </Box>
  );
}
