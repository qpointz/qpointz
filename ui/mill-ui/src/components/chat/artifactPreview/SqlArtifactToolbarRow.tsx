import { Box } from '@mantine/core';
import type { ReactNode } from 'react';

interface SqlArtifactToolbarRowProps {
  actions: ReactNode;
  pagination?: ReactNode;
  tabs: ReactNode;
}

/** Three-column artifact header: actions left, optional paging center, tabs right. */
export function SqlArtifactToolbarRow({ actions, pagination, tabs }: SqlArtifactToolbarRowProps) {
  return (
    <Box
      style={{
        display: 'grid',
        gridTemplateColumns: '1fr auto 1fr',
        alignItems: 'center',
        columnGap: 8,
        marginBottom: 4,
      }}
    >
      <Box style={{ justifySelf: 'start', minWidth: 0 }}>{actions}</Box>
      <Box style={{ justifySelf: 'center' }}>{pagination}</Box>
      <Box style={{ justifySelf: 'end', minWidth: 0 }}>{tabs}</Box>
    </Box>
  );
}
