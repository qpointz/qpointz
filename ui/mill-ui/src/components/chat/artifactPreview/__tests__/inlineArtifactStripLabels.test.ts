import { describe, it, expect } from 'vitest';
import { sqlStripDescription, sqlStripTitle } from '../inlineArtifactStripLabels';

describe('inlineArtifactStripLabels', () => {
  it('should prefer artifact title for sql strip label', () => {
    expect(sqlStripTitle({ kind: 'sql', sql: 'select 1', info: { title: 'Top orders' } })).toBe(
      'Top orders.sql',
    );
  });

  it('should fall back to generated query label', () => {
    expect(sqlStripTitle({ kind: 'sql', sql: 'select 1' })).toBe('Generated query.sql');
  });

  it('should expose artifact description when present', () => {
    expect(
      sqlStripDescription({
        kind: 'sql',
        sql: 'select 1',
        info: { description: 'Adds revenue per customer' },
      }),
    ).toBe('Adds revenue per customer');
  });
});
