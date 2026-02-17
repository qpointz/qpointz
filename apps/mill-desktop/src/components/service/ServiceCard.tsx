import {
  ActionIcon,
  Anchor,
  Badge,
  Box,
  Button,
  Group,
  List,
  Paper,
  Stack,
  Text,
  ThemeIcon,
  Tooltip,
  useMantineColorScheme
} from "@mantine/core";
import { ServiceViewModel } from "../../../shared/service";
import { StatusLamp } from "./StatusLamp";
import {
  HiOutlineBeaker,
  HiOutlineCircleStack,
  HiOutlineCodeBracket,
  HiOutlineCommandLine,
  HiOutlineCube,
  HiOutlineDocumentText,
  HiOutlineLink,
  HiOutlinePlay,
  HiOutlineSquares2X2,
  HiOutlineStop,
  HiOutlineTrash,
  HiOutlineArrowPath
} from "react-icons/hi2";

interface ServiceCardProps {
  service: ServiceViewModel;
  onStart: (serviceId: string) => Promise<void>;
  onStop: (serviceId: string) => Promise<void>;
  onOpenLink: (serviceId: string, url: string) => Promise<void>;
  onOpenLog: (serviceId: string) => Promise<void>;
  onDelete: (service: ServiceViewModel) => Promise<void>;
  onClean: (service: ServiceViewModel) => Promise<void>;
  busy: boolean;
}

export function ServiceCard({ service, onStart, onStop, onOpenLink, onOpenLog, onDelete, onClean, busy }: ServiceCardProps) {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === "dark";

  const prereqFailed = service.prerequisitesOk === false;
  const canStart = (service.status === "stopped" || service.status === "error") && !prereqFailed;
  const canStop = service.status === "running" || service.status === "starting";
  const typeIcon = resolveTypeIcon(service.serviceType);
  const typeLabel = service.serviceType ?? "command";
  const cardOpacity = prereqFailed ? 0.45 : 1;

  return (
    <Paper withBorder p="md" shadow={prereqFailed ? "none" : "xs"} style={{ height: "100%", opacity: cardOpacity }}>
      <Stack gap="sm" h="100%">
        <Group justify="space-between" align="flex-start" wrap="nowrap">
          <Group gap="sm" align="flex-start" wrap="nowrap">
            <img
              src={service.logoPath}
              alt={`${service.name} logo`}
              style={{
                width: 36, height: 36, borderRadius: 8, objectFit: "contain",
                filter: prereqFailed ? "grayscale(100%)" : undefined
              }}
            />
            <Box>
              <Text fw={650} c={prereqFailed ? "dimmed" : (isDark ? "gray.1" : "gray.8")}>
                {service.name}
              </Text>
              <Group gap={6} mt={4}>
                <ThemeIcon size={16} radius="xl" variant="light" color="gray">
                  {typeIcon}
                </ThemeIcon>
                <Text size="xs" c="dimmed">
                  {typeLabel}
                </Text>
              </Group>
              <Text size="sm" c="dimmed" lineClamp={2}>
                {service.description}
              </Text>
            </Box>
          </Group>
          <StatusLamp status={service.status} />
        </Group>

        <Group gap={8}>
          <Badge variant="light" color={service.pid ? "teal" : "gray"}>
            PID: {service.pid ?? "-"}
          </Badge>
          <Badge variant="outline" color={service.lastError ? "red" : "gray"}>
            {service.lastError ? "Error" : "Healthy"}
          </Badge>
          {prereqFailed ? (
            <Tooltip label={service.prerequisitesMessage ?? "Prerequisites not met"} withArrow multiline maw={340}>
              <Badge variant="outline" color="orange">
                Prereq missing
              </Badge>
            </Tooltip>
          ) : (
            <Badge variant="outline" color="teal">
              Prereq ok
            </Badge>
          )}
        </Group>

        <Box>
          <Text size="sm" fw={600} mb={6}>
            Links
          </Text>
          <List spacing={4} size="sm" icon={<HiOutlineLink size={12} />}>
            <List.Item>
              <Anchor
                component="button"
                type="button"
                onClick={() => void onOpenLink(service.id, stackDocsUrl(service.serviceType))}
                size="sm"
              >
                Stack docs
              </Anchor>
            </List.Item>
            {service.readme && (
              <List.Item>
                <Anchor
                  component="button"
                  type="button"
                  onClick={() => void onOpenLink(service.id, service.readme!)}
                  size="sm"
                >
                  Readme
                </Anchor>
              </List.Item>
            )}
            {service.links.map((link) => (
              <List.Item key={`${service.id}-${link.url}`}>
                <Anchor
                  component="button"
                  type="button"
                  onClick={() => void onOpenLink(service.id, link.url)}
                  size="sm"
                >
                  {link.label}
                </Anchor>
              </List.Item>
            ))}
            {service.links.length === 0 && (
              <List.Item>
                <Text size="xs" c="dimmed">
                  No links configured
                </Text>
              </List.Item>
            )}
          </List>
        </Box>

        <Box mt="auto">
          <Group justify="space-between" wrap="nowrap">
            <Group gap="xs">
              <Button
                size="compact-sm"
                leftSection={<HiOutlinePlay size={14} />}
                disabled={!canStart || busy}
                onClick={() => void onStart(service.id)}
              >
                Start
              </Button>
              <Button
                size="compact-sm"
                variant="light"
                color={isDark ? "cyan" : "teal"}
                leftSection={<HiOutlineStop size={14} />}
                disabled={!canStop || busy}
                onClick={() => void onStop(service.id)}
              >
                Stop
              </Button>
            </Group>
            <Group gap={4}>
              <Tooltip label="Open service log" withArrow>
                <ActionIcon variant="subtle" onClick={() => void onOpenLog(service.id)} disabled={busy}>
                  <HiOutlineDocumentText size={16} />
                </ActionIcon>
              </Tooltip>
              {service.lifecycle?.clean && (
                <Tooltip label="Run clean command" withArrow>
                  <ActionIcon variant="subtle" color="orange" onClick={() => void onClean(service)} disabled={busy}>
                    <HiOutlineArrowPath size={16} />
                  </ActionIcon>
                </Tooltip>
              )}
              <Tooltip label="Delete service" withArrow>
                <ActionIcon
                  variant="subtle"
                  color="red"
                  onClick={() => void onDelete(service)}
                  disabled={busy}
                >
                  <HiOutlineTrash size={16} />
                </ActionIcon>
              </Tooltip>
            </Group>
          </Group>
          {service.lastError ? <Text size="xs" c="red" mt={8}>{service.lastError}</Text> : null}
          {!service.lastError && service.prerequisitesOk === false && service.prerequisitesMessage ? (
            <Text size="xs" c="orange.7" mt={8}>
              {service.prerequisitesMessage}
            </Text>
          ) : null}
        </Box>
      </Stack>
    </Paper>
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
