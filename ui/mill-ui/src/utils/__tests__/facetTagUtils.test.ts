import { describe, expect, it } from 'vitest';
import {
  allTagsInFacetPayload,
  distinctFacetTagsOnEntity,
  facetUnitMatchesTagFilter,
  tagsFromPayload,
  tagsForRenderUnit,
} from '../facetTagUtils';

describe('facetTagUtils', () => {
  it('should read tags from descriptive payload', () => {
    expect(tagsFromPayload({ tags: ['core', 'customer'] })).toEqual(['core', 'customer']);
  });

  it('should collect distinct tags from entity facets', () => {
    const facets = {
      byType: {
        'urn:mill/metadata/facet-type:descriptive': { tags: ['core', 'customer'] },
        'urn:mill/metadata/facet-type:concept': { tags: ['customer', 'kpi'] },
      },
    };
    expect(distinctFacetTagsOnEntity(facets, facets.byType)).toEqual(['core', 'customer', 'kpi']);
  });

  it('should collect tags from concept facet concepts[] entries', () => {
    const conceptPayload = {
      conceptRef: 'urn:mill/model/concept:vip-passengers',
      concepts: [
        {
          name: 'VIP Passengers',
          tags: ['passenger', 'premium', 'travel'],
        },
      ],
    };
    const facets = {
      resolvedRows: [
        {
          uid: 'c1',
          facetTypeUrn: 'urn:mill/metadata/facet-type:concept',
          scopeUrn: 'urn:mill/metadata/scope:global',
          origin: 'CAPTURED' as const,
          originId: 'o1',
          payload: conceptPayload,
        },
      ],
    };
    expect(distinctFacetTagsOnEntity(facets, {})).toEqual(['passenger', 'premium', 'travel']);
    expect(
      tagsForRenderUnit(
        { kind: 'single', facetType: 'urn:mill/metadata/facet-type:concept' },
        { 'urn:mill/metadata/facet-type:concept': conceptPayload },
      ),
    ).toEqual(['passenger', 'premium', 'travel']);
  });

  it('should match facets without tags regardless of filter', () => {
    expect(facetUnitMatchesTagFilter([], new Set(['core']))).toBe(true);
  });

  it('should filter tagged facets by selected tags', () => {
    expect(facetUnitMatchesTagFilter(['core'], new Set(['core']))).toBe(true);
    expect(facetUnitMatchesTagFilter(['core'], new Set(['kpi']))).toBe(false);
  });

  it('should read tags from multiple facet rows', () => {
    const payload = [{ tags: ['a'] }, { tags: ['b'] }];
    expect(allTagsInFacetPayload(payload)).toEqual(['a', 'b']);
    expect(
      tagsForRenderUnit(
        { kind: 'multipleRow', facetType: 'urn:mill/metadata/facet-type:relation', index: 1, total: 2 },
        { 'urn:mill/metadata/facet-type:relation': payload },
      ),
    ).toEqual(['b']);
  });
});
