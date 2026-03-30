/**
 * Pure helpers for MULTIPLE-cardinality facet payloads (normalise, captions, replace/append).
 * Used by `EntityDetails`; no React dependencies.
 */

/**
 * Normalizes a MULTIPLE-cardinality facet payload to a list of values for per-instance cards.
 * Supports:
 * - JSON **array** of instances (merged client shape after several `GET /facets` rows)
 * - Legacy `{ "relations": [...] }` envelope (uses `relations` array as-is, including empty = zero instances)
 * - **Single object** from one `FacetInstanceDto` row (JPA one row per instance): non-envelope objects,
 *   including `{}`, count as **one** logical instance. Otherwise a stored `{}` round-trips as “no instances”
 *   and the UI stuck on “Add entry” / empty state forever.
 *
 * @param payload raw facet payload from entity facets `byType` map
 * @returns list of item payloads (may be empty)
 */
export function multipleFacetItemValues(payload: unknown): unknown[] {
  if (payload == null) return [];
  let p: unknown = payload;
  /** Legacy: REST coercion wrapped JSON arrays as `{ "value": [ … ] }` (see server `FacetPayloadCoercion`). */
  if (typeof p === 'object' && p !== null && !Array.isArray(p)) {
    const rec = p as Record<string, unknown>;
    const keys = Object.keys(rec);
    if (keys.length === 1 && keys[0] === 'value' && Array.isArray(rec.value)) {
      p = rec.value;
    }
  }
  if (Array.isArray(p)) return p;
  if (typeof p === 'object' && !Array.isArray(p)) {
    const rec = p as Record<string, unknown>;
    const rel = rec.relations;
    if (Array.isArray(rel)) return rel;
    if (Object.keys(rec).length === 0) return [{}];
  }
  return [p];
}

/**
 * @param item one MULTIPLE instance payload
 * @param index zero-based index
 * @param total total instance count
 */
export function multipleInstanceCaption(item: unknown, index: number, total: number): string {
  if (item && typeof item === 'object' && 'name' in item) {
    const nameRaw = (item as { name: unknown }).name;
    const name = nameRaw == null ? '' : String(nameRaw).trim();
    if (name.length > 0) return name;
  }
  /** Omit generic "Entry 1 of 1" when only one row — card title is already the facet name (e.g. Links). */
  if (total <= 1) return '';
  return `Entry ${index + 1} of ${total}`;
}

export function isRelationsEnvelope(raw: unknown): raw is { relations: unknown[] } {
  return (
    raw != null &&
    typeof raw === 'object' &&
    !Array.isArray(raw) &&
    Array.isArray((raw as { relations?: unknown }).relations)
  );
}

/** Writes one instance back into a MULTIPLE payload, preserving `{ relations: [...] }` when present. */
export function replaceMultipleItemAt(raw: unknown, index: number, newItem: unknown): unknown {
  const items = multipleFacetItemValues(raw);
  if (index < 0 || index >= items.length) return raw;
  const next = [...items];
  next[index] = newItem;
  if (isRelationsEnvelope(raw)) {
    return { ...(raw as object), relations: next };
  }
  return next;
}

/** Appends an empty object instance for MULTIPLE facets. */
export function appendEmptyMultipleItem(raw: unknown): unknown {
  const items = multipleFacetItemValues(raw);
  const next = [...items, {}];
  if (raw != null && isRelationsEnvelope(raw)) {
    return { ...(raw as object), relations: next };
  }
  return next;
}

export type FacetRenderUnit =
  | { kind: 'single'; facetType: string }
  | { kind: 'multipleRow'; facetType: string; index: number; total: number }
  | { kind: 'multipleEmpty'; facetType: string };
