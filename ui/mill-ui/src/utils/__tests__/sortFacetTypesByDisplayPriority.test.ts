import { describe, expect, it } from 'vitest';
import { facetTypeArrivalOrderFromRegistry } from '../../config/facetTypeDisplayPriority';
import type { FacetTypeManifest } from '../../types/facetTypes';
import { sortFacetTypesByDisplayPriority } from '../sortFacetTypesByDisplayPriority';

describe('sortFacetTypesByDisplayPriority', () => {
  it('should place priority keys first in list order, then preserve arrival for the rest', () => {
    const priority = ['c', 'a'];
    const arrival = ['z', 'a', 'b', 'c'];
    expect(sortFacetTypesByDisplayPriority(arrival, priority)).toEqual(['c', 'a', 'z', 'b']);
  });

  it('should ignore priority entries not in arrival', () => {
    const priority = ['x', 'a'];
    const arrival = ['b', 'a'];
    expect(sortFacetTypesByDisplayPriority(arrival, priority)).toEqual(['a', 'b']);
  });

  it('should preserve arrival when priority is empty', () => {
    expect(sortFacetTypesByDisplayPriority(['b', 'a'], [])).toEqual(['b', 'a']);
  });
});

describe('facetTypeArrivalOrderFromRegistry', () => {
  it('should follow registry order then sort leftovers', () => {
    const keys = ['urn:z', 'urn:mill/metadata/facet-type:descriptive', 'urn:a'];
    const registry = [
      { typeKey: 'urn:a' },
      { typeKey: 'urn:mill/metadata/facet-type:descriptive' },
      { typeKey: 'urn:z' },
    ] as FacetTypeManifest[];
    expect(facetTypeArrivalOrderFromRegistry(keys, registry)).toEqual([
      'urn:a',
      'urn:mill/metadata/facet-type:descriptive',
      'urn:z',
    ]);
  });

  it('should append keys missing from registry lexicographically', () => {
    const keys = ['urn:only-local', 'urn:a'];
    const registry = [{ typeKey: 'urn:a' }] as FacetTypeManifest[];
    expect(facetTypeArrivalOrderFromRegistry(keys, registry)).toEqual(['urn:a', 'urn:only-local']);
  });
});
