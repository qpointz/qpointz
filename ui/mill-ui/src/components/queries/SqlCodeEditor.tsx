import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Box, useMantineColorScheme } from '@mantine/core';
import CodeMirror from '@uiw/react-codemirror';
import { sql } from '@codemirror/lang-sql';
import { analysisService } from '../../services/api';
import type { AnalysisDialect, AnalysisDialectIdentifiers, EditorDialectId } from '../../types/analysis';
import { resolveCodeMirrorDialect } from './codemirrorDialect';
import { toQuotedSchemaCompletions } from './quoteSqlIdentifier';
import {
  autocompletion,
  type Completion,
  type CompletionContext,
  type CompletionResult,
} from '@codemirror/autocomplete';
import { keymap, EditorView, placeholder as cmPlaceholder } from '@codemirror/view';
import { Prec } from '@codemirror/state';
import { schemaService } from '../../services/api';
import type { SchemaCompletionEntry } from './schemaCompletionIndex';
import {
  buildColumnCompletionEntries,
  buildCompletionIndexFromTree,
  filterColumnEntries,
  filterCompletionEntries,
} from './schemaCompletionIndex';

export interface SqlCodeEditorProps {
  /** SQL document text. */
  value: string;
  /** Called when the document changes. */
  onChange: (value: string) => void;
  /**
   * Invoked on Ctrl/Cmd+Enter when {@link executeEnabled} is true.
   * Receives the selected SQL fragment when the selection is non-empty; otherwise `undefined` (run full document).
   */
  onExecute?: (selectedSql?: string) => void;
  /** When false, Ctrl/Cmd+Enter is not bound. */
  executeEnabled?: boolean;
  /** Shown when the document is empty. */
  placeholder?: string;
}

const basicSetup = {
  lineNumbers: true,
  foldGutter: true,
  dropCursor: false,
  allowMultipleSelections: false,
  indentOnInput: true,
  bracketMatching: true,
  closeBrackets: true,
  autocompletion: true,
  highlightSelectionMatches: true,
} as const;

const fullHeightTheme = EditorView.theme({
  '&': { height: '100%' },
  '.cm-scroller': { overflow: 'auto', fontFamily: 'ui-monospace, SFMono-Regular, "SF Mono", Menlo, Consolas, "Liberation Mono", monospace' },
  '.cm-content': { padding: '12px 0' },
  '.cm-gutters': { borderRight: 'none' },
});

const DEFAULT_IDENTIFIER_QUOTES: AnalysisDialectIdentifiers = { quoteStart: '`', quoteEnd: '`' };

function flattenDialectFunctions(functions: AnalysisDialect['functions']): Completion[] {
  const seen = new Set<string>();
  const options: Completion[] = [];
  for (const names of Object.values(functions)) {
    for (const name of names) {
      const upper = name.toUpperCase();
      if (seen.has(upper)) {
        continue;
      }
      seen.add(upper);
      options.push({ label: upper, type: 'function', detail: 'function' });
    }
  }
  return options;
}

/**
 * CodeMirror 6 SQL editor with Mantine theming and schema-aware autocompletion.
 */
export function SqlCodeEditor({
  value,
  onChange,
  onExecute,
  executeEnabled = true,
  placeholder = 'Write your SQL query here...',
}: SqlCodeEditorProps) {
  const { colorScheme } = useMantineColorScheme();
  const onExecuteRef = useRef(onExecute);
  onExecuteRef.current = onExecute;

  const [staticIndex, setStaticIndex] = useState<SchemaCompletionEntry[]>([]);
  const [editorDialectId, setEditorDialectId] = useState<EditorDialectId>('standard');
  const [identifierQuotes, setIdentifierQuotes] = useState<AnalysisDialectIdentifiers>(DEFAULT_IDENTIFIER_QUOTES);
  const [dialectFunctions, setDialectFunctions] = useState<Completion[]>([]);
  const schemaContextRef = useRef('global');
  const columnCacheRef = useRef(new Map<string, SchemaCompletionEntry[]>());

  useEffect(() => {
    let cancelled = false;
    analysisService.getDialect().then((dialect) => {
      if (cancelled) {
        return;
      }
      setEditorDialectId(dialect.editorDialect);
      setIdentifierQuotes(dialect.identifiers);
      setDialectFunctions(flattenDialectFunctions(dialect.functions));
    }).catch(() => {
      if (!cancelled) {
        setEditorDialectId('standard');
        setIdentifierQuotes(DEFAULT_IDENTIFIER_QUOTES);
        setDialectFunctions([]);
      }
    });
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const contextInfo = await schemaService.getContext();
        schemaContextRef.current = contextInfo.selectedContext;
        const tree = await schemaService.getTree(contextInfo.selectedContext);
        if (!cancelled) {
          setStaticIndex(buildCompletionIndexFromTree(tree));
        }
      } catch {
        if (!cancelled) {
          setStaticIndex([]);
        }
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  const loadColumnEntries = useCallback(async (schema: string, table: string) => {
    const cacheKey = `${schemaContextRef.current}:${schema}.${table}`;
    const cached = columnCacheRef.current.get(cacheKey);
    if (cached) {
      return cached;
    }
    try {
      const detail = await schemaService.getTable(schema, table, schemaContextRef.current);
      const names = detail?.columns?.map((column) => column.columnName) ?? [];
      const entries = buildColumnCompletionEntries(schema, table, names);
      columnCacheRef.current.set(cacheKey, entries);
      return entries;
    } catch {
      return [];
    }
  }, []);

  const schemaCompletions = useCallback(
    (context: CompletionContext): CompletionResult | null | Promise<CompletionResult | null> => {
      const qualColumn = context.matchBefore(/(\w+)\.(\w+)\.(\w*)$/);
      if (qualColumn) {
        const match = qualColumn.text.match(/^(\w+)\.(\w+)\.(\w*)$/);
        if (!match) {
          return null;
        }
        const [, schema, table, partial] = match;
        return loadColumnEntries(schema!, table!).then((entries) => {
          const filtered = filterColumnEntries(entries, partial ?? '');
          if (filtered.length === 0 && !context.explicit) {
            return null;
          }
          return {
            from: qualColumn.from,
            options: toQuotedSchemaCompletions(
              filtered,
              identifierQuotes.quoteStart,
              identifierQuotes.quoteEnd,
            ),
          };
        });
      }

      const word = context.matchBefore(/[\w.]*$/);
      if (!word || (word.from === word.to && !context.explicit)) {
        return null;
      }
      const filtered = filterCompletionEntries(staticIndex, word.text);
      const partial = word.text.toUpperCase();
      const fnMatches = dialectFunctions.filter(
        (fn) => fn.label.toUpperCase().startsWith(partial) || partial === '',
      );
      const schemaOptions = toQuotedSchemaCompletions(
        filtered,
        identifierQuotes.quoteStart,
        identifierQuotes.quoteEnd,
      );
      const options = [...fnMatches, ...schemaOptions];
      if (options.length === 0) {
        return null;
      }
      return {
        from: word.from,
        options,
      };
    },
    [dialectFunctions, identifierQuotes, loadColumnEntries, staticIndex],
  );

  const extensions = useMemo(() => {
    const runKeymap = executeEnabled
      ? Prec.highest(
          keymap.of([
            {
              key: 'Mod-Enter',
              run: (view) => {
                const { from, to } = view.state.selection.main;
                const selectedSql = from !== to ? view.state.doc.sliceString(from, to) : undefined;
                onExecuteRef.current?.(selectedSql);
                return true;
              },
            },
          ]),
        )
      : [];

    return [
      sql({ dialect: resolveCodeMirrorDialect(editorDialectId) }),
      fullHeightTheme,
      cmPlaceholder(placeholder),
      autocompletion({ override: [schemaCompletions] }),
      runKeymap,
    ];
  }, [editorDialectId, executeEnabled, placeholder, schemaCompletions]);

  return (
    <Box style={{ height: '100%', minHeight: 0 }}>
      <CodeMirror
        value={value}
        height="100%"
        theme={colorScheme === 'dark' ? 'dark' : 'light'}
        extensions={extensions}
        onChange={onChange}
        basicSetup={basicSetup}
        style={{
          height: '100%',
          fontSize: 13,
          border: 'none',
        }}
      />
    </Box>
  );
}
