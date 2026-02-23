# WI-006: Facet Type Library (Facet Catalog)

**Type:** feature
**Priority:** high
**Rules:** See [RULES.md](RULES.md)
**Branch name:** `feat/wi-006-facet-type-catalog`
**Depends on:** WI-005 (legacy metadata elimination must be completed first)

---

## Goal

Introduce a persistent, per-deployment configurable catalog of facet types. Each
facet type is a first-class entity that can be stored, queried, and managed
through the service layer and REST API. Facet types define what kinds of facets
are allowed in a deployment, which entity types they can attach to, and
optionally how their content is validated.

## Problem

The current `FacetRegistry` is a global singleton that maps a type key string to a
Java class. It has no concept of:

- **Facet type is not stored with facet data** — the type key exists only as the
  outer map key in the entity's facets map. If facet data is extracted or moved,
  there is no self-describing marker inside the JSON bag that identifies what type
  of facet it is. The type is positional, not intrinsic.
- **No persistent type definitions** — facet types only exist as in-memory
  registrations. There is no repository or storage for the type definitions
  themselves. You cannot query "what facet types exist in this deployment" from
  the database — only from application memory.
- **No target type validation** — nothing prevents attaching a `structural` facet
  to a CONCEPT entity or a `relation` facet to an ATTRIBUTE. The `applicableTo`
  constraint exists only in documentation, not in code.
- **No content validation** — the Java `validate()` method on each facet class is
  a hand-coded check. There is no declarative schema (JSON Schema or similar) that
  validates facet content structure. Custom facet types from customers have no way
  to declare their expected shape without writing Java code.
- **Mandatory vs optional** — all facet types are equal; no enforcement that core
  facets must always be present.
- **Per-deployment configuration** — every deployment gets the same hardcoded set
  registered in `MetadataCoreConfiguration.@PostConstruct`.

---

## Design: Facet Type Descriptor (Persistent)

The facet type descriptor is a **first-class persistable entity**, not just an
in-memory registration. It is stored in the same repository infrastructure as
metadata entities (file, DB, etc.) and can be managed (CRUD) through the service
layer and REST API.

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacetTypeDescriptor implements Serializable {
    private String typeKey;                       // PK: "descriptive", "value-mapping", "customer-audit"
    private boolean mandatory;                    // true = platform-required, cannot be disabled
    private boolean enabled;                      // false = disabled in this deployment
    private String displayName;                   // human-readable name
    private String description;                   // what this facet type represents
    private Set<MetadataTargetType> applicableTo; // which entity types this facet can attach to (nullable = ANY)
    private String version;                       // schema version for evolution tracking
    private Map<String, Object> contentSchema;    // JSON Schema as a JSON bag (nullable = no validation)

    // Audit
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
```

Persistence-friendly design — same principles as `MetadataEntity`:

- **All fields are plain persistable types.** No `Class<?>` references — Java
  class binding is a runtime concern resolved by the catalog, not stored in the
  repository. The descriptor stores the `typeKey`; the platform code maps known
  type keys to Java classes at startup.
- **`contentSchema` is a JSON bag** (`Map<String, Object>`), stored as JSONB in
  DB or inline YAML in file. Not a typed Java object.
- **`applicableTo` is a set of enum values**, stored as `VARCHAR[]` or a
  comma-separated string in DB, or a YAML list in file.
- **No `ObjectMapper` or serialization logic** in the class — it is a plain data
  holder.
- **`Serializable`** for cache/transfer readiness.
- **Audit fields** (`createdAt`, `updatedAt`, `createdBy`, `updatedBy`) same as
  `MetadataEntity`.
- **No-arg constructor** via Lombok for JPA/deserialization compatibility.

Java class resolution for platform facet types happens outside the descriptor:

```java
public interface FacetClassResolver {
    Optional<Class<? extends MetadataFacet>> resolve(String typeKey);
}
```

Platform code registers known mappings (`"descriptive" → DescriptiveFacet.class`).
Custom facet types with no Java class are deserialized as raw
`Map<String, Object>` — the resolver returns `Optional.empty()`.

### Target Table Layout (DB)

```
facet_type
├── type_key        VARCHAR   PK
├── mandatory       BOOLEAN
├── enabled         BOOLEAN
├── display_name    VARCHAR
├── description     TEXT
├── applicable_to   VARCHAR[]       — or comma-separated string
├── version         VARCHAR
├── content_schema  JSONB           — the JSON Schema document (nullable)
├── created_at      TIMESTAMP
├── updated_at      TIMESTAMP
├── created_by      VARCHAR
└── updated_by      VARCHAR
```

Same persistence pattern as `metadata_entity`: minimal indexed columns, JSON bag
for the schema, audit trail. No `Class<?>` or Java-specific types in the table.

---

## Facet Type Stored With Every Facet Instance

Every facet instance in the JSON bag must carry an explicit `_type` marker so the
data is self-describing:

```yaml
entities:
  - id: moneta.customers
    type: TABLE
    schemaName: moneta
    tableName: customers
    facets:
      descriptive:
        global:
          _type: descriptive            # explicit type marker
          displayName: Customers
          description: Customer master data
      structural:
        global:
          _type: structural             # explicit type marker
          physicalName: CUSTOMERS
          tableType: TABLE
      customer-audit:
        global:
          _type: customer-audit         # custom facet, validated by JSON Schema
          lastAuditDate: "2026-01-15"
          auditor: "compliance-team"
          status: "passed"
```

The `_type` field is redundant with the map key but serves a different purpose:
- The **map key** is the storage location (where the facet lives in the entity).
- The **`_type` field** is the identity of the facet content (what it is).

This enables:
- Extracting a facet bag from the entity and still knowing its type.
- DB-level queries like `facets->'descriptive'->'global'->>'_type'` for integrity
  checks.
- Import/export where facet data travels separately from the entity structure.
- Future use cases where a single map slot could hold versioned or polymorphic
  facet content.

The `_type` field is injected automatically by the repository on write and
validated on read. It does not need to be manually specified in every YAML file —
the system adds it if missing based on the map key.

---

## Target Type Validation

### Revise `MetadataType` → `MetadataTargetType`

The current `MetadataType` enum serves dual purpose: it is the entity type AND the
implicit target classifier for facets. These should be explicitly separated or at
minimum the enum should be reviewed for completeness:

```java
public enum MetadataTargetType {
    CATALOG,
    SCHEMA,
    TABLE,
    ATTRIBUTE,
    CONCEPT,
    ANY               // facet applies to all entity types
}
```

Whether this is a separate enum or the existing `MetadataType` with an `ANY` value
added is an implementation decision. The key requirement is that `FacetTypeDescriptor`
declares which target types it supports.

### Validation Rules

Target type validation is **optional** — it only applies when the facet type
descriptor explicitly defines `applicableTo` constraints. Facet types that omit
`applicableTo` (or set it to `ANY`) are unrestricted.

When attaching a facet to an entity:

1. Look up the `FacetTypeDescriptor` by the facet's `_type` (or map key).
2. If the descriptor defines `applicableTo` (non-empty, not `ANY`), check that
   the entity's `type` is in the set. Reject if not.
3. If `applicableTo` is empty or contains `ANY`, skip the target check — facet
   is allowed on any entity type.

Examples:
- `structural` facet on a TABLE entity → allowed (`applicableTo: TABLE, ATTRIBUTE`)
- `structural` facet on a CONCEPT entity → rejected
- `relation` facet on an ATTRIBUTE entity → rejected (`applicableTo: TABLE`)
- `customer-audit` with no `applicableTo` defined → allowed on any entity type

---

## Content Validation (JSON Schema)

Content validation is **optional**. It applies only when the facet type descriptor
defines a `contentSchema`. Facet types without a `contentSchema` accept any
content structure — the JSON bag is stored as-is without validation.

### Schema Storage

The JSON Schema is stored as part of the `FacetTypeDescriptor`. It is nullable —
most facet types may not need it:

```yaml
facet-types:
  - typeKey: descriptive
    mandatory: true
    displayName: Descriptive
    applicableTo: [SCHEMA, TABLE, ATTRIBUTE]
    version: "1.0"
    # no contentSchema — content is not validated

  - typeKey: customer-audit
    mandatory: false
    displayName: Customer Audit Trail
    applicableTo: [TABLE]
    version: "1.0"
    contentSchema:                                # defines expected shape
      type: object
      properties:
        lastAuditDate:
          type: string
          format: date
        auditor:
          type: string
        status:
          type: string
          enum: [passed, failed, pending]
      required: [lastAuditDate, status]
```

### Validation Flow

On `MetadataService.save()`, for each facet on the entity:

1. **Type check** — is the facet type key known in the catalog? If the catalog
   has no descriptor for this type key, the facet is accepted as-is (unknown
   types are not rejected — they are simply untyped bags).
2. **Target check** — only if the descriptor defines `applicableTo`.
3. **Schema check** — only if the descriptor defines `contentSchema`.

All three checks are opt-in per facet type. A minimally defined facet type
(just `typeKey` and `displayName`) skips both target and content validation.

For platform facets with a Java class, the `validate()` method is an additional
programmatic check that runs alongside (not instead of) the schema check.
Neither is mandatory — they apply only when defined.

### Library Choice

Use an existing JSON Schema validation library for Java. Candidates:
- `networknt/json-schema-validator` (lightweight, standalone)
- `everit-org/json-schema` (well-established)

The library should be a dependency of `mill-metadata-core`, not autoconfigure.
It is only invoked when a `contentSchema` is present on the descriptor.

---

## Facet Type Repository

Facet type descriptors are persisted in the same storage as metadata entities.
For the file-based repository, they live in a dedicated YAML file or a
`facet-types` section in the metadata YAML:

```yaml
facet-types:
  - typeKey: descriptive
    mandatory: true
    displayName: Descriptive
    applicableTo: [SCHEMA, TABLE, ATTRIBUTE]
    version: "1.0"

  - typeKey: customer-audit
    mandatory: false
    displayName: Customer Audit Trail
    description: Tracks compliance audit status per entity
    applicableTo: [TABLE]
    version: "1.0"
    contentSchema:
      type: object
      properties:
        lastAuditDate:
          type: string
          format: date
        auditor:
          type: string
        status:
          type: string
          enum: [passed, failed, pending]
      required: [lastAuditDate, status]

entities:
  - id: moneta.customers
    ...
```

Repository interface (separate from `MetadataRepository`):

```java
public interface FacetTypeRepository {
    void save(FacetTypeDescriptor descriptor);
    Optional<FacetTypeDescriptor> findByTypeKey(String typeKey);
    Collection<FacetTypeDescriptor> findAll();
    void deleteByTypeKey(String typeKey);
    boolean existsByTypeKey(String typeKey);
}
```

---

## Mandatory Facet Types

A small fixed set that every deployment must support. These are registered by the
platform and cannot be disabled or deleted:

| Type Key       | Applies To               | Rationale                        |
|----------------|--------------------------|----------------------------------|
| `structural`   | TABLE, ATTRIBUTE         | Physical schema binding          |
| `descriptive`  | SCHEMA, TABLE, ATTRIBUTE | Human-readable metadata          |
| `relation`     | TABLE                    | Cross-entity relationships       |

## Optional (Customer-Configurable) Facet Types

Ship with the platform but can be enabled/disabled per deployment. Customers can
also create entirely new facet types via YAML/API (no Java code required if they
provide a JSON Schema).

| Type Key        | Applies To     | Default |
|-----------------|----------------|---------|
| `concept`       | CONCEPT        | enabled |
| `value-mapping` | ATTRIBUTE      | enabled |

---

## Catalog API

```java
public interface FacetCatalog {
    // CRUD
    void register(FacetTypeDescriptor descriptor);
    void update(FacetTypeDescriptor descriptor);
    void delete(String typeKey);                   // fails for mandatory types

    // Queries
    Optional<FacetTypeDescriptor> get(String typeKey);
    Collection<FacetTypeDescriptor> getAll();
    Collection<FacetTypeDescriptor> getEnabled();
    Collection<FacetTypeDescriptor> getMandatory();
    Collection<FacetTypeDescriptor> getForTargetType(MetadataTargetType targetType);

    // Checks
    boolean isAllowed(String typeKey);
    boolean isMandatory(String typeKey);
    boolean isApplicableTo(String typeKey, MetadataTargetType targetType);

    // Validation
    ValidationResult validateFacetContent(String typeKey, Object facetData);
    ValidationResult validateEntityFacets(MetadataEntity entity);
}
```

---

## Registration Sources

Three sources of facet type descriptors, merged at startup:

1. **Platform core** (`mill-metadata-core`) — registers mandatory facets
   programmatically. These are always present, always enabled.
2. **Platform optional** (`mill-metadata-core` or extension modules) — registers
   optional platform facets with `mandatory=false`.
3. **Customer definitions** — loaded from the facet type repository (YAML file
   or DB). Customers define custom facet types with JSON Schema and
   `applicableTo` constraints. No Java code needed.

On startup, the catalog merges all three sources. Mandatory types from source 1
override any attempt to disable them via configuration or repository.

---

## Configuration

Per-deployment facet catalog is driven by configuration (Spring properties or YAML):

```yaml
mill:
  metadata:
    facets:
      enabled:
        - descriptive      # mandatory — always on regardless of config
        - structural        # mandatory
        - relation          # mandatory
        - concept           # optional — customer chose to enable
        - value-mapping     # optional — customer chose to enable
        - customer-audit    # custom facet type from customer's extension module
      disabled:
        - enrichment        # optional — customer chose to disable
```

Mandatory facet types ignore the `disabled` list — they are always active.

---

## Impact on Current Code

- `FacetRegistry` singleton is replaced by injectable `FacetCatalog`.
- `MetadataCoreConfiguration.@PostConstruct` registration changes to contribute
  `FacetTypeDescriptor` beans (with JSON Schemas for platform facets).
- `FileMetadataRepository` loads `facet-types` section alongside `entities`.
- `MetadataService.save()` gains optional validation — target type and content
  schema checks run only when the facet type descriptor defines them.
- `MetadataFacet.validate()` remains for Java-level checks on platform facets.
  It is not required — facet types without `validate()` logic or `contentSchema`
  simply skip validation.
- REST controllers in `mill-metadata-service` expose the catalog:
  - `GET /metadata/facet-types` — list all registered facet types
  - `GET /metadata/facet-types/{typeKey}` — get descriptor including schema
  - `POST /metadata/facet-types` — register new custom facet type
  - `PUT /metadata/facet-types/{typeKey}` — update (non-mandatory only)
  - `DELETE /metadata/facet-types/{typeKey}` — delete (non-mandatory only)

---

## Steps

1. Create `FacetTypeDescriptor` domain class in `mill-metadata-core`
2. Create `FacetTypeRepository` interface in `mill-metadata-core`
3. Create `FacetCatalog` interface in `mill-metadata-core`
4. Create `FacetClassResolver` interface in `mill-metadata-core`
5. Implement `FacetCatalog` default implementation
6. Review `MetadataType` enum — decide on `MetadataTargetType` or `ANY` addition
7. Implement `_type` injection in repository write path
8. Implement `_type` validation in repository read path
9. Extend `FileMetadataRepository` to load `facet-types` section
10. Create `FileFacetTypeRepository` implementation
11. Implement target type validation in `MetadataService.save()`
12. Add JSON Schema validation library dependency
13. Implement content schema validation in `MetadataService.save()`
14. Wire `FacetCatalog` into autoconfigure (`mill-metadata-autoconfigure`)
15. Register platform mandatory + optional facet types at startup
16. Remove old `FacetRegistry` singleton
17. Add REST endpoints for facet type CRUD in `mill-metadata-service`
18. Update existing YAML test fixtures with `_type` markers (or verify auto-inject)
19. Write tests for catalog, validation, and persistence

---

## Out of Scope

- JPA `FacetTypeRepository` implementation (future phase).
- UI for facet type management in Grinder UI (future phase).
- Facet type versioning / migration tooling (future phase).

---

## Verification

1. `FacetTypeDescriptor` can be loaded from YAML and persisted.
2. Platform mandatory facets cannot be disabled or deleted.
3. Custom facet types can be registered via YAML or REST API.
4. `_type` field is auto-injected on write and present in stored data.
5. Target type validation rejects mismatched facets when `applicableTo` is defined.
6. Target type validation is skipped when `applicableTo` is not defined.
7. Content validation rejects invalid content when `contentSchema` is defined.
8. Content validation is skipped when `contentSchema` is not defined.
9. `FacetRegistry` singleton no longer exists.
10. `./gradlew test` passes in all metadata modules.
11. REST `GET /metadata/facet-types` returns the deployed catalog.

## Estimated Effort

Medium-large — new domain class, new repository, new catalog service, validation
logic, REST endpoints, and migration of existing `FacetRegistry` usage. Most work
is greenfield (new code), not migration.
