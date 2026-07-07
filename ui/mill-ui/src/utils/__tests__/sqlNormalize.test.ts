import { describe, expect, it } from 'vitest';
import { normalizeSqlForExecution } from '../sqlNormalize';

describe('normalizeSqlForExecution', () => {
  it('shouldStripTrailingSemicolon', () => {
    expect(normalizeSqlForExecution('SELECT 1;')).toBe('SELECT 1');
  });

  it('shouldTrimWhitespace', () => {
    expect(normalizeSqlForExecution('  SELECT 1  ')).toBe('SELECT 1');
  });
});
