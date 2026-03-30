import type { FacetPayloadSchema, FacetTypeManifest } from '../types/facetTypes';

function isRecord(v: unknown): v is Record<string, unknown> {
  return v != null && typeof v === 'object' && !Array.isArray(v);
}

/**
 * Normalizes facet-type catalog JSON from the metadata API into the UI's {@link FacetTypeManifest}
 * (internal keys `typeKey` / `payload`). Accepts current wire shape (`facetTypeUrn`, `title`, `contentSchema`)
 * and transitional keys (`typeKey`, `displayName`, `payload`).
 */
export function facetTypeManifestFromWire(raw: unknown): FacetTypeManifest {
  if (!isRecord(raw)) {
    throw new TypeError('Facet type manifest must be an object');
  }
  const rawKey =
    raw.facetTypeUrn ??
    raw.typeKey ??
    raw.typeRes ??
    raw.id ??
    raw.key;
  const typeKey = typeof rawKey === 'string' ? rawKey.trim() : String(rawKey ?? '').trim();
  if (!typeKey || typeKey === 'undefined' || typeKey === 'null') {
    throw new TypeError('Facet type manifest missing facetTypeUrn / typeKey');
  }
  const title = String(raw.title ?? raw.displayName ?? '').trim();
  const description = String(raw.description ?? '');
  const payload = (raw.contentSchema ?? raw.payload) as FacetPayloadSchema;
  if (!payload || typeof payload !== 'object') {
    throw new TypeError('Facet type manifest missing contentSchema / payload');
  }
  const applicableTo = raw.applicableTo;
  return {
    typeKey,
    title: title || typeKey,
    description,
    category: typeof raw.category === 'string' ? raw.category : undefined,
    enabled: Boolean(raw.enabled !== false),
    mandatory: Boolean(raw.mandatory),
    targetCardinality:
      String(raw.targetCardinality ?? 'SINGLE').toUpperCase() === 'MULTIPLE' ? 'MULTIPLE' : 'SINGLE',
    applicableTo: Array.isArray(applicableTo) ? applicableTo.map((v) => String(v)) : null,
    schemaVersion: raw.schemaVersion != null ? String(raw.schemaVersion) : null,
    payload,
  };
}

/**
 * Serializes the UI manifest to the API request body shape (`facetTypeUrn`, `title`, `contentSchema`).
 */
export function facetTypeManifestToWire(m: FacetTypeManifest): Record<string, unknown> {
  const out: Record<string, unknown> = {
    facetTypeUrn: m.typeKey,
    title: m.title,
    description: m.description,
    enabled: m.enabled,
    mandatory: m.mandatory,
    targetCardinality: m.targetCardinality ?? 'SINGLE',
    contentSchema: m.payload,
  };
  if (m.category != null && m.category !== '') {
    out.category = m.category;
  }
  if (m.applicableTo != null) {
    out.applicableTo = m.applicableTo;
  }
  if (m.schemaVersion != null && m.schemaVersion !== '') {
    out.schemaVersion = m.schemaVersion;
  }
  return out;
}
