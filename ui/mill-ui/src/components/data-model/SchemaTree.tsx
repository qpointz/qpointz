import { Box, NavLink, Collapse, Text, useMantineColorScheme } from '@mantine/core';
import { useState } from 'react';
import {
  HiOutlineCircleStack,
  HiOutlineTableCells,
  HiOutlineViewColumns,
  HiChevronRight,
  HiChevronDown,
} from 'react-icons/hi2';
import type { SchemaEntity } from '../../types/schema';

interface SchemaTreeProps {
  tree: SchemaEntity[];
  selectedId: string | null;
  onSelect: (entity: SchemaEntity) => void;
}

const entityIcons = {
  SCHEMA: HiOutlineCircleStack,
  TABLE: HiOutlineTableCells,
  ATTRIBUTE: HiOutlineViewColumns,
};

function TreeNode({
  entity,
  selectedId,
  onSelect,
  depth = 0,
}: {
  entity: SchemaEntity;
  selectedId: string | null;
  onSelect: (entity: SchemaEntity) => void;
  depth?: number;
}) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const [expanded, setExpanded] = useState(depth < 1); // Auto-expand first level
  const hasChildren = entity.children && entity.children.length > 0;
  const isSelected = selectedId === entity.id;
  const Icon = entityIcons[entity.type];

  const handleClick = () => {
    onSelect(entity);
    if (hasChildren) {
      setExpanded(!expanded);
    }
  };

  return (
    <Box>
      <NavLink
        label={entity.name}
        leftSection={<Icon size={16} />}
        rightSection={
          hasChildren ? (
            expanded ? (
              <HiChevronDown size={14} />
            ) : (
              <HiChevronRight size={14} />
            )
          ) : null
        }
        active={isSelected}
        onClick={handleClick}
        variant="light"
        color={isDark ? 'cyan' : 'teal'}
        style={{
          paddingLeft: depth * 16 + 8,
          borderRadius: 6,
        }}
        styles={{
          root: {
            '&[data-active]': {
              backgroundColor: isDark
                ? 'var(--mantine-color-cyan-9)'
                : 'var(--mantine-color-teal-1)',
            },
          },
          label: {
            fontWeight: isSelected ? 500 : 400,
          },
        }}
      />
      {hasChildren && (
        <Collapse in={expanded}>
          <Box>
            {entity.children!.map((child) => (
              <TreeNode
                key={child.id}
                entity={child}
                selectedId={selectedId}
                onSelect={onSelect}
                depth={depth + 1}
              />
            ))}
          </Box>
        </Collapse>
      )}
    </Box>
  );
}

export function SchemaTree({ tree, selectedId, onSelect }: SchemaTreeProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';

  if (tree.length === 0) {
    return (
      <Box py="xl" ta="center">
        <HiOutlineCircleStack
          size={32}
          color={isDark ? 'var(--mantine-color-slate-5)' : 'var(--mantine-color-slate-4)'}
        />
        <Text size="sm" c={isDark ? 'slate.4' : 'slate.5'} mt="sm">
          No schemas available
        </Text>
      </Box>
    );
  }

  return (
    <Box>
      {tree.map((entity) => (
        <TreeNode
          key={entity.id}
          entity={entity}
          selectedId={selectedId}
          onSelect={onSelect}
        />
      ))}
    </Box>
  );
}
