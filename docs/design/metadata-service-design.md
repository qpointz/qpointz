# Metadata Service Design - Faceted Architecture

**Status:** Design Approved, Implementation In Progress  
**Date:** November 5, 2025  
**Last Updated:** December 2024  
**Author:** Architecture Team

---

## Implementation Notes (December 2024)

**Important Changes from Original Design:**

1. **Simplified Hierarchy**: The `catalogName` field was removed from the domain model. The hierarchy is now `schema → table → attribute` instead of `catalog → schema → table → attribute`. Entity IDs follow the format `schema.table` or `schema.table.attribute`.

2. **Phase 1 & 2 Completed**: 
   - ✅ Core foundation (M1) - domain model, facets, file-based repository
   - ✅ REST API service (M2) - read-only endpoints with Swagger documentation
   - ✅ Metadata browser UI - collapsible sidebar, entity details, URL routing

3. **Tree API Enhancement**: The `/api/metadata/v1/explorer/tree` endpoint now includes attributes as children of tables, enabling expandable table views in the UI.

4. **URL Routing**: The UI supports shareable URLs in the format `/explore/:schema/:table/:attribute?` for bookmarking and sharing specific entities.

5. **Read-Only API**: Initial implementation is read-only (PUT/DELETE endpoints removed) to establish the foundation before adding editing capabilities.

6. **Multi-file Repository Support**: FileMetadataRepository now supports loading from multiple files or file patterns (e.g., `classpath:metadata/base.yml,classpath:metadata/overrides/*.yml`). Later files replace entities with the same ID, and facets are merged by type+scope (replaced, not merged within facet).

7. **ValueMappingFacet**: Strongly-typed facet implementation for value mappings, replacing HashMap-based parsing. Registered in FacetRegistry and used by MetadataV2AnnotationsProvider.

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
   - AI features in `mill-ai-core` (optional dependency)
   - Clear module boundaries

2. **Faceted Architecture**
   - Each metadata entity can have multiple "facets" (aspects)
   - Core facets: Structural, Descriptive, Relation, Concept (in `mill-metadata-core`)
   - AI facets: ValueMapping, Enrichment, DataQuality, Semantic (in `mill-ai-core`)
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
└── mill-metadata-core/              # ⭐ NEW - Core metadata system
    ├── domain/
    │   ├── MetadataEntity.java          # Base entity with facets
    │   ├── MetadataFacet.java           # Base facet interface
    │   ├── FacetRegistry.java           # Plugin registry
    │   └── core/                         # Core facets
    │       ├── StructuralFacet.java     # Physical schema binding
    │       ├── DescriptiveFacet.java    # Descriptions, display names
    │       ├── RelationFacet.java       # Relationships
    │       └── ConceptFacet.java        # Business concepts
    │
    ├── repository/
    │   ├── MetadataRepository.java      # Main repository interface
    │   ├── file/                         # YAML/JSON implementation
    │   ├── jdbc/                         # Database implementation
    │   └── composite/                    # Composite (physical + annotations)
    │
    └── service/
        ├── MetadataService.java         # CRUD + search
        ├── MetadataSyncService.java     # Sync physical schema
        └── FacetService.java            # Facet management

ai/
└── mill-ai-core/                     # AI-specific facets and services
    ├── metadata/                      # ⭐ NEW - AI metadata facets
    │   ├── facets/
    │   │   ├── ValueMappingFacet.java       # Value mapping (NL2SQL)
    │   │   ├── EnrichmentFacet.java         # NL2SQL enrichments
    │   │   ├── DataQualityFacet.java        # DQ rules (future)
    │   │   └── SemanticFacet.java           # Embeddings (future)
    │   │
    │   └── service/
    │       ├── ValueMappingService.java
    │       └── EnrichmentService.java
    │
    └── ...                            # Other AI core functionality (NL2SQL, etc.)

services/
└── mill-metadata-service/            # ⭐ NEW - REST API service
    ├── api/
    │   ├── MetadataController.java      # Main metadata API
    │   ├── SchemaExplorerController.java # Navigation API
    │   ├── FacetController.java         # Facet CRUD API
    │   └── AIMetadataController.java    # AI-specific API
    │
    ├── dto/
    │   └── ...                          # API DTOs (separate from domain)
    │
    └── config/
        └── MetadataServiceConfiguration.java  # Spring Boot configuration
```

---

## Core Concepts

### 1. Metadata Entity

A `MetadataEntity` represents any metadata object (schema, table, attribute, concept). Each entity:

- Has a unique identifier (e.g., `moneta.clients` or `moneta.clients.account_id`)
- Has a hierarchical location (schema → table → attribute) - **simplified from original design (catalog removed)**
- Contains multiple **facets** (pluggable aspects of metadata)
- Tracks audit information (created/updated by/at)

```java
public class MetadataEntity {
    private String id;
    private MetadataType type;  // SCHEMA, TABLE, ATTRIBUTE, CONCEPT (CATALOG removed)
    
    // Hierarchical location (null for unbound entities like CONCEPT)
    private String schemaName;
    private String tableName;
    private String attributeName;
    
    // Audit
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    
    // Facets: Map<facetType, Map<scope, facetData>>
    // Structure: { "descriptive": { "global": {...}, "user:alice": {...} } }
    // All facets stored as JSON-serializable maps with scope support
    private Map<String, Map<String, Object>> facets = new HashMap<>();
    
    // Helper methods for scope-aware facet access
    public <T> Optional<T> getFacet(String facetType, String scope, Class<T> facetClass);
    public <T> Optional<T> getMergedFacet(String facetType, String userId, 
                                         List<String> userTeams, List<String> userRoles, 
                                         Class<T> facetClass);
    public void setFacet(String facetType, String scope, Object facetData);
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
- Can have **scope** for visibility control (global, user-specific, team-specific)

**Facet Binding Types:**

Facets can be bound in three ways:

1. **Entity-Bound Facets** - Attached to a single physical entity (schema, table, attribute)
   - Examples: `DescriptiveFacet`, `ValueMappingFacet`, `StructuralFacet`
   - Stored directly on the entity's facets map

2. **Cross-Entity Facets** - Span multiple physical elements
   - Examples: `RelationFacet` (stored on source table, references target table)
   - Contains references to other entities via FQN or entity IDs

3. **Unbound Facets** - Standalone concepts not tied to a single entity
   - Examples: `ConceptFacet` (e.g., "Premium Customers" spanning multiple tables)
   - Stored as standalone `MetadataEntity` with `type = CONCEPT`
   - References multiple physical entities but exists independently

**Facet Scope:**

Each facet can have a **scope** attribute that controls visibility:

- `"global"` - visible to everyone
- `"user:{userId}"` - visible only to specific user (e.g., `"user:alice@company.com"`)
- `"team:{teamName}"` - visible to team members (e.g., `"team:engineering"`)
- `"role:{roleName}"` - visible to users with role (e.g., `"role:admin"`)
- Custom scopes as needed

When querying facets, the system merges facets in priority order: user > team > role > global.

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

## Facet Binding Types

Facets can be bound to entities in three distinct ways, each serving different use cases:

### 1. Entity-Bound Facets

**Definition:** Facets attached to a single physical entity (catalog, schema, table, or attribute).

**Examples:**
- `DescriptiveFacet` - descriptions, synonyms, tags for a table/column
- `ValueMappingFacet` - value mappings for a specific attribute
- `StructuralFacet` - physical schema info for a table/attribute

**Storage:**
- Stored directly on the `MetadataEntity` in the `facets` map
- Entity has a clear hierarchical location (catalog → schema → table → attribute)

**YAML Example:**
```yaml
entities:
  - id: prod.moneta.customers.customer_id
    type: ATTRIBUTE
    catalogName: production
    schemaName: moneta
    tableName: customers
    attributeName: customer_id
    facets:
      descriptive:
        global:
          displayName: Customer ID
          description: Unique customer identifier
      value-mapping:
        global:
          mappings:
            - userTerm: customer number
              databaseValue: customer_id
```

**API Access:**
```
GET /api/metadata/v1/catalogs/{catalog}/schemas/{schema}/tables/{table}/attributes/{attr}/facets/descriptive
```

### 2. Cross-Entity Facets

**Definition:** Facets that span multiple physical entities. The facet is stored on one entity but references others.

**Examples:**
- `RelationFacet` - relationships between tables (stored on source table, references target table)
- `LineageFacet` - data lineage spanning multiple tables

**Storage:**
- Stored on one of the participating entities (typically the "source" or "primary" entity)
- Contains references to other entities via FQN or entity IDs
- Can be queried from any participating entity via service layer

**YAML Example:**
```yaml
entities:
  - id: prod.moneta.customers
    type: TABLE
    catalogName: production
    schemaName: moneta
    tableName: customers
    facets:
      relation:  # ← Stored on "customers" but references "accounts"
        global:
          relations:
            - name: customer_accounts
              sourceTable:
                catalog: production
                schema: moneta
                table: customers
              targetTable:
                catalog: production
                schema: moneta
                table: accounts
              cardinality: ONE_TO_MANY
              type: FOREIGN_KEY
```

**API Access:**
```
# Get relations from source table
GET /api/metadata/v1/catalogs/{catalog}/schemas/{schema}/tables/{table}/facets/relation

# Query relations (bidirectional - service layer handles this)
GET /api/metadata/v1/catalogs/{catalog}/schemas/{schema}/tables/{table}/relations
```

### 3. Unbound Facets

**Definition:** Facets that represent standalone concepts not tied to a single physical entity. They may reference multiple entities but exist independently.

**Examples:**
- `ConceptFacet` - business concepts like "Premium Customers" that span multiple tables
- `SemanticFacet` - semantic embeddings for search
- Future: `DataQualityFacet` for cross-table quality rules

**Storage:**
- Stored as standalone `MetadataEntity` with `type = CONCEPT`
- No hierarchical location (catalog/schema/table/attribute are null or used for organization)
- Contains references to physical entities it relates to

**YAML Example:**
```yaml
entities:
  - id: concept_premium_customers
    type: CONCEPT
    catalogName: production  # Optional: for organization
    schemaName: business-concepts  # Optional: logical grouping
    facets:
      concept:
        global:
          concepts:
            - name: Premium Customers
              description: High-value customer segment
              sql: "segment = 'PREMIUM' AND balance > 100000"
              targets:
                - schema: moneta
                  table: customers
                - schema: moneta
                  table: accounts
                  attributes: [segment, balance]
```

**API Access:**
```
# List all concepts
GET /api/metadata/v1/concepts

# Get concept by ID
GET /api/metadata/v1/concepts/{conceptId}

# Find concepts referencing a table
GET /api/metadata/v1/concepts?referencedTable={catalog}.{schema}.{table}
```

### Summary Table

| Facet Type | Storage Location | Entity Type | References | Example |
|------------|------------------|-------------|------------|---------|
| **Entity-Bound** | On the entity itself | CATALOG, SCHEMA, TABLE, ATTRIBUTE | None (self-contained) | `DescriptiveFacet` on `customers.customer_id` |
| **Cross-Entity** | On one participating entity | TABLE (typically) | References other entities | `RelationFacet` on `customers` referencing `accounts` |
| **Unbound** | Standalone entity | CONCEPT | References multiple entities | `ConceptFacet` for "Premium Customers" |

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
  - `targets[]` - list of target tables with optional attributes
    - `schema` - schema name
    - `table` - table name
    - `attributes[]` - optional list of attribute names
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
      targets:
        - schema: moneta
          table: customers
        - schema: moneta
          table: accounts
          attributes: [segment, balance]
      tags: [segmentation, marketing]
      category: segmentation
      source: MANUAL
```

---

### AI Facets (mill-ai-core)

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

### Document-Style Persistence

The metadata system uses **document-style persistence** where each `MetadataEntity` is stored as a single document with all facets serialized as JSON within it. This approach:

- **Simplifies the model** - no joins, single read/write per entity
- **Enables flexibility** - add facets without schema changes
- **Supports atomicity** - update all facets of an entity in one transaction
- **Maintains consistency** - same structure for file and database backends

### File-Based Repository (YAML/JSON)

**Multi-file Support:**

The file-based repository supports loading metadata from multiple files or file patterns:

```yaml
# application.yml
mill:
  metadata:
    v2:
      file:
        path: "classpath:metadata/base.yml,classpath:metadata/overrides/*.yml"
```

**Merging Strategy:**
- Files are loaded in order (left to right)
- Entities with the same ID: later files completely replace earlier entities
- Facets: For each `facetType + scope` combination, later files replace facets completely (not merged within facet)
- Facets from earlier files that don't exist in later files are preserved

**Example:**
```yaml
# base.yml
entities:
  - id: moneta.clients
    facets:
      descriptive:
        global:
          displayName: "Clients"
          description: "Base description"
      structural:
        global:
          physicalName: "CLIENTS"

# overrides.yml
entities:
  - id: moneta.clients
    facets:
      descriptive:
        global:
          displayName: "Customer Accounts"  # Replaces entire descriptive.global
        user:alice:
          displayName: "My Clients"  # New scope

# Result:
# - descriptive.global: {displayName: "Customer Accounts"} (description removed)
# - structural.global: {physicalName: "CLIENTS"} (preserved from base)
# - descriptive.user:alice: {displayName: "My Clients"} (new from override)
```

**Complete YAML format with scoped facets:**

```yaml
# metadata/complete.yml
entities:
  # Entity-bound facet example
  - id: prod.moneta.customers.customer_id
    type: ATTRIBUTE
    catalogName: production
    schemaName: moneta
    tableName: customers
    attributeName: customer_id
    facets:
      structural:
        global:  # ← Scope: visible to everyone
          physicalName: CUSTOMER_ID
          physicalType: INTEGER
          isPrimaryKey: true
      descriptive:
        global:
          displayName: Customer ID
          description: Unique customer identifier
        user:alice@company.com:  # ← User-specific override
          description: My personal notes about customer ID
          notes: "This is my annotation"
      value-mapping:
        global:
          mappings:
            - userTerm: customer number
              databaseValue: customer_id
              language: en
        user:bob@company.com:  # ← User's personal mapping
          mappings:
            - userTerm: cust id
              databaseValue: customer_id
              language: en
  
  # Cross-entity facet example (stored on source table)
  - id: prod.moneta.customers
    type: TABLE
    catalogName: production
    schemaName: moneta
    tableName: customers
    facets:
      structural:
        global:
          physicalName: CUSTOMERS
          tableType: TABLE
          backendType: jdbc
          lastSyncedAt: "2025-11-05T10:00:00Z"
      descriptive:
        global:
          displayName: Customers
          description: Customer master data
          businessMeaning: Central repository for all customer information
          synonyms: [clients, customer base]
          tags: [pii, gdpr]
          businessDomain: customer-management
          owner: crm-team@company.com
          classification: CONFIDENTIAL
        team:engineering:  # ← Team-specific
          owner: engineering-team@company.com
          technicalNotes: "High-volume table, optimize queries"
      relation:  # ← Cross-entity facet
        global:
          relations:
            - name: customer_accounts
              description: Customer to accounts relationship
              sourceTable:
                catalog: production
                schema: moneta
                table: customers
              sourceAttributes: [customer_id]
              targetTable:
                catalog: production
                schema: moneta
                table: accounts
              targetAttributes: [customer_id]
              cardinality: ONE_TO_MANY
              type: FOREIGN_KEY
              joinSql: "customers.customer_id = accounts.customer_id"
  
  # Unbound facet example (standalone CONCEPT entity)
  - id: concept_premium_customers
    type: CONCEPT
    catalogName: production  # Optional: for organization
    schemaName: business-concepts  # Optional: logical grouping
    facets:
      concept:
        global:
          concepts:
            - name: Premium Customers
              description: High-value customer segment
              sql: "segment = 'PREMIUM' AND balance > 100000"
              targets:
                - schema: moneta
                  table: customers
                - schema: moneta
                  table: accounts
                  attributes: [segment, balance]
              tags: [segmentation, marketing]
              category: segmentation
              source: MANUAL
```

**Benefits:**
- Version control friendly
- Human-readable
- Easy to edit
- Diff/merge support
- Scope-aware facets for personalization

### Database Repository (Document-Style)

**Single Table Schema:**

```sql
-- Single table for all metadata entities (document-style)
CREATE TABLE metadata_entity (
    id VARCHAR(255) PRIMARY KEY,
    type VARCHAR(50) NOT NULL,  -- CATALOG, SCHEMA, TABLE, ATTRIBUTE, CONCEPT
    
    -- Hierarchical location (for physical entities)
    catalog_name VARCHAR(255),
    schema_name VARCHAR(255),
    table_name VARCHAR(255),
    attribute_name VARCHAR(255),
    
    -- All facets stored as single JSON document with scope support
    -- Structure: { "facetType": { "scope": {...}, "scope2": {...} } }
    facets JSONB NOT NULL DEFAULT '{}'::jsonb,
    
    -- Audit
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    
    -- Unique constraint for physical entities
    CONSTRAINT unique_physical_entity UNIQUE NULLS NOT DISTINCT (
        catalog_name, schema_name, table_name, attribute_name
    ) WHERE type IN ('CATALOG', 'SCHEMA', 'TABLE', 'ATTRIBUTE')
);

-- Indexes
CREATE INDEX idx_metadata_type ON metadata_entity(type);
CREATE INDEX idx_metadata_location ON metadata_entity(catalog_name, schema_name, table_name, attribute_name);
CREATE INDEX idx_metadata_facets_gin ON metadata_entity USING GIN (facets);  -- Full JSONB index
CREATE INDEX idx_metadata_facets_descriptive ON metadata_entity USING GIN ((facets->'descriptive'));  -- Specific facet index
CREATE INDEX idx_metadata_facets_tags ON metadata_entity USING GIN ((facets->'descriptive'->'global'->'tags'));  -- Tags array index
```

**Example JSON in `facets` column:**

```json
{
  "structural": {
    "global": {
      "physicalName": "CUSTOMERS",
      "physicalType": "TABLE",
      "tableType": "TABLE",
      "backendType": "jdbc",
      "lastSyncedAt": "2025-11-05T10:00:00Z"
    }
  },
  "descriptive": {
    "global": {
      "displayName": "Customers",
      "description": "Customer master data",
      "tags": ["pii", "gdpr"]
    },
    "user:alice@company.com": {
      "description": "My custom description",
      "notes": "This is my personal annotation"
    },
    "team:engineering": {
      "owner": "engineering-team@company.com",
      "technicalNotes": "High-volume table"
    }
  },
  "relation": {
    "global": {
      "relations": [
        {
          "name": "customer_accounts",
          "sourceTable": {
            "catalog": "production",
            "schema": "moneta",
            "table": "customers"
          },
          "targetTable": {
            "catalog": "production",
            "schema": "moneta",
            "table": "accounts"
          },
          "cardinality": "ONE_TO_MANY",
          "type": "FOREIGN_KEY"
        }
      ]
    }
  }
}
```

**Benefits:**
- Single table/document per entity - no joins needed
- All facets in one JSON document - atomic updates
- Scope support built-in - personalization and access control
- Flexible querying via JSONB operators
- Multi-user support with transactional updates

### Composite Repository

**Merges physical schema + annotations:**

The composite repository combines physical schema discovery with user-defined annotations stored in the document-style repository.

```java
@Service
public class CompositeMetadataRepository implements MetadataRepository {
    
    private final SchemaProvider physicalProvider;  // From JDBC/Calcite
    private final MetadataRepository annotationsRepo;  // From file/DB (document-style)
    
    @Override
    public Optional<MetadataEntity> findByLocation(String catalog, String schema, String table, String attribute) {
        // Get physical schema
        var physical = physicalProvider.getTable(schema, table);
        
        // Get annotations from document repository
        var annotations = annotationsRepo.findByLocation(catalog, schema, table, attribute);
        
        // Merge: physical + annotations
        return Optional.of(merge(physical, annotations));
    }
    
    private MetadataEntity merge(
        io.qpointz.mill.proto.Table physical,
        Optional<MetadataEntity> annotations
    ) {
        var entity = annotations.orElse(new MetadataEntity());
        
        // Structural facet from physical schema (always global scope)
        var structuralData = Map.of(
            "physicalName", physical.getName(),
            "physicalType", "TABLE",
            "tableType", physical.getType().toString(),
            "backendType", "jdbc",
            "lastSyncedAt", Instant.now().toString()
        );
        entity.setFacet("structural", "global", structuralData);
        
        // Preserve user annotations (descriptive, value-mapping, etc.)
        // All user annotations remain in their respective scopes
        
        return entity;
    }
}
```

**Benefits:**
- Auto-sync physical schema
- Preserve user annotations with scope support
- Single source of truth
- Schema drift detection
- Document-style storage for all metadata

### External Metadata Provider (Pluggable Architecture)

**Design for External Metadata Integration:**

The metadata system supports plugging in external metadata sources (e.g., Collibra, Alation, DataHub, custom systems) through a provider interface. External providers map their native metadata format to Mill's faceted model.

**Architecture:**

```
┌─────────────────────────────────────────────────────────┐
│              MetadataService                            │
│  (uses MetadataRepository interface)                   │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
        ┌───────────────────────────────┐
        │   MetadataRepository          │  (interface)
        └───────────────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        │               │               │
        ▼               ▼               ▼
┌─────────────┐ ┌─────────────┐ ┌──────────────────┐
│   JPA Repo  │ │  YAML Repo  │ │ External Provider │
│  (default)  │ │  (default)  │ │   (pluggable)     │
└─────────────┘ └─────────────┘ └──────────────────┘
                                              │
                                              ▼
                                    ┌──────────────────┐
                                    │ External System  │
                                    │ (Collibra, etc.) │
                                    └──────────────────┘
```

**Provider Interface:**

```java
// In mill-metadata-core/repository/provider/
public interface ExternalMetadataProvider {
    
    /**
     * Provider identifier (e.g., "collibra", "alation", "datahub")
     */
    String getProviderId();
    
    /**
     * Provider name for display
     */
    String getProviderName();
    
    /**
     * Check if provider is available/configured
     */
    boolean isAvailable();
    
    /**
     * Initialize provider with configuration
     */
    void initialize(Map<String, Object> config);
    
    /**
     * Fetch metadata entities from external system
     * Returns entities mapped to Mill's MetadataEntity format
     */
    List<MetadataEntity> fetchEntities(
        String catalog, 
        String schema, 
        Optional<String> table
    );
    
    /**
     * Fetch specific entity by location
     */
    Optional<MetadataEntity> fetchEntity(
        String catalog, 
        String schema, 
        String table, 
        Optional<String> attribute
    );
    
    /**
     * Search entities in external system
     */
    List<MetadataEntity> search(String query, MetadataType... types);
    
    /**
     * Get capabilities of this provider
     */
    ProviderCapabilities getCapabilities();
}

@Data
public class ProviderCapabilities {
    private boolean supportsRead = true;
    private boolean supportsWrite = false;  // Most external systems are read-only
    private boolean supportsScopes = false;  // External systems may not support scopes
    private boolean supportsRealTimeSync = false;
    private List<String> supportedFacetTypes = List.of();  // Which facets can be mapped
}
```

**External Repository Adapter:**

```java
// In mill-metadata-core/repository/external/
@Service
public class ExternalMetadataRepository implements MetadataRepository {
    
    private final ExternalMetadataProvider provider;
    private final MetadataEntityMapper mapper;
    private final Cache<EntityKey, MetadataEntity> cache;  // Optional caching
    
    public ExternalMetadataRepository(
        ExternalMetadataProvider provider,
        MetadataEntityMapper mapper
    ) {
        this.provider = provider;
        this.mapper = mapper;
        this.cache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();
    }
    
    @Override
    public Optional<MetadataEntity> findByLocation(
        String catalog, String schema, String table, String attribute
    ) {
        EntityKey key = new EntityKey(catalog, schema, table, attribute);
        
        // Check cache first
        return Optional.ofNullable(cache.getIfPresent(key))
            .or(() -> {
                // Fetch from external provider
                var entity = provider.fetchEntity(catalog, schema, table, Optional.ofNullable(attribute));
                
                // Map external format to Mill facets
                var mappedEntity = entity.map(e -> mapper.mapToMillFormat(e));
                
                // Cache result
                mappedEntity.ifPresent(e -> cache.put(key, e));
                
                return mappedEntity;
            });
    }
    
    @Override
    public List<MetadataEntity> findByType(MetadataType type) {
        return provider.fetchEntities(null, null, Optional.empty())
            .stream()
            .filter(e -> e.getType() == type)
            .map(mapper::mapToMillFormat)
            .toList();
    }
    
    @Override
    public void save(MetadataEntity entity) {
        if (!provider.getCapabilities().isSupportsWrite()) {
            throw new UnsupportedOperationException(
                "Provider " + provider.getProviderId() + " does not support writes"
            );
        }
        // Write back to external system (if supported)
        // ...
    }
}
```

**Metadata Entity Mapper:**

```java
// In mill-metadata-core/repository/external/
public interface MetadataEntityMapper {
    
    /**
     * Map external metadata format to Mill's MetadataEntity
     */
    MetadataEntity mapToMillFormat(Object externalEntity);
    
    /**
     * Map Mill's MetadataEntity to external format (for writes)
     */
    Object mapToExternalFormat(MetadataEntity millEntity);
}

// Example implementation for Collibra
@Component
public class CollibraMetadataMapper implements MetadataEntityMapper {
    
    @Override
    public MetadataEntity mapToMillFormat(Object externalEntity) {
        CollibraAsset asset = (CollibraAsset) externalEntity;
        
        MetadataEntity entity = new MetadataEntity();
        entity.setId(asset.getId());
        entity.setType(mapType(asset.getType()));
        
        // Map location
        entity.setCatalogName(asset.getDomain());
        entity.setSchemaName(asset.getCommunity());
        entity.setTableName(asset.getName());
        
        // Map facets from Collibra attributes
        Map<String, Map<String, Object>> facets = new HashMap<>();
        
        // Descriptive facet from Collibra description/attributes
        Map<String, Object> descriptive = new HashMap<>();
        descriptive.put("global", Map.of(
            "displayName", asset.getName(),
            "description", asset.getDescription().orElse(""),
            "tags", asset.getTags(),
            "owner", asset.getOwner().orElse("")
        ));
        facets.put("descriptive", descriptive);
        
        // Map other Collibra attributes to appropriate facets
        // ...
        
        entity.setFacets(facets);
        return entity;
    }
    
    private MetadataType mapType(String collibraType) {
        return switch (collibraType) {
            case "Table" -> MetadataType.TABLE;
            case "Column" -> MetadataType.ATTRIBUTE;
            case "Schema" -> MetadataType.SCHEMA;
            default -> MetadataType.TABLE;
        };
    }
}
```

**Provider Registry:**

```java
// In mill-metadata-core/repository/provider/
@Service
public class MetadataProviderRegistry {
    
    private final Map<String, ExternalMetadataProvider> providers = new HashMap<>();
    private final ApplicationContext applicationContext;
    
    @PostConstruct
    public void registerProviders() {
        // Auto-discover providers via Spring
        applicationContext.getBeansOfType(ExternalMetadataProvider.class)
            .values()
            .forEach(provider -> {
                providers.put(provider.getProviderId(), provider);
                if (provider.isAvailable()) {
                    log.info("Registered metadata provider: {}", provider.getProviderName());
                }
            });
    }
    
    public Optional<ExternalMetadataProvider> getProvider(String providerId) {
        return Optional.ofNullable(providers.get(providerId));
    }
    
    public List<ExternalMetadataProvider> getAvailableProviders() {
        return providers.values().stream()
            .filter(ExternalMetadataProvider::isAvailable)
            .toList();
    }
}
```

**Configuration:**

```yaml
# application.yml
mill:
  metadata:
    storage:
      type: external  # file, jpa, composite, external
      provider: collibra  # Provider ID
    
    # External provider configuration
    external:
      provider: collibra
      config:
        baseUrl: https://collibra.company.com/api
        apiKey: ${COLLIBRA_API_KEY}
        timeout: 30s
        cache:
          enabled: true
          ttl: 5m
      
      # Mapping configuration
      mapping:
        # Map Collibra asset types to Mill types
        typeMapping:
          "Table": TABLE
          "Column": ATTRIBUTE
          "Schema": SCHEMA
        
        # Map Collibra attributes to Mill facets
        facetMapping:
          "Description": descriptive.description
          "Business Owner": descriptive.owner
          "Tags": descriptive.tags
          "Data Classification": descriptive.classification
```

**Multi-Source Composite Repository:**

```java
// In mill-metadata-core/repository/composite/
@Service
public class MultiSourceMetadataRepository implements MetadataRepository {
    
    private final List<MetadataRepository> sources;  // Ordered list
    private final MetadataRepository primarySource;  // For writes
    
    public MultiSourceMetadataRepository(
        List<MetadataRepository> sources,
        @Qualifier("primaryMetadataRepository") MetadataRepository primarySource
    ) {
        this.sources = sources;
        this.primarySource = primarySource;
    }
    
    @Override
    public Optional<MetadataEntity> findByLocation(
        String catalog, String schema, String table, String attribute
    ) {
        // Try sources in order, merge results
        List<MetadataEntity> found = sources.stream()
            .map(repo -> repo.findByLocation(catalog, schema, table, attribute))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
        
        if (found.isEmpty()) {
            return Optional.empty();
        }
        
        // Merge entities (primary source wins for conflicts)
        return Optional.of(mergeEntities(found));
    }
    
    private MetadataEntity mergeEntities(List<MetadataEntity> entities) {
        MetadataEntity merged = new MetadataEntity();
        // Merge logic: combine facets from all sources
        // Primary source facets take precedence
        // ...
        return merged;
    }
    
    @Override
    public void save(MetadataEntity entity) {
        // Always write to primary source
        primarySource.save(entity);
    }
}
```

**Benefits:**
- **Pluggable**: Add external providers without changing core code
- **Flexible**: Support multiple metadata sources simultaneously
- **Mappable**: External formats mapped to Mill's faceted model
- **Cached**: Optional caching for performance
- **Configurable**: Provider selection via configuration
- **Extensible**: Easy to add new providers by implementing interface

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

### Facet Management (Scope-Aware)

```
# Get merged facet for current user (global + user + team + role merged)
GET    /api/metadata/v1/entities/{entityId}/facets/{facetType}

# Get all scopes for a facet type
GET    /api/metadata/v1/entities/{entityId}/facets/{facetType}/scopes

# Get facet for specific scope (admin only)
GET    /api/metadata/v1/entities/{entityId}/facets/{facetType}/scopes/{scope}

# Save facet with scope
PUT    /api/metadata/v1/entities/{entityId}/facets/{facetType}/scopes/{scope}

# Delete facet for specific scope
DELETE /api/metadata/v1/entities/{entityId}/facets/{facetType}/scopes/{scope}
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

**Last Updated:** December 2024

### Phase 1: Core Foundation ✅ COMPLETED
- [x] Create `mill-metadata-core` module
- [x] Implement `MetadataEntity` with facet support
- [x] Implement core facets: Structural, Descriptive, Relation, Concept
- [x] Implement `FacetRegistry` plugin system
- [x] File-based repository with YAML serialization
- [x] Basic REST API (read-only)
- [x] Unit tests and integration tests
- [x] Removed `catalogName` from model (simplified hierarchy)
- [x] Converted classes to records where appropriate (EntityReference, Relation, Concept)

**Deliverables:** ✅
- Working file-based metadata system
- YAML format defined (document-style with scoped facets)
- REST API for read operations
- Example metadata files (`moneta-meta-repository.yaml`)
- Spring Boot auto-configuration
- Swagger/OpenAPI documentation

### Phase 2: REST API Service & UI ✅ COMPLETED
- [x] Create `mill-metadata-service` module
- [x] Implement REST controllers (MetadataController, SchemaExplorerController, FacetController)
- [x] Read-only API endpoints
- [x] DTOs for API responses
- [x] Swagger/OpenAPI annotations
- [x] Tree API with attributes as children
- [x] Metadata browser UI at `/explore` route
- [x] Collapsible sidebar matching chat view design
- [x] URL routing for shareable links (`/explore/:schema/:table/:attribute?`)
- [x] OpenAPI-generated TypeScript client
- [x] React context provider for state management

**Deliverables:** ✅
- Fully functional read-only REST API
- Metadata browser UI with tree navigation
- Entity details view with facet tabs
- Shareable/bookmarkable URLs
- Type-safe API integration

### Phase 3: AI Facets (Pending)
- [ ] Add AI metadata facets to `mill-ai-core` module (under `metadata/` package)
- [ ] Implement `ValueMappingFacet` in `mill-ai-core/metadata/facets/`
- [ ] Implement `EnrichmentFacet` in `mill-ai-core/metadata/facets/`
- [ ] Migrate existing value mapping to new structure
- [ ] Auto-registration of AI facets
- [ ] AI-specific REST API (`AIMetadataController`)
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
      type: composite  # file, jpa, composite, external, multi-source
    
    # File-based
    file:
      path: classpath:metadata/complete.yml
      watch: true  # Auto-reload on changes
    
    # Database (document-style with JSONB)
    jpa:
      database-platform: org.hibernate.dialect.PostgreSQLDialect
      ddl-auto: update
      # Single table: metadata_entity with facets as JSONB column
    
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
    
    # External metadata provider (optional, for future use)
    external:
      enabled: false
      provider: collibra  # collibra, alation, datahub, custom
      config:
        baseUrl: https://collibra.company.com/api
        apiKey: ${EXTERNAL_METADATA_API_KEY}
        timeout: 30s
        cache:
          enabled: true
          ttl: 5m
      mapping:
        # Map external asset types to Mill types
        typeMapping:
          "Table": TABLE
          "Column": ATTRIBUTE
          "Schema": SCHEMA
        # Map external attributes to Mill facets
        facetMapping:
          "Description": descriptive.description
          "Business Owner": descriptive.owner
          "Tags": descriptive.tags
    
    # Multi-source configuration (optional, for future use)
    multi-source:
      enabled: false
      sources:
        - type: jpa  # Primary source (for writes)
          priority: 1
        - type: external
          provider: collibra
          priority: 2  # Lower priority (read-only)
    
    # Scope support
    scope:
      enabled: true
      default-scope: global  # Default scope for new facets
      merge-priority: [user, team, role, global]  # Priority order for merging
```

---

## Benefits Summary

### 1. Separation of Concerns
- ✅ Core metadata in `mill-metadata-core` (no AI dependencies)
- ✅ AI features in `mill-ai-core` (optional)
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

