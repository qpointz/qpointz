import { describe, expect, it } from 'vitest';
import { formatResultRowLabel } from '../QueryResults';
import type { QueryResult } from '../../../types/query';

function makeResult(overrides: Partial<QueryResult>): QueryResult {
  return {
    columns: [],
    rows: [],
    rowCount: 1,
    executionTimeMs: 10,
    page: {
      executionId: 'exec-1',
      epoch: 1,
      pageIndex: 0,
      pageSize: 50,
      pageRowCount: 1,
      totalResult: 120,
      hasNext: true,
      hasPrevious: false,
    },
    ...overrides,
  };
}

describe('formatResultRowLabel', () => {
  it('should show range when total is known', () => {
    const label = formatResultRowLabel(makeResult({ rowCount: 50 }));
    expect(label).toBe('1–50 of 120');
  });

  it('should show second page range', () => {
    const label = formatResultRowLabel(makeResult({
      rowCount: 50,
      page: {
        executionId: 'exec-1',
        epoch: 1,
        pageIndex: 1,
        pageSize: 50,
        pageRowCount: 50,
        totalResult: 120,
        hasNext: true,
        hasPrevious: true,
      },
    }));
    expect(label).toBe('51–100 of 120');
  });

  it('should show page row count when total is unknown', () => {
    const label = formatResultRowLabel(makeResult({
      page: {
        executionId: 'exec-1',
        epoch: 1,
        pageIndex: 0,
        pageSize: 50,
        pageRowCount: 3,
        totalResult: null,
        hasNext: false,
        hasPrevious: false,
      },
      rowCount: 3,
    }));
    expect(label).toBe('3 rows');
  });
});
