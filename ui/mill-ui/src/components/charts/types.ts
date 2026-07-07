/** Semantic chart visualization config from sql.generated visualizations[]. */
export interface ChartVisualizationConfig {
  key: string;
  kind: 'chart';
  title?: string;
  description?: string;
  chartType: string;
  encodings: Record<string, { field: string; label?: string }>;
  options?: Record<string, unknown>;
  presentation?: Record<string, unknown>;
}

/** Nested sql.generated artifact sections preserved on wire. */
export interface SqlArtifactPayload {
  sql: string;
  dialectId?: string;
  statementKind?: string;
  info?: { title?: string; description?: string };
  schema?: Array<{ name: string; type: string; nullable?: boolean }>;
  visualizations?: ChartVisualizationConfig[];
  profiling?: unknown[];
  artifactType?: string;
}

export interface ChartSnapshotRow {
  [column: string]: unknown;
}
