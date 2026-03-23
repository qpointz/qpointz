export type EntityType = 'SCHEMA' | 'TABLE' | 'COLUMN';

export interface ScopeOption {
  id: string;
  slug: string;
  displayName: string;
}

export interface SchemaContext {
  selectedContext: string;
  availableScopes: ScopeOption[];
}

export interface SchemaListItem {
  id: string;
  entityType: 'SCHEMA';
  schemaName: string;
  metadataEntityId?: string;
}

export interface DataTypeDescriptor {
  type: string;
  nullable: boolean;
  precision?: number;
  scale?: number;
}

export interface SchemaDetail {
  id: string;
  entityType: 'SCHEMA';
  schemaName: string;
  metadataEntityId?: string;
  tables: TableSummary[];
}

export interface TableSummary {
  id: string;
  entityType: 'TABLE';
  schemaName: string;
  tableName: string;
  metadataEntityId?: string;
}

export interface TableDetail {
  id: string;
  entityType: 'TABLE';
  schemaName: string;
  tableName: string;
  tableType: string;
  metadataEntityId?: string;
  columns: ColumnDetail[];
}

export interface ColumnDetail {
  id: string;
  entityType: 'COLUMN';
  schemaName: string;
  tableName: string;
  columnName: string;
  fieldIndex: number;
  type: DataTypeDescriptor;
  metadataEntityId?: string;
}

export type SchemaEntity = SchemaDetail | TableDetail | ColumnDetail;

export interface SchemaNode {
  id: string;
  type: EntityType;
  name: string;
  children?: SchemaNode[];
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
  type?: string;
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

export interface SchemaService {
  getContext(): Promise<SchemaContext>;
  listSchemas(context: string, facetMode?: 'none' | 'direct' | 'hierarchy'): Promise<SchemaListItem[]>;
  getSchema(schemaName: string, context: string, facetMode?: 'none' | 'direct' | 'hierarchy'): Promise<SchemaDetail | null>;
  getTable(
    schemaName: string,
    tableName: string,
    context: string,
    facetMode?: 'none' | 'direct' | 'hierarchy'
  ): Promise<TableDetail | null>;
  getColumn(
    schemaName: string,
    tableName: string,
    columnName: string,
    context: string,
    facetMode?: 'none' | 'direct' | 'hierarchy'
  ): Promise<ColumnDetail | null>;
  getTree(context: string): Promise<SchemaNode[]>;
  getEntityById(id: string, context: string): Promise<SchemaEntity | null>;
  getEntityFacets(id: string, context: string): Promise<EntityFacets>;
}
