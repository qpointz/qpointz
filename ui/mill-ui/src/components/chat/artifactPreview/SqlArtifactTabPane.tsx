import { Box } from '@mantine/core';
import type { ReactNode } from 'react';
import { CONDENSED_ARTIFACT_CONTENT_HEIGHT } from './artifactContentLayout';

interface SqlArtifactTabPaneProps {
  variant: 'condensed' | 'expanded';
  children: ReactNode;
}

/** Stable tab body — condensed uses a fixed height; expanded fills remaining card space. */
export function SqlArtifactTabPane({ variant, children }: SqlArtifactTabPaneProps) {
  const condensed = variant === 'condensed';

  return (
    <Box
      style={{
        ...(condensed
          ? {
              height: CONDENSED_ARTIFACT_CONTENT_HEIGHT,
              minHeight: CONDENSED_ARTIFACT_CONTENT_HEIGHT,
            }
          : {
              flex: 1,
              minHeight: 0,
            }),
        display: 'flex',
        flexDirection: 'column',
        overflow: 'hidden',
      }}
    >
      {children}
    </Box>
  );
}
