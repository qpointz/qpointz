import { Paper, type PaperProps } from '@mantine/core';
import type { ReactNode } from 'react';
import classes from './ChatArtifactCard.module.css';

interface ChatArtifactCardProps extends PaperProps {
  children: ReactNode;
}

/** Shared chat-native shell for condensed and expand artefact views. */
export function ChatArtifactCard({ children, style, className, ...paperProps }: ChatArtifactCardProps) {
  return (
    <Paper
      p="sm"
      radius="md"
      {...paperProps}
      className={[classes.root, className].filter(Boolean).join(' ')}
      style={style}
    >
      {children}
    </Paper>
  );
}

/**
 * Wraps {@code Tabs.List} for inline placement beside {@link ChatArtifactActionBar}.
 */
export function ArtifactTabsRail({ children }: { children: ReactNode }) {
  return <div className={classes.tabsRail}>{children}</div>;
}
