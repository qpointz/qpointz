import {
  Box,
  Text,
  NavLink,
  ScrollArea,
  Popover,
  Group,
  Button,
  Tooltip,
  ActionIcon,
  useMantineColorScheme,
} from '@mantine/core';
import { useState } from 'react';
import { HiOutlineDocumentText, HiOutlineChatBubbleLeftRight, HiOutlineTrash } from 'react-icons/hi2';
import { useInlineChat } from '../../context/InlineChatContext';
import type { SavedQuery } from '../../types/query';

interface QuerySidebarProps {
  queries: SavedQuery[];
  activeQueryId: string | null;
  onSelectQuery: (query: SavedQuery) => void;
  onDeleteQuery?: (queryId: string) => void;
}

export function QuerySidebar({ queries, activeQueryId, onSelectQuery, onDeleteQuery }: QuerySidebarProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const { getSessionByContextId } = useInlineChat();
  const [confirmDeleteId, setConfirmDeleteId] = useState<string | null>(null);

  return (
    <>
      {/* Query list */}
      <ScrollArea style={{ flex: 1 }} type="auto">
        <Box py={4}>
          {queries.map((query) => {
            const hasChat = !!getSessionByContextId(query.id);
            return (
            <NavLink
              key={query.id}
              label={
                <Box style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                  <Text size="sm" fw={activeQueryId === query.id ? 500 : 400} lineClamp={1} style={{ flex: 1 }}>
                    {query.name}
                  </Text>
                  {hasChat && (
                    <HiOutlineChatBubbleLeftRight
                      size={10}
                      color={isDark ? 'var(--mantine-color-cyan-4)' : 'var(--mantine-color-teal-6)'}
                    />
                  )}
                </Box>
              }
              leftSection={
                <HiOutlineDocumentText
                  size={14}
                  color={
                    activeQueryId === query.id
                      ? isDark
                        ? 'var(--mantine-color-cyan-4)'
                        : 'var(--mantine-color-teal-6)'
                      : isDark
                        ? 'var(--mantine-color-gray-4)'
                        : 'var(--mantine-color-gray-5)'
                  }
                />
              }
              rightSection={
                onDeleteQuery ? (
                  <Popover
                    opened={confirmDeleteId === query.id}
                    onChange={(opened) => { if (!opened) setConfirmDeleteId(null); }}
                    position="right"
                    shadow="md"
                    withArrow
                    arrowSize={8}
                  >
                    <Popover.Target>
                      <Tooltip label="Delete" withArrow position="right" disabled={confirmDeleteId === query.id}>
                        <ActionIcon
                          size="sm"
                          variant="subtle"
                          color={confirmDeleteId === query.id ? 'red' : 'gray'}
                          onClick={(e) => {
                            e.stopPropagation();
                            setConfirmDeleteId(query.id);
                          }}
                        >
                          <HiOutlineTrash size={14} />
                        </ActionIcon>
                      </Tooltip>
                    </Popover.Target>
                    <Popover.Dropdown
                      p="xs"
                      style={{
                        backgroundColor: 'var(--mantine-color-body)',
                        border: `1px solid ${isDark ? 'var(--mantine-color-gray-6)' : 'var(--mantine-color-gray-3)'}`,
                      }}
                    >
                      <Text size="xs" fw={500} c={isDark ? 'gray.2' : 'gray.7'} mb={8}>
                        Delete this query?
                      </Text>
                      <Group gap="xs" justify="flex-end">
                        <Button
                          size="compact-xs"
                          variant="subtle"
                          color="gray"
                          onClick={(e) => {
                            e.stopPropagation();
                            setConfirmDeleteId(null);
                          }}
                        >
                          Cancel
                        </Button>
                        <Button
                          size="compact-xs"
                          variant="filled"
                          color="red"
                          onClick={(e) => {
                            e.stopPropagation();
                            onDeleteQuery(query.id);
                            setConfirmDeleteId(null);
                          }}
                        >
                          Delete
                        </Button>
                      </Group>
                    </Popover.Dropdown>
                  </Popover>
                ) : undefined
              }
              active={activeQueryId === query.id}
              onClick={() => onSelectQuery(query)}
              style={{ borderRadius: 6 }}
              styles={{
                root: {
                  padding: '4px 8px',
                  ...(activeQueryId === query.id && {
                    backgroundColor: isDark
                      ? 'var(--mantine-color-cyan-9)'
                      : 'var(--mantine-color-teal-1)',
                    color: isDark
                      ? 'var(--mantine-color-cyan-4)'
                      : 'var(--mantine-color-teal-7)',
                  }),
                },
              }}
            />
            );
          })}
        </Box>
      </ScrollArea>

      {/* Footer with tags summary */}
      <Box
        px="sm"
        py="xs"
        style={{
          borderTop: `1px solid var(--mantine-color-default-border)`,
        }}
      >
        <Text size="xs" c="dimmed">
          Click a query to load it into the editor
        </Text>
      </Box>
    </>
  );
}
