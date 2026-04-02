import { Badge, Box, Group, Stack, Text } from '@mantine/core';
import type { ReactNode } from 'react';

/**
 * Read-only value when the facet schema is a generic OBJECT without nested `fields`
 * (e.g. flow `effectiveMapping` / `params` maps). Recurses for nested objects and arrays.
 */
export function renderGenericFacetValueReadOnly(value: unknown): ReactNode {
  if (value == null) {
    return (
      <Text size="xs" c="dimmed">
        —
      </Text>
    );
  }
  if (typeof value === 'boolean') {
    return (
      <Badge size="xs" variant="light" color={value ? 'green' : 'gray'}>
        {String(value)}
      </Badge>
    );
  }
  if (typeof value === 'number' && Number.isFinite(value)) {
    return (
      <Text size="sm" ff="monospace">
        {String(value)}
      </Text>
    );
  }
  if (typeof value === 'string') {
    return (
      <Text size="sm" style={{ wordBreak: 'break-word', whiteSpace: 'pre-wrap' }}>
        {value}
      </Text>
    );
  }
  if (Array.isArray(value)) {
    if (value.length === 0) {
      return (
        <Text size="xs" c="dimmed" ff="monospace">
          []
        </Text>
      );
    }
    return (
      <Stack gap={6} pl="xs" style={{ borderLeft: '2px solid var(--mantine-color-default-border)' }}>
        {value.map((item, i) => (
          <Group key={i} gap={8} align="flex-start" wrap="nowrap">
            <Text size="xs" c="dimmed" mt={2} style={{ flexShrink: 0 }}>
              {i + 1}.
            </Text>
            <Box style={{ flex: 1, minWidth: 0 }}>{renderGenericFacetValueReadOnly(item)}</Box>
          </Group>
        ))}
      </Stack>
    );
  }
  if (typeof value === 'object') {
    return (
      <Box pl="xs" style={{ borderLeft: '2px solid var(--mantine-color-default-border)' }}>
        {renderGenericFacetObjectReadOnly(value as Record<string, unknown>)}
      </Box>
    );
  }
  return (
    <Text size="xs" c="dimmed">
      {String(value)}
    </Text>
  );
}

/** Key-value presentation for arbitrary record-shaped facet fragments (inferred display mode). */
export function renderGenericFacetObjectReadOnly(obj: Record<string, unknown>): ReactNode {
  const keys = Object.keys(obj)
    .filter((k) => obj[k] !== undefined)
    .sort((a, b) => a.localeCompare(b));
  if (keys.length === 0) {
    return (
      <Text size="xs" c="dimmed">
        —
      </Text>
    );
  }
  return (
    <Stack gap={6}>
      {keys.map((k) => (
        <Group key={k} gap="sm" wrap="nowrap" align="flex-start">
          <Text size="xs" fw={500} c="dimmed" style={{ minWidth: 120, flexShrink: 0 }}>
            {k}
          </Text>
          <Box style={{ flex: 1, minWidth: 0 }}>{renderGenericFacetValueReadOnly(obj[k])}</Box>
        </Group>
      ))}
    </Stack>
  );
}
