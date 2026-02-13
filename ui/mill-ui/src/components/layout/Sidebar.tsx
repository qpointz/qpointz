import {
  Box,
  Text,
  NavLink,
  ScrollArea,
  Popover,
  Group,
  Button,
  useMantineColorScheme,
  Tooltip,
  ActionIcon,
} from '@mantine/core';
import { useState } from 'react';
import {
  HiOutlineTrash,
  HiOutlineChatBubbleLeftRight,
} from 'react-icons/hi2';
import { useChat } from '../../context/ChatContext';

export function Sidebar() {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const {
    state,
    activeConversation,
    deleteConversation,
    setActiveConversation,
  } = useChat();

  const [confirmDeleteId, setConfirmDeleteId] = useState<string | null>(null);

  return (
    <ScrollArea style={{ flex: 1 }} type="auto">
      <Box py={4}>
        {state.conversations.length === 0 ? (
          <Box py="xl" px="md" ta="center">
            <HiOutlineChatBubbleLeftRight
              size={32}
              color={isDark ? 'var(--mantine-color-gray-5)' : 'var(--mantine-color-gray-4)'}
            />
            <Text size="sm" c={isDark ? 'gray.4' : 'gray.5'} mt="sm">
              No conversations yet
            </Text>
            <Text size="xs" c="dimmed">
              Start a new chat to begin
            </Text>
          </Box>
        ) : (
          state.conversations.map((conv) => {
            const isActive = conv.id === activeConversation?.id;
            return (
              <NavLink
                key={conv.id}
                label={
                  <Text size="sm" fw={isActive ? 500 : 400} lineClamp={1}>
                    {conv.title}
                  </Text>
                }
                description={
                  <Text size="xs" c="dimmed" style={{ fontSize: 10 }}>
                    {conv.messages.length} messages
                  </Text>
                }
                leftSection={
                  <HiOutlineChatBubbleLeftRight
                    size={14}
                    color={
                      isActive
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
                  <Popover
                    opened={confirmDeleteId === conv.id}
                    onChange={(opened) => { if (!opened) setConfirmDeleteId(null); }}
                    position="right"
                    shadow="md"
                    withArrow
                    arrowSize={8}
                  >
                    <Popover.Target>
                      <Tooltip label="Delete" withArrow position="right" disabled={confirmDeleteId === conv.id}>
                        <ActionIcon
                          size="sm"
                          variant="subtle"
                          color={confirmDeleteId === conv.id ? 'red' : 'gray'}
                          onClick={(e) => {
                            e.stopPropagation();
                            setConfirmDeleteId(conv.id);
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
                        Delete this chat?
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
                            deleteConversation(conv.id);
                            setConfirmDeleteId(null);
                          }}
                        >
                          Delete
                        </Button>
                      </Group>
                    </Popover.Dropdown>
                  </Popover>
                }
                active={isActive}
                onClick={() => setActiveConversation(conv.id)}
                style={{ borderRadius: 6 }}
                styles={{
                  root: {
                    padding: '4px 8px',
                    ...(isActive && {
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
          })
        )}
      </Box>
    </ScrollArea>
  );
}
