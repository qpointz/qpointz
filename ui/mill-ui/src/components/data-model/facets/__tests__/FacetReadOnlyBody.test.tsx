import { describe, expect, it } from 'vitest';
import type { ReactNode } from 'react';
import { render, screen } from '@testing-library/react';
import { MantineProvider } from '@mantine/core';
import { FacetReadOnlyBody } from '../FacetReadOnlyBody';
import type { FacetTypeManifest } from '../../../../types/facetTypes';

function renderNode(node: ReactNode) {
  return render(<MantineProvider>{node}</MantineProvider>);
}

const descriptor: FacetTypeManifest = {
  typeKey: 'urn:mill/metadata/facet-type:descriptive',
  title: 'Descriptive',
  payload: {
    type: 'OBJECT',
    fields: [
      { name: 'displayName', schema: { type: 'STRING' } },
    ],
  },
};

describe('FacetReadOnlyBody', () => {
  it('shows skeleton while facet manifests load instead of JSON fallback', () => {
    renderNode(
      <FacetReadOnlyBody
        facetTypeKey="urn:mill/metadata/facet-type:descriptive"
        payload={{ displayName: 'Passenger id' }}
        descriptor={null}
        manifestLoading
      />,
    );
    expect(screen.getByLabelText('Loading facet content')).toBeInTheDocument();
    expect(screen.queryByText(/Passenger id/)).not.toBeInTheDocument();
  });

  it('renders schema-driven fields when descriptor payload is available', () => {
    renderNode(
      <FacetReadOnlyBody
        facetTypeKey="urn:mill/metadata/facet-type:descriptive"
        payload={{ displayName: 'Passenger id' }}
        descriptor={descriptor}
      />,
    );
    expect(screen.getByText('displayName')).toBeInTheDocument();
    expect(screen.getByText('Passenger id')).toBeInTheDocument();
  });

  it('falls back to JSON when manifests finished loading without a descriptor', () => {
    renderNode(
      <FacetReadOnlyBody
        facetTypeKey="urn:mill/metadata/facet-type:custom"
        payload={{ foo: 'bar' }}
        descriptor={null}
        manifestLoading={false}
      />,
    );
    expect(screen.getByText(/"foo"/)).toBeInTheDocument();
  });
});
