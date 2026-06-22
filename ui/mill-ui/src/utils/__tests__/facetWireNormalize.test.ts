import { describe, expect, it } from 'vitest';
import { facetTypeKeyFromCaptureType, parseFacetProposalArtifact } from '../facetWireNormalize';

describe('facetWireNormalize', () => {
  it('should map schema capture shape to facet-proposal', () => {
    expect(
      parseFacetProposalArtifact({
        captureType: 'description',
        targetEntityId: 'retail.orders',
        serializedPayload: { summary: 'Orders table' },
      }),
    ).toEqual({
      kind: 'facet-proposal',
      facetTypeKey: 'descriptive',
      metadataEntityId: 'retail.orders',
      payload: { summary: 'Orders table' },
    });
  });

  it('should pass through facet-proposal wire shape', () => {
    expect(
      parseFacetProposalArtifact({
        facetTypeKey: 'relation',
        metadataEntityId: 'sales.orders',
        payload: { join: 'customer_id' },
      }),
    ).toEqual({
      kind: 'facet-proposal',
      facetTypeKey: 'relation',
      metadataEntityId: 'sales.orders',
      payload: { join: 'customer_id' },
    });
  });

  it('should map capture subtypes to facet type keys', () => {
    expect(facetTypeKeyFromCaptureType('description')).toBe('descriptive');
    expect(facetTypeKeyFromCaptureType('relation')).toBe('relation');
  });
});
