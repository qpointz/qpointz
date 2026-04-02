# WI-143 — Dataset and persistence migration

**Story:** typed-entity-urns  
**Status:** Open  
**Type:** refactoring  
**Area:** data, metadata, test

## Summary

After the codec change (WI-142), all existing persisted URNs and YAML test/seed datasets
still carry the old `urn:mill/metadata/entity:…` form. This WI migrates them to typed URNs.

## Scope

### YAML canonical datasets

Update all `id:` fields that carry `urn:mill/metadata/entity:…` to the typed form:

| File | Action |
|------|--------|
| `test/datasets/skymill/skymill-meta-seed-canonical.yaml` | rewrite `id:` values |
| `test/datasets/skymill/skymill-canonical.yaml` | rewrite `id:` values |
| `test/datasets/moneta/moneta-canonical.yaml` | rewrite `id:` values |
| `metadata/mill-metadata-core/src/main/resources/metadata/platform-bootstrap.yaml` | rewrite if any entity ids present |
| `metadata/mill-metadata-persistence/src/testIT/resources/metadata-seed/one-entity.yaml` | rewrite |
| `metadata/mill-metadata-core/src/test/resources/metadata/facet-types-test.yml` | rewrite if applicable |

Rewrite rules:
- `urn:mill/metadata/entity:<schema>` → `urn:mill/data/schema:<schema>` (single segment, no dots unless a concept)
- `urn:mill/metadata/entity:<schema>.<table>` → `urn:mill/data/table:<schema>.<table>`
- `urn:mill/metadata/entity:<schema>.<table>.<col>` → `urn:mill/data/attribute:<schema>.<table>.<col>`
- `urn:mill/metadata/entity:model-entity` → `urn:mill/data/model:model-entity`
- `urn:mill/metadata/entity:concept:<id>` → `urn:mill/data/concept:<id>`

### Flyway migration (JPA store)

Add a new Flyway SQL migration in `metadata/mill-metadata-persistence` (next version after
existing migrations) that rewrites `entity_res` values in `metadata_entity` for any
deployment that used the legacy URN form:

```sql
-- Rewrite legacy urn:mill/metadata/entity:<local> to typed urn:mill/data/<class>:<local>
-- Schema-level entities (no dot in local part)
UPDATE metadata_entity
SET entity_res = 'urn:mill/data/schema:' || SUBSTRING(entity_res, LENGTH('urn:mill/metadata/entity:')+1)
WHERE entity_res LIKE 'urn:mill/metadata/entity:%'
  AND INSTR(SUBSTRING(entity_res, LENGTH('urn:mill/metadata/entity:')+1), '.') = 0
  AND SUBSTRING(entity_res, LENGTH('urn:mill/metadata/entity:')+1) NOT IN ('model-entity')
  AND SUBSTRING(entity_res, LENGTH('urn:mill/metadata/entity:')+1) NOT LIKE 'concept:%';
-- (Repeat for table, attribute, model, concept cases — exact SQL depends on target DB dialect)
```

> **Note:** The exact SQL rewrite strategy depends on the target dialect (H2 for tests,
> PostgreSQL for production). Use a Flyway Java migration if a single SQL statement cannot
> express all cases portably. Coordinate with the existing migration version sequence in
> `metadata/mill-metadata-persistence/src/main/resources/db/migration/`.

### Integration tests

All integration tests in `mill-metadata-persistence` that persist or assert entity URNs
must pass with the new typed form after the migration runs.

## Out of scope

- `entity.kind` field removal (WI-144).
- Design doc update (WI-145).

## Acceptance criteria

- All YAML canonical datasets use typed URNs exclusively.
- Flyway migration rewrites all legacy `urn:mill/metadata/entity:…` rows on a fresh or
  migrated JPA H2 database.
- `./gradlew testIT` for `mill-metadata-persistence` passes (seed load + entity CRUD).
- `./gradlew test` for all touched modules passes.

## Testing

```bash
./gradlew :metadata:mill-metadata-persistence:testIT \
          :data:mill-data-schema-core:testIT
```

## Commit

One logical `[refactoring]` commit; update `STORY.md`; clean tree.
