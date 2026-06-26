import { format as formatSQL } from 'sql-formatter';

/**
 * Pretty-prints SQL for read-only UI surfaces (chat SQL tab, highlights).
 * Returns the original text when the formatter cannot parse the input.
 */
export function formatSqlForDisplay(sql: string): string {
  const trimmed = sql.trim();
  if (!trimmed) return sql;
  try {
    return formatSQL(trimmed, {
      language: 'sql',
      tabWidth: 2,
      keywordCase: 'upper',
      linesBetweenQueries: 2,
    });
  } catch {
    return sql;
  }
}
