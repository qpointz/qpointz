import { ActionIcon, Badge, Box, Button, Group, Menu, Text, Tooltip, useMantineColorScheme } from "@mantine/core";
import { useEffect, useState } from "react";
import {
  HiOutlineMoon,
  HiOutlinePlay,
  HiOutlineSun,
  HiOutlineStop,
  HiOutlineArrowPath,
  HiOutlineFolderOpen,
  HiOutlineCog6Tooth,
  HiOutlineArrowPathRoundedSquare,
  HiOutlineArrowDownTray,
  HiOutlineQuestionMarkCircle
} from "react-icons/hi2";
import { RecentWorkspace } from "../../../shared/service";

interface AppHeaderProps {
  runningCount: number;
  totalCount: number;
  onStartAll: () => Promise<void>;
  onStopAll: () => Promise<void>;
  onRefresh: () => Promise<void>;
  onChooseWorkspace: () => Promise<void>;
  onReloadWorkspace: () => Promise<void>;
  onImportWorkspaceServices: () => Promise<void>;
  onSelectRecentWorkspace: (directory: string) => Promise<void>;
  onOpenWorkspaceSettings: () => void;
  onOpenLink: (url: string) => void;
  workspaceDir: string;
  workspaceSlug: string;
  recentWorkspaces: RecentWorkspace[];
  busy: boolean;
  settingsActive: boolean;
}

export function AppHeader({
  runningCount,
  totalCount,
  onStartAll,
  onStopAll,
  onRefresh,
  onChooseWorkspace,
  onReloadWorkspace,
  onImportWorkspaceServices,
  onSelectRecentWorkspace,
  onOpenWorkspaceSettings,
  onOpenLink,
  workspaceDir,
  workspaceSlug,
  recentWorkspaces,
  busy,
  settingsActive
}: AppHeaderProps) {
  const { colorScheme, toggleColorScheme } = useMantineColorScheme();
  const isDark = colorScheme === "dark";
  const [logoUrl, setLogoUrl] = useState<string>("");

  useEffect(() => {
    window.controlPanel?.getAppLogoUrl().then(setLogoUrl).catch(() => {});
  }, []);

  return (
    <Box
      h={60}
      px="md"
      style={{
        borderBottom: "1px solid var(--mantine-color-default-border)",
        backgroundColor: "var(--mantine-color-body)",
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between"
      }}
    >
      <Group gap="sm">
        {logoUrl && <img src={logoUrl} alt="Mill Launcher logo" style={{ width: 30, height: 30 }} />}
        <Box>
          <Text fw={650} size="lg" c={isDark ? "gray.1" : "gray.8"}>
            Mill Launcher
          </Text>
          <Text size="xs" c="dimmed">
            Workspace: {workspaceDir}
          </Text>
          <Text size="xs" c="dimmed">
            Slug: {workspaceSlug}
          </Text>
        </Box>
      </Group>

      <Group gap="xs">
        <Badge size="lg" variant="light" color={runningCount > 0 ? (isDark ? "cyan" : "teal") : "gray"}>
          {runningCount}/{totalCount} running
        </Badge>
        <Button
          size="compact-sm"
          leftSection={<HiOutlinePlay size={15} />}
          onClick={() => void onStartAll()}
          disabled={busy || settingsActive}
        >
          Start All
        </Button>
        <Button
          size="compact-sm"
          variant="light"
          color={isDark ? "cyan" : "teal"}
          leftSection={<HiOutlineStop size={15} />}
          onClick={() => void onStopAll()}
          disabled={busy || settingsActive}
        >
          Stop All
        </Button>
        <Tooltip label="Refresh status" withArrow>
          <ActionIcon variant="subtle" onClick={() => void onRefresh()} disabled={busy || settingsActive}>
            <HiOutlineArrowPath size={18} />
          </ActionIcon>
        </Tooltip>
        <Menu shadow="md" width={230} withArrow position="bottom-end">
          <Menu.Target>
            <Button
              size="compact-sm"
              variant="subtle"
              leftSection={<HiOutlineFolderOpen size={15} />}
              disabled={busy || settingsActive}
            >
              Workspace
            </Button>
          </Menu.Target>
          <Menu.Dropdown>
            <Menu.Label>Workspace actions</Menu.Label>
            <Menu.Item leftSection={<HiOutlineFolderOpen size={14} />} onClick={() => void onChooseWorkspace()}>
              Choose workspace
            </Menu.Item>
            <Menu.Item
              leftSection={<HiOutlineArrowPathRoundedSquare size={14} />}
              onClick={() => void onReloadWorkspace()}
            >
              Reload workspace
            </Menu.Item>
            <Menu.Item
              leftSection={<HiOutlineArrowDownTray size={14} />}
              onClick={() => void onImportWorkspaceServices()}
            >
              Import services from URL
            </Menu.Item>
            <Menu.Divider />
            <Menu.Label>Recent workspaces</Menu.Label>
            {recentWorkspaces.length === 0 ? (
              <Menu.Item disabled>No recent workspaces</Menu.Item>
            ) : (
              recentWorkspaces.map((item) => (
                <Menu.Item
                  key={item.directory}
                  onClick={() => void onSelectRecentWorkspace(item.directory)}
                >
                  {item.settings.general?.workspaceSlug ?? "workspace"} - {item.directory}
                </Menu.Item>
              ))
            )}
          </Menu.Dropdown>
        </Menu>
        <Tooltip label={settingsActive ? "Currently editing settings" : "Open workspace settings"} withArrow>
          <Button
            size="compact-sm"
            variant="subtle"
            leftSection={<HiOutlineCog6Tooth size={15} />}
            onClick={onOpenWorkspaceSettings}
            disabled={busy || settingsActive}
          >
            Settings
          </Button>
        </Tooltip>
        <Tooltip label="Help & documentation" withArrow>
          <ActionIcon variant="subtle" onClick={() => onOpenLink("https://docs.qpointz.io/latest/launcher/")}>
            <HiOutlineQuestionMarkCircle size={18} />
          </ActionIcon>
        </Tooltip>
        <Tooltip label={isDark ? "Switch to light mode" : "Switch to dark mode"} withArrow>
          <ActionIcon variant="subtle" onClick={() => toggleColorScheme()}>
            {isDark ? <HiOutlineSun size={18} /> : <HiOutlineMoon size={18} />}
          </ActionIcon>
        </Tooltip>
      </Group>
    </Box>
  );
}
