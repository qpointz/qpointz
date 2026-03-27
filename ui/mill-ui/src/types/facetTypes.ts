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

export interface FacetPayloadField {
  name: string;
  schema: FacetPayloadSchema;
  required?: boolean;
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

