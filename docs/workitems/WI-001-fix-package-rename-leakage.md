# WI-001: Fix Package Rename Leakage Outside Data Backend Modules

**Type:** refactoring
**Priority:** high
**Rules:** See [RULES.md](RULES.md)
**Branch name:** `fix/wi-001-package-rename-leakage`

---

## Problem

During the rename of `io.qpointz.mill.services` to `io.qpointz.mill.data.backend` inside
`data/mill-data-backends/` and `data/mill-data-backend-core/`, the rename leaked into
modules that are **not** part of the data backend. Classes in metadata, UI, core service API,
AI, apps, and clients now sit under `io.qpointz.mill.data.backend.*` even though they have
nothing to do with the data backend.

## Related Backlog Items

- R-1: Move services/mill-metadata-service to metadata/mill-metadata-service
- R-2: Remove Spring contamination from mill-metadata-core
- R-13: Reduce technical debt: review 119 files with TODOs/FIXMEs

## Scope

Restore correct package names for classes **outside** `data/mill-data-backends/` and
`data/mill-data-backend-core/`. The `io.qpointz.mill.data.backend` package is correct
inside those two modules — do not touch them.

## Affected Modules and Target Packages

### 1. `metadata/mill-metadata-provider/` (16 files)

**Current:** `io.qpointz.mill.data.backend.metadata.*`
**Target:** `io.qpointz.mill.metadata.*`

Files:
- `MetadataProvider.java` -> `io.qpointz.mill.metadata`
- `RelationsProvider.java` -> `io.qpointz.mill.metadata`
- `AnnotationsRepository.java` -> `io.qpointz.mill.metadata`
- `impl/MetadataProviderImpl.java` -> `io.qpointz.mill.metadata.impl`
- `impl/NoneAnnotationsRepository.java` -> `io.qpointz.mill.metadata.impl`
- `impl/NoneRelationsProvider.java` -> `io.qpointz.mill.metadata.impl`
- `impl/file/FileRepository.java` -> `io.qpointz.mill.metadata.impl.file`
- `impl/file/FileRelationsProvider.java` -> `io.qpointz.mill.metadata.impl.file`
- `impl/file/FileAnnotationsRepository.java` -> `io.qpointz.mill.metadata.impl.file`
- `impl/v2/MetadataV2RelationsProvider.java` -> `io.qpointz.mill.metadata.impl.v2`
- `impl/v2/MetadataV2AnniotationsProvider.java` -> `io.qpointz.mill.metadata.impl.v2`
- `model/Model.java` -> `io.qpointz.mill.metadata.model`
- `model/Schema.java` -> `io.qpointz.mill.metadata.model`
- `model/Table.java` -> `io.qpointz.mill.metadata.model`
- `model/Attribute.java` -> `io.qpointz.mill.metadata.model`
- `model/Relation.java` -> `io.qpointz.mill.metadata.model`
- `model/ValueMapping.java` -> `io.qpointz.mill.metadata.model`

Tests (5 files): update package declarations and imports accordingly.

### 2. `core/mill-service-api/` (4 files)

**Current:** `io.qpointz.mill.data.backend.descriptors.*` / `io.qpointz.mill.data.backend.annotations.*`
**Target:** `io.qpointz.mill.service.descriptors` / `io.qpointz.mill.service.annotations`

Files:
- `descriptors/ServiceDescriptor.java` -> `io.qpointz.mill.service.descriptors`
- `descriptors/SecurityDescriptor.java` -> `io.qpointz.mill.service.descriptors`
- `annotations/OnServiceEnabledCondition.java` -> `io.qpointz.mill.service.annotations`
- `annotations/ConditionalOnService.java` -> `io.qpointz.mill.service.annotations`

### 3. `core/mill-well-known-service/` (6 files)

**Current:** `io.qpointz.mill.data.backend.descriptors.*` / `io.qpointz.mill.data.backend.controllers.*` / `io.qpointz.mill.data.backend.configuration.security.*`
**Target:** `io.qpointz.mill.service.descriptors` / `io.qpointz.mill.service.controllers` / `io.qpointz.mill.service.configuration.security`

Files:
- `descriptors/ApplicationDescriptor.java` -> `io.qpointz.mill.service.descriptors`
- `controllers/ApplicationDescriptorController.java` -> `io.qpointz.mill.service.controllers`

Tests (4 files): update package declarations and imports accordingly.

### 4. `ui/mill-grinder-service/` (3 files)

**Current:** `io.qpointz.mill.data.backend.grinder.filters.*`
**Target:** `io.qpointz.mill.ui.grinder.filters`

Files:
- `GrinderUIFilter.java` -> `io.qpointz.mill.ui.grinder.filters`
- `GrinderUIWebConfig.java` -> `io.qpointz.mill.ui.grinder.filters`

Tests (1 file): update package declaration and imports.

### 5. `apps/mill-service/` (2 files)

**Current:** `io.qpointz.mill.data.backend.*`
**Target:** `io.qpointz.mill.app`

Files:
- `MillService.java` -> `io.qpointz.mill.app`
- `PropertyLogger.java` -> `io.qpointz.mill.app`

### 6. `data/mill-data-autoconfigure/` (partial — metadata config only)

**Current:** `io.qpointz.mill.data.backend.metadata.configuration.*`
**Target:** `io.qpointz.mill.metadata.configuration`

Files:
- `MetadataConfiguration.java` -> `io.qpointz.mill.metadata.configuration`

Note: other files in this module (`DefaultServiceConfiguration`, `PolicyConfiguration`,
`DefaultFilterChainConfiguration`, `SecurityContextSecurityProvider`,
`LegacyAutoconfiguration`, and the `autoconfigure.data.backend.*` autoconfig classes) are
correctly placed — they are data backend wiring. Do not move them.

## Import-Only Updates (no package move, just fix imports)

These modules import from the affected classes above. After the package moves, their
`import` statements need updating. No directory/file moves required.

### 7. `ai/mill-ai-v1-core/` (17 files)

Imports `MetadataProvider`, `DataOperationDispatcher`, `ServiceHandler`, etc.
Update imports to new locations.

### 8. `ai/mill-ai-v1-nlsql-chat-service/` (6 files)

Imports from metadata and data operation classes. Update imports.

### 9. `clients/mill-jdbc-driver/` (2 test files)

Imports `ServiceHandler`, `ConditionalOnService`. Update imports.

### 10. `core/mill-test-kit/` (1 file)

Imports `PlanRewriter`. Update import.

## Verification

After all renames:

1. `./gradlew build` from each affected module root must succeed.
2. `./gradlew test` must pass in all affected modules.
3. No class under any module outside `data/mill-data-backends/` or
   `data/mill-data-backend-core/` should have `io.qpointz.mill.data.backend` in its
   package declaration, except for `data/` submodules where it is correct:
   - `data/services/mill-data-grpc-service/` — OK
   - `data/services/mill-data-http-service/` — OK
   - `data/mill-data-autoconfigure/` — OK (except `MetadataConfiguration`, moved above)
   - `data/mill-data-testkit/` — OK

## Estimated Effort

Medium — mostly mechanical file moves and import updates, but touches many modules so
careful testing is required.
