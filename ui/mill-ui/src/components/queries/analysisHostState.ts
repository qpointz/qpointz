/** Shared Analysis host execution flag for inline artifact strips. */
let analysisHostExecuting = false;
const executingListeners = new Set<() => void>();

/** Last SQL proposal explicitly applied to the Analysis editor (artifact key, not SQL text). */
let analysisAppliedArtifactKey: string | null = null;
const appliedArtifactListeners = new Set<() => void>();

/** Collapses whitespace for stable SQL equality checks in the UI. */
export function normalizeAnalysisEditorSql(sql: string): string {
  return sql.trim().replace(/\s+/g, ' ');
}

/** Updates whether Analysis query execution is in progress. */
export function setAnalysisHostExecuting(executing: boolean): void {
  analysisHostExecuting = executing;
  executingListeners.forEach((listener) => listener());
}

/** Records which inline SQL proposal was last applied (by artifact key). */
export function setAnalysisAppliedArtifactKey(key: string | null): void {
  const next = key?.trim() ? key.trim() : null;
  if (next === analysisAppliedArtifactKey) return;
  analysisAppliedArtifactKey = next;
  appliedArtifactListeners.forEach((listener) => listener());
}

/** Whether Apply & Run should be disabled on Analysis SQL strips. */
export function isAnalysisHostExecuting(): boolean {
  return analysisHostExecuting;
}

/** True when the given artifact key matches the last applied inline SQL proposal. */
export function isAnalysisAppliedArtifact(key: string | undefined | null): boolean {
  if (!analysisAppliedArtifactKey || !key?.trim()) return false;
  return key.trim() === analysisAppliedArtifactKey;
}

/** Subscribe to execution flag changes (for strip re-renders). */
export function subscribeAnalysisHostExecuting(listener: () => void): () => void {
  executingListeners.add(listener);
  return () => executingListeners.delete(listener);
}

/** Subscribe to applied-artifact key changes (for inline strip markers). */
export function subscribeAnalysisAppliedArtifact(listener: () => void): () => void {
  appliedArtifactListeners.add(listener);
  return () => appliedArtifactListeners.delete(listener);
}
