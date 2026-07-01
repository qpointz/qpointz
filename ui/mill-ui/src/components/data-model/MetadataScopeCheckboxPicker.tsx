import { Button, Checkbox, Popover, Stack, Text } from '@mantine/core';
import { useEffect, useState } from 'react';
import { HiOutlineAdjustmentsHorizontal } from 'react-icons/hi2';
import {
  nextReadScopesAfterPickerToggle,
  scopeDisplayLabel,
} from '../../utils/modelScopeQuery';

export interface MetadataScopeCheckboxPickerProps {
  /** Scope slugs declared in the URL (picker options). */
  urlScopes: string[];
  /** Currently active scope slugs (subset of {@link urlScopes}). */
  activeScopes: string[];
  onChange: (nextActive: string[]) => void;
}

/**
 * Toolbar control to toggle among URL-declared metadata scopes in the model explorer.
 * Uses a Popover (not Menu) so it stays interactive beside other header menus and does
 * not depend on which facets are currently visible on the entity.
 */
export function MetadataScopeCheckboxPicker({
  urlScopes,
  activeScopes,
  onChange,
}: MetadataScopeCheckboxPickerProps) {
  const [opened, setOpened] = useState(false);
  const [pendingScopes, setPendingScopes] = useState<string[] | null>(null);
  const displayedScopes = pendingScopes ?? activeScopes;
  const displayedSet = new Set(displayedScopes);

  useEffect(() => {
    setPendingScopes(null);
  }, [activeScopes]);

  const toggleScope = (slug: string) => {
    const nextChecked = !displayedSet.has(slug);
    const next = nextReadScopesAfterPickerToggle(
      urlScopes,
      displayedScopes,
      slug,
      nextChecked,
    );
    setPendingScopes(next);
    onChange(next);
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
          leftSection={<HiOutlineAdjustmentsHorizontal size={14} />}
          onClick={() => setOpened((value) => !value)}
          aria-label={`Scopes (${displayedScopes.length})`}
          aria-expanded={opened}
        >
          Scopes ({displayedScopes.length})
        </Button>
      </Popover.Target>
      <Popover.Dropdown
        p="sm"
        style={{ minWidth: 180 }}
        onMouseDown={(event) => event.preventDefault()}
      >
        <Text size="xs" fw={600} mb="xs">
          Metadata scopes
        </Text>
        <Stack gap="xs">
          {urlScopes.map((slug) => (
            <Checkbox
              key={slug}
              label={scopeDisplayLabel(slug)}
              checked={displayedSet.has(slug)}
              onChange={() => toggleScope(slug)}
            />
          ))}
        </Stack>
      </Popover.Dropdown>
    </Popover>
  );
}
