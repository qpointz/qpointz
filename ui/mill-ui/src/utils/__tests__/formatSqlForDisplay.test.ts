import { describe, expect, it } from 'vitest';
import { formatSqlForDisplay } from '../formatSqlForDisplay';

describe('formatSqlForDisplay', () => {
  it('pretty-prints valid SQL with uppercase keywords', () => {
    const out = formatSqlForDisplay('select id from passenger where id = 1');
    expect(out).toContain('SELECT');
    expect(out).toContain('FROM');
    expect(out).toContain('passenger');
  });

  it('returns original text when input is blank', () => {
    expect(formatSqlForDisplay('   ')).toBe('   ');
  });

  it('returns original text when formatter throws', () => {
    const broken = 'select {{{ not valid sql';
    expect(formatSqlForDisplay(broken)).toBe(broken);
  });
});
