import { useCallback, useEffect, useRef, useState } from 'react';

interface UseAutoScrollOptions {
  /** Dependency array that triggers auto-scroll when changed */
  deps: unknown[];
  /** Distance from bottom (px) before the scroll-to-bottom indicator triggers (default 200) */
  threshold?: number;
}

interface UseAutoScrollReturn {
  /** Ref to attach to the ScrollArea viewport */
  viewportRef: React.RefObject<HTMLDivElement | null>;
  /** Whether the user has scrolled away from the bottom */
  showScrollBottom: boolean;
  /** Scroll to the bottom of the viewport */
  scrollToBottom: () => void;
  /** Callback to pass to onScrollPositionChange or onScroll */
  handleScroll: () => void;
}

export function useAutoScroll({ deps, threshold = 200 }: UseAutoScrollOptions): UseAutoScrollReturn {
  const viewportRef = useRef<HTMLDivElement>(null);
  const [showScrollBottom, setShowScrollBottom] = useState(false);

  const handleScroll = useCallback(() => {
    if (!viewportRef.current) return;
    const { scrollTop, scrollHeight, clientHeight } = viewportRef.current;
    const distanceFromBottom = scrollHeight - scrollTop - clientHeight;
    setShowScrollBottom(distanceFromBottom > threshold);
  }, [threshold]);

  // Auto-scroll to bottom when dependencies change
  useEffect(() => {
    if (viewportRef.current) {
      viewportRef.current.scrollTo({
        top: viewportRef.current.scrollHeight,
        behavior: 'smooth',
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);

  const scrollToBottom = useCallback(() => {
    if (viewportRef.current) {
      viewportRef.current.scrollTo({
        top: viewportRef.current.scrollHeight,
        behavior: 'smooth',
      });
    }
  }, []);

  return { viewportRef, showScrollBottom, scrollToBottom, handleScroll };
}
