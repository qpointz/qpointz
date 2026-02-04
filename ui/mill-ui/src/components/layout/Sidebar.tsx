import {
  Box,
  Text,
  Button,
  ActionIcon,
  ScrollArea,
  Modal,
  Stack,
  Switch,
  Divider,
  useMantineColorScheme,
  Tooltip,
} from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import {
  HiOutlinePlus,
  HiOutlineCog6Tooth,
  HiOutlineTrash,
  HiOutlineChatBubbleLeftRight,
  HiOutlineSun,
  HiOutlineMoon,
} from 'react-icons/hi2';
import { useChat } from '../../context/ChatContext';

interface SidebarProps {
  opened: boolean;
}

export function Sidebar({ opened }: SidebarProps) {
  const { colorScheme, toggleColorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const [settingsOpened, { open: openSettings, close: closeSettings }] = useDisclosure(false);
  const {
    state,
    activeConversation,
    createConversation,
    deleteConversation,
    setActiveConversation,
    clearAllConversations,
  } = useChat();

  const bgColor = isDark ? 'var(--mantine-color-slate-9)' : 'var(--mantine-color-slate-1)';
  const hoverBgColor = isDark ? 'var(--mantine-color-slate-8)' : 'var(--mantine-color-slate-2)';
  const activeBgColor = isDark ? 'var(--mantine-color-slate-7)' : 'var(--mantine-color-slate-3)';
  const borderColor = isDark ? 'var(--mantine-color-slate-7)' : 'var(--mantine-color-slate-3)';

  if (!opened) return null;

  return (
    <>
      <Box
        style={{
          width: '280px',
          height: '100%',
          backgroundColor: bgColor,
          borderRight: `1px solid ${borderColor}`,
          display: 'flex',
          flexDirection: 'column',
        }}
      >
        {/* Header */}
        <Box p="md" style={{ borderBottom: `1px solid ${borderColor}` }}>
          <Button
            fullWidth
            leftSection={<HiOutlinePlus size={18} />}
            variant="filled"
            color={isDark ? 'cyan' : 'teal'}
            onClick={createConversation}
          >
            New Chat
          </Button>
        </Box>

        {/* Conversation List */}
        <ScrollArea style={{ flex: 1 }} scrollbarSize={6}>
          <Box p="xs">
            {state.conversations.length === 0 ? (
              <Box py="xl" px="md" ta="center">
                <HiOutlineChatBubbleLeftRight
                  size={32}
                  color={isDark ? 'var(--mantine-color-slate-5)' : 'var(--mantine-color-slate-4)'}
                />
                <Text size="sm" c={isDark ? 'slate.4' : 'slate.5'} mt="sm">
                  No conversations yet
                </Text>
                <Text size="xs" c={isDark ? 'slate.5' : 'slate.4'}>
                  Start a new chat to begin
                </Text>
              </Box>
            ) : (
              state.conversations.map((conv) => {
                const isActive = conv.id === activeConversation?.id;
                return (
                  <Box
                    key={conv.id}
                    onClick={() => setActiveConversation(conv.id)}
                    style={{
                      padding: '10px 12px',
                      borderRadius: '8px',
                      cursor: 'pointer',
                      backgroundColor: isActive ? activeBgColor : 'transparent',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      gap: '8px',
                      marginBottom: '4px',
                      transition: 'background-color 0.15s ease',
                    }}
                    onMouseEnter={(e) => {
                      if (!isActive) {
                        e.currentTarget.style.backgroundColor = hoverBgColor;
                      }
                    }}
                    onMouseLeave={(e) => {
                      if (!isActive) {
                        e.currentTarget.style.backgroundColor = 'transparent';
                      }
                    }}
                  >
                    <Box style={{ flex: 1, minWidth: 0 }}>
                      <Text
                        size="sm"
                        fw={isActive ? 500 : 400}
                        c={isDark ? 'slate.1' : 'slate.8'}
                        lineClamp={1}
                      >
                        {conv.title}
                      </Text>
                      <Text size="xs" c={isDark ? 'slate.5' : 'slate.5'}>
                        {conv.messages.length} messages
                      </Text>
                    </Box>
                    <Tooltip label="Delete" withArrow position="right">
                      <ActionIcon
                        size="sm"
                        variant="subtle"
                        color="red"
                        onClick={(e) => {
                          e.stopPropagation();
                          deleteConversation(conv.id);
                        }}
                      >
                        <HiOutlineTrash size={14} />
                      </ActionIcon>
                    </Tooltip>
                  </Box>
                );
              })
            )}
          </Box>
        </ScrollArea>

        {/* Footer */}
        <Box
          p="md"
          style={{
            borderTop: `1px solid ${borderColor}`,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
          }}
        >
          <Button
            variant="subtle"
            color={isDark ? 'slate.4' : 'slate.6'}
            size="sm"
            leftSection={<HiOutlineCog6Tooth size={16} />}
            onClick={openSettings}
          >
            Settings
          </Button>
          <Tooltip label={isDark ? 'Light mode' : 'Dark mode'} withArrow>
            <ActionIcon
              variant="subtle"
              color={isDark ? 'slate.4' : 'slate.6'}
              size="lg"
              onClick={() => toggleColorScheme()}
            >
              {isDark ? <HiOutlineSun size={18} /> : <HiOutlineMoon size={18} />}
            </ActionIcon>
          </Tooltip>
        </Box>
      </Box>

      {/* Settings Modal */}
      <Modal
        opened={settingsOpened}
        onClose={closeSettings}
        title="Settings"
        centered
      >
        <Stack gap="md">
          <Box>
            <Text size="sm" fw={500} mb="xs">
              Appearance
            </Text>
            <Switch
              label="Dark mode"
              checked={isDark}
              onChange={() => toggleColorScheme()}
              thumbIcon={isDark ? <HiOutlineMoon size={12} /> : <HiOutlineSun size={12} />}
            />
          </Box>

          <Divider />

          <Box>
            <Text size="sm" fw={500} mb="xs">
              Data
            </Text>
            <Button
              variant="outline"
              color="red"
              size="sm"
              leftSection={<HiOutlineTrash size={16} />}
              onClick={() => {
                if (confirm('Are you sure you want to delete all conversations? This cannot be undone.')) {
                  clearAllConversations();
                  closeSettings();
                }
              }}
            >
              Clear all conversations
            </Button>
            <Text size="xs" c="dimmed" mt="xs">
              This will permanently delete all your chat history.
            </Text>
          </Box>
        </Stack>
      </Modal>
    </>
  );
}
