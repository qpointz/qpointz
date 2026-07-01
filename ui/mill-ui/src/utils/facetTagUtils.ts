import {
  multipleFacetItemValues,
  type FacetRenderUnit,
} from '../components/data-model/facetPayloadUtils';
import type { EntityFacets, FacetResolvedRow } from '../types/schema';

/** One UI card unit when rendering {@link EntityFacets.resolvedRows}. */
export type ResolvedFacetTagUnit =
  | { kind: 'multipleEmpty'; facetType: string }
  | { kind: 'inferred'; facetType: string; row: FacetResolvedRow }
  | { kind: 'capturedSingle'; facetType: string; row: FacetResolvedRow }
  | { kind: 'capturedMultiple'; facetType: string; row: FacetResolvedRow; capturedIndex: number };

/**
 * Reads `tags` from a facet payload object (descriptive, concept, etc.).
 *
 * @param payload facet instance JSON
 * @returns normalized tag strings
 */
export function tagsFromPayload(payload: unknown): string[] {
  if (payload == null || typeof payload !== 'object' || Array.isArray(payload)) {
    return [];
  }
  const tags = (payload as { tags?: unknown }).tags;
  if (!Array.isArray(tags)) {
    return [];
  }
  return tags.map((t) => String(t).trim()).filter((t) => t.length > 0);
}

/**
 * Collects tag strings from one facet payload object, including concept entries under `concepts[]`.
 *
 * @param payload facet instance JSON
 * @returns distinct tag strings on the payload and nested concept entries
 */
export function tagsFromFacetPayloadObject(payload: unknown): string[] {
  const set = new Set<string>();
  for (const t of tagsFromPayload(payload)) {
    set.add(t);
  }
  if (payload != null && typeof payload === 'object' && !Array.isArray(payload)) {
    const concepts = (payload as { concepts?: unknown }).concepts;
    if (Array.isArray(concepts)) {
      for (const entry of concepts) {
        for (const t of tagsFromPayload(entry)) {
          set.add(t);
        }
      }
    }
  }
  return [...set];
}

/**
 * Collects tags from a facet payload, including each MULTIPLE instance row.
 *
 * @param payload raw facet payload from `byType` or a resolved row
 * @returns distinct tag strings on the payload and its instances
 */
export function allTagsInFacetPayload(payload: unknown): string[] {
  const set = new Set<string>();
  for (const t of tagsFromFacetPayloadObject(payload)) {
    set.add(t);
  }
  for (const item of multipleFacetItemValues(payload)) {
    if (item === payload) {
      continue;
    }
    for (const t of tagsFromFacetPayloadObject(item)) {
      set.add(t);
    }
  }
  return [...set];
}

/**
 * Distinct tags across all facets on the current entity (for local UI filtering).
 *
 * @param facets entity facet bundle
 * @param byType legacy `byType` map when resolved rows are absent
 * @returns sorted unique tag names
 */
export function distinctFacetTagsOnEntity(
  facets: EntityFacets,
  byType: Record<string, unknown>,
): string[] {
  const set = new Set<string>();
  if (facets.resolvedRows !== undefined) {
    for (const row of facets.resolvedRows) {
      for (const t of allTagsInFacetPayload(row.payload)) {
        set.add(t);
      }
    }
  }
  for (const payload of Object.values(byType)) {
    for (const t of allTagsInFacetPayload(payload)) {
      set.add(t);
    }
  }
  return [...set].sort((a, b) => a.localeCompare(b));
}

/**
 * Tags on one resolved-constellation facet card.
 *
 * @param unit resolved facet render unit
 * @returns tag strings for that card
 */
export function tagsForResolvedUnit(unit: ResolvedFacetTagUnit): string[] {
  if (unit.kind === 'multipleEmpty') {
    return [];
  }
  return allTagsInFacetPayload(unit.row.payload);
}

/**
 * Tags on one legacy facet card.
 *
 * @param unit facet render unit
 * @param byType entity `byType` map
 * @returns tag strings for that card
 */
export function tagsForRenderUnit(
  unit: FacetRenderUnit,
  byType: Record<string, unknown>,
): string[] {
  const payload = byType[unit.facetType];
  if (unit.kind === 'multipleEmpty') {
    return [];
  }
  if (unit.kind === 'single') {
    return allTagsInFacetPayload(payload);
  }
  const items = multipleFacetItemValues(payload);
  return allTagsInFacetPayload(items[unit.index]);
}

/**
 * Whether a facet card should remain visible under the active tag filter.
 *
 * @param tagsOnUnit tags extracted for the card
 * @param selectedTags active filter selection
 * @returns true when the card should render
 */
export function facetUnitMatchesTagFilter(
  tagsOnUnit: string[],
  selectedTags: ReadonlySet<string>,
): boolean {
  if (tagsOnUnit.length === 0) {
    return true;
  }
  return tagsOnUnit.some((t) => selectedTags.has(t));
}
