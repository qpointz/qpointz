import { describe, expect, it } from 'vitest';
import {
  catalogPathFromModelEntityUrn,
  facetEntityCatalogPath,
  modelRouteFromCatalogPath,
} from '../metadataEntityDisplay';

describe('metadataEntityDisplay', () => {
  it('shouldDecodeModelEntityUrns_toCatalogPath', () => {
    expect(catalogPathFromModelEntityUrn('urn:mill/model/schema:Skymill')).toBe('skymill');
    expect(catalogPathFromModelEntityUrn('urn:mill/model/table:skymill.passenger')).toBe('skymill.passenger');
    expect(catalogPathFromModelEntityUrn('urn:mill/model/attribute:skymill.passenger.id')).toBe(
      'skymill.passenger.id',
    );
  });

  it('shouldPreferWireCatalogPath_overUrn', () => {
    expect(
      facetEntityCatalogPath('skymill.passenger.first_name', 'urn:mill/model/attribute:skymill.passenger.id'),
    ).toBe('skymill.passenger.first_name');
  });

  it('shouldBuildModelRoute_fromCatalogPath', () => {
    expect(modelRouteFromCatalogPath('skymill.passenger.id')).toBe('/model/skymill/passenger/id');
  });
});
