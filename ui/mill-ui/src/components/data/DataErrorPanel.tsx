import {
  Box,
  Group,
  Modal,
  Text,
  UnstyledButton,
} from '@mantine/core';
import { useState } from 'react';
import { HiOutlineExclamationCircle } from 'react-icons/hi2';
import { getDataStatusLayout } from './dataStatusLayout';

interface DataErrorPanelProps {
  message: string;
  compact?: boolean;
}

/**
 * Failed-query state: centered error affordance that opens full details in a modal.
 */
export function DataErrorPanel({ message, compact = false }: DataErrorPanelProps) {
  const [opened, setOpened] = useState(false);
  const { minHeight, iconSize, labelSize, gap } = getDataStatusLayout(compact);

  return (
    <>
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
        <UnstyledButton
          aria-label="Show error details"
          onClick={() => setOpened(true)}
          style={{ borderRadius: 8 }}
        >
          <Group gap={gap} wrap="nowrap" align="center">
            <HiOutlineExclamationCircle
              size={iconSize}
              color="var(--mantine-color-red-6)"
            />
            <Text size={labelSize} fw={600} c="red.6">
              Error
            </Text>
          </Group>
        </UnstyledButton>
      </Box>

      <Modal
        opened={opened}
        onClose={() => setOpened(false)}
        title="Data error"
        size={compact ? 'md' : 'lg'}
        centered
      >
        <Text
          size="sm"
          component="pre"
          style={{
            margin: 0,
            whiteSpace: 'pre-wrap',
            wordBreak: 'break-word',
            fontFamily: 'var(--mantine-font-family-monospace)',
            lineHeight: 1.45,
          }}
        >
          {message}
        </Text>
      </Modal>
    </>
  );
}
