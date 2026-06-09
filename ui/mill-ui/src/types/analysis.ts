/** CodeMirror {@code @codemirror/lang-sql} dialect keys returned by the server. */
export type EditorDialectId = 'standard' | 'postgresql' | 'mysql';

export interface AnalysisDialectIdentifiers {
  quoteStart: string;
  quoteEnd: string;
}

/**
 * Configured SQL dialect for the Analysis view ({@code GET /api/v1/analysis/dialect}).
 */
export interface AnalysisDialect {
  id: string;
  name: string;
  readOnly: boolean;
  editorDialect: EditorDialectId;
  identifiers: AnalysisDialectIdentifiers;
  functions: Record<string, string[]>;
}

export interface AnalysisService {
  getDialect(): Promise<AnalysisDialect>;
}
