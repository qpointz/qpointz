import type { FacetPayloadSchema } from '../types/facetTypes';

const SCALAR_SCHEMA_TYPES = new Set(['STRING', 'NUMBER', 'BOOLEAN', 'ENUM']);

/** True when the schema node may carry an optional `default` hint. */
export function facetSchemaSupportsDefault(schema: FacetPayloadSchema): boolean {
  return SCALAR_SCHEMA_TYPES.has(schema.type);
}

/**
 * Value for form display / validation: explicit payload wins; otherwise schema `default` when set.
 */
export function facetFieldEffectiveValue(schema: FacetPayloadSchema, value: unknown): unknown {
  if (schema.type === 'BOOLEAN') {
    if (typeof value === 'boolean') return value;
  } else if (value !== undefined && value !== null) {
    return value;
  }
  if (facetSchemaSupportsDefault(schema) && schema.default !== undefined && schema.default !== null) {
    return schema.default;
  }
  return value;
}

/**
 * Fills missing scalar defaults from the payload schema (recursive for nested OBJECT fields).
 * Used when opening facet create/edit forms so optional fields with schema defaults appear pre-populated.
 */
export function applyFacetPayloadSchemaDefaults(schema: FacetPayloadSchema, value: unknown): unknown {
  if (schema.type !== 'OBJECT') return value;
  const obj = (value && typeof value === 'object' && !Array.isArray(value) ? value : {}) as Record<string, unknown>;
  const result: Record<string, unknown> = { ...obj };
  for (const field of schema.fields ?? []) {
    const current = result[field.name];
    if (field.schema.type === 'OBJECT') {
      result[field.name] = applyFacetPayloadSchemaDefaults(field.schema, current);
      continue;
    }
    if (current === undefined || current === null || (field.schema.type === 'BOOLEAN' && typeof current !== 'boolean')) {
      const effective = facetFieldEffectiveValue(field.schema, current);
      if (effective !== undefined && effective !== null) {
        result[field.name] = effective;
      }
    }
  }
  return result;
}

/** Display string for the admin default-value control (empty = no default). */
export function facetSchemaDefaultDisplay(schema: FacetPayloadSchema): string {
  const d = schema.default;
  if (d === undefined || d === null) return '';
  if (schema.type === 'BOOLEAN') {
    return d === true ? 'true' : d === false ? 'false' : '';
  }
  return String(d);
}

/**
 * Parses admin input into a schema `default`, or `undefined` when cleared / invalid.
 */
export function facetSchemaDefaultFromInput(schema: FacetPayloadSchema, raw: string): unknown | undefined {
  const trimmed = raw.trim();
  if (!trimmed) return undefined;
  switch (schema.type) {
    case 'BOOLEAN':
      if (trimmed === 'true') return true;
      if (trimmed === 'false') return false;
      return undefined;
    case 'NUMBER': {
      const n = Number(trimmed);
      return Number.isFinite(n) ? n : undefined;
    }
    case 'ENUM':
      return (schema.values ?? []).some((v) => v.value === trimmed) ? trimmed : undefined;
    case 'STRING':
      return trimmed;
    default:
      return undefined;
  }
}
