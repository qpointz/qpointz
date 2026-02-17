import { Box } from "@mantine/core";
import { ReactNode } from "react";

interface AppShellProps {
  header: ReactNode;
  children: ReactNode;
}

export function AppShell({ header, children }: AppShellProps) {
  return (
    <Box
      style={{
        display: "flex",
        flexDirection: "column",
        height: "100vh",
        overflow: "hidden",
        backgroundColor: "var(--mantine-color-body)"
      }}
    >
      {header}
      <Box
        p="md"
        style={{
          flex: 1,
          overflow: "auto"
        }}
      >
        {children}
      </Box>
    </Box>
  );
}
