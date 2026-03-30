export type FacetSchemaType =
  | 'OBJECT'
  | 'ARRAY'
  | 'STRING'
  | 'NUMBER'
  | 'BOOLEAN'
  | 'ENUM';

export interface FacetPayloadSchema {
  type: FacetSchemaType;
  title: string;
  description: string;
  fields?: FacetPayloadField[];
  items?: FacetPayloadSchema;
  values?: FacetEnumValue[];
  format?: string;
  required?: string[];
}

export interface FacetEnumValue {
  value: string;
  description: string;
}

/**
 * One property of an OBJECT payload schema. Order matches editor / API order.
 *
 * Optional **stereotype** is a presentation metatype hint (not validated by the server): e.g.
 * {@code table} for a table picker, or hints for string hyperlink vs array tag-list renderers in the UI.
 */
export interface FacetPayloadField {
  name: string;
  schema: FacetPayloadSchema;
  required?: boolean;
  /**
   * Presentation hint tags. On the wire: comma-separated string for non-ARRAY value schemas,
   * JSON string array when the field's value schema is ARRAY.
   */
  stereotype?: string | string[] | null;
}

export interface FacetTypeManifest {
  typeKey: string;
  title: string;
  description: string;
  category?: string;
  enabled: boolean;
  mandatory: boolean;
  targetCardinality?: 'SINGLE' | 'MULTIPLE';
  applicableTo?: string[] | null;
  schemaVersion?: string | null;
  payload: FacetPayloadSchema;
}

