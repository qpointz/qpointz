import { describe, expect, it } from 'vitest';
import { MySQL, PostgreSQL, StandardSQL } from '@codemirror/lang-sql';
import { resolveCodeMirrorDialect } from '../codemirrorDialect';

describe('resolveCodeMirrorDialect', () => {
  it('should return PostgreSQL for postgresql editor dialect', () => {
    expect(resolveCodeMirrorDialect('postgresql')).toBe(PostgreSQL);
  });

  it('should return MySQL for mysql editor dialect', () => {
    expect(resolveCodeMirrorDialect('mysql')).toBe(MySQL);
  });

  it('should redefine standard dialect when server uses backtick identifiers', () => {
    const dialect = resolveCodeMirrorDialect('standard', { quoteStart: '`', quoteEnd: '`' });
    expect(dialect).not.toBe(StandardSQL);
    expect(dialect.spec.identifierQuotes).toBe('`');
  });

  it('should keep PostgreSQL when server uses double-quote identifiers', () => {
    const dialect = resolveCodeMirrorDialect('postgresql', { quoteStart: '"', quoteEnd: '"' });
    expect(dialect).toBe(PostgreSQL);
  });
});
