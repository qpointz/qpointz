/** Clamp right-pane width during horizontal split drag. */
export function clampRightPaneWidth(
  offsetFromRight: number,
  usableWidth: number,
  minLeftPx: number,
  minRightPx: number,
  maxRightFraction: number,
): number {
  if (usableWidth <= 0) {
    return minRightPx;
  }
  const minRight = Math.max(minRightPx, 0);
  const maxRight = Math.min(usableWidth * maxRightFraction, usableWidth - minLeftPx);
  const upper = Math.max(minRight, maxRight);
  return Math.min(upper, Math.max(minRight, offsetFromRight));
}

/** Clamp a persisted width to current layout limits. */
export function clampStoredRightPx(
  value: number,
  usableWidth: number,
  minLeftPx: number,
  minRightPx: number,
  maxRightFraction: number,
  fallback: number,
): number {
  if (!Number.isFinite(value)) {
    return fallback;
  }
  return clampRightPaneWidth(value, usableWidth, minLeftPx, minRightPx, maxRightFraction);
}
