import type {
  QueryColumn,
  QueryExecuteOptions,
  QueryFetchPageParams,
  QueryResult,
  QueryService,
  SavedQuery,
  SavedQueryCreateInput,
  SavedQueryWriteInput,
} from '../types/query';
import { ANALYSIS_QUERIES_BASE } from './analysisService';
import { mapQueryPageRows, QUERY_ROW_FORMAT } from './queryRowFormat';

const QUERY_API = '/api/v1/query';
/** UI preview page sizes — kept modest to limit per-request JSON and server buffer windows. */
export const QUERY_PAGE_SIZE_OPTIONS = [25, 50, 100, 200] as const;
export type QueryPageSizeOption = (typeof QUERY_PAGE_SIZE_OPTIONS)[number];
export const DEFAULT_QUERY_PAGE_SIZE: QueryPageSizeOption = 50;
export const QUERY_PAGE_SIZE_STORAGE_KEY = 'mill-ui.analysis.queryPageSize';

export function isQueryPageSizeOption(value: number): value is QueryPageSizeOption {
  return (QUERY_PAGE_SIZE_OPTIONS as readonly number[]).includes(value);
}

export function normalizeQueryPageSize(value: number): QueryPageSizeOption {
  if (isQueryPageSizeOption(value)) {
    return value;
  }
  if (!Number.isFinite(value) || value <= 0) {
    return DEFAULT_QUERY_PAGE_SIZE;
  }
  const maxOption = QUERY_PAGE_SIZE_OPTIONS[QUERY_PAGE_SIZE_OPTIONS.length - 1]!;
  if (value > maxOption) {
    return maxOption;
  }
  for (let index = QUERY_PAGE_SIZE_OPTIONS.length - 1; index >= 0; index -= 1) {
    const option = QUERY_PAGE_SIZE_OPTIONS[index];
    if (option != null && value >= option) {
      return option;
    }
  }
  return DEFAULT_QUERY_PAGE_SIZE;
}

export function readStoredQueryPageSize(): QueryPageSizeOption {
  try {
    const saved = localStorage.getItem(QUERY_PAGE_SIZE_STORAGE_KEY);
    if (saved) {
      const value = Number(saved);
      if (Number.isFinite(value)) {
        return normalizeQueryPageSize(value);
      }
    }
  } catch {
    // Ignore storage errors.
  }
  return DEFAULT_QUERY_PAGE_SIZE;
}

export function storeQueryPageSize(pageSize: QueryPageSizeOption): void {
  try {
    localStorage.setItem(QUERY_PAGE_SIZE_STORAGE_KEY, String(pageSize));
  } catch {
    // Ignore storage errors.
  }
}

interface PageEnvelope {
  epoch: number;
  pageIndex: number;
  pageSize: number;
  rowCount: number;
  totalResult: number | null;
  hasNext: boolean;
  hasPrevious: boolean;
  schema?: Array<{ name?: string; type?: string }>;
  data: unknown;
}

interface CreateQueryResponse {
  executionId: string;
  epoch?: number;
  firstPage?: PageEnvelope;
}

function mapSchemaColumns(schema: unknown, data: unknown): QueryColumn[] {
  if (Array.isArray(schema) && schema.length > 0) {
    return schema.map((col) => ({
      name: String((col as { name?: string }).name ?? ''),
      type: String((col as { type?: string }).type ?? 'unknown').toLowerCase(),
    }));
  }
  const rows = mapQueryPageRows(data);
  if (rows.length === 0) {
    return [];
  }
  const firstRow = rows[0];
  if (firstRow === undefined) {
    return [];
  }
  return Object.keys(firstRow).map((name) => ({ name, type: 'unknown' }));
}

function mapPageEnvelope(executionId: string, envelope: PageEnvelope, executionTimeMs: number): QueryResult {
  const rows = mapQueryPageRows(envelope.data);
  return {
    columns: mapSchemaColumns(envelope.schema, envelope.data),
    rows,
    rowCount: envelope.rowCount,
    executionTimeMs,
    page: {
      executionId,
      epoch: envelope.epoch,
      pageIndex: envelope.pageIndex,
      pageSize: envelope.pageSize,
      pageRowCount: envelope.rowCount,
      totalResult: envelope.totalResult,
      hasNext: envelope.hasNext,
      hasPrevious: envelope.hasPrevious,
    },
  };
}

async function readError(res: Response, label: string): Promise<never> {
  const text = await res.text();
  throw new Error(`${label} (${res.status}): ${text}`);
}

/**
 * Executes SQL via session-based `/api/v1/query` and returns the first presentation page.
 * The session stays open until {@link closeQuerySession} is called.
 */
async function httpExecuteQuery(sql: string, options?: QueryExecuteOptions): Promise<QueryResult> {
  const trimmed = sql.trim();
  if (!trimmed) {
    throw new Error('Empty query');
  }

  const pageSize = options?.pageSize ?? DEFAULT_QUERY_PAGE_SIZE;
  const started = performance.now();

  const createRes = await fetch(QUERY_API, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      sql: trimmed,
      includeFirstPage: true,
      firstPageSize: pageSize,
      defaultFormat: QUERY_ROW_FORMAT,
    }),
  });
  if (!createRes.ok) {
    await readError(createRes, 'Query failed');
  }

  const created = (await createRes.json()) as CreateQueryResponse;
  const executionId = created.executionId;
  const executionTimeMs = Math.round(performance.now() - started);

  let envelope: PageEnvelope;
  if (created.firstPage) {
    envelope = created.firstPage;
  } else {
    const rowsRes = await fetch(
      `${QUERY_API}/${encodeURIComponent(executionId)}?pageIndex=0&pageSize=${pageSize}&format=${QUERY_ROW_FORMAT}`,
      { credentials: 'include' },
    );
    if (!rowsRes.ok) {
      await readError(rowsRes, 'Query rows failed');
    }
    envelope = (await rowsRes.json()) as PageEnvelope;
  }

  return mapPageEnvelope(executionId, envelope, executionTimeMs);
}

/**
 * Fetches another presentation page from an existing query session.
 */
async function httpFetchQueryPage(params: QueryFetchPageParams): Promise<QueryResult> {
  const { executionId, pageIndex, pageSize, epoch } = params;
  const url = new URL(`${QUERY_API}/${encodeURIComponent(executionId)}`, window.location.origin);
  url.searchParams.set('pageIndex', String(pageIndex));
  url.searchParams.set('pageSize', String(pageSize));
  url.searchParams.set('format', QUERY_ROW_FORMAT);
  url.searchParams.set('epoch', String(epoch));

  const res = await fetch(url.pathname + url.search, { credentials: 'include' });
  if (!res.ok) {
    await readError(res, 'Query page failed');
  }

  const envelope = (await res.json()) as PageEnvelope;
  return mapPageEnvelope(executionId, envelope, 0);
}

/**
 * Releases server-side buffers for a query session.
 */
async function httpCloseQuerySession(executionId: string): Promise<void> {
  const res = await fetch(`${QUERY_API}/${encodeURIComponent(executionId)}`, {
    method: 'DELETE',
    credentials: 'include',
  });
  if (res.status === 404) {
    return;
  }
  if (!res.ok) {
    await readError(res, 'Close query session failed');
  }
}

async function httpGetSavedQueries(): Promise<SavedQuery[]> {
  const res = await fetch(ANALYSIS_QUERIES_BASE, { credentials: 'include' });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(`Saved queries failed (${res.status}): ${text}`);
  }
  const body = (await res.json()) as { queries?: SavedQuery[] };
  return body.queries ?? [];
}

async function httpGetSavedQueryById(id: string): Promise<SavedQuery | null> {
  const res = await fetch(`${ANALYSIS_QUERIES_BASE}/${encodeURIComponent(id)}`, { credentials: 'include' });
  if (res.status === 404) {
    return null;
  }
  if (!res.ok) {
    const text = await res.text();
    throw new Error(`Saved query failed (${res.status}): ${text}`);
  }
  return (await res.json()) as SavedQuery;
}

async function httpCreateSavedQuery(input: SavedQueryCreateInput): Promise<SavedQuery> {
  const res = await fetch(ANALYSIS_QUERIES_BASE, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(input),
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(`Create saved query failed (${res.status}): ${text}`);
  }
  return (await res.json()) as SavedQuery;
}

async function httpUpdateSavedQuery(id: string, input: SavedQueryWriteInput): Promise<SavedQuery> {
  const res = await fetch(`${ANALYSIS_QUERIES_BASE}/${encodeURIComponent(id)}`, {
    method: 'PUT',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(input),
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(`Update saved query failed (${res.status}): ${text}`);
  }
  return (await res.json()) as SavedQuery;
}

async function httpDeleteSavedQuery(id: string): Promise<void> {
  const res = await fetch(`${ANALYSIS_QUERIES_BASE}/${encodeURIComponent(id)}`, {
    method: 'DELETE',
    credentials: 'include',
  });
  if (res.status === 404) {
    throw new Error(`Saved query not found: ${id}`);
  }
  if (!res.ok) {
    const text = await res.text();
    throw new Error(`Delete saved query failed (${res.status}): ${text}`);
  }
}

/**
 * HTTP-only {@link QueryService} for Analysis saved-query catalog and execution.
 */
export const queryService: QueryService = {
  executeQuery: httpExecuteQuery,
  fetchQueryPage: httpFetchQueryPage,
  closeQuerySession: httpCloseQuerySession,
  getSavedQueries: httpGetSavedQueries,
  getSavedQueryById: httpGetSavedQueryById,
  createSavedQuery: httpCreateSavedQuery,
  updateSavedQuery: httpUpdateSavedQuery,
  deleteSavedQuery: httpDeleteSavedQuery,
};
