/**
 * Display helpers for metadata entity targets on facet proposals and chat commentary.
 */

const MODEL_SCHEMA_URN = /^urn:mill\/model\/schema:(.+)$/i;
const MODEL_TABLE_URN = /^urn:mill\/model\/table:(.+)$/i;
const MODEL_ATTRIBUTE_URN = /^urn:mill\/model\/attribute:(.+)$/i;
const MODEL_ROOT_URN = /^urn:mill\/model\/model:model-entity$/i;
const MODEL_ROOT_CATALOG_PATH = 'model-entity';

function normalizeCatalogPath(raw: string): string {
  return raw
    .split('.')
    .map((part) => part.trim().toLowerCase())
    .filter(Boolean)
    .join('.');
}

/**
 * Parses a canonical `urn:mill/model/…` entity URN into a qualified catalog path.
 */
export function catalogPathFromModelEntityUrn(metadataEntityId: string): string | null {
  const trimmed = metadataEntityId.trim();
  const schema = MODEL_SCHEMA_URN.exec(trimmed);
  if (schema?.[1]) return normalizeCatalogPath(schema[1]);
  const table = MODEL_TABLE_URN.exec(trimmed);
  if (table?.[1]) return normalizeCatalogPath(table[1]);
  const attribute = MODEL_ATTRIBUTE_URN.exec(trimmed);
  if (attribute?.[1]) return normalizeCatalogPath(attribute[1]);
  return null;
}

/**
 * Qualified database object name for display (`schema`, `schema.table`, or `schema.table.column`).
 */
export function facetEntityCatalogPath(
  catalogPath: string | undefined,
  metadataEntityId: string,
): string {
  const fromWire = catalogPath?.trim();
  if (fromWire) {
    if (fromWire.toLowerCase() === MODEL_ROOT_CATALOG_PATH) return MODEL_ROOT_CATALOG_PATH;
    return normalizeCatalogPath(fromWire);
  }

  const trimmed = metadataEntityId.trim();
  if (MODEL_ROOT_URN.test(trimmed)) return MODEL_ROOT_CATALOG_PATH;

  const fromUrn = catalogPathFromModelEntityUrn(trimmed);
  if (fromUrn) return fromUrn;

  if (trimmed && !/^urn:/i.test(trimmed) && trimmed.includes('.')) {
    return normalizeCatalogPath(trimmed);
  }

  return trimmed;
}

/**
 * Model explorer route for a qualified catalog path.
 */
export function modelRouteFromCatalogPath(catalogPath: string): string {
  const normalized = normalizeCatalogPath(catalogPath);
  if (!normalized) return '/model';
  return `/model/${normalized.split('.').join('/')}`;
}
