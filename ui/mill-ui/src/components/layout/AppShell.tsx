import { Box, Badge, ActionIcon, Tooltip, useMantineColorScheme } from '@mantine/core';
import { Sidebar } from './Sidebar';
import { ChatArea } from '../chat/ChatArea';
import { CollapsibleSidebar } from '../common/CollapsibleSidebar';
import { useChat } from '../../context/ChatContext';
import { HiOutlineChatBubbleLeftRight, HiOutlinePlus } from 'react-icons/hi2';

export function AppShell() {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const { state, createConversation } = useChat();

  return (
    <Box
      style={{
        display: 'flex',
        height: '100%',
        overflow: 'hidden',
        position: 'relative',
      }}
    >
      <CollapsibleSidebar
        icon={HiOutlineChatBubbleLeftRight}
        title="Conversations"
        headerRight={
          <>
            <Badge size="xs" variant="light" color={isDark ? 'cyan' : 'teal'}>
              {state.conversations.length}
            </Badge>
            <Tooltip label="New chat" withArrow>
              <ActionIcon
                size="sm"
                variant="subtle"
                color={isDark ? 'cyan' : 'teal'}
                onClick={createConversation}
              >
                <HiOutlinePlus size={14} />
              </ActionIcon>
            </Tooltip>
          </>
        }
      >
        <Sidebar />
      </CollapsibleSidebar>

      {/* Main content */}
      <Box style={{ flex: 1, display: 'flex', flexDirection: 'column', position: 'relative' }}>
        <ChatArea />
      </Box>
    </Box>
  );
}
