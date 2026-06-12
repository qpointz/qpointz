import { Button, Select, Tooltip, useMantineColorScheme, Group } from '@mantine/core';
import { HiOutlineArrowPath, HiOutlineChatBubbleLeftRight } from 'react-icons/hi2';
import type { AgentProfileResponseWire } from '../../types/chat';
import { ContentPaneHeader } from '../layout/ContentPaneHeader';

interface ChatToolbarProps {
  title: string;
  profileId?: string;
  agentProfiles: AgentProfileResponseWire[];
  profileChangeDisabled?: boolean;
  onProfileChange?: (profileId: string) => void;
  sqlQueryCount: number;
  runAllDisabled: boolean;
  runAllLoading: boolean;
  onRunAllQueries: () => void;
}

function formatProfileLabel(profileId: string): string {
  return profileId
    .split(/[-_]/)
    .filter(Boolean)
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ');
}

export function ChatToolbar({
  title,
  profileId,
  agentProfiles,
  profileChangeDisabled = false,
  onProfileChange,
  sqlQueryCount,
  runAllDisabled,
  runAllLoading,
  onRunAllQueries,
}: ChatToolbarProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  const accentColor = isDark ? 'cyan' : 'teal';
  const profileOptions = agentProfiles.map((profile) => ({
    value: profile.id,
    label: formatProfileLabel(profile.id),
  }));
  const resolvedProfileId = profileId ?? profileOptions[0]?.value;
  const canChangeProfile = profileOptions.length >= 2 && onProfileChange != null;
  const showProfileControl = profileOptions.length > 0 && resolvedProfileId != null;

  return (
    <ContentPaneHeader
      icon={HiOutlineChatBubbleLeftRight}
      title={title}
      actions={
        <Group gap="xs" wrap="nowrap">
          {showProfileControl ? (
            <Select
              size="xs"
              w={180}
              data={profileOptions}
              value={resolvedProfileId}
              disabled={profileChangeDisabled || !canChangeProfile}
              onChange={(value) => {
                if (value && value !== resolvedProfileId && onProfileChange) {
                  onProfileChange(value);
                }
              }}
              comboboxProps={{ withinPortal: true }}
              aria-label="Agent profile"
              styles={{ input: { flexShrink: 0 } }}
            />
          ) : null}
          <Tooltip
            label={
              sqlQueryCount > 0
                ? `Re-run ${sqlQueryCount} SQL ${sqlQueryCount === 1 ? 'query' : 'queries'} from newest to oldest`
                : 'No SQL queries in this conversation'
            }
            withArrow
          >
            <Button
              size="xs"
              variant="light"
              color={accentColor}
              leftSection={<HiOutlineArrowPath size={14} />}
              loading={runAllLoading}
              disabled={runAllDisabled}
              onClick={onRunAllQueries}
            >
              Run all
              {sqlQueryCount > 0 ? ` (${sqlQueryCount})` : ''}
            </Button>
          </Tooltip>
        </Group>
      }
    />
  );
}
