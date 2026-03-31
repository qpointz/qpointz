import { Box, Badge, ActionIcon, Tooltip, useMantineColorScheme } from '@mantine/core';
import { Sidebar } from './Sidebar';
import { ChatArea } from '../chat/ChatArea';
import { ExplorerSplitLayout } from './ExplorerSplitLayout';
import { useChat } from '../../context/ChatContext';
import { HiOutlineChatBubbleLeftRight, HiOutlinePlus } from 'react-icons/hi2';

export function AppShell() {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const { state, createConversation } = useChat();

  return (
    <ExplorerSplitLayout
      icon={HiOutlineChatBubbleLeftRight}
      title="Conversations"
      sidebarHeaderRight={
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
      sidebarBody={<Sidebar />}
      main={
        <Box style={{ height: '100%', display: 'flex', flexDirection: 'column', position: 'relative' }}>
          <ChatArea />
        </Box>
      }
    />
  );
}
