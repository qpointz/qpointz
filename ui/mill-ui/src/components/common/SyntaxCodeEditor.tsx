import { useMemo } from 'react';
import { Box, useMantineColorScheme } from '@mantine/core';
import CodeMirror from '@uiw/react-codemirror';
import { json, jsonParseLinter } from '@codemirror/lang-json';
import { yaml } from '@codemirror/lang-yaml';
import { linter, lintGutter } from '@codemirror/lint';

export interface SyntaxCodeEditorProps {
  /** Document text. */
  value: string;
  /** Called when the document changes (omit when {@link readOnly}). */
  onChange?: (value: string) => void;
  language: 'json' | 'yaml';
  /** Minimum height in pixels. */
  minHeight?: number;
  /** Read-only display (no editing, no lint gutter for JSON). */
  readOnly?: boolean;
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

/**
 * Single-language CodeMirror surface with Mantine light/dark theme.
 * JSON mode includes parse diagnostics; YAML uses the Lezer YAML highlighter.
 */
export function SyntaxCodeEditor({
  value,
  onChange,
  language,
  minHeight = 360,
  readOnly = false,
}: SyntaxCodeEditorProps) {
  const { colorScheme } = useMantineColorScheme();

  const extensions = useMemo(() => {
    const lang = language === 'json' ? json() : yaml();
    if (readOnly || language !== 'json') {
      return [lang];
    }
    return [lang, linter(jsonParseLinter()), lintGutter()];
  }, [language, readOnly]);

  const minH = `${minHeight}px`;

  return (
    <Box
      style={{
        border: '1px solid var(--mantine-color-default-border)',
        borderRadius: 'var(--mantine-radius-sm)',
        overflow: 'hidden',
      }}
    >
      <CodeMirror
        value={value}
        height={minH}
        theme={colorScheme === 'dark' ? 'dark' : 'light'}
        extensions={extensions}
        onChange={readOnly ? undefined : onChange}
        editable={!readOnly}
        readOnly={readOnly}
        basicSetup={basicSetup}
        style={{ fontSize: 13 }}
      />
    </Box>
  );
}
