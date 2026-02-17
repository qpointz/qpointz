import { Group, Text, ThemeIcon } from "@mantine/core";
import { ServiceStatus } from "../../../shared/service";
import { HiMiniExclamationTriangle } from "react-icons/hi2";

function getLampColor(status: ServiceStatus): string {
  if (status === "running") {
    return "green";
  }
  if (status === "error") {
    return "red";
  }
  if (status === "starting" || status === "stopping") {
    return "yellow";
  }
  return "gray";
}

export function StatusLamp({ status }: { status: ServiceStatus }) {
  const color = getLampColor(status);
  return (
    <Group gap={6} wrap="nowrap">
      <ThemeIcon size={14} radius="xl" color={color} variant="filled">
        {status === "error" ? <HiMiniExclamationTriangle size={10} /> : null}
      </ThemeIcon>
      <Text size="xs" fw={600} tt="uppercase" c="dimmed">
        {status}
      </Text>
    </Group>
  );
}
