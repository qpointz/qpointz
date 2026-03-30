import type { FacetPayloadSchema } from '../types/facetTypes';

/** Manifest slice used to derive the payload schema currently being edited (matches `EntityDetails` editing rules). */
export type FacetEditManifestSlice = {
  targetCardinality?: string;
  payload?: FacetPayloadSchema;
};

/**
 * Schema applied when editing a facet: one OBJECT row for a MULTIPLE instance, or wrapped ARRAY otherwise for whole-list MULTIPLE edits.
 */
export function effectiveFacetPayloadSchemaForEdit(
  manifest: FacetEditManifestSlice | null | undefined,
  editInstanceIndex: number | null
): FacetPayloadSchema | null {
  if (!manifest?.payload) return null;
  if (editInstanceIndex !== null) return manifest.payload;
  if ((manifest.targetCardinality ?? 'SINGLE') === 'MULTIPLE') {
    return {
      type: 'ARRAY',
      title: manifest.payload.title,
      description: manifest.payload.description,
      items: manifest.payload,
    };
  }
  return manifest.payload;
}

/**
 * True when {@link EntityDetails} `renderField` can edit the entire schema without the JSON/Textarea fallback
 * (arrays of objects, arrays of arrays, missing `items`, etc. are unsupported).
 */
export function facetPayloadSchemaFormSupported(schema: FacetPayloadSchema): boolean {
  switch (schema.type) {
    case 'OBJECT':
      return (schema.fields ?? []).every((f) => facetPayloadSchemaFormSupported(f.schema));
    case 'STRING':
    case 'NUMBER':
    case 'BOOLEAN':
    case 'ENUM':
      return true;
    case 'ARRAY': {
      const items = schema.items;
      if (!items) return false;
      if (items.type === 'STRING' || items.type === 'ENUM' || items.type === 'NUMBER') return true;
      if (items.type === 'OBJECT') return facetPayloadSchemaFormSupported(items);
      return false;
    }
    default:
      return false;
  }
}

/**
 * True when the facet-type admin **form** cannot edit the full content schema: the tree only walks
 * `OBJECT.fields`, not `ARRAY.items`, so ARRAYs whose `items` are OBJECT or ARRAY must be maintained
 * in expert JSON/YAML to avoid silent defaults from type switches.
 */
export function facetTypeContentSchemaRequiresExpertMode(schema: FacetPayloadSchema): boolean {
  const walk = (node: FacetPayloadSchema): boolean => {
    switch (node.type) {
      case 'OBJECT':
        return (node.fields ?? []).some((f) => walk(f.schema));
      case 'ARRAY': {
        const it = node.items;
        if (!it) return true;
        if (it.type === 'OBJECT' || it.type === 'ARRAY') return true;
        return false;
      }
      default:
        return false;
    }
  };
  return walk(schema);
}
