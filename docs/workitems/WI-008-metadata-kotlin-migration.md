# WI-008: Migrate Metadata Modules to Kotlin

**Type:** refactoring
**Priority:** medium
**Rules:** See [RULES.md](RULES.md)
**Branch name:** `refactor/wi-008-metadata-kotlin-migration`
**Depends on:** WI-005, WI-006

---

## Goal

Migrate `mill-metadata-core`, `mill-metadata-autoconfigure`, and
`mill-metadata-service` from Java + Lombok to Kotlin. The metadata module is a
natural fit for Kotlin: data classes replace Lombok boilerplate, null safety
aligns with nullable fields (`applicableTo`, `contentSchema`), and the rest of
the data layer (`mill-data-source-core`, `mill-data-testkit`) already uses Kotlin.

---

## Preconditions

- WI-005 completed — legacy code removed, no `mill-metadata-provider` consumers
  to worry about.
- WI-006 completed — new code (FacetTypeDescriptor, FacetCatalog, etc.) already
  written. Whether WI-006 code is Java or Kotlin, this WI migrates everything to
  Kotlin.

---

## Current State

### `mill-metadata-core` (22 Java files)

**Domain classes (Lombok `@Data` / `@Getter`):**
- `MetadataEntity` — `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`
- `MetadataFacet` — interface
- `AbstractFacet` — `@Getter`, `@Setter`
- `DescriptiveFacet` — `@Data`, `@EqualsAndHashCode`
- `StructuralFacet` — `@Data`, `@EqualsAndHashCode`
- `RelationFacet` — `@EqualsAndHashCode`, `@Getter`
- `ConceptFacet` — `@EqualsAndHashCode`, `@Getter`
- `ValueMappingFacet` — `@Data`, `@EqualsAndHashCode`
- `FacetRegistry` — `@Slf4j`, singleton
- `EntityReference`, `ConceptTarget` — records or small classes

**Enums:**
- `MetadataType`, `RelationType`, `RelationCardinality`, `DataClassification`,
  `ConceptSource`, `TableType`

**Exceptions:**
- `ValidationException`

**Service:**
- `MetadataService` — `@Slf4j`, `@RequiredArgsConstructor`

**Repository:**
- `MetadataRepository` — interface
- `FileMetadataRepository` — `@Slf4j`
- `ResourceResolver` — interface

**Build:** `java-library` plugin, Lombok compile-only dependency.

### `mill-metadata-autoconfigure` (3 Java files)

- `MetadataProperties`
- `MetadataCoreConfiguration`
- `MetadataRepositoryAutoConfiguration`

**Build:** depends on `mill-metadata-core`, Spring Boot.

### `mill-metadata-service` (remaining after WI-007: ~5 Java files)

- `MetadataController`, `FacetController`
- `DtoMapper`
- `MetadataEntityDto`, `FacetDto`

**Build:** depends on `mill-metadata-core`, Spring Web.

---

## Migration Strategy

### Phase 1: Build setup

1. Add Kotlin plugin to `mill-metadata-core/build.gradle.kts`:
   ```kotlin
   plugins {
       alias(libs.plugins.kotlin)
       id("io.qpointz.plugins.mill")
       id("org.jetbrains.dokka")
   }
   ```
2. Add `jackson-module-kotlin` dependency for Kotlin data class deserialization.
3. Remove Lombok dependencies (`compileOnly`, `annotationProcessor`).
4. Create `src/main/kotlin` and `src/test/kotlin` directories.

### Phase 2: Migrate domain classes

Convert one class at a time. Each conversion is a self-contained commit.

**Mapping rules:**

| Java (Lombok)                | Kotlin                              |
|------------------------------|-------------------------------------|
| `@Data`                      | `data class`                        |
| `@Getter` / `@Setter`       | `val` / `var`                       |
| `@NoArgsConstructor`         | Secondary constructor or defaults   |
| `@AllArgsConstructor`        | Primary constructor                 |
| `@RequiredArgsConstructor`   | Primary constructor with `val`      |
| `@EqualsAndHashCode`         | `data class` (auto-generated)       |
| `@Slf4j`                     | Companion `LoggerFactory` or `mu.KotlinLogging` |
| `@Builder`                   | Named arguments + `copy()`          |
| Java records                 | `data class`                        |
| `Optional<T>`                | Nullable `T?`                       |
| `Map<String, Object>`        | `Map<String, Any?>`                 |

**Migration order** (leaf classes first, to avoid mixed-language compilation
issues within a single commit):

1. Enums: `MetadataType`, `RelationType`, `RelationCardinality`,
   `DataClassification`, `ConceptSource`, `TableType`
2. `ValidationException`
3. Small domain: `EntityReference`, `ConceptTarget`
4. `MetadataFacet` interface → Kotlin interface
5. `AbstractFacet` → Kotlin abstract class
6. Facet classes: `DescriptiveFacet`, `StructuralFacet`, `RelationFacet`,
   `ConceptFacet`, `ValueMappingFacet`
7. `MetadataEntity`
8. `MetadataRepository` interface
9. `ResourceResolver` interface
10. `FileMetadataRepository`
11. `FacetRegistry` (or `FacetCatalog` if WI-006 already replaced it)
12. `MetadataService`

### Phase 3: Migrate autoconfigure

13. `MetadataProperties` — Kotlin data class with `@ConfigurationProperties`
14. `MetadataCoreConfiguration`
15. `MetadataRepositoryAutoConfiguration`

### Phase 4: Migrate service controllers

16. DTOs: `MetadataEntityDto`, `FacetDto`
17. `DtoMapper`
18. `MetadataController`, `FacetController`

### Phase 5: Migrate tests

19. Convert all unit tests to Kotlin (JUnit 5 works identically)
20. Convert integration tests (`testIT`)

### Phase 6: Cleanup

21. Remove `src/main/java` and `src/test/java` directories (should be empty)
22. Remove Lombok from all metadata `build.gradle.kts` files
23. Verify no Java sources remain in metadata modules

---

## Kotlin-Specific Conventions

Follow conventions already established in `mill-data-source-core`:

- Use `data class` for value types, not regular classes
- Use `val` (immutable) by default, `var` only when mutation is required
- Use Kotlin null safety (`T?`) instead of `Optional<T>`
- Use `companion object` for factory methods and loggers
- Use named arguments instead of builders
- Use `require()` / `check()` for validation instead of throwing manually
- Use extension functions where they improve readability
- No wildcard imports — explicit imports only

---

## Jackson Compatibility

Add `jackson-module-kotlin` to ensure Jackson can deserialize Kotlin data classes
(especially those without `@JsonProperty` annotations):

```kotlin
dependencies {
    api(libs.jackson.module.kotlin)
}
```

Verify that existing YAML files deserialize correctly with Kotlin data classes.
Key concern: Kotlin data classes require all constructor parameters to be present
in JSON/YAML unless they have defaults. Ensure all optional fields have defaults:

```kotlin
data class MetadataEntity(
    val id: String,
    val type: MetadataType,
    val schemaName: String? = null,
    val tableName: String? = null,
    val attributeName: String? = null,
    val facets: MutableMap<String, MutableMap<String, Any?>> = mutableMapOf(),
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
    val createdBy: String? = null,
    val updatedBy: String? = null
)
```

---

## Consumer Impact

Modules that depend on `mill-metadata-core`:

| Module | Language | Impact |
|--------|----------|--------|
| `ai/mill-ai-v1-core` | Java | Seamless — Kotlin classes are callable from Java. `data class` properties appear as `getX()` / `setX()`. Nullable `T?` appears as `@Nullable T`. |
| `data/mill-data-autoconfigure` | Java | Same as above. |
| `data/mill-data-backend-core` | Java | Same as above. |
| `metadata/mill-metadata-autoconfigure` | Kotlin (after this WI) | Native Kotlin. |
| `metadata/mill-metadata-service` | Kotlin (after this WI) | Native Kotlin. |

No consumer code changes needed — Kotlin's Java interop handles it. The one
exception: if any consumer uses Lombok's `@Builder` on metadata classes (unlikely,
since those are Lombok annotations on the metadata classes themselves, not on
consumer code).

---

## Risks

1. **Jackson deserialization breakage** — Kotlin data classes have different
   constructor semantics than Java POJOs. Missing `jackson-module-kotlin` or
   missing default values on optional fields will cause deserialization failures.
   Mitigate with comprehensive YAML/JSON round-trip tests.

2. **Mutable vs immutable** — Kotlin encourages immutability (`val`), but
   `MetadataEntity.facets` is mutated in place (e.g. `setFacet()`,
   `mergeEntityFacets()`). Need to decide: keep `MutableMap` (pragmatic) or
   redesign to return new instances (idiomatic but bigger change).

3. **Test framework compatibility** — Mockito works with Kotlin but has quirks
   (final classes by default). Consider `mockito-kotlin` or `MockK` as the
   mocking library for Kotlin tests.

4. **Incremental compilation** — Mixed Java/Kotlin in the same module during
   migration requires `kapt` or the Kotlin compiler to run first. This can slow
   builds temporarily until migration is complete.

---

## Verification

1. All existing YAML test fixtures deserialize into Kotlin data classes.
2. `./gradlew test` passes in all metadata modules.
3. `./gradlew test` passes in all consumer modules (AI, data).
4. No Java source files remain in metadata modules.
5. No Lombok dependency remains in metadata `build.gradle.kts` files.
6. `./gradlew build` from project root succeeds.

## Estimated Effort

Medium — ~30 files to convert across 3 modules. Each file is a mechanical
translation (Lombok → Kotlin data class). The real effort is in verifying
Jackson compatibility and updating tests. No logic changes.
