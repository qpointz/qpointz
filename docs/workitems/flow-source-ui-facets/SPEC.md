# Spec: flow-source-ui-facets

**Story folder:** `docs/workitems/flow-source-ui-facets/`  
**Normative for this story** — work items (**`WI-146`–`WI-150`**) must stay consistent with this document.  
**Related:** deferred sketch **WI-139** in `docs/workitems/completed/20260401-metadata-and-ui-improve-and-clean/`.  
**Layered sources narrative:** `docs/design/metadata/metadata-layered-sources-and-ephemeral-facets.md` (repository normative for inferred vs captured).

---

## 1. Goals and constraints

- Emit **read-only inferred** `FacetInstance` rows (`FacetOrigin.INFERRED`) from **flow** YAML descriptors already consumed by `SourceDefinitionRepository`.
- **No** persistence of these rows; metadata mutation APIs continue to reject non-captured origins where guards exist (pattern from WI-135).
- **Flow facet family:** all flow facet types use URN slug **`flow-*`**, **`category: flow`**, and are seeded from **`metadata/platform-flow-facet-types.yaml`** (not mixed into `platform-bootstrap.yaml`). Every inferred row emitted for these types uses **`originId`**: **`flow`** ([`MetadataOriginIds.FLOW`](../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/source/MetadataOriginIds.kt)). **Calcite** / **JDBC** may introduce their own facet families later (`calcite-*`, `jdbc-*`, or shared physical layer) without renaming flow types.
- **Security:** payloads mirror YAML — treat `storage.rootPath`, regex patterns, and similar as **operator-visible** unless a deployment policy requires redaction (document in WI-146 if redaction is implemented).
- **Table/reader inference:** use existing **flow stack** (`SourceMaterializer`, **`SourceResolver.resolve` / `resolveDescriptor`**, table mappers, blob listing) to discover **which tables exist** and **how readers map** blobs to table names — the same logic that builds `ResolvedSource` / `SourceTable`, not a second ad-hoc scanner.
- **Caching:** cache the **expensive inference result** (per source/schema: resolved table names, reader binding hints, attribute lists) in a **Caffeine** cache with **configurable TTL** under **`mill.data.backend.flow.cache.facets`** (sibling of **`cache.schema`**). Avoid re-listing storage on every metadata `fetchForEntity` call.
- **Opt-out:** **`mill.data.backend.flow.metadata.enabled`** — when **`false`**, do **not** register the flow descriptor **`MetadataSource`**; no flow facet rows are contributed (flow query/schema unchanged).
- **User-level information:** flow facet payloads are **operator-facing** — what someone configuring or auditing the source needs to recognise (aligned with YAML and everyday terms), not a dump of internal types, stack traces, or wire-only encodings. Prefer **short strings and maps** over deep technical nesting unless it aids comprehension.
- **Polymorphic fragments → key-value:** anywhere the underlying descriptor is **polymorphic** (storage kind, **table mapping** kind, **format** options, **attribute** source), carry the facet fragment as a **loose key-value map** (flat or shallow JSON object: string keys, primitive or string values in UI). Do **not** model every discriminator variant as separate rigid `contentSchema` branches; **`contentSchema`** stays coarse (OBJECT / ARRAY of OBJECT with a few fixed keys such as **`format`** + **`params`**).
- **Extensible projection code:** new **`StorageDescriptor`** subtypes (e.g. **S3**, **Azure Data Lake**) and new **reader** `type` values extend **facet building** via small **contributor** units and shared **context** types — not a single growing mapper class. **Facet URNs stay `flow-*`.** Implementation sketch: **`docs/design/data/flow-facet-projection-extensibility.md`**.
- **Same facets, different descriptor sources:** any **`SourceDefinitionRepository`** (files today, DB/API later) that yields **`SourceDescriptor`** (or adapters normalising to it) must be able to reuse the **same** projection orchestrator and emit the **same** payloads for a given descriptor — only **discovery** of descriptors differs.

---

## 2. Class sketch and modules

### 2.1 `metadata/mill-metadata-core`

| Artifact | Responsibility |
|----------|----------------|
| `MetadataOriginIds` | **`FLOW`** = **`"flow"`** — **`originId`** for every **`FlowDescriptorMetadataSource`** / flow inferred facet row (`flow-schema`, `flow-table`, `flow-column`). |
| **`metadata/platform-flow-facet-types.yaml`** | `FacetTypeDefinition` documents for **`urn:mill/metadata/facet-type:flow-schema`**, **`:flow-table`**, **`:flow-column`** — **`category: flow`**. Listed in **`mill.metadata.seed.resources`** after **`platform-bootstrap.yaml`**. |

**Do not** add flow facet types to `platform-bootstrap.yaml`; keep the platform file stable.

No Flow-specific types (`SourceDescriptor`, etc.) in this module.

### 2.2 `data/mill-data-source-core`

| Class / interface | Responsibility |
|-------------------|----------------|
| `SourceCatalogProvider` | Narrow contract: supplies `Iterable<SourceDescriptor>` (or stream) for the active flow configuration. **Today** the live type is **`SourceDefinitionRepository`** in **`mill-data-backends`** (`io.qpointz.mill.data.backend.flow`); **WI-146** introduces this interface in **source-core** and changes **`SourceDefinitionRepository extends SourceCatalogProvider`** (or equivalent) so auto-config and tests depend on the **narrow** contract. Verify signature match during implementation — no silent adapter if return types differ. |

### 2.3 `data/mill-data-metadata` (foundation only)

| Class | Responsibility |
|-------|----------------|
| `AbstractInferredMetadataSource` | **Reuse** — base for inferred `fetchForEntity` + `FacetOrigin.INFERRED`. |
| `ModelEntityUrn`, path parsing | **Reuse** — map entity id → `schema` / `table` / `column`. |
| *Optional* thin helpers | JSON-safe `Map<String, Any?>` builders shared by multiple physical contributors (only if duplication across backends justifies it); **no** `FlowDescriptorMetadataSource` class here. |

### 2.4 `data/mill-data-backends` (flow implementation)

| Class | Responsibility |
|---------|----------------|
| `FlowDescriptorMetadataSource` | `MetadataSource` / extends `AbstractInferredMetadataSource`; **`originId`** = **`MetadataOriginIds.FLOW`** (`"flow"`) for **all** flow facet types; uses `SourceCatalogProvider`, `SchemaProvider`, and **`FlowFacetInferenceCache`** (or equivalent). Package: `io.qpointz.mill.data.backend.flow.metadata`. |
| *Recommended* **`FlowFacetProjectionOrchestrator`** (name may vary) | Composes **`flow-schema`**, **`flow-table`**, **`flow-column`** JSON-safe maps from **`FlowSourceFacetContext`** / **`FlowTableFacetContext`** / **`FlowColumnFacetContext`** (see **`docs/design/data/flow-facet-projection-extensibility.md`**). Delegates storage and reader fragments to pluggable **`FlowStorageFacetContributor`** / **`FlowReaderFormatFacetContributor`** implementations. |
| *Optional* **`FlowFacetPayloadMapper`** | Thin alias or legacy name for the orchestrator + contributor wiring. |
| *Recommended* `FlowFacetInferenceCache` | **Caffeine** cache: key = flow source id (`SourceDescriptor.name`); value = immutable snapshot (per-table **`tableInputs`**, attribute lists, etc.) built via **`SourceResolver`** — same path as runtime flow, no duplicated mapper rules. |

**Inference:** reuse **`SourceResolver`** and existing reader/table mapping types to list tables and reader associations (blob scan cost).

**Caffeine:** add **`implementation(libs.caffeine)`** to **`mill-data-backends`**. The catalog already defines **`caffeine`** in **`libs.versions.toml`** (com.github.ben-manes.caffeine); **no TOML change** expected unless bumping version. Pattern examples: `ai/mill-ai-v3/.../InMemoryChatMemoryStore.kt` (`expireAfterAccess`); `ai/mill-ai-v1-core/.../StepBackReasoner.java`. For facet inference prefer **`expireAfterWrite(ttl)`** when **`mill.data.backend.flow.cache.facets.ttl`** is set so storage/descriptor changes appear after the interval.

**Language (implementation-blocking):** Implement **`FlowDescriptorMetadataSource`**, **`FlowFacetInferenceCache`**, and flow-local collaborators in **`Java`** inside **`mill-data-backends`** for this story. The module stays **`java-library`** without the Kotlin Gradle plugin; **`AbstractInferredMetadataSource`** is Kotlin but **callable from Java** (`super(MetadataOriginIds.FLOW)`). **Do not** add Kotlin sources to **`mill-data-backends`** without first adding **`alias(libs.plugins.kotlin)`** and dependencies — that is **out of scope** unless a follow-up WI explicitly migrates the module.

**Dependencies to add (typical):** `implementation(project(":data:mill-data-metadata"))`, `implementation(project(":metadata:mill-metadata-core"))` (if not already transitive), `implementation(project(":data:mill-data-source-core"))`, **`implementation(libs.caffeine)`** — exact graph trimmed during implementation.

### 2.5 `data/mill-data-schema-core` / services

No new public API required if existing schema explorer DTOs already carry `facetsResolved`; extend **tests** only (WI-148).

### 2.6 `ui/mill-ui`

| Area | Responsibility |
|------|----------------|
| `facetTypeDisplayPriority.ts` | Order new physical facet URNs near layout facets (WI-149). |

---

## 3. Autoconfigure implementation plan

**Module:** `data/mill-data-autoconfigure`

| Step | Action |
|------|--------|
| A | Add **`FlowDescriptorMetadataSourceAutoConfiguration`** (name may vary) in **`mill-data-autoconfigure`** under **`io.qpointz.mill.autoconfigure.data.backend.flow`** — flow-backend-specific wiring (**not** the generic `data.schema` package used by `LogicalLayoutMetadataSourceAutoConfiguration`). **`@AutoConfigureAfter(FlowBackendAutoConfiguration.class)`**, **`AutoConfiguration.imports`** entry. |
| B | **Register bean** `FlowDescriptorMetadataSource` only when: **`mill.data.backend.flow.metadata.enabled`** is **`true`** (default); **`SourceCatalogProvider`** bean is present; active backend is **flow** (`mill.data.backend.type=flow`) — use **`@ConditionalOnProperty`** + **`@ConditionalOnBean`** as appropriate and document in class JavaDoc. When **`metadata.enabled`** is **`false`**, the flow metadata source must **not** be wired or used. |
| C | Inject: `SourceCatalogProvider`, `SchemaProvider`, **`FlowBackendProperties`**. Construct **`FlowFacetInferenceCache`** (or pass TTL/enabled into `FlowDescriptorMetadataSource`) from **`flow.cache.facets`** (**§3.1**). Merge infrastructure unchanged. |
| D | **Future (Calcite/JDBC):** parallel `@Bean` / auto-config; each backend uses its own **`MetadataOriginIds`** value (flow stays **`FLOW`** / `"flow"`). |

**`spring.factories` / imports:** follow existing pattern used by `LogicalLayoutMetadataSourceAutoConfiguration` for registration in the data autoconfigure module.

### 3.1 Flow backend configuration — metadata switch and facet cache

Extend **`FlowBackendProperties`** (`data/mill-data-autoconfigure`, Java `@ConfigurationProperties` for **`mill.data.backend.flow`**) with:

**Metadata gate** (top-level under **`flow`**, sibling of **`sources`** / **`cache`**):

| Property | Type | Purpose |
|----------|------|---------|
| `metadata.enabled` | `boolean` | When **`false`**, **do not** register **`FlowDescriptorMetadataSource`**; no flow facet rows (`originId` **`flow`**). Default **`true`**. Does not disable the flow query backend. |

**Facet cache** (nested under **`cache`**, sibling of **`cache.schema`**):

| Property | Type | Purpose |
|----------|------|---------|
| `cache.facets.enabled` | `boolean` | When **`false`**, always compute facet inference on demand (or use no-op cache). Default **`true`**. |
| `cache.facets.ttl` | `Duration` | Optional; e.g. `5m`, `PT5M`. When **null** and enabled, align with **`cache.schema`** behaviour: **no automatic expiry** unless implementation chooses a default — document in WI-146 and align with `FlowContextFactory` schema cache semantics. |

Existing reference: **`FlowBackendProperties.CacheProperties.SchemaCacheProperties`** (`enabled`, `ttl`) for **`cache.schema`**.

**Bootstrap YAML example:**

```yaml
mill:
  data:
    backend:
      type: flow
      flow:
        metadata:
          enabled: true   # false => no FlowDescriptorMetadataSource bean; no flow facets
        sources:
          - ./config/sources.yaml
        cache:
          schema:
            enabled: true
            ttl: 10m
          facets:
            enabled: true
            ttl: 5m
```

Ensure **JavaDoc** on new nested properties; Spring Boot configuration metadata follows from the Java properties class.

---

## 4. Flow facet types — review sheet

**Rules:** every facet type below has **`category: flow`** in metadata seeding. Names express **binding** (schema / table / column to flow configuration). Authoritative `contentSchema` and titles live in **`metadata/mill-metadata-core/src/main/resources/metadata/platform-flow-facet-types.yaml`**; adjust there during WI-147/WI-146 as payloads stabilise. Payloads follow **§1** (user-level + KV for polymorphic pieces).

### 4.0 Readers vs columns (flow descriptor model)

- **`readers`** in YAML are **table-level bindings**: each reader has a **format** (CSV, Parquet, …) and **table mapping** rules that assign **blobs → logical table name**. They do **not** declare a per-column “reader binding”.
- **Base columns** in a table come from the format handler’s **inferred file schema** (row layout).
- **Extra / synthetic columns** may come from **`table.attributes`** / **`readers[].table.attributes`** (**`TableAttributeDescriptor`** — regex on path, constant, etc.); that is what **`flow-column`** describes with **`binding.type: attribute`** (details in **`binding.params`**).
- So: **`flow-table`** carries **`tableInputs`** only (besides **`schema`** / **`table`**): each item is **`format`** + **`params`** (KV) **+ `effectiveMapping`** (KV) for **that reader’s** merged table mapping (source defaults + reader override). Per-reader mapping can differ, so **do not** put a single table-level **`effectiveMapping`**. **`flow-schema`** carries only **source identity + storage** (**`type`** discriminator + **`params`** KV for all other storage fields). Reader lists, conflicts, and table defaults live on **`flow-table`** / inference, not on **`flow-schema`**. **`flow-column`**: **`binding`**: **`type`** (`attribute` \| `inferred`) + optional **`params`** (KV for attribute / regex details).

### 4.1 Quick reference

| # | Facet URN | Binding focus | Target entity kind | Cardinality |
|---|-----------|---------------|-------------------|-------------|
| 1 | `urn:mill/metadata/facet-type:flow-schema` | Flow **source name + storage** (type + params KV) ↔ catalog **schema** | `entity-type:schema` | SINGLE |
| 2 | `urn:mill/metadata/facet-type:flow-table` | Flow **table inputs** — contributing readers **`(format + kv params)`** per table | `entity-type:table` | SINGLE |
| 3 | `urn:mill/metadata/facet-type:flow-column` | **`binding.type`** + **`binding.params`** (column provenance; not reader→column) | `entity-type:attribute` | SINGLE |

### 4.2 Payload sketches (implementation guide)

| URN | Intent | Payload shape (sketch) |
|-----|--------|------------------------|
| **flow-schema** | **User-level** source + storage only. | **`sourceName`**; **`storage`**: **`type`** (unchanged discriminator, e.g. `local`) + **`params`** (KV — all other storage fields, e.g. `rootPath`). No **`conflicts`**, **`tableDefaults`**, or **`readers`** (those appear only via **`flow-table`** / lower facets). |
| **flow-table** | **User-level** picture of how this table is fed. | **`schema`**, **`table`**; **`tableInputs`**: each row **`format`**, **`params`** (KV), **`effectiveMapping`** (KV — blob→table rule for **that** reader), optional **`readerIndex`**, **`label`**. |
| **flow-column** | **User-level** column provenance. | **`schema`**, **`table`**, **`column`**; **`binding`**: **`{ "type": "attribute" \| "inferred", "params": { … } }`** — **`params`** optional KV (regex/constant fields when **`attribute`**). |

### 4.3 Constants / UI

- Kotlin/Java facet URN strings for the data layer should stay aligned with this file (object or companion — **WI-146**).
- **mill-ui:** add these three URNs to **`facetTypeDisplayPriority.ts`** in a sensible order (**WI-149**).

### 4.4 Example payloads

See **Appendix B** for full JSON examples per facet type (pair with **Appendix A** seed definitions).

---

## 5. Flow facet → YAML mapping (sketch)

| Facet URN | Primary flow descriptor YAML areas |
|-----------|-----------------------------------|
| **flow-schema** | Descriptor **`name`** → **`sourceName`**; **`storage`**: **`storage.type`** → **`storage.type`**, other storage keys → **`storage.params`** |
| **flow-table** | For each contributing reader: **`effectiveMapping`** = merged mapping for that reader; **`params`** from format block; **`SourceResolver`** selects which readers feed this table name |
| **flow-column** | YAML attributes → **`binding.type: attribute`** + **`binding.params`** (KV); file-only columns → **`binding.type: inferred`** (often **`params`**: `{}` or omitted) |

**Descriptor discovery:** paths from **`mill.data.backend.flow.sources[]`** (`FlowBackendProperties`).

### 5.1 Extensibility — polymorphic storage, evolving formats, parallel repositories

| Concern | Approach |
|---------|----------|
| **Storage** today is mostly **`local`**; **S3**, **ADLS**, and similar add new **`JsonTypeName`** subtypes of **`StorageDescriptor`** | **Do not** add new flow facet URNs per cloud. Put operator-visible fields in **`flow-schema.storage.params`**; implement a **`FlowStorageFacetContributor`** per discriminator (see design doc). |
| **Reader formats** grow (new `readers[].type` values) | Extend **`tableInputs[].params`** (and **`format`** string); add **`FlowReaderFormatFacetContributor`** per format when specialised shaping is needed. |
| **Monolithic mapper risk** | **`FlowDescriptorMetadataSource`** should call an **orchestrator** that dispatches to contributors; avoid `when (storage)` / `when (reader.type)` in one unbounded class. |
| **Multiple “flow implementations”** (e.g. **`MultiFileSourceRepository`** vs future DB-backed repo) | **Same** orchestrator and **same** `flow-*` facet contract; repositories differ only in **how** `Iterable<SourceDescriptor>` is produced. Document normalisation adapters if wire shapes differ. |
| **Secrets in cloud storage config** | Apply **redaction** or **reference** fields in **`params`** per deployment policy; do not dump raw credentials into facets (align with §1 **Security**). |

**Design reference (interfaces, context types, registration):** [`docs/design/data/flow-facet-projection-extensibility.md`](../../design/data/flow-facet-projection-extensibility.md).

**Cross-backend implementer guide (foundation vs per-backend facet families):** [`docs/design/data/implementing-backend-metadata-source.md`](../../design/data/implementing-backend-metadata-source.md).

---

## 6. Work item mapping

| Section | Primary WI |
|---------|------------|
| §2 Classes / modules (inference + Caffeine) | WI-146 |
| §3 Autoconfigure; **§3.1** `FlowBackendProperties.metadata` + `cache.facets` | WI-148 |
| §4–5 Facet types + seed file + mapping | WI-147 (suggested **before** WI-146 for classpath facet types — **[`STORY.md`](STORY.md)**) |
| UI ordering / validation | WI-149 |
| Design + public docs: backend-provided metadata, configuration | **WI-150** (`WI-150-pre-closure-docs-backend-metadata.md`) |
| §5.1 Extensibility / contributor model | Design: **`docs/design/data/flow-facet-projection-extensibility.md`**; implementation aligns with **WI-146** (or follow-up refactors). |

---

## 7. Out of scope (this story)

- Editing descriptor YAML via metadata APIs or UI.
- New OpenAPI endpoints unless an existing explorer endpoint cannot return new facet types (should not be the case if registry + merge already propagate).
- Full **Calcite** / **JDBC** `MetadataSource` implementations — only **hooks and naming** that allow them to plug in later.

---

## Appendix A — `platform-flow-facet-types.yaml` (draft)

**Review copy (story folder, not on classpath):** [`review/platform-flow-facet-types.yaml`](review/platform-flow-facet-types.yaml) — edit there first; copy to target path after review.

**Target path:** `metadata/mill-metadata-core/src/main/resources/metadata/platform-flow-facet-types.yaml`

**Seeding:** add to `mill.metadata.seed.resources` **after** `classpath:metadata/platform-bootstrap.yaml` (e.g. `mill-service`).

**Example facet payloads** (what **`FlowDescriptorMetadataSource`** emits under each type key): see **Appendix B**.

*While restricted tooling blocks committing non-markdown files, this appendix is the canonical draft to copy.*

```yaml
# Flow backend inferred facet types (category: flow).
# Loaded via mill.metadata.seed.resources — list AFTER metadata/platform-bootstrap.yaml.
---
kind: FacetTypeDefinition
facetTypeUrn: urn:mill/metadata/facet-type:flow-schema
title: Flow schema binding
description: "Binds a catalog schema entity to flow source name and storage (type + params)."
enabled: true
category: flow
mandatory: false
targetCardinality: SINGLE
contentSchema:
  type: OBJECT
  title: Flow schema binding
  description: "Operator-oriented: source name and storage only (type + params KV). Readers and table defaults are flow-table facets."
  fields:
    - name: sourceName
      schema:
        type: STRING
        title: Source name
        description: "Flow source name (descriptor name)."
      required: true
    - name: storage
      schema:
        type: OBJECT
        title: Storage
        description: "Discriminator type plus key-value params for all other storage properties."
        fields:
          - name: type
            schema:
              type: STRING
              title: Storage type
              description: "Storage kind (e.g. local) — same role as descriptor storage.type."
            required: true
          - name: params
            schema:
              type: OBJECT
              title: Storage params
              description: "Remaining storage fields as key-value pairs (e.g. rootPath)."
            required: false
      required: false
  required: []
applicableTo:
  - urn:mill/metadata/entity-type:schema
schemaVersion: "1.0"
---
kind: FacetTypeDefinition
facetTypeUrn: urn:mill/metadata/facet-type:flow-table
title: Flow table binding
description: "Binds a table entity to participating table inputs (per-reader effectiveMapping + format + params)."
enabled: true
category: flow
mandatory: false
targetCardinality: SINGLE
contentSchema:
  type: OBJECT
  title: Flow table binding
  description: "Operator-oriented: tableInputs only — each entry is format, format params (KV), and that reader’s effectiveMapping (KV)."
  fields:
    - name: schema
      schema:
        type: STRING
        title: Schema
        description: "Catalog schema name."
      required: true
    - name: table
      schema:
        type: STRING
        title: Table
        description: "Table name."
      required: true
    - name: tableInputs
      schema:
        type: ARRAY
        title: Table inputs
        description: "One row per contributing reader: mapping + format + params (all user-level KV where polymorphic)."
        items:
          type: OBJECT
          title: Table input
          description: "One contributing reader: effectiveMapping (KV) for this reader, plus format discriminator and format params (KV)."
          fields:
            - name: format
              schema:
                type: STRING
                title: Format
                description: "Reader format id (e.g. csv, parquet) — from reader type in descriptor."
              required: true
            - name: effectiveMapping
              schema:
                type: OBJECT
                title: Effective mapping
                description: "Merged table.mapping for this reader (KV: type, depth, pattern, table, …)."
              required: false
            - name: params
              schema:
                type: OBJECT
                title: Format params
                description: "Polymorphic format options as key-value pairs for operators."
              required: false
            - name: readerIndex
              schema:
                type: NUMBER
                title: Reader index
                description: "Index in descriptor readers[] when available."
              required: false
            - name: label
              schema:
                type: STRING
                title: Reader label
                description: "Optional reader label from descriptor."
              required: false
      required: false
  required: []
applicableTo:
  - urn:mill/metadata/entity-type:table
schemaVersion: "1.0"
---
kind: FacetTypeDefinition
facetTypeUrn: urn:mill/metadata/facet-type:flow-column
title: Flow column binding
description: "Binds a column to flow-derived provenance via binding.type and optional binding.params."
enabled: true
category: flow
mandatory: false
targetCardinality: SINGLE
contentSchema:
  type: OBJECT
  title: Flow column binding
  description: "User-level column provenance: binding type (attribute vs inferred) + optional params KV."
  fields:
    - name: schema
      schema:
        type: STRING
        title: Schema
        description: "Catalog schema name."
      required: true
    - name: table
      schema:
        type: STRING
        title: Table
        description: "Table name."
      required: true
    - name: column
      schema:
        type: STRING
        title: Column
        description: "Column name."
      required: true
    - name: binding
      schema:
        type: OBJECT
        title: Binding
        description: "Discriminator type plus optional key-value details."
        fields:
          - name: type
            schema:
              type: STRING
              title: Binding type
              description: "attribute | inferred"
            required: true
          - name: params
            schema:
              type: OBJECT
              title: Binding params
              description: "Optional KV — e.g. TableAttributeDescriptor fields (source, pattern, group, value, type, format)."
            required: false
      required: true
  required: []
applicableTo:
  - urn:mill/metadata/entity-type:attribute
schemaVersion: "1.0"
```

Field grammar must match **`platform-bootstrap.yaml`** (only documented `contentSchema` keys such as `type`, `fields`, `items`, `required`, etc.).

---

## Appendix B — Example inferred payloads (per facet type)

Illustrative **`payload`** maps for **`FacetInstance`** / **`facetsResolved`** (**`FacetOrigin.INFERRED`**). These are **user-level**: polymorphic fragments appear as **nested objects that behave as key-value maps** (see **§1**). Shapes align with Appendix A; **WI-146** may add or omit keys as long as the **operator-first** rule holds. Values loosely follow `data/mill-data-backends/config/test/flow-source.yaml`.

### B.1 `urn:mill/metadata/facet-type:flow-schema`

```json
{
  "sourceName": "flowtest",
  "storage": {
    "type": "local",
    "params": {
      "rootPath": "./config/test/flow-data"
    }
  }
}
```

### B.2 `urn:mill/metadata/facet-type:flow-table`

**One contributing reader** (`tableInputs` length 1):

```json
{
  "schema": "flowtest",
  "table": "animals",
  "tableInputs": [
    {
      "format": "csv",
      "effectiveMapping": { "type": "directory", "depth": 1 },
      "params": { "delimiter": ",", "hasHeader": true },
      "readerIndex": 0,
      "label": null
    }
  ]
}
```

**Several inputs** on one logical table — each row carries **its own** **`effectiveMapping`** (readers may override mapping differently):

```json
{
  "schema": "warehouse",
  "table": "events",
  "tableInputs": [
    {
      "format": "csv",
      "effectiveMapping": { "type": "glob", "pattern": "**/*.csv", "table": "events" },
      "params": { "delimiter": ",", "hasHeader": true },
      "readerIndex": 0
    },
    {
      "format": "parquet",
      "effectiveMapping": { "type": "glob", "pattern": "**/*.parquet", "table": "events" },
      "params": {},
      "readerIndex": 1
    }
  ]
}
```

### B.3 `urn:mill/metadata/facet-type:flow-column` (path attribute)

```json
{
  "schema": "flowtest",
  "table": "animals",
  "column": "pipeline",
  "binding": {
    "type": "attribute",
    "params": {
      "source": "CONSTANT",
      "value": "raw-ingest"
    }
  }
}
```

### B.4 `urn:mill/metadata/facet-type:flow-column` (regex attribute — sketch)

```json
{
  "schema": "skymill",
  "table": "Airlines",
  "column": "year",
  "binding": {
    "type": "attribute",
    "params": {
      "source": "REGEX",
      "pattern": ".*_(?<year>\\d{4})\\d{4}\\.csv$",
      "group": "year",
      "type": "int"
    }
  }
}
```

### B.5 `urn:mill/metadata/facet-type:flow-column` (file-inferred, no YAML attribute)

```json
{
  "schema": "flowtest",
  "table": "animals",
  "column": "species",
  "binding": {
    "type": "inferred"
  }
}
```

*(For **`inferred`**, **`params`** may be omitted or `{}`.)*

**Notes:**

- Facet payloads target **operators** (§1); polymorphic pieces are **KV maps**, not parallel strict DTO trees.
- **`tableInputs`**: each element includes **`effectiveMapping`** (KV) for **that** reader — no table-level **`effectiveMapping`**. **`format`** + **`params`** (KV) as before.
- **`flow-column` `binding.params`**: optional **KV** for **`attribute`** (regex, constant, …). For **`inferred`**, omit **`params`** or use `{}`.
