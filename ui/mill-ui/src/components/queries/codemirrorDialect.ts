import { MySQL, PostgreSQL, StandardSQL, type SQLDialect } from '@codemirror/lang-sql';
import type { EditorDialectId } from '../../types/analysis';

/**
 * Maps server {@link EditorDialectId} to a CodeMirror SQL dialect extension.
 */
export function resolveCodeMirrorDialect(editorDialect: EditorDialectId): SQLDialect {
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
