import type { QueryResult } from '../../../types/query';
import { DataPaginationControls } from '../../data/DataPaginationControls';
import { buildQueryPageLabel, shouldShowArtifactPagination } from '../../data/dataPagination';

interface SqlArtifactPaginationProps {
  result: QueryResult | null;
  isPageLoading: boolean;
  isExecuting: boolean;
  onPageChange: (pageIndex: number) => void;
}

/** Centered artifact-toolbar paging; hidden unless another page exists ahead. */
export function SqlArtifactPagination({
  result,
  isPageLoading,
  isExecuting,
  onPageChange,
}: SqlArtifactPaginationProps) {
  if (!shouldShowArtifactPagination(result, isExecuting)) {
    return null;
  }

  return (
    <DataPaginationControls
      pageLabel={buildQueryPageLabel(result)}
      hasPrevious={result?.page.hasPrevious ?? false}
      hasNext={result?.page.hasNext ?? false}
      disabled={isPageLoading || isExecuting || !result}
      onPrevPage={result ? () => onPageChange(result.page.pageIndex - 1) : undefined}
      onNextPage={result ? () => onPageChange(result.page.pageIndex + 1) : undefined}
    />
  );
}
