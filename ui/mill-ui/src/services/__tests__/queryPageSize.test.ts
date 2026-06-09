import { afterEach, describe, expect, it } from 'vitest';
import {
  DEFAULT_QUERY_PAGE_SIZE,
  QUERY_PAGE_SIZE_STORAGE_KEY,
  isQueryPageSizeOption,
  normalizeQueryPageSize,
  readStoredQueryPageSize,
  storeQueryPageSize,
} from '../queryService';

describe('query page size helpers', () => {
  afterEach(() => {
    localStorage.removeItem(QUERY_PAGE_SIZE_STORAGE_KEY);
  });

  it('should accept fixed page size options', () => {
    expect(isQueryPageSizeOption(50)).toBe(true);
    expect(isQueryPageSizeOption(75)).toBe(false);
  });

  it('should read stored page size when valid', () => {
    storeQueryPageSize(100);
    expect(readStoredQueryPageSize()).toBe(100);
  });

  it('should fall back to default for invalid stored value', () => {
    localStorage.setItem(QUERY_PAGE_SIZE_STORAGE_KEY, 'not-a-number');
    expect(readStoredQueryPageSize()).toBe(DEFAULT_QUERY_PAGE_SIZE);
  });

  it('should clamp legacy large stored page sizes', () => {
    localStorage.setItem(QUERY_PAGE_SIZE_STORAGE_KEY, '1000');
    expect(readStoredQueryPageSize()).toBe(200);
    expect(normalizeQueryPageSize(1000)).toBe(200);
  });
});
