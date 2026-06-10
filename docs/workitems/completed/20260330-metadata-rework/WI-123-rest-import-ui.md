# WI-123 — REST + Import/Export + mill-ui: URN-Only Entity IDs

**Story:** Metadata Rework
**Spec sections:** §10, §11, §13
**Depends on:** WI-122

## Objective

Update the REST controllers, DTOs, OpenAPI annotations, YAML import/export, and the
`mill-ui` React app to work exclusively with URN-based entity ids and the new facet
endpoint surface. Remove all coordinate-based API contracts.

## Scope

### 1. `MetadataEntityController` changes (SPEC §10.1)

`GET /api/v1/metadata/entities`:
- Remove `?schema=`, `?table=` query parameters.
- Add optional `?kind=` — opaque string, equality filter; passed straight to
  `MetadataEntityService.findByKind(kind)`.

`GET /api/v1/metadata/entities/{id}`:
- `{id}` must be a full entity URN (or URN-slug). Reject dot-path ids with 400
  and a descriptive error message.
- Response: `MetadataEntityDto` (identity only — no coordinate or facet fields).

`POST /api/v1/metadata/entities`:
- Body: `MetadataEntityDto` (`id` + optional `kind`).
- Validate `id` starts with `urn:mill/` (case-insensitive). 400 on invalid.

`DELETE /api/v1/metadata/entities/{id}`:
- Hard delete entity and all its facet rows (cascades via FK).

### 2. `MetadataEntityDto` (SPEC §10.3)

```kotlin
data class MetadataEntityDto(
    val id: String,
    val kind: String?,
    val createdAt: Instant,
    val lastModifiedAt: Instant,
    val createdBy: String?,
    val lastModifiedBy: String?
)
```

**Remove fields:** `schemaName`, `tableName`, `attributeName`, `type`, `facets`.

### 3. Facet endpoints (SPEC §10.2)

`GET /api/v1/metadata/entities/{id}/facets`:
- Returns `List<FacetInstanceDto>`.
- `?context=<scopeUrn,...>` — comma-separated scope URNs in priority order; defaults to global.

`GET /api/v1/metadata/entities/{id}/facets/{typeKey}`:
- `typeKey` normalised via `MetadataUrns.normaliseFacetTypePath`.
- Returns `List<FacetInstanceDto>`.

`POST /api/v1/metadata/entities/{id}/facets/{typeKey}` — assign a facet:
- Body: payload JSON object.
- `?scope=<scopeUrn>` — target scope; defaults to global.
- SINGLE: updates payload in-place if assignment already exists (same uid). Otherwise creates new.
- MULTIPLE: always creates a new assignment with a new uid.
- Returns `FacetInstanceDto` with `uid`.

`PATCH /api/v1/metadata/entities/{id}/facets/{typeKey}/{facetUid}` — update assignment payload:
- Body: payload JSON object (full replacement).
- Returns updated `FacetInstanceDto`; 404 if uid not found.

`DELETE /api/v1/metadata/entities/{id}/facets/{typeKey}/{facetUid}` — remove specific assignment:
- `facetUid` always required.
- **§10.2 unassign rules:** physical **204 + DELETE** only for **`merge_action == SET`** in **non-overlay** scopes; overlay / non-SET → **TOMBSTONE** (204 or 200 per API convention — document in controller).
- Returns 404 if uid not found.

`DELETE /api/v1/metadata/entities/{id}/facets/{typeKey}` — remove all assignments of type at scope:
- `?scope=<scopeUrn>` required.
- Apply same **SET / overlay / TOMBSTONE** rules per row.

**Remove endpoint:** `DELETE /api/v1/metadata/entities/{id}/facet-instances/{facetUid}`.

### 4. `FacetInstanceDto`

**No `mergeAction` field** — merge semantics are internal; use **`merge-trace`** for UI (below).

```kotlin
data class FacetInstanceDto(
    val uid: String,               // stable UUID — primary client handle (DB column `uuid`)
    val facetType: String,         // facet type URN
    val scope: String,             // scope URN
    val payload: Map<String, Any?>,
    val createdAt: Instant,
    val lastModifiedAt: Instant
)
```

### 4a. `GET .../facets/merge-trace` (SPEC §10.5)

- Implement **`GET /api/v1/metadata/entities/{id}/facets/merge-trace`** with **`?context=`** (same as facet list).
- Response DTO includes per-scope layers and **`mergeAction`** for diagnostics / multi-scope UI.

### 5. OpenAPI annotation pass (SPEC §10.8)

- All controllers: `@Operation`, `@Parameter`, `@Schema` examples use full URN strings.
- `MetadataEntityDto` docs note no coordinate fields.
- No `deprecated` operations — remove old endpoints cleanly.
- `springdoc-openapi` spec reflects final surface only.

### 6. YAML import validation (SPEC §11)

`MetadataImportService.import(yaml)`:
- Reject any entity whose `id` does not start with `urn:mill/` (case-insensitive) with a
  descriptive 400 error listing the offending ids.
- Entity YAML shape uses `kind:` (nullable). No `type:` field.
- Example in import dialog updated:
  ```yaml
  entities:
    - id: "urn:mill/metadata/entity:public.orders"
      kind: "table"
      facets:
        "urn:mill/metadata/facet-type:descriptive":
          "urn:mill/metadata/scope:global":
            description: "Order master table"
  ```

### 7. `mill-ui` changes (SPEC §13)

**`schemaService.ts`:**
- Replace dot-path entity id construction with URN construction via `buildEntityUrn(schema, table?, column?)`.
- Remove any code that stores or passes `schemaName`/`tableName`/`attributeName` from `MetadataEntityDto`.
- `buildEntityUrn` utility:
  ```ts
  function buildEntityUrn(schema: string, table?: string, column?: string): string {
    const parts = [schema, table, column].filter(Boolean).map(s => s!.toLowerCase())
    return `urn:mill/metadata/entity:${parts.join('.')}`
  }
  ```

**`EntityDetails` component:**
- Remove display of `schemaName`/`tableName`/`attributeName` from metadata API response (those come from the schema tree, not the metadata service).
- Facet delete: **`DELETE .../facets/{typeKey}/{facetUid}`** (uid in path, SPEC §10.2).
- Remove the `DELETE /facet-instances/{uid}` call (endpoint gone).

**Import dialog:**
- YAML example updated to URN-format entity ids (see §11 example above).

### 8. KDoc / TSDoc coverage

- All new/modified Java and Kotlin in `mill-metadata-service`: KDoc to method + parameter level.
- `buildEntityUrn` and modified service methods in `schemaService.ts`: JSDoc/TSDoc.

## Done Criteria

- `GET /entities?schema=` returns 400 (param no longer accepted) or is silently ignored — confirm with spec; prefer 400.
- `GET /entities/{dot.path.id}` returns 400.
- `MetadataEntityDto` JSON has no coordinate fields.
- `DELETE /facet-instances/{uid}` returns 404 (endpoint removed).
- YAML import rejects dot-path ids with 400.
- `buildEntityUrn` exists in `schemaService.ts`; all metadata API calls use URN ids.
- Facet assignment via `POST .../facets/{typeKey}` (not `PUT`).
- Facet delete uses `DELETE .../facets/{typeKey}/{facetUid}` (uid in path, not query param).
- All new/modified backend code has KDoc.
- `./gradlew :metadata:mill-metadata-service:test` passes.
- `npm run test` passes in `ui/mill-grinder-ui`.
