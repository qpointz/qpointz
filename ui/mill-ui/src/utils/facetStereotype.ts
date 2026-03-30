import type { FacetPayloadSchema, FacetSchemaType } from '../types/facetTypes';

/** Presentation for STRING or STRING[] (array-of-string) facet value schemas driven by stereotype tags. */
export type FacetStringStereotypeKind = 'none' | 'hyperlink' | 'email';

/**
 * True if stereotype hints apply to this value schema: a scalar STRING or an ARRAY whose items are STRING.
 */
export function facetStereotypeAppliesToValueSchema(schema: FacetPayloadSchema): boolean {
  return (
    schema.type === 'STRING' ||
    (schema.type === 'ARRAY' && schema.items?.type === 'STRING')
  );
}

/**
 * Parses API / YAML stereotype wire form into ordered tags.
 * Accepts a scalar comma-separated string or a JSON string array.
 */
export function stereotypeTagsFromWire(raw: string | string[] | null | undefined): string[] {
  if (raw == null) return [];
  if (Array.isArray(raw)) {
    return raw.map((t) => String(t).trim()).filter((t) => t.length > 0);
  }
  return String(raw)
    .split(',')
    .map((t) => t.trim())
    .filter((t) => t.length > 0);
}

/**
 * Serializes tags for the facet payload field wire shape.
 * Non-array value schemas use a single comma-separated string; ARRAY uses a JSON array of strings.
 */
export function stereotypeWireFromTags(
  tags: string[],
  valueSchemaType: FacetSchemaType
): string | string[] | undefined {
  const cleaned = tags.map((t) => t.trim()).filter((t) => t.length > 0);
  if (cleaned.length === 0) return undefined;
  if (valueSchemaType === 'ARRAY') return cleaned;
  return cleaned.join(',');
}

/**
 * Stereotype presentation for a facet field's value schema. Applies to STRING and to ARRAY of STRING.
 * If both `email` and `hyperlink` tags are present, `email` wins.
 */
export function facetStringStereotype(
  fieldValueSchema: FacetPayloadSchema,
  stereotypeWire: string | string[] | null | undefined
): FacetStringStereotypeKind {
  if (!facetStereotypeAppliesToValueSchema(fieldValueSchema)) return 'none';
  const tags = stereotypeTagsFromWire(stereotypeWire).map((t) => t.toLowerCase());
  if (tags.includes('email')) return 'email';
  if (tags.includes('hyperlink')) return 'hyperlink';
  return 'none';
}

/**
 * True when stereotype tags include `hyperlink` (case-insensitive), not overridden by `email`,
 * for a STRING or ARRAY-of-STRING value schema.
 */
export function facetFieldIsHyperlinkStringField(
  fieldValueSchema: FacetPayloadSchema,
  stereotypeWire: string | string[] | null | undefined
): boolean {
  return facetStringStereotype(fieldValueSchema, stereotypeWire) === 'hyperlink';
}

/**
 * Whether read-only UI should render the hyperlink row (`FacetHyperlinkReadOnly`): STRING or ARRAY of STRING
 * (hyperlink tag; email wins over hyperlink), or OBJECT / ARRAY of OBJECT with a hyperlink tag (`title` + `href`).
 */
export function facetHyperlinkPresentationActive(
  fieldValueSchema: FacetPayloadSchema,
  stereotypeWire: string | string[] | null | undefined
): boolean {
  const tags = stereotypeTagsFromWire(stereotypeWire).map((t) => t.toLowerCase());
  if (!tags.includes('hyperlink')) return false;
  if (facetStereotypeAppliesToValueSchema(fieldValueSchema)) {
    if (tags.includes('email')) return false;
    return true;
  }
  if (fieldValueSchema.type === 'OBJECT') return true;
  if (fieldValueSchema.type === 'ARRAY' && fieldValueSchema.items?.type === 'OBJECT') return true;
  return false;
}

/**
 * True when the field uses the `tags` stereotype for presentation (#value read-only, TagsInput in edit).
 * `email` / `hyperlink` take precedence over `tags` when present.
 */
export function facetTagsPresentationActive(
  fieldValueSchema: FacetPayloadSchema,
  stereotypeWire: string | string[] | null | undefined
): boolean {
  if (!facetStereotypeAppliesToValueSchema(fieldValueSchema)) return false;
  const tags = stereotypeTagsFromWire(stereotypeWire).map((t) => t.toLowerCase());
  if (!tags.includes('tags')) return false;
  if (tags.includes('email')) return false;
  if (tags.includes('hyperlink')) return false;
  return true;
}

/** Pragmatic single-address check for facet form validation (not full RFC 5322). */
const FACET_EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function facetEmailLooksValid(s: string): boolean {
  return FACET_EMAIL_PATTERN.test(s.trim());
}

/**
 * Builds a mailto href for facet email values.
 * Unsafe schemes (e.g. javascript:) resolve to #.
 */
export function facetMailtoHref(raw: string): string {
  const t = raw.trim();
  if (!t) return '#';
  if (/^\s*javascript:/i.test(t)) return '#';
  return `mailto:${t}`;
}

/**
 * Builds an href for facet hyperlink values; adds https when no scheme is present.
 * Unsafe schemes (e.g. javascript:) resolve to #.
 */
export function facetHyperlinkHref(raw: string): string {
  const t = raw.trim();
  if (!t) return '#';
  if (/^\s*javascript:/i.test(t)) return '#';
  if (/^https?:\/\//i.test(t)) return t;
  return `https://${t}`;
}
