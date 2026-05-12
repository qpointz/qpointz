import type { QueryColumn, QueryResult, QueryService } from '../types/query';
import { getResultForQuery, mockSavedQueries, getSavedQueryById } from '../data/mockQueries';

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

const FIRST_PAGE_SIZE = 2000;

function mapRowsObjectsData(data: unknown): QueryResult {
  if (!Array.isArray(data)) {
    throw new Error('Unexpected query response: rows-objects data must be a JSON array');
  }
  const rows = data as Record<string, string | number | boolean | null>[];
  if (rows.length === 0) {
    return { columns: [], rows: [], rowCount: 0, executionTimeMs: 0 };
  }
  const names = Object.keys(rows[0]);
  const columns: QueryColumn[] = names.map((name) => ({ name, type: 'unknown' }));
  return { columns, rows, rowCount: rows.length, executionTimeMs: 0 };
}

/**
 * Executes SQL via session-based `/api/v1/query` (first page only for the UI result grid).
 */
async function httpExecuteQuery(sql: string): Promise<QueryResult> {
  const started = performance.now();
  let executionId: string | null = null;
  try {
    const createRes = await fetch('/api/v1/query', {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        sql,
        includeFirstPage: true,
        firstPageSize: FIRST_PAGE_SIZE,
        defaultFormat: 'rows-objects',
      }),
    });
    const createText = await createRes.text();
    if (!createRes.ok) {
      throw new Error(`Query failed (${createRes.status}): ${createText}`);
    }
    const created = JSON.parse(createText) as {
      executionId: string;
      firstPage?: { data: unknown };
    };
    executionId = created.executionId;

    let data: unknown;
    if (created.firstPage) {
      data = created.firstPage.data;
    } else {
      const rowsRes = await fetch(
        `/api/v1/query/${encodeURIComponent(executionId)}?pageIndex=0&pageSize=${FIRST_PAGE_SIZE}&format=rows-objects`,
        { credentials: 'include' },
      );
      const rowsText = await rowsRes.text();
      if (!rowsRes.ok) {
        throw new Error(`Query rows failed (${rowsRes.status}): ${rowsText}`);
      }
      data = (JSON.parse(rowsText) as { data: unknown }).data;
    }

    const result = mapRowsObjectsData(data);
    result.executionTimeMs = Math.round(performance.now() - started);
    return result;
  } finally {
    if (executionId) {
      void fetch(`/api/v1/query/${encodeURIComponent(executionId)}`, {
        method: 'DELETE',
        credentials: 'include',
      });
    }
  }
}

const mockQueryService: QueryService = {
  async executeQuery(sql: string) {
    await sleep(800 + Math.random() * 700);

    const trimmed = sql.trim();
    if (!trimmed) {
      throw new Error('Empty query');
    }
    if (trimmed.length < 10 && !trimmed.toLowerCase().startsWith('select')) {
      throw new Error(`Syntax error near "${trimmed}": expected SELECT, INSERT, UPDATE, or DELETE`);
    }

    const result = getResultForQuery(sql);
    return {
      ...result,
      executionTimeMs: Math.round(result.executionTimeMs * (0.8 + Math.random() * 0.4)),
    };
  },

  async getSavedQueries() {
    return mockSavedQueries;
  },

  async getSavedQueryById(id: string) {
    return getSavedQueryById(id) ?? null;
  },
};

const useHttpExecution =
  import.meta.env.MODE !== 'test' &&
  (import.meta.env.VITE_MILL_QUERY_TRANSPORT as string | undefined)?.toLowerCase() !== 'mock';

/**
 * Default {@link QueryService}: HTTP execution against `/api/v1/query` unless
 * `VITE_MILL_QUERY_TRANSPORT=mock` (offline). In Vitest (`import.meta.env.MODE === 'test'`), execution
 * stays on the in-memory mock regardless of that variable.
 */
export const queryService: QueryService = {
  executeQuery: useHttpExecution ? httpExecuteQuery : mockQueryService.executeQuery.bind(mockQueryService),
  getSavedQueries: mockQueryService.getSavedQueries.bind(mockQueryService),
  getSavedQueryById: mockQueryService.getSavedQueryById.bind(mockQueryService),
};
