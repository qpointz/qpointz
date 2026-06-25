# WI-347 — Catalog-generic facet tools (`list_facet_types`, `get_facet_type`, `validate_facet_payload`, `propose_facet_assignment`)

Status: `planned`  
Type: `✨ feature`  
Area: `ai`, `metadata`  
Depends on: [WI-346](WI-346-metadata-read-port-adapter.md), [WI-351](WI-351-multi-artifact-protocol-runtime.md), [WI-352](WI-352-metadata-content-entity-and-seed.md)  
**Stage:** 5 — branch `feat/metadata-authoring-tools` (see [`STORY.md`](STORY.md))

## Problem Statement

**Metadata authoring works in practice only for the `descriptive` facet type.** The generic capture
tool exists but the **contract** between catalog, validation, and capture is underspecified: the LLM
is not guided to build **`payload` from the selected facet type's `contentSchema`**, and
**`validate_facet_payload`** is optional in practice.

**Story aim:** the LLM can propose **any catalog facet type** using a single tuple:

**`propose_facet_assignment(target, facetType, payload)`**

where **`payload` is generated with respect to that facet type's schema**, and
**`validate_facet_payload(facetType, payload)`** proves the proposal against **`contentSchema`**
before capture.

## Goal

Review and rework the **metadata QUERY/CAPTURE tool surface** so authoring is **catalog- and
schema-driven** for all facet types, with **`MetadataContent`**-backed category guidance and examples.

## Normative proposal contract

```
propose_facet_assignment(
  target: metadataEntityId,   # urn:mill/model/…
  facetType: facetTypeKey,    # from list_facet_types
  payload: object             # MUST conform to facet type contentSchema
)
```

**No `scopeUrn` or `mergeAction` tool args.** Runtime stamps **`writeScopeUrns[]`** on artefact from **`AgentContext.scopes`** (all **`w` + `rw`** scopes).

| Step | Tool | Capability | Responsibility |
|------|------|------------|----------------|
| Ground target | `list_schemas` / `list_tables` / `list_columns` | **`schema`** (existing) | Canonical catalog paths; derive or copy **`metadataEntityId`** per [`metadata-urn-platform.md`](../../../design/metadata/metadata-urn-platform.md) — **no `build_metadata_entity_urn` tool** ([`GAPS.md`](GAPS.md) §3a) |
| Discover scopes | `list_metadata_scopes` | **`metadata`** | Returns scopes with **`access`** (`r` \| `w` \| `rw`); chat default: global **`r`**, chat **`rw`** ([`GAPS.md`](GAPS.md) §3c) |
| Category routing | **`list_facet_categories`** | **`metadata`** | Distinct categories + **`facet-type-category`** guidance from **`MetadataContent`** |
| Reason — shortlist type | `list_facet_types` | **`metadata`** | **Summary rows only** — pick `facetTypeKey` ([`GAPS.md`](GAPS.md) §3b); optional filters |
| Generate — read schema | `get_facet_type` | **`metadata`** | Full manifest + **`contentSchema`** + synthetic **`examples[]`** from content store |
| Optional content drill-down | **`list_content`** / **`get_content`** | **`metadata`** | Raw **`MetadataContent`** rows |
| Dry-run | `validate_facet_payload` | **`metadata`** | **`(facetType, payload [, target])`** — schema + **`applicableTo`** |
| Capture | `propose_facet_assignment` | **`metadata-authoring`** | **`(target, facetType, payload)`** — delegates to **`metadata`** shared validator; artefact gets **`writeScopeUrns[]`** |

**`validate_facet_payload` and `propose_facet_assignment` must use the same validation core**
in the **`metadata`** package. **`metadata-authoring`** only wraps CAPTURE emit + scope stamping.

Prompts must instruct: **categories → facet type → build payload from schema → validate → capture**.

**Prompt enforcement (required):** Add / expand YAML prompts — see [`STORY.md`](STORY.md) § Prompt enforcement and WI-345 § A2. Minimum deliverables:

- `metadata-authoring.intent` — classify `AUTHOR_FACET` vs explore vs SQL vs chat; **decompose mixed** documentary + query in one utterance ([`GAPS.md`](GAPS.md) §10)
- `metadata-authoring.reasoning` — **`list_facet_categories`** → **`list_facet_types`** → **`get_facet_type`** (dynamic category index — **not** hardcoded YAML table)
- **`metadata-authoring.batch`** — parallel multi-facet capture; on partial failure, **do not stop** after first failure — persist all successes, remediate failures next iteration ([`GAPS.md`](GAPS.md) §9)
- Strengthen `metadata.faceting.system` (**profile-neutral**) and `metadata.faceting.request` (**`get_facet_type`** generation step)

**Prerequisite:** multi-artifact batch emission and fan-out are delivered in **[WI-351](WI-351-multi-artifact-protocol-runtime.md)**.

## Tool review

### 1. `list_facet_categories` (metadata QUERY) — **category routing**

- Output: distinct `category` values from catalog + joined **`facet-type-category`** guidance (`signalPhrases`, `exampleFacetTypeKeys`, …)
- Backed by **`MetadataReadPort.listFacetCategories()`** + **WI-352** seeds

### 2. `list_facet_types` (metadata QUERY) — **reasoning**

Return **summary rows only** (no full `contentSchema`) — [`GAPS.md`](GAPS.md) §3b:

- Required: `facetTypeKey`, `category`, `applicableTo`, `description`
- Recommended: `title`; optional: `targetCardinality`, `source`

Optional input filters: `applicableTo`, `category`, `metadataEntityId`.

### 3. `get_facet_type` (metadata QUERY) — **generation**

- Input: **`facetTypeKey`** (or type URN)
- Output: **full** `FacetTypeManifest` including **`contentSchema`** + synthetic **`examples[]`** from **`facet-type-example`** content rows
- Registers on **`metadata`** capability

### 4. `list_content` / `get_content` (metadata QUERY)

- List/filter **`MetadataContent`** by `targetUrn`, `contentKind`
- Get single row by `contentUrn`

### 5. `list_metadata_scopes` (metadata QUERY) — **scope discovery**

- Handler reads **`AgentContext.scopes`**
- Output: `{ scopeUrn, access, label? }` where **`access`** is **`r`**, **`w`**, or **`rw`**
- HTTP chat: **`global`** (`r`) + **`chat-<chatId>`** (`rw`)
- **`[]` empty:** capture may persist artefact but **`writeScopeUrns[]`** empty ([`GAPS.md`](GAPS.md) §3c)

### 6. `validate_facet_payload` (metadata QUERY)

- Input: **`facetTypeKey`** + **`payload`** + optional **`metadataEntityId`**
- Output: `valid: boolean`, `errors: string[]`
- Delegates to **`MetadataReadPort.validateFacetPayload`** (**WI-346**)

### 7. `propose_facet_assignment` (metadata-authoring CAPTURE)

- Input: **`metadataEntityId`**, **`facetTypeKey`**, **`payload`**, optional `rationale` — **no scope/merge args**
- Reject when shared validator fails
- On success: artefact includes **`writeScopeUrns[]`** from context; `status: pending`; `MetadataFacetProposalCapture` → **`facet-proposal`**
- **Does not** write metadata scope — **WI-353** handles via **`artifact.facet.persisted`**

### 8. Per-facet `capture_*` cleanup → [WI-350](WI-350-schema-authoring-description-tool-cleanup.md)

## In Scope

1. **`capabilities/metadata.yaml`** — all QUERY tools above + **`metadata.faceting.system`**
2. **`capabilities/schema.yaml`** (optional) — **`metadataEntityId`** on list outputs
3. **`capabilities/metadata-authoring.yaml`** — `propose_facet_assignment`; prompts; **`AgentContext.scopes`**
4. **`MetadataCapabilities.kt`** — shared validator; content/category mappers; **`get_facet_type`** examples join
5. **`MetadataAuthoringCapability`** — validator + **`writeScopeUrns[]`** stamping
6. Unit tests: list omits schema; get returns schema + examples; multi-type capture

## Out of Scope

- **`MetadataContent`** DDL/seeds — **WI-352**
- Batch protocol, agent loop, SSE/persist fan-out — **WI-351**
- **`HarnessMetadataReadPort`** expansion — **WI-346** ([`GAPS.md`](GAPS.md) §12)
- Removing `capture_*` — **WI-350**
- **`FacetProposalMerger`** implementation — **WI-352** (invoked by **WI-353** persist handler)
- Scope assign, Accept/Reject REST, event handlers — **WI-353**

## Acceptance Criteria

- [ ] **`list_facet_categories`** returns WI-352 category guidance for `general`, `relation`, `data-quality`
- [ ] **`get_facet_type`** returns synthetic **`examples[]`** for seeded types (`descriptive`, `relation-source`, `dq-null-check`, `dq-predicate`)
- [ ] **`metadata-authoring.reasoning`** uses **`list_facet_categories`** — no hardcoded category table in YAML
- [ ] **`metadata-authoring.reasoning`** instructs relation key selection by target entity kind + table role in join ([`GAPS.md`](GAPS.md) §6)
- [ ] **`list_facet_types`** summary without `contentSchema`; **`get_facet_type`** full manifest
- [ ] **`list_metadata_scopes`** returns **`access`** flags; chat: global `r`, chat `rw`
- [ ] **`propose_facet_assignment`** has **no** `scopeUrn` / `mergeAction` args; artefact has **`writeScopeUrns[]`**
- [ ] Valid captures for descriptive, relation\*, and DQ types → **`facet-proposal`**
- [ ] **`metadata-authoring.intent`**, **`.reasoning`**, **`.batch`** prompts present with dq-null-check example
- [ ] **`metadata-authoring.batch`** + capture-remediation: partial parallel failure must not block persisting successful captures (§9)

## Suggested commit

`[feat] WI-347: catalog-generic metadata authoring tools and prompts`
