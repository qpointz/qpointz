import { describe, it, expect } from 'vitest';
import type { ChatMessageArtifact } from '../../types/chat';
import { mergeArtifactIdsFromServer } from '../artifactMerge';

describe('mergeArtifactIdsFromServer', () => {
  it('should attach artifactId to streamed concept facet-proposal', () => {
    const streamed: ChatMessageArtifact[] = [
      {
        kind: 'facet-proposal',
        facetTypeKey: 'concept',
        metadataEntityId: 'urn:mill/model/model:model-entity',
        catalogPath: 'model-entity',
        payload: { conceptRef: 'urn:mill/model/concept:vip-passengers' },
      },
    ];
    const server: ChatMessageArtifact[] = [
      {
        kind: 'facet-proposal',
        facetTypeKey: 'urn:mill/metadata/facet-type:concept',
        metadataEntityId: 'urn:mill/model/model:model-entity',
        catalogPath: 'model-entity',
        payload: { conceptRef: 'urn:mill/model/concept:vip-passengers' },
        artifactId: 'art-concept-1',
        status: 'active',
      },
    ];
    const merged = mergeArtifactIdsFromServer(streamed, server);
    const first = merged[0];
    expect(first?.kind).toBe('facet-proposal');
    if (first?.kind === 'facet-proposal') {
      expect(first.artifactId).toBe('art-concept-1');
      expect(first.status).toBe('active');
    }
  });

  it('should attach artifactId to streamed descriptive facet by entity match', () => {
    const streamed: ChatMessageArtifact[] = [
      {
        kind: 'facet-proposal',
        facetTypeKey: 'descriptive',
        metadataEntityId: 'urn:mill/model/table:sales.orders',
        catalogPath: 'sales.orders',
        payload: { description: 'Orders' },
      },
    ];
    const server: ChatMessageArtifact[] = [
      {
        kind: 'facet-proposal',
        facetTypeKey: 'descriptive',
        metadataEntityId: 'urn:mill/model/table:sales.orders',
        catalogPath: 'sales.orders',
        payload: { description: 'Orders' },
        artifactId: 'art-desc-1',
      },
    ];
    const merged = mergeArtifactIdsFromServer(streamed, server);
    const first = merged[0];
    expect(first?.kind).toBe('facet-proposal');
    if (first?.kind === 'facet-proposal') {
      expect(first.artifactId).toBe('art-desc-1');
    }
  });
});
