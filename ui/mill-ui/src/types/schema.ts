export type EntityType = 'SCHEMA' | 'TABLE' | 'ATTRIBUTE';

export interface SchemaEntity {
  id: string;
  type: EntityType;
  name: string;
  children?: SchemaEntity[];
}

export interface DescriptiveFacet {
  displayName?: string;
  description?: string;
  businessMeaning?: string;
  businessDomain?: string;
  businessOwner?: string;
  tags?: string[];
  synonyms?: string[];
}

export interface StructuralFacet {
  physicalName?: string;
  physicalType?: string;
  precision?: number;
  scale?: number;
  isPrimaryKey?: boolean;
  isForeignKey?: boolean;
  isUnique?: boolean;
  nullable?: boolean;
  defaultValue?: string;
}

export interface RelationFacet {
  id: string;
  name: string;
  sourceEntity: string;
  targetEntity: string;
  cardinality: '1:1' | '1:N' | 'N:1' | 'N:N';
  relationType: 'FOREIGN_KEY' | 'LOGICAL' | 'HIERARCHICAL';
  description?: string;
}

export interface EntityFacets {
  descriptive?: DescriptiveFacet;
  structural?: StructuralFacet;
  relations?: RelationFacet[];
}

export interface EntityWithFacets {
  entity: SchemaEntity;
  facets: EntityFacets;
}
