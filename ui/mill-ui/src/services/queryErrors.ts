/** True when a query session or page fetch failed because the execution id expired server-side. */
export function isQuerySessionNotFound(error: unknown): boolean {
  const message = error instanceof Error ? error.message : String(error);
  return /\(\s*404\s*\)/.test(message) || /not found/i.test(message);
}
