import type { QueryResult } from '../../types/query';

/** Human-readable page indicator for paged query results. */
export function buildQueryPageLabel(result: QueryResult | null): string {
  if (!result) return 'Page 0';
  const totalPages = result.page.totalResult != null && result.page.pageSize > 0
    ? Math.max(1, Math.ceil(result.page.totalResult / result.page.pageSize))
    : null;
  return totalPages != null
    ? `Page ${result.page.pageIndex + 1} / ${totalPages}`
    : `Page ${result.page.pageIndex + 1}`;
}

/** Artifact toolbar paging is shown only while another page exists ahead. */
export function shouldShowArtifactPagination(
  result: QueryResult | null,
  isExecuting: boolean,
): boolean {
  return Boolean(result?.page.hasNext && !isExecuting);
}
