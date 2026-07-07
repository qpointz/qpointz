import { ActionIcon, Group, Text, Tooltip } from '@mantine/core';
import { HiChevronLeft, HiChevronRight } from 'react-icons/hi2';

export interface DataPaginationControlsProps {
  pageLabel: string;
  hasPrevious: boolean;
  hasNext: boolean;
  disabled?: boolean;
  onPrevPage?: () => void;
  onNextPage?: () => void;
}

/** Compact prev / page label / next control for toolbars. */
export function DataPaginationControls({
  pageLabel,
  hasPrevious,
  hasNext,
  disabled = false,
  onPrevPage,
  onNextPage,
}: DataPaginationControlsProps) {
  return (
    <Group gap={2} wrap="nowrap" justify="center">
      <Tooltip label="Previous page" withArrow>
        <ActionIcon
          variant="subtle"
          size="sm"
          disabled={disabled || !hasPrevious}
          onClick={onPrevPage}
          aria-label="Previous page"
        >
          <HiChevronLeft size={14} />
        </ActionIcon>
      </Tooltip>
      <Text size="xs" c="dimmed" style={{ minWidth: 72, textAlign: 'center' }}>
        {pageLabel}
      </Text>
      <Tooltip label="Next page" withArrow>
        <ActionIcon
          variant="subtle"
          size="sm"
          disabled={disabled || !hasNext}
          onClick={onNextPage}
          aria-label="Next page"
        >
          <HiChevronRight size={14} />
        </ActionIcon>
      </Tooltip>
    </Group>
  );
}
