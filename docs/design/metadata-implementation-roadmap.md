## Metadata Implementation Roadmap

**Status:** In Progress  
**Last Updated:** December 2024  
**Scope:** Implement new faceted metadata system, migrate existing metadata + value mapping, and adopt it in the UI and NL2SQL.

## Implementation Status

### ✅ Phase 1 – Core Foundation (M1) - COMPLETED
- Core domain model, facets, and file-based repository implemented
- Integration tests added

### ✅ Phase 2 – UI Read-Only + REST API (M2) - COMPLETED  
- REST API service with read-only endpoints
- Metadata browser UI with collapsible sidebar
- URL routing for shareable links
- Tree API includes attributes as children of tables

### 🔄 Phase 3 – NL2SQL & Value Mapping Migration - PENDING
### 🔄 Phase 4 – Legacy Metadata Migration - PENDING
### 🔄 Phase 5 – Enrichments, Editing & JPA/Composite Persistence - PENDING
### 🔄 Phase 6 – Advanced Facets & Search - PENDING

---

## Goals

- **Implement faceted metadata system** in a **separate module** `core/mill-metadata-core` as a **standalone, pluggable implementation**.
- **Implement AI-specific facets** in `ai/mill-ai-core` (under `metadata/` package) to keep AI-related code together.
- **Migrate** current metadata service and `ValueMapper` onto the new design with **backwards-compatible adapters**.
- **Adopt** the new metadata APIs in the **UI early (read-only first)** and then for editing and enrichments.

## Module Structure

All new implementation lives in separate modules:

- **`core/mill-metadata-core/`** - Core faceted metadata system
  - Domain model (`MetadataEntity`, `MetadataFacet`, `FacetRegistry`)
  - Core facets (`StructuralFacet`, `DescriptiveFacet`, `RelationFacet`, `ConceptFacet`)
  - Repository implementations (file-based, JPA, composite)
  - Service layer (`MetadataService`, `MetadataSyncService`, `FacetService`)
  - **No REST controllers** - pure business logic library

- **`ai/mill-ai-core/`** - AI-specific facets and services (⭐ NEW metadata package)
  - AI metadata facets under `metadata/facets/`:
    - `ValueMappingFacet`, `EnrichmentFacet`, `DataQualityFacet`, `SemanticFacet`
  - AI metadata services under `metadata/service/`:
    - `ValueMappingService`, `EnrichmentService`
  - **No REST controllers** - pure business logic library
  - Depends on `mill-metadata-core` (to use `MetadataFacet`, `FacetRegistry`)

- **`services/mill-metadata-service/`** - REST API service (⭐ NEW)
  - REST controllers (`MetadataController`, `SchemaExplorerController`, `FacetController`, `AIMetadataController`)
  - Spring Boot application configuration
  - DTOs for API responses
  - Depends on `mill-metadata-core` and `mill-ai-core` modules

These modules are **independent** of existing metadata implementations in `mill-service-core` and can be developed, tested, and deployed separately. The REST service module provides HTTP/gRPC entry points while core modules remain framework-agnostic.

---

## High-Level Phases

- **Phase 1 – Core Foundation (New M1)**
  - Create new core module with entities, facets, and file-based persistence.
- **Phase 2 – UI Read-Only + AI Facet Basics (New M2)**
  - Expose read-only explorer/search APIs and wire the UI to them.
  - Introduce basic `ValueMappingFacet` and read-only value mapping APIs.
- **Phase 3 – NL2SQL & Value Mapping Migration**
  - Bridge existing `ValueMapper` to the new facet model and gradually flip NL2SQL to use it.
- **Phase 4 – Legacy Metadata Migration**
  - Move the existing metadata implementation onto the new repository and deprecate old storage.
- **Phase 5 – Enrichments, Editing & JPA/Composite Persistence**
  - Implement enrichments, editing workflows, and production-grade persistence (JPA/composite).
- **Phase 6 – Advanced Facets & Search**
  - Add data quality, semantic facets, and richer search/navigation.

Details below are intentionally implementation-oriented so they can be turned into tickets.

---

## Phase 1 – Core Foundation (New M1) ✅ COMPLETED

**Objective:** Stand up the new metadata core in `core/mill-metadata-core` with file-based persistence and basic REST, independent of current metadata consumers.

**Status:** ✅ Completed December 2024

- **Module setup** ✅
  - ✅ `core/mill-metadata-core` Gradle module created
  - ✅ Dependencies configured: Jackson YAML, Spring Boot starter, Lombok
  - ✅ Package structure: `io.qpointz.mill.metadata.*`

- **Domain model (in `mill-metadata-core`)** ✅
  - ✅ `MetadataEntity` implemented in `domain/` package with:
    - Identifier + `MetadataType` (SCHEMA, TABLE, ATTRIBUTE, CONCEPT)
    - **Simplified hierarchy**: Removed `catalogName`, using schema/table/attribute only
    - Hierarchical location (schema, table, attribute) - nullable for unbound entities
    - Audit fields (createdAt, updatedAt, createdBy, updatedBy)
    - **Document-style facets map**: `Map<String, Map<String, Object>>` with scoped facets
    - Helper methods: `getFacet(facetType, scope, class)`, `getMergedFacet(...)`, `setFacet(...)`
  - ✅ `MetadataFacet` base interface and `AbstractFacet` helper defined
  - ✅ `FacetRegistry` implemented to register facet types and resolve them by key
  - ✅ Basic scope resolution logic (user > team > role > global)

- **Core facets (in `mill-metadata-core/domain/core/`)** ✅
  - ✅ Core facets implemented with three binding types:
    - **Entity-bound**: `StructuralFacet`, `DescriptiveFacet` - attached to single entity
    - **Cross-entity**: `RelationFacet` - stored on source entity, references target entities
    - **Unbound**: `ConceptFacet` - standalone CONCEPT entities referencing multiple physical entities
  - ✅ All facets have stable `facetType` keys and register with `FacetRegistry`
  - ✅ Converted `EntityReference`, `Relation`, and `Concept` to Java records
  - ✅ Service layer helpers for cross-entity and unbound facet queries

- **File-based repository (in `mill-metadata-core/repository/file/`)** ✅
  - ✅ `MetadataRepository` interface in `repository/` package
  - ✅ `FileMetadataRepository` in `repository/file/` that:
    - Reads YAML files with **document-style structure** (entities array with scoped facets)
    - Maps YAML structures into `MetadataEntity` with scoped facets map
    - Supports lookups: by ID, by location (schema/table/attribute), list all
    - Handles all three facet binding types

- **Service layer (in `mill-metadata-core/service/`)** ✅
  - ✅ `MetadataService` in `service/` package over `MetadataRepository` with:
    - Scope-aware facet access
    - Basic scope resolution (global + user + team + role)
    - Support for all three facet binding types
  - ✅ Auto-configuration with `@ConditionalOnBean` and `@ConditionalOnProperty`
  - ✅ **No REST controllers in core module** - framework-agnostic

- **REST API service module (in `services/mill-metadata-service/`)** ✅
  - ✅ `services/mill-metadata-service` Gradle module created
  - ✅ Dependencies: `mill-metadata-core`, Spring Boot Web starter
  - ✅ REST controllers in `api/` package:
    - ✅ `MetadataController` - read-only entity endpoints (GET by ID, by location)
    - ✅ `SchemaExplorerController` - tree navigation, search endpoints
    - ✅ `FacetController` - scope-aware facet access
    - ⏳ `AIMetadataController` - AI-specific endpoints (pending Phase 3)
  - ✅ DTOs for API responses (`MetadataEntityDto`, `FacetDto`, `TreeNodeDto`, `SearchResultDto`)
  - ✅ Spring Boot auto-configuration with `mill.metadata.v2.*` properties
  - ✅ Swagger/OpenAPI documentation with proper parameter documentation
  - ✅ **Read-only API** - PUT/DELETE endpoints removed for initial release

- **Scope support (basic)** ✅
  - ✅ Scope-aware facet storage in `MetadataEntity`
  - ✅ "global" scope supported
  - ✅ Basic scope resolution: return global facets by default
  - ✅ REST API supports scope parameter (defaults to "global")

- **Testing** ✅
  - ✅ Unit tests for domain model and repository
  - ✅ Integration tests in `src/testIT` folder
  - ✅ Auto-configuration tests

- **Deliverables** ✅
  - ✅ `core/mill-metadata-core` module builds and passes tests
  - ✅ `services/mill-metadata-service` module builds and exposes REST API
  - ✅ Example YAML (`moneta-meta-repository.yaml`) with scoped facets
  - ✅ Read-only REST endpoints serve metadata from file-based repository
  - ✅ Modules can be included as dependencies without breaking existing code
  - ✅ Document-style persistence working (all facets in single JSON structure)
  - ✅ Clear separation: core = business logic, service = REST API

---

## Phase 2 – UI Read-Only + REST API (New M2) ✅ COMPLETED

**Objective:** Let the UI consume the new metadata model read-only and establish the REST API foundation.

**Status:** ✅ Completed December 2024

- **Explorer/search APIs for UI (in `services/mill-metadata-service/api/`)** ✅
  - ✅ `SchemaExplorerController` implemented in `mill-metadata-service` providing:
    - ✅ `/api/metadata/v1/explorer/tree?schema={schema}&scope={scope}` - hierarchical tree with schemas → tables → attributes
    - ✅ `/api/metadata/v1/explorer/search?q={query}&type={type}&scope={scope}` - search metadata entities
    - ⏳ `/api/metadata/v1/explorer/lineage?table={fqn}&depth={depth}` - pending Phase 6
  - ✅ UI-friendly DTOs in `mill-metadata-service` (`MetadataEntityDto`, `TreeNodeDto`, `SearchResultDto`, `FacetDto`)
  - ✅ Controllers delegate to `MetadataService` from `mill-metadata-core`
  - ✅ Tree API includes attributes as children of tables

- **Metadata Browser UI (in `services/mill-grinder-ui/`)** ✅
  - ✅ New metadata browser at `/explore` route
  - ✅ **Collapsible sidebar** matching chat view design:
    - Navigation buttons at top (Explore, Data, Chat)
    - Tables list with expandable columns
    - Scope selector at bottom
  - ✅ **Entity details view**:
    - Entity information display
    - Facet tabs for different facet types
    - Facet viewer with scope selection
  - ✅ **URL routing**:
    - `/explore/:schema/:table/:attribute?` format
    - Shareable and bookmarkable links
    - Browser back/forward navigation support
  - ✅ **OpenAPI-generated TypeScript client** for type-safe API integration
  - ✅ React context provider (`MetadataProvider`) for state management
  - ✅ Loading states, error handling, and empty states

- **Scope support (basic)** ✅
  - ✅ Basic scope resolution in `MetadataService` (global scope)
  - ✅ REST API supports scope parameter (defaults to "global")
  - ✅ Scope selector in UI sidebar
  - ⏳ Full scope resolution (user > team > role > global) - pending Phase 5
  - ⏳ User context from security - pending Phase 5

- **Deliverables** ✅
  - ✅ UI can navigate metadata tree and view table/column details from the new service
  - ✅ UI displays entity details with all facets in tabbed interface
  - ✅ URL-based navigation for sharing and bookmarking
  - ✅ Read-only metadata browser fully functional
  - ⏳ Value mappings display - pending Phase 3
  - ⏳ User-scoped facets - pending Phase 5

---

## Phase 3 – NL2SQL & Value Mapping Migration

**Objective:** Move NL2SQL value resolution from the legacy `ValueMapper` into the new `ValueMappingFacet`, with a clean feature-flagged bridge.

- **Abstraction for value resolution**
  - Introduce `ValueResolver` (or similar) interface in `ai/mill-ai-core`.
  - Refactor existing `ValueMapper` to implement `ValueResolver` without changing NL2SQL callers.
  - Add a second implementation `FacetValueResolver` in `mill-ai-core` that:
    - Depends on `mill-metadata-core` module.
    - Calls `MetadataService` from `mill-metadata-core` and `ValueMappingService` from `mill-ai-core`.
    - Resolves mappings from `ValueMappingFacet` as in the design's `ValueMappingResolver`.

- **Feature flag for migration**
  - Add config property, e.g.:
    - `mill.metadata.value-mapping.provider = legacy|faceted|hybrid`
  - In Spring configuration:
    - Wire the appropriate `ValueResolver` bean based on this flag.
    - Optionally support `hybrid` mode (facet-first, fallback to legacy).

- **Data migration (value mappings)**
  - Implement exporter that:
    - Reads existing `ValueMapper` configuration.
    - Produces YAML/JSON compatible with `ValueMappingFacet` structure.
  - Import into file-based repo or directly into JPA repo once available.

- **Validation**
  - Add tests comparing:
    - `legacyValueResolver.resolveValue(...)` vs `facetValueResolver.resolveValue(...)` for key attributes/terms.
  - Run NL2SQL integration tests with both providers to ensure parity.

- **Deliverables**
  - NL2SQL code depends only on `ValueResolver`.
  - New facet-backed resolver implemented and feature-flagged.
  - Initial value mapping data present in the new metadata system.

---

## Phase 4 – Legacy Metadata Migration

**Objective:** Migrate the existing metadata service implementation so that it reads from the new repository and faceted model, while keeping external contracts stable.

- **Inventory and mapping**
  - Catalog current metadata representations and APIs in `core/mill-service-core`:
    - Catalogs, schemas, tables, columns, relations, tags, etc.
  - Define how each maps to:
    - `MetadataEntity` (type, hierarchy, ID strategy) from `mill-metadata-core`.
    - Core facets (`StructuralFacet`, `DescriptiveFacet`, `RelationFacet`, `ConceptFacet`) from `mill-metadata-core`.

- **Adapter layer (in `mill-service-core` or new adapter module)**
  - Implement adapter service(s), e.g. `LegacyMetadataAdapter`:
    - Depends on `mill-metadata-core` module.
    - Reads from `MetadataService` / new repository.
    - Maps `MetadataEntity` + facets into existing DTOs used by current callers.
  - Switch existing services/controllers in `mill-service-core` to call this adapter instead of the old storage.

- **Data migration (core metadata)**
  - Implement exporter from the old metadata storage into:
    - YAML files following the “complete YAML” format in `metadata-service-design.md`, or
    - Direct insertion into the new JPA schema (when Phase 5 is ready).
  - Run migration in a staging environment and validate:
    - Structural parity (same tables/columns/relations).
    - Descriptive fields (names, descriptions, tags).

- **Deliverables**
  - Existing metadata APIs backed by the new repository via adapters.
  - Old storage no longer used in normal flows (but still present as a safety net if needed).

---

## Phase 5 – Enrichments, Editing & JPA/Composite Persistence

**Objective:** Make the metadata system production-ready with editing, enrichments from NL2SQL, and robust persistence options.

- **JPA repository & database schema (in `mill-metadata-core/repository/jdbc/`)**
  - Implement JPA-backed `JpaMetadataRepository` in `repository/jdbc/` package using **document-style schema**:
    - **Single table**: `metadata_entity` with `facets` as JSONB column.
    - Structure: `{ "facetType": { "scope": {...}, "scope2": {...} } }`
    - No separate `metadata_facet` table - all facets in one JSON document per entity.
  - Add Flyway/Liquibase migrations for the new schema in `mill-metadata-core/src/main/resources/db/migration/`.
  - Implement JSONB indexes for efficient querying:
    - Full-text search on facets
    - Scope-based queries
    - Tag-based queries
  - Support configuration in `mill-metadata-core`:
    - `file`, `jpa`, `composite` storage modes via `application.yml` (default implementations).
    - Auto-configuration to select repository implementation based on config.

- **External metadata provider interface (in `mill-metadata-core/repository/provider/`)**
  - Define `ExternalMetadataProvider` interface for pluggable external metadata sources:
    - Methods: `fetchEntities()`, `fetchEntity()`, `search()`, `getCapabilities()`
    - Support for read-only and read-write providers
  - Define `MetadataEntityMapper` interface for mapping external formats to Mill facets.
  - Implement `MetadataProviderRegistry` for auto-discovery and registration of providers.
  - Implement `ExternalMetadataRepository` adapter that wraps external providers.
  - **Note**: Actual provider implementations (Collibra, Alation, etc.) can be added later as separate modules or plugins.

- **Composite repository & sync (in `mill-metadata-core/repository/composite/` and `service/`)**
  - Implement `CompositeMetadataRepository` in `repository/composite/` that merges:
    - Physical schema from `SchemaProvider` (JDBC/Calcite) - likely from `mill-service-core`.
    - Annotations from file/JPA metadata repository.
  - Implement `MetadataSyncService` in `service/` package with:
    - Manual sync endpoint (`/api/metadata/v1/catalogs/{catalog}/sync`).
    - Scheduled sync based on configuration.

- **Enrichments (in `ai/mill-ai-core/metadata/`)**
  - Implement `EnrichmentFacet` in `mill-ai-core/metadata/facets/` and register it.
  - Implement `EnrichmentService` in `mill-ai-core/metadata/service/`:
    - Capture enrichments from NL2SQL (enrich-model intent).
    - Approval workflow: PENDING → APPROVED/REJECTED.
    - Application of enrichments to relevant facets (descriptive, data quality, relations, concepts).
  - **No REST controllers in AI module** - keep it framework-agnostic.

- **Enrichment REST API (in `services/mill-metadata-service/api/`)**
  - Extend `AIMetadataController` in `mill-metadata-service/api/` with enrichment APIs:
    - `POST /api/metadata/v1/ai/entities/{entityId}/enrichments`
    - `POST /api/metadata/v1/ai/entities/{entityId}/enrichments/{id}/approve`
  - Controllers delegate to `EnrichmentService` from `mill-ai-core`.

- **UI editing**
  - Extend UI to:
    - Edit `DescriptiveFacet`, `RelationFacet`, `ConceptFacet`, and `ValueMappingFacet`.
    - Review and approve/reject enrichments in a “Pending Enrichments” panel.
  - Wire editing forms to:
    - `PUT /api/metadata/v1/entities/{entityId}/facets/{facetType}`.

- **Deliverables**
  - Database-backed metadata with composite option available.
  - Enrichment capture and approval end-to-end.
  - UI editing for key facets enabled in at least one environment.

---

## Phase 6 – Advanced Facets & Search

**Objective:** Leverage the faceted architecture for advanced capabilities (data quality, semantic search, lineage, etc.).

- **Data quality facet (in `ai/mill-ai-core/metadata/facets/`)**
  - Implement `DataQualityFacet` in `mill-ai-core/metadata/facets/` and rule execution engine:
    - Rule types: COMPLETENESS, VALIDITY, CONSISTENCY, UNIQUENESS, REFERENTIAL.
    - Execution scheduling and result storage.
  - Expose APIs in `AIMetadataController` (in `mill-metadata-service`) and minimal UI to view rule status and scores.

- **Semantic facet (in `ai/mill-ai-core/metadata/facets/`)**
  - Implement `SemanticFacet` in `mill-ai-core/metadata/facets/` for embeddings and semantic similarity.
  - Integrate with chosen vector store / search provider.
  - Use semantic metadata in:
    - NL2SQL table/column selection.
    - UI search/browse experiences.

- **Lineage facet (in `mill-metadata-core/domain/core/` or new `mill-metadata-lineage` module)**
  - Implement `LineageFacet` similar to the example in the design.
  - Provide lineage APIs in `mill-metadata-core/api/` for UI visualization:
    - Upstream/downstream graph for tables and attributes.

- **Search improvements (in `mill-metadata-core/service/`)**
  - Implement full-text and facet-aware search in `MetadataService` based on configured provider (Postgres/Elastic/Lucene).
  - Support tag-based navigation and statistics (e.g., counts per domain, classification).

- **Deliverables**
  - Extended metadata capabilities beyond the current system.
  - Demonstrated value in NL2SQL, UI navigation, and governance use cases.

---

## Migration & Rollout Principles

- **Feature flags everywhere**
  - UI: toggle between old and new explorer/search.
  - NL2SQL: toggle between legacy and facet-backed value resolution.
  - Storage: switch between file-only, JPA, and composite.

- **Adapters over big-bang**
  - Introduce adapters so existing APIs and callers can remain stable while implementations move to the new model.

- **Incremental data migration**
  - Start with a small dataset (e.g., moneta) for validation.
  - Gradually expand coverage and deprecate old storage once confidence is high.

- **Testing**
  - Unit and integration tests per module.
  - Parity tests (old vs new) for:
    - Metadata fetches.
    - Value mapping resolution.
    - NL2SQL flows on critical paths.
  - Scope resolution tests:
    - Merging facets (user > team > role > global)
    - Scope visibility filtering
    - User-specific facet creation/access


