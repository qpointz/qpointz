/**
 * Normalizes ad-hoc SQL before submission to the Mill query engine (Calcite).
 * Strips trailing statement separators the parser rejects on single-statement submits.
 */
export function normalizeSqlForExecution(sql: string): string {
  let text = sql.trim();
  while (text.endsWith(';')) {
    text = text.slice(0, -1).trimEnd();
  }
  return text;
}
