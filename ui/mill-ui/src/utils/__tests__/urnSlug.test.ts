import { describe, it, expect } from 'vitest';
import {
  decodeUrnSlug,
  encodeUrnSlug,
  facetTypeLocalDisplayKey,
  facetTypePathSegment,
  metadataEntityPathSegment,
  METADATA_FACET_TYPE_PREFIX,
  normalizeFacetTypeKeyForApi,
  slugifyFacetTypeTitle,
} from '../urnSlug';

describe('urnSlug (Java UrnSlug parity)', () => {
  it('round-trips entity URNs', () => {
    const urn = 'urn:mill/model/attribute:skymill.cargo_clients.country_id';
    const slug = encodeUrnSlug(urn);
    expect(slug).not.toContain('/');
    expect(decodeUrnSlug(slug)).toBe(urn);
  });

  it('metadataEntityPathSegment is percent-encoded and slash-free before encoding', () => {
    const urn = 'urn:mill/model/table:sales.customers';
    const seg = metadataEntityPathSegment(urn);
    expect(decodeUrnSlug(decodeURIComponent(seg))).toBe(urn);
  });

  it('facetTypePathSegment uses prefixed slug for standard facet-type URNs', () => {
    expect(facetTypePathSegment(`${METADATA_FACET_TYPE_PREFIX}descriptive`)).toBe('descriptive');
    expect(facetTypePathSegment(`${METADATA_FACET_TYPE_PREFIX}value-mapping`)).toBe('value-mapping');
  });

  it('facetTypePathSegment uses full slug for other urn: values', () => {
    const urn = 'urn:mill/metadata/facet-type:custom/ns/name';
    const seg = facetTypePathSegment(urn);
    expect(seg).not.toContain('/');
    expect(decodeUrnSlug(decodeURIComponent(seg))).toBe(urn);
  });

  it('facetTypePathSegment encodes bare local keys', () => {
    expect(facetTypePathSegment('descriptive')).toBe('descriptive');
  });

  it('slugifyFacetTypeTitle matches admin facet-type rules', () => {
    expect(slugifyFacetTypeTitle('My Facet!')).toBe('my-facet');
    expect(slugifyFacetTypeTitle('  ')).toBe('');
  });

  it('normalizeFacetTypeKeyForApi prefixes bare keys and preserves URNs', () => {
    expect(normalizeFacetTypeKeyForApi('table')).toBe(`${METADATA_FACET_TYPE_PREFIX}table`);
    expect(normalizeFacetTypeKeyForApi('urn:mill/metadata/facet-type:x')).toBe('urn:mill/metadata/facet-type:x');
    expect(() => normalizeFacetTypeKeyForApi('   ')).toThrow(/empty/i);
  });

  it('facetTypeLocalDisplayKey strips standard facet-type prefix or last URN segment', () => {
    expect(facetTypeLocalDisplayKey(`${METADATA_FACET_TYPE_PREFIX}descriptive`)).toBe('descriptive');
    expect(facetTypeLocalDisplayKey(`${METADATA_FACET_TYPE_PREFIX}custom/ns/name`)).toBe('custom/ns/name');
    expect(facetTypeLocalDisplayKey('bare-key')).toBe('bare-key');
    expect(facetTypeLocalDisplayKey('urn:mill/metadata/facet-type:custom/ns/name')).toBe('custom/ns/name');
    expect(facetTypeLocalDisplayKey('')).toBe('');
  });
});
