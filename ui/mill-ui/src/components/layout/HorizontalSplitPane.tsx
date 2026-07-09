import { Box, useMantineColorScheme } from '@mantine/core';
import {
  useCallback,
  useEffect,
  useRef,
  useState,
  type ReactNode,
  type PointerEvent as ReactPointerEvent,
} from 'react';
import { clampRightPaneWidth, clampStoredRightPx } from './horizontalSplitPaneMath';

const DIVIDER_WIDTH_PX = 6;

export interface HorizontalSplitPaneProps {
  left: ReactNode;
  right: ReactNode;
  /** Initial width of the right pane in pixels. */
  initialRightPx?: number;
  minLeftPx?: number;
  minRightPx?: number;
  /** Maximum right pane width as a fraction of usable width (0–1). */
  maxRightFraction?: number;
  /** When set, right pane width is persisted in `localStorage`. */
  storageKey?: string;
}

function readStoredRightPx(
  storageKey: string,
  fallback: number,
  minLeftPx: number,
  minRightPx: number,
  maxRightFraction: number,
): number {
  try {
    const saved = localStorage.getItem(storageKey);
    if (!saved) {
      return fallback;
    }
    const value = Number(saved);
    // Assume a wide viewport when rehydrating so stored width is not over-clamped.
    return clampStoredRightPx(value, 1600, minLeftPx, minRightPx, maxRightFraction, fallback);
  } catch {
    // Ignore storage errors.
  }
  return fallback;
}

/**
 * Horizontal split layout with a draggable divider between left and right panes.
 */
export function HorizontalSplitPane({
  left,
  right,
  initialRightPx = 380,
  minLeftPx = 280,
  minRightPx = 260,
  maxRightFraction = 0.5,
  storageKey,
}: HorizontalSplitPaneProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const containerRef = useRef<HTMLDivElement>(null);
  const draggingRef = useRef(false);
  const rightPxRef = useRef(initialRightPx);

  const [rightPx, setRightPx] = useState(() =>
    storageKey
      ? readStoredRightPx(storageKey, initialRightPx, minLeftPx, minRightPx, maxRightFraction)
      : initialRightPx,
  );
  rightPxRef.current = rightPx;

  const persistRightPx = useCallback((value: number) => {
    if (!storageKey) {
      return;
    }
    try {
      localStorage.setItem(storageKey, String(Math.round(value)));
    } catch {
      // Ignore storage errors.
    }
  }, [storageKey]);

  const updateFromPointer = useCallback((clientX: number) => {
    const container = containerRef.current;
    if (!container) {
      return;
    }
    const rect = container.getBoundingClientRect();
    const usable = rect.width - DIVIDER_WIDTH_PX;
    const offsetFromRight = rect.right - clientX - DIVIDER_WIDTH_PX / 2;
    const next = clampRightPaneWidth(
      offsetFromRight,
      usable,
      minLeftPx,
      minRightPx,
      maxRightFraction,
    );
    setRightPx(next);
    rightPxRef.current = next;
  }, [maxRightFraction, minLeftPx, minRightPx]);

  const endDrag = useCallback(() => {
    if (!draggingRef.current) {
      return;
    }
    draggingRef.current = false;
    document.body.style.cursor = '';
    document.body.style.userSelect = '';
    persistRightPx(rightPxRef.current);
  }, [persistRightPx]);

  useEffect(() => {
    const onPointerMove = (event: PointerEvent) => {
      if (!draggingRef.current) {
        return;
      }
      updateFromPointer(event.clientX);
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
    document.body.style.cursor = 'col-resize';
    document.body.style.userSelect = 'none';
    updateFromPointer(event.clientX);
  };

  const dividerColor = isDark ? 'var(--mantine-color-dark-4)' : 'var(--mantine-color-gray-3)';
  const dividerHoverColor = isDark ? 'var(--mantine-color-cyan-7)' : 'var(--mantine-color-teal-4)';

  return (
    <Box
      ref={containerRef}
      style={{
        height: '100%',
        minHeight: 0,
        minWidth: 0,
        flex: 1,
        display: 'flex',
        flexDirection: 'row',
      }}
    >
      <Box
        style={{
          flex: '1 1 0',
          minWidth: minLeftPx,
          minHeight: 0,
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden',
        }}
      >
        {left}
      </Box>
      <Box
        role="separator"
        aria-orientation="vertical"
        aria-valuenow={Math.round(rightPx)}
        onPointerDown={onDividerPointerDown}
        style={{
          flex: `0 0 ${DIVIDER_WIDTH_PX}px`,
          cursor: 'col-resize',
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
          flex: `0 0 ${rightPx}px`,
          width: rightPx,
          minWidth: minRightPx,
          minHeight: 0,
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden',
        }}
      >
        {right}
      </Box>
    </Box>
  );
}
