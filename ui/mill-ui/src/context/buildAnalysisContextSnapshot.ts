import type { QueryColumn, QueryResult } from '../types/query';

/** Live Analysis editor/execution state read at inline chat send time. */
export interface AnalysisSnapshotSource {
  sql: string;
  dialectId?: string | null;
  activeQueryId?: string | null;
  activeQueryName?: string | null;
  activeQueryDescription?: string | null;
  isDirty: boolean;
  isExecuting: boolean;
  error?: string | null;
  activeExecutionId?: string | null;
  result?: QueryResult | null;
}

function executionStatus(source: AnalysisSnapshotSource): string | undefined {
  if (source.isExecuting) return 'running';
  if (source.error?.trim()) return 'failed';
  if (source.result || source.activeExecutionId) return 'completed';
  return undefined;
}

function columnMetadata(columns: QueryColumn[] | undefined): Array<{ name: string; type: string }> | undefined {
  if (!columns?.length) return undefined;
  return columns.map((column) => ({ name: column.name, type: column.type }));
}

/**
 * Builds the Analysis copilot `context.values` map for one inline chat turn.
 * Omits empty keys; never includes raw result rows.
 */
export function buildAnalysisContextSnapshot(source: AnalysisSnapshotSource): Record<string, unknown> {
  const values: Record<string, unknown> = {};

  const sql = source.sql ?? '';
  if (sql.trim()) {
    values['sql.current'] = sql;
  }

  const dialect = source.dialectId?.trim();
  if (dialect) {
    values['sql.dialect'] = dialect;
  }

  const queryId = source.activeQueryId?.trim();
  if (queryId) {
    values['artifact.query.id'] = queryId;
  }

  const queryName = source.activeQueryName?.trim();
  if (queryName) {
    values['artifact.query.name'] = queryName;
  }

  const queryDescription = source.activeQueryDescription?.trim();
  if (queryDescription) {
    values['artifact.query.description'] = queryDescription;
  }

  values['artifact.query.dirty'] = source.isDirty;

  const executionId = source.result?.page.executionId ?? source.activeExecutionId?.trim();
  if (executionId) {
    values['execution.last.id'] = executionId;
  }

  const status = executionStatus(source);
  if (status) {
    values['execution.last.status'] = status;
  }

  const rowCount = source.result?.rowCount;
  if (typeof rowCount === 'number') {
    values['execution.last.rowCount'] = rowCount;
  }

  const columns = columnMetadata(source.result?.columns);
  if (columns?.length) {
    values['execution.last.columns'] = columns;
  }

  const error = source.error?.trim();
  if (error) {
    values['execution.last.error'] = error;
  }

  return values;
}
