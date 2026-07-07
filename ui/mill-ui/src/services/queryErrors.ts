/** True when a query session or page fetch failed because the execution id expired server-side. */
export function isQuerySessionNotFound(error: unknown): boolean {
  const message = error instanceof Error ? error.message : String(error);
  return /\(\s*404\s*\)/.test(message) || /not found/i.test(message);
}

/** True when a stored execution id can no longer be paged and should be re-run. */
export function isRecoverableQuerySessionError(error: unknown): boolean {
  const message = error instanceof Error ? error.message : String(error);
  return (
    isQuerySessionNotFound(error) ||
    /\(\s*409\s*\)/.test(message) ||
    /stale_epoch/i.test(message)
  );
}
