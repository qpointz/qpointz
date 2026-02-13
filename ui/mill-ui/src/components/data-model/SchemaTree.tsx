import { Box, NavLink, Collapse, Text, Badge, useMantineColorScheme } from '@mantine/core';
import { useState } from 'react';
import {
  HiOutlineCircleStack,
  HiOutlineTableCells,
  HiOutlineViewColumns,
  HiChevronRight,
  HiChevronDown,
  HiOutlineChatBubbleLeftRight,
} from 'react-icons/hi2';
import type { SchemaEntity } from '../../types/schema';
import { useInlineChat } from '../../context/InlineChatContext';
import { useChatReferencesContext } from '../../context/ChatReferencesContext';
import { useFeatureFlags } from '../../features/FeatureFlagContext';

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
  const flags = useFeatureFlags();
  const { getSessionByContextId } = useInlineChat();
  const { getRefsForContextId } = useChatReferencesContext();
  const [expanded, setExpanded] = useState(depth < 1); // Auto-expand first level
  const hasChildren = entity.children && entity.children.length > 0;
  const isSelected = selectedId === entity.id;
  const Icon = entityIcons[entity.type];
  const hasChat = !!getSessionByContextId(entity.id);
  const chatRefs = flags.chatReferencesEnabled && flags.chatReferencesSidebarIndicator
    ? getRefsForContextId(entity.id)
    : [];
  const hasRelatedChats = chatRefs.length > 0;

  const handleClick = () => {
    onSelect(entity);
    if (hasChildren) {
      setExpanded(!expanded);
    }
  };

  return (
    <Box>
      <NavLink
        label={
          <Box style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
            <Text size="sm" fw={isSelected ? 500 : 400} lineClamp={1}>{entity.name}</Text>
            {hasChat && (
              <HiOutlineChatBubbleLeftRight
                size={10}
                color={isDark ? 'var(--mantine-color-cyan-4)' : 'var(--mantine-color-teal-6)'}
              />
            )}
            {hasRelatedChats && (
              <Badge
                size="xs"
                variant="light"
                color="violet"
                circle
                style={{
                  minWidth: 14,
                  height: 14,
                  padding: 0,
                  fontSize: '9px',
                }}
              >
                {chatRefs.length}
              </Badge>
            )}
          </Box>
        }
        leftSection={<Icon size={14} />}
        rightSection={
          hasChildren ? (
            expanded ? (
              <HiChevronDown size={12} />
            ) : (
              <HiChevronRight size={12} />
            )
          ) : null
        }
        active={isSelected}
        onClick={handleClick}
        variant="light"
        color={isDark ? 'cyan' : 'teal'}
        style={{
          borderRadius: 6,
        }}
        styles={{
          root: {
            padding: `4px 8px 4px ${depth * 16 + 8}px`,
            ...(isSelected && {
              backgroundColor: isDark
                ? 'var(--mantine-color-cyan-9)'
                : 'var(--mantine-color-teal-1)',
            }),
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
          color={isDark ? 'var(--mantine-color-gray-5)' : 'var(--mantine-color-gray-4)'}
        />
        <Text size="sm" c={isDark ? 'gray.4' : 'gray.5'} mt="sm">
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
