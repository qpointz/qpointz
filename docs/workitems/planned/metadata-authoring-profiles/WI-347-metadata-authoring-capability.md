# WI-347 — Catalog-generic facet tools (`list_facet_types`, `validate_facet_payload`, `propose_facet_assignment`)

Status: `planned`  
Type: `✨ feature`  
Area: `ai`, `metadata`  
Depends on: [WI-346](WI-346-metadata-read-port-adapter.md), [WI-351](WI-351-multi-artifact-protocol-runtime.md)

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
schema-driven** for all facet types.

## Normative proposal contract

```
propose_facet_assignment(
  target: metadataEntityId,   # urn:mill/model/…
  facetType: facetTypeKey,    # from list_facet_types
  payload: object             # MUST conform to facet type contentSchema
)
```

| Step | Tool | Capability | Responsibility |
|------|------|------------|----------------|
| Discover types | `list_facet_types` | **`metadata`** | Types + `contentSchema`; filter by **`applicableTo`** / entity kind — **extend if needed** |
| Dry-run | `validate_facet_payload` | **`metadata`** | **`(facetType, payload [, target])`** — schema + **`applicableTo`** |
| Capture | `propose_facet_assignment` | **`metadata-authoring`** | **`(target, facetType, payload)`** — delegates to **`metadata`** shared validator |

**`validate_facet_payload` and `propose_facet_assignment` must use the same validation core**
in the **`metadata`** package (extend [`MetadataCapabilities.kt`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/capabilities/metadata/MetadataCapabilities.kt) /
[`FacetPayloadStructureValidator`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/capabilities/metadata/FacetPayloadStructureValidator.kt) as needed). **`metadata-authoring`** only wraps CAPTURE emit.

Prompts must instruct: **select facet type from catalog → build payload from its schema → validate → capture**.

**Prompt enforcement (required):** Add / expand YAML prompts so the model **reasons user statements
into facets** when this capability is active — see [`STORY.md`](STORY.md) § Prompt enforcement and
WI-345 § A2. Minimum deliverables in `metadata-authoring.yaml` + `metadata.yaml`:

- `metadata-authoring.intent` — classify `AUTHOR_FACET` vs explore vs SQL vs chat
- `metadata-authoring.reasoning` — utterance pattern table (DQ, descriptive, relation, …)
- **`metadata-authoring.batch`** — multi-entity / multi-facet parallel capture (do not stop after first)
- Strengthen `metadata.faceting.system` and `metadata.faceting.request` (replace one-line stub)

**Prerequisite:** multi-artifact batch emission and fan-out are delivered in **[WI-351](WI-351-multi-artifact-protocol-runtime.md)**. This WI wires facet tools and prompts onto that platform; do not re-implement agent/SSE fan-out here.

## Tool review

### 1. `list_facet_types` (metadata QUERY)

Return what the LLM needs to **choose facetType and draft payload**:

- `facetTypeKey` / URN, `title`, `description`, `category`, `applicableTo`, `targetCardinality`, `source`
- **`contentSchema`** or a deterministic summary (required fields, types) — design doc defines size limits

Optional input filters: `applicableTo`, `category`, `metadataEntityId` (narrow catalog to types valid for target).

### 2. `validate_facet_payload` (metadata QUERY)

**Target:**

- Input: **`facetTypeKey`** + **`payload`** + optional **`metadataEntityId`** (target for **`applicableTo`** check)
- Output: `valid: boolean`, `errors: string[]` (schema field paths and/or applicability messages)
- Validates against catalog **`contentSchema`** and, when target present, facet type **`applicableTo`** vs entity kind — **implemented on `metadata` capability**, extended if today's handler lacks target arg

### 3. `propose_facet_assignment` (metadata-authoring CAPTURE)

**Target:**

- Input: **`metadataEntityId`** (target), **`facetTypeKey`**, **`payload`**, optional `rationale`
- Reject when shared **`metadata`** validator fails (unknown type, schema, **`applicableTo`**)
- On success: `MetadataFacetProposalCapture` → chat artefact via **`inferred-facet`** / **`facet-proposal`** (same for all types)
- Tests: **≥3** facet families with **schema-valid** payloads; assert **identical** wire `partType` / persist kind

### 4. Per-facet `capture_*` cleanup → [WI-350](WI-350-schema-authoring-description-tool-cleanup.md)

## In Scope

1. **`capabilities/metadata.yaml`** — `list_facet_types`, `validate_facet_payload` contracts + **`metadata.faceting.system`**
2. **`capabilities/metadata-authoring.yaml`** — `propose_facet_assignment`; prompts (`intent`, `reasoning`, `batch`, `request`); **`finalSchema.results[]`** on protocol (schema only if not done in WI-351)
3. **`MetadataCapabilities.kt`** — shared validator; extend QUERY tools; **`MetadataAuthoringCapability`** calls shared validator before emit
4. Unit tests: invalid payload rejected; valid multi-type **single** and **parallel multi** captures (multi replay asserts WI-351 behaviour)

## Out of Scope

- Batch protocol, agent loop, SSE/persist fan-out (**WI-351**)
- Removing `capture_*` tools (**WI-350**)
- Metadata service persistence
- MCP default changes

## Acceptance Criteria

- [ ] `validate_facet_payload` rejects payload missing required schema fields for a known facet type
- [ ] `validate_facet_payload` with `metadataEntityId` rejects facet type not **`applicableTo`** target entity kind
- [ ] `propose_facet_assignment` rejects the same applicability mismatch (shared validator)
- [ ] Valid `(target, facetType, payload)` succeeds for descriptive, relation\*, and DQ examples
- [ ] Successful captures for descriptive, relation\*, and DQ types all emit **`partType: facet-proposal`** (not type-specific artefact kinds)
- [ ] **`metadata-authoring.intent`** and **`metadata-authoring.reasoning`** prompts present in manifest; include dq-null-check worked example and SQL disambiguation rules
- [ ] **`metadata-authoring.batch`** prompt present; instructs parallel capture for multi-facet utterances
- [ ] Parallel multi-capture (via **WI-351**): two+ `propose_facet_assignment` in one iteration → two+ `facet-proposal` replay rows

## Suggested commit

`[feat] WI-347: schema-validated propose_facet_assignment and validate_facet_payload`
