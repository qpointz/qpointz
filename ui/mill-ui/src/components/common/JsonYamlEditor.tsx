import { useEffect, useMemo, useState } from 'react';
import { Button, Group, SegmentedControl, Stack, Text } from '@mantine/core';
import { notifications } from '@mantine/notifications';
import { parse as parseYaml, stringify as stringifyYaml } from 'yaml';
import { SyntaxCodeEditor } from './SyntaxCodeEditor';

type EditorFormat = 'json' | 'yaml';

/** Expert JSON/YAML editor with CodeMirror syntax highlighting and optional parse callbacks. */
interface JsonYamlEditorProps {
  /** Serializable value shown and replaced on Apply. */
  value: unknown;
  /** Called with parsed object when the user clicks Apply. */
  onApply: (next: unknown) => void;
  /** Fired on every edit with parse success/failure (for live validation UI). */
  onDraftParsed?: (draft: { valid: boolean; value?: unknown; error?: string }) => void;
  /** Minimum editor height in pixels. */
  minHeight?: number;
}

export function JsonYamlEditor({ value, onApply, onDraftParsed, minHeight = 360 }: JsonYamlEditorProps) {
  const [format, setFormat] = useState<EditorFormat>('json');
  const [text, setText] = useState('');

  const jsonText = useMemo(() => JSON.stringify(value, null, 2), [value]);
  const yamlText = useMemo(() => stringifyYaml(value), [value]);

  useEffect(() => {
    setText(format === 'json' ? jsonText : yamlText);
  }, [format, jsonText, yamlText]);

  useEffect(() => {
    if (!onDraftParsed) return;
    try {
      const parsed = format === 'json' ? JSON.parse(text) : parseYaml(text);
      onDraftParsed({ valid: true, value: parsed });
    } catch (e) {
      onDraftParsed({ valid: false, error: e instanceof Error ? e.message : 'Invalid document' });
    }
  }, [format, onDraftParsed, text]);

  const formatDocument = () => {
    try {
      if (format === 'json') {
        setText(JSON.stringify(JSON.parse(text), null, 2));
      } else {
        setText(stringifyYaml(parseYaml(text), { lineWidth: 100 }));
      }
      notifications.show({ color: 'green', title: 'Formatted', message: 'Document reformatted.' });
    } catch (e) {
      notifications.show({
        color: 'red',
        title: 'Cannot format',
        message: e instanceof Error ? e.message : 'Invalid document',
      });
    }
  };

  const apply = () => {
    if (format === 'json') {
      onApply(JSON.parse(text));
      return;
    }
    onApply(parseYaml(text));
  };

  return (
    <Stack gap="xs" style={{ minHeight }}>
      <Group justify="space-between" wrap="wrap">
        <SegmentedControl
          value={format}
          onChange={(v) => setFormat((v as EditorFormat) ?? 'json')}
          data={[
            { label: 'JSON', value: 'json' },
            { label: 'YAML', value: 'yaml' },
          ]}
          size="xs"
        />
        <Button variant="light" size="xs" onClick={formatDocument}>
          Format
        </Button>
      </Group>
      <SyntaxCodeEditor
        key={format}
        value={text}
        onChange={setText}
        language={format}
        minHeight={minHeight}
      />
      <Group>
        <Button variant="light" onClick={apply}>Apply</Button>
        <Text size="xs" c="dimmed">Syntax-colored editor; use Format to prettify. Apply commits changes.</Text>
      </Group>
    </Stack>
  );
}
