# WI-089 — Metadata Scopes and Context Composition

Status: `done`
Type: `✨ feature`
Area: `metadata`

## Goal

Introduce metadata scopes as independent, named sets of facet data (like Git branches) and
replace the hardcoded `global → role → team → user` precedence with a caller-defined
`MetadataContext` — an ordered list of scope URNs whose resolution order is determined by the
consumer, not the platform.

---

## Design Rationale

Scopes are branches, not a hierarchy. `global` is always present (like `main`), but beyond that
there is no platform-defined precedence. Different consumers compose scopes differently:

| Consumer | Context (ordered, last wins) — as passed in `?context=` |
|----------|--------------------------------------------------------------|
| Model view (read-only) | `global` — single scope, no merging |
| AI chat | `global,user:alice,chat:xyz` — chat overrides user, user overrides global |
| Team review | `global,team:eng` — team layer on top of global |

Values in the table are **prefixed slugs** as sent by callers in `?context=`.
After `MetadataContext.parse(csv)`, `MetadataContext.scopes` holds **full URN strings**
(e.g. `urn:mill/metadata/scope:global`). The table is narrative shorthand, not code.

The caller passes the desired context with every read request. The platform resolves and merges
in that order; it does not guess or impose. Adding a new scope type (e.g. `chat`) requires zero
changes to the service layer or entity model — the caller just includes it in the list.

---

## Domain Changes in `mill-metadata-core`

### `MetadataContext.kt` — new type

```kotlin
/**
 * An ordered sequence of scope URN keys defining metadata resolution precedence.
 * Facets are merged in list order — later scopes override earlier ones for the same facet type.
 * A single-element context performs no merging.
 */
data class MetadataContext(
    /** Ordered scope URN keys. Must not be empty. */
    val scopes: List<String>
) {
    init { require(scopes.isNotEmpty()) { "MetadataContext must contain at least one scope" } }

    companion object {
        /** Single-scope context — no merging. */
        fun of(scopeKey: String) = MetadataContext(listOf(scopeKey))
        /** Global-only context (default for read-only views with no user session). */
        fun global() = MetadataContext(listOf(MetadataUrns.SCOPE_GLOBAL))
        /**
         * Parse a comma-separated string of scope slugs or URN keys.
         * Each element is expanded to a full URN via [MetadataUrns.normaliseScopePath].
         * Unknown slug formats are passed through unchanged (they will produce no facets).
         */
        fun parse(csv: String) = MetadataContext(
            csv.split(",")
               .map { it.trim() }
               .filter { it.isNotEmpty() }
               .map { MetadataUrns.normaliseScopePath(it) }
        )
    }
}
```

### `MetadataEntity.getMergedFacet` — updated signature

Replace the existing method (which hard-codes `userId`, `userTeams`, `userRoles` parameters)
with a `MetadataContext`-based version:

```kotlin
/**
 * Resolves the effective facet value for [facetType] by merging across the scopes
 * in [context] in order. Later scopes override earlier ones. Returns the last non-null
 * payload found, converted to [facetClass].
 */
fun <T : Any> getMergedFacet(
    facetType: String,
    context: MetadataContext,
    facetClass: Class<T>
): Optional<T> {
    val scopedFacets = facets[facetType] ?: return Optional.empty()
    val winner = context.scopes
        .mapNotNull { scopedFacets[it] }
        .lastOrNull()
        ?: return Optional.empty()
    return converter().convert(winner, facetClass)
}
```

The old overload (with `userId`, `userTeams`, `userRoles`) is **deleted** — no backward
compatibility shim.

### `MetadataScope.kt` — domain type (pure, zero persistence annotations)

```kotlin
/**
 * A named, isolated set of metadata facet data.
 * Scopes are independent — no inherent hierarchy between them.
 * The [scopeId] is the canonical URN key (e.g. `urn:mill/metadata/scope:global`).
 */
data class MetadataScope(
    val scopeId: String,         // URN key, e.g. urn:mill/metadata/scope:global
    val displayName: String?,
    val ownerId: String?,        // null for global scope
    val createdAt: Instant
)
```

No `scopeType`, `visibility`, or `referenceId` on the domain type — those are persistence
concerns. The scope identity is entirely encoded in the URN key:
- `urn:mill/metadata/scope:global` — the global scope
- `urn:mill/metadata/scope:user:alice` — user personal scope
- `urn:mill/metadata/scope:team:eng` — team scope
- `urn:mill/metadata/scope:chat:xyz` — ephemeral chat scope (future)

### `MetadataScopeRepository.kt` — interface

```kotlin
interface MetadataScopeRepository {
    fun findById(scopeId: String): Optional<MetadataScope>
    fun findAll(): List<MetadataScope>
    fun save(scope: MetadataScope): MetadataScope
    fun deleteById(scopeId: String)
    fun existsById(scopeId: String): Boolean
}
```

### `MetadataScopeService.kt` — service

```kotlin
/**
 * Manages the lifecycle of metadata scopes.
 * Scopes are created explicitly or on demand during import/write operations.
 * No precedence logic lives here — context composition is the caller's responsibility.
 */
class MetadataScopeService(private val repo: MetadataScopeRepository) {

    /** Returns the global scope, which always exists. */
    fun globalScope(): MetadataScope = repo.findById(MetadataUrns.SCOPE_GLOBAL)
        .orElseThrow { IllegalStateException("Global scope not found — Flyway migration may not have run") }

    /** Returns all known scopes. */
    fun findAll(): List<MetadataScope> = repo.findAll()

    /** Returns the scope for the given URN key, or empty if it does not exist. */
    fun findByKey(scopeKey: String): Optional<MetadataScope> = repo.findById(scopeKey)

    /** Creates a new scope. Throws if a scope with this key already exists. */
    fun create(scopeId: String, displayName: String?, ownerId: String?): MetadataScope {
        require(!repo.existsById(scopeId)) { "Scope already exists: $scopeId" }
        return repo.save(MetadataScope(scopeId, displayName, ownerId, Instant.now()))
    }

    /** Deletes a scope. Throws if attempting to delete the global scope. */
    fun delete(scopeId: String) {
        require(scopeId != MetadataUrns.SCOPE_GLOBAL) { "Cannot delete the global scope" }
        repo.deleteById(scopeId)
    }
}
```

---

## JPA Adapter in `mill-metadata-persistence`

### `JpaMetadataScopeRepository.kt`

Implements `MetadataScopeRepository`; delegates to `MetadataScopeJpaRepository` (already
defined in WI-087); maps `MetadataScopeEntity ↔ MetadataScope`.

**Layering rule — scope resolution in `JpaMetadataRepository`:**
`JpaMetadataRepository.resolveOrCreateScope(scopeKey)` calls `MetadataScopeJpaRepository`
directly (both are in `mill-metadata-persistence`). It does **not** call `MetadataScopeService`.

Reason: `MetadataScopeService` is the service-layer facade used by controllers and
application-level orchestration. Having a repository class depend on a service creates an
inversion of the normal layering (repository → service → repository) and risks circular
Spring wiring. The persistence layer stays self-contained.

Rule: `MetadataScopeService` is used by controllers and `MetadataEditService` only.
`JpaMetadataRepository` talks directly to `MetadataScopeJpaRepository`.

---

## REST Endpoints — `MetadataScopeController`

`@RestController @RequestMapping("/api/v1/metadata/scopes")`
`@Tag(name = "metadata-scopes")`
`@ConditionalOnBean(MetadataScopeService::class)`

```
GET    /api/v1/metadata/scopes               → list all scopes (returns full URN scopeIds)
POST   /api/v1/metadata/scopes               → create scope → 201 + Location
DELETE /api/v1/metadata/scopes/{scopeSlug}   → 204 (409 if global)
```

No `scopes/my` endpoint — the application layer (model view, chat service) composes the
context it needs from known scope URNs. The platform does not guess the caller's intent.

- `{scopeSlug}` in path — **prefixed slug** (local part after `urn:mill/metadata/scope:`).
  Examples: `global`, `user:alice`, `team:eng`, `chat:xyz`.
  Decoded to full URN via `MetadataUrns.normaliseScopePath(slug)`.
  Only URNs within the `urn:mill/metadata/scope:` namespace are accepted; other URNs return 400.
- Request/response bodies use full URN strings for `scopeId`.
- `Location` header in 201 response: `/api/v1/metadata/scopes/<slug>`.

Request body for `POST`:
```json
{ "scopeId": "urn:mill/metadata/scope:team:eng", "displayName": "Engineering Team" }
```

---

## Context Parameter on Read Endpoints

Read endpoints in `MetadataEntityController` accept a `context` query parameter — a
comma-separated list of scope URN keys. The value uses **prefixed slugs** (not full URNs) for
readability; each slug is expanded server-side via `MetadataUrns.normaliseScopePath`.

```
GET /api/v1/metadata/entities/{id}?context=global
GET /api/v1/metadata/entities/{id}?context=global,user:alice
GET /api/v1/metadata/entities/{id}?context=global,user:123,chat:lalala
GET /api/v1/metadata/entities/{id}/facets/{type}?context=global,team:eng
```

The scope local-id after `urn:mill/metadata/scope:` can be any string — `user:<id>`,
`team:<name>`, `chat:<sessionId>`, or any custom scope created by the caller. The platform
does not restrict scope types. Unknown scope keys are silently skipped during resolution
(no facets exist for them yet — they produce an empty contribution to the merge).

Parsing: `MetadataContext.parse(contextParam)` expands each comma-delimited slug to a full
URN via `MetadataUrns.normaliseScopePath`. When the `context` parameter is absent, defaults
to `MetadataContext.global()` (`urn:mill/metadata/scope:global`).

The controller passes the `MetadataContext` to `MetadataService`, which uses
`entity.getMergedFacet(facetType, context, class)` to resolve the effective value.

**No automatic security-driven context injection at this stage.** The caller supplies the full
ordered scope list. Future work may add a `MetadataContextResolver` that builds a context from
the security principal (e.g. `[global, user:<principalId>]`) for convenience.

---

## Autoconfiguration

Add to `MetadataEntityServiceAutoConfiguration`:

```kotlin
@Bean @ConditionalOnBean(MetadataScopeRepository::class)
fun metadataScopeService(repo: MetadataScopeRepository): MetadataScopeService =
    MetadataScopeService(repo)

@Bean @ConditionalOnBean(MetadataScopeService::class)
fun metadataScopeController(svc: MetadataScopeService): MetadataScopeController = ...
```

---

## Tests

### Unit (`src/test/`)

- `MetadataContextTest`
  - `shouldMergeFacetsInOrder_whenMultipleScopesGiven` — last scope wins
  - `shouldReturnGlobal_whenOnlyOneScopeInContext` — no merge, exact value
  - `shouldReturnEmpty_whenNoScopeHasFacet`
  - `shouldParseSlugs_whenContextParamProvided` — `"global,user:alice"` → two URN scopes
  - `shouldIgnoreUnknownScope_whenNoFacetsExistForIt` — `"global,chat:xyz"` still resolves global
  - `shouldAcceptArbitraryScopeType_whenChatOrCustomScopeInContext` — any local-id is valid

- `MetadataScopeServiceTest`
  - `shouldReturnGlobalScope_whenRequested`
  - `shouldCreate_whenScopeDoesNotExist`
  - `shouldThrow_whenCreatingDuplicateScope`
  - `shouldThrow_whenDeletingGlobalScope`

- `MetadataScopeControllerTest` (`@WebMvcTest`)
  - `GET /scopes` → 200 list; each entry has `scopeId` as full URN
  - `POST /scopes` → 201; `Location` header contains slug path
  - `DELETE /scopes/global` → 409 (prefixed slug of global scope)
  - `DELETE /scopes/user:alice` → 204 (prefixed slug of user scope)

### Integration (`src/testIT/`)

- `JpaMetadataScopeRepositoryIT` — save/findById/findAll/deleteById; verify global scope
  seeded by V4 migration.
- `MetadataContextResolutionIT` — load moneta entities from YAML; resolve facets via
  `MetadataContext.global()`; assert descriptive facet present.

---

## Acceptance Criteria

- `MetadataContext.parse("global,user:alice")` returns context with two full URN scopes;
  `getMergedFacet` returns user-scope value when present, global otherwise.
- `MetadataContext.parse("global,user:123,chat:lalala")` returns three-scope context;
  unknown scope `chat:lalala` silently contributes no facets.
- `GET /api/v1/metadata/entities/{id}?context=global` returns only facets from global scope.
- `GET /api/v1/metadata/entities/{id}` (no context param) defaults to global scope.
- `POST /api/v1/metadata/scopes` creates a new scope; `GET /api/v1/metadata/scopes` lists it.
- `DELETE /api/v1/metadata/scopes/global` returns 409 (prefixed slug of global scope).
- Old `getMergedFacet(facetType, userId, userTeams, userRoles, class)` signature is gone.
- All unit and integration tests pass.
