import { MySQL, PostgreSQL, SQLDialect, StandardSQL } from '@codemirror/lang-sql';
import type { AnalysisDialectIdentifiers, EditorDialectId } from '../../types/analysis';

function baseDialect(editorDialect: EditorDialectId): SQLDialect {
  switch (editorDialect) {
    case 'postgresql':
      return PostgreSQL;
    case 'mysql':
      return MySQL;
    case 'standard':
    default:
      return StandardSQL;
  }
}

/**
 * Maps server {@link EditorDialectId} to a CodeMirror SQL dialect extension.
 * Applies {@code identifiers.quoteStart} from {@code GET /api/v1/analysis/dialect} so the parser
 * and completion layer recognize dialect-quoted identifiers.
 *
 * @param editorDialect server dialect key
 * @param identifiers quote characters from the Analysis dialect API
 */
export function resolveCodeMirrorDialect(
  editorDialect: EditorDialectId,
  identifiers?: AnalysisDialectIdentifiers,
): SQLDialect {
  const base = baseDialect(editorDialect);
  const quoteStart = identifiers?.quoteStart?.trim();
  if (!quoteStart) {
    return base;
  }
  const nativeQuote = base.spec.identifierQuotes?.[0]
    ?? (editorDialect === 'mysql' ? '`' : '"');
  if (nativeQuote === quoteStart) {
    return base;
  }
  return SQLDialect.define({
    ...base.spec,
    identifierQuotes: quoteStart,
  });
}
