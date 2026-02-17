import {
  ActionIcon,
  Anchor,
  Badge,
  Box,
  Button,
  Group,
  Paper,
  Text,
  ThemeIcon,
  Tooltip,
  useMantineColorScheme
} from "@mantine/core";
import { ServiceViewModel } from "../../../shared/service";
import {
  HiOutlineBeaker,
  HiOutlineCircleStack,
  HiOutlineCodeBracket,
  HiOutlineCommandLine,
  HiOutlineCube,
  HiOutlineArrowPath,
  HiOutlineDocumentText,
  HiOutlinePlay,
  HiOutlineSquares2X2,
  HiOutlineTrash
} from "react-icons/hi2";

interface StoppedServiceRowProps {
  service: ServiceViewModel;
  onStart: (serviceId: string) => Promise<void>;
  onOpenLink: (serviceId: string, url: string) => Promise<void>;
  onOpenLog: (serviceId: string) => Promise<void>;
  onDelete: (service: ServiceViewModel) => Promise<void>;
  onClean: (service: ServiceViewModel) => Promise<void>;
  busy: boolean;
}

export function StoppedServiceRow({ service, onStart, onOpenLink, onOpenLog, onDelete, onClean, busy }: StoppedServiceRowProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === "dark";
  const canStart = service.prerequisitesOk !== false;
  const typeIcon = resolveTypeIcon(service.serviceType);
  const typeLabel = service.serviceType ?? "command";

  const disabled = !canStart;
  const rowOpacity = disabled ? 0.45 : 1;

  return (
    <Tooltip
      label={service.prerequisitesMessage ?? "Prerequisites not met"}
      withArrow
      multiline
      maw={340}
      disabled={!disabled}
    >
      <Paper
        withBorder
        px="md"
        py="xs"
        shadow={disabled ? "none" : "xs"}
        style={{ opacity: rowOpacity, pointerEvents: disabled ? "auto" : undefined }}
      >
        <Group justify="space-between" wrap="nowrap">
          <Group gap="sm" wrap="nowrap" style={{ minWidth: 0, flex: 1 }}>
            <img
              src={service.logoPath}
              alt={`${service.name} logo`}
              style={{
                width: 28, height: 28, borderRadius: 6, objectFit: "contain", flexShrink: 0,
                filter: disabled ? "grayscale(100%)" : undefined
              }}
            />
            <Box style={{ minWidth: 0 }}>
              <Group gap={6} wrap="nowrap">
                <Text fw={600} size="sm" c={disabled ? "dimmed" : (isDark ? "gray.1" : "gray.8")} truncate>
                  {service.name}
                </Text>
                <ThemeIcon size={14} radius="xl" variant="light" color="gray">
                  {typeIcon}
                </ThemeIcon>
                <Anchor
                  component="button"
                  type="button"
                  size="xs"
                  c="dimmed"
                  style={{ whiteSpace: "nowrap" }}
                  onClick={() => void onOpenLink(service.id, stackDocsUrl(service.serviceType))}
                >
                  {typeLabel}
                </Anchor>
              </Group>
              <Text size="xs" c="dimmed" lineClamp={1}>
                {service.description}
              </Text>
            </Box>
          </Group>
          <Group gap="xs" wrap="nowrap" style={{ flexShrink: 0 }}>
            {!disabled && service.lastError && (
              <Tooltip label={service.lastError} withArrow multiline maw={300}>
                <Badge variant="outline" color="red" size="xs">
                  Error
                </Badge>
              </Tooltip>
            )}
            {!disabled && service.readme && (
              <Anchor
                component="button"
                type="button"
                size="xs"
                style={{ whiteSpace: "nowrap" }}
                onClick={() => void onOpenLink(service.id, service.readme!)}
              >
                Readme
              </Anchor>
            )}
            {!disabled && service.status === "error" && (
              <Tooltip label="Open service log" withArrow>
                <ActionIcon
                  variant="subtle"
                  size="sm"
                  onClick={() => void onOpenLog(service.id)}
                  disabled={busy}
                >
                  <HiOutlineDocumentText size={14} />
                </ActionIcon>
              </Tooltip>
            )}
            {!disabled && (
              <Button
                size="compact-xs"
                leftSection={<HiOutlinePlay size={12} />}
                disabled={busy}
                onClick={() => void onStart(service.id)}
              >
                Start
              </Button>
            )}
            {!disabled && service.lifecycle?.clean && (
              <Tooltip label="Run clean command" withArrow>
                <ActionIcon
                  variant="subtle"
                  color="orange"
                  size="sm"
                  onClick={() => void onClean(service)}
                  disabled={busy}
                >
                  <HiOutlineArrowPath size={14} />
                </ActionIcon>
              </Tooltip>
            )}
            <ActionIcon
              variant="subtle"
              color="red"
              size="sm"
              onClick={() => void onDelete(service)}
              disabled={busy}
            >
              <HiOutlineTrash size={14} />
            </ActionIcon>
          </Group>
        </Group>
      </Paper>
    </Tooltip>
  );
}

function stackDocsUrl(serviceType?: ServiceViewModel["serviceType"]): string {
  return `https://docs.qpointz.io/latest/launcher/stacks/${serviceType ?? "command"}`;
}

function resolveTypeIcon(serviceType?: ServiceViewModel["serviceType"]) {
  switch (serviceType) {
    case "node":
      return <HiOutlineCodeBracket size={10} />;
    case "javaBoot":
      return <HiOutlineCircleStack size={10} />;
    case "docker":
      return <HiOutlineCube size={10} />;
    case "docker-compose":
      return <HiOutlineSquares2X2 size={10} />;
    case "python":
      return <HiOutlineBeaker size={10} />;
    case "command":
    default:
      return <HiOutlineCommandLine size={10} />;
  }
}
