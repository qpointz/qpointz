import { describe, expect, it } from 'vitest';
import { facetTypeManifestFromWire, facetTypeManifestToWire } from '../facetTypeWire';

const samplePayload = {
  type: 'OBJECT' as const,
  title: 'Root',
  description: 'Root schema',
  fields: [],
  required: [] as string[],
};

describe('facetTypeWire', () => {
  it('maps API wire shape to UI manifest', () => {
    const m = facetTypeManifestFromWire({
      facetTypeUrn: 'urn:mill/metadata/facet-type:descriptive',
      title: 'Description',
      description: 'Human text',
      enabled: true,
      mandatory: true,
      targetCardinality: 'SINGLE',
      contentSchema: samplePayload,
    });
    expect(m.typeKey).toBe('urn:mill/metadata/facet-type:descriptive');
    expect(m.title).toBe('Description');
    expect(m.payload).toEqual(samplePayload);
  });

  it('accepts transitional keys (typeKey, displayName, payload)', () => {
    const m = facetTypeManifestFromWire({
      typeKey: 'urn:mill/metadata/facet-type:x',
      displayName: 'X',
      description: 'd',
      contentSchema: samplePayload,
    });
    expect(m.typeKey).toBe('urn:mill/metadata/facet-type:x');
    expect(m.title).toBe('X');
  });

  it('rejects missing or bogus type keys', () => {
    expect(() =>
      facetTypeManifestFromWire({
        title: 'Only title',
        contentSchema: samplePayload,
      })
    ).toThrow(/facetTypeUrn|typeKey/i);
    expect(() =>
      facetTypeManifestFromWire({
        facetTypeUrn: 'undefined',
        title: 'Bad',
        contentSchema: samplePayload,
      })
    ).toThrow(/facetTypeUrn|typeKey|null|undefined/i);
  });

  it('round-trips through toWire', () => {
    const m = facetTypeManifestFromWire({
      facetTypeUrn: 'urn:mill/metadata/facet-type:governance',
      title: 'Gov',
      description: 'G',
      category: 'general',
      enabled: false,
      mandatory: false,
      applicableTo: ['urn:mill/metadata/entity-type:table'],
      schemaVersion: '1.0',
      contentSchema: samplePayload,
    });
    const w = facetTypeManifestToWire(m);
    expect(w.facetTypeUrn).toBe(m.typeKey);
    expect(w.title).toBe('Gov');
    expect(w.contentSchema).toBe(m.payload);
    expect(facetTypeManifestFromWire(w)).toEqual(m);
  });
});
