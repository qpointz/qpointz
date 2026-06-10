# SPEC — Metadata Rework (Greenfield)

**Status:** Draft
**Story folder (archived):** `docs/workitems/completed/20260330-metadata-rework/`
**Branch:** `feat/metadata-rework-final`

**Handoff:** Implementation backlog and decisions are consolidated in [`PLAN.md`](./PLAN.md) (sync with this SPEC when normative text diverges).

This is a **greenfield** redesign. Existing code is reference material only — not a constraint.
Full freedom on module layout, domain model, API surface, and dependency graph.
No backward compatibility. Breaking changes delivered in one cut.

---

## 1. Problem Statement

The metadata subsystem carries three structural problems that a partial fix cannot resolve:

1. **JDBC coordinates baked into metadata-core.** `MetadataEntity` stores `schemaName`,
   `tableName`, `attributeName`. This makes non-relational bindings impossible and violates
   the module boundary rule: `metadata/*` must not know about JDBC or catalog conventions.

2. **Facets are denormalized onto the entity aggregate.** The `facets: Map<typeUrn, Map<scopeUrn, payload>>` field on `MetadataEntity` forces every read and write to touch the whole entity document. Row-per-instance facet storage was added in V8 but the domain model was never updated to match; the service still goes through the entity aggregate.

3. **Incremental Flyway debt.** V4–V8 are interdependent migrations with data transforms that
   cannot be reasoned about in isolation and are difficult to apply on a fresh database.

---

## 2. Design Goals

| # | Goal |
|---|------|
| G1 | Entity identity is a **full Mill URN** (`urn:mill/...`). The group/class/id segments are opaque to metadata-core (e.g. `urn:mill/model/table:...` from the data layer, or `urn:mill/metadata/entity:...` in seed YAML). No coordinate fields anywhere in `metadata/*`. |
| G2 | Facets are **first-class rows**, not a nested map on the entity. Entity and facet lifecycles are managed independently. |
| G3 | `mill-metadata-core` depends **only** on `mill-core`. Pure Kotlin/Java — no Spring, no JPA, no direct Jackson. Other metadata modules depend only on `metadata/*` + their designated upstream (`mill-persistence` for the JPA adapter). No `data/*`, `ai/*`, `apps/*`, `clients/*` imports anywhere in `metadata/*`. |
| G4 | JDBC (and future) bindings encode their world into URNs in the `data/*` layer. `metadata-core` treats local-id as opaque. |
| G5 | **Single squashed Flyway migration** for all metadata tables. Row-audit quad on every `metadata_*` table **except** `metadata_audit`. Unified **`metadata_audit`** append-only table with JPA listeners. |
| G6 | Java `@ConfigurationProperties` for all `mill.metadata.*` keys. |
| G7 | **Redesign** `FileMetadataRepository`. Keep a file-backed adapter but replace its internal format with the canonical multi-document YAML format (§15). The old ad-hoc format is deleted. |
| G8 | Breaking changes to domain model, persistence DDL, REST API, and YAML import format — no shims. |
| G9 | **Metadata is maximally permissive.** The metadata service accepts any entity URN, any facet type key, and any payload without rejecting on semantic grounds. Validation and enforcement of type constraints, schema conformance, and `applicableTo` rules is the exclusive responsibility of client modules (e.g. `data/mill-data-schema-*`). |

---

## 3. Non-Goals

- Backward compatibility of any kind.
- Value mapping bridge (M-1–M-9) — separate story.
- Metadata promotion workflow (M-23) — deferred.
- Complex type metadata (M-27) — separate story.
- Reintroduction of the **legacy** file adapter **format** or obsolete **`mill.metadata.storage.*`** keys — the **canonical YAML** file backend (§15) and **`mill.metadata.repository.*`** are in scope (see [`PLAN.md`](./PLAN.md)).
- `apps/mill-regression-cli` (AI v1).

---

## 4. Module Layout and Dependency Rules

### 4.1 Modules

Four modules; module names unchanged. Package root `io.qpointz.mill.metadata.*` unchanged.

```
metadata/
  mill-metadata-core

  mill-metadata-persistence

  mill-metadata-autoconfigure

  mill-metadata-service
```

### 4.2 Dependency rules (hard constraints)

```
core/mill-core
    │
    ▼
mill-metadata-core          ← PURE. Only depends on mill-core.
    │                          No Spring, no JPA, no direct Jackson.
    │                          Uses JsonUtils / YamlUtils from mill-core.
    │                          Defines all domain types, repository interfaces,
    │                          service interfaces, and NoOp stubs.
    │
    ├──────────────────────────────────────────────────┐
    │                                                  │
    ▼                                                  ▼
mill-metadata-persistence            mill-metadata-service
(JPA adapter)                        (REST layer)
Depends on:                          Depends on:
  - mill-metadata-core               - mill-metadata-core
  - persistence/mill-persistence     - spring-boot-web
    (provides JPA infra,             - springdoc-openapi
     Flyway, shared converters)
Implements: MetadataEntityRepository,
            FacetRepository,
            FacetTypeRepository,
            MetadataScopeRepository
JPA entities, Spring Data repos,
Flyway migration scripts,
audit listeners — all here.
                │
                ▼
    mill-metadata-autoconfigure
    (composition root)
    Depends on:
      - mill-metadata-core
      - mill-metadata-persistence
      - mill-metadata-service
    Wires JPA adapters as beans,
    creates service instances,
    Java @ConfigurationProperties
    for all mill.metadata.* keys.
```

### 4.3 URN case-insensitivity — repository contract

All repository implementations (JPA or otherwise) **MUST** enforce canonical lowercase
representation for every URN / id value before persistence and comparison:

- `entity_res`, assignment **`uuid`** (facet row), `scope_res` / scope lookup keys, `type_res` (facet type URN) — stored lowercase.
- Any lookup by id/URN (e.g. `findById`, `findByUid`) performs a case-insensitive
  match. The simplest compliant implementation: normalise the input to lowercase before
  issuing the query (since stored values are already lowercase, a plain equality query
  suffices).
- `MetadataEntityUrn.canonicalize(urn)` is the standard normaliser — call it on any
  incoming URN at the service boundary before passing to the repository.
- The `kind` field is **not** a URN; it is NOT normalised — stored as-is.

### 4.4 `mill-metadata-core` — strict purity rules

| Rule | Detail |
|------|--------|
| **No Spring** | No `@Service`, `@Component`, `@Autowired`, `@Bean`, `@Configuration`, `@ConditionalOn*`, or any `org.springframework.*` import. |
| **No JPA** | No `javax.persistence.*`, `jakarta.persistence.*`, Spring Data, Hibernate. |
| **No direct Jackson** | Use `JsonUtils.defaultJsonMapper()` and `YamlUtils.defaultYamlMapper()` from `mill-core` wherever JSON/YAML serialisation is needed. |
| **Allowed** | `mill-core`, Kotlin stdlib, Java stdlib. Pure interfaces, data classes, enums, and plain service implementations. |
| **NoOp stubs** | `NoOpMetadataEntityRepository`, `NoOpFacetRepository`, `NoOpFacetTypeRepository`, `NoOpMetadataScopeRepository` — plain Kotlin objects, no framework. |
| **Service implementations** | `DefaultMetadataEntityService`, `DefaultFacetService`, `DefaultFacetCatalog`, `DefaultMetadataScopeService` — plain classes, constructor-injected repositories and dependencies, no annotations. |

### 4.5 `mill-metadata-persistence` — adapter rules

- Implements the repository interfaces defined in `mill-metadata-core`.
- Depends on `mill-persistence` (provides Spring Boot Data JPA, Flyway, `MapJsonConverter`, `SetJsonConverter`).
- Knows about JPA entities, Spring Data repositories, Flyway scripts. Nothing outside this module needs to know any of this.
- All Flyway migration scripts for metadata live here (inside `mill-persistence/src/main/resources/db/migration/` per the shared migration path convention, or in a dedicated subfolder if `mill-persistence` supports it).
- Audit listeners (`@PrePersist`, etc.) live here, not in core.

### 4.6 `mill-metadata-autoconfigure` — composition root rules

- The only module that couples `mill-metadata-core` services to `mill-metadata-persistence` adapters.
- Creates service instances (e.g. `DefaultMetadataEntityService`, `DefaultFacetService`) by injecting the appropriate repository beans.
- Exposes them as Spring beans using `@Bean` methods in `@Configuration` classes.
- All `@ConfigurationProperties` classes for `mill.metadata.*` keys live here and are implemented in **Java** (for automatic `spring-boot-configuration-processor` metadata generation).
- `@AutoConfigureAfter` ordering if needed between sub-configurations.

### 4.7 `mill-metadata-service` — REST rules

- Depends on `mill-metadata-core` only (consumes service interfaces and domain types).
- Spring MVC controllers and DTOs live here; no JPA or persistence imports.
- Receives service implementations via constructor injection (wired by `mill-metadata-autoconfigure` at runtime).
- No business logic — all logic in `mill-metadata-core` services.

### 4.8 What other modules see

Modules outside `metadata/` (e.g. `data/mill-data-schema-core`, `ai/mill-ai-v3`) depend only
on `mill-metadata-core`. They receive service/repository implementations at runtime through the
Spring context wired by `mill-metadata-autoconfigure`. They never import `mill-metadata-persistence`
or any JPA type.

**Module boundary check (CI or build-time):**
`./gradlew :metadata:mill-metadata-core:dependencies` must show no `data/*` artifacts.

---

## 5. Domain Model

### 5.1 Core principles

#### Generic entity-facet framework

`metadata-core` has **no notion of schema, table, attribute, or any database object**.
It does not model or know about hierarchies, parent-child relationships, or dependencies
between entities. Every entity is an **independent, flat record** identified by a URN and
carrying a set of typed, scoped facet payloads.

The mapping of database objects (schemas, tables, columns) onto metadata entities is
entirely the responsibility of `data/mill-data-schema-*`. From metadata's perspective,
those are just entities like any other.

#### Metadata is a permissive store — not a validator

The metadata service **accepts any combination of entity URNs, facet type keys, and payloads**
without rejecting on semantic grounds. Specifically:

| Scenario | Metadata behaviour |
|----------|-------------------|
| Unknown `facetTypeKey` (no `FacetTypeDefinition` registered) | Auto-creates an `OBSERVED` `FacetType` record; stores the assignment |
| Payload does not conform to `contentSchema` | Stored as-is; no validation error |
| Entity `kind` not in `applicableTo` list | Ignored; assignment proceeds |
| Any valid `urn:mill/` URN as entity id | Accepted; entity auto-created on first facet assignment if it does not exist |

**`applicableTo` and `contentSchema` are advisory metadata** — they exist to inform clients,
not to gate writes. The metadata service exposes an `inspect(typeKey, payload)` method
that clients can call to check conformance before submitting, but calling it is entirely
optional and the service never calls it internally.

**Validation and rejection is the responsibility of client modules** (`data/mill-data-schema-*`
and others) that understand the domain semantics of the facet types they own.

### 5.2 URN Grammar — universal Mill identifier format

All Mill resource identifiers use the same grammar:

```
urn:mill/<group>/<class>:<id>
```

| Segment | Role |
|---------|------|
| `urn:mill/` | Fixed prefix for all Mill URNs |
| `<group>` | Topic / namespace (e.g. `metadata`, `model`, `auth`) |
| `<class>` | Kind of resource within the topic (e.g. `facet-type`, `scope`, `table`, `attribute`) |
| `<id>` | Opaque resource identifier — structure is the responsibility of the owner group |

**URNs are case-insensitive.** The canonical form is lowercase. Any component that stores
or compares URNs (repositories, services, REST path handling) MUST normalise to lowercase
before persistence or comparison.

Examples:

| URN | Group | Class | Owner |
|-----|-------|-------|-------|
| `urn:mill/metadata/facet-type:descriptive` | `metadata` | `facet-type` | metadata-core |
| `urn:mill/metadata/scope:global` | `metadata` | `scope` | metadata-core |
| `urn:mill/model/table:sales.orders` | `model` | `table` | `mill-data-schema-*` |
| `urn:mill/model/schema:sales` | `model` | `schema` | `mill-data-schema-*` |
| `urn:mill/model/attribute:sales.orders.customer_id` | `model` | `attribute` | `mill-data-schema-*` |

**Entity URNs** (the `entity_res` stored in `metadata_entity`) are not restricted to the
`metadata` group. When the data layer registers a database table as a metadata entity, the
entity URN is `urn:mill/model/table:<id>` — not `urn:mill/metadata/entity:<id>`. The
metadata service stores whatever URN the caller provides and does not interpret the group or
class segments.

`MetadataEntityUrn.canonicalize(urn)` in `mill-metadata-core` normalises any URN string to
lowercase and trims whitespace. It does not validate the group or class — that is the caller's
responsibility. Any incoming URN that does not begin with `urn:mill/` is rejected with a
descriptive error.

### 5.3 `MetadataEntity` — aggregation root

An entity is the **assignment point** for facets. It carries no payload of its own — all
descriptive data lives in `FacetInstance` rows attached to it.

```kotlin
data class MetadataEntity(
    val id: String,           // full URN — the stable identity of the entity
    val kind: String?,        // opaque label assigned by the creator; null = untyped
                              // metadata-core does not interpret or validate this string
    val createdAt: Instant,
    val createdBy: String?,
    val lastModifiedAt: Instant,
    val lastModifiedBy: String?
)
```

- **No** `schemaName`, `tableName`, `attributeName`.
- **No** hierarchy fields (`parentId`, `children`, etc.).
- **No** `facets` field — facets are independent `FacetInstance` rows.
- `kind` is a free-form label. The data layer uses values like `"schema"`, `"table"`,
  `"attribute"` when registering database objects. Metadata-core stores whatever string is
  given and does not validate or restrict it.

### 5.4 `FacetInstance` — facet assignment with unique id

Every time a facet type is assigned to an entity a `FacetInstance` row is created.
Each assignment carries a **unique UUID (`uid`)** that identifies that specific assignment
for its entire lifetime. The `uid` is generated at creation and is immutable.

```
Entity E1
  └── FacetInstance  uid=α  facetType=A  scope=global  payload={...}
  └── FacetInstance  uid=β  facetType=B  scope=global  payload={...}
  └── FacetInstance  uid=γ  facetType=B  scope=global  payload={...}
  └── FacetInstance  uid=δ  facetType=C  scope=global  payload={...}
```

F(B) appears twice above because facet type B has `MULTIPLE` cardinality. Each
assignment is a distinct row with its own `uid`.

```kotlin
data class FacetInstance(
    val uid: String,           // UUID v4 — the stable, immutable id of this assignment
    val entityId: String,      // entity URN (FK → metadata_entity.entity_res)
    val facetTypeKey: String,  // facet type URN
    val scopeKey: String,      // scope URN
    val mergeAction: MergeAction, // SET | TOMBSTONE | CLEAR — mirrors metadata_entity_facet.merge_action
    val payload: Map<String, Any?>,
    val createdAt: Instant,
    val createdBy: String?,
    val lastModifiedAt: Instant,
    val lastModifiedBy: String?
)
```

**`uid` is the primary handle for all assignment operations.** Reading, updating, and
deleting a specific assignment always targets its `uid`. The
`(entityId, facetTypeKey, scopeKey)` triple is used for listing and resolution but never
for single-row mutation.

Cardinality (from `FacetTypeManifest.targetCardinality`):

| Cardinality | Rule | Write behaviour |
|-------------|------|-----------------|
| `SINGLE` | At most one assignment per `(entityId, facetTypeKey, scopeKey)` triple | Writing when a row already exists **updates the payload in-place** (uid unchanged). Writing when no row exists creates a new row with a new uid. |
| `MULTIPLE` | Unlimited assignments per triple | Every write creates a **new row with a new uid**. Existing assignments are never touched implicitly. |

Replaces the old `MetadataFacetInstanceRow` bridge type.

```kotlin
enum class MergeAction { SET, TOMBSTONE, CLEAR }
```

### 5.5 Facet type model — defined vs observed

There are two distinct facet type concepts in the system:

#### `FacetTypeDefinition` — declared upfront

A `FacetTypeDefinition` is a predefined contract for a facet. It specifies what fields
a facet of this type must carry, their types, and validation rules. Definitions are declared
by the metadata layer (generic types like `descriptive`) or registered at startup by other
modules (data-layer types like `structural`, `relation`).

```kotlin
data class FacetTypeDefinition(
    val typeKey: String,           // facet type URN — e.g. urn:mill/metadata/facet-type:descriptive
    val displayName: String?,
    val description: String?,
    val mandatory: Boolean,        // must every applicable entity carry this facet?
    val enabled: Boolean,
    val targetCardinality: FacetTargetCardinality,  // SINGLE | MULTIPLE
    val applicableTo: List<String>?,   // which entity kinds this type is intended for — provided as
                                       // a hint for clients; metadata-core NEVER enforces this
    val contentSchema: Map<String, Any?>?,  // JSON Schema made available to clients for validation;
                                             // metadata-core NEVER validates against this internally —
                                             // any payload is stored as-is
    val schemaVersion: String?,
    val createdAt: Instant,
    val createdBy: String?,
    val lastModifiedAt: Instant,
    val lastModifiedBy: String?
)
```

`applicableTo` holds opaque entity `kind` strings. The data layer sets
`applicableTo = ["table", "attribute"]` for its types; metadata-core stores these as-is.

`FacetTargetCardinality` (`SINGLE | MULTIPLE`) — governs how many assignments a single
`(entity, facetType, scope)` triple may hold.

#### `FacetType` — observed/runtime record

A `FacetType` is the runtime record of a facet type that actually appears in stored
`FacetInstance` rows. Every `FacetInstance` references a `FacetType` (not a
`FacetTypeDefinition` directly). A `FacetType` optionally links back to a
`FacetTypeDefinition` if a matching definition was registered.

```kotlin
data class FacetType(
    val typeKey: String,           // facet type URN — unique, matches FacetTypeDefinition if defined
    val source: FacetTypeSource,   // DEFINED | OBSERVED
    val definition: FacetTypeDefinition?,  // null if source = OBSERVED with no known definition
    val createdAt: Instant,
    val createdBy: String?,
    val lastModifiedAt: Instant,
    val lastModifiedBy: String?
)

enum class FacetTypeSource { DEFINED, OBSERVED }
```

- `DEFINED` — a `FacetTypeDefinition` exists for this key; `definition` is non-null.
- `OBSERVED` — a `FacetInstance` was stored with this type key but no definition has been
  registered; `definition` is null. The metadata service **auto-creates** an OBSERVED
  `FacetType` row on the first assignment that uses an unknown type key — it never rejects.

`FacetInstance.facetTypeKey` is the `FacetType.typeKey` (not the definition key). The
resolution: `FacetInstance → FacetType → FacetTypeDefinition?`.

The split maps directly to the two persistence tables:
`metadata_facet_type_def` (definitions) and `metadata_facet_type` (runtime types). Assignments live in **`metadata_entity_facet`**.

### 5.6 `MetadataScope`, `MetadataContext`

`MetadataScope` carries scope type and optional reference id as opaque strings.

`MetadataContext` is an **ordered list of scope URNs** supplied by the **caller** (HTTP handler, UI session, agent runtime, etc.). **Precedence is not hard-coded** in metadata-core as a fixed RBAC ordering (global vs team vs user) — the list order defines evaluation order for **last-wins** merge. Documented examples (e.g. user over team over global) are **illustrative**; another valid pattern is **global then chat-scoped** URN so the **last** scope in the list wins (e.g. AI v3 conversation context).

**Merge engine:** Scope folding, **SINGLE** vs **MULTIPLE**, and interpretation of **`merge_action`** on assignment rows (§8.3) live in a **repository-agnostic** component in **`mill-metadata-core`** (e.g. `MetadataReader` or a dedicated collaborator used by `DefaultFacetService`). **Repositories** persist and load rows only — **no** effective-view merge logic in JPA adapters.

**Read model for downstream modules (`MetadataView`):** `data/*` must not call metadata repositories directly for **read** paths that need resolved metadata. **`mill-metadata-core`** provides a **`MetadataView`** (name fixed) — a plain class constructed with the **metadata repository interfaces** it needs plus a **`MetadataContext`** (ordered scope list). It produces the **resultant facet view** (effective assignments and, when needed, intermediate layers for merge-trace APIs). **Controllers and services in `mill-data-schema-*` (and related data modules)** use **`MetadataView`** (or a thin façade around it) for read operations; they remain responsible for building **`MetadataContext`** from HTTP/session/agent state.

**Facet assignment row — `merge_action`:** Operational overlay semantics (**SET**, **TOMBSTONE**, **CLEAR** — see §8.3) are **not** stored inside `payload_json`; they use a dedicated column so domain payloads stay portable.

### 5.7 Types removed from `mill-metadata-core`

| Type | Where it goes |
|------|--------------|
| `MetadataType` enum (SCHEMA, TABLE, ATTRIBUTE, CONCEPT, CATALOG) | Deleted entirely from metadata. The data layer uses `entity.kind` strings directly. |
| `MetadataEntityUrn.forSchema/forTable/forAttribute/forConcept` | Moved to `mill-data-schema-core` as part of `MetadataEntityUrnCodec`. metadata-core retains only a generic `MetadataEntityUrn.canonicalize(urn)` normaliser. |
| `MetadataEntityIds` | Deleted (coordinate bridge). |
| `EntityReference` (schema/table/attribute typed) | Deleted from metadata-core. Data layer defines its own typed reference if needed. |
| `RelationFacet` (typed, with `EntityReference`) | Moved to `mill-data-schema-core`. metadata-core stores relation payloads as opaque JSON. |
| `StructuralFacet` | Moved to `mill-data-schema-core`. |
| `ConceptFacet.targets` with schema/table coordinates | Replaced by opaque URN-reference list if concept facet is retained; otherwise moved. |
| `MetadataFacetInstanceRow` | Replaced by `FacetInstance`. |

---

## 6. Repository Interfaces

All interfaces live in `mill-metadata-core` — plain Kotlin, no framework annotations.
Implementations live in `mill-metadata-persistence` (JPA) or `mill-metadata-core` (NoOp).

Downstream callers see only these interfaces, never JPA entities or Spring Data types.

### 6.1 `MetadataEntityRepository`

```kotlin
interface MetadataEntityRepository {
    fun findById(id: String): MetadataEntity?
    fun findAll(): List<MetadataEntity>
    fun findByKind(kind: String): List<MetadataEntity>   // opaque kind string filter
    fun exists(id: String): Boolean
    fun save(entity: MetadataEntity): MetadataEntity
    fun delete(id: String)
    fun deleteAll()
}
```

No `findByLocation`, no `findByType(MetadataType)` — those were coordinate concepts.
`findByKind` accepts the opaque kind string; returns all entities whose `kind` matches.

Implementations MUST normalise `id` to lowercase before querying. Storing values as lowercase
ensures plain equality queries are sufficient (no `LOWER()` in SQL).

### 6.2 `FacetRepository`

```kotlin
interface FacetRepository {
    /** All assignments attached to an entity, across all types and scopes. */
    fun findByEntity(entityId: String): List<FacetInstance>

    /** All assignments of a specific facet type on an entity, across all scopes. */
    fun findByEntityAndType(entityId: String, facetTypeKey: String): List<FacetInstance>

    /** All assignments at a specific (entity, type, scope) triple — used for resolution. */
    fun findByEntityTypeAndScope(
        entityId: String, facetTypeKey: String, scopeKey: String
    ): List<FacetInstance>

    /** Look up a single assignment by its stable uid. */
    fun findByUid(uid: String): FacetInstance?

    /**
     * Persist an assignment. If [FacetInstance.uid] is blank, generate a new UUID.
     * For SINGLE-cardinality upserts, the caller is responsible for passing the existing
     * uid so the row is updated in-place rather than a new row inserted.
     */
    fun save(facet: FacetInstance): FacetInstance

    /** Delete a single assignment by uid. Returns true if deleted, false if not found. */
    fun deleteByUid(uid: String): Boolean

    /** Delete all assignments belonging to an entity (cascade on entity delete). */
    fun deleteByEntity(entityId: String)

    /** Delete all assignments at a given (entity, type, scope) triple. */
    fun deleteByEntityTypeAndScope(
        entityId: String, facetTypeKey: String, scopeKey: String
    )
}
```

### 6.3 `FacetTypeDefinitionRepository` + `FacetTypeRepository`

Two separate repository interfaces — one per domain type:

```kotlin
/** Repository for predefined facet type definitions (metadata_facet_type_def). */
interface FacetTypeDefinitionRepository {
    fun findByKey(typeKey: String): FacetTypeDefinition?
    fun findAll(): List<FacetTypeDefinition>
    fun save(definition: FacetTypeDefinition): FacetTypeDefinition
    fun delete(typeKey: String)
}

/** Repository for runtime/observed facet types (metadata_facet_type). */
interface FacetTypeRepository {
    fun findByKey(typeKey: String): FacetType?
    fun findAll(): List<FacetType>
    /** Find all types that have a linked definition (source = DEFINED). */
    fun findDefined(): List<FacetType>
    /** Find all types that have no linked definition (source = OBSERVED). */
    fun findObserved(): List<FacetType>
    fun save(facetType: FacetType): FacetType
    fun delete(typeKey: String)
}
```

Remove file-backed implementations of both.

### 6.4 `MetadataScopeRepository`

Part of the core `mill-metadata-core` contract. Scope listing is a first-class operation.

```kotlin
interface MetadataScopeRepository {
    fun findByRes(scopeRes: String): MetadataScope?   // lookup by URN (case-normalised before call)
    fun findAll(): List<MetadataScope>
    fun findByType(scopeType: String): List<MetadataScope>
    fun exists(scopeRes: String): Boolean
    fun save(scope: MetadataScope): MetadataScope
    fun delete(scopeRes: String)
}
```

`MetadataScope` domain type carries: `res` (URN), `scopeType`, `referenceId`, `displayName`,
`ownerId`, `visibility`, and row-audit fields.

### 6.5 `MetadataAuditRepository`

```kotlin
interface MetadataAuditRepository {
    fun record(entry: AuditEntry)
    fun findBySubjectRef(subjectRef: String): List<AuditEntry>
    fun findByActor(actorId: String): List<AuditEntry>
}
```

`AuditEntry` carries: `operation`, `subjectType`, `subjectRef`, `actorId`, `correlationId`,
`occurredAt`, `payloadBefore`, `payloadAfter`.

This repository is implemented in `mill-metadata-persistence`. **JPA mode:** **only** JPA entity
listeners obtain `MetadataAuditRepository` (via `ApplicationContext` or equivalent) and call
`record(...)` on entity lifecycle events. **File repository mode:** the file adapter implements
`MetadataAuditRepository` internally (e.g. append to `_audit.yml`) — still **not** exposed to
domain services. **Domain services in `mill-metadata-core` and REST controllers do not depend on,
inject, or call** `MetadataAuditRepository` — audit is **not** a service concern. Fail-open:
catch all exceptions, log at WARN, never propagate.

### 6.6 `MetadataSeedLedgerRepository` (core interface)

```kotlin
interface MetadataSeedLedgerRepository {
    fun findBySeedKey(seedKey: String): SeedLedgerEntry?
    fun recordCompletion(seedKey: String, metadata: SeedCompletionMetadata)
    // … fingerprint on metadata: e.g. md5:<hex> of raw seed bytes (§14.1)
}
```

Lives in **`mill-metadata-core`** — **no Spring**. JPA implementation in **`mill-metadata-persistence`**. Used by the startup seed runner (autoconfigure) and optional tests.

---

## 7. Service Layer

All service interfaces and implementations live in `mill-metadata-core`. They are **plain Kotlin
classes** with no Spring annotations. The composition root (`mill-metadata-autoconfigure`)
instantiates and wires them as Spring beans.

### 7.1 `MetadataEntityService` interface + `DefaultMetadataEntityService`

```kotlin
// Interface in mill-metadata-core — pure
interface MetadataEntityService {
    fun findById(id: String): MetadataEntity?
    fun findAll(): List<MetadataEntity>
    fun findByKind(kind: String): List<MetadataEntity>   // opaque kind string; no MetadataType enum
    fun create(entity: MetadataEntity, actor: String): MetadataEntity
    fun update(entity: MetadataEntity, actor: String): MetadataEntity
    fun delete(id: String, actor: String)
}

// Implementation in mill-metadata-core — no annotations, constructor-injected
class DefaultMetadataEntityService(
    private val entityRepository: MetadataEntityRepository
) : MetadataEntityService { ... }
```

`DefaultMetadataEntityService` stamps `createdAt`/`lastModifiedAt`, validates URN form, delegates
to `MetadataEntityRepository`. It has no knowledge of JPA, Spring, Flyway, or audit — **`metadata_audit` rows are written only by JPA listeners** (§8.4).

The `@Bean` definition that creates it lives in `mill-metadata-autoconfigure`:
```kotlin
// In mill-metadata-autoconfigure — Spring lives here
@Bean
fun metadataEntityService(
    entityRepo: MetadataEntityRepository
): MetadataEntityService = DefaultMetadataEntityService(entityRepo)
```

### 7.2 `FacetService` interface + `DefaultFacetService`

```kotlin
// Interface in mill-metadata-core — pure
interface FacetService {

    /**
     * Returns all facet assignments for an entity, resolved across the context (last-wins
     * per facet type). The returned list contains one entry per (facetType, uid) pair
     * that survives the scope resolution.
     */
    fun resolve(entityId: String, context: MetadataContext): List<FacetInstance>

    /**
     * Returns all assignments for a specific facet type, resolved across the context.
     * Returns a list because MULTIPLE cardinality may yield several assignments.
     */
    fun resolveByType(
        entityId: String, facetTypeKey: String, context: MetadataContext
    ): List<FacetInstance>

    /**
     * Assigns a facet to an entity at the given scope. **Never rejects on semantic grounds.**
     *
     * - Unknown [facetTypeKey] (no definition registered): auto-creates an OBSERVED [FacetType]
     *   record and proceeds. No error.
     * - Payload: stored as-is without schema validation. Any JSON object is accepted.
     * - [applicableTo] on the facet type definition: purely advisory; not checked here.
     *
     * Write mode determined by [FacetCatalog.resolveCardinality]:
     *   SINGLE — if an assignment already exists for (entityId, facetTypeKey, scopeKey),
     *     its payload is updated in-place (same uid returned). Otherwise a new uid is created.
     *   MULTIPLE — always creates a new assignment with a new uid.
     *   Unknown type (no definition) → treated as MULTIPLE.
     *
     * Returns the saved [FacetInstance] with its uid.
     */
    fun assign(
        entityId: String, facetTypeKey: String, scopeKey: String,
        payload: Map<String, Any?>, actor: String
    ): FacetInstance

    /**
     * Updates the payload of an existing assignment identified by [uid].
     * uid is immutable — only the payload and lastModifiedAt/By change.
     * Throws if no assignment with that uid exists.
     */
    fun update(uid: String, payload: Map<String, Any?>, actor: String): FacetInstance

    /**
     * Removes a specific facet assignment by its uid.
     * Works for both SINGLE and MULTIPLE cardinality.
     * Returns true if deleted, false if uid not found.
     */
    fun unassign(uid: String, actor: String): Boolean

    /**
     * Removes all facet assignments of a given type on an entity at a specific scope.
     * Convenience method for removing all MULTIPLE assignments in one call, or for
     * removing the single SINGLE assignment without having to know its uid.
     */
    fun unassignAll(entityId: String, facetTypeKey: String, scopeKey: String, actor: String)
}

// Implementation in mill-metadata-core — plain class
class DefaultFacetService(
    private val facetRepository: FacetRepository,
    private val facetCatalog: FacetCatalog
) : FacetService { ... }
```

**Resolution algorithm** (in `DefaultFacetService.resolve`):
- Iterate `context.scopes` in order (first = lowest priority, last = highest priority).
- For each scope, query all assignments for the entity at that scope.
- Group by facet type. For SINGLE types, the later scope's result overwrites the earlier.
  For MULTIPLE types, the later scope's full assignment list replaces the earlier scope's
  list entirely (last-wins per type, not per uid).
- Return the surviving assignments across all types.

**Removed from service layer:**
- `findRelatedEntities` — coordinate-based traversal deleted entirely.
- `MetadataEditService` — dissolved. Entity writes → `MetadataEntityService`, facet writes → `FacetService`.
- The old `set` / `delete` methods replaced by `assign` / `update` / `unassign` / `unassignAll`.

### 7.3 `FacetCatalog` interface + `DefaultFacetCatalog`

`FacetCatalog` provides the service-level view over both defined and observed facet types.
Services use it to **resolve cardinality** before writing `FacetInstance` rows; optional **`inspect`** is for **clients only** (§7.3).

```kotlin
// Interface in mill-metadata-core — pure
interface FacetCatalog {
    /** Resolve the runtime FacetType record for a given type key. Returns null if unknown. */
    fun findType(typeKey: String): FacetType?

    /** Resolve the FacetTypeDefinition for a given type key. Returns null if not defined. */
    fun findDefinition(typeKey: String): FacetTypeDefinition?

    /** All registered FacetTypeDefinitions. */
    fun listDefinitions(): List<FacetTypeDefinition>

    /** All runtime FacetTypes (defined + observed). */
    fun listTypes(): List<FacetType>

    /**
     * Validates a payload against the [FacetTypeDefinition.contentSchema] for the given type
     * and returns a [ValidationResult] describing conformance.
     *
     * This is a **helper provided for downstream clients** (e.g. [mill-data-schema-*]) that
     * want to enforce their own rules before calling [FacetService.assign]. The metadata
     * service itself **never calls this method internally** — it accepts any payload regardless
     * of schema conformance.
     *
     * Returns [ValidationResult.OK] if no definition exists for the type key (unknown type
     * passes through without error).
     */
    fun inspect(typeKey: String, payload: Map<String, Any?>): ValidationResult

    /**
     * Resolve the write-mode cardinality for a type key.
     * SINGLE = upsert (in-place update if assignment exists at scope).
     * MULTIPLE = always append a new assignment.
     * Defaults to **MULTIPLE** if no definition exists — most permissive.
     */
    fun resolveCardinality(typeKey: String): FacetTargetCardinality

    /**
     * Register or update a FacetTypeDefinition and the corresponding FacetType record.
     * Called by modules that own data-layer facet types (e.g. SchemaFacetAutoConfiguration).
     */
    fun registerDefinition(definition: FacetTypeDefinition): FacetTypeDefinition
}

// Implementation — plain class, no annotations, constructor-injected repositories
class DefaultFacetCatalog(
    private val definitionRepository: FacetTypeDefinitionRepository,
    private val facetTypeRepository: FacetTypeRepository
) : FacetCatalog { ... }
```

Jackson used via `JsonUtils.defaultJsonMapper()` from `mill-core` for schema validation.
No Spring annotations.

### 7.4 `MetadataScopeService` — plain class, same contract

```kotlin
class DefaultMetadataScopeService(
    private val scopeRepository: MetadataScopeRepository
) : MetadataScopeService { ... }
```

### 7.5 `MetadataImportService` — plain class

Parses YAML using `YamlUtils.defaultYamlMapper()` from `mill-core`. Validates entity ids are
full URNs. Delegates to `MetadataEntityService` and `FacetService`. No Spring annotations.

---

## 8. Persistence — DDL

### 8.1 Squash strategy

Delete **all metadata migrations V4–V10** (SQL files and Kotlin classes):

```
db/migration/V4__metadata.sql
db/migration/V5__metadata_facet_type_manifest.sql
db/migration/V6__metadata_facet_target_cardinality.sql
db/migration/V7__metadata_cleanup_legacy_facets_and_seed_new_platform_types.sql
db/migration/V8__metadata_jpa_facet_normalization.sql
db/migration/V9__metadata_surrogate_scope_entity.kt    ← Kotlin migration
db/migration/V10__metadata_facet_uid.kt                ← Kotlin migration
```

V1–V3 (AI/auth) are unrelated and stay. Introduce a single new **`V4__metadata_greenfield.sql`**
(first metadata version, plain SQL, no Kotlin migration) with the complete greenfield DDL.
No upgrade path — dev databases must be recreated from scratch.

The squashed migration is a **plain SQL file**: no Flyway Java/Kotlin migration class needed.
All transformations that V9/V10 did incrementally are expressed as direct `CREATE TABLE`
statements in the greenfield baseline.

### 8.2 Row audit convention

Every `metadata_*` table **except `metadata_audit`** carries:

```sql
created_at       TIMESTAMP NOT NULL,
created_by       VARCHAR(255),
last_modified_at TIMESTAMP NOT NULL,
last_modified_by VARCHAR(255)
```

`last_modified_*` replaces the previous `updated_*` column names.

**`metadata_audit`** is append-only and uses its own columns (`occurred_at`, `actor_id`, …) — **no** row-audit quad on that table.

**Cross-system `uuid` column:** Every auditable `metadata_*` business table (**except** `metadata_audit`) carries a column named **`uuid`** (`VARCHAR(36)` or native `UUID` type per dialect), **`NOT NULL`**, **`UNIQUE`** within the table, assigned on insert. Use case: replication, external references, and APIs that must not expose surrogate `BIGINT` PKs. On **`metadata_entity_facet`**, this **`uuid`** is the **same** stable identifier as the domain **`FacetInstance.uid`** and the REST path **`{facetUid}`** (naming: “uid” in API/domain, column **`uuid`** in DDL).

**Tables that also need the seed ledger:** `metadata_seed` (§14.1) follows the same row-audit + **`uuid`** pattern as other `metadata_*` tables.

### 8.3 Target tables

**FK design principle:** All foreign keys between `metadata_*` tables use surrogate `BIGINT`
columns. String URN columns (`*_res`, `type_res`) are unique business keys used for
external lookup only — they are **never** the target of a FK constraint. This aligns with
the existing JPA entity design and avoids string comparisons in hot join paths.

#### `metadata_scope`
```sql
-- Surrogate PK; scope_res holds the full URN for external lookup.
CREATE TABLE metadata_scope (
    scope_id         BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    uuid             VARCHAR(36)  NOT NULL,   -- cross-system stable id
    scope_res        VARCHAR(512) NOT NULL,   -- full scope URN, e.g. urn:mill/metadata/scope:global
    scope_type       VARCHAR(32)  NOT NULL,
    reference_id     VARCHAR(255),
    display_name     VARCHAR(512),
    owner_id         VARCHAR(255),
    visibility       VARCHAR(32)  NOT NULL DEFAULT 'PUBLIC',
    created_at       TIMESTAMP NOT NULL,
    created_by       VARCHAR(255),
    last_modified_at TIMESTAMP NOT NULL,
    last_modified_by VARCHAR(255),
    CONSTRAINT uq_metadata_scope_res UNIQUE (scope_res),
    CONSTRAINT uq_metadata_scope_uuid UNIQUE (uuid)
);
INSERT INTO metadata_scope (uuid, scope_res, scope_type, display_name, visibility,
    created_at, last_modified_at)
VALUES ('00000000-0000-4000-8000-000000000001', 'urn:mill/metadata/scope:global', 'GLOBAL', 'Global', 'PUBLIC',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
```

#### `metadata_entity`
```sql
-- Surrogate PK; entity_res holds the full entity URN for external lookup.
-- No schema/table/attribute columns — entity identity is fully opaque to metadata.
CREATE TABLE metadata_entity (
    entity_id        BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    uuid             VARCHAR(36)  NOT NULL,   -- cross-system stable id
    entity_res       VARCHAR(512) NOT NULL,   -- full entity URN (opaque; e.g. urn:mill/model/table:sales.orders)
    entity_kind      VARCHAR(255),            -- opaque label; e.g. "table", "attribute" — not validated
    created_at       TIMESTAMP NOT NULL,
    created_by       VARCHAR(255),
    last_modified_at TIMESTAMP NOT NULL,
    last_modified_by VARCHAR(255),
    CONSTRAINT uq_metadata_entity_res UNIQUE (entity_res),
    CONSTRAINT uq_metadata_entity_uuid UNIQUE (uuid)
);
```

#### `metadata_facet_type_def` (facet type definitions — declared upfront)
```sql
-- Surrogate PK; type_res holds the facet type URN for external lookup.
CREATE TABLE metadata_facet_type_def (
    def_id           BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    uuid             VARCHAR(36)  NOT NULL,   -- cross-system stable id
    type_res         VARCHAR(512) NOT NULL,   -- facet type URN, e.g. urn:mill/metadata/facet-type:descriptive
    manifest_json    TEXT         NOT NULL,   -- FacetTypeDefinition JSON (schema, cardinality, applicableTo, etc.)
    mandatory        BOOLEAN      NOT NULL DEFAULT FALSE,
    enabled          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP NOT NULL,
    created_by       VARCHAR(255),
    last_modified_at TIMESTAMP NOT NULL,
    last_modified_by VARCHAR(255),
    CONSTRAINT uq_metadata_facet_type_def_res UNIQUE (type_res),
    CONSTRAINT uq_metadata_facet_type_def_uuid UNIQUE (uuid)
);
-- Seeded from platform-facet-types.json (see §8.5)
```

#### `metadata_facet_type` (runtime/observed facet types)
```sql
-- Surrogate PK. def_id is an integer FK to metadata_facet_type_def — no string join.
CREATE TABLE metadata_facet_type (
    facet_type_id    BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    uuid             VARCHAR(36)  NOT NULL,   -- cross-system stable id
    type_res         VARCHAR(512) NOT NULL,   -- facet type URN (matches def.type_res if DEFINED)
    slug             VARCHAR(512),
    display_name     VARCHAR(512),
    description      TEXT,
    source           VARCHAR(32)  NOT NULL DEFAULT 'OBSERVED',   -- 'DEFINED' | 'OBSERVED'
    def_id           BIGINT REFERENCES metadata_facet_type_def(def_id),  -- null for OBSERVED
    created_at       TIMESTAMP NOT NULL,
    created_by       VARCHAR(255),
    last_modified_at TIMESTAMP NOT NULL,
    last_modified_by VARCHAR(255),
    CONSTRAINT uq_metadata_facet_type_res UNIQUE (type_res),
    CONSTRAINT uq_metadata_facet_type_uuid UNIQUE (uuid)
);
```

#### `metadata_entity_facet` (facet assignments — the hot table)

**Mandatory greenfield name:** `metadata_entity_facet` (replaces legacy `metadata_facet`). The squashed migration **drops** all prior metadata tables with **no** backward compatibility.

```sql
-- All FKs are integer (BIGINT) joins. No string FKs.
-- uuid is the stable external handle for a specific assignment (domain FacetInstance.uid / REST {facetUid}).
CREATE TABLE metadata_entity_facet (
    facet_id         BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    uuid             VARCHAR(36)  NOT NULL,   -- assignment id; UUID v4 at insert; immutable
    entity_id        BIGINT       NOT NULL REFERENCES metadata_entity(entity_id) ON DELETE CASCADE,
    facet_type_id    BIGINT       NOT NULL REFERENCES metadata_facet_type(facet_type_id),
    scope_id         BIGINT       NOT NULL REFERENCES metadata_scope(scope_id) ON DELETE CASCADE,
    payload_json     TEXT         NOT NULL DEFAULT '{}',
    merge_action     VARCHAR(32)  NOT NULL DEFAULT 'SET',   -- SET | TOMBSTONE | CLEAR (overlay semantics; not in payload_json)
    created_at       TIMESTAMP NOT NULL,
    created_by       VARCHAR(255),
    last_modified_at TIMESTAMP NOT NULL,
    last_modified_by VARCHAR(255),
    CONSTRAINT uq_metadata_entity_facet_uuid UNIQUE (uuid)
);
-- All join-path indexes use integer columns.
CREATE INDEX idx_metadata_entity_facet_entity    ON metadata_entity_facet(entity_id);
CREATE INDEX idx_metadata_entity_facet_type      ON metadata_entity_facet(facet_type_id);
CREATE INDEX idx_metadata_entity_facet_scope     ON metadata_entity_facet(scope_id);
-- Composite for the common resolution query (entity + type + scope):
CREATE INDEX idx_metadata_entity_facet_triple    ON metadata_entity_facet(entity_id, facet_type_id, scope_id);
```

**`merge_action` (v1):**

| Value | Meaning |
|--------|--------|
| **SET** (default) | Normal overlay: `payload_json` is the facet payload at this scope; **last-wins** across `MetadataContext` applies for **SINGLE**; **MULTIPLE** rules unchanged. |
| **TOMBSTONE** | While this scope is in the active context, the merged result behaves as if this facet type is **absent** for this entity (suppresses inherited **SET** from earlier scopes). Does not delete rows in other scopes. |
| **CLEAR** | This scope contributes nothing to the merge (same **effective** result as no row for that triple); row may remain for audit / no-DELETE policy. |

**`CLEAR` vs physical unassign (DELETE row):** When the merge engine skips a row, the **effective** view matches “no contribution” from that assignment; **operational** difference vs **`DELETE`** is auditability and retention. **HTTP `DELETE` / unassign rules** are normative in **§10.2** (`merge_action`, overlay vs non-overlay scopes).

String URN lookups in the service layer follow the two-step pattern:
1. Resolve URN → surrogate id (single indexed lookup on the unique `*_res` column).
2. Use the surrogate id for all facet queries (integer joins, no string comparison).

#### `metadata_audit` (unified operation audit — see §8.4)

#### `metadata_seed` (startup seed ledger — see §14.1)

Ledger for **`mill.metadata.seed.*`**: each seed resource has one row keyed by **`seed_key`**; **`fingerprint`** holds **`md5:`** + hex of raw bytes so content changes trigger a re-import (MERGE) and row update. Columns include stable **`seed_key`** (unique), row-audit quad + **`uuid`** (per §8.2), **`completed_at`**, **fingerprint** / **`last_error`**. No FK to facet rows — seeds drive **`MetadataImportService`** only.

**`metadata_promotion`:** **Not** part of this greenfield delivery. The legacy table and all related code are **removed** (promotion workflow **M-23** remains a future story).

### 8.4 Unified audit table

**All metadata operations write an audit row.** This replaces both the old
`metadata_operation_audit` and the draft `metadata_facet_investigation_audit`. One table,
all subjects.

Operations covered:

| `operation` value | Correlates with (listeners infer from JPA lifecycle / entity state) |
|-------------------|-------------|
| `ENTITY_CREATED` | `@PrePersist` on new entity record |
| `ENTITY_DELETED` | `@PreRemove` on entity record |
| `FACET_ASSIGNED` | `@PrePersist` on new facet assignment row |
| `FACET_UPDATED`  | `@PreUpdate` on facet assignment row |
| `FACET_UNASSIGNED` | `@PreRemove` on facet assignment row |
| `FACET_TYPE_REGISTERED` | `@PrePersist` on facet type def / runtime type as applicable |
| `FACET_TYPE_UPDATED` | `@PreUpdate` on facet type records |
| `FACET_TYPE_DELETED` | `@PreRemove` on facet type records |

```sql
CREATE TABLE metadata_audit (
    audit_id         BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    operation        VARCHAR(64)  NOT NULL,   -- value from the table above
    subject_type     VARCHAR(32)  NOT NULL,   -- 'ENTITY' | 'FACET' | 'FACET_TYPE'
    subject_ref      VARCHAR(512),            -- entity_res, assignment uuid, or type_res — human-readable ref
    actor_id         VARCHAR(255),            -- null for system/background operations
    correlation_id   VARCHAR(255),            -- request/trace id if available; null otherwise
    occurred_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payload_before   TEXT,                    -- JSON snapshot before mutation; null for CREATE
    payload_after    TEXT                     -- JSON snapshot after mutation; null for DELETE
);
CREATE INDEX idx_metadata_audit_subject   ON metadata_audit(subject_ref);
CREATE INDEX idx_metadata_audit_occurred  ON metadata_audit(occurred_at);
CREATE INDEX idx_metadata_audit_actor     ON metadata_audit(actor_id);
CREATE INDEX idx_metadata_audit_operation ON metadata_audit(operation);
```

**Writer (sole path):** JPA entity listeners on `MetadataEntityRecord`, **`MetadataEntityFacetEntity`**
(or the mapped name for **`metadata_entity_facet`**), `MetadataFacetTypeDefRecord`, and
`MetadataFacetTypeInstEntity`. One audit row per `@PrePersist`, `@PreUpdate`, `@PreRemove` event.
Services and REST layers **never** call `MetadataAuditRepository` directly.

**Fail-open:** listeners catch all `Exception`, log at WARN, never roll back the primary
transaction. A failed audit write must not block the operation.

### 8.5 Platform facet type seeds

**Source of truth:** `metadata/mill-metadata-core/src/main/resources/metadata/platform-facet-types.json`.

**Flyway SQL:** Generate **`INSERT`** statements for `metadata_facet_type_def` (and matching
`metadata_facet_type` **DEFINED** rows) **once** from that JSON as a **one-time** step when
authoring **`V4__metadata_greenfield.sql`**. **Afterwards, maintain JSON and SQL manually in sync**
when adding or changing platform types — reviewer diff is the enforcement mechanism (no
build-time code generation required).

**Facet types seeded in the metadata Flyway migration (generic, owned by metadata layer):**

| URN key | Owned by | Notes |
|---------|----------|-------|
| `urn:mill/metadata/facet-type:descriptive` | metadata-core | Generic: any entity can have a description, tags, display name |
| `urn:mill/metadata/facet-type:concept`     | metadata-core | Generic: business concept grouping; payload references opaque URNs |

**Facet types owned by the data layer (`mill-data-schema-core`) — NOT seeded in metadata migration:**

| URN key | Registered by |
|---------|---------------|
| `urn:mill/metadata/facet-type:structural`    | `mill-data-schema-core` — describes column data type, nullability, key flags |
| `urn:mill/metadata/facet-type:relation`      | `mill-data-schema-core` — FK and logical relations between database entities |
| `urn:mill/metadata/facet-type:value-mapping` | data/AI layer — NL-to-SQL value lookup tables |

Data-layer facet types are registered at application startup by the data module's autoconfiguration
(e.g. `SchemaFacetAutoConfiguration` calls `FacetCatalog.registerDefinition(...)` for each type it
owns). `registerDefinition` writes to both `metadata_facet_type_def` (the definition) and
`metadata_facet_type` (a corresponding `DEFINED` runtime record). They are **not** in the metadata
Flyway migration. The distinction is only about who owns and seeds them.

The squashed `V4__metadata_greenfield.sql` seeds `metadata_facet_type_def` (using `type_res`)
then inserts a matching `DEFINED` row into `metadata_facet_type` (using `type_res`) for each
generic platform type. Column names must match the greenfield DDL exactly — no `type_key`.

**Flyway vs `mill.metadata.seed`:** Platform facet **definitions** owned by metadata are **SQL in
Flyway**, sourced from **`platform-facet-types.json`** as above. **Additional** dataset / sample /
environment-specific metadata uses **ordered `mill.metadata.seed`** resources (§14.1). Ad-hoc
startup loads without a ledger are **abandoned**.

### 8.6 Database dialect

PostgreSQL is primary. H2 in PostgreSQL compatibility mode is required for `testIT`.
Use `TEXT` for JSON columns (not `JSONB`) to retain H2 compatibility.

### 8.7 Table and term registry (quick reference)

| Table | Role | Typical JPA / domain |
|-------|------|---------------------|
| `metadata_scope` | Scope registry | `MetadataScope` |
| `metadata_entity` | Entity identity (URN + kind) | `MetadataEntity` |
| `metadata_facet_type_def` | Declared facet type contracts | `MetadataFacetTypeEntity` (def) |
| `metadata_facet_type` | Runtime facet type (DEFINED \| OBSERVED) | `MetadataFacetTypeInstEntity` — **catalog row, not a facet assignment** |
| `metadata_entity_facet` | Facet **assignments** (entity + type + scope + payload + **merge_action**) | `FacetInstance` / `MetadataEntityFacetEntity` |
| `metadata_audit` | Append-only **operation** log | `MetadataAuditRecord` — **no** row-audit quad |
| `metadata_seed` | Seed ledger | ledger entity (§14.1) |

**Row audit** = `created_*` / `last_modified_*` columns on business tables. **`metadata_audit`** = separate table name for the operation log (do not confuse the two). Legacy names `metadata_operation_audit`, `metadata_facet_investigation_audit` are **not** recreated.

---

## 8a. Database Model

```
┌──────────────────────────────┐
│       metadata_scope         │
│──────────────────────────────│
│ PK  scope_id      BIGINT     │◄────────────────────────────────────┐
│     scope_res     VARCHAR    │  (unique URN; external lookup key)  │
│     scope_type    VARCHAR    │                                     │
│     reference_id  VARCHAR?   │                                     │
│     display_name  VARCHAR?   │                                     │
│     owner_id      VARCHAR?   │                                     │
│     visibility    VARCHAR    │                                     │
│     [row-audit]              │                                     │
└──────────────────────────────┘                                     │
                                                                     │ FK scope_id
┌──────────────────────────────┐       ┌────────────────────────────┴──┐
│      metadata_entity         │       │   metadata_entity_facet        │
│──────────────────────────────│       │────────────────────────────────│
│ PK  entity_id    BIGINT      │◄──────┤FK entity_id       BIGINT       │
│     entity_res   VARCHAR     │  FK   │PK  facet_id        BIGINT      │
│     entity_kind  VARCHAR?    │       │    uuid            VARCHAR(36) │◄── assignment id (= domain uid)
│     uuid         VARCHAR(36) │       │FK  facet_type_id   BIGINT      │
│     [row-audit]              │       │    payload_json    TEXT        │
└──────────────────────────────┘       │    merge_action    VARCHAR     │
                                       │    [row-audit]                 │
                                       └──────────┬─────────────────────┘
                                                  │ FK facet_type_id
                                       ┌──────────▼─────────────────────┐
                                       │    metadata_facet_type          │
                                       │────────────────────────────────│
                                       │ PK  facet_type_id  BIGINT      │
                                       │     type_res       VARCHAR     │  (unique URN)
                                       │     slug           VARCHAR?    │
                                       │     display_name   VARCHAR?    │
                                       │     source         VARCHAR     │  DEFINED|OBSERVED
                                       │ FK  def_id         BIGINT?     │──────────────────────┐
                                       │     [row-audit]                │                      │
                                       └────────────────────────────────┘                      │ FK def_id
                                                                         ┌────────────────────▼──┐
                                                                         │ metadata_facet_type_def│
                                                                         │────────────────────────│
                                                                         │ PK  def_id    BIGINT   │
                                                                         │     type_res  VARCHAR  │ (unique URN)
                                                                         │     manifest_json TEXT  │
                                                                         │     mandatory BOOLEAN  │
                                                                         │     enabled   BOOLEAN  │
                                                                         │     [row-audit]        │
                                                                         └────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────┐
│                          metadata_audit                                      │
│──────────────────────────────────────────────────────────────────────────────│
│ PK  audit_id        BIGINT                                                   │
│     operation       VARCHAR    ENTITY_CREATED | FACET_ASSIGNED | ...        │
│     subject_type    VARCHAR    ENTITY | FACET | FACET_TYPE                  │
│     subject_ref     VARCHAR?   entity_res, assignment uuid, or type_res       │
│     actor_id        VARCHAR?                                                 │
│     correlation_id  VARCHAR?                                                 │
│     occurred_at     TIMESTAMP                                                │
│     payload_before  TEXT?                                                    │
│     payload_after   TEXT?                                                    │
└──────────────────────────────────────────────────────────────────────────────┘
```

**Key design rules:**
- Every `metadata_*` business table has a surrogate `BIGINT` PK and a **`uuid`** column (except **`metadata_audit`**).
- All FK references between tables use `BIGINT` columns — never string/URN columns.
- String URN columns (`*_res`, `type_res`) carry `UNIQUE` constraints and are indexed for external
  lookups. They are never FK targets.
- `metadata_entity_facet` is the hot table. Its triple composite index
  `(entity_id, facet_type_id, scope_id)` covers the common resolution query.
- `metadata_audit` is append-only; no FKs to other tables (audit must survive deletes).

---

## 9. JPA Changes

The existing JPA entities are largely correct in their FK design. Changes are targeted:

### 9.1 `MetadataEntityRecord`

**Remove:**
- `entityType: String` (was `MetadataType` serialisation) → replaced by `entityKind: String?`
- `schemaName`, `tableName`, `attributeName` and their composite `UniqueConstraint`

**Rename:**
- `updatedAt` → `lastModifiedAt`, `updatedBy` → `lastModifiedBy`

**Keep:** `entityId: Long` (surrogate PK), `entityRes: String` (unique business key URN).

**Add:** `uuid: String` — maps to **`uuid`** column (§8.2).

### 9.2 `MetadataScopeEntity`

Surrogate `scopeId: Long` PK and `scopeRes: String` URN column.

**Add:** `uuid: String` — maps to **`uuid`** column (§8.2).

**Rename / complete row-audit:** add `lastModifiedAt`/`lastModifiedBy` for the row-audit quad if missing.

### 9.3 `MetadataFacetTypeEntity` (`metadata_facet_type_def`)

**Rename fields to match greenfield DDL:**
- `facetTypeDefId: Long` → `defId: Long` (column `def_id`)
- `typeRes: String` — already correct
- `updatedAt` → `lastModifiedAt`, `updatedBy` → `lastModifiedBy`

**Add:** `uuid: String` — column **`uuid`** (§8.2).

### 9.4 `MetadataFacetTypeInstEntity` (`metadata_facet_type`)

**FK field rename:** `facetTypeDef: MetadataFacetTypeEntity` (join column `facet_type_def_id`) → rename join column to `def_id` to match greenfield DDL.

**Rename:** `updatedAt` → `lastModifiedAt`, `updatedBy` → `lastModifiedBy`.

**Keep:** `facetTypeId: Long` (PK), `typeRes: String` (unique URN), `source: String`, integer FK to def.

**Add:** `uuid: String` — column **`uuid`** (§8.2).

### 9.5 `MetadataEntityFacetEntity` (`metadata_entity_facet`)

Maps the facet **assignment** table (**mandatory** rename from legacy `metadata_facet`). Uses **integer object references** (Long-to-Long FKs):

```kotlin
@ManyToOne(optional = false)
@JoinColumn(name = "entity_id", referencedColumnName = "entity_id")
var entity: MetadataEntityRecord          // BIGINT FK

@ManyToOne(optional = false)
@JoinColumn(name = "scope_id", referencedColumnName = "scope_id")
var scope: MetadataScopeEntity            // BIGINT FK

@ManyToOne(optional = false)
@JoinColumn(name = "facet_type_id", referencedColumnName = "facet_type_id")
var facetType: MetadataFacetTypeInstEntity  // BIGINT FK
```

**Columns:** surrogate PK, **`uuid`** (maps to domain `FacetInstance.uid` / REST `{facetUid}`), **`merge_action`**, **`payload_json`**, row-audit quad.

**Rename:** `updatedAt` → `lastModifiedAt`, `updatedBy` → `lastModifiedBy`.

### 9.6 JPQL / Spring Data query methods

The existing `MetadataFacetJpaRepository` has queries that navigate string columns
(`findByEntityEntityRes`, `countByEntityResAndFacetTypeResAndScopeRes`). These must change to
use the integer FK path via the object references (entity type **`MetadataEntityFacetEntity`**):

```kotlin
// Old — traverses string column
@Query("SELECT f FROM MetadataEntityFacetEntity f WHERE f.entity.entityRes = :entityRes")
fun findByEntityEntityRes(@Param("entityRes") entityRes: String): List<MetadataEntityFacetEntity>

// New — caller resolves entity first, then queries by surrogate id
fun findByEntityEntityId(entityId: Long): List<MetadataEntityFacetEntity>
fun findByEntityEntityIdAndFacetTypeFacetTypeId(entityId: Long, facetTypeId: Long): List<MetadataEntityFacetEntity>
fun findByEntityEntityIdAndFacetTypeFacetTypeIdAndScopeScopeId(
    entityId: Long, facetTypeId: Long, scopeId: Long
): List<MetadataEntityFacetEntity>
```

The `JpaFacetRepository` adapter resolves URN → surrogate id in one lookup per dimension
(entity, facet type, scope), then uses integer-FK queries for all facet reads/writes.

### 9.7 `MetadataAuditRecord` (new JPA entity)

Maps to `metadata_audit` table. Fields: `auditId: Long` (PK), `operation: String`,
`subjectType: String`, `subjectRef: String?`, `actorId: String?`, `correlationId: String?`,
`occurredAt: Instant`, `payloadBefore: String?`, `payloadAfter: String?`.

### 9.8 Audit listeners

`MetadataAuditListener` registered on `MetadataEntityRecord`, **`MetadataEntityFacetEntity`**,
`MetadataFacetTypeEntity`, and `MetadataFacetTypeInstEntity`:
- Injected with `MetadataAuditJpaRepository` via `ApplicationContext`.
- Resolves `actorId` from `SecurityContextHolder` (nullable — null for system operations).
- One row inserted into `metadata_audit` per `@PrePersist`, `@PreUpdate`, `@PreRemove`.
- **Fail-open:** catch `Exception`, log at WARN, never roll back the primary transaction.

---

## 10. REST API

### 10.1 Entity endpoints — identity only

`GET /api/v1/metadata/entities` — returns list of entity identity records.
- **Remove** `?schema=`, `?table=` query parameters (coordinate concepts gone).
- Optional `?kind=` filter — opaque kind string pass-through (e.g. `?kind=table`); metadata layer does not interpret it, just filters by equality.
- Response: `MetadataEntityDto` — contains `id` (URN), `kind` (nullable opaque string), `createdAt`, `lastModifiedAt`, `createdBy`, `lastModifiedBy`. **No type enum, no facets field.**

`GET /api/v1/metadata/entities/{id}` — returns single entity by URN.
- `{id}` must be a full entity URN or URN-slug. Dot-path ids return 400.
- Response: `MetadataEntityDto` (identity only).

`POST /api/v1/metadata/entities` — create entity. Body: `MetadataEntityDto` (`id` + optional `kind`).

`PUT /api/v1/metadata/entities/{id}` — replace entity identity record (`kind` updatable).

`DELETE /api/v1/metadata/entities/{id}` — hard delete entity and all its facet rows.

### 10.2 Facet endpoints — primary data surface

`GET /api/v1/metadata/entities/{id}/facets` — all facet assignments resolved across context.
- Returns `List<FacetInstanceDto>`.
- `?context=<scopeUrn,...>` query param: comma-separated scope URNs in priority order; defaults to global scope.

`GET /api/v1/metadata/entities/{id}/facets/{typeKey}` — all assignments for a facet type, resolved in context.
- `typeKey` accepts prefixed slug or full URN (normalised via `MetadataUrns.normaliseFacetTypePath`).
- Returns `List<FacetInstanceDto>` — list because MULTIPLE cardinality may return several assignments.

`POST /api/v1/metadata/entities/{id}/facets/{typeKey}` — assign a facet.
- Body: payload JSON object.
- `?scope=<scopeUrn>` query param — identifies the target scope; defaults to global.
- SINGLE cardinality: if an assignment already exists at that scope, updates payload in-place
  (same uid returned). Otherwise creates a new assignment.
- MULTIPLE cardinality: always creates a new assignment with a new uid.
- Returns `FacetInstanceDto` with the assignment `uid`.

`PATCH /api/v1/metadata/entities/{id}/facets/{typeKey}/{facetUid}` — update an existing assignment's payload.
- Body: payload JSON object (full replacement of payload).
- `facetUid` identifies the specific assignment. Returns 404 if uid not found.
- Returns updated `FacetInstanceDto`.

`DELETE /api/v1/metadata/entities/{id}/facets/{typeKey}/{facetUid}` — remove a specific assignment.
- `facetUid` is always required. Every assignment has a uid — use it.
- Returns 204 on success, 404 if uid not found.

`DELETE /api/v1/metadata/entities/{id}/facets/{typeKey}` — remove all assignments of a type at a scope.
- `?scope=<scopeUrn>` required.
- Applies the same **physical vs tombstone** rules as single-assignment delete for each row in the triple.

**Unassign semantics (`DELETE` / `unassign` / `unassignAll`):**

- **`merge_action == SET`:** A **physical row delete** is allowed **only** when the assignment’s scope is a **non-overlay** scope (v1: **`GLOBAL`** and any other scope type the product explicitly whitelists for hard delete — document in autoconfigure). For **overlay** scopes (v1: **`USER`**, **`TEAM`**, **`ROLE`**, **`CUSTOM`**, chat/session scopes, etc.), a delete request **must not** remove the row; persist **`TOMBSTONE`** instead (same `(entity, type, scope)` triple, updated `merge_action`).
- **`merge_action` is `TOMBSTONE` or `CLEAR`:** A delete request **must not** physically remove the row; set or retain **`TOMBSTONE`** per service rules (idempotent).

**Rationale:** Overlay layers participate in merged views; hard delete in those scopes would destroy audit trail and confuse inheritance. **`SET`** rows in **non-overlay** scopes may be removed when the operator intends true removal.

**Removed endpoints:**
- `DELETE /api/v1/metadata/entities/{id}/facet-instances/{facetUid}` — eliminated (replaced by uid in path above).
- `GET /api/v1/metadata/entities/{id}/history` — move to operation audit controller (or defer).

### 10.3 `MetadataEntityDto`

```json
{
  "id":             "urn:mill/model/table:sales.orders",
  "kind":           "table",
  "createdAt":      "2026-01-01T00:00:00Z",
  "lastModifiedAt": "2026-03-01T00:00:00Z",
  "createdBy":      "alice",
  "lastModifiedBy": "bob"
}
```

**Removed fields:** `schemaName`, `tableName`, `attributeName`, `type` (enum), `facets`.
`kind` is a nullable free-form string — the metadata layer does not validate its value.

### 10.4 `FacetInstanceDto`

```json
{
  "uid":          "550e8400-e29b-41d4-a716-446655440000",
  "facetType":    "urn:mill/metadata/facet-type:descriptive",
  "scope":        "urn:mill/metadata/scope:global",
  "payload":      { "description": "Order master table", "tags": ["finance"] },
  "createdAt":    "2026-01-01T00:00:00Z",
  "lastModifiedAt": "2026-03-01T00:00:00Z"
}
```

`uid` is always present in responses and is the stable handle clients use for update and delete.
**`merge_action` is not part of this DTO** — it is internal merge semantics (see §5.4, §8.3).

### 10.5 Facet merge trace (multi-scope UI)

When the UI shows **multiple scopes** in `MetadataContext`, operators may need to see **how** the effective view was produced (per-scope contributions and `merge_action`).

`GET /api/v1/metadata/entities/{id}/facets/merge-trace`

- **Query:** `?context=<scopeUrn,...>` — comma-separated scope URNs in **evaluation order** (same as §10.2).
- **Response:** `FacetMergeTraceDto` — structured **layers** (one per scope in order) listing contributions (`facetType`, assignment `uuid`, **`mergeAction`**, `payload` snapshot, flags such as **superseded** / **effective**). The last layer’s **effective** list aligns with **`GET .../facets`** (`FacetInstanceDto`) for the same context, but **`FacetInstanceDto` remains free of `merge_action`**.

OpenAPI: document `FacetMergeTraceDto` as the **diagnostic** surface; default metadata clients use **`GET .../facets`** only.

### 10.6 Scope endpoints (new)

`GET /api/v1/metadata/scopes` — list all registered scopes.
- Response: `List<MetadataScopeDto>`.
- Optional `?type=<scopeType>` filter (e.g. `?type=GLOBAL`, `?type=USER`).

`GET /api/v1/metadata/scopes/{scopeRes}` — get a single scope by URN.
- `{scopeRes}` is URL-encoded; normalised to lowercase before lookup.
- Returns 404 if not found.

`POST /api/v1/metadata/scopes` — register a new scope.
- Body: `MetadataScopeDto` (`res`, `scopeType`, optional fields).

`DELETE /api/v1/metadata/scopes/{scopeRes}` — remove scope.
- Cascades: all facet assignments at this scope are deleted.

`MetadataScopeDto`:
```json
{
  "res":         "urn:mill/metadata/scope:global",
  "scopeType":   "GLOBAL",
  "referenceId": null,
  "displayName": "Global",
  "ownerId":     null,
  "visibility":  "PUBLIC"
}
```

### 10.7 Facet-type, import/export, snapshot controllers

`MetadataFacetController` (facet type CRUD) — unchanged API surface.

`MetadataImportExportController`:
- YAML import (`POST /api/v1/metadata/import`) validates entity `id` is a full URN; rejects
  dot-path ids with 400. Parses canonical multi-document YAML via `MetadataYamlSerializer`.
- YAML export (`GET /api/v1/metadata/export`) — calls `MetadataSnapshotService.snapshotAll()`
  and returns the canonical YAML as `text/yaml; charset=utf-8`. Optional `?entities=` param
  for partial export via `snapshotEntities(...)`.

These two endpoints together make YAML the portable interchange format — import from file,
export from any backend (JPA or file), snapshot for backup or migration.

### 10.8 OpenAPI annotation pass

All controllers: `@Operation`, `@Parameter`, `@Schema` examples use full URN strings.
`MetadataEntityDto` docs note no coordinate fields.
Spec reflects final surface only — no `deprecated` operations.

---

## 11. YAML Import/Export

### 11.0 Two YAML shapes (why this looked ambiguous)

There are **two different YAML layouts** referenced in the repo; they are **not** interchangeable files.

| Shape | Where it appears | Role |
|--------|------------------|------|
| **A — Normative canonical** | **§15.2** — multi-document, top-level **`kind:`** (`MetadataEntity`, `FacetTypeDefinition`, `MetadataScope`) | **Interchange** for **import API**, **`mill.metadata.seed`**, **file repository**, **export/snapshot**. This is what new content must be written in. |
| **B — Migration / test envelope** | **`convert_to_canonical_yaml.py`** output — single root doc with **`metadataFormat: CANONICAL`**, **`formatVersion: 1`**, **`entities:`** tree, facet keys as full URNs | **Legacy → URN** exercise data and regression fixtures. Entity ids are often **`urn:mill/metadata/entity:<dot-path>`** from the script. |

**Runtime** entity URNs for catalog objects are typically **`urn:mill/model/table:…`** etc. (§5.2, codec in `mill-data-schema-core`). **Metadata accepts any** valid **`urn:mill/…`** entity id at import.

**Rule for implementers:** **`MetadataImportService`**, seeds, and file backend **must** implement **shape A (§15)** as the **supported** interchange. **Shape B** is **not** required to be a first-class import format; use the **Python script (or a dedicated mapper)** **offline** to convert legacy repo YAML toward **shape A**, or extend import with an **explicit** optional parser for **`metadataFormat: CANONICAL`** v1 — if you do, document it next to **`MetadataYamlSerializer`**. Do not leave “maybe multi-doc, maybe single-doc” implicit.

Worked examples (shape B) still live under:

- [`test/datasets/convert_to_canonical_yaml.py`](../../../../test/datasets/convert_to_canonical_yaml.py)
- [`test/datasets/skymill/skymill-meta-canonical.yaml`](../../../../test/datasets/skymill/skymill-meta-canonical.yaml), [`test/datasets/moneta/moneta-meta-canonical.yaml`](../../../../test/datasets/moneta/moneta-meta-canonical.yaml)

**Complete shape A (§15.2) fixtures** — facet definitions from
[`platform-facet-types.json`](../../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-facet-types.json),
global scope, and all entities from legacy `*-meta-repository.yaml`, generated by
[`test/datasets/build_multidoc_metadata_fixtures.py`](../../../../test/datasets/build_multidoc_metadata_fixtures.py):

- [`test/datasets/skymill/skymill-meta-multidoc-v1.yaml`](../../../../test/datasets/skymill/skymill-meta-multidoc-v1.yaml)
- [`test/datasets/moneta/moneta-meta-multidoc-v1.yaml`](../../../../test/datasets/moneta/moneta-meta-multidoc-v1.yaml)

### 11.1 Entity `id` validation

Import rejects any entity whose `id` does not start with `urn:mill/` (case-insensitive).
Operators must migrate YAML files offline. A migration note is provided in docs (not a runtime converter).

### 11.2 YAML format

Entity-level structure:
```yaml
entities:
  - id: "urn:mill/model/table:sales.orders"
    kind: "table"                        # opaque; set by whoever created the entity
    facets:
      "urn:mill/metadata/facet-type:descriptive":
        "urn:mill/metadata/scope:global":
          description: "Order master table"
```

No `type` enum. `kind` is nullable. Facet-types section and import modes unchanged semantics.

---

## 12. Data Layer Mapping — `mill-data-schema-*` Owns Schema/Table/Attribute

### 12.1 Responsibility boundary

`mill-metadata-core` has **no concept of schema, table, or attribute**. The data layer is the
sole owner of:

- The convention for encoding database coordinates into entity URNs.
- The definition and registration of database-specific facet types (`structural`, `relation`).
- The entity `kind` strings used for database objects (`"schema"`, `"table"`, `"attribute"`).
- Typed deserialisation of facet payloads into domain objects like `RelationFacet`, `StructuralFacet`.

### 12.2 `MetadataEntityUrnCodec` — in `mill-data-schema-core`

Defines the convention for encoding database coordinates into entity URNs using the
`urn:mill/model/<class>:<id>` group. Lives entirely in `mill-data-schema-core`.
`mill-metadata-core` does not define or depend on this class.

```kotlin
// In mill-data-schema-core
interface MetadataEntityUrnCodec {
    fun forSchema(schema: String): String
    // → urn:mill/model/schema:<lc-schema>

    fun forTable(schema: String, table: String): String
    // → urn:mill/model/table:<lc-schema>.<lc-table>

    fun forAttribute(schema: String, table: String, column: String): String
    // → urn:mill/model/attribute:<lc-schema>.<lc-table>.<lc-column>

    fun decode(urn: String): CatalogPath
    // (schema, table?, column?) — inverse of the above
}
```

All ids produced are lowercase. `decode` must handle both uppercase and lowercase input
(normalise before parsing).

`mill-metadata-core` retains only `MetadataEntityUrn.canonicalize(urn)` — a generic normaliser
that lowercases and trims any Mill URN string.

### 12.3 Entity kind constants — in `mill-data-schema-core`

```kotlin
// In mill-data-schema-core
object SchemaEntityKinds {
    const val SCHEMA    = "schema"
    const val TABLE     = "table"
    const val ATTRIBUTE = "attribute"
}
```

Used when calling `MetadataEntityService.create(MetadataEntity(id = urn, kind = "table", ...))`.

**Legacy `entities:` YAML** (fixture `type` + `schemaName` / `tableName` / `attributeName`): import normalises short `id` values to canonical relational entity URNs inside `MetadataYamlSerializer` (same local-part rules as the codec), without adding a Gradle dependency from `mill-metadata-core` to `mill-data-schema-core`.

### 12.4 `SchemaFacetService` registration of database-specific facet types

On startup, `SchemaFacetAutoConfiguration` registers the data-owned facet types by calling
`FacetTypeRepository.save(...)` for:
- `urn:mill/metadata/facet-type:structural` — column data type, nullability, key flags.
- `urn:mill/metadata/facet-type:relation` — FK and logical relations between entities.
- `urn:mill/metadata/facet-type:value-mapping` — NL-to-SQL value lookup tables.

These are stored in `metadata_facet_type_def` (same table as generic types) but are owned
and seeded by the data/AI module, not the metadata migration.

### 12.5 `RelationFacet` and `StructuralFacet` — in `mill-data-schema-core`

Typed Kotlin classes that deserialise the opaque JSON stored in `metadata_entity_facet.payload_json`.
Only `mill-data-schema-core` (and modules that depend on it) ever interact with these types.

Relation payload stored in DB (opaque to metadata layer):
```json
{
  "relations": [
    {
      "type": "FOREIGN_KEY",
      "cardinality": "MANY_TO_ONE",
      "sourceUrn": "urn:mill/metadata/entity:public.orders.customer_id",
      "targetUrn": "urn:mill/metadata/entity:public.customers.id"
    }
  ]
}
```

### 12.6 `SchemaFacetService` — lookup via URN

All metadata lookups in `SchemaFacetService` go through entity URNs built by `MetadataEntityUrnCodec`.
No coordinate arguments (`schema`, `table`, `attribute` strings) are passed to `MetadataEntityService`
or `FacetService`.

---

## 13. `mill-ui` Changes

### 13.1 Entity ids

All metadata API calls use full entity URNs. The UI constructs URNs from tree-node schema/
table/attribute segments via a `buildEntityUrn(schema, table?, column?)` utility function.
It never passes dot-path ids to the metadata REST API.

### 13.2 `schemaService.ts`

- Replace dot-path entity id construction with URN construction.
- Remove any code that stores or passes `schemaName`/`tableName`/`attributeName` from `MetadataEntityDto`.

### 13.3 `EntityDetails`

- Entity detail panel no longer receives or displays `schemaName`/`tableName`/`attributeName` from the metadata service (those fields come from the physical schema tree, not the metadata API).
- Facet delete: use **`DELETE .../facets/{typeKey}/{facetUid}`** (uid in path per §10.2). For bulk remove at a scope, use `DELETE .../facets/{typeKey}?scope=...`.
- The separate `/facet-instances/{uid}` call is removed (endpoint eliminated).

### 13.4 Import UI

YAML example in import dialog updated to show URN-format entity ids.

---

## 14. Configuration — Java `@ConfigurationProperties`

All changed `mill.metadata.*` keys must be in **Java** `@ConfigurationProperties` classes so
`spring-boot-configuration-processor` emits `META-INF/spring-configuration-metadata.json`.
**Do not** implement new property classes in Kotlin without **`META-INF/additional-spring-configuration-metadata.json`** — default is **Java** for IDE metadata.

### 14.0 Repository backend prefix (breaking rename)

**`mill.metadata.storage.*` is removed.** Use **`mill.metadata.repository.*`**:

| Key | Meaning |
|-----|---------|
| `mill.metadata.repository.type` | `jpa` \| `file` \| `noop` — selects the metadata **repository** implementation |
| `mill.metadata.repository.file.path` | Directory or classpath for file backend (§15) |
| `mill.metadata.repository.file.writable` | Allow writes back to files |
| `mill.metadata.repository.file.watch` | Reload on change (dev) |

No silent alias for the old `storage` prefix unless product explicitly requires a deprecation release.

### 14.1 Startup metadata seeding — `mill.metadata.seed`

**Replaces** ad-hoc on-startup metadata loading without idempotency.

| Aspect | Rule |
|--------|------|
| **Config** | Ordered list of **Spring `Resource` locations** (e.g. `classpath:metadata/seeds/01-platform.yml`). List order = execution order. |
| **Idempotency** | **JPA** mode persists completion in **`metadata_seed`** via **`MetadataSeedLedgerRepository`**. **`seed_key`** identifies the resource (trimmed location, or canonical **`file:`** URI for existing file resources) — **not** the list index. **`fingerprint`** stores a content digest (**`md5:`** + hex of raw bytes); if the file changes, the seed is **re-applied** (MERGE) and the row is updated. Unchanged content is skipped after the first successful run. |
| **Import** | Same canonical YAML / import semantics as **`MetadataImportService`** (§7.5, §11) — not a second format. |
| **Failure** | SPEC recommends **fail-fast** after recording attempt, or document **continue** policy explicitly. |

Core contract: **`MetadataSeedLedgerRepository`** (§6.6). Runner bean: **`mill-metadata-autoconfigure`**.

---


## 15. File Repository — Canonical YAML Format

The file-backed repository is **kept and redesigned** around a canonical multi-document YAML
format that mirrors the domain model exactly. The old ad-hoc format is replaced entirely.

### 15.1 Storage backend

`mill.metadata.repository.type=file` remains a valid configuration value.
`mill.metadata.repository.file.path` points to a directory containing one or more `.yml` / `.yaml`
files. The repository loads all files in the directory on startup (read-only or read-write
depending on configuration). On write, changes are appended or merged in-place.

The file adapter implements the same repository interfaces as the JPA adapter:
`MetadataEntityRepository`, `FacetRepository`, `FacetTypeDefinitionRepository`,
`FacetTypeRepository`, `MetadataScopeRepository`. **`MetadataAuditRepository`** is implemented
only inside **`mill-metadata-persistence`** (file adapter appends to **`_audit.yml`**). **Domain
services never inject or call it** — same rule as JPA mode (audit is not a service concern).

### 15.2 Canonical YAML format — multi-document

Each YAML file may contain **multiple documents** separated by `---`. Every document has a
top-level `kind:` discriminator. Unknown `kind` values are skipped with a WARN log.

Supported kinds: `FacetTypeDefinition`, `MetadataScope`, `MetadataEntity`.

#### `FacetTypeDefinition` document

One document per facet type definition. Multiple definitions in one file are supported.

```yaml
---
kind: FacetTypeDefinition
typeRes: urn:mill/metadata/facet-type:descriptive
displayName: Descriptive
description: Human-readable metadata — description, tags, display name
mandatory: false
enabled: true
targetCardinality: SINGLE   # SINGLE | MULTIPLE
applicableTo:               # opaque entity kind strings; omit for "any"
  - table
  - attribute
  - schema
contentSchema:              # JSON Schema object; omit for no schema
  type: object
  properties:
    description: { type: string }
    displayName: { type: string }
    tags:
      type: array
      items: { type: string }
```

#### `MetadataScope` document

```yaml
---
kind: MetadataScope
scopeRes: urn:mill/metadata/scope:global
scopeType: GLOBAL           # GLOBAL | USER | TEAM | ROLE | CUSTOM
referenceId: ~              # null for GLOBAL
displayName: Global
visibility: PUBLIC          # PUBLIC | PRIVATE
```

#### `MetadataEntity` document

Each entity is a separate document. Its facet assignments are embedded inline.

```yaml
---
kind: MetadataEntity
entityRes: urn:mill/model/table:sales.orders
entityKind: table           # opaque; omit if untyped
facets:
  - uid: 550e8400-e29b-41d4-a716-446655440000   # omit → generated on load
    facetType: urn:mill/metadata/facet-type:descriptive
    scope: urn:mill/metadata/scope:global
    payload:
      description: Order master table
      tags: [finance, orders]
  - uid: ~                                       # null → generated
    facetType: urn:mill/metadata/facet-type:concept
    scope: urn:mill/metadata/scope:global
    payload:
      conceptRef: urn:mill/model/concept:revenue
```

### 15.3 YAML loading rules

| Rule | Detail |
|------|--------|
| **Case normalisation** | `typeRes`, `entityRes`, `scopeRes`, `uid` are lowercased on load. |
| **uid generation** | If `uid` is absent or null, a stable UUID is generated deterministically from `(entityRes, facetType, scope, position-in-list)` so repeated loads produce the same uid for the same assignment. |
| **Unknown facet types** | Facet assignments referencing an unregistered `facetType` are loaded as `OBSERVED` (permissive — no rejection). |
| **Unknown scopes** | Scope URNs referenced in facet assignments that are not declared in any loaded `MetadataScope` document are auto-created as ephemeral in-memory scope records. |
| **Duplicate entities** | If the same `entityRes` appears in multiple documents across files, assignments are **merged** — the later document's facets are appended (MULTIPLE) or override (SINGLE) per cardinality rules. |
| **Read order** | Files loaded in lexicographic name order within the directory; documents processed top-to-bottom within each file. |

### 15.4 Serialisation — `MetadataYamlSerializer`

The canonical YAML format is bidirectional. A `MetadataYamlSerializer` utility lives in
`mill-metadata-core` (uses only `YamlUtils` from `mill-core`, works on pure domain types):

```kotlin
/**
 * Converts domain objects to and from the canonical multi-document YAML format.
 * Used by the file repository for both load and save, and exposed for snapshot exports
 * from any repository backend (e.g. dump JPA state to YAML).
 */
object MetadataYamlSerializer {

    /** Serialise a mixed collection of domain objects to a multi-document YAML string. */
    fun serialize(
        scopes: List<MetadataScope> = emptyList(),
        definitions: List<FacetTypeDefinition> = emptyList(),
        entities: List<MetadataEntity> = emptyList(),
        facetsByEntity: Map<String, List<FacetInstance>> = emptyMap()
    ): String

    /** Parse a multi-document YAML string into typed domain bags. */
    fun deserialize(yaml: String): MetadataYamlDocument
}

/** All objects parsed from one or more YAML documents. */
data class MetadataYamlDocument(
    val scopes: List<MetadataScope>,
    val definitions: List<FacetTypeDefinition>,
    val entities: List<MetadataEntity>,
    val facetsByEntity: Map<String, List<FacetInstance>>   // key = entityRes
)
```

### 15.5 Snapshot export — `MetadataSnapshotService`

`MetadataSnapshotService` reads the current state of any set of repositories and serialises
it to the canonical YAML format via `MetadataYamlSerializer`. Lives in `mill-metadata-core`.

```kotlin
/**
 * Captures the current state of the provided repositories as canonical YAML.
 * Use this to snapshot a JPA repository to a file, seed test fixtures, or
 * export configuration-as-code from a running system.
 */
class DefaultMetadataSnapshotService(
    private val entityRepository: MetadataEntityRepository,
    private val facetRepository: FacetRepository,
    private val definitionRepository: FacetTypeDefinitionRepository,
    private val scopeRepository: MetadataScopeRepository
) : MetadataSnapshotService {

    /**
     * Export all entities, their facet assignments, all facet type definitions,
     * and all scopes to canonical multi-document YAML written to [out].
     */
    fun snapshotAll(out: Writer)

    /**
     * Export a subset — only the given entities and their facets.
     * Definitions and scopes referenced by those facets are included automatically.
     */
    fun snapshotEntities(entityIds: List<String>, out: Writer)
}
```

The `MetadataSnapshotService` is wired as a Spring bean in `mill-metadata-autoconfigure`
alongside the other services.

### 15.6 Implementation location

- `MetadataYamlSerializer` and `MetadataSnapshotService` — `mill-metadata-core`
  (pure domain types + `YamlUtils`; no Spring, no JPA).
- File repository adapters (`FileMetadataEntityRepository`, `FileFacetRepository`, etc.) —
  `mill-metadata-persistence`, using `MetadataYamlSerializer` for load/save.
- `FileMetadataRepositoryFactory` wired by `MetadataRepositoryAutoConfiguration` when
  `mill.metadata.repository.type=file`.
- Old `FileMetadataRepository`, `FileFacetTypeRepository`, `ResourceResolver`,
  `SpringResourceResolver` are **deleted**. New implementation is a clean rewrite.

### 15.7 `mill.metadata.repository.file.*` configuration

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `mill.metadata.repository.file.path` | `String` | `classpath:metadata/` | Directory or classpath path to load |
| `mill.metadata.repository.file.writable` | `Boolean` | `false` | Allow write operations back to files |
| `mill.metadata.repository.file.watch` | `Boolean` | `false` | Reload on file change (dev mode) |

---

## 16. Breaking Changes Summary

| Layer | What breaks |
|-------|-------------|
| Domain | `MetadataEntity`: `schemaName`, `tableName`, `attributeName`, `facets` fields removed; `MetadataType.CATALOG` removed |
| Domain | `MetadataRepository.findByLocation` removed |
| Domain | `MetadataService` dissolved into `MetadataEntityService` + `FacetService`; `MetadataEditService` dissolved |
| Domain | `RelationFacet`, `EntityReference` moved to `mill-data-schema-core` |
| Domain | `MetadataFacetInstanceRow` replaced by `FacetInstance` |
| Persistence | Flyway V4–V10 deleted; single new `V4__metadata_greenfield.sql`. Fresh DB required. |
| Persistence | `metadata_entity` loses coordinate columns; `updated_*` → `last_modified_*` everywhere |
| Persistence | **`metadata_facet` → `metadata_entity_facet`**; assignment id column **`uuid`**; **`uuid`** on all business `metadata_*` tables (except `metadata_audit`) |
| Persistence | **Audit** only via **JPA listeners** — services do not call `MetadataAuditRepository` |
| Domain / data | **`MetadataView`** required for **read** metadata in **`data/*`** (no direct repository use for those reads) |
| REST | **`GET .../facets/merge-trace`** for merge chain; **`FacetInstanceDto`** excludes **`mergeAction`** |
| REST | **Unassign:** hard delete only for **`SET`** in **non-overlay** scopes; otherwise **TOMBSTONE** |
| REST | `GET /entities`: `?schema=`/`?table=` params removed |
| REST | Entity `{id}` path: dot-path ids rejected (400) |
| REST | `MetadataEntityDto`: coordinate and `facets` fields removed |
| REST | `DELETE .../facet-instances/{facetUid}` endpoint removed |
| YAML | Entity `id` must be full URN; dot-path ids rejected at import |
| Config | **`mill.metadata.storage.*` removed** — use **`mill.metadata.repository.*`**; old storage keys and legacy file **format** invalid; YAML must be canonical multi-document (§15) |

---

## 17. Test Requirements

### Unit
- `MetadataEntityUrn`: round-trip encode/decode for all entity types.
- `FacetService`: scope resolution (last-wins), SINGLE vs MULTIPLE cardinality enforcement.
- `MetadataEntityService`: URN validation on create; 404 on missing entity.
- `MetadataEntityController`: 400 on dot-path `{id}`; no `?schema=`/`?table=` params; entity DTO has no coordinate fields.
- **`FacetInstanceDto`** / **`GET .../facets`:** no `mergeAction` field; **`merge-trace`** exposes it.
- `MetadataView`: effective view matches `FacetService.resolve` for same context.
- Import validator: 400 on dot-path entity id in YAML.

### Integration (`testIT`)
- Fresh Flyway migrate on H2 PostgreSQL mode succeeds; schema matches JPA entities.
- **`metadata_audit`** rows written on facet insert / update / delete (**listeners only** — no service-layer audit calls).
- `FacetRepository`: SINGLE overwrite; MULTIPLE append; scope-based resolution.
- **Unassign:** overlay scope + **`SET`** → row becomes **`TOMBSTONE`** (no physical delete); **GLOBAL** + **`SET`** → physical delete allowed (adjust if product whitelist differs).
- JDBC binding: **`MetadataView`** (or equivalent) used for read paths in data layer; `SchemaFacetService` / fixtures resolve by URN.
- `mill-ui` (Vitest): URN construction in `schemaService.ts`; facet delete uses **path** `{facetUid}` (§10.2); optional **`merge-trace`** when multi-scope UI ships.

### Module boundary
`./gradlew :metadata:mill-metadata-core:dependencies` — no `data/*` artifacts.

---

## 18. Implementation Order

1. **Design lock** (URN grammar, `FacetInstance` + **`merge_action`**, `MetadataEntityUrnCodec` contract, **`metadata_audit`** shape, REST surface). Unblocks all other items.
2. **Domain + service rework** (`MetadataEntity` strip coordinates; **`FacetInstance`** + **`MetadataReader`** / merge semantics; **`MetadataView`** for downstream reads; `MetadataEntityService` + `FacetService` **without** audit dependencies; move `RelationFacet` to `mill-data-schema-core`).
3. **JDBC binding** (`MetadataEntityUrnCodec` + `SchemaFacetService` URN integration in `mill-data-schema-core`). Parallel with domain rework.
4. **Persistence** (delete old metadata migrations; write squashed **`V4__metadata_greenfield.sql`**; **`metadata_entity_facet`**; **`metadata_seed`**; **drop `metadata_promotion`**; row-audit + **`uuid`** on business tables; **`merge_action`**; JPA + **`metadata_audit`** listeners **only**).
5. **REST + import/export + mill-ui** (endpoint changes; **`merge-trace`**; OpenAPI pass; YAML §15 validation; UI URN construction; facet delete uid **in path**; unassign rules §10.2).
6. **Startup seed + config rename** (**WI-126**): `mill.metadata.seed`, ledger, runner; **`mill.metadata.repository.*`** (**WI-124**).
7. **Cleanup** (legacy file **format** removal; dead code sweep; Java `@ConfigurationProperties`; tests).
8. **Design docs as-built sync** (`docs/design/metadata/` reconciled with final DDL, JPA, APIs, audit).
