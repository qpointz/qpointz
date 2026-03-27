# Metadata UI Implementation Plan

## Implementation Notes (March 2026 — Admin Facet Type Management)

**Status:** Delivered MVP for WI-095/WI-096 integration.

### Delivered

- ✅ Admin routing moved to wildcard form and supports two-level navigation:
  - `/admin/:group/:section/...`
- ✅ Admin sidebar now includes grouped navigation with `System` and `Model`.
- ✅ Feature flags implemented in code (not only docs):
  - `adminModelNavEnabled`
  - `adminFacetTypesEnabled`
  - `facetTypesReadOnly`
- ✅ New facet type management UI in `ui/mill-ui`:
  - list page: `/admin/model/facet-types`
  - create page: `/admin/model/facet-types/new`
  - edit page: `/admin/model/facet-types/:typeKey/edit`
- ✅ List supports:
  - quick search (`typeKey`, `title`, `description`)
  - two view modes (list/cards)
  - create/edit/delete actions
- ✅ Edit supports:
  - descriptor metadata editing
  - split payload schema editor (left tree / right node editor)
  - nested objects/arrays and enum editing via pills
  - expert JSON mode for full-manifest editing
- ✅ Read-only mode keeps actions visible but disabled with tooltip guidance.

### Current constraints / follow-up

- Schema validation strategy for metadata payload values is deferred to follow-up WIs.
- Advanced schema composition (`oneOf`/`anyOf`/`allOf`) is still out of scope.
- Drag-and-drop schema field ordering is not implemented yet (order is preserved as entered).

---

**Status:** Planning  
**Date:** January 2025  
**Branch:** `feat/metadata-ui`  
**Current Phase:** Phase 2 Complete → Phase 3 Starting

---

## Current Status

### ✅ Completed (Phase 1 & 2)

- **Phase 1 - Core Foundation** ✅
  - `mill-metadata-core` module with domain model, facets, file-based repository
  - Core facets: `StructuralFacet`, `DescriptiveFacet`, `RelationFacet`, `ConceptFacet`
  - `FacetRegistry` plugin system
  - File-based YAML persistence with scoped facets

- **Phase 2 - UI Read-Only + REST API** ✅
  - `mill-metadata-service` REST API module
  - Read-only endpoints: `/api/metadata/v1/explorer/tree`, `/api/metadata/v1/explorer/search`
  - Metadata browser UI at `/explore` route
  - Collapsible sidebar with schema tree navigation
  - Entity details view with facet tabs
  - URL routing: `/explore/:schema/:table/:attribute?`
  - OpenAPI-generated TypeScript client
  - React context provider (`MetadataProvider.tsx`)

### 🔄 Next Phase: Phase 3 - NL2SQL & Value Mapping Migration

---

## Phase 3 Implementation Plan

### Objective

Migrate NL2SQL value resolution from legacy `MetadataProvider`/`ValueMapper` to the new facet-based `ValueMappingFacet` system, enabling:
- Value mapping stored as facets on metadata entities
- Backward compatibility during migration
- Feature-flagged transition path
- Clean separation between legacy and new systems

### Prerequisites

- ✅ Phase 1 & 2 completed
- ✅ `mill-metadata-core` module available
- ✅ `mill-metadata-service` REST API available
- ✅ File-based repository working
- ⚠️ Legacy `MetadataProvider` still in use by AI components

---

## Implementation Tasks

### Task 1: Create ValueMappingFacet in mill-ai-core

**Purpose:** Define AI-specific facet for value mappings, demonstrating facet extensibility.

**Files to Create:**
- `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/metadata/ValueMappingFacet.java`
- `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/metadata/AiMetadataConfiguration.java`
- `ai/mill-ai-core/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

**Dependencies:**
- Add `mill-metadata-core` dependency to `ai/mill-ai-core/build.gradle.kts`

**Implementation Details:**
- Extend `AbstractFacet` from `mill-metadata-core`
- Support static mappings: `userTerm`, `databaseValue`, `aliases`, `description`, `language`
- Support SQL-based sources: `sql`, `enabled`, `cacheTtlSeconds`, `name`, `description`
- Return facet type: `"value-mapping"`
- Match structure from `FileRepository.ValueMapping` and `FileRepository.ValueMappingSource`
- Register facet with `FacetRegistry` in `AiMetadataConfiguration`

**Acceptance Criteria:**
- ✅ `ValueMappingFacet` compiles and registers successfully
- ✅ Facet can be serialized/deserialized to/from YAML
- ✅ Facet appears in `FacetRegistry` when `mill-ai-core` is on classpath
- ✅ Unit tests pass

**Estimated Effort:** 4-6 hours

---

### Task 2: Create MetadataAdapterService

**Purpose:** Provide backward-compatible adapter during migration period.

**Files to Create:**
- `core/mill-service-core/src/main/java/io/qpointz/mill/services/metadata/MetadataAdapterService.java`

**Implementation Details:**
- Implements `MetadataProvider` interface (for backward compatibility)
- Uses `MetadataService` from `mill-metadata-core` and `SchemaProvider` for structural info
- Methods to implement:
  - `getModel()`: Read from MODEL entity or DescriptiveFacet
  - `getSchemas()`: Query SCHEMA entities, enrich with SchemaProvider
  - `getTables()`: Query TABLE entities, enrich with SchemaProvider
  - `getRelations()`: Extract from RelationFacet on entities
  - `getTable()`: Find specific table entity
  - `getAllValueMappings()`: Extract from ValueMappingFacet on ATTRIBUTE entities
  - `getAllValueMappingSources()`: Extract SQL sources from ValueMappingFacet
- Create helper methods for entity-to-model conversion:
  - Convert `MetadataEntity` to `Model`, `Schema`, `Table`, `Attribute`, `Relation` records
  - Handle missing entities gracefully (fallback to SchemaProvider)

**Acceptance Criteria:**
- ✅ `MetadataAdapterService` implements `MetadataProvider` interface
- ✅ All methods delegate to `MetadataService` appropriately
- ✅ Value mappings extracted from `ValueMappingFacet`
- ✅ Unit tests verify adapter behavior matches legacy implementation
- ✅ Integration tests pass

**Estimated Effort:** 6-8 hours

---

### Task 3: Update Configuration

**Purpose:** Wire new adapter service and remove deprecated beans.

**Files to Modify:**
- `core/mill-service-core/src/main/java/io/qpointz/mill/services/metadata/configuration/MetadataConfiguration.java`

**Changes:**
- Replace `MetadataProviderImpl` bean with `MetadataAdapterService`
- Remove beans for:
  - `FileAnnotationsRepository`
  - `FileRelationsProvider`
  - `NoneAnnotationsRepository`
  - `NoneRelationsProvider`
- Keep `FileRepository` bean temporarily if needed for migration
- Ensure `MetadataService` and `MetadataRepository` are available (from `mill-metadata-core` auto-configuration)

**Configuration Properties:**
- Check `application.yml` for legacy `mill.metadata.file.repository.path`
- Ensure `mill.metadata.v2.*` properties are configured

**Acceptance Criteria:**
- ✅ `MetadataAdapterService` bean created and available
- ✅ Deprecated beans removed
- ✅ Application starts successfully
- ✅ Existing code using `MetadataProvider` continues to work

**Estimated Effort:** 2-3 hours

---

### Task 4: Migrate Value Mapping Data

**Purpose:** Convert existing value mapping YAML files to new facet format.

**Files to Create/Modify:**
- Migration script/tool (optional): `core/mill-service-core/src/main/java/io/qpointz/mill/services/metadata/migration/ValueMappingMigrator.java`
- Update existing YAML files or create new ones in facet format

**Migration Strategy:**
- Read existing `ValueMapper` configuration from YAML files
- Convert to `ValueMappingFacet` structure
- Write to file-based metadata repository (`moneta-meta-repository.yaml` or similar)
- Map structure:
  - Static mappings → `ValueMappingFacet.mappings[]`
  - SQL sources → `ValueMappingFacet.sources[]`
  - Attach facets to appropriate ATTRIBUTE entities

**Example Migration:**

**Before (legacy format):**
```yaml
# value-mappings.yml
mappings:
  - attribute: moneta.clients.segment
    mappings:
      - userTerm: premium
        databaseValue: PREMIUM
        language: en
```

**After (facet format):**
```yaml
# moneta-meta-repository.yaml
entities:
  - id: moneta.clients.segment
    type: ATTRIBUTE
    schemaName: moneta
    tableName: clients
    attributeName: segment
    facets:
      value-mapping:
        global:
          mappings:
            - userTerm: premium
              databaseValue: PREMIUM
              language: en
```

**Acceptance Criteria:**
- ✅ Value mappings migrated to facet format
- ✅ Data preserved correctly (no loss)
- ✅ Can be read via `MetadataService`
- ✅ NL2SQL can access mappings via adapter

**Estimated Effort:** 4-6 hours

---

### Task 5: Update AI Components (Gradual Migration)

**Purpose:** Update AI components to use new adapter, preparing for eventual direct `MetadataService` usage.

**Files to Review/Update:**
- `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/nlsql/components/ValueMappingComponents.java`
- `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/nlsql/IntentSpecs.java`
- `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/nlsql/MessageSpecs.java`
- `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/nlsql/messages/specs/SchemaMessageSpec.java` ⚠️ **CRITICAL**: Uses model classes
- `ai/mill-ai-nlsql-chat-service/src/main/java/io/qpointz/mill/ai/nlsql/components/ChatProcessor.java`
- `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/nlsql/ChatApplication.java`

**Strategy:**
- Components continue using `MetadataProvider` interface (no code changes needed initially)
- `MetadataAdapterService` provides same API, so components work transparently
- Later: Migrate to `MetadataService` directly (follow-up task)

**Acceptance Criteria:**
- ✅ All AI components compile and run
- ✅ Value mapping resolution works via adapter
- ✅ NL2SQL integration tests pass
- ✅ No breaking changes to existing functionality

**Estimated Effort:** 2-4 hours (mostly verification)

---

### Task 6: Update Tests

**Purpose:** Update test files to use new adapter or `MetadataService` directly.

**Files to Update:**
- `core/mill-service-core/src/test/java/io/qpointz/mill/services/metadata/impl/MetadataProviderImplTest.java`
- `ai/mill-ai-core/src/test/java/io/qpointz/mill/ai/nlsql/components/ValueMappingComponentsTest.java`
- All testIT files in `ai/mill-ai-core/src/testIT/java/io/qpointz/mill/ai/nlsql/`

**Changes:**
- Replace mocks/stubs of `MetadataProvider` with `MetadataAdapterService` or `MetadataService`
- Update test doubles to use new service
- Ensure test data uses facet format

**Acceptance Criteria:**
- ✅ All unit tests pass
- ✅ All integration tests pass
- ✅ Test coverage maintained or improved

**Estimated Effort:** 4-6 hours

---

### Task 7: Add Value Mapping REST API Endpoints

**Purpose:** Expose value mapping functionality via REST API for UI consumption.

**Files to Create/Modify:**
- `services/mill-metadata-service/src/main/java/io/qpointz/mill/metadata/api/AIMetadataController.java` (extend existing or create new)

**Endpoints to Add:**
```
GET    /api/metadata/v1/ai/entities/{entityId}/value-mappings
POST   /api/metadata/v1/ai/entities/{entityId}/value-mappings
GET    /api/metadata/v1/ai/entities/{entityId}/value-mappings/map?term={term}&language={lang}
```

**Implementation:**
- Delegate to `ValueMappingService` (to be created in `mill-ai-core`)
- Or directly use `MetadataService` to read `ValueMappingFacet`
- Return DTOs compatible with UI needs

**Acceptance Criteria:**
- ✅ REST endpoints available and documented (Swagger)
- ✅ Can read value mappings for an attribute
- ✅ Can resolve user term to database value
- ✅ UI can consume endpoints

**Estimated Effort:** 4-6 hours

---

### Task 8: Update UI to Display Value Mappings

**Purpose:** Show value mappings in metadata browser UI.

**Files to Modify:**
- `services/mill-grinder-ui/src/component/explore/EntityDetails.tsx`
- `services/mill-grinder-ui/src/component/explore/FacetViewer.tsx`

**Changes:**
- Add value mapping display in `FacetViewer` for `value-mapping` facet type
- Show mappings table: userTerm → databaseValue
- Show SQL sources if available
- Add UI for adding/editing mappings (if editing enabled)

**Acceptance Criteria:**
- ✅ Value mappings visible in entity details view
- ✅ Mappings displayed in readable format
- ✅ UI handles missing mappings gracefully

**Estimated Effort:** 4-6 hours

---

## Testing Strategy

### Unit Tests
- ✅ `ValueMappingFacet` serialization/deserialization
- ✅ `MetadataAdapterService` method implementations
- ✅ Value mapping extraction from facets
- ✅ Entity-to-model conversion helpers

### Integration Tests
- ✅ End-to-end value mapping resolution via adapter
- ✅ NL2SQL value mapping integration
- ✅ REST API endpoints for value mappings
- ✅ UI value mapping display

### Validation Tests
- ✅ Compare legacy vs new value mapping resolution (parity tests)
- ✅ Verify no data loss during migration
- ✅ Performance comparison (should be similar or better)

---

## Rollout Plan

### Phase 3a: Foundation (Tasks 1-3)
- Create `ValueMappingFacet` and register it
- Create `MetadataAdapterService`
- Update configuration
- **Goal:** Adapter available, no breaking changes

### Phase 3b: Data Migration (Task 4)
- Migrate value mapping data to facet format
- Verify data integrity
- **Goal:** Value mappings available in new format

### Phase 3c: Integration (Tasks 5-6)
- Update tests
- Verify AI components work with adapter
- **Goal:** All tests pass, NL2SQL works

### Phase 3d: API & UI (Tasks 7-8)
- Add REST API endpoints
- Update UI to display value mappings
- **Goal:** Value mappings visible and accessible via UI

---

## Success Criteria

1. ✅ `ValueMappingFacet` created and registered
2. ✅ `MetadataAdapterService` provides backward-compatible API
3. ✅ Value mapping data migrated to facet format
4. ✅ All existing tests pass
5. ✅ NL2SQL value resolution works via adapter
6. ✅ REST API exposes value mappings
7. ✅ UI displays value mappings
8. ✅ No breaking changes to existing functionality
9. ✅ Documentation updated

---

## Risks & Mitigations

### Risk 1: Breaking Changes During Migration
**Mitigation:** Use adapter pattern - existing code continues to work, gradual migration

### Risk 2: Data Loss During Migration
**Mitigation:** 
- Create migration script with validation
- Test on sample data first
- Keep legacy data as backup

### Risk 3: Performance Degradation
**Mitigation:**
- Profile adapter performance
- Cache frequently accessed data
- Optimize facet queries

### Risk 4: Model Classes Dependency
**Mitigation:**
- Keep model classes temporarily for `SchemaMessageSpec`
- Plan migration to `MetadataEntity` in follow-up phase

---

## Follow-Up Tasks (Post Phase 3)

1. **Remove Adapter Layer**
   - Migrate all usages from `MetadataAdapterService` to `MetadataService` directly
   - Remove `MetadataProvider` interface
   - Remove model classes after `SchemaMessageSpec` migration

2. **Migrate SchemaMessageSpec**
   - Update to use `MetadataEntity` directly
   - Remove dependency on model classes

3. **Add Value Mapping Editing UI**
   - Allow users to add/edit value mappings via UI
   - Support both static and SQL-based mappings

4. **Enrichment Facet Implementation**
   - Create `EnrichmentFacet` in `mill-ai-core`
   - Implement enrichment capture from NL2SQL sessions
   - Add approval workflow

---

## Estimated Timeline

- **Phase 3a (Foundation):** 1-2 days
- **Phase 3b (Data Migration):** 1 day
- **Phase 3c (Integration):** 1-2 days
- **Phase 3d (API & UI):** 1-2 days

**Total:** 4-7 days

---

## Dependencies

- ✅ `mill-metadata-core` module (Phase 1)
- ✅ `mill-metadata-service` REST API (Phase 2)
- ⚠️ Legacy `MetadataProvider` interface (to be deprecated)
- ⚠️ `SchemaProvider` (still needed for structural info)

---

## Related Documents

- [Metadata Implementation Roadmap](./metadata-implementation-roadmap.md)
- [Metadata Provider Refactoring Plan](./metadata-provider-refactoring-plan.md)
- [Metadata Service Design](./metadata-service-design.md)
- [Value Mapping Tactical Solution](./value-mapping-tactical-solution.md)

---

**Next Steps:**
1. Review and approve this plan
2. Start with Task 1: Create `ValueMappingFacet`
3. Proceed sequentially through tasks
4. Update plan as needed based on implementation findings
