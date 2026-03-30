/**
 * TypeScript port of {@code io.qpointz.mill.UrnSlug} (core/mill-core). Keeps path segments free of raw {@code /}
 * so dev proxies never split entity or facet-type IDs.
 *
 * @see core/mill-core/src/main/java/io/qpointz/mill/UrnSlug.java
 */

const URN_COLON_PREFIX = 'urn:';

/** Mill metadata facet-type namespace (mode 2 — strip prefix for path segments). */
export const METADATA_FACET_TYPE_PREFIX = 'urn:mill/metadata/facet-type:' as const;

/**
 * Mode 1 — full slug: encodes any URN to a single path segment (no {@code /}).
 *
 * @param urn must start with {@code urn:} (Java is case-sensitive; normalize with {@link normalizeUrnPrefix} if needed)
 */
export function encodeUrnSlug(urn: string): string {
  if (!urn.startsWith(URN_COLON_PREFIX)) {
    throw new Error(`Not a URN: ${urn}`);
  }
  const stripped = urn.slice(URN_COLON_PREFIX.length);
  return stripped.replace(/-/g, '--').replace(/\//g, '-');
}

/**
 * Decodes a segment produced by {@link encodeUrnSlug}.
 */
export function decodeUrnSlug(slug: string): string {
  const SENTINEL = '\u0000';
  const withSentinel = slug.replace(/--/g, SENTINEL);
  const slashed = withSentinel.replace(/-/g, '/');
  const restored = slashed.split(SENTINEL).join('-');
  return URN_COLON_PREFIX + restored;
}

function normalizeUrnPrefix(urn: string): string {
  return urn.replace(/^urn:/i, URN_COLON_PREFIX);
}

/**
 * Path segment for {@code /api/v1/metadata/facets/{typeKey}} and entity facet routes {@code …/facets/{typeKey}}.
 * Uses **prefixed slug** (mode 2) for standard facet-type URNs; full slug for other {@code urn:} values; otherwise passes through as a local key.
 */
/**
 * Slugifies a display title into a facet-type local id (matches facet-type admin UI rules).
 */
export function slugifyFacetTypeTitle(title: string): string {
  return title
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9\s-]/g, '')
    .replace(/\s+/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '');
}

/**
 * Normalises a facet type key for JSON request bodies: values already starting with {@code urn:}
 * are sent as-is; bare slugs are prefixed with {@link METADATA_FACET_TYPE_PREFIX}.
 *
 * @param raw trimmed or empty local key or full facet-type URN
 * @throws Error if {@code raw} is blank
 */
export function normalizeFacetTypeKeyForApi(raw: string): string {
  const t = raw.trim();
  if (t.length === 0) {
    throw new Error('Facet type key is empty');
  }
  if (/^urn:/i.test(t)) {
    return t.replace(/^urn:/i, 'urn:');
  }
  return `${METADATA_FACET_TYPE_PREFIX}${t}`;
}

/**
 * Short label for facet-type URN lists: the local id after {@link METADATA_FACET_TYPE_PREFIX},
 * or the segment after the last ":" for other {@code urn:} values, or the string unchanged if not a URN.
 *
 * @param typeKey full facet-type URN or bare local key
 */
export function facetTypeLocalDisplayKey(typeKey: string | undefined | null): string {
  const k = (typeKey ?? '').trim();
  if (!k) {
    return '';
  }
  if (k.startsWith(METADATA_FACET_TYPE_PREFIX)) {
    return k.slice(METADATA_FACET_TYPE_PREFIX.length).trim() || k;
  }
  if (k.toLowerCase().startsWith(URN_COLON_PREFIX)) {
    const lastColon = k.lastIndexOf(':');
    if (lastColon >= 0 && lastColon < k.length - 1) {
      return k.slice(lastColon + 1).trim() || k;
    }
  }
  return k;
}

export function facetTypePathSegment(typeKey: string): string {
  const t = typeKey.trim();
  if (t.startsWith(METADATA_FACET_TYPE_PREFIX)) {
    const local = t.slice(METADATA_FACET_TYPE_PREFIX.length);
    // Mode 2 (prefixed slug); if the local id contains `/`, use full slug so proxies never decode `%2F` into extra segments.
    if (local.includes('/')) {
      return encodeURIComponent(encodeUrnSlug(normalizeUrnPrefix(t)));
    }
    return encodeURIComponent(local);
  }
  if (t.toLowerCase().startsWith('urn:')) {
    return encodeURIComponent(encodeUrnSlug(normalizeUrnPrefix(t)));
  }
  return encodeURIComponent(t);
}

/**
 * Path segment for {@code /api/v1/metadata/entities/{id}/…} (metadata entity URN only).
 */
export function metadataEntityPathSegment(entityUrn: string): string {
  const t = entityUrn.trim();
  if (t.length === 0) {
    throw new Error('metadata entity URN must not be blank');
  }
  if (!t.toLowerCase().startsWith('urn:mill/')) {
    throw new Error(`metadata facet API expects a full urn:mill/… entity URN, got: ${t}`);
  }
  return encodeURIComponent(encodeUrnSlug(normalizeUrnPrefix(t)));
}
