# WI-144 — Eliminate `entity.kind`

**Story:** eliminate-entity-kind  
**Status:** Open  
**Type:** refactoring  
**Area:** data, metadata

*Previously tracked under **typed-entity-urns**; moved here when that story closed (typed URNs +
datasets + docs shipped first).*

## Summary

With typed URNs (WI-142), the `entity.kind` field (`MetadataEntity.kind`) is redundant for
all first-class entity types: the class segment of the URN (`schema`, `table`, `attribute`,
`model`, `concept`) already carries the same information. This WI removes the field from
the domain, persistence, and YAML format.

## What `entity.kind` is used for today

| Location | Usage |
|----------|-------|
| `MetadataEntity.kind` | Optional nullable field on domain object |
| `SchemaEntityKinds` constants | `"schema"`, `"table"`, `"attribute"`, `"concept"`, `"model"` |
| `SchemaModelRoot.ENTITY_KIND` | Sets kind=`model` when creating the model root entity |
| `MetadataEntityRecord` (JPA) | Persisted column `entity_kind` (nullable) |
| YAML canonical datasets | `kind:` key on entity documents |
| `MetadataEntityDto` | `kind` field in REST request/response body |
| `MetadataEntityController` | Reads/writes `kind` on create/update |
| `JpaMetadataEntityRepository` | May filter by kind |

## Replacement: derive kind from URN class segment

Add a utility method (e.g. `RelationalMetadataEntityUrns.classOf(urn): String?`) that
extracts the class segment (`schema`, `table`, `attribute`, `model`, `concept`) from a
typed entity URN. Callers that previously read `entity.kind` switch to `classOf(entity.id)`.

## Scope

### Domain (`metadata/mill-metadata-core`)

- Remove `kind` property from `MetadataEntity`.
- Remove references to `kind` in `MetadataYamlSerializer` (import/export).
- Remove `kind` from `MetadataEntityUrn` helpers if present.

### Persistence (`metadata/mill-metadata-persistence`)

- Remove `entity_kind` column from `MetadataEntityRecord` JPA entity.
- Add Flyway migration to `DROP COLUMN entity_kind` from `metadata_entity`.
- Update `JpaMetadataEntityRepository` — remove any kind-based queries.

### Service and API (`metadata/mill-metadata-service`)

- Remove `kind` from `MetadataEntityDto` (request and response body).
- Remove `kind` from `MetadataEntityController` create/update paths.
- If `kind` was used for validation (e.g. reject unknown kinds), replace with URN class
  segment validation: `classOf(urn)` must not be null for a recognized entity URN.

### Data layer (`data/mill-data-metadata`, `data/mill-data-schema-core`)

- Remove `SchemaModelRoot.ENTITY_KIND` — it is no longer set on entity rows.
- Evaluate whether `SchemaEntityKinds` constants are still needed; if the only remaining
  use is `entity-type` URN vocabulary (facet manifests `applicableTo`), keep in
  `SchemaEntityTypeUrns` and remove `SchemaEntityKinds` as a separate object.

### YAML datasets

- Remove `kind:` keys from all canonical YAML datasets and seed files updated in WI-143.
  The YAML serializer must not emit or consume `kind` after this WI.

## Out of scope

- Changing `SchemaEntityTypeUrns` (`urn:mill/metadata/entity-type:…` vocabulary) — those
  are facet manifest applicability URNs, not instance URNs, and are a separate concern.

## Acceptance criteria

- `MetadataEntity` has no `kind` field.
- `metadata_entity` table has no `entity_kind` column after migration.
- `MetadataEntityDto` has no `kind` field.
- YAML round-trip (import → export) does not emit `kind:` on entity documents.
- `SchemaEntityKinds` removed or consolidated into URN-based helper.
- `./gradlew` tests for all touched modules pass.

## Testing

```bash
./gradlew :metadata:mill-metadata-core:test \
          :metadata:mill-metadata-persistence:testIT \
          :metadata:mill-metadata-service:test \
          :data:mill-data-schema-core:test
```

## Commit

One logical `[refactoring]` commit; update `STORY.md`; clean tree.
