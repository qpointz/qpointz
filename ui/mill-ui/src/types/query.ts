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

/** Paging metadata for an active `/api/v1/query/{executionId}` session. */
export interface QueryPageInfo {
  executionId: string;
  epoch: number;
  pageIndex: number;
  pageSize: number;
  pageRowCount: number;
  totalResult: number | null;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface QueryResult {
  columns: QueryColumn[];
  rows: Record<string, string | number | boolean | null>[];
  /** Rows on the current presentation page. */
  rowCount: number;
  executionTimeMs: number;
  page: QueryPageInfo;
}

export interface QueryExecuteOptions {
  pageSize?: number;
}

export interface QueryFetchPageParams {
  executionId: string;
  pageIndex: number;
  pageSize: number;
  epoch: number;
}

export interface SavedQueryWriteInput {
  name: string;
  description?: string;
  sql: string;
  tags?: string[];
}

export interface SavedQueryCreateInput extends SavedQueryWriteInput {
  id?: string;
}

export interface QueryService {
  executeQuery(sql: string, options?: QueryExecuteOptions): Promise<QueryResult>;
  fetchQueryPage(params: QueryFetchPageParams): Promise<QueryResult>;
  closeQuerySession(executionId: string): Promise<void>;
  getSavedQueries(): Promise<SavedQuery[]>;
  getSavedQueryById(id: string): Promise<SavedQuery | null>;
  createSavedQuery(input: SavedQueryCreateInput): Promise<SavedQuery>;
  updateSavedQuery(id: string, input: SavedQueryWriteInput): Promise<SavedQuery>;
  deleteSavedQuery(id: string): Promise<void>;
}
