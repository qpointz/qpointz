import { Box, useMantineColorScheme } from '@mantine/core';
import CodeMirror from '@uiw/react-codemirror';
import { sql as sqlLang } from '@codemirror/lang-sql';
import { EditorView } from '@codemirror/view';

interface SqlReadOnlyPanelProps {
  sql: string;
  /** Bounded height for in-chat condensed view. */
  maxHeight?: number;
}

const readOnlyTheme = EditorView.theme({
  '&': { fontSize: '13px' },
  '.cm-scroller': {
    overflow: 'auto',
    fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Consolas, monospace',
  },
  '.cm-content': { padding: '4px 0' },
  '.cm-gutters': { borderRight: 'none' },
});

export function SqlReadOnlyPanel({ sql, maxHeight = 220 }: SqlReadOnlyPanelProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';

  return (
    <Box style={{ maxHeight, overflow: 'hidden', borderRadius: 6 }}>
      <CodeMirror
        value={sql}
        height={`${Math.min(maxHeight, 400)}px`}
        extensions={[sqlLang(), EditorView.editable.of(false), readOnlyTheme]}
        theme={isDark ? 'dark' : 'light'}
        basicSetup={{ lineNumbers: true, foldGutter: false, highlightActiveLine: false }}
      />
    </Box>
  );
}
