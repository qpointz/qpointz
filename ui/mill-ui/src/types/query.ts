export interface SavedQuery {
  id: string;
  name: string;
  description?: string;
  sql: string;
  createdAt: number;
  updatedAt: number;
  tags?: string[];
}

export interface QueryColumn {
  name: string;
  type: string;
}

export interface QueryResult {
  columns: QueryColumn[];
  rows: Record<string, string | number | boolean | null>[];
  rowCount: number;
  executionTimeMs: number;
}

export interface QueryService {
  executeQuery(sql: string): Promise<QueryResult>;
  getSavedQueries(): Promise<SavedQuery[]>;
  getSavedQueryById(id: string): Promise<SavedQuery | null>;
}
