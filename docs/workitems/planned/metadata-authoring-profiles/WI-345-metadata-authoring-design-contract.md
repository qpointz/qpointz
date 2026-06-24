# WI-345 — Design contract: catalog-generic authoring + YAML profiles

Status: `planned`  
Type: `📋 design`  
Area: `ai`, `metadata`  
Depends on: none (first WI in story)

## Problem Statement

Metadata authoring is **descriptive-centric** in code and tests, while the platform catalog defines
**many** facet types (descriptive, relation\*, value-mapping, **15** DQ types, flow facets, …).
Operators need a normative **tool matrix** and authoring loop before implementation.

## Goal

Design doc(s) under `docs/design/agentic/` that lock:

1. **Catalog-generic authoring loop** (all facet types)
2. **Tool contracts** for `list_facet_types`, `validate_facet_payload`, `propose_facet_assignment`
3. **Removal plan** — **no `capture_<specific facet>` tools** platform-wide; only **`propose_facet_assignment`**
4. **YAML agent profiles** + `mill.ai.profiles.seed`

## In Scope

### A. Tool matrix (normative)

| Tool | Capability | Change summary |
|------|------------|----------------|
| `list_facet_types` | `metadata` | **Summary rows only** (reasoning); optional filters — **no** `contentSchema` ([`GAPS.md`](GAPS.md) §3b) |
| `get_facet_type` | `metadata` | **Full manifest** + `contentSchema` for one `facetTypeKey` (generation) |
| `list_metadata_scopes` | `metadata` | **Context-sensitive** assignable scopes (chat vs MCP); see [`GAPS.md`](GAPS.md) §3c |
| `validate_facet_payload` | `metadata` | Validates **`(facetType, payload)`** against **`contentSchema`**; **reuse/extend** for **`applicableTo`** when `metadataEntityId` (target) supplied |
| `propose_facet_assignment` | `metadata-authoring` | **`(target, facetType, payload)`**; delegates schema + **`applicableTo`** checks to **`metadata`** shared validation — capture only when valid |
| `capture_description` | `schema-authoring` | **Remove** |
| `capture_relation` | `schema-authoring` | **Remove** |
| *(any future `capture_<facet>`)* | *any capability* | **Forbidden** — use `propose_facet_assignment` + `facetTypeKey` |

**Normative rule:** facet writes use **one** CAPTURE tool — **`propose_facet_assignment`** under **`metadata-authoring`**. No typed capture tool names.

Document **before/after** prompt flow for `schema-authoring` and `metadata-authoring` profiles.

### A2. Prompt enforcement — utterance → facet (normative)

When **`metadata-authoring`** capability is on the profile, the agent **must** attempt catalog facet
capture for user statements that **document** data (constraints, meaning, relations, DQ rules) — not
answer only in prose and not jump to SQL unless the user asks to **retrieve** data.

**New / expanded YAML prompts** (see [`STORY.md`](STORY.md) § Prompt enforcement):

| Prompt | Purpose |
|--------|---------|
| `metadata-authoring.intent` | Classify turn: `AUTHOR_FACET` vs explore vs query vs clarification |
| `metadata-authoring.reasoning` | Pattern table: utterance → `facetTypeKey` + entity kind + payload hints |
| `metadata.faceting.system` | Grounding + “if `AUTHOR_FACET`, run validate → capture” |
| `metadata.faceting.request` | Structured fields before `propose_facet_assignment` |

**Worked example (required in design doc):**

> User: “In table **orders**, column **customer_id** must not be null.”

1. Intent: **`AUTHOR_FACET`** (normative constraint on a column — not a row query).
2. Ground: `list_tables` / `list_columns` → `orders.customer_id` → `metadataEntityId` = attribute URN.
3. `list_facet_types` filtered by attribute → select **`dq-null-check`**.
4. Payload from `contentSchema`: e.g. `{ "name": "customer_id_not_null", "description": "…", "severity": "error" }`.
5. `validate_facet_payload` → `propose_facet_assignment`.

**Not:** `SELECT … WHERE customer_id IS NOT NULL` unless the user asked to see violating rows.

**Cross-capability priority:** In profiles that load **`sql-query`** and **`metadata-authoring`**
(e.g. legacy `schema-authoring`), `metadata-authoring.intent` wins for documentary utterances;
`sql-query.system` wins only when intent is **`QUERY_DATA`**.

### A3. Multi-facet batch (normative)

One user turn may require **multiple** facet proposals. Rules:

| Rule | Detail |
|------|--------|
| **Decompose** | Split rich NL into independent `(target, facetType, payload)` tuples before capture. |
| **Emit all** | Parallel **`propose_facet_assignment`** per tuple in one model round when possible. |
| **Do not truncate** | Never stop after the first successful capture if the utterance implies more. |
| **Artefacts** | Each success → separate **`facet-proposal`** chat artefact (same kind; distinct JSON bodies). |
| **Runtime** | Agent must emit and persist **N** captures per turn — not only the last parallel call ([`LangChain4jAgent`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/runtime/langchain4j/LangChain4jAgent.kt) gap). |
| **UI** | Chat renders **all** `facet-proposal` entries in `artifacts[]` ([`artifactGroups.ts`](../../../../ui/mill-ui/src/components/chat/artifactPreview/artifactGroups.ts)). |

Prompt: **`metadata-authoring.batch`** (see [`STORY.md`](STORY.md) § Multi-facet batch).

### A4. Protocol batch envelope (proposal → **WI-351**)

Prefer **one `ProtocolFinal`** with **`results[]`** for **`metadata.faceting.capture`** — see [`STORY.md`](STORY.md) § *Protocol: batch ProtocolFinal*. **Implementation:** [WI-351](WI-351-multi-artifact-protocol-runtime.md) (before WI-347). Design doc must specify:

- YAML flag (`multi: true` or `structured_final_batch`)
- `finalSchema.results[]` item shape (= current single proposal)
- Fan-out rules: persistence, SSE, GET replay → **N** flat `facet-proposal` artefacts (not one array card)

**Interim:** N × `ProtocolFinal` acceptable only in **WI-351** if batch envelope slips; must not lose facets in live SSE (`replace` mode).

### B. Authoring loop (normative sequence)

```
ground target entity (schema tools) → resolve metadataEntityId (URN)
→ list_facet_types [optional filters]              # reasoning — shortlist facetTypeKey
→ get_facet_type(facetTypeKey)                     # generation — read contentSchema
→ validate_facet_payload(facetType, payload [, target])
→ propose_facet_assignment(target, facetType, payload)
→ facet-proposal artefact
```

**Proposal tuple (normative):**

| Field | Tool arg | Meaning |
|-------|----------|---------|
| **target** | `metadataEntityId` | Canonical entity URN the facet is assigned to |
| **facetType** | `facetTypeKey` | Catalog facet type key or URN from `list_facet_types` |
| **payload** | `payload` | JSON object **conforming to** that type's `contentSchema` |

`validate_facet_payload` and `propose_facet_assignment` share the **same schema validation**;
capture must not succeed when validation fails.

### C. Chat artefact — **one kind for all facet types**

Every successful **`propose_facet_assignment`** is saved as a **chat artefact** (stream + durable
store for replay). **The artefact kind is identical for every facet type** — it means
“metadata facet captured (proposal)”, not “descriptive captured” vs “DQ captured”.

| Layer | Value | Notes |
|-------|--------|--------|
| Descriptor id | `inferred-facet` | [`metadata-authoring.yaml`](../../../../ai/mill-ai/src/main/resources/capabilities/metadata-authoring.yaml) |
| Wire `partType` | **`facet-proposal`** | Same SSE / GET replay shell as today ([`chat-artefact-architecture.md`](../../../design/ai/chat-artefact-architecture.md)) |
| `persistKind` | **`metadata.faceting.capture`** | Single persist lane for all facet proposals |
| Protocol | `metadata.faceting.capture` | `STRUCTURED_FINAL` / OnCaptureSuccess |
| **Per-type identity** | **`facetTypeKey`** (+ `metadataEntityId`, `payload`) **inside content** | UI may render type-specific read-only bodies; **no** separate `partType` per facet slug |

**After WI-350:** no facet proposals via `schema.authoring.capture` / `captureType: description|relation`;
all facets use the row above. Remove or bypass [`FacetProposalWire`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/core/artifact/FacetProposalWire.kt)
`captureType` → `facetTypeKey` bridging for new captures.

### D. `applicableTo` — **`metadata` capability** (reuse / extend)

**`applicableTo`** logic lives on the **`metadata`** capability (QUERY), not ad hoc in
**`metadata-authoring`**:

| Concern | Owner | Notes |
|---------|--------|--------|
| Expose `applicableTo` on catalog rows | `list_facet_types` | Summary rows include **`applicableTo`**; optional filter by entity kind or target URN |
| Full schema for payload | `get_facet_type` | One type’s full **`contentSchema`** — not on list output ([`GAPS.md`](GAPS.md) §3b) |
| Resolve entity kind from target URN | `MetadataReadPort` / **`validate_facet_payload`** | Adapter resolves kind from URN for **`applicableTo`** (**WI-346**) |
| Ground catalog paths | **`schema`** tools (existing) | `list_schemas` / `list_tables` / `list_columns` → canonical names; optional **`metadataEntityId`** on responses (**WI-347**) — no `build_metadata_entity_urn` tool ([`GAPS.md`](GAPS.md) §3a) |
| Check type applies to target | `validate_facet_payload` | **Extend input** with optional `metadataEntityId` (target) to validate **`applicableTo`** alongside schema |
| Capture gate | `propose_facet_assignment` | Calls same **`metadata`** validation helper (schema + applicableTo + target) |

Add shared internal API in `mill-ai` (e.g. `FacetAssignmentValidator` in metadata package) used by
both QUERY and CAPTURE tool handlers.

### E. Facet-type coverage examples

At minimum document worked examples for:

- `descriptive` (table/column)
- `relation-source` or `relation` (table)
- `dq-null-check` (attribute)
- `dq-predicate` (table)

Reference platform seeds and [`dq-rule-facet-types.md`](../../../design/metadata/dq-rule-facet-types.md).

### F. YAML profiles + seed config

- `kind: AgentProfile` document spec
- `mill.ai.profiles.seed.resources` (default classpath seed path)

### G. Tool gaps (proposed extensions)

See **Tool gaps and proposals** in [`STORY.md`](STORY.md). **`get_facet_type`** and **`list_metadata_scopes`** locked in [`GAPS.md`](GAPS.md) §3b / §3c. Remaining P1: optional **`examplePayload`** on facet type seeds.

### H. Out of scope callouts

- Unified Mill `kind` seed runner
- Persistence write API
- **M-32** admin UI

## Acceptance Criteria

- [ ] Design doc includes tool matrix and authoring loop diagram or table
- [ ] Explicit rule: **no `capture_<specific facet>`** tools in any capability manifest
- [ ] Chat artefact contract documented: **one** `facet-proposal` kind; `facetTypeKey` in body only
- [ ] **Prompt enforcement** documented: `metadata-authoring.intent` + reasoning cookbook; dq-null-check worked example; SQL vs facet disambiguation
- [ ] **Multi-facet batch** documented: parallel capture, N artefacts per turn, UI replay
- [ ] **Batch `ProtocolFinal`** (`results[]` on `metadata.faceting.capture`) documented with fan-out to N `facet-proposal` persist/SSE rows
- [ ] STORY architectural table matches design doc

## Suggested commit

`[docs] WI-345: catalog-generic facet authoring and profile YAML design`
