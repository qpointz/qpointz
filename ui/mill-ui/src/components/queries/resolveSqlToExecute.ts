/**
 * Resolves the SQL text to send to the execution API.
 *
 * @param documentSql full editor document
 * @param selectedSql optional editor selection (Ctrl/Cmd+Enter path)
 */
export function resolveSqlToExecute(documentSql: string, selectedSql?: string): string {
  const trimmedSelection = selectedSql?.trim();
  if (trimmedSelection) {
    return trimmedSelection;
  }
  return documentSql.trim();
}
