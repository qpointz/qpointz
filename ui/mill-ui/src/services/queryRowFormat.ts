/** Marshaller used for Analysis grid paging — compact rows reduce JSON size vs `rows-objects`. */
export const QUERY_ROW_FORMAT = 'rows-compact-batch';

/** Above this row count, the UI nudges users toward server export instead of paging the full grid. */
export const LARGE_RESULT_PREVIEW_THRESHOLD = 5_000;

interface CompactBatchData {
  fields?: string[];
  rows?: unknown[][];
}

/**
 * Maps one paged `data` payload (`rows-objects` array or `rows-compact-batch` object) to row records.
 */
export function mapQueryPageRows(data: unknown): Record<string, string | number | boolean | null>[] {
  if (Array.isArray(data)) {
    return data as Record<string, string | number | boolean | null>[];
  }
  if (data && typeof data === 'object') {
    const batch = data as CompactBatchData;
    const fields = batch.fields ?? [];
    const rows = batch.rows ?? [];
    return rows.map((values) => {
      const record: Record<string, string | number | boolean | null> = {};
      fields.forEach((name, index) => {
        const value = values[index];
        record[name] =
          value === null || typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean'
            ? value
            : value == null
              ? null
              : String(value);
      });
      return record;
    });
  }
  throw new Error('Unexpected query response: data must be a row array or compact batch');
}
