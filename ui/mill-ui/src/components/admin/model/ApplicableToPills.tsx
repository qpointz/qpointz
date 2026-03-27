import { useState } from 'react';
import { Group, Pill, PillsInput, PillGroup, Popover, Text } from '@mantine/core';
import { KNOWN_TARGETS, normalizeTargetValue, targetMeta } from './knownTargets';

interface ApplicableToPillsProps {
  values: string[];
  editable?: boolean;
  draftValue?: string;
  onDraftChange?: (value: string) => void;
  onAdd?: (value: string) => void;
  onRemove?: (value: string) => void;
  placeholder?: string;
}

export function ApplicableToPills({
  values,
  editable = false,
  draftValue = '',
  onDraftChange,
  onAdd,
  onRemove,
  placeholder = 'Type value and press Enter',
}: ApplicableToPillsProps) {
  const [hintOpen, setHintOpen] = useState(false);

  const renderPill = (value: string) => {
    const meta = targetMeta(value);
    return (
      <Pill
        key={value}
        size="xs"
        withRemoveButton={editable}
        onRemove={editable ? () => onRemove?.(value) : undefined}
        style={{
          background: 'var(--mantine-color-blue-light)',
          border: '1px solid var(--mantine-color-blue-light-color)',
          color: 'var(--mantine-color-blue-light-color)',
          paddingInline: 6,
          minHeight: 'unset',
          display: 'inline-flex',
          alignItems: 'center',
        }}
      >
        <Group gap={4} wrap="nowrap" align="center" style={{ lineHeight: 1, display: 'inline-flex' }}>
          <Text fw={700} size="xs" lh={1}>{meta.icon}</Text>
          <Text size="xs" lh={1}>{meta.label}</Text>
        </Group>
      </Pill>
    );
  };

  if (!editable) {
    return (
      <Group gap={6} wrap="wrap" style={{ padding: 0, margin: 0, background: 'transparent' }}>
        {values.length === 0 ? (
          <Pill
            size="xs"
            style={{
              background: 'var(--mantine-color-blue-light)',
              border: '1px solid var(--mantine-color-blue-light-color)',
              color: 'var(--mantine-color-blue-light-color)',
              paddingInline: 6,
              minHeight: 'unset',
              display: 'inline-flex',
              alignItems: 'center',
            }}
          >
            <Text size="xs" c="dimmed" lh={1}>any</Text>
          </Pill>
        ) : (
          values.map((value) => renderPill(value))
        )}
      </Group>
    );
  }

  return (
    <PillsInput styles={{ root: { background: 'transparent', padding: 0, border: 0 } }}>
      <PillGroup>
        {values.length === 0 && (
          <Pill
            size="xs"
            style={{
              background: 'var(--mantine-color-blue-light)',
              border: '1px solid var(--mantine-color-blue-light-color)',
              color: 'var(--mantine-color-blue-light-color)',
              paddingInline: 6,
              minHeight: 'unset',
              display: 'inline-flex',
              alignItems: 'center',
            }}
          >
            <Text size="xs" c="dimmed" lh={1}>any</Text>
          </Pill>
        )}
        {values.map((value) => renderPill(value))}
        {editable && (
          <Popover
            opened={hintOpen || draftValue.trim().length > 0}
            onChange={setHintOpen}
            position="bottom-start"
            shadow="md"
            withinPortal={false}
          >
            <Popover.Target>
              <PillsInput.Field
                value={draftValue}
                onFocus={() => setHintOpen(true)}
                onBlur={() => setHintOpen(false)}
                onChange={(e) => onDraftChange?.(e.currentTarget.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ',') {
                    e.preventDefault();
                    const raw = draftValue.replace(/,$/, '').trim();
                    if (!raw) return;
                    onAdd?.(normalizeTargetValue(raw));
                    onDraftChange?.('');
                  }
                  if (e.key === 'Backspace' && !draftValue && values.length > 0) {
                    const last = values[values.length - 1];
                    if (last) onRemove?.(last);
                  }
                }}
                placeholder={placeholder}
              />
            </Popover.Target>
            <Popover.Dropdown p="xs">
              <Text size="xs" c="dimmed" mb={6}>Known targets</Text>
              <Group gap={6}>
                {KNOWN_TARGETS.map((t) => (
                  <Pill
                    key={t.urn}
                    size="xs"
                    onMouseDown={(e) => e.preventDefault()}
                    onClick={() => {
                      onAdd?.(t.urn);
                      onDraftChange?.('');
                    }}
                    style={{
                      cursor: 'pointer',
                      background: 'var(--mantine-color-blue-light)',
                      border: '1px solid var(--mantine-color-blue-light-color)',
                      color: 'var(--mantine-color-blue-light-color)',
                    }}
                  >
                    <Group gap={4} wrap="nowrap" align="center" style={{ lineHeight: 1, display: 'inline-flex' }}>
                      <Text fw={700} size="xs" lh={1}>{t.icon}</Text>
                      <Text size="xs" lh={1}>{t.slug}</Text>
                    </Group>
                  </Pill>
                ))}
              </Group>
            </Popover.Dropdown>
          </Popover>
        )}
      </PillGroup>
    </PillsInput>
  );
}

