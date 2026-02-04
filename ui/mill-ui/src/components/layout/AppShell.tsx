import { Box, ActionIcon, useMantineColorScheme, Tooltip } from '@mantine/core';
import { useDisclosure, useMediaQuery } from '@mantine/hooks';
import { HiOutlineBars3, HiOutlineXMark } from 'react-icons/hi2';
import { Sidebar } from './Sidebar';
import { ChatArea } from '../chat/ChatArea';
import { useChat } from '../../context/ChatContext';
import { useEffect } from 'react';

export function AppShell() {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const isMobile = useMediaQuery('(max-width: 768px)');
  const [sidebarOpened, { toggle: toggleSidebar, close: closeSidebar }] = useDisclosure(true);
  const { state, createConversation } = useChat();

  // Create initial conversation if none exists
  useEffect(() => {
    if (state.conversations.length === 0) {
      createConversation();
    }
  }, [state.conversations.length, createConversation]);

  // Close sidebar on mobile when clicking chat
  useEffect(() => {
    if (isMobile) {
      closeSidebar();
    }
  }, [isMobile, closeSidebar]);

  return (
    <Box
      style={{
        display: 'flex',
        height: '100%',
        overflow: 'hidden',
        position: 'relative',
      }}
    >
      {/* Mobile sidebar overlay */}
      {isMobile && sidebarOpened && (
        <Box
          onClick={closeSidebar}
          style={{
            position: 'fixed',
            inset: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.5)',
            zIndex: 100,
          }}
        />
      )}

      {/* Sidebar */}
      <Box
        style={{
          position: isMobile ? 'fixed' : 'relative',
          left: 0,
          top: 0,
          zIndex: isMobile ? 101 : 1,
          transform: isMobile && !sidebarOpened ? 'translateX(-100%)' : 'translateX(0)',
          transition: 'transform 0.3s ease',
        }}
      >
        <Sidebar opened={sidebarOpened || !isMobile} />
      </Box>

      {/* Main content */}
      <Box style={{ flex: 1, display: 'flex', flexDirection: 'column', position: 'relative' }}>
        {/* Mobile toggle button */}
        <Tooltip label={sidebarOpened ? 'Close sidebar' : 'Open sidebar'} withArrow position="right">
          <ActionIcon
            variant="subtle"
            color={isDark ? 'slate.4' : 'slate.6'}
            size="lg"
            onClick={toggleSidebar}
            style={{
              position: 'absolute',
              top: '12px',
              left: '12px',
              zIndex: 50,
            }}
          >
            {sidebarOpened && !isMobile ? (
              <HiOutlineXMark size={20} />
            ) : (
              <HiOutlineBars3 size={20} />
            )}
          </ActionIcon>
        </Tooltip>

        <ChatArea />
      </Box>
    </Box>
  );
}
