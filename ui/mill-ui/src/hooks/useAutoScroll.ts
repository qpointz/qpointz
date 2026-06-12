import { useCallback, useRef, useState } from 'react';

interface UseAutoScrollOptions {
  /** Distance from bottom (px) before the scroll-to-bottom indicator triggers (default 200) */
  threshold?: number;
}

interface UseAutoScrollReturn {
  /** Ref to attach to the ScrollArea viewport */
  viewportRef: React.RefObject<HTMLDivElement | null>;
  /** Whether the user has scrolled away from the bottom */
  showScrollBottom: boolean;
  /** Scroll to the bottom of the viewport */
  scrollToBottom: (behavior?: ScrollBehavior) => void;
  /** Scroll to bottom only when the user is already near the bottom */
  scrollToBottomIfNear: (behavior?: ScrollBehavior) => void;
  /** Whether the viewport is currently near the bottom */
  isNearBottom: () => boolean;
  /** Callback to pass to onScrollPositionChange or onScroll */
  handleScroll: () => void;
}

export function useAutoScroll({ threshold = 200 }: UseAutoScrollOptions = {}): UseAutoScrollReturn {
  const viewportRef = useRef<HTMLDivElement>(null);
  const [showScrollBottom, setShowScrollBottom] = useState(false);
  const isNearBottomRef = useRef(true);

  const distanceFromBottom = useCallback(() => {
    if (!viewportRef.current) return 0;
    const { scrollTop, scrollHeight, clientHeight } = viewportRef.current;
    return scrollHeight - scrollTop - clientHeight;
  }, []);

  const isNearBottom = useCallback(() => distanceFromBottom() <= threshold, [distanceFromBottom, threshold]);

  const handleScroll = useCallback(() => {
    const distance = distanceFromBottom();
    isNearBottomRef.current = distance <= threshold;
    setShowScrollBottom(distance > threshold);
  }, [distanceFromBottom, threshold]);

  const scrollToBottom = useCallback((behavior: ScrollBehavior = 'smooth') => {
    if (!viewportRef.current) return;
    viewportRef.current.scrollTo({
      top: viewportRef.current.scrollHeight,
      behavior,
    });
    isNearBottomRef.current = true;
    setShowScrollBottom(false);
  }, []);

  const scrollToBottomIfNear = useCallback((behavior: ScrollBehavior = 'smooth') => {
    if (!isNearBottomRef.current || !viewportRef.current) return;
    viewportRef.current.scrollTo({
      top: viewportRef.current.scrollHeight,
      behavior,
    });
  }, []);

  return {
    viewportRef,
    showScrollBottom,
    scrollToBottom,
    scrollToBottomIfNear,
    isNearBottom,
    handleScroll,
  };
}
