# Metadata Service Design - Faceted Architecture

**Status:** Design Approved  
**Date:** November 5, 2025  
**Author:** Architecture Team

---

## Executive Summary

This document describes the design for a centralized metadata service in Mill using a **faceted architecture**. The system provides:

1. **Centralized metadata management** - single source of truth for schema, table, and column metadata
2. **Extensible facet system** - easily add new metadata aspects without breaking changes
3. **AI-specific metadata** - value mappings, NL2SQL enrichments, data quality rules
4. **Flexible persistence** - file-based (YAML) or database (JPA), consistent serialization
5. **REST API** - comprehensive API for UI navigation and metadata management
6. **NL2SQL integration** - capture and apply enrichments from chat sessions

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Module Structure](#module-structure)
3. [Core Concepts](#core-concepts)
4. [Domain Model](#domain-model)
5. [Facet Types](#facet-types)
6. [Persistence](#persistence)
7. [REST API](#rest-api)
8. [NL2SQL Integration](#nl2sql-integration)
9. [Implementation Roadmap](#implementation-roadmap)
10. [Examples](#examples)

---

## Architecture Overview

### Design Principles

1. **Separation of Concerns**
   - Core metadata in `mill-metadata-core` (no AI dependencies)
   - AI features in `mill-metadata-ai` (optional dependency)
   - Clear module boundaries

2. **Faceted Architecture**
   - Each metadata entity can have multiple "facets" (aspects)
   - Core facets: Structural, Descriptive, Relation, Concept
   - AI facets: ValueMapping, Enrichment, DataQuality, Semantic
   - New facets added without schema changes

3. **Pluggable Persistence**
   - File-based (YAML/JSON) for version control
   - Database (JPA) for production
   - Composite (physical schema + annotations)

4. **Extensibility**
   - Plugin system for facets via `FacetRegistry`
   - Automatic serialization/deserialization
   - REST API generation

5. **Integration-Friendly**
   - Adapters for existing interfaces
   - Event-driven architecture
   - REST API for any consumer

---

## Module Structure

```
core/
├── mill-metadata-core/              # ⭐ NEW - Core metadata system
│   ├── domain/
│   │   ├── MetadataEntity.java          # Base entity with facets
│   │   ├── MetadataFacet.java           # Base facet interface
│   │   ├── FacetRegistry.java           # Plugin registry
│   │   └── core/                         # Core facets
│   │       ├── StructuralFacet.java     # Physical schema binding
│   │       ├── DescriptiveFacet.java    # Descriptions, display names
│   │       ├── RelationFacet.java       # Relationships
│   │       └── ConceptFacet.java        # Business concepts
│   │
│   ├── repository/
│   │   ├── MetadataRepository.java      # Main repository interface
│   │   ├── file/                         # YAML/JSON implementation
│   │   ├── jdbc/                         # Database implementation
│   │   └── composite/                    # Composite (physical + annotations)
│   │
│   ├── service/
│   │   ├── MetadataService.java         # CRUD + search
│   │   ├── MetadataSyncService.java     # Sync physical schema
│   │   └── FacetService.java            # Facet management
│   │
│   └── api/
│       ├── MetadataController.java      # Main metadata API
│       ├── SchemaExplorerController.java # Navigation API
│       └── FacetController.java         # Facet CRUD API
│
└── mill-metadata-ai/                # ⭐ NEW - AI-specific facets
    ├── facets/
    │   ├── ValueMappingFacet.java       # Value mapping (NL2SQL)
    │   ├── EnrichmentFacet.java         # NL2SQL enrichments
    │   ├── DataQualityFacet.java        # DQ rules (future)
    │   └── SemanticFacet.java           # Embeddings (future)
    │
    ├── service/
    │   ├── ValueMappingService.java
    │   └── EnrichmentService.java
    │
    └── api/
        └── AIMetadataController.java
```

---

## Core Concepts

### 1. Metadata Entity

A `MetadataEntity` represents any metadata object (catalog, schema, table, attribute). Each entity:

- Has a unique identifier
- Has a hierarchical location (catalog → schema → table → attribute)
- Contains multiple **facets** (pluggable aspects of metadata)
- Tracks audit information (created/updated by/at)

```java
public class MetadataEntity {
    private String id;
    private MetadataType type;  // CATALOG, SCHEMA, TABLE, ATTRIBUTE
    
    // Hierarchical location
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String attributeName;
    
    // Audit
    private Instant createdAt;
    private Instant updatedAt;
    
    // Facets (pluggable aspects)
    private Map<String, MetadataFacet> facets = new HashMap<>();
}
```

### 2. Facets

A **facet** is one aspect or dimension of metadata. Examples:

- **Structural facet** - physical schema (data types, nullability, constraints)
- **Descriptive facet** - human-readable info (descriptions, synonyms, tags)
- **Relation facet** - relationships between entities
- **Value mapping facet** - user term → database value mappings
- **Enrichment facet** - metadata learned from NL2SQL sessions

Each facet:
- Is independent and optional
- Can be added/removed dynamically
- Serializes consistently (YAML/JSON/DB)
- Has its own validation rules

```java
public interface MetadataFacet {
    String getFacetType();
    void setOwner(MetadataEntity owner);
    void validate() throws ValidationException;
    MetadataFacet merge(MetadataFacet other);
}
```

### 3. Facet Registry

The `FacetRegistry` is a plugin system that:

- Registers facet types at startup
- Maps facet keys (strings) to classes
- Enables dynamic facet discovery
- Supports facets from different modules

```java
// Core module registers core facets
FacetRegistry.register(StructuralFacet.class);
FacetRegistry.register(DescriptiveFacet.class);

// AI module registers AI facets
FacetRegistry.register(ValueMappingFacet.class);
FacetRegistry.register(EnrichmentFacet.class);
```

### 4. Composite Repository

The composite repository merges:
- **Physical schema** (from `SchemaProvider` - actual database structure)
- **Annotations** (from file/database - user-defined metadata)

This allows:
- Auto-discovery of schema from databases
- Enrichment with business metadata
- Sync between physical and logical views

---

## Domain Model

### Enums

```java
public enum MetadataType {
    CATALOG, SCHEMA, TABLE, ATTRIBUTE, RELATION, CONCEPT
}

public enum TableType {
    TABLE, VIEW, MATERIALIZED_VIEW, EXTERNAL
}

public enum DataClassification {
    PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
}

public enum RelationCardinality {
    ONE_TO_ONE, ONE_TO_MANY, MANY_TO_ONE, MANY_TO_MANY
}

public enum RelationType {
    FOREIGN_KEY,    // Physical FK constraint
    LOGICAL,        // Business relationship (no FK)
    HIERARCHICAL    // Parent-child hierarchy
}
```

---

## Facet Types

### Core Facets (mill-metadata-core)

#### 1. StructuralFacet - Physical Schema Binding

**Purpose:** Links logical metadata to actual database objects.

**Fields:**
- `physicalName` - actual database name
- `physicalType` - data type (VARCHAR, INTEGER, etc.)
- `precision`, `scale` - numeric precision
- `nullable` - NULL allowed?
- `isPrimaryKey`, `isForeignKey`, `isUnique` - constraints
- `backendType` - jdbc, calcite, etc.
- `lastSyncedAt` - last sync timestamp

**Use cases:**
- Map logical names to physical names (e.g., "Customer ID" → "CUSTOMER_ID")
- Track schema changes over time
- Support multiple backends

```yaml
structural:
  physicalName: CUSTOMER_ID
  physicalType: INTEGER
  nullable: false
  isPrimaryKey: true
  backendType: jdbc
  lastSyncedAt: "2025-11-05T10:00:00Z"
```

#### 2. DescriptiveFacet - Human-Readable Metadata

**Purpose:** Business-friendly descriptions for NL2SQL and UI.

**Fields:**
- `displayName` - user-friendly name
- `description` - short description
- `businessMeaning` - detailed explanation for NL2SQL
- `synonyms`, `aliases` - alternative names
- `tags` - categorization
- `businessDomain` - e.g., "sales", "finance"
- `owner` - responsible team/person
- `classification` - PUBLIC, CONFIDENTIAL, etc.
- `unit` - USD, meters, etc.

**Use cases:**
- NL2SQL query understanding
- UI display
- Data catalog
- Glossary

```yaml
descriptive:
  displayName: Customer ID
  description: Unique customer identifier
  businessMeaning: Primary identifier for customer records across all systems
  synonyms: [customer number, client id, cust id]
  tags: [pii, primary-key]
  businessDomain: customer-management
  owner: crm-team@company.com
  classification: CONFIDENTIAL
```

#### 3. RelationFacet - Relationships

**Purpose:** Define relationships between tables.

**Fields:**
- `relations[]` - list of relationships
  - `name` - relationship name
  - `description` - explanation
  - `sourceTable`, `sourceAttributes` - parent side
  - `targetTable`, `targetAttributes` - child side
  - `cardinality` - ONE_TO_ONE, ONE_TO_MANY, etc.
  - `type` - FOREIGN_KEY, LOGICAL, HIERARCHICAL
  - `joinSql` - SQL join expression
  - `businessMeaning` - business context

**Use cases:**
- Auto-join in NL2SQL
- Data lineage
- ER diagram generation
- Query optimization

```yaml
relation:
  relations:
    - name: customer_accounts
      description: Customer to accounts relationship
      sourceTable: customers
      sourceAttributes: [customer_id]
      targetTable: accounts
      targetAttributes: [customer_id]
      cardinality: ONE_TO_MANY
      type: FOREIGN_KEY
      joinSql: "customers.customer_id = accounts.customer_id"
      businessMeaning: One customer can have multiple accounts
```

#### 4. ConceptFacet - Business Concepts

**Purpose:** Define business concepts that span multiple tables/columns.

**Fields:**
- `concepts[]` - list of concepts
  - `name` - concept name
  - `description` - explanation
  - `sql` - SQL definition (optional)
  - `referencedTables`, `referencedAttributes` - references
  - `tags`, `category` - organization
  - `source` - MANUAL, INFERRED, NL2SQL
  - `sourceSession` - NL2SQL session ID (if applicable)

**Use cases:**
- NL2SQL concept understanding ("premium customers")
- Business glossary
- Data dictionary
- Knowledge capture

```yaml
concept:
  concepts:
    - name: Premium Customers
      description: High-value customer segment
      sql: "segment = 'PREMIUM' AND balance > 100000"
      referencedTables: [customers, accounts]
      referencedAttributes: [segment, balance]
      tags: [segmentation, marketing]
      category: segmentation
      source: MANUAL
```

---

### AI Facets (mill-metadata-ai)

#### 5. ValueMappingFacet - Term Mapping

**Purpose:** Map user terms to database values for NL2SQL.

**Fields:**
- `mappings[]` - list of value mappings
  - `userTerm` - what user says (e.g., "premium")
  - `databaseValue` - actual DB value (e.g., "PREMIUM")
  - `displayValue` - UI display (e.g., "Premium")
  - `description` - explanation
  - `language` - ISO 639-1 code (en, es, etc.)
  - `confidence` - 0.0 to 1.0
  - `aliases` - alternative terms
  - `sourceType` - MANUAL, SQL_QUERY, NL2SQL, etc.
- `sources[]` - mapping sources
  - `type` - SQL_QUERY, API, VECTOR_STORE
  - `definition` - SQL query or API endpoint
  - `cronExpression` - refresh schedule

**Use cases:**
- NL2SQL value resolution ("California" → "CA")
- Multi-language support
- Dynamic value lists from database
- RAG-based value suggestion

```yaml
value-mapping:
  mappings:
    - userTerm: premium
      databaseValue: PREMIUM
      displayValue: Premium
      language: en
      confidence: 1.0
      sourceType: MANUAL
      createdAt: "2025-11-01T00:00:00Z"
    
    - userTerm: gold
      databaseValue: PREMIUM
      displayValue: Premium (Gold)
      language: en
      confidence: 0.9
      sourceType: NL2SQL
      createdAt: "2025-11-02T15:30:00Z"
    
    - userTerm: básico
      databaseValue: BASIC
      displayValue: Básico
      language: es
      confidence: 1.0
      sourceType: MANUAL
  
  sources:
    - id: segment_values_sql
      type: SQL_QUERY
      definition: "SELECT DISTINCT segment FROM customers"
      cronExpression: "0 0 * * * *"
      lastSynced: "2025-11-05T10:00:00Z"
```

#### 6. EnrichmentFacet - NL2SQL Enrichments

**Purpose:** Capture metadata enrichments from NL2SQL sessions (enrich-model intent).

**Fields:**
- `enrichments[]` - list of enrichments
  - `id` - unique enrichment ID
  - `type` - MODEL, RULE, CONCEPT, RELATION
  - `description` - explanation
  - `sessionId` - NL2SQL session ID
  - `userId` - who created it
  - `createdAt` - timestamp
  - `status` - PENDING, APPROVED, REJECTED, AUTO_APPLIED
  - `data` - type-specific data (ModelEnrichment, RuleEnrichment, etc.)

**Enrichment Types:**

1. **ModelEnrichment** - descriptive metadata
   - `targetType` - schema, table, attribute
   - `description` - text description
   - `categories` - e.g., ["definition"]

2. **RuleEnrichment** - data quality rules
   - `ruleName` - rule identifier
   - `description` - explanation
   - `sql` - filter expression

3. **ConceptEnrichment** - business concepts
   - `name`, `description`, `sql`
   - `tags`, `category`, `targets`

4. **RelationEnrichment** - relationships
   - `name`, `description`
   - `source`, `target`, `cardinality`
   - `sql`, `columns`

**Use cases:**
- Capture metadata from chat sessions
- User approval workflow
- Continuous metadata improvement
- Knowledge base building

```yaml
enrichment:
  enrichments:
    - id: enrich_001
      type: MODEL
      description: "User explained customer_id meaning"
      sessionId: "nl2sql-session-123"
      userId: "alice@company.com"
      createdAt: "2025-11-05T14:30:00Z"
      status: APPROVED
      data:
        enrichment-type: model
        targetType: attribute
        description: "Unique identifier for customers across all systems"
        categories: [definition]
    
    - id: enrich_002
      type: RULE
      description: "Active customers only"
      sessionId: "nl2sql-session-124"
      userId: "bob@company.com"
      createdAt: "2025-11-05T15:00:00Z"
      status: PENDING
      data:
        enrichment-type: rule
        targetType: table
        ruleName: active_customers_only
        description: "Only include active customer records"
        sql: "status = 'ACTIVE' AND last_activity > CURRENT_DATE - INTERVAL '90 days'"
```

#### 7. DataQualityFacet - Data Quality Rules (Future)

**Purpose:** Define and track data quality rules.

**Fields:**
- `rules[]` - list of DQ rules
  - `name`, `description`
  - `type` - COMPLETENESS, VALIDITY, CONSISTENCY, UNIQUENESS, REFERENTIAL
  - `expression` - SQL or DSL
  - `threshold` - pass threshold (0.0 to 1.0)
  - `cronExpression` - execution schedule
  - `lastExecuted`, `lastScore`, `status`

**Use cases:**
- Data quality monitoring
- Data profiling
- Validation rules
- Alerting

```yaml
data-quality:
  rules:
    - name: customer_id_unique
      description: Customer ID must be unique
      type: UNIQUENESS
      expression: "COUNT(*) = COUNT(DISTINCT customer_id)"
      threshold: 1.0
      cronExpression: "0 0 0 * * *"
      lastExecuted: "2025-11-05T00:00:00Z"
      lastScore: 1.0
      status: PASSING
```

#### 8. SemanticFacet - Vector Embeddings (Future)

**Purpose:** Store vector embeddings for semantic search.

**Fields:**
- `embeddingModel` - model name (e.g., "text-embedding-ada-002")
- `embedding` - vector representation (float[])
- `embeddingCreatedAt` - timestamp
- `similarEntities[]` - semantically similar entities
  - `targetId`, `similarity`, `relationshipType`

**Use cases:**
- Semantic search
- Similar table/column discovery
- NL2SQL table selection
- Knowledge graph

```yaml
semantic:
  embeddingModel: text-embedding-ada-002
  embedding: [0.123, -0.456, 0.789, ...]  # 1536-dim vector
  embeddingCreatedAt: "2025-11-05T10:00:00Z"
  similarEntities:
    - targetId: entity_456
      similarity: 0.92
      relationshipType: similar
```

---

## Persistence

### File-Based Repository (YAML)

**Complete YAML format:**

```yaml
# metadata/complete.yml
catalog:
  name: production
  displayName: Production Data Catalog

schemas:
  - name: moneta
    displayName: Moneta Banking System
    
    facets:
      descriptive:
        description: Core banking system
        owner: banking-team@company.com
        tags: [banking, financial]
        businessDomain: finance
    
    tables:
      - name: customers
        
        facets:
          structural:
            physicalName: CUSTOMERS
            tableType: TABLE
            backendType: jdbc
            lastSyncedAt: "2025-11-05T10:00:00Z"
          
          descriptive:
            displayName: Customers
            description: Customer master data
            businessMeaning: Central repository for all customer information
            synonyms: [clients, customer base]
            tags: [pii, gdpr]
            businessDomain: customer-management
            owner: crm-team@company.com
            classification: CONFIDENTIAL
          
          relation:
            relations:
              - name: customer_accounts
                description: Customer to accounts relationship
                sourceTable: customers
                sourceAttributes: [customer_id]
                targetTable: accounts
                targetAttributes: [customer_id]
                cardinality: ONE_TO_MANY
                type: FOREIGN_KEY
          
          concept:
            concepts:
              - name: Premium Customers
                description: High-value customer segment
                sql: "segment = 'PREMIUM' AND balance > 100000"
                category: segmentation
                source: MANUAL
          
          value-mapping:
            mappings:
              - userTerm: premium
                databaseValue: PREMIUM
                displayValue: Premium
                language: en
                sourceType: MANUAL
          
          enrichment:
            enrichments:
              - id: enrich_001
                type: MODEL
                description: "User explanation"
                sessionId: "nl2sql-session-123"
                status: APPROVED
        
        attributes:
          - name: customer_id
            facets:
              structural:
                physicalName: CUSTOMER_ID
                physicalType: INTEGER
                isPrimaryKey: true
              descriptive:
                displayName: Customer ID
                description: Unique identifier
```

**Benefits:**
- Version control friendly
- Human-readable
- Easy to edit
- Diff/merge support

### Database Repository (JPA)

**Schema:**

```sql
-- Main entity table
CREATE TABLE metadata_entity (
    id VARCHAR(255) PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    catalog_name VARCHAR(255),
    schema_name VARCHAR(255),
    table_name VARCHAR(255),
    attribute_name VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- Facets stored as JSON
CREATE TABLE metadata_facet (
    id UUID PRIMARY KEY,
    entity_id VARCHAR(255) NOT NULL REFERENCES metadata_entity(id),
    facet_type VARCHAR(100) NOT NULL,
    facet_data JSONB NOT NULL,  -- PostgreSQL JSONB
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE(entity_id, facet_type)
);

CREATE INDEX idx_facet_entity ON metadata_facet(entity_id);
CREATE INDEX idx_facet_type ON metadata_facet(facet_type);
CREATE INDEX idx_facet_data ON metadata_facet USING GIN (facet_data);  -- JSONB index
```

**Benefits:**
- Multi-user support
- Transactional updates
- Query facet data
- Full-text search

### Composite Repository

**Merges physical schema + annotations:**

```java
@Service
public class CompositeMetadataRepository implements MetadataRepository {
    
    private final SchemaProvider physicalProvider;  // From JDBC/Calcite
    private final MetadataRepository annotationsRepo;  // From file/DB
    
    @Override
    public Optional<TableMetadata> getTable(String catalog, String schema, String table) {
        // Get physical schema
        var physical = physicalProvider.getTable(schema, table);
        
        // Get annotations
        var annotations = annotationsRepo.findById(tableId(catalog, schema, table));
        
        // Merge: physical + annotations
        return Optional.of(merge(physical, annotations));
    }
    
    private TableMetadata merge(
        io.qpointz.mill.proto.Table physical,
        Optional<MetadataEntity> annotations
    ) {
        var entity = annotations.orElse(new MetadataEntity());
        
        // Structural facet from physical schema
        var structural = entity.getOrCreateFacet(StructuralFacet.class);
        structural.setPhysicalName(physical.getName());
        structural.setLastSyncedAt(Instant.now());
        
        // Preserve user annotations (descriptive, value-mapping, etc.)
        // ...
        
        return entity;
    }
}
```

**Benefits:**
- Auto-sync physical schema
- Preserve user annotations
- Single source of truth
- Schema drift detection

---

## REST API

### Metadata CRUD

```
GET    /api/metadata/v1/catalogs
GET    /api/metadata/v1/catalogs/{catalog}
PUT    /api/metadata/v1/catalogs/{catalog}

GET    /api/metadata/v1/catalogs/{catalog}/schemas
GET    /api/metadata/v1/catalogs/{catalog}/schemas/{schema}
PUT    /api/metadata/v1/catalogs/{catalog}/schemas/{schema}

GET    /api/metadata/v1/catalogs/{catalog}/schemas/{schema}/tables
GET    /api/metadata/v1/catalogs/{catalog}/schemas/{schema}/tables/{table}
PUT    /api/metadata/v1/catalogs/{catalog}/schemas/{schema}/tables/{table}

GET    /api/metadata/v1/catalogs/{catalog}/schemas/{schema}/tables/{table}/attributes
GET    /api/metadata/v1/catalogs/{catalog}/schemas/{schema}/tables/{table}/attributes/{attr}
PUT    /api/metadata/v1/catalogs/{catalog}/schemas/{schema}/tables/{table}/attributes/{attr}
```

### Facet Management

```
GET    /api/metadata/v1/entities/{entityId}/facets
GET    /api/metadata/v1/entities/{entityId}/facets/{facetType}
PUT    /api/metadata/v1/entities/{entityId}/facets/{facetType}
DELETE /api/metadata/v1/entities/{entityId}/facets/{facetType}
```

### Search & Navigation

```
GET    /api/metadata/v1/search?q={query}
GET    /api/metadata/v1/tags/{tag}
GET    /api/metadata/v1/explorer/tree?catalog={catalog}&schema={schema}
GET    /api/metadata/v1/explorer/lineage?table={fqn}&depth={depth}
```

### Sync

```
POST   /api/metadata/v1/catalogs/{catalog}/sync
GET    /api/metadata/v1/catalogs/{catalog}/sync/status
```

### Import/Export

```
POST   /api/metadata/v1/catalogs/{catalog}/import
GET    /api/metadata/v1/catalogs/{catalog}/export
```

### AI-Specific APIs

```
# Value Mapping
POST   /api/metadata/v1/ai/entities/{entityId}/value-mappings
GET    /api/metadata/v1/ai/entities/{entityId}/value-mappings
GET    /api/metadata/v1/ai/entities/{entityId}/value-mappings/map?term={term}&language={lang}

# Enrichments
POST   /api/metadata/v1/ai/entities/{entityId}/enrichments
GET    /api/metadata/v1/ai/entities/{entityId}/enrichments
POST   /api/metadata/v1/ai/entities/{entityId}/enrichments/{enrichmentId}/approve
POST   /api/metadata/v1/ai/entities/{entityId}/enrichments/{enrichmentId}/reject
```

---

## NL2SQL Integration

### Capturing Enrichments (enrich-model intent)

When a user provides metadata during a chat session:

```
User: "The FIRST_NAME column in CLIENTS table represents the client's given name"
```

NL2SQL returns:

```json
{
  "enrichment": [
    {
      "type": "model",
      "target": "MONETA.CLIENTS.FIRST_NAME",
      "target-type": "attribute",
      "description": "The client's given name",
      "category": ["definition"]
    }
  ]
}
```

System captures this as an enrichment:

```java
@Component
public class EnrichModelIntentHandler {
    
    private final EnrichmentService enrichmentService;
    
    public void handle(EnrichModelResponse response, String sessionId, String userId) {
        for (var enrichmentDto : response.getEnrichments()) {
            var enrichment = new EnrichmentFacet.Enrichment();
            enrichment.setId(UUID.randomUUID().toString());
            enrichment.setType(EnrichmentType.valueOf(enrichmentDto.getType().toUpperCase()));
            enrichment.setDescription(enrichmentDto.getDescription());
            enrichment.setSessionId(sessionId);
            enrichment.setUserId(userId);
            enrichment.setCreatedAt(Instant.now());
            enrichment.setStatus(EnrichmentStatus.PENDING);  // Requires approval
            enrichment.setData(convertData(enrichmentDto));
            
            String entityId = resolveEntityId(enrichmentDto.getTarget());
            enrichmentService.captureEnrichment(entityId, enrichment);
        }
    }
}
```

### Approval Workflow

1. **Enrichment captured** → status = PENDING
2. **User reviews in UI** → shows in "Pending Enrichments" panel
3. **User approves** → status = APPROVED, applied to metadata
4. **User rejects** → status = REJECTED, not applied

```java
@Service
public class EnrichmentService {
    
    public void approveEnrichment(String entityId, String enrichmentId) {
        var entity = repository.findById(entityId).orElseThrow();
        var enrichmentFacet = entity.getFacet(EnrichmentFacet.class).orElseThrow();
        var enrichment = findEnrichment(enrichmentFacet, enrichmentId);
        
        // Apply to appropriate facet
        applyEnrichment(entity, enrichment);
        
        // Mark as approved
        enrichment.setStatus(EnrichmentStatus.APPROVED);
        
        repository.save(entity);
    }
    
    private void applyEnrichment(MetadataEntity entity, Enrichment enrichment) {
        switch (enrichment.getType()) {
            case MODEL -> {
                var descriptive = entity.getOrCreateFacet(DescriptiveFacet.class);
                var data = (ModelEnrichment) enrichment.getData();
                descriptive.setDescription(data.getDescription());
            }
            case RULE -> {
                var dq = entity.getOrCreateFacet(DataQualityFacet.class);
                var data = (RuleEnrichment) enrichment.getData();
                var rule = new DataQualityFacet.QualityRule();
                rule.setName(data.getRuleName());
                rule.setDescription(data.getDescription());
                rule.setExpression(data.getSql());
                dq.getRules().add(rule);
            }
            // ... other types
        }
    }
}
```

### Using Metadata in NL2SQL

Value mapping resolution:

```java
@Component
public class ValueMappingResolver {
    
    private final MetadataService metadataService;
    
    public String resolveValue(String attributeId, String userTerm, String language) {
        return metadataService.getFacet(attributeId, ValueMappingFacet.class)
            .flatMap(facet -> facet.getMappings().stream()
                .filter(m -> m.getUserTerm().equalsIgnoreCase(userTerm))
                .filter(m -> m.getLanguage().equals(language))
                .map(ValueMappingFacet.ValueMapping::getDatabaseValue)
                .findFirst()
            )
            .orElse(userTerm);  // No mapping found, use original
    }
}
```

Enhanced schema prompts:

```java
@Component
public class SchemaPromptGenerator {
    
    public String generatePrompt(List<String> tableIds) {
        var sb = new StringBuilder();
        
        for (var tableId : tableIds) {
            var table = metadataService.findById(tableId);
            var descriptive = table.getFacet(DescriptiveFacet.class).orElse(null);
            var concept = table.getFacet(ConceptFacet.class).orElse(null);
            
            sb.append(String.format("Table: %s\n", descriptive.getDisplayName()));
            sb.append(String.format("Description: %s\n", descriptive.getDescription()));
            sb.append(String.format("Business Meaning: %s\n", descriptive.getBusinessMeaning()));
            
            if (descriptive.getSynonyms() != null && !descriptive.getSynonyms().isEmpty()) {
                sb.append(String.format("Also known as: %s\n", 
                    String.join(", ", descriptive.getSynonyms())));
            }
            
            if (concept != null && !concept.getConcepts().isEmpty()) {
                sb.append("Business Concepts:\n");
                for (var c : concept.getConcepts()) {
                    sb.append(String.format("  - %s: %s\n", c.getName(), c.getDescription()));
                }
            }
            
            // Attributes...
        }
        
        return sb.toString();
    }
}
```

---

## Implementation Roadmap

### Phase 1: Core Foundation (Week 1)
- [ ] Create `mill-metadata-core` module
- [ ] Implement `MetadataEntity` with facet support
- [ ] Implement core facets: Structural, Descriptive, Relation, Concept
- [ ] Implement `FacetRegistry` plugin system
- [ ] File-based repository with YAML serialization
- [ ] Basic REST API (CRUD)
- [ ] Unit tests

**Deliverables:**
- Working file-based metadata system
- YAML format defined
- REST API for basic operations
- Example metadata files

### Phase 2: AI Facets (Week 2)
- [ ] Create `mill-metadata-ai` module
- [ ] Implement `ValueMappingFacet`
- [ ] Implement `EnrichmentFacet`
- [ ] Migrate existing value mapping to new structure
- [ ] Auto-registration of AI facets
- [ ] AI-specific REST API
- [ ] Integration tests

**Deliverables:**
- AI facets working
- Value mapping migrated
- Enrichment capture ready

### Phase 3: NL2SQL Integration (Week 3)
- [ ] Implement `EnrichmentService`
- [ ] Implement enrichment approval workflow
- [ ] Integrate with enrich-model intent
- [ ] Value mapping resolution in NL2SQL
- [ ] Enhanced schema prompt generation
- [ ] UI mockups for enrichment approval
- [ ] End-to-end tests

**Deliverables:**
- Enrichments captured from chat sessions
- Approval workflow working
- NL2SQL using enhanced metadata

### Phase 4: Persistence Options (Week 4)
- [ ] JPA repository implementation
- [ ] Database schema with migrations
- [ ] Composite repository (physical + annotations)
- [ ] Sync service
- [ ] Migration tools (file ↔ database)
- [ ] Performance testing

**Deliverables:**
- Database persistence working
- Composite repository merging physical + annotations
- Sync service auto-updating metadata

### Phase 5: Search & Navigation (Week 5)
- [ ] Full-text search implementation
- [ ] Schema explorer tree API
- [ ] Lineage graph generation
- [ ] Tag-based navigation
- [ ] Statistics collection
- [ ] Performance optimization

**Deliverables:**
- Search working across all metadata
- Navigation API for UI
- Lineage visualization support

### Phase 6: Future Facets (Week 6+)
- [ ] `DataQualityFacet` implementation
- [ ] DQ rule execution engine
- [ ] `SemanticFacet` with vector embeddings
- [ ] Semantic search
- [ ] `LineageFacet` (data lineage)
- [ ] Custom facet examples

**Deliverables:**
- Data quality monitoring
- Semantic search
- Extensibility demonstrated

---

## Examples

### Example 1: Adding Value Mapping

**Scenario:** User says "show me gold customers" but DB has "PREMIUM" segment.

**Step 1:** Capture in metadata

```yaml
# metadata/complete.yml
schemas:
  - name: moneta
    tables:
      - name: customers
        attributes:
          - name: segment
            facets:
              value-mapping:
                mappings:
                  - userTerm: gold
                    databaseValue: PREMIUM
                    displayValue: Premium (Gold)
                    language: en
                    confidence: 0.9
                    sourceType: MANUAL
```

**Step 2:** NL2SQL uses mapping

```
User: "show me gold customers"

Internal: mapValue("segment", "gold", "en") → "PREMIUM"

Generated SQL: SELECT * FROM customers WHERE segment = 'PREMIUM'
```

### Example 2: Capturing Enrichment

**Scenario:** User explains a column during chat.

**Chat:**
```
User: "The BALANCE column represents the total amount across all accounts"
```

**System captures:**

```yaml
enrichment:
  enrichments:
    - id: enrich_123
      type: MODEL
      description: "User explained BALANCE column"
      sessionId: "chat-session-456"
      userId: "alice@company.com"
      createdAt: "2025-11-05T14:30:00Z"
      status: PENDING
      data:
        enrichment-type: model
        targetType: attribute
        description: "The total amount across all accounts"
        categories: [definition]
```

**UI shows pending enrichment:**
```
Pending Enrichments (1)
├── MONETA.CUSTOMERS.BALANCE
│   └── "The total amount across all accounts"
│       [Approve] [Reject]
```

**User approves → applied to metadata:**

```yaml
attributes:
  - name: balance
    facets:
      descriptive:
        description: The total amount across all accounts  # ← Applied!
        businessMeaning: The total amount across all accounts
```

### Example 3: Adding New Facet

**Scenario:** Need to track data lineage.

**Step 1:** Define facet

```java
@Data
@EqualsAndHashCode(callSuper = true)
@FacetType("lineage")
public class LineageFacet extends AbstractFacet {
    private List<LineageEdge> upstream = new ArrayList<>();
    private List<LineageEdge> downstream = new ArrayList<>();
    
    @Data
    public static class LineageEdge {
        private String sourceTable;
        private String targetTable;
        private LineageType type;  // DIRECT, TRANSFORMATION, AGGREGATION
        private String transformationSql;
    }
}
```

**Step 2:** Register facet

```java
@Configuration
public class LineageAutoConfiguration {
    @PostConstruct
    public void registerLineageFacet() {
        FacetRegistry.register(LineageFacet.class);
    }
}
```

**Step 3:** Use it!

```yaml
tables:
  - name: customer_summary
    facets:
      lineage:
        upstream:
          - sourceTable: customers
            targetTable: customer_summary
            type: AGGREGATION
            transformationSql: "SELECT customer_id, COUNT(*) FROM customers GROUP BY customer_id"
```

**That's it!** Serialization, REST API, everything works automatically.

---

## Configuration

```yaml
# application.yml
mill:
  metadata:
    # Storage backend
    storage:
      type: composite  # file, jpa, composite
    
    # File-based
    file:
      path: classpath:metadata/complete.yml
      watch: true  # Auto-reload on changes
    
    # Database
    jpa:
      database-platform: org.hibernate.dialect.PostgreSQLDialect
      ddl-auto: update
    
    # Composite
    composite:
      physical-provider: auto  # Use SchemaProvider
      annotations-source: file
      merge-strategy: annotations-override
    
    # Sync
    sync:
      auto-sync: true
      cron: "0 0 * * * *"  # Hourly
      on-startup: true
    
    # Value mapping
    value-mapping:
      enable: true
      sources:
        - type: file
          path: classpath:metadata/value-mappings.yml
        - type: sql
          cron: "0 0 0 * * *"
    
    # Search
    search:
      provider: postgres  # elasticsearch, lucene, postgres
```

---

## Benefits Summary

### 1. Separation of Concerns
- ✅ Core metadata in `mill-metadata-core` (no AI dependencies)
- ✅ AI features in `mill-metadata-ai` (optional)
- ✅ Clean module boundaries

### 2. Easy Extensibility
- ✅ Add new facet = create class + register
- ✅ No schema changes
- ✅ No code changes in core
- ✅ Automatic serialization

### 3. Consistent Persistence
- ✅ Same YAML format for all facets
- ✅ Same database schema
- ✅ Same REST API pattern

### 4. Flexible Deployment
- ✅ File-only (development)
- ✅ Database-only (production)
- ✅ Composite (best of both)
- ✅ With/without AI features

### 5. NL2SQL Integration
- ✅ Value mapping built-in
- ✅ Enrichment capture
- ✅ Approval workflow
- ✅ Enhanced schema prompts

### 6. Future-Proof
- ✅ Add data quality rules
- ✅ Add vector embeddings
- ✅ Add lineage
- ✅ No breaking changes

---

## Next Steps

1. **Review and approve design**
2. **Start Phase 1 implementation** (Core Foundation)
3. **Create example metadata files** (moneta dataset)
4. **Design UI mockups** (enrichment approval, metadata editor)
5. **Plan migration** (existing metadata → new format)

---

## References

- [NL2SQL Enrich-Model Intent](../ai/mill-ai-core/src/main/resources/templates/nlsql/intent/enrich-model/user.prompt)
- [Current Metadata Implementation](../core/mill-service-core/src/main/java/io/qpointz/mill/services/metadata/)
- [Current Value Mapping](../ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/nlsql/ValueMapper.java)
- [Codebase Analysis](./CODEBASE_ANALYSIS.md)

---

**Document Version:** 1.0  
**Last Updated:** November 5, 2025  
**Status:** ✅ Design Approved - Ready for Implementation

