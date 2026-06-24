# WI-347 — Catalog-generic facet tools (`list_facet_types`, `get_facet_type`, `validate_facet_payload`, `propose_facet_assignment`)

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
| Ground target | `list_schemas` / `list_tables` / `list_columns` | **`schema`** (existing) | Canonical catalog paths; derive or copy **`metadataEntityId`** per [`metadata-urn-platform.md`](../../../design/metadata/metadata-urn-platform.md) — **no `build_metadata_entity_urn` tool** ([`GAPS.md`](GAPS.md) §3a) |
| Pick scope | `list_metadata_scopes` | **`metadata`** | Returns **global** (read) + **chat** (write) in HTTP chat; each row **`writable`**; default writable on **`AgentContext`** — capture **writable only** ([`GAPS.md`](GAPS.md) §3c) |
| Reason — shortlist type | `list_facet_types` | **`metadata`** | **Summary rows only** — pick `facetTypeKey` ([`GAPS.md`](GAPS.md) §3b); optional filters |
| Generate — read schema | `get_facet_type` | **`metadata`** | Full manifest + **`contentSchema`** for payload drafting |
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
- `metadata-authoring.reasoning` — utterance pattern table; **`list_metadata_scopes`** then **`list_facet_types`** shortlist step
- **`metadata-authoring.batch`** — multi-entity / multi-facet parallel capture (do not stop after first)
- Strengthen `metadata.faceting.system` (**profile-neutral**) and `metadata.faceting.request` (**`get_facet_type`** generation step)

**Prerequisite:** multi-artifact batch emission and fan-out are delivered in **[WI-351](WI-351-multi-artifact-protocol-runtime.md)**. This WI wires facet tools and prompts onto that platform; do not re-implement agent/SSE fan-out here.

## Tool review

### 1. `list_facet_types` (metadata QUERY) — **reasoning**

Return **summary rows only** (no full `contentSchema`) — [`GAPS.md`](GAPS.md) §3b:

- Required: `facetTypeKey`, `category`, `applicableTo`, `description`
- Recommended: `title`; optional: `targetCardinality`, `source`

Optional input filters: `applicableTo`, `category`, `metadataEntityId` (narrow catalog to types valid for target).

### 2. `get_facet_type` (metadata QUERY) — **generation**

- Input: **`facetTypeKey`** (or type URN)
- Output: **full** `FacetTypeManifest` including **`contentSchema`**
- Registers on **`metadata`** capability → available to all profiles that load `metadata` (see GAPS §3b cross-impact)

### 3. `list_metadata_scopes` (metadata QUERY) — **scope discovery**

- **No input** (or optional filters TBD) — handler reads **`AgentContext`** (`chatId`, default writable scope, MCP auth); **ensure-or-create** chat **`metadata_scope`** row per GAPS §3c / WI-233 before returning chat row
- **Chat scope URN (locked):** `urn:mill/metadata/scope:chat-<chatId>`
- **`metadata_scope` row:** `scopeType=CHAT`, `visibility=PRIVATE`, `referenceId=chatId`, `displayName=Chat <chatName>`, `ownerId=chat.userId`
- Output: array of `{ scopeUrn, writable, label? }`
  - HTTP chat: **`global`** (`writable: false`) + **`chat-<chatId>`** (`writable: true`)
  - **`metadata`** QUERY tools read merged metadata from **all** scopes; **`metadata-authoring`** CAPTURE must use **`writable: true`** only
- Reject **`propose_facet_assignment`** when `scopeUrn` is read-only (e.g. global in chat)
- **`[]` empty:** capture may persist artefact but no metadata-scope merge ([`GAPS.md`](GAPS.md) §3c)

### 4. `validate_facet_payload` (metadata QUERY)

**Target:**

- Input: **`facetTypeKey`** + **`payload`** + optional **`metadataEntityId`** (target for **`applicableTo`** check)
- Output: `valid: boolean`, `errors: string[]` (schema field paths and/or applicability messages)
- Validates against catalog **`contentSchema`** and, when **`metadataEntityId`** present, facet type **`applicableTo`** vs entity kind — **delegates to `MetadataReadPort.validateFacetPayload`** (port extended in **WI-346**; no duplicate rules here)

### 5. `propose_facet_assignment` (metadata-authoring CAPTURE)

**Target:**

- Input: **`metadataEntityId`** (target), **`facetTypeKey`**, **`payload`**, optional `rationale`
- Reject when shared **`metadata`** validator fails (unknown type, schema, **`applicableTo`**)
- On success: `MetadataFacetProposalCapture` → chat artefact via **`inferred-facet`** / **`facet-proposal`** (same for all types)
- Tests: **≥3** facet families with **schema-valid** payloads; assert **identical** wire `partType` / persist kind

### 6. Per-facet `capture_*` cleanup → [WI-350](WI-350-schema-authoring-description-tool-cleanup.md)

## In Scope

1. **`capabilities/metadata.yaml`** — `list_facet_types` (summary), **`get_facet_type`** (full), **`list_metadata_scopes`** (context-sensitive), `validate_facet_payload` + **`metadata.faceting.system`** (profile-neutral)
2. **`capabilities/schema.yaml`** (optional) — document URN derivation; optionally add **`metadataEntityId`** to `list_schemas` / `list_tables` / `list_columns` outputs via `SchemaCatalogPort` adapter
3. **`capabilities/metadata-authoring.yaml`** — `propose_facet_assignment`; prompts (`intent`, `reasoning`, `batch`, `request`); **`finalSchema.results[]`** on protocol (schema only if not done in WI-351)
4. **`MetadataCapabilities.kt`** — shared validator; extend QUERY tools (incl. **`list_metadata_scopes`** handler with **`AgentContext`** binding); **`MetadataAuthoringCapability`** calls shared validator before emit
5. Unit tests: invalid payload rejected; valid multi-type **single** and **parallel multi** captures (multi replay asserts WI-351 behaviour)

## Out of Scope

- Batch protocol, agent loop, SSE/persist fan-out (**WI-351**)
- Removing `capture_*` tools (**WI-350**)
- Metadata service persistence (merge/write — consumer/M-23)
- MCP default changes
- Chat scope URN grammar finalisation — **locked** in GAPS §3c; **`metadata_scope`** row shape **WI-233** (`CHAT`, `PRIVATE`, `reference_id`, display name, owner)

## Acceptance Criteria

- [ ] Prompts document grounding: **`schema`** tools first, then canonical **`metadataEntityId`** (no dedicated URN builder tool)
- [ ] **`list_facet_types`** returns summary rows **without** nested `contentSchema`; **`get_facet_type`** returns full manifest for one key
- [ ] **`metadata-authoring.reasoning`** references **`list_facet_types`**; **`metadata.faceting.request`** references **`get_facet_type`** for payload generation
- [ ] **`metadata.faceting.system`** does not imply facet authoring on non-authoring profiles ([`GAPS.md`](GAPS.md) §3b)
- [ ] **`list_metadata_scopes`** returns **global** + **chat** in HTTP chat with **`writable`** flags; chat scope URN `urn:mill/metadata/scope:chat-<chatId>`
- [ ] Default **writable** scope on **`AgentContext`**; prompts instruct capture to use writable row only
- [ ] `propose_facet_assignment` **rejects** read-only `scopeUrn` (e.g. global in chat)
- [ ] When scope list was **empty**, `propose_facet_assignment` still persists **`facet-proposal`** for replay but artefact is **not** treated as merged into metadata scope
- [ ] When writable scope present, captured artefact includes **`scopeUrn`** from tool output
- [ ] `propose_facet_assignment` rejects the same applicability mismatch (shared validator)
- [ ] Valid `(target, facetType, payload)` succeeds for descriptive, relation\*, and DQ examples
- [ ] Successful captures for descriptive, relation\*, and DQ types all emit **`partType: facet-proposal`** (not type-specific artefact kinds)
- [ ] **`metadata-authoring.intent`** and **`metadata-authoring.reasoning`** prompts present in manifest; include dq-null-check worked example and SQL disambiguation rules
- [ ] **`metadata-authoring.batch`** prompt present; instructs parallel capture for multi-facet utterances
- [ ] Parallel multi-capture (via **WI-351**): two+ `propose_facet_assignment` in one iteration → two+ `facet-proposal` replay rows

## Suggested commit

`[feat] WI-347: schema-validated propose_facet_assignment and validate_facet_payload`
