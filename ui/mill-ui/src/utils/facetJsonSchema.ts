import type { FacetPayloadField, FacetPayloadSchema, FacetTypeManifest } from '../types/facetTypes';

export type JsonSchemaObject = Record<string, unknown>;

export const FACET_JSON_SCHEMA_DRAFT_07 = 'http://json-schema.org/draft-07/schema#';

export function facetTypeManifestToJsonSchema(manifest: FacetTypeManifest): JsonSchemaObject {
  const schema = facetPayloadSchemaToJsonSchema(manifest.payload);
  return {
    $schema: FACET_JSON_SCHEMA_DRAFT_07,
    $id: `${manifest.typeKey}/schema`,
    ...schema,
    'x-mill-facetTypeUrn': manifest.typeKey,
    'x-mill-targetCardinality': manifest.targetCardinality ?? 'SINGLE',
    ...(manifest.applicableTo != null ? { 'x-mill-applicableTo': manifest.applicableTo } : {}),
    ...(manifest.category ? { 'x-mill-category': manifest.category } : {}),
    ...(manifest.schemaVersion ? { 'x-mill-schemaVersion': manifest.schemaVersion } : {}),
  };
}

export function facetPayloadSchemaToJsonSchema(schema: FacetPayloadSchema): JsonSchemaObject {
  const base: JsonSchemaObject = {
    title: schema.title,
    description: schema.description,
  };
  if (schema.default !== undefined) {
    base.default = schema.default;
  }

  switch (schema.type) {
    case 'OBJECT': {
      const properties: Record<string, JsonSchemaObject> = {};
      const required: string[] = [];
      for (const field of schema.fields ?? []) {
        properties[field.name] = facetFieldToJsonSchema(field);
        if (field.required !== false) {
          required.push(field.name);
        }
      }
      return {
        ...base,
        type: 'object',
        properties,
        ...(required.length > 0 ? { required } : {}),
        additionalProperties: true,
      };
    }
    case 'ARRAY':
      return {
        ...base,
        type: 'array',
        items: schema.items ? facetPayloadSchemaToJsonSchema(schema.items) : {},
      };
    case 'STRING':
      return {
        ...base,
        type: 'string',
        ...(schema.format ? { format: schema.format } : {}),
      };
    case 'NUMBER':
      return { ...base, type: 'number' };
    case 'BOOLEAN':
      return { ...base, type: 'boolean' };
    case 'ENUM':
      return {
        ...base,
        type: 'string',
        enum: (schema.values ?? []).map((v) => v.value),
        ...(schema.values?.length
          ? { 'x-mill-enumDescriptions': Object.fromEntries(schema.values.map((v) => [v.value, v.description])) }
          : {}),
      };
  }
}

function facetFieldToJsonSchema(field: FacetPayloadField): JsonSchemaObject {
  const out = facetPayloadSchemaToJsonSchema(field.schema);
  const stereotype = Array.isArray(field.stereotype)
    ? field.stereotype
    : typeof field.stereotype === 'string'
      ? field.stereotype.split(',').map((s) => s.trim()).filter(Boolean)
      : [];
  if (stereotype.length > 0) {
    out['x-mill-stereotype'] = stereotype;
  }
  return out;
}
