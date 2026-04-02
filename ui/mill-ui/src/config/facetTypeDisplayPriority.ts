import type { FacetTypeManifest } from '../types/facetTypes';

/**
 * Preferred display order for facet type keys in the data-model Entity Details panel.
 * Types listed here are shown first (in this order). Any other type appears after them,
 * preserving the order received from the server / facet registry.
 *
 * Future: this list may be merged from a backend endpoint (same pattern as remote feature flags
 * overlaid on defaults in {@code FeatureFlagProvider}).
 */
export const DEFAULT_FACET_TYPE_DISPLAY_PRIORITY: readonly string[] = [
  'urn:mill/metadata/facet-type:schema',
  'urn:mill/metadata/facet-type:table',
  'urn:mill/metadata/facet-type:column',
  'urn:mill/metadata/facet-type:flow-schema',
  'urn:mill/metadata/facet-type:flow-table',
  'urn:mill/metadata/facet-type:flow-column',
  'urn:mill/metadata/facet-type:descriptive',
  'urn:mill/metadata/facet-type:links',
];

/**
 * Builds a stable "arrival" order for facet types that have payload in {@code byTypeKeys}:
 * first the order from the facet-type registry (server list), then any keys only present
 * in {@code byTypeKeys} (sorted lexicographically for deterministic UI).
 *
 * @param byTypeKeys facet type URNs that currently have stored payload for the entity
 * @param registryFacetTypes facet types returned by {@code facetTypeService.list} (server order)
 */
export function facetTypeArrivalOrderFromRegistry(
  byTypeKeys: readonly string[],
  registryFacetTypes: readonly FacetTypeManifest[],
): string[] {
  const keySet = new Set(byTypeKeys);
  const seen = new Set<string>();
  const out: string[] = [];
  for (const ft of registryFacetTypes) {
    if (keySet.has(ft.typeKey) && !seen.has(ft.typeKey)) {
      out.push(ft.typeKey);
      seen.add(ft.typeKey);
    }
  }
  const leftovers = byTypeKeys.filter((k) => !seen.has(k)).sort((a, b) => a.localeCompare(b));
  out.push(...leftovers);
  return out;
}
