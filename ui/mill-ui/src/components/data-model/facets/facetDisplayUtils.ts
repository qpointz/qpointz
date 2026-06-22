import type { FacetTypeManifest } from '../../../types/facetTypes';

/**
 * Title for a facet card header when registry metadata is still loading or missing for this type key.
 */
export function facetBoxBaseTitle(
  facetType: string,
  titleByKey: Record<string, string>,
  descriptor: FacetTypeManifest | null,
): string {
  const fromRegistry = titleByKey[facetType]?.trim();
  if (fromRegistry) return fromRegistry;
  const payloadTitle = descriptor?.payload?.title?.trim();
  if (payloadTitle) return payloadTitle;
  const slug = facetType.replace('urn:mill/metadata/facet-type:', '');
  if (!slug) return facetType;
  return slug.charAt(0).toUpperCase() + slug.slice(1).toLowerCase();
}

/** Condensed chat tab label: {@code Facet:Descriptive}, {@code Facet:Structural}, etc. */
export function facetCondensedTabLabel(
  facetTypeKey: string,
  titleByKey: Record<string, string>,
  descriptor: FacetTypeManifest | null,
): string {
  return `Facet:${facetBoxBaseTitle(facetTypeKey, titleByKey, descriptor)}`;
}
