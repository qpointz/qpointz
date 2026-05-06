import { Box, Badge, ActionIcon, Tooltip, useMantineColorScheme, Select } from '@mantine/core';
import { useNavigate } from 'react-router';
import { Sidebar } from './Sidebar';
import { ChatArea } from '../chat/ChatArea';
import { ExplorerSplitLayout } from './ExplorerSplitLayout';
import { useChat } from '../../context/ChatContext';
import { useFeatureFlags } from '../../features/FeatureFlagContext';
import { HiOutlineChatBubbleLeftRight, HiOutlinePlus } from 'react-icons/hi2';

export function AppShell() {
  const navigate = useNavigate();
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const flags = useFeatureFlags();
  const {
    state,
    createConversation,
    initialized,
    agentProfiles,
    agentProfilesLoading,
    selectedAgentProfileId,
    setSelectedAgentProfileId,
  } = useChat();

  const profileData = agentProfiles.map((p) => ({ value: p.id, label: p.id }));

  return (
    <ExplorerSplitLayout
      icon={HiOutlineChatBubbleLeftRight}
      title="Conversations"
      sidebarHeaderRight={
        <>
          <Badge size="xs" variant="light" color={isDark ? 'cyan' : 'teal'}>
            {state.conversations.length}
          </Badge>
          {flags.chatAgentPicker && (
            <Select
              size="xs"
              w={150}
              placeholder="Agent profile"
              data={profileData}
              value={selectedAgentProfileId}
              onChange={(v) => setSelectedAgentProfileId(v)}
              clearable
              disabled={!initialized || agentProfilesLoading || profileData.length === 0}
            />
          )}
          <Tooltip label="New chat" withArrow>
            <ActionIcon
              size="sm"
              variant="subtle"
              color={isDark ? 'cyan' : 'teal'}
              onClick={() =>
                void createConversation().then((id) => {
                  if (id) {
                    navigate(`/chat/${id}`, { replace: true });
                  }
                })}
              disabled={!initialized}
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
