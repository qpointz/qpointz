import { Box, Group, Loader, Text } from '@mantine/core';
import { getDataStatusLayout } from './dataStatusLayout';

interface DataLoadingPanelProps {
  compact?: boolean;
  label?: string;
}

/** Centered loading state aligned with {@link DataErrorPanel}. */
export function DataLoadingPanel({ compact = false, label = 'Loading' }: DataLoadingPanelProps) {
  const { minHeight, iconSize, labelSize, gap } = getDataStatusLayout(compact);

  return (
    <Box
      style={{
        flex: 1,
        minHeight,
        width: '100%',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      <Group gap={gap} wrap="nowrap" align="center">
        <Loader size={iconSize} type="dots" aria-label={label} />
        <Text size={labelSize} fw={600} c="dimmed">
          {label}
        </Text>
      </Group>
    </Box>
  );
}
