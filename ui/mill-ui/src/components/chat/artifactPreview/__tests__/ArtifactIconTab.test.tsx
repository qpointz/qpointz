import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MantineProvider, Tabs } from '@mantine/core';
import { HiOutlineTableCells } from 'react-icons/hi2';
import { ArtifactIconTab } from '../ArtifactIconTab';
import { useArtifactTabsStyles } from '../artifactToolbar';
import { ArtifactToolbarIcon } from '../ArtifactToolbarIcon';

function TabsHarness() {
  const { styles, classNames } = useArtifactTabsStyles();
  return (
    <Tabs value="data" styles={styles} classNames={classNames}>
      <Tabs.List>
        <ArtifactIconTab
          value="data"
          label="Data"
          icon={<ArtifactToolbarIcon icon={HiOutlineTableCells} />}
        />
      </Tabs.List>
      <Tabs.Panel value="data">panel</Tabs.Panel>
    </Tabs>
  );
}

describe('ArtifactIconTab', () => {
  it('should render tab with aria-label for accessibility', () => {
    render(
      <MantineProvider>
        <TabsHarness />
      </MantineProvider>,
    );

    expect(screen.getByRole('tab', { name: 'Data' })).toBeInTheDocument();
  });
});
