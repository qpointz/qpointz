import { Paper, type PaperProps } from '@mantine/core';
import type { ReactNode } from 'react';

interface ChatArtifactCardProps extends PaperProps {
  children: ReactNode;
}

/** Shared chat-native shell for condensed and expand artefact views. */
export function ChatArtifactCard({ children, ...paperProps }: ChatArtifactCardProps) {
  return (
    <Paper withBorder p="sm" radius="md" style={{ maxWidth: '100%' }} {...paperProps}>
      {children}
    </Paper>
  );
}
