import { describe, expect, it } from 'vitest';
import { buildQueryPageLabel, shouldShowArtifactPagination } from '../dataPagination';
import type { QueryResult } from '../../../types/query';

function makeResult(overrides: Partial<QueryResult['page']> = {}): QueryResult {
  return {
    columns: [{ name: 'id', type: 'number' }],
    rows: [{ id: 1 }],
    rowCount: 1,
    executionTimeMs: 1,
    page: {
      executionId: 'exec-1',
      epoch: 0,
      pageIndex: 0,
      pageSize: 50,
      pageRowCount: 1,
      totalResult: 120,
      hasNext: true,
      hasPrevious: false,
      ...overrides,
    },
  };
}

describe('dataPagination', () => {
  it('shouldBuildPageLabel_withTotalPages', () => {
    expect(buildQueryPageLabel(makeResult())).toBe('Page 1 / 3');
  });

  it('shouldShowArtifactPagination_whenNextPageExists', () => {
    expect(shouldShowArtifactPagination(makeResult({ hasNext: true }), false)).toBe(true);
  });

  it('shouldHideArtifactPagination_whenNoNextPage', () => {
    expect(shouldShowArtifactPagination(makeResult({ hasNext: false, hasPrevious: true }), false)).toBe(false);
    expect(shouldShowArtifactPagination(makeResult({ hasNext: false, hasPrevious: false }), false)).toBe(false);
  });

  it('shouldKeepArtifactPaginationVisible_whilePageIsLoading', () => {
    expect(shouldShowArtifactPagination(makeResult({ hasNext: true }), false)).toBe(true);
  });

  it('shouldHideArtifactPagination_whileExecuting', () => {
    expect(shouldShowArtifactPagination(makeResult({ hasNext: true }), true)).toBe(false);
  });
});
