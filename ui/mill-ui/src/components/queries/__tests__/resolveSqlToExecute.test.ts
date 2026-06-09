import { describe, expect, it } from 'vitest';
import { resolveSqlToExecute } from '../resolveSqlToExecute';

describe('resolveSqlToExecute', () => {
  it('should run selected fragment when selection is non-empty', () => {
    expect(resolveSqlToExecute('SELECT 1;\nSELECT 2;', 'SELECT 2')).toBe('SELECT 2');
  });

  it('should run full document when selection is empty', () => {
    expect(resolveSqlToExecute('SELECT 1;\nSELECT 2;', undefined)).toBe('SELECT 1;\nSELECT 2;');
  });

  it('should fall back to document when selection is whitespace', () => {
    expect(resolveSqlToExecute('SELECT 1', '   ')).toBe('SELECT 1');
  });
});
