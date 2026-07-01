import { Button, Checkbox, Popover, Stack, Text } from '@mantine/core';
import { useState } from 'react';
import { HiOutlineTag } from 'react-icons/hi2';

export interface FacetTagFilterPickerProps {
  /** Distinct tags on the current entity's facets. */
  tags: string[];
  /** Active tag filter; all tags selected shows every tagged facet. */
  selectedTags: ReadonlySet<string>;
  onSelectedTagsChange: (next: Set<string>) => void;
}

/**
 * Toolbar control to filter visible facet cards by payload tags (local UI only).
 * Matches {@link MetadataScopeCheckboxPicker} placement and interaction pattern.
 */
export function FacetTagFilterPicker({
  tags,
  selectedTags,
  onSelectedTagsChange,
}: FacetTagFilterPickerProps) {
  const [opened, setOpened] = useState(false);

  if (tags.length === 0) {
    return null;
  }

  const toggleTag = (tag: string) => {
    const next = new Set(selectedTags);
    if (next.has(tag)) {
      if (next.size <= 1) {
        return;
      }
      next.delete(tag);
    } else {
      next.add(tag);
    }
    onSelectedTagsChange(next);
  };

  return (
    <Popover
      opened={opened}
      onChange={setOpened}
      position="bottom-end"
      trapFocus={false}
      withinPortal
    >
      <Popover.Target>
        <Button
          size="xs"
          variant="subtle"
          leftSection={<HiOutlineTag size={14} />}
          onClick={() => setOpened((value) => !value)}
          aria-label={`Tags (${selectedTags.size})`}
          aria-expanded={opened}
        >
          Tags (
          {selectedTags.size}
          )
        </Button>
      </Popover.Target>
      <Popover.Dropdown
        p="sm"
        style={{ minWidth: 180 }}
        onMouseDown={(event) => event.preventDefault()}
      >
        <Text size="xs" fw={600} mb="xs">
          Filter by tag
        </Text>
        <Stack gap="xs">
          {tags.map((tag) => (
            <Checkbox
              key={tag}
              label={tag}
              checked={selectedTags.has(tag)}
              disabled={selectedTags.has(tag) && selectedTags.size <= 1}
              onChange={() => toggleTag(tag)}
            />
          ))}
        </Stack>
      </Popover.Dropdown>
    </Popover>
  );
}
