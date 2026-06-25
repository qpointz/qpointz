import { describe, it, expect } from 'vitest';
import { groupMessageArtifacts } from '../artifactGroups';

describe('groupMessageArtifacts', () => {
  it('should emit one facet-proposal group per artefact', () => {
    const groups = groupMessageArtifacts([
      {
        kind: 'facet-proposal',
        facetTypeKey: 'descriptive',
        metadataEntityId: 'sales.customers',
        payload: { summary: 'VIP' },
      },
      {
        kind: 'facet-proposal',
        facetTypeKey: 'descriptive',
        metadataEntityId: 'sales.orders',
        payload: { summary: 'Orders' },
      },
    ]);

    expect(groups).toHaveLength(2);
    expect(groups.every((g) => g.kind === 'facet-proposal')).toBe(true);
  });
});
