# Implementing a backend-specific `MetadataSource`

**Audience:** engineers adding **read-only inferred** metadata for a **data backend** (Flow today; JDBC, Calcite, or others later)  
**Status:** design guidance (contract classes live in repo; this document is normative for **what to generalise vs specialise**)  
**Related:** [`backend-provided-metadata.md`](../metadata/backend-provided-metadata.md), [`metadata-layered-sources-and-ephemeral-facets.md`](../metadata/metadata-layered-sources-and-ephemeral-facets.md), [`flow-facet-projection-extensibility.md`](flow-facet-projection-extensibility.md), story **[`SPEC.md`](../../workitems/completed/20260402-flow-source-ui-facets/SPEC.md)** (flow facet shapes)

---

## 1. Your role

You implement a **`MetadataSource`** that:

- Returns **`FacetInstance`** rows with **`FacetOrigin.INFERRED`** for catalog entities (typically schema / table / column URNs aligned with **`SchemaProvider`**).
- Uses a **stable `originId`** for **all** facet types **your** backend contributes (one origin per backend family unless you deliberately split origins — avoid unless merge semantics require it).
- **Never** persists through **`FacetRepository`**; mutations for these rows are rejected upstream.
- Reads **`MetadataReadContext`** (**`isOriginActive`**, scopes) like **`LogicalLayoutMetadataSource`**.

You **do not** own merge logic — **`FacetInstanceReadMerge`** collects every **`MetadataSource`** bean.

---

## 2. What stays generic (foundation)

Keep shared surface **small**. These are the **intended** generalisation points:

| Foundation | Module | Role |
|------------|--------|------|
| **`MetadataSource`** | `mill-metadata-core` | Contract: **`originId`**, **`fetchForEntity(entityId, context)`**. |
| **`AbstractInferredMetadataSource`** | `mill-data-metadata` | Helper base: **`inferredFacet(...)`** builds **`FacetInstance`** with **`INFERRED`**, deterministic **`assignmentUuid`**, canonical type keys. |
| **`MetadataOriginIds`** | `mill-metadata-core` | **One string constant per contributor family** (e.g. **`FLOW`**, future **`JDBC_PHYSICAL`**, **`CALCITE_MODEL`**). Add a new `const val` when you add a **new** backend metadata family — **do not** overload an existing id for unrelated payloads. |
| **`FacetInstance`**, **`MetadataReadContext`**, merge | `mill-metadata-core` | Unified read path and optional **`?origin=`** filtering. |
| **`SchemaProvider`** (often) | `mill-data-backend-core` | Verify entity paths exist; align facet targets with catalog. |

**Rule:** `mill-metadata-core` and **`mill-data-metadata`** must **not** depend on backend-specific types (no `SourceDescriptor`, JDBC `DataSource`, etc.). **Your** implementation classes live in **backend** modules (e.g. `mill-data-backends`, future JDBC adapter module).

---

## 3. What you specialise (per backend) — expect change

Facet **structure will differ** between backends. **Do not** design a single shared JSON schema for “physical metadata” across Flow, JDBC, and Calcite.

| Aspect | Per backend | Examples |
|--------|-------------|----------|
| **Facet type URNs** | Own family | `urn:mill/metadata/facet-type:flow-*` vs future `urn:mill/metadata/facet-type:jdbc-*` or `calcite-*`. |
| **`category` in facet type seed** | Own value | `flow`, `jdbc`, `calcite`, … — UI and registry filters may use it. |
| **`originId`** | Own constant | `flow`, `jdbc`, … — must match **`MetadataSource.originId`**. |
| **Payload shape** | Own maps / nested objects | Flow: **`sourceName`**, **`storage`**, **`tableInputs`**. JDBC might expose **connection profile** (non-secret), **catalog/schema**, **imported key** hints — **no** “source descriptor” concept. |
| **Domain concepts** | Own model | Flow centres **sources** and **readers**; JDBC centres **connections**, **schemas**, driver capabilities. |
| **Seed YAML** | Own file(s) | e.g. `platform-flow-facet-types.yaml` vs `platform-jdbc-facet-types.yaml` listed in **`mill.metadata.seed.resources`**. |
| **Projection / mapping code** | Own package | Flow: [`flow-facet-projection-extensibility.md`](flow-facet-projection-extensibility.md). JDBC: new contributors when built. |

**Generalisation is intentional only at the “plumbing” level:** **`MetadataSource`** + **`INFERRED`** + **`originId`** + coarse **facet type definitions**. **Semantic** generalisation across backends is **not** a goal.

---

## 4. Layering (conceptual)

```text
mill-metadata-core          MetadataSource, FacetInstance, MetadataOriginIds.*
        ▲
mill-data-metadata          AbstractInferredMetadataSource (optional base)
        ▲
mill-data-backends / jdbc / …   Your XxxMetadataSource + payload builders + cache
        ▲
mill-data-autoconfigure     XxxMetadataSourceAutoConfiguration (@ConditionalOn* backend-specific)
```

- **One** primary **`MetadataSource` bean per backend family** is typical (originId **X**).
- Optionally **multiple** beans **only** if you need **distinct originIds** for merge ordering or muting — document why.

---

## 5. Implementation checklist (new backend family)

1. **Add `MetadataOriginIds.YOUR_FAMILY`** in `mill-metadata-core` with KDoc (stable string, one sentence on what contributes).
2. **Define facet types** in a **dedicated** seed YAML (`platform-*-facet-types.yaml`): URNs, **`category`**, coarse **`contentSchema`** (OBJECT/array — allow evolution inside payloads).
3. **Register** seed in **`mill.metadata.seed.resources`** (order after `platform-bootstrap.yaml` as appropriate).
4. **Implement `MetadataSource`** (extend **`AbstractInferredMetadataSource`** or delegate to a helper) in the **backend** module:
   - **`fetchForEntity`**: map **`entityId`** → your facet payloads; return empty if entity not yours or origin muted.
5. **Auto-configure** in **`mill-data-autoconfigure`** under **`io.qpointz.mill.autoconfigure.data.backend.<backend>`**:
   - **`@ConditionalOnProperty`** for **`mill.data.backend.type`**
   - **Global metadata gating:** all backend `MetadataSource` beans must be gated by `@ConditionalOnProperty(prefix = "mill.data.backend.metadata", name = "enabled", havingValue = "true", matchIfMissing = true)`. This is the unified kill-switch (`BackendMetadataProperties`). There are no backend-specific toggles — per-source overrides in source descriptor YAML are the only narrower control (Flow backend only).
   - **Redaction:** inject `BackendMetadataProperties.getRedact()` (type `io.qpointz.mill.data.metadata.StorageFacetRedactMode`) into your `MetadataSource` bean. Apply redaction in payload-building methods before emitting facet rows.
6. **Tests:** unit tests for payload shape + **originId**; slice/integration test that **`FacetInstanceReadMerge`** sees your bean when context loads. Add tests for global gating (`enabled: false` suppresses bean) and redaction modes.
7. **Docs:** public operator blurb + design pointer (this file + backend page).

**Note:** Flow story text may reference **`SourceCatalogProvider`** as a narrow catalog contract; if that type is not in the codebase yet, **`SourceDefinitionRepository`** is the concrete bean for **`@ConditionalOnBean`** until **WI-146** extracts or renames the interface — keep SPEC, WI-148, and auto-config conditions in sync.

---

## 6. Contrasts: Flow vs hypothetical JDBC

| | Flow (as designed) | JDBC (hypothetical future) |
|--|-------------------|----------------------------|
| **Centre of gravity** | **`SourceDescriptor`** / YAML **source** | **Connection**, driver, **catalog/schema** identity |
| **Facet URNs** | **`flow-schema`**, **`flow-table`**, **`flow-column`** | e.g. **`jdbc-connection`**, **`jdbc-table`**, **`jdbc-column`** — names illustrative |
| **`category`** | **`flow`** | **`jdbc`** |
| **`originId`** | **`flow`** | e.g. **`jdbc`** (or split read-only vs captured if ever needed — separate design) |
| **“Source” in payload** | Yes — schema ≈ source **name** | Unlikely same word; might use **`connectionRef`**, **`catalog`**, **`schema`**, **`table`** |

Reuse **entity URNs** (`urn:mill/model/...`) from the shared catalog where possible so **`SchemaFacetService`** and Data Model navigation stay consistent; **payload internals** differ.

---

## 7. Evolution when facet structure changes

- **Additive** fields inside payloads or **`params` maps** are preferred (UI and **`contentSchema`** should stay tolerant).
- **Breaking** changes to meaning or required keys: prefer **new facet type URN** or explicit **version** field inside payload — coordinate with UI and seed migration.
- Changing **`originId`** is a **compatibility break** for clients filtering by origin; treat as rare.

---

## 8. Metadata controls

### Global gating and redaction

All backend `MetadataSource` beans are governed by `BackendMetadataProperties` (`mill.data.backend.metadata`):

- **`enabled`** (default `true`) — global kill-switch. When `false`, no `MetadataSource` beans are registered for **any** backend.
- **`redact`** (default `basic`) — payload hygiene. Levels: `NONE` < `BASIC` < `SAFE`.

Both `LogicalLayoutMetadataSourceAutoConfiguration` and `FlowDescriptorMetadataSourceAutoConfiguration` carry `@ConditionalOnProperty` on the global prefix. New backend metadata sources must do the same.

### Per-source overrides (Flow only)

Flow source descriptors can carry an optional `metadata` block (`SourceMetadataDescriptor`) to override `enabled` and `redact` per source. Resolution rules:

- **`enabled`:** global `false` is absolute. Source can only opt *out* of a globally enabled setting.
- **`redact`:** effective = `max(global, source)`. Source can tighten but never relax below the global floor.

Calcite and JDBC backends have no per-source granularity — only global settings apply.

For full resolution tables: see `docs/workitems/completed/20260514-cloud-blob-source/WI-271-unified-backend-metadata-controls.md` and `docs/public/src/metadata/backend-metadata.md`.

---

## 9. Anti-patterns

- **Shared facet types** forced across Flow and JDBC for different real-world concepts — leads to empty or misleading fields.
- **Backend types** in `mill-metadata-core` / `mill-data-metadata` implementation code (violates dependency direction).
- **One giant `MetadataSource`** that switches on `mill.data.backend.type` internally — harder to test; prefer **one bean per backend** + **auto-config conditions**.
- **Secrets** in inferred payloads — always apply the global redact mode (at minimum); never emit credentials verbatim unless `redact: none` is explicitly configured.
- **Bypassing global gating** — there is no backend-specific toggle. The global `mill.data.backend.metadata.enabled` is the only application-level switch. Per-source overrides (Flow only) operate within the source descriptor YAML, not as Spring properties.

---

## 10. Flow reference implementation

Implementers should read the flow story **[`SPEC.md`](../../workitems/completed/20260402-flow-source-ui-facets/SPEC.md)** and **[`flow-facet-projection-extensibility.md`](flow-facet-projection-extensibility.md)** for a **concrete** contributor/orchestrator pattern. Other backends should copy **checklist §5**, not Flow’s payload keys.

---

## See also

- [`schema-facet-service.md`](schema-facet-service.md) — how explorer resolves facets for schema entities.
- [`mill-metadata-domain-model.md`](../metadata/mill-metadata-domain-model.md) — URNs, **`FacetInstance`**, merge.
