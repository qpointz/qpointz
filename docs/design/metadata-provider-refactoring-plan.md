# MetadataProvider Refactoring Plan

**Status:** Planning  
**Date:** January 2025  
**Purpose:** Refactor legacy `MetadataProvider` to use new facet-based metadata system from `mill-metadata-core`

---

## Overview

This plan outlines the migration from the legacy `MetadataProvider` interface and its implementation to the new facet-based metadata system in `mill-metadata-core`. The refactoring will:

1. Replace `MetadataProvider` with `MetadataService` from `mill-metadata-core`
2. Create `ValueMappingFacet` in `mill-ai-core` to demonstrate facet extensibility
3. Create an adapter service for backward compatibility during migration
4. Safely remove deprecated classes after migration
5. Update all usages across AI components and tests

---

## Current State Analysis

### Legacy Components

**Core Interface:**
- `MetadataProvider` (`core/mill-service-core/src/main/java/io/qpointz/mill/services/metadata/MetadataProvider.java`)
  - Provides: `getModel()`, `getSchemas()`, `getTables()`, `getRelations()`, `getTable()`
  - Value mapping methods: `getAllValueMappings()`, `getAllValueMappingSources()`
  - Contains nested records: `ValueMappingWithContext`, `ValueMappingSourceWithContext`

**Implementation:**
- `MetadataProviderImpl` - Uses `SchemaProvider`, `AnnotationsRepository`, `RelationsProvider`

**Supporting Classes (to be removed):**
- `AnnotationsRepository` - Only used by `MetadataProviderImpl` and implementations
- `RelationsProvider` - Only used by `MetadataProviderImpl` and implementations
- `FileRepository` - Used by file-based implementations
- Model classes: `Model`, `Schema`, `Table`, `Attribute`, `Relation` - Used by `MetadataProvider` and `SchemaMessageSpec`

**Usages:** 31+ files across:
- AI components: `ValueMappingComponents`, `IntentSpecs`, `MessageSpecs`, `SchemaMessageSpec`, `ChatProcessor`, `ChatApplication`
- Test files: Multiple test and testIT files
- Configuration: `MetadataConfiguration`

### New State (mill-metadata-core)

**Core Services:**
- `MetadataService` - Service layer for entity and facet operations
- `MetadataRepository` - Repository interface for entity persistence
- `MetadataEntity` - Document-style entity with scoped facets

**Existing Facets:**
- `DescriptiveFacet`, `StructuralFacet`, `RelationFacet`, `ConceptFacet`

**Missing:**
- `ValueMappingFacet` - Needs to be created (will be in `mill-ai-core`)

---

## Implementation Plan

### Phase 1: Create Value Mapping Facet in mill-ai-core

**Decision:** Create `ValueMappingFacet` in `mill-ai-core` module to:
- Demonstrate facet extensibility from other modules
- Keep AI-specific facets in the AI module
- Showcase how modules can extend metadata with domain-specific facets

**Tasks:**

1. **Add dependency** on `mill-metadata-core` to `ai/mill-ai-core/build.gradle.kts`
   ```kotlin
   api("io.qpointz.mill:mill-metadata-core")
   ```

2. **Create ValueMappingFacet** in `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/metadata/ValueMappingFacet.java`
   - Package: `io.qpointz.mill.ai.metadata`
   - Extend `AbstractFacet` from `mill-metadata-core`
   - Support static mappings: `userTerm`, `databaseValue`, `aliases`, `description`, `language`
   - Support SQL-based sources: `sql`, `enabled`, `cacheTtlSeconds`, `name`, `description`
   - Return facet type: `"value-mapping"`
   - Match structure from `FileRepository.ValueMapping` and `FileRepository.ValueMappingSource`

3. **Create configuration class** `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/metadata/AiMetadataConfiguration.java`
   - Use `@Configuration` and `@PostConstruct`
   - Register `ValueMappingFacet.class` with `FacetRegistry.getInstance()`
   - Log registration for visibility

4. **Optional: Auto-configuration** (recommended)
   - Create `ai/mill-ai-core/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
   - Add: `io.qpointz.mill.ai.metadata.AiMetadataConfiguration`
   - Ensures facet is registered automatically when `mill-ai-core` is on classpath

---

### Phase 2: Create Metadata Adapter Service

**Purpose:** Provide backward-compatible adapter during migration period

**Tasks:**

1. **Create MetadataAdapterService** in `core/mill-service-core/src/main/java/io/qpointz/mill/services/metadata/MetadataAdapterService.java`
   - Implements `MetadataProvider` interface (for backward compatibility)
   - Uses `MetadataService` and `SchemaProvider` to provide same API
   - Methods:
     - `getModel()`: Read from MODEL entity or DescriptiveFacet
     - `getSchemas()`: Query SCHEMA entities, enrich with SchemaProvider
     - `getTables()`: Query TABLE entities, enrich with SchemaProvider
     - `getRelations()`: Extract from RelationFacet on entities
     - `getTable()`: Find specific table entity
     - `getAllValueMappings()`: Extract from ValueMappingFacet on ATTRIBUTE entities
     - `getAllValueMappingSources()`: Extract SQL sources from ValueMappingFacet

2. **Create helper methods** for entity-to-model conversion
   - Convert `MetadataEntity` to `Model`, `Schema`, `Table`, `Attribute`, `Relation` records
   - Handle missing entities gracefully (fallback to SchemaProvider)
   - Support `SchemaMessageSpec` migration path

---

### Phase 3: Update Configuration

**Tasks:**

1. **Update MetadataConfiguration** (`core/mill-service-core/src/main/java/io/qpointz/mill/services/metadata/configuration/MetadataConfiguration.java`)
   - Replace `MetadataProviderImpl` bean with `MetadataAdapterService`
   - Remove beans for: `FileAnnotationsRepository`, `FileRelationsProvider`, `NoneAnnotationsRepository`, `NoneRelationsProvider`
   - Keep `FileRepository` bean temporarily if needed for migration, or remove if fully migrated
   - Ensure `MetadataService` and `MetadataRepository` are available (from mill-metadata-core auto-configuration)

2. **Check application properties**
   - Remove `mill.metadata.file.repository.path` if no longer needed
   - Ensure `mill.metadata.v2.*` properties are configured for new system

---

### Phase 4: Update All Usages

**Tasks:**

1. **Update AI components** to use `MetadataAdapterService`:
   - `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/nlsql/components/ValueMappingComponents.java`
   - `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/nlsql/IntentSpecs.java`
   - `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/nlsql/MessageSpecs.java`
   - `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/nlsql/messages/specs/SchemaMessageSpec.java` ⚠️ **CRITICAL**: Uses model classes
   - `ai/mill-ai-nlsql-chat-service/src/main/java/io/qpointz/mill/ai/nlsql/components/ChatProcessor.java`
   - `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/nlsql/ChatApplication.java`

2. **Update test files** (replace mocks/stubs):
   - `core/mill-service-core/src/test/java/io/qpointz/mill/services/metadata/impl/MetadataProviderImplTest.java`
   - `ai/mill-ai-core/src/test/java/io/qpointz/mill/ai/nlsql/components/ValueMappingComponentsTest.java`
   - All testIT files in `ai/mill-ai-core/src/testIT/java/io/qpointz/mill/ai/nlsql/`
   - Create test doubles using `MetadataService` instead of `MetadataProvider`

---

### Phase 5: Analyze and Safely Remove Deprecated Classes

**Safe Deletion Order:**

#### Step 1: Remove Repository Interfaces and Implementations (SAFE)
These are only used by `MetadataProvider` ecosystem:
- ✅ `core/mill-service-core/src/main/java/io/qpointz/mill/services/metadata/AnnotationsRepository.java`
- ✅ `core/mill-service-core/src/main/java/io/qpointz/mill/services/metadata/RelationsProvider.java`
- ✅ `core/mill-service-core/src/main/java/io/qpointz/mill/services/metadata/impl/file/FileAnnotationsRepository.java`
- ✅ `core/mill-service-core/src/main/java/io/qpointz/mill/services/metadata/impl/file/FileRelationsProvider.java`
- ✅ `core/mill-service-core/src/main/java/io/qpointz/mill/services/metadata/impl/NoneAnnotationsRepository.java`
- ✅ `core/mill-service-core/src/main/java/io/qpointz/mill/services/metadata/impl/NoneRelationsProvider.java`

#### Step 2: Remove FileRepository (SAFE after migration)
- ✅ `core/mill-service-core/src/main/java/io/qpointz/mill/services/metadata/impl/file/FileRepository.java`
  - Only used by `FileAnnotationsRepository`, `FileRelationsProvider`, and tests
  - Safe after migration to new metadata system

#### Step 3: Remove Model Classes (REQUIRES MIGRATION)
These are used by `SchemaMessageSpec` - must migrate first:
- ⚠️ `core/mill-service-core/src/main/java/io/qpointz/mill/services/metadata/model/Model.java` - Only in `MetadataProvider` (SAFE)
- ⚠️ `core/mill-service-core/src/main/java/io/qpointz/mill/services/metadata/model/Schema.java` - Used in `SchemaMessageSpec` (NEEDS MIGRATION)
- ⚠️ `core/mill-service-core/src/main/java/io/qpointz/mill/services/metadata/model/Table.java` - Used in `SchemaMessageSpec` (NEEDS MIGRATION)
- ⚠️ `core/mill-service-core/src/main/java/io/qpointz/mill/services/metadata/model/Attribute.java` - Used in `SchemaMessageSpec` (NEEDS MIGRATION)
- ⚠️ `core/mill-service-core/src/main/java/io/qpointz/mill/services/metadata/model/Relation.java` - Used in `SchemaMessageSpec` (NEEDS MIGRATION)

**Migration Strategy for Model Classes:**
- Option A: Update `SchemaMessageSpec` to use `MetadataEntity` and facets directly
- Option B: Keep model classes temporarily and use adapter's conversion methods
- **Recommendation:** Use Option B during transition, then Option A in follow-up

#### Step 4: Remove MetadataProvider (LAST)
- ✅ `core/mill-service-core/src/main/java/io/qpointz/mill/services/metadata/MetadataProvider.java`
- ✅ `core/mill-service-core/src/main/java/io/qpointz/mill/services/metadata/impl/MetadataProviderImpl.java`

#### Step 5: Remove Test Files
- ✅ `core/mill-service-core/src/test/java/io/qpointz/mill/services/metadata/impl/MetadataProviderImplTest.java`
- ✅ `core/mill-service-core/src/test/java/io/qpointz/mill/services/metadata/impl/file/ValueMappingTest.java`
- ✅ `core/mill-service-core/src/test/java/io/qpointz/mill/services/metadata/impl/FileAnnotationsRepositoryTest.java`

---

### Phase 6: Clean Up Configuration

**Tasks:**

1. **Final MetadataConfiguration cleanup**
   - Remove all deprecated bean definitions
   - Keep only `MetadataAdapterService` bean (or remove if all usages migrated to `MetadataService`)

2. **Verify application properties**
   - Remove legacy `mill.metadata.*` properties
   - Ensure `mill.metadata.v2.*` properties are documented

---

## Migration Strategy

### Option A: Adapter Pattern (Recommended)

**Approach:**
- Create `MetadataAdapterService` implementing `MetadataProvider` interface
- Allows gradual migration without breaking existing code
- Eventually remove adapter and migrate all usages to `MetadataService` directly

**Benefits:**
- Non-breaking migration
- Can migrate incrementally
- Easy rollback if needed

**Timeline:**
- Phase 1-4: Use adapter
- Follow-up: Remove adapter, migrate to `MetadataService` directly

### Option B: Direct Migration

**Approach:**
- Replace all `MetadataProvider` usages with `MetadataService` directly
- More invasive but cleaner long-term

**Benefits:**
- Cleaner architecture
- No adapter layer

**Drawbacks:**
- Requires updating all call sites simultaneously
- Higher risk

**Recommendation:** Use Option A (Adapter) for Phase 1-4, then Option B in a follow-up to remove the adapter.

---

## Key Considerations

### 1. Value Mapping Migration
- Static mappings and SQL sources need to be migrated from YAML files to `ValueMappingFacet` on ATTRIBUTE entities
- Migration script or tool may be needed to convert existing YAML to new format

### 2. SchemaProvider Dependency
- `MetadataAdapterService` will still need `SchemaProvider` for structural information until fully migrated
- This is acceptable during transition period

### 3. Backward Compatibility
- Adapter ensures existing code continues to work during transition
- Model classes can be kept temporarily for `SchemaMessageSpec` compatibility

### 4. Model Classes Migration
- `SchemaMessageSpec` uses model classes (`Schema`, `Table`, `Attribute`, `Relation`)
- Must either:
  - Migrate `SchemaMessageSpec` to use `MetadataEntity` directly, OR
  - Keep model classes temporarily and use adapter's conversion methods

### 5. Facet Definition Location
- **Decision:** `ValueMappingFacet` will be defined in `mill-ai-core` to demonstrate facet extensibility
- Requires adding `mill-metadata-core` dependency to `mill-ai-core`
- Creates configuration class to register the facet
- Showcases how modules can extend metadata with domain-specific facets

### 6. Test Coverage
- Ensure all tests pass after migration, especially integration tests
- Update test doubles to use `MetadataService` instead of `MetadataProvider`

### 7. Frontend
- `services/mill-grinder-ui/src/component/explore/MetadataProvider.tsx` is a React component (different namespace)
- **No changes needed** - this is unrelated to Java `MetadataProvider`

### 8. Safe Deletion Order
1. **First:** Remove `AnnotationsRepository`, `RelationsProvider`, and their implementations (only used by `MetadataProvider`)
2. **Second:** Remove `FileRepository` (only used by file-based implementations)
3. **Third:** Remove model classes after `SchemaMessageSpec` migration
4. **Last:** Remove `MetadataProvider` interface and implementation

---

## Files Summary

### Create:
- `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/metadata/ValueMappingFacet.java`
- `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/metadata/AiMetadataConfiguration.java`
- `ai/mill-ai-core/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` (optional)
- `core/mill-service-core/src/main/java/io/qpointz/mill/services/metadata/MetadataAdapterService.java`

### Modify:
- `ai/mill-ai-core/build.gradle.kts` (add `mill-metadata-core` dependency)
- `core/mill-service-core/src/main/java/io/qpointz/mill/services/metadata/configuration/MetadataConfiguration.java` (replace beans)
- All files importing `MetadataProvider` (31+ files)

### Delete:
- 15+ deprecated class files
- 3+ deprecated test files

---

## Success Criteria

1. ✅ All existing tests pass
2. ✅ `ValueMappingFacet` is registered and accessible via `MetadataService`
3. ✅ `MetadataAdapterService` provides same API as `MetadataProvider`
4. ✅ All AI components work with new adapter
5. ✅ Deprecated classes safely removed
6. ✅ Configuration cleaned up
7. ✅ Documentation updated

---

## Next Steps

After completing this refactoring:

1. **Follow-up:** Remove `MetadataAdapterService` and migrate all usages to `MetadataService` directly
2. **Follow-up:** Migrate `SchemaMessageSpec` to use `MetadataEntity` directly
3. **Follow-up:** Remove model classes after full migration
4. **Enhancement:** Add migration tool to convert legacy YAML to new facet format

---

## Related Documents

- [Metadata Implementation Roadmap](./metadata-implementation-roadmap.md)
- [Metadata Service Design](./metadata-service-design.md)
- [Value Mapping Tactical Solution](./value-mapping-tactical-solution.md)

