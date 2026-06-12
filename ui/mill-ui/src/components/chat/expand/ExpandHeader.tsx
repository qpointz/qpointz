import { ActionIcon, Group, Text } from '@mantine/core';
import { HiArrowLeft } from 'react-icons/hi2';

interface ExpandHeaderProps {
  title: string;
  onBack: () => void;
}

export function ExpandHeader({ title, onBack }: ExpandHeaderProps) {
  return (
    <Group gap="sm" px="md" py="sm" style={{ borderBottom: '1px solid var(--mantine-color-default-border)' }}>
      <ActionIcon variant="subtle" onClick={onBack} aria-label="Back to message">
        <HiArrowLeft size={18} />
      </ActionIcon>
      <Text size="sm" fw={600}>
        {title}
      </Text>
    </Group>
  );
}
