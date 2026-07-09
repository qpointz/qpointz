import { Box, NavLink, Collapse, Text, Badge, ActionIcon, useMantineColorScheme } from '@mantine/core';
import { useEffect, useState, type MouseEvent } from 'react';
import {
  HiOutlineCircleStack,
  HiOutlineTableCells,
  HiOutlineViewColumns,
  HiOutlineCube,
  HiChevronRight,
  HiChevronDown,
  HiOutlineChatBubbleLeftRight,
} from 'react-icons/hi2';
import type { SchemaNode } from '../../types/schema';
import { catalogIdsEqual } from './catalogEntityId';
import { collectTreeExpansionIds, defaultExpandedRootIds } from './schemaTreeExpansion';
import { useInlineChat } from '../../context/InlineChatContext';
import { useChatReferencesContext } from '../../context/ChatReferencesContext';
import { useFeatureFlags } from '../../features/FeatureFlagContext';

interface SchemaTreeProps {
  tree: SchemaNode[];
  selectedId: string | null;
  onSelect: (entity: SchemaNode) => void;
}

const entityIcons = {
  MODEL: HiOutlineCube,
  SCHEMA: HiOutlineCircleStack,
  TABLE: HiOutlineTableCells,
  COLUMN: HiOutlineViewColumns,
};

function TreeNode({
  entity,
  selectedId,
  onSelect,
  depth = 0,
  expandedIds,
  onToggleExpand,
}: {
  entity: SchemaNode;
  selectedId: string | null;
  onSelect: (entity: SchemaNode) => void;
  depth?: number;
  expandedIds: Set<string>;
  onToggleExpand: (nodeId: string, expanded: boolean) => void;
}) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const flags = useFeatureFlags();
  const { getSessionByContext } = useInlineChat();
  const { getRefsForContextId } = useChatReferencesContext();
  const expanded = expandedIds.has(entity.id);
  const hasChildren = entity.children && entity.children.length > 0;
  const isExpandable = hasChildren || entity.type === 'TABLE';
  const isSelected = selectedId != null && catalogIdsEqual(selectedId, entity.id);
  const Icon = entityIcons[entity.type];
  const hasChat = !!getSessionByContext('model', entity.id);
  const chatRefs =
    flags.chatReferencesEnabled &&
    flags.chatReferencesSidebarIndicator &&
    flags.chatReferencesModelContext
      ? getRefsForContextId(entity.id)
      : [];
  const hasRelatedChats = chatRefs.length > 0;

  const handleClick = () => {
    onSelect(entity);
  };

  const handleToggleExpand = (event: MouseEvent) => {
    event.preventDefault();
    event.stopPropagation();
    if (hasChildren) {
      onToggleExpand(entity.id, !expanded);
      return;
    }
    if (entity.type === 'TABLE') {
      onSelect(entity);
      onToggleExpand(entity.id, true);
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
          isExpandable ? (
            <ActionIcon
              size="sm"
              variant="subtle"
              onClick={handleToggleExpand}
              aria-label={expanded ? 'Collapse' : 'Expand'}
            >
              {expanded ? <HiChevronDown size={12} /> : <HiChevronRight size={12} />}
            </ActionIcon>
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
      {isExpandable && (
        <Collapse expanded={expanded}>
          <Box>
            {hasChildren
              ? entity.children!.map((child) => (
                  <TreeNode
                    key={child.id}
                    entity={child}
                    selectedId={selectedId}
                    onSelect={onSelect}
                    depth={depth + 1}
                    expandedIds={expandedIds}
                    onToggleExpand={onToggleExpand}
                  />
                ))
              : null}
          </Box>
        </Collapse>
      )}
    </Box>
  );
}

export function SchemaTree({ tree, selectedId, onSelect }: SchemaTreeProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const [expandedIds, setExpandedIds] = useState<Set<string>>(() => new Set());

  useEffect(() => {
    setExpandedIds((prev) => {
      const next = new Set(prev);
      defaultExpandedRootIds(tree).forEach((id) => next.add(id));
      return next;
    });
  }, [tree]);

  useEffect(() => {
    if (!selectedId) return;
    const fromSelection = collectTreeExpansionIds(tree, selectedId);
    if (fromSelection.size === 0) return;
    setExpandedIds((prev) => {
      const next = new Set(prev);
      fromSelection.forEach((id) => next.add(id));
      return next;
    });
  }, [tree, selectedId]);

  const handleToggleExpand = (nodeId: string, expanded: boolean) => {
    setExpandedIds((prev) => {
      const next = new Set(prev);
      if (expanded) {
        next.add(nodeId);
      } else {
        next.delete(nodeId);
      }
      return next;
    });
  };

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
          expandedIds={expandedIds}
          onToggleExpand={handleToggleExpand}
        />
      ))}
    </Box>
  );
}
