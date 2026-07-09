import { ActionIcon, Box, Menu, Radio, Stack, Text, Tooltip } from '@mantine/core';
import { HiOutlineAdjustmentsHorizontal } from 'react-icons/hi2';
import type { InlineChatSession, AnalysisCopilotSettings } from '../../types/inlineChat';
import { getCopilotHeaderMenuConfig } from './copilotHeaderSettingsRegistry';

export interface InlineChatCopilotSettingsMenuProps {
  session: InlineChatSession;
  onSettingsChange: (settings: AnalysisCopilotSettings) => void;
}

/**
 * Context-specific copilot options in the inline chat drawer header.
 * Only renders when the active copilot exposes header settings (e.g. Analysis).
 */
export function InlineChatCopilotSettingsMenu({
  session,
  onSettingsChange,
}: InlineChatCopilotSettingsMenuProps) {
  const menuConfig = getCopilotHeaderMenuConfig(session.contextType);
  if (!menuConfig) {
    return null;
  }

  const { automationMode } = menuConfig;
  const currentMode = automationMode.getValue(session);

  const handleModeChange = (value: string) => {
    onSettingsChange(
      menuConfig.applyAutomationMode(
        session,
        value as typeof currentMode,
      ),
    );
  };

  return (
    <Menu position="bottom-end" width={280} withinPortal closeOnItemClick={false}>
      <Menu.Target>
        <Tooltip label={menuConfig.menuTitle} withArrow>
          <ActionIcon
            size="xs"
            variant="subtle"
            color="gray"
            aria-label={`${menuConfig.menuTitle} settings`}
          >
            <HiOutlineAdjustmentsHorizontal size={14} />
          </ActionIcon>
        </Tooltip>
      </Menu.Target>

      <Menu.Dropdown>
        <Menu.Label>{menuConfig.menuTitle}</Menu.Label>
        <Box px="sm" py={6} onClick={(event) => event.stopPropagation()}>
          <Text size="xs" fw={600} mb={6}>
            {automationMode.label}
          </Text>
          <Radio.Group value={currentMode} onChange={handleModeChange}>
            <Stack gap={8}>
              {automationMode.options.map((option) => (
                <Radio
                  key={option.value}
                  value={option.value}
                  size="xs"
                  label={option.label}
                  description={option.description}
                />
              ))}
            </Stack>
          </Radio.Group>
        </Box>
      </Menu.Dropdown>
    </Menu>
  );
}
