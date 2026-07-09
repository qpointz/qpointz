import {
  forwardRef,
  useCallback,
  useEffect,
  useImperativeHandle,
  useMemo,
  useRef,
  useState,
} from 'react';
import { Box, useMantineColorScheme } from '@mantine/core';
import CodeMirror from '@uiw/react-codemirror';
import { sql, type SQLNamespace } from '@codemirror/lang-sql';
import { analysisService } from '../../services/api';
import type { AnalysisDialect, AnalysisDialectIdentifiers, EditorDialectId } from '../../types/analysis';
import { resolveCodeMirrorDialect } from './codemirrorDialect';
import { dialectQuotedSchemaCompletionSource } from './dialectQuotedSchemaCompletion';
import {
  autocompletion,
  type Completion,
  type CompletionContext,
  type CompletionResult,
} from '@codemirror/autocomplete';
import { history, redo, redoDepth, undo, undoDepth } from '@codemirror/commands';
import { EditorView, placeholder as cmPlaceholder } from '@codemirror/view';
import { Prec, Transaction } from '@codemirror/state';
import { schemaService } from '../../services/api';
import {
  buildCompletionIndexFromTree,
  buildSqlNamespace,
  preloadTableColumns,
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

/** Imperative editor API for chat apply and toolbar undo/redo. */
export interface SqlCodeEditorHandle {
  replaceSql: (nextSql: string, recordHistory?: boolean) => void;
  undo: () => boolean;
  redo: () => boolean;
  canUndo: () => boolean;
  canRedo: () => boolean;
}

const basicSetup = {
  lineNumbers: true,
  foldGutter: true,
  dropCursor: false,
  allowMultipleSelections: false,
  indentOnInput: true,
  bracketMatching: true,
  closeBrackets: true,
  autocompletion: false,
  highlightSelectionMatches: true,
} as const;

const fullHeightTheme = EditorView.theme({
  '&': { height: '100%' },
  '.cm-scroller': { overflow: 'auto', fontFamily: 'ui-monospace, SFMono-Regular, "SF Mono", Menlo, Consolas, "Liberation Mono", monospace' },
  '.cm-content': { padding: '12px 0' },
  '.cm-gutters': { borderRight: 'none' },
});

const DEFAULT_IDENTIFIER_QUOTES: AnalysisDialectIdentifiers = { quoteStart: '`', quoteEnd: '`' };
const EMPTY_SQL_SCHEMA: SQLNamespace = {};

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
export const SqlCodeEditor = forwardRef<SqlCodeEditorHandle, SqlCodeEditorProps>(function SqlCodeEditor(
  {
    value,
    onChange,
    onExecute,
    executeEnabled = true,
    placeholder = 'Write your SQL query here...',
  },
  ref,
) {
  const { colorScheme } = useMantineColorScheme();
  const onChangeRef = useRef(onChange);
  onChangeRef.current = onChange;
  const onExecuteRef = useRef(onExecute);
  onExecuteRef.current = onExecute;
  const executeEnabledRef = useRef(executeEnabled);
  executeEnabledRef.current = executeEnabled;
  const viewRef = useRef<EditorView | null>(null);
  const syncingRef = useRef(false);

  const [sqlSchema, setSqlSchema] = useState<SQLNamespace>(EMPTY_SQL_SCHEMA);
  const [editorDialectId, setEditorDialectId] = useState<EditorDialectId>('standard');
  const [identifierQuotes, setIdentifierQuotes] = useState<AnalysisDialectIdentifiers>(DEFAULT_IDENTIFIER_QUOTES);
  const [dialectFunctions, setDialectFunctions] = useState<Completion[]>([]);
  const schemaContextRef = useRef('global');

  useImperativeHandle(ref, () => ({
    replaceSql(nextSql: string, recordHistory = true) {
      const view = viewRef.current;
      if (!view) {
        onChangeRef.current(nextSql);
        return;
      }
      const current = view.state.doc.toString();
      if (current === nextSql) return;
      syncingRef.current = true;
      view.dispatch({
        changes: { from: 0, to: view.state.doc.length, insert: nextSql },
        annotations: recordHistory ? Transaction.userEvent.of('input') : Transaction.addToHistory.of(false),
      });
      onChangeRef.current(nextSql);
      syncingRef.current = false;
    },
    undo() {
      const view = viewRef.current;
      return view ? undo(view) : false;
    },
    redo() {
      const view = viewRef.current;
      return view ? redo(view) : false;
    },
    canUndo() {
      const view = viewRef.current;
      return view ? undoDepth(view.state) > 0 : false;
    },
    canRedo() {
      const view = viewRef.current;
      return view ? redoDepth(view.state) > 0 : false;
    },
  }), []);

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
        const catalogIndex = buildCompletionIndexFromTree(tree);
        const columnsByTable = await preloadTableColumns(catalogIndex, (schemaName, tableName) =>
          schemaService.getTable(schemaName, tableName, schemaContextRef.current, 'none'),
        );
        if (!cancelled) {
          setSqlSchema(buildSqlNamespace(catalogIndex, columnsByTable));
        }
      } catch {
        if (!cancelled) {
          setSqlSchema(EMPTY_SQL_SCHEMA);
        }
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  const dialectFunctionCompletions = useCallback(
    (context: CompletionContext): CompletionResult | null => {
      const word = context.matchBefore(/\w*$/);
      if (!word || (word.from === word.to && !context.explicit)) {
        return null;
      }
      const partial = word.text.toUpperCase();
      const fnMatches = dialectFunctions.filter(
        (fn) => fn.label.toUpperCase().startsWith(partial) || partial === '',
      );
      if (fnMatches.length === 0) {
        return null;
      }
      return {
        from: word.from,
        options: fnMatches,
      };
    },
    [dialectFunctions],
  );

  const extensions = useMemo(() => {
    const dialect = resolveCodeMirrorDialect(editorDialectId, identifierQuotes);
    const schemaCompletions = dialectQuotedSchemaCompletionSource(
      { dialect, schema: sqlSchema },
      identifierQuotes,
    );
    const executeOnModEnter = Prec.highest(
      EditorView.domEventHandlers({
        keydown(event, view) {
          if (!executeEnabledRef.current) {
            return false;
          }
          if (event.key !== 'Enter' || !(event.ctrlKey || event.metaKey)) {
            return false;
          }
          const { from, to } = view.state.selection.main;
          const selectedSql = from !== to ? view.state.doc.sliceString(from, to) : undefined;
          onExecuteRef.current?.(selectedSql);
          return true;
        },
      }),
    );

    return [
      history(),
      sql({ dialect, upperCaseKeywords: true }),
      dialect.language.data.of({ autocomplete: schemaCompletions }),
      dialect.language.data.of({ autocomplete: dialectFunctionCompletions }),
      fullHeightTheme,
      cmPlaceholder(placeholder),
      autocompletion({ activateOnTyping: true }),
      executeOnModEnter,
      EditorView.updateListener.of((update) => {
        if (update.view) {
          viewRef.current = update.view;
        }
      }),
    ];
  }, [dialectFunctionCompletions, editorDialectId, identifierQuotes, placeholder, sqlSchema]);

  const handleChange = useCallback((next: string) => {
    if (syncingRef.current) return;
    onChangeRef.current(next);
  }, []);

  return (
    <Box style={{ height: '100%', minHeight: 0 }}>
      <CodeMirror
        value={value}
        height="100%"
        theme={colorScheme === 'dark' ? 'dark' : 'light'}
        extensions={extensions}
        onChange={handleChange}
        basicSetup={basicSetup}
        style={{
          height: '100%',
          fontSize: 13,
          border: 'none',
        }}
      />
    </Box>
  );
});
