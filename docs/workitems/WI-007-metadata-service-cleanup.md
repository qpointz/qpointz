# WI-007: Metadata Service Layer Cleanup

**Type:** refactoring
**Priority:** medium
**Rules:** See [RULES.md](RULES.md)
**Branch name:** `refactor/wi-007-metadata-service-cleanup`
**Depends on:** WI-005

---

## Goal

Ensure the metadata module (`mill-metadata-core`, `mill-metadata-service`,
`mill-metadata-autoconfigure`) is context-free — it manages metadata entities and
facets without imposing any interpretation about schemas, tables, lineage, or other
data-layer concepts. Higher-level concerns that consume metadata belong in
higher-level modules.

---

## Problem: `SchemaExplorerController` in Wrong Module

`SchemaExplorerController` in `metadata/mill-metadata-service` is a consumer of
metadata, not a manager of it. It:

- Builds a **hierarchical tree** (schema → table → attribute) — this imposes a
  relational data model interpretation on metadata entities.
- Performs **text search** across entities with hardcoded knowledge of the
  `descriptive` facet structure (reaches into `displayName`, `description` fields).
- Exposes a **lineage** endpoint — squarely a data-platform feature.

The metadata module should be agnostic to how metadata is consumed. Tree views,
search, and lineage belong in a higher-level module where the data-layer context
is established.

---

## Files to Relocate

From `metadata/mill-metadata-service/src/main/java/io/qpointz/mill/metadata/api/`:

| File | Destination |
|------|-------------|
| `SchemaExplorerController.java` | Higher-level service (e.g. `data/` module or grinder) |
| `dto/TreeNodeDto.java` | Moves with the controller |
| `dto/SearchResultDto.java` | Moves with the controller |

## Files That Stay

| File | Reason |
|------|--------|
| `MetadataController.java` | Context-free entity/facet CRUD |
| `FacetController.java` | Context-free scope-aware facet reads |
| `DtoMapper.java` | Used by `MetadataController` |
| `dto/MetadataEntityDto.java` | Context-free DTO |
| `dto/FacetDto.java` | Context-free DTO |

---

## Deleted Files (Already Removed)

- `MetadataServiceApplication.java` — standalone Spring Boot entry point. The
  metadata REST API is embedded in host services, not run standalone.
- `OpenApiConfig.java` — removed; OpenAPI configuration belongs to the host
  service.

---

## Steps

1. Identify the correct destination module for `SchemaExplorerController`.
   Candidates: `data/mill-data-service`, grinder service, or a new
   `data/mill-data-explorer` module.
2. Move `SchemaExplorerController.java`, `TreeNodeDto.java`, `SearchResultDto.java`
   to the destination.
3. Update package declarations and imports.
4. Add `mill-metadata-core` dependency to the destination module (if not already
   present) so it can use `MetadataService`.
5. Remove the moved files from `mill-metadata-service`.
6. Verify `mill-metadata-service` compiles without the moved files.
7. Verify the destination module compiles and tests pass.

---

## Verification

1. `metadata/mill-metadata-service` contains only context-free controllers
   (`MetadataController`, `FacetController`) and their DTOs.
2. `SchemaExplorerController` works from its new location (same endpoints,
   same behavior).
3. `./gradlew build` passes for both the metadata and destination modules.
4. No references to `TreeNodeDto` or `SearchResultDto` remain in the metadata
   module.

## Estimated Effort

Small — mechanical file move, package rename, dependency wiring. No logic changes.
