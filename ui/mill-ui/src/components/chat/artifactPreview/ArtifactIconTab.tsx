import { Tabs, Tooltip } from '@mantine/core';
import type { ReactNode } from 'react';

interface ArtifactIconTabProps {
  /** Tab panel value. */
  value: string;
  /** Accessible label and tooltip text. */
  label: string;
  /** Icon shown in the tab control. */
  icon: ReactNode;
}

/** Icon-only artefact tab with a text tooltip — styled like ChatArtifactActionBar icons. */
export function ArtifactIconTab({ value, label, icon }: ArtifactIconTabProps) {
  return (
    <Tooltip label={label} withArrow>
      <Tabs.Tab value={value} aria-label={label}>
        {icon}
      </Tabs.Tab>
    </Tooltip>
  );
}

// Re-export for consumers that spread base tab layout styles.
export { buildArtifactTabsStyles, useArtifactTabsStyles } from './artifactToolbar';
