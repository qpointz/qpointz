import type { SchemaCompletionEntry } from './schemaCompletionIndex';

/** Parsed cursor context for lazy-loaded column completions. */
export interface ColumnCompletionContext {
  schema: string;
  table: string;
  /** Partial column name after the final dot (may be empty). */
  partial: string;
  /** Start offset in the text-before-cursor string where replacement should begin. */
  from: number;
}

const UNQUOTED_THREE_PART = /(\w+)\.(\w+)\.(\w*)$/;
const QUOTED_THREE_PART = /`([^`]+)`\.`([^`]+)`\.([^`]*)$/;
const UNQUOTED_TWO_PART = /(\w+)\.(\w*)$/;
const QUOTED_TWO_PART = /`([^`]+)`\.([^`]*)$/;

function matchAtEnd(text: string, regex: RegExp): RegExpMatchArray | null {
  const match = text.match(regex);
  if (!match || match.index === undefined) {
    return null;
  }
  if (match.index + match[0].length !== text.length) {
    return null;
  }
  return match;
}

function resolveUniqueTable(
  tableName: string,
  tables: SchemaCompletionEntry[],
): { schema: string; table: string } | null {
  const normalized = tableName.toLowerCase();
  const matches = tables.filter(
    (entry) => entry.kind === 'table' && entry.table?.toLowerCase() === normalized,
  );
  if (matches.length !== 1 || !matches[0]?.schema || !matches[0]?.table) {
    return null;
  }
  return { schema: matches[0].schema, table: matches[0].table };
}

/**
 * Detects whether the cursor sits after {@code schema.table.} or {@code table.} (unique table)
 * so column names can be suggested. Supports dialect-quoted identifiers from autocomplete apply.
 *
 * @param textBeforeCursor document text from line start through the cursor
 * @param tables table entries from the schema completion index
 */
export function resolveColumnCompletionContext(
  textBeforeCursor: string,
  tables: SchemaCompletionEntry[],
): ColumnCompletionContext | null {
  const quotedThree = matchAtEnd(textBeforeCursor, QUOTED_THREE_PART);
  if (quotedThree?.index != null) {
    const [, schema, table, partial] = quotedThree;
    if (schema && table) {
      return {
        schema,
        table,
        partial: partial ?? '',
        from: quotedThree.index,
      };
    }
  }

  const unquotedThree = matchAtEnd(textBeforeCursor, UNQUOTED_THREE_PART);
  if (unquotedThree?.index != null) {
    const [, schema, table, partial] = unquotedThree;
    if (schema && table) {
      return {
        schema,
        table,
        partial: partial ?? '',
        from: unquotedThree.index,
      };
    }
  }

  const quotedTwo = matchAtEnd(textBeforeCursor, QUOTED_TWO_PART);
  if (quotedTwo?.index != null) {
    const [, tableName, partial] = quotedTwo;
    const resolved = tableName ? resolveUniqueTable(tableName, tables) : null;
    if (resolved) {
      return {
        schema: resolved.schema,
        table: resolved.table,
        partial: partial ?? '',
        from: quotedTwo.index,
      };
    }
  }

  const unquotedTwo = matchAtEnd(textBeforeCursor, UNQUOTED_TWO_PART);
  if (unquotedTwo?.index != null) {
    const [, tableName, partial] = unquotedTwo;
    const resolved = tableName ? resolveUniqueTable(tableName, tables) : null;
    if (resolved) {
      return {
        schema: resolved.schema,
        table: resolved.table,
        partial: partial ?? '',
        from: unquotedTwo.index,
      };
    }
  }

  return null;
}
