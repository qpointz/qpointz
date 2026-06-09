import type { Completion } from '@codemirror/autocomplete';
import type { SchemaCompletionEntry } from './schemaCompletionIndex';

/**
 * Quotes one SQL identifier segment using the active dialect quote characters.
 *
 * @param part raw identifier segment (unquoted)
 * @param quoteStart opening quote character
 * @param quoteEnd closing quote character
 */
export function quoteIdentifierPart(part: string, quoteStart: string, quoteEnd: string): string {
  if (!part || !quoteStart || !quoteEnd) {
    return part;
  }
  const escaped = part.split(quoteStart).join(quoteStart + quoteStart);
  return `${quoteStart}${escaped}${quoteEnd}`;
}

/**
 * Quotes a possibly qualified name (`schema`, `schema.table`, or `schema.table.column`).
 *
 * @param qualified dot-separated identifier path
 * @param quoteStart opening quote character
 * @param quoteEnd closing quote character
 */
export function quoteQualifiedName(
  qualified: string,
  quoteStart: string,
  quoteEnd: string,
): string {
  if (!qualified.includes('.')) {
    return quoteIdentifierPart(qualified, quoteStart, quoteEnd);
  }
  return qualified
    .split('.')
    .map((part) => quoteIdentifierPart(part, quoteStart, quoteEnd))
    .join('.');
}

/**
 * Maps schema completion entries to CodeMirror options with dialect-quoted {@link Completion.apply} text.
 */
export function toQuotedSchemaCompletions(
  entries: SchemaCompletionEntry[],
  quoteStart: string,
  quoteEnd: string,
): Completion[] {
  return entries.map((entry) => ({
    label: entry.label,
    apply: quoteQualifiedName(entry.label, quoteStart, quoteEnd),
    type: entry.kind === 'schema' ? 'namespace' : entry.kind === 'table' ? 'class' : 'property',
    detail: entry.kind,
  }));
}
