# Canonical metadata YAML — single-document specification (handoff)

This document describes the **YAML stream format** that Mill’s metadata **import** and **export** use today (`DefaultMetadataImportService`), plus the **recommended explicit envelope** for future refactors (e.g. `metadataFormat: CANONICAL`). It is intended so another engineer or agent can implement parsers, validators, or generators without reading the whole codebase.

**Normative implementation (today):** `metadata/mill-metadata-core/.../DefaultMetadataImportService.kt`  
**Domain types:** `metadata/mill-metadata-core/.../MetadataEntity.kt`, `FacetTypeManifest.kt`, `MetadataUrns.kt`, `MetadataEntityUrn.kt`  
**Platform metadata at runtime:** `metadata/mill-metadata-core/src/main/resources/metadata/platform-bootstrap.yaml`, loaded **only** via **`mill.metadata.seed.resources`** (global scope + standard facet types). Flyway applies **DDL only** — no data `INSERT`s for scopes or facet types. **Reference JSON** `platform-facet-types.json` (descriptive + concept) and §11 tables supplement fixtures and authoring — they are **not** a substitute for listing the bootstrap file in `mill.metadata.seed.resources`.

**Related design:** [metadata-urn-platform.md](metadata-urn-platform.md), [facet-type-descriptor-formats.md](facet-type-descriptor-formats.md)  
**Synthetic → canonical writer handoff:** [metadata-synthetic-to-canonical-writer-handoff.md](metadata-synthetic-to-canonical-writer-handoff.md)

---

## 1. Stream structure: multi-document YAML

- The byte stream is **UTF-8** text containing **one or more YAML documents**.
- Documents are separated by a line that matches the regex `(?m)^---\s*$` (a line with only `---` and optional trailing spaces).
- Each non-empty document parses as a **single YAML mapping** (JSON object): root must be a map, not a scalar or list.
- **Processing order:** documents are processed **in file order**. Within **each** document:
  1. All entries under the `facet-types` key (if any) are imported **first**.
  2. Then all entries under the `entities` key (if any) are imported.

There is **no** cross-document requirement that facet-types appear before entities globally: if document A only has `entities` and document B only has `facet-types`, today’s code processes A before B. A robust loader may choose to **collect all facet-type manifests first** (planned improvement); this spec documents **current** behaviour for compatibility.

---

## 2. Root document keys (CANONICAL / “legacy canonical”)

### 2.1 Recommended explicit discriminator (forward-compatible)

For new files and generated exports, include on **each** document (or at least the first):

```yaml
metadataFormat: CANONICAL
```

**Today’s importer ignores this key** if present; omission means “same as always” (canonical shape below). Future code may require it for non-canonical formats.

Optional version field (not required today):

```yaml
formatVersion: 1
```

### 2.2 `facet-types` (optional)

- **Type:** list of objects (YAML sequence of mappings).
- **Semantics:** each item is a **facet type manifest** bound to `FacetTypeManifest` (Jackson) and passed through `FacetTypeManifestNormalizer.normalizeStrict` before persistence.
- **Empty or absent:** allowed. The importer **does not** auto-create platform facet types or the global scope when `facet-types` is missing. Operators must apply **`mill.metadata.seed.resources`** (for example `classpath:metadata/platform-bootstrap.yaml` first) or include equivalent `facet-types` / `FacetTypeDefinition` documents in the imported stream. See §11.

### 2.3 `entities` (optional)

- **Type:** list of objects.
- **Semantics:** each item becomes a `MetadataEntity` after Jackson `convertValue`, validation, normalisation, and optional facet payload transforms (see §5–§6).

A document may contain **only** `facet-types`, **only** `entities`, **both**, or neither (empty map is useless but parseable).

---

## 3. `FacetTypeManifest` object (facet-types list items)

Maps 1:1 to the Kotlin data class `FacetTypeManifest` (see `FacetTypeManifest.kt`). Required fields for strict normalisation:

| Field | Notes |
|--------|--------|
| `typeKey` | Non-blank; normalised to full facet-type URN (`urn:mill/metadata/facet-type:…`) |
| `title` | Non-blank |
| `description` | Non-blank |
| `payload` | `FacetPayloadSchema` object; structure validated recursively (object/array/enum/string/number/boolean rules) |

Common optional fields:

- `category` (default `general`)
- `enabled` (default `true`)
- `mandatory` (default `false`)
- `targetCardinality` — `SINGLE` or `MULTIPLE`
- `applicableTo` — list of entity-type identifiers (normalised to `urn:mill/metadata/entity-type:…`)
- `schemaVersion` — string

**Persistence note:** the normalised manifest is stored as JSON in `FacetTypeDescriptor.manifestJson`; the importer also builds display fields on the descriptor.

### 3.1 Object payload fields (`fields[]` entries)

Each element matches **`FacetPayloadField`** (`name`, `schema`, optional `required`, optional `stereotype`):

| Field | Notes |
|--------|--------|
| `stereotype` | Optional UI hints: ordered list of tags after normalisation (trim, lowercase rules in `FacetTypeManifestNormalizer`). **Wire JSON:** comma-separated string if the field’s value schema is **not** `ARRAY`; JSON **array of strings** if the value schema is `ARRAY` (tag list applies to that whole array-valued field — e.g. tags UI, hyperlink list, email list in Model view). The server does **not** enforce allowed tag sets. **Known stereotypes** in Mill UI (`tags`, `hyperlink`, `email`, precedence, OBJECT / array-of-OBJECT hyperlink): [`mill-ui-facet-stereotypes.md`](mill-ui-facet-stereotypes.md). |

---

## 4. `MetadataEntity` object (entities list items)

Maps to `MetadataEntity`:

| Field | Required | Notes |
|--------|----------|--------|
| `id` | Yes (for successful import) | Must be non-blank and pass `MetadataEntityUrn.isMillUrn` (`urn:mill/…`). After load, `MetadataEntityIds.normalizeEntityInPlace` canonicalises the instance URN (see §4.1). |
| `type` | Recommended | `MetadataType` enum string: `CATALOG`, `SCHEMA`, `TABLE`, `ATTRIBUTE`, `CONCEPT`. If null, may be **inferred from `id`** during normalisation when possible. |
| `facets` | Optional | Default empty. Two-level map: **facet type key** → **scope key** → **payload** (see §5). |
| `createdAt`, `updatedAt` | Optional | `Instant` (ISO-8601 in YAML). If `createdAt` null on import, set to now; `updatedAt` always set to now on import. |
| `createdBy`, `updatedBy` | Optional | Strings. |

### 4.1 Entity instance URN grammar

Relational and logical-model entities use **typed** instance URNs under the `model` group (see [`metadata-urn-platform.md`](metadata-urn-platform.md)):

- **Schema:** `urn:mill/model/schema:<schema>`
- **Table:** `urn:mill/model/table:<schema>.<table>`
- **Attribute / column:** `urn:mill/model/attribute:<schema>.<table>.<column>`
- **Model root:** `urn:mill/model/model:model-entity`
- **Concept:** `urn:mill/model/concept:<id>`

Path segments after the final `:` are **dot-separated** physical names, trimmed and lowercased on canonicalisation (`MetadataEntityUrn.canonicalize`).

**Builders (data layer):** `RelationalMetadataEntityUrns.forSchema` / `forTable` / `forAttribute` in `mill-data-metadata`; codec: `DefaultMetadataEntityUrnCodec` in `mill-data-schema-core`.

The legacy flat form `urn:mill/metadata/entity:<local-id>` is **not** used for new seeds or exports.

**Optional `kind` in YAML:** importers may still accept or emit a parallel `kind` / `entityKind` field on some paths; the **authoritative** entity class for relational rows is the **`model` URN class segment**. **WI-144** removes redundant `kind` from the domain and persistence.

**Authoring rule (strict CANONICAL):** relational coordinates belong **inside facet payloads**, not as duplicate top-level fields on the entity row. Hand-authored YAML must not rely on non-standard top-level keys for coordinates; generators should emit full entity URNs and typed facets only.

---

## 5. Facets map: keys and structure

### 5.1 Intended persisted shape

After import normalisation, keys are **full URNs**:

- Facet type key: e.g. `urn:mill/metadata/facet-type:descriptive`
- Scope key: import path forces **global** scope for the platform facet transforms (see §6); value is `urn:mill/metadata/scope:global`

Legacy short keys are still accepted **at parse time** and normalised:

- Facet type: `descriptive`, `structural`, `relation`, `concept`, `value-mapping` → see `MetadataUrns.normaliseFacetTypeKey`
- Scope: `global` → `urn:mill/metadata/scope:global`; `user:…`, `team:…`, `role:…` → prefixed under `urn:mill/metadata/scope:…`

**YAML style in existing datasets:** often nested as `descriptive` → `global` → payload (see `test/datasets/moneta/moneta-meta-repository.yaml`).

### 5.2 Custom facet types

Any facet type key that normalises to a **non-platform** URN is copied through: payload stored under **global** scope after normalisation (same global rule as platform types in the current importer).

---

## 6. Import-time payload transforms (platform facets only)

After Jackson deserialisation, `normaliseAndTransformFacets` runs. Behaviour summary:

### 6.1 `descriptive`

- Input: map payload.
- Output: only `displayName` and `description` keys retained (if present).
- Stored under facet type URN for descriptive, scope **global**.

### 6.2 `structural`

Depends on `MetadataType`:

| Entity `type` | Input (typical YAML) | Output facet type URN | Output payload (conceptual) |
|---------------|----------------------|-------------------------|------------------------------|
| `TABLE` | e.g. `physicalName`, `tableType`, `backendType` | `urn:mill/metadata/facet-type:source-table` (via `normaliseFacetTypePath("source-table")`) | `sourceType`=`FLOW`, `package`=`""`, `name` = `physicalName` or `""` |
| `ATTRIBUTE` | e.g. `physicalName`, `physicalType`, `nullable`, `isForeignKey`, `isPrimaryKey` | `urn:mill/metadata/facet-type:source-column` | `name`, `type`, `nullable`, `isFK`, `isPK` mapped from legacy keys |
| Other | — | **Dropped** (returns null) | — |

So **author-facing** YAML often uses `structural` with table/column fields; **persisted** facets use `source-table` / `source-column` for those shapes.

### 6.3 `relation`

- Input: map with `relations`: list of relation objects.
- Each relation may include: `name`, `description`, `cardinality` (`ONE_TO_ONE`, `ONE_TO_MANY`, `MANY_TO_MANY` or else stored as `UNKNOWN`), `sourceTable` / `targetTable` maps with `schema` and `table`, `sourceAttributes` / `targetAttributes` lists, `joinSql`.
- Output: list of normalised maps with `source` / `target` structure and string column lists, plus `expression` from `joinSql`.

---

## 7. Import modes and REPLACE semantics

- **`ImportMode.REPLACE`:** Before processing **the first** `entities` list that is non-empty, `MetadataRepository.deleteAll()` runs **once** (`replacePerformed` guard). Facet types are **not** deleted by this call in the default service (entities only).
- Each successfully saved entity triggers `MetadataChangeEvent.Imported` on the configured observer.

---

## 8. Export format (generator contract)

The shared **`MetadataCanonicalYamlWriter`** (planned; today the logic lives in **`DefaultMetadataImportService.export`**) should accept **any** `MetadataRepository` implementation plus **`FacetTypeRepository`**. Typical sources: **JPA** (primary runtime store) or a **read-only `FileMetadataRepository`** YAML snapshot — the **same CANONICAL layout** applies so file-backed metadata can be **dumped** for backup, drift review, or a normalised round-trip.

`export(scopeKey: String)` on **`DefaultMetadataImportService`** today:

1. Optionally emits a first document: `facet-types` list for **custom** (non-platform) facet types only, each manifest map parsed from `manifestJson` or synthesised from descriptor fields.
2. Separator: newline + `---` + newline.
3. Second document: `entities` list — all entities from the repository with facets **filtered** to scopes where the scope key **equals** the requested `scopeKey` (typically the global scope URN for full dumps).

**YAML style:** `YAMLGenerator.Feature.WRITE_DOC_START_MARKER` is **disabled** (no leading `---` on the first document unless the writer adds it elsewhere).

**Note:** Export is **scope-filtered**; a full round-trip test must use the same scope key or compare normalised global payloads only.

---

## 9. Minimal examples

### 9.1 Entities-only document (typical hand-maintained / generated)

```yaml
metadataFormat: CANONICAL
entities:
  - id: urn:mill/model/schema:moneta
    type: SCHEMA
    createdAt: "2025-11-05T10:00:00Z"
    updatedAt: "2025-11-05T10:00:00Z"
    facets:
      descriptive:
        global:
          displayName: Moneta
          description: Client moneta schema
```

### 9.2 Facet-types fragment + entities fragment (two documents)

```yaml
metadataFormat: CANONICAL
facet-types:
  - typeKey: urn:mill/metadata/facet-type:example.custom
    title: Custom facet
    description: Example custom facet type
    payload:
      type: OBJECT
      title: Payload
      description: Custom payload root
      fields: []
      required: []
---
metadataFormat: CANONICAL
entities: []
```

---

## 10. Implementation checklist for a new parser/generator

1. Split stream on `(?m)^---\s*$`; trim; drop empty chunks.
2. Parse each chunk as YAML map.
3. Read optional `metadataFormat` / `formatVersion` (ignore or validate for CANONICAL).
4. For each document in order (or collect facet-types globally if implementing improved semantics):
   - Deserialize `facet-types` → `FacetTypeManifest`; run same normalisation as `FacetTypeManifestNormalizer.normalizeStrict`; persist per product rules.
   - Deserialize `entities` → `MetadataEntity`; validate `id` with `MetadataEntityUrn.isMillUrn`; run `normaliseAndTransformFacets`, `MetadataEntityIds.normalizeEntityInPlace`, timestamps; persist.
5. If the deployment requires built-in scopes and facet types, ensure they are present **outside** this stream via **`mill.metadata.seed.resources`** (or prior API/import) — the importer does not synthesise them.
6. For export, mirror Jackson settings and two-document layout if aiming for **byte-identical** output; otherwise document intentional diffs.

---

## 11. Default platform facet types and seeding

### 11.0 Authoritative startup seed (`platform-bootstrap.yaml`)

**Path:** `metadata/mill-metadata-core/src/main/resources/metadata/platform-bootstrap.yaml`

This multi-document YAML is the **production** source for:

- the **global** `MetadataScope`, and  
- every built-in **`FacetTypeDefinition`** document in that file (for example `descriptive`, `schema`, `table`, `column`, `relation`, `relation-source`, `relation-target` — each as `kind: FacetTypeDefinition` with a full `facetTypeUrn`).

**Authoritative list:** treat the file as the source of truth for titles, `category`, payload shapes, and field `stereotype` values. Subsections §11.2–11.5 below are **non-exhaustive** summaries; if they disagree with the YAML, **update the YAML-backed behaviour first**, then refresh this spec.

Configure `platform-bootstrap.yaml` as the **first** entry (or an early entry) in **`mill.metadata.seed.resources`**. There is **no** parallel seeding path: no Flyway data scripts, no file-repository auto-import of this content without seeds, and no `ApplicationRunner` that registers platform types outside **`mill.metadata.seed.*`**.

### 11.1 Reference JSON (`platform-facet-types.json`)

**Path:** `metadata/mill-metadata-core/src/main/resources/metadata/platform-facet-types.json`

A **partial** JSON array used for **tests, fixtures, and tooling** (for example descriptive + concept shapes). It is **not** a substitute for `platform-bootstrap.yaml` at runtime — operators must still list the bootstrap YAML under **`mill.metadata.seed.resources`** when those facet types are required.

`MetadataUrns` recognises short keys such as `concept` and `value-mapping` for normalisation.

The subsections **11.2–11.5** describe historical / example **payload shapes** and **entity import** behaviour. **§11.0** and **`platform-bootstrap.yaml`** define the **live** platform facet-type set and field names.

**YAML authoring note:** entity documents often use the **short** facet keys `descriptive`, `structural`, and `relation` under `facets` (see §5–§6). After import, **persisted** facet type keys are full URNs. The `structural` facet on `TABLE` / `ATTRIBUTE` entities is **transformed** into **`source-table`** / **`source-column`** payloads (§6.2), which match the schemas in §11.3–11.4.

### 11.2 `urn:mill/metadata/facet-type:descriptive` — Description

| Manifest field | Value |
|----------------|--------|
| Title | Description |
| Description | Capture a human-friendly title and narrative context for an entity. |
| Category | `general` |
| Mandatory | `true` |
| Target cardinality | `SINGLE` |
| Applicable to | *(empty list in JSON — not restricted to specific entity types there)* |
| Schema version | `1.0` |

**Payload** (`OBJECT` — “Description payload”): fields shown in UI and used by AI prompts.

| Field | Type | Required (in schema) | Meaning |
|-------|------|----------------------|---------|
| `displayName` | STRING | no | Display title for end users. |
| `description` | STRING | yes | Narrative text for what the entity represents. |

**Import:** only `displayName` and `description` are retained from YAML when using the legacy `descriptive` short key (§6.1).

---

### 11.3 `urn:mill/metadata/facet-type:source-table` — Source Table

| Manifest field | Value |
|----------------|--------|
| Title | Source Table |
| Description | Describe the physical source table behind a logical table entity. |
| Category | `source` |
| Mandatory | `false` |
| Target cardinality | `SINGLE` |
| Applicable to | `urn:mill/metadata/entity-type:table` |
| Schema version | `1.0` |

**Payload** (`OBJECT` — “Source table payload”): identifies the originating table in the source system.

| Field | Type | Required | Meaning |
|-------|------|----------|---------|
| `sourceType` | ENUM | yes | `JDBC` — JDBC-accessible database; `FLOW` — Mill Flow; `CALCITE` — Calcite schema; `UNKNOWN` — unidentified. |
| `package` | STRING | no | Source namespace, e.g. `catalog.schema`. |
| `name` | STRING | yes | Physical source table name. |

**Import:** YAML `structural` on `TABLE` rows is mapped here with `sourceType` defaulting to `FLOW`, `package` to `""`, and `name` from `physicalName` (§6.2). Extra keys in YAML (e.g. `tableType`, `backendType`) are not part of this schema and are dropped by that transform.

---

### 11.4 `urn:mill/metadata/facet-type:source-column` — Source Column

| Manifest field | Value |
|----------------|--------|
| Title | Source Column |
| Description | Describe the physical source column behind a logical attribute. |
| Category | `source` |
| Mandatory | `false` |
| Target cardinality | `SINGLE` |
| Applicable to | `urn:mill/metadata/entity-type:attribute` |
| Schema version | `1.0` |

**Payload** (`OBJECT` — “Source column payload”): source-system metadata for the column.

| Field | Type | Required | Meaning |
|-------|------|----------|---------|
| `name` | STRING | yes | Physical source column name. |
| `type` | STRING | yes | Source-system data type. |
| `nullable` | BOOLEAN | yes | Whether nulls are allowed. |
| `isPK` | BOOLEAN | yes | Part of primary key. |
| `isFK` | BOOLEAN | yes | Part of a foreign key. |

**Import:** YAML `structural` on `ATTRIBUTE` maps `physicalName` → `name`, `physicalType` → `type`, `nullable`, `isPrimaryKey` → `isPK`, `isForeignKey` → `isFK` (§6.2). Keys such as `backendType` are not in this schema and are dropped by the transform.

---

### 11.5 `urn:mill/metadata/facet-type:relation` — Relation

| Manifest field | Value |
|----------------|--------|
| Title | Relation |
| Description | Describes entity relationship |
| Category | `general` |
| Mandatory | `false` |
| Target cardinality | `MULTIPLE` |
| Applicable to | `urn:mill/metadata/entity-type:schema`, `urn:mill/metadata/entity-type:table` |
| Schema version | `1.0` |

**Payload** (`OBJECT` — “Relation payload”): describes **one** relationship (facet cardinality `MULTIPLE` means many such payloads can exist conceptually; stored as a list under the relation facet after import normalisation).

| Field | Type | Required | Meaning |
|-------|------|----------|---------|
| `name` | STRING | no | Relation name. |
| `description` | STRING | no | Relation description. |
| `cardinality` | ENUM | yes | `ONE_TO_ONE`, `ONE_TO_MANY`, `MANY_TO_MANY`, or `UNKNOWN`. |
| `source` | OBJECT | yes | `schema` (STRING, required), `table` (STRING, required), `columns` (ARRAY of STRING, required) — participating source attributes. |
| `target` | OBJECT | yes | Same shape as `source` for the target table. |
| `expression` | STRING | yes | SQL-like join / relation expression. |

**Import:** YAML typically nests a `relations` list under the short `relation` facet key; the importer rewrites entries into this shape (`joinSql` → `expression`, table maps and attribute lists → `source` / `target`, §6.3).

---

## 12. Out of scope in this spec

- **`metadataFormat: SIMPLE`** and coordinate-based authoring — separate spec (planned); compiler produces the same in-memory `MetadataEntity` list as §4–§6.
- **Mutable file-backed repository** or merge strategies for conflicting entity ids.
- **REST API** request/response shapes (only YAML file/stream here).

---

*Last updated: 2026-04-02 — entity instance URNs for the relational catalog use typed `urn:mill/model/…` forms (see `metadata-urn-platform.md`); legacy `urn:mill/metadata/entity:…` is retired for those entities.*
