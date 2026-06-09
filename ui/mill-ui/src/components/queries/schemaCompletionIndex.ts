import type { SchemaNode } from '../../types/schema';

/** Maximum static completion labels derived from the schema tree. */
export const MAX_COMPLETION_ENTRIES = 500;

export type SchemaCompletionKind = 'schema' | 'table' | 'column';

/** One autocomplete label derived from catalog metadata. */
export interface SchemaCompletionEntry {
  label: string;
  kind: SchemaCompletionKind;
  schema?: string;
  table?: string;
}

/**
 * Flattens a Model explorer tree into schema and qualified `schema.table` completion labels.
 * Column entries are added separately when table detail is loaded.
 */
export function buildCompletionIndexFromTree(roots: SchemaNode[]): SchemaCompletionEntry[] {
  const entries: SchemaCompletionEntry[] = [];
  const seen = new Set<string>();

  const add = (entry: SchemaCompletionEntry) => {
    if (seen.has(entry.label) || entries.length >= MAX_COMPLETION_ENTRIES) {
      return;
    }
    seen.add(entry.label);
    entries.push(entry);
  };

  const schemaNodes: SchemaNode[] = [];
  for (const root of roots) {
    if (root.type === 'MODEL') {
      schemaNodes.push(...(root.children ?? []));
    } else if (root.type === 'SCHEMA') {
      schemaNodes.push(root);
    }
  }

  for (const schemaNode of schemaNodes) {
    if (schemaNode.type !== 'SCHEMA') {
      continue;
    }
    const schemaName = schemaNode.name;
    add({ label: schemaName, kind: 'schema', schema: schemaName });
    for (const tableNode of schemaNode.children ?? []) {
      if (tableNode.type !== 'TABLE') {
        continue;
      }
      add({
        label: `${schemaName}.${tableNode.name}`,
        kind: 'table',
        schema: schemaName,
        table: tableNode.name,
      });
    }
  }

  return entries;
}

/**
 * Filters static completion entries by a case-insensitive prefix on the label.
 */
export function filterCompletionEntries(
  entries: SchemaCompletionEntry[],
  prefix: string,
): SchemaCompletionEntry[] {
  const trimmed = prefix.trim();
  if (!trimmed) {
    return entries;
  }
  const lower = trimmed.toLowerCase();
  return entries.filter((entry) => entry.label.toLowerCase().startsWith(lower));
}

/**
 * Builds column completion entries for a resolved table detail payload.
 */
export function buildColumnCompletionEntries(
  schemaName: string,
  tableName: string,
  columnNames: string[],
): SchemaCompletionEntry[] {
  const qualifiedPrefix = `${schemaName}.${tableName}.`;
  const entries: SchemaCompletionEntry[] = [];
  for (const columnName of columnNames) {
    if (entries.length >= MAX_COMPLETION_ENTRIES) {
      break;
    }
    entries.push({
      label: `${qualifiedPrefix}${columnName}`,
      kind: 'column',
      schema: schemaName,
      table: tableName,
    });
  }
  return entries;
}

/**
 * Filters column labels by the partial name after `schema.table.`.
 */
export function filterColumnEntries(
  entries: SchemaCompletionEntry[],
  partial: string,
): SchemaCompletionEntry[] {
  if (!partial) {
    return entries;
  }
  const lower = partial.toLowerCase();
  return entries.filter((entry) => {
    const column = entry.label.split('.').pop() ?? '';
    return column.toLowerCase().startsWith(lower);
  });
}
