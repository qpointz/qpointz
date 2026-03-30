import { Group, Pill, PillGroup, PillsInput, Text } from '@mantine/core';

export interface StereotypeTagsPillsProps {
  /** Current tags in display order. */
  tags: string[];
  /** When false, renders read-only pills. */
  editable?: boolean;
  /** Draft text for the inline input (edit mode). */
  draftValue?: string;
  onDraftChange?: (value: string) => void;
  /** Adds a single tag (caller deduplicates). */
  onAdd?: (tag: string) => void;
  onRemove?: (tag: string) => void;
  placeholder?: string;
}

/**
 * Editable stereotype tags: Enter or comma commits; a single entry may contain several comma-separated
 * tokens which are all added at once.
 */
export function StereotypeTagsPills({
  tags,
  editable = false,
  draftValue = '',
  onDraftChange,
  onAdd,
  onRemove,
  placeholder = 'Type tag(s), comma or Enter to add',
}: StereotypeTagsPillsProps) {
  const commitDraft = () => {
    const raw = draftValue.replace(/,$/, '').trim();
    if (!raw) return;
    const parts = raw
      .split(',')
      .map((s) => s.trim())
      .filter((s) => s.length > 0);
    if (parts.length === 0) return;
    const seen = new Set(tags);
    for (const p of parts) {
      if (!seen.has(p)) {
        seen.add(p);
        onAdd?.(p);
      }
    }
    onDraftChange?.('');
  };

  const renderPill = (value: string) => (
    <Pill
      key={value}
      size="xs"
      withRemoveButton={editable}
      onRemove={editable ? () => onRemove?.(value) : undefined}
      style={{
        background: 'var(--mantine-color-violet-light)',
        border: '1px solid var(--mantine-color-violet-light-color)',
        color: 'var(--mantine-color-violet-light-color)',
        paddingInline: 6,
        minHeight: 'unset',
        display: 'inline-flex',
        alignItems: 'center',
      }}
    >
      <Text size="xs" lh={1}>
        {value}
      </Text>
    </Pill>
  );

  if (!editable) {
    return (
      <Group gap={6} wrap="wrap" style={{ padding: 0, margin: 0, background: 'transparent' }}>
        {tags.length === 0 ? (
          <Text size="xs" c="dimmed">
            —
          </Text>
        ) : (
          tags.map((value) => renderPill(value))
        )}
      </Group>
    );
  }

  return (
    <PillsInput styles={{ root: { background: 'transparent', padding: 0, border: 0 } }}>
      <PillGroup>
        {tags.map((value) => renderPill(value))}
        <PillsInput.Field
          value={draftValue}
          onChange={(e) => onDraftChange?.(e.currentTarget.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter' || e.key === ',') {
              e.preventDefault();
              commitDraft();
            }
            if (e.key === 'Backspace' && !draftValue && tags.length > 0) {
              const last = tags[tags.length - 1];
              if (last) onRemove?.(last);
            }
          }}
          placeholder={placeholder}
        />
      </PillGroup>
    </PillsInput>
  );
}
