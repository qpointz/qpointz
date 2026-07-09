import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MantineProvider } from '@mantine/core';
import { InlineArtifactPillStrip } from '../InlineArtifactPillStrip';

describe('InlineArtifactPillStrip', () => {
  it('should render headline and type badge', () => {
    render(
      <MantineProvider>
        <InlineArtifactPillStrip
          ariaLabel="SQL proposal test"
          typeBadge={{ kind: 'sql-data-composite' }}
          headline="Revenue by segment"
          stripActions={<span data-testid="actions" />}
          popoverBody={<div>body</div>}
        />
      </MantineProvider>,
    );
    expect(screen.getByText('Revenue by segment')).toBeInTheDocument();
    expect(screen.getByText('SQL')).toBeInTheDocument();
  });
});
