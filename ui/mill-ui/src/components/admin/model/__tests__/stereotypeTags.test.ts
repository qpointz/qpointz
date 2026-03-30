import { describe, expect, it } from 'vitest';
import { stereotypeTagsFromWire, stereotypeWireFromTags } from '../../../../utils/facetStereotype';

describe('stereotypeTags', () => {
  it('stereotypeTagsFromWire splits comma string', () => {
    expect(stereotypeTagsFromWire(' a , b , ')).toEqual(['a', 'b']);
  });

  it('stereotypeTagsFromWire accepts array', () => {
    expect(stereotypeTagsFromWire([' x ', 'y'])).toEqual(['x', 'y']);
  });

  it('stereotypeWireFromTags joins for non-array schema', () => {
    expect(stereotypeWireFromTags(['a', 'b'], 'STRING')).toBe('a,b');
    expect(stereotypeWireFromTags([], 'STRING')).toBeUndefined();
  });

  it('stereotypeWireFromTags keeps array for ARRAY schema', () => {
    expect(stereotypeWireFromTags(['a', 'b'], 'ARRAY')).toEqual(['a', 'b']);
  });
});
