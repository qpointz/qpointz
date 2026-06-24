import { forwardRef, useEffect, useImperativeHandle, useMemo, useState } from 'react';
import { useDebouncedValue } from '@mantine/hooks';
import { Box, Button, Group, SegmentedControl, Stack, Text } from '@mantine/core';
import { notifications } from '@mantine/notifications';
import { parse as parseYaml, stringify as stringifyYaml } from 'yaml';
import { SyntaxCodeEditor } from './SyntaxCodeEditor';

type EditorFormat = 'json' | 'yaml';

/** Parse buffer and make YAML trees JSON-serializable (plain objects/arrays) for APIs and `facetTypeManifestFromWire`. */
function normalizeParsedForWire(parsed: unknown, sourceFormat: EditorFormat): unknown {
  if (sourceFormat !== 'yaml') return parsed;
  return JSON.parse(JSON.stringify(parsed));
}

function serializeForFormat(value: unknown, format: EditorFormat): string {
  return format === 'json' ? JSON.stringify(value, null, 2) : stringifyYaml(value);
}

export type JsonYamlEditorHandle = {
  /**
   * Parses the current editor text immediately (no debounce). Use on Save / leave-expert so YAML mode
   * does not rely on a stale `onDraftParsed` snapshot.
   */
  getParsedForSubmit: () => { ok: true; value: unknown } | { ok: false; error: string };
};

/** Expert JSON/YAML editor with CodeMirror syntax highlighting and optional parse callbacks. */
interface JsonYamlEditorProps {
  /** Serializable value shown when format toggles or parent state replaces the buffer. */
  value: unknown;
  /** Snapshot to restore on Rollback (defaults to `value`). */
  rollbackValue?: unknown;
  /** Fired on every edit with parse success/failure (for live validation UI). */
  onDraftParsed?: (draft: { valid: boolean; value?: unknown; error?: string }) => void;
  /** Minimum editor height in pixels. */
  minHeight?: number;
  /** When true, expand the code editor to fill remaining vertical space in a flex parent. */
  fillHeight?: boolean;
}

export const JsonYamlEditor = forwardRef<JsonYamlEditorHandle, JsonYamlEditorProps>(
  function JsonYamlEditor(
    { value, rollbackValue, onDraftParsed, minHeight = 360, fillHeight = false },
    ref
  ) {
    const [format, setFormat] = useState<EditorFormat>('yaml');
    const [text, setText] = useState('');

    const jsonText = useMemo(() => JSON.stringify(value, null, 2), [value]);
    const yamlText = useMemo(() => stringifyYaml(value), [value]);
    const rollbackSource = rollbackValue ?? value;

    useEffect(() => {
      setText(format === 'json' ? jsonText : yamlText);
    }, [format, jsonText, yamlText]);

    useImperativeHandle(
      ref,
      () => ({
        getParsedForSubmit: () => {
          try {
            const parsed = format === 'json' ? JSON.parse(text) : parseYaml(text);
            const wire = normalizeParsedForWire(parsed, format);
            return { ok: true, value: wire };
          } catch (e) {
            return {
              ok: false,
              error: e instanceof Error ? e.message : 'Invalid document',
            };
          }
        },
      }),
      [format, text]
    );

    /** Debounce parse + parent validation so keystrokes stay on the main thread; editor text updates immediately. */
    const [debouncedText] = useDebouncedValue(text, 120);

    useEffect(() => {
      if (!onDraftParsed) return;
      try {
        const parsed = format === 'json' ? JSON.parse(debouncedText) : parseYaml(debouncedText);
        const wire = normalizeParsedForWire(parsed, format);
        onDraftParsed({ valid: true, value: wire });
      } catch (e) {
        onDraftParsed({ valid: false, error: e instanceof Error ? e.message : 'Invalid document' });
      }
    }, [format, onDraftParsed, debouncedText]);

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

    const rollback = () => {
      try {
        const nextText = serializeForFormat(rollbackSource, format);
        setText(nextText);
        if (onDraftParsed) {
          onDraftParsed({ valid: true, value: rollbackSource });
        }
        notifications.show({
          color: 'blue',
          title: 'Reverted',
          message: 'Editor restored to the snapshot from when expert mode was opened.',
        });
      } catch (e) {
        notifications.show({
          color: 'red',
          title: 'Cannot revert',
          message: e instanceof Error ? e.message : 'Invalid snapshot',
        });
      }
    };

    return (
      <Stack
        gap="xs"
        style={
          fillHeight
            ? { flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column' }
            : { minHeight }
        }
      >
        <Group justify="space-between" wrap="nowrap" style={{ flexShrink: 0 }}>
          <SegmentedControl
            value={format}
            onChange={(v) => setFormat((v as EditorFormat) ?? 'json')}
            data={[
              { label: 'JSON', value: 'json' },
              { label: 'YAML', value: 'yaml' },
            ]}
            size="xs"
          />
          <Group gap="xs" wrap="nowrap">
            <Button variant="light" size="xs" onClick={formatDocument}>
              Format
            </Button>
            <Button variant="default" size="xs" onClick={rollback}>
              Rollback
            </Button>
          </Group>
        </Group>
        <Box style={fillHeight ? { flex: 1, minHeight, display: 'flex', flexDirection: 'column', minWidth: 0 } : undefined}>
          <SyntaxCodeEditor
            key={format}
            value={text}
            onChange={setText}
            language={format}
            minHeight={minHeight}
            fillHeight={fillHeight}
          />
        </Box>
        <Text size="xs" c="dimmed" style={{ flexShrink: 0 }}>
          Syntax-colored editor; use Format to prettify. Rollback discards unsaved edits in the buffer. Save commits
          the current document.
        </Text>
      </Stack>
    );
  }
);
