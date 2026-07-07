import { Box, useMantineColorScheme } from '@mantine/core';
import CodeMirror from '@uiw/react-codemirror';
import { sql as sqlLang } from '@codemirror/lang-sql';
import { EditorView } from '@codemirror/view';
import { useMemo } from 'react';
import { formatSqlForDisplay } from '../../../utils/formatSqlForDisplay';

interface SqlReadOnlyPanelProps {
  sql: string;
  /** Bounded height for standalone use. Ignored when {@link fill} is true. */
  maxHeight?: number;
  /** Stretch to fill the parent tab pane (artifact card tabs). */
  fill?: boolean;
}

const readOnlyTheme = EditorView.theme({
  '&': { fontSize: '13px', height: '100%' },
  '.cm-scroller': {
    overflow: 'auto',
    fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Consolas, monospace',
  },
  '.cm-content': { padding: '4px 0' },
  '.cm-gutters': { borderRight: 'none' },
});

export function SqlReadOnlyPanel({ sql, maxHeight = 220, fill = false }: SqlReadOnlyPanelProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const displaySql = useMemo(() => formatSqlForDisplay(sql), [sql]);
  const editorHeight = fill ? '100%' : `${Math.min(maxHeight, 400)}px`;

  return (
    <Box
      style={
        fill
          ? {
              flex: 1,
              minHeight: 0,
              display: 'flex',
              flexDirection: 'column',
              overflow: 'hidden',
              borderRadius: 6,
            }
          : { maxHeight, overflow: 'hidden', borderRadius: 6 }
      }
    >
      <CodeMirror
        value={displaySql}
        height={editorHeight}
        extensions={[sqlLang(), EditorView.editable.of(false), readOnlyTheme]}
        theme={isDark ? 'dark' : 'light'}
        basicSetup={{ lineNumbers: true, foldGutter: false, highlightActiveLine: false }}
      />
    </Box>
  );
}
