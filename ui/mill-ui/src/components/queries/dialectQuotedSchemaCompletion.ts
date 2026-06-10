import { schemaCompletionSource, type SQLConfig } from '@codemirror/lang-sql';
import type { Completion, CompletionContext, CompletionResult } from '@codemirror/autocomplete';
import type { AnalysisDialectIdentifiers } from '../../types/analysis';
import { quoteIdentifierPart } from './quoteSqlIdentifier';

/** Strips one layer of SQL identifier quotes or brackets. */
export function stripIdentifierQuotes(part: string): string {
  const trimmed = part.trim();
  const quoted = /^([`'"])(.*)\1$/.exec(trimmed);
  if (quoted) {
    return quoted[2] ?? trimmed;
  }
  const bracketed = /^\[(.*)\]$/.exec(trimmed);
  if (bracketed) {
    return bracketed[1] ?? trimmed;
  }
  return trimmed;
}

/**
 * Builds dialect-quoted insert text for a completion label (bare or qualified).
 */
export function dialectQuotedApplyText(
  raw: string,
  quoteStart: string,
  quoteEnd: string,
): string {
  if (!raw.includes('.')) {
    return quoteIdentifierPart(stripIdentifierQuotes(raw), quoteStart, quoteEnd);
  }
  return raw
    .split('.')
    .map((segment) => quoteIdentifierPart(stripIdentifierQuotes(segment), quoteStart, quoteEnd))
    .join('.');
}

function mapQuotedOptions(
  options: readonly Completion[],
  identifiers: AnalysisDialectIdentifiers,
): Completion[] {
  const { quoteStart, quoteEnd } = identifiers;
  return options.map((option) => {
    const raw = typeof option.apply === 'string' ? option.apply : option.label;
    return {
      ...option,
      apply: dialectQuotedApplyText(raw, quoteStart, quoteEnd),
    };
  });
}

function mapQuotedResult(
  result: CompletionResult | null,
  identifiers: AnalysisDialectIdentifiers,
): CompletionResult | null {
  if (!result) {
    return null;
  }
  return {
    ...result,
    options: mapQuotedOptions(result.options, identifiers),
  };
}

/**
 * Wraps CodeMirror {@link schemaCompletionSource} so accepted options use Analysis dialect quoting.
 */
export function dialectQuotedSchemaCompletionSource(
  config: SQLConfig,
  identifiers: AnalysisDialectIdentifiers,
) {
  const inner = schemaCompletionSource(config);
  return (context: CompletionContext): CompletionResult | null | Promise<CompletionResult | null> => {
    const result = inner(context);
    if (result instanceof Promise) {
      return result.then((resolved) => mapQuotedResult(resolved, identifiers));
    }
    return mapQuotedResult(result, identifiers);
  };
}
