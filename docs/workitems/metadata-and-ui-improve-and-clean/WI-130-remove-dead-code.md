# WI-130 — Remove dead code from metadata module

**Story:** metadata-and-ui-improve-and-clean
**Status:** Pending

## Objective

Remove classes from the metadata module that have zero production consumers. This is a safe
preparatory cleanup before the larger facet class demotion work.

## Scope

### Tier 1 — Completely unused (zero code references outside defining file)

| Class | Module / Path |
|-------|---------------|
| `FacetTypeConflictException` | `mill-metadata-core` / `domain/facet/exceptions/FacetTypeManifestExceptions.kt` |
| `FacetTypeNotFoundException` | `mill-metadata-core` / `domain/facet/exceptions/FacetTypeManifestExceptions.kt` |
| `FacetTypeDescriptorDto` | `mill-metadata-service` / `api/dto/FacetTypeDescriptorDto.kt` |
| `FacetDto` | `mill-metadata-service` / `api/dto/FacetDto.kt` |

### Tier 2 — Dead clusters (only reference each other, never consumed externally)

**Snapshot service cluster:**

| Class | Module / Path |
|-------|---------------|
| `MetadataView` | `mill-metadata-core` / `service/MetadataView.kt` |
| `MetadataSnapshotService` | `mill-metadata-core` / `service/MetadataSnapshotService.kt` |
| `DefaultMetadataSnapshotService` | `mill-metadata-core` / `service/DefaultMetadataSnapshotService.kt` |

**ResourceResolver cluster:**

| Class | Module / Path |
|-------|---------------|
| `ResourceResolver` | `mill-metadata-core` / `repository/file/ResourceResolver.kt` |
| `SpringResourceResolver` | `mill-metadata-autoconfigure` / `repository/file/SpringResourceResolver.kt` |
| `ClasspathResourceResolver` | `mill-metadata-core` / `test/.../ClasspathResourceResolver.kt` |

### Tier 3 — Orphan (no production consumers)

| Class | Module / Path |
|-------|---------------|
| `PlatformFacetTypeDefinitions` | `mill-metadata-core` / `domain/facet/PlatformFacetTypeDefinitions.kt` |

### Tier 4 — Used only by retired legacy code (`mill-grinder-ui`)

| Class | Module / Path |
|-------|---------------|
| `TreeNodeDto` | `mill-metadata-service` / `api/dto/TreeNodeDto.kt` |
| `SearchResultDto` | `mill-metadata-service` / `api/dto/SearchResultDto.kt` |

## Verification

- `./gradlew :metadata:mill-metadata-core:test`
- `./gradlew :metadata:mill-metadata-service:test`
- `./gradlew :metadata:mill-metadata-persistence:testIT`
- Full `./gradlew build` to confirm no compile errors across dependent modules

## Notes

- `FacetPayloadFieldJsonSerde.kt` contains `FacetPayloadFieldDeserializer` and
  `FacetPayloadFieldSerializer` — these ARE used via Jackson annotations on `FacetPayloadSchema`.
  Only the **file name** is misleading; the classes inside are live. Do not remove.
- `FacetTypeManifestInvalidException` (same file as the two unused exceptions) IS used by
  `FacetTypeManifestNormalizer`. Keep it; only remove the two unused siblings.
