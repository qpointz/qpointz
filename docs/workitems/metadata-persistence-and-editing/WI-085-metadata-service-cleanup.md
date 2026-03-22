# WI-085 — Metadata Service API Cleanup

Status: `planned`
Type: `🔧 cleanup`
Area: `metadata`, `platform`

## Goal

Drop the `mill.metadata.v2.*` configuration prefix and the old `/api/metadata/v1/**` URL routing.
After this WI there is one metadata implementation with no version suffix in config keys or code.

**The file-backed and in-memory repository implementations are kept.** They remain the default
storage mode (`mill.metadata.storage.type=file`). Only the version labels (`v1`, `v2`) are
removed — not the repositories themselves.

---

## Background

The metadata service went through two historical redesigns:

**Original v1** — annotation/relation-based (`AnnotationsProvider`, `RelationsProvider`,
`mill-metadata-provider` module) bound metadata to the physical schema. This implementation
has **already been deleted** from the codebase (verified — no files remain). No action needed.

**Current (formerly v2)** — facet-based implementation in `metadata/mill-metadata-core/` with
file-backed and in-memory repositories. This is what remains, and it keeps its repositories.
The only leftover from the v1→v2 transition is the version labelling:
- Config prefix `mill.metadata.v2.*` — rename to `mill.metadata.*`
- Old REST URL paths `/api/metadata/v1/**` — deleted in WI-086

What does **not** change:
- `FileMetadataRepository` and its Spring wiring — retained as the default `file` storage mode
- `InMemoryFacetTypeRepository` — retained as the in-memory fallback
- `mill.metadata.storage.type=file` default behaviour

---

## Changes

### `MetadataProperties.kt` (Java rewrite — see Implementation Standards)

Change `@ConfigurationProperties` prefix from `mill.metadata.v2` to `mill.metadata`:

```java
@ConfigurationProperties(prefix = "mill.metadata")
public class MetadataProperties {
    private Storage storage = new Storage();
    private File file = new File();
    private String importOnStartup;   // added for WI-086 startup import

    public static class Storage {
        private String type = "file";
        // getters/setters
    }
    public static class File {
        private String path = "classpath:metadata/example.yml";
        private boolean watch = false;
        // getters/setters
    }
    // getters/setters + JavaDoc on every field
}
```

`importOnStartup` field added here (used by `MetadataImportExportAutoConfiguration` in WI-086).

### `MetadataRepositoryAutoConfiguration.kt`

Change `@ConditionalOnProperty` prefix from `mill.metadata.v2.storage` to `mill.metadata.storage`:

```kotlin
@ConditionalOnProperty(
    prefix = "mill.metadata.storage",
    name = ["type"],
    havingValue = "file",
    matchIfMissing = true
)
```

Remove the `v2` doc comment on the class.

### Integration tests in `mill-metadata-core/src/testIT/`

Update all `@TestPropertySource` annotations:

| Before | After |
|--------|-------|
| `mill.metadata.v2.storage.type=file` | `mill.metadata.storage.type=file` |
| `mill.metadata.v2.file.path=classpath:...` | `mill.metadata.file.path=classpath:...` |

Affected files:
- `MetadataAutoConfigurationIT.java`
- `MetadataAutoConfigurationDisabledIT.java`
- `MetadataAutoConfigurationDefaultIT.java`

### `data/mill-data-schema-core/src/testIT/` — `SchemaFacetServiceSkyMillIT.kt`

This test references `mill.metadata.v2.*` properties — update to `mill.metadata.*`.

### Any `application.yml` / `application.properties` files

Grep the repo for `mill.metadata.v2` and update every occurrence to `mill.metadata`.

---

## V1 Artifact Inventory — Deferred to WI-086

The following files are the complete set of v1 artifacts identified in the codebase.
They are **not** touched in WI-085 — WI-086 deletes or replaces each one.
This list is the authoritative checklist for WI-086 to verify nothing is left behind.

### Production controllers (`metadata/mill-metadata-service/src/main/kotlin/…/api/`)

| File | v1 reference | WI-086 action |
|------|-------------|---------------|
| `MetadataController.kt` | `@RequestMapping("/api/metadata/v1")` | Delete — replaced by `MetadataEntityController` |
| `FacetController.kt` | `@RequestMapping("/api/metadata/v1/facets")` | Delete — merged into `MetadataEntityController` |
| `FacetTypeController.kt` | `@RequestMapping("/api/metadata/v1/facet-types")` | Delete — replaced by `MetadataFacetController` |
| `SchemaExplorerController.kt` | `@RequestMapping("/api/metadata/v1/explorer")` | Delete — schema browsing deferred to future schema service WI |

### Tests (`metadata/mill-metadata-service/src/test/kotlin/…/api/`)

| File | v1 reference | WI-086 action |
|------|-------------|---------------|
| `MetadataControllerTest.kt` | `get("/api/metadata/v1/entities/…")` | Delete — replaced by `MetadataEntityControllerTest` |

### Retired UI (`ui/mill-grinder-ui/src/api/mill/api.ts`)

| File | v1 reference | Action |
|------|-------------|--------|
| `api.ts` | Generated OpenAPI client calling `/api/metadata/v1/**` | No action needed — `mill-grinder-ui` is retired |

---

## Traces to Verify (checklist)

Run before closing:
```bash
grep -r "mill\.metadata\.v2" --include="*.kt" --include="*.java" --include="*.yml" --include="*.yaml" --include="*.properties" .
grep -r "metadata/v1" --include="*.kt" --include="*.java" .
```
Both must return zero matches after WI-085 + WI-086 are applied.

---

## Tests

No new test files. Update the three existing integration tests listed above. All must pass:
```
./gradlew :metadata:mill-metadata-core:testIT
./gradlew :metadata:mill-metadata-autoconfigure:testIT
./gradlew :data:mill-data-schema-core:testIT
```

---

## Acceptance Criteria

- `mill.metadata.v2.*` prefix is gone from all source files.
- `mill.metadata.*` is the single config namespace for metadata.
- Grep for `mill\.metadata\.v2` returns zero matches.
- All updated integration tests pass.
