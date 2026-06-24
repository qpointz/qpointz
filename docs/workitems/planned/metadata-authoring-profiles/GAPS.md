# Gaps and open decisions — metadata-authoring-profiles

**Story:** [`STORY.md`](STORY.md)  
**Branch:** `feat/meta-capability-improve`  
**Status:** planning review (not implementation)

This document collects **gaps**, **ambiguities**, and **decisions still needed** before or during implementation. Resolved items should be moved to WI acceptance criteria or STORY architectural table, then struck from here.

---

## 1. WI-351 — proof strategy before WI-347 — **LOCKED**

**Gap (original):** WI-351 requires two successful CAPTURE emissions → two artefacts, but catalog-generic `propose_facet_assignment` and full validation land in **WI-347**.

| Option | Approach | Verdict |
| ------ | -------- | ------- |
| **A** | Two parallel `capture_description` on `schema-authoring` profile | **Rejected** — couples to legacy tools removed in WI-350 |
| **B** | Test-only capability YAML with dummy multi-capture tool | **Rejected** as normative — optional supplement only |
| **C** | Unit tests only — mock capture results in isolation | **Rejected** alone — insufficient without downstream chain |
| **D** | **Layered mock-LLM + per-layer unit tests** | **Locked** — normative; see below |

### Locked decision (2026-06-24)

**Normative proof vehicle: Option D — layered mock, no real LLM, no legacy `capture_*`.**

WI-351 acceptance is proven by mocking LLM tool output with **two parallel `propose_facet_assignment`** calls (facet artefacts) and asserting correct handling at each downstream layer. `propose_facet_assignment` already exists in [`metadata-authoring.yaml`](../../../../ai/mill-ai/src/main/resources/capabilities/metadata-authoring.yaml); WI-347 extends catalog breadth and prompts — WI-351 does **not** wait for WI-347.

| Layer | Module / file | What to mock / inject | Assert |
| ----- | ------------- | --------------------- | ------ |
| **L1 Agent** | `mill-ai` — [`LangChain4jAgentEmitTest`](../../../../ai/mill-ai/src/test/kotlin/io/qpointz/mill/ai/runtime/langchain4j/LangChain4jAgentEmitTest.kt) | Inner `StreamingChatModel` returns **two** `ToolExecutionRequest`s for `propose_facet_assignment` (e.g. `descriptive` + `dq-null-check`); mock [`MetadataReadPort`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/capabilities/metadata/MetadataReadPort.kt) with minimal manifests for both types | **One** batch `ProtocolFinal` (`metadata.faceting.capture`) with `{ results: […, …] }`; turn terminates |
| **L2 Normalizer** | `mill-ai` — unit test for batch expand/collapse helper | Scalar capture map ↔ `{ results: [one] }`; batch `{ results: [a, b] }` → two facet maps | Backward compat for single capture |
| **L3 Persist** | `mill-ai` — [`StandardPersistenceProjectorTest`](../../../../ai/mill-ai/src/test/kotlin/io/qpointz/mill/ai/persistence/StandardPersistenceProjectorTest.kt) | Routed `ProtocolFinal` content with batch payload (no agent) | **N** `ArtifactRecord` rows, same `turnId` / `persistKind` |
| **L4 SSE** | `mill-ai` — [`AgentEventToSseMapperTest`](../../../../ai/mill-ai/src/test/kotlin/io/qpointz/mill/ai/sse/ChatSseEventTest.kt) | `AgentEvent.ProtocolFinal` with batch facet payload | **N** `item.part.updated` (`partType: facet-proposal`, **append** after first) |
| **L5 GET wire** | `mill-ai-service` — [`ArtifactWireMapperTest`](../../../../ai/mill-ai-service/src/test/kotlin/io/qpointz/mill/ai/service/ArtifactWireMapperTest.kt) | **N** persisted facet rows | **N** wire `artifacts[]` entries (`kind: facet-proposal`) |
| **L6 UI** | `ui/mill-ui` — Vitest | `artifacts[]` with 2+ `facet-proposal` (no live SSE) | [`artifactGroups`](../../../../ui/mill-ui/src/components/chat/artifactPreview/artifactGroups.ts) / [`MessageArtifactComposer`](../../../../ui/mill-ui/src/components/chat/artifactPreview/MessageArtifactComposer.tsx) render **2+** cards |

**Out of scope for WI-351 proof:** `mill-ai-test` live-LLM scenarios (defer to **WI-349**); legacy `capture_description` ×2.

**Primary envelope:** one batch `ProtocolFinal` with `results[]` (not N scalar finals). N × scalar finals remain an **interim** escape hatch only per GAPS §21.

**Owner:** acceptance criteria in [`WI-351-multi-artifact-protocol-runtime.md`](WI-351-multi-artifact-protocol-runtime.md) §6 Tests.

---

## 2. `MetadataReadPort` and `applicableTo` — **LOCKED**

**Gap (original):** Story requires `validate_facet_payload(facetType, payload [, metadataEntityId])` for **applicableTo** checks. Current port:

```kotlin
fun validateFacetPayload(facetTypeKey: String, payload: Map<String, Any?>): List<String>
```

| Option | Verdict |
| ------ | ------- |
| Extend **`MetadataReadPort`** with optional **`metadataEntityId`** | **Locked** |
| Handler-only applicability in **`MetadataCapabilities`** (no port change) | **Rejected** |

### Locked decision (2026-06-24)

**Extend `MetadataReadPort.validateFacetPayload` with an optional `metadataEntityId` (target entity URN).**

```kotlin
fun validateFacetPayload(
    facetTypeKey: String,
    payload: Map<String, Any?>,
    metadataEntityId: String? = null,
): List<String>
```

| Concern | Owner | Notes |
| ------- | ----- | ----- |
| Port contract + adapter implementation | **WI-346** | When `metadataEntityId` is present, validate **`applicableTo`** (facet type vs target entity kind) in addition to **`contentSchema`** / manifest rules; when absent, schema-only (backward compatible) |
| Tool handler wiring | **WI-347** | `validate_facet_payload` and `propose_facet_assignment` pass target URN through to port; no duplicate applicability rules in **`metadata-authoring`** |
| Shared helper | `mill-ai` | `validateFacetPayloadInternal` and mocks (`EmptyMetadataReadPort`, harness, tests) updated in same WI as contract change |

**Rationale:** Single validation path for schema + applicability; adapter can resolve entity kind from URN and consult `FacetTypeManifest.applicableTo` without ad hoc handler logic.

**Owner:** [`WI-346-metadata-read-port-adapter.md`](WI-346-metadata-read-port-adapter.md) (port + adapter); [`WI-347-metadata-authoring-capability.md`](WI-347-metadata-authoring-capability.md) (tools).

---

## 3. P1 tools — no owning WI

From STORY § Tool gaps — not in any WI acceptance criteria:

| Item | Purpose | Status |
|------|---------|--------|
| **`build_metadata_entity_urn`** | Catalog path → canonical `metadataEntityId` | **Locked: not needed** — see §3a |
| **`get_facet_type`** | Full manifest + **`contentSchema`** for one type | **Locked** — see §3b |
| **`list_metadata_scopes`** | Context-sensitive assignable scopes | **Locked** — see §3c |

**Decision needed (remaining):** §4 catalog data only.

### 3a. `metadataEntityId` resolution — **LOCKED**

**No dedicated `build_metadata_entity_urn` tool for v1.** Target entity URNs are resolved through **existing** capabilities:

| Step | Tool / asset | Role |
| ---- | ------------ | ---- |
| 1 Ground | **`schema`**: `list_schemas` → `list_tables` → `list_columns` (and `list_relations` when needed) | Canonical `schemaName` / `tableName` / `columnName` — same flow as today |
| 2 Derive URN | Prompts + [`metadata-urn-platform.md`](../../../design/metadata/metadata-urn-platform.md) | Map grounded path → `urn:mill/model/schema:…`, `table:…`, `attribute:…` (normative grammar) |
| 3 Verify (optional) | **`metadata`**: `list_entity_facets(metadataEntityId)` | Confirms entity + existing facets when useful before capture |
| 4 Gate | **`metadata`**: `validate_facet_payload(…, metadataEntityId?)` | Rejects malformed URNs, **`applicableTo`** mismatches, schema errors (**WI-346** port) |

**Optional enhancement (WI-347 — not a separate tool):** today **`list_schemas` / `list_tables` / `list_columns`** return catalog **names** only (`schemaName`, `tableName`, `columnName`, …). The LLM must **construct** the target URN itself using prompt rules + [`metadata-urn-platform.md`](../../../design/metadata/metadata-urn-platform.md) (e.g. `skymill` + `orders` + `customer_id` → `urn:mill/model/attribute:skymill/orders/customer_id`). **Enhancement:** add a **`metadataEntityId`** string on each row (computed server-side via [`MetadataEntityUrnCodec`](../../../../data/mill-data-schema-core/src/main/kotlin/io/qpointz/mill/data/schema/MetadataEntityUrnCodec.kt)) so the model **copies** the canonical URN into `propose_facet_assignment` instead of guessing syntax. **v1 minimum:** prompts + `validate_facet_payload(…, metadataEntityId)` catch bad URNs — the extra field is **convenience**, not required for the story to work.

**Owner:** prompt + schema-output extension → **WI-347**; URN validation on port → **WI-346**.

### 3b. `list_facet_types` + `get_facet_type` — **LOCKED**

**Normative v1:** two tools on the **`metadata`** capability — minimal list for **reasoning**, full manifest for **generation**.

| Tool | Phase | Wire output | Prompt owner |
| ---- | ----- | ----------- | ------------ |
| **`list_facet_types`** | **Reasoning** — shortlist facet type(s) for the utterance + target | **Summary rows only** (no full `contentSchema`) | **`metadata-authoring.reasoning`** (+ optional filters in tool args) |
| **`get_facet_type`** | **Generation** — build `payload` from schema | **Full** `FacetTypeManifest` including **`contentSchema`** | **`metadata.faceting.request`** (+ **`metadata-authoring.batch`** for multi-type turns) |

**`list_facet_types` summary row (minimum):**

| Field | Required |
| ----- | -------- |
| `facetTypeKey` (and/or type URN) | yes |
| `category` | yes |
| `applicableTo` | yes |
| `description` | yes |
| `title` | recommended |
| `targetCardinality`, `source` | optional when present on manifest |

Omit nested **`contentSchema`** / payload field trees from list output.

**`get_facet_type` input:** `facetTypeKey` (or type URN). **Output:** full manifest for that type only.

**Authoring loop (normative):**

```
ground (schema tools) → metadataEntityId
→ list_facet_types [optional filters]     # reasoning — pick facetTypeKey
→ get_facet_type(facetTypeKey)          # generation — read contentSchema
→ validate_facet_payload(…)
→ propose_facet_assignment(…)
```

Optional **`list_facet_types` filters** (WI-347): `category`, `applicableTo`, `metadataEntityId` (narrow to types valid for target).

**Cross-capability / profile impact:**

| Surface | Impact |
| ------- | ------ |
| **`metadata` capability** | **Only code owner** — reshape `list_facet_types` mapper; add `get_facet_type` handler |
| **`metadata-authoring`** | **Prompts only** — reasoning vs generation steps; no separate tool registration |
| **`MetadataReadPort` / validation** | **Unchanged wire** — `validateFacetPayloadInternal` keeps using `port.listFacetTypes()` (full manifests); tool output shape does not affect validation |
| **Profiles loading `metadata`** | **All gain `get_facet_type`** automatically: `schema-exploration`, `data-analysis`, `schema-authoring`, `metadata-authoring` — not authoring-only |
| **`schema-exploration`** | Usually **`list_entity_facets`** to read assigned facets; call **`get_facet_type`** only when inspecting type shape |
| **`data-analysis`** | **`metadata.faceting.system`** must stay **neutral** (no authoring loop); facet capture rules live in **`metadata-authoring.intent`** (authoring profiles only) |
| **`schema`**, **`sql-query`**, **`value-mapping`**, **`schema-authoring`** tools | **No change** |
| **MCP inventory** | Add **`get_facet_type`** row ([`v3-mcp-capability-exposure.md`](../../../design/agentic/v3-mcp-capability-exposure.md)) — **WI-349** doc note |
| **`mill-py` / REST** | Already expose list + get-by-key — aligned |

**Owner:** **WI-347** (`metadata.yaml`, `MetadataCapabilities.kt`, summary/full mappers, prompts). Optional port `getFacetType(key)` on **WI-346** adapter for efficiency only.

**Proof (WI-347):** catalog with **≥3 facet families** + one large `contentSchema`; unit test that `list_facet_types` omits `contentSchema` and `get_facet_type` returns it (resolves **GAPS §17**).

### 3c. `list_metadata_scopes` — **LOCKED** (Option B + empty-list caveat)

**Decision (2026-06-24):** **Option B — context-sensitive `list_metadata_scopes`**, with explicit **persistence vs metadata-merge** split when the scope list is empty.

**Decision (2026-06-24, scope rows):** In **chat**, return **global + chat** scopes; each row flags **`writable`**. **Global is read-only** for authoring; **default writable scope** is the chat scope, carried on **`AgentContext`** (not inferred by the LLM). **`metadata`** QUERY tools **read** merged metadata from **all** listed scopes; **`metadata-authoring`** CAPTURE must target a **`writable: true`** scope only.

#### Chat scope URN grammar — **LOCKED**

```
urn:mill/metadata/scope:chat-<chatId>
```

where **`<chatId>`** is the conversation GUID from transport / **`AgentContext`** (same id as HTTP chat path).

#### Chat scope persistence — **LOCKED** (`metadata_scope`)

Each chat has a **durable row** in **`metadata_scope`** (Metadata service JPA / [`MetadataScopeEntity`](../../../../metadata/mill-metadata-persistence/src/main/kotlin/io/qpointz/mill/persistence/metadata/jpa/entities/MetadataScopeEntity.kt)). **Ensure-or-create** idempotently when the agent runs for a bound `chatId` (first `sendMessage` or prelude load — **WI-233** / chat runtime).

| Field | Value |
| ----- | ----- |
| **`scope_res` / URN** | `urn:mill/metadata/scope:chat-<chatId>` |
| **`scope_type`** | **`CHAT`** |
| **`reference_id`** | **`chatId`** (conversation GUID) |
| **`display_name`** | **`Chat <title>`** — `<title>` = chat **`chatName`** from AI chat persistence ([`ChatResponse.chatName`](../../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/dto/ChatDtos.kt)); update when user renames chat |
| **`owner_id`** | Chat **`userId`** (conversation owner) |
| **`visibility`** | **`PRIVATE`** |

Register **`CHAT`** scope type and **`MetadataUrns.scopeChat(chatId)`** helper in **WI-233** (metadata core). **`list_metadata_scopes`** resolves the row (create if missing) before returning the writable chat scope row.

**Not** a separate AI sidecar store — facet assignments promoted into chat scope use normal Metadata **`metadata_entity_facet`** rows keyed by this scope URN.

**Global (read catalogue):** `urn:mill/metadata/scope:global` — always **`writable: false`** in chat authoring.

#### Tool behaviour

Add **`list_metadata_scopes`** on the **`metadata`** capability. Handler reads **`AgentContext`** (transport — `chatId`, MCP auth, default writable scope, …):

| Runtime | Rows returned | Writable default |
| ------- | ------------- | ---------------- |
| **HTTP chat** (`chatId` bound) | **`global`** (`writable: false`) + **`chat-<chatId>`** (`writable: true`) | Chat scope on **`AgentContext`**; prompts instruct capture to use **writable** row only |
| **MCP** (no chat session) | Catalogue scopes from auth (at minimum **global**); **`writable`** per **`AgentContext`** / policy | No chat row; never invent `chat-*` scope |
| **Harness / tests** | Configurable fake list + writable flags | Explicit test doubles |

**Scope row (minimum):**

| Field | Required | Notes |
| ----- | -------- | ----- |
| `scopeUrn` | yes | e.g. `urn:mill/metadata/scope:global`, `urn:mill/metadata/scope:chat-<chatId>` |
| `writable` | yes | **`metadata-authoring`** may only capture to scopes with **`writable: true`** |
| `label` | recommended | UI / prompt hint (“Global catalogue”, “This chat”) |

**Read vs write split:**

| Capability | Scope use |
| ---------- | --------- |
| **`metadata`** (QUERY — `list_entity_facets`, validation context, …) | **Read** merged metadata from **all** scopes relevant to the invocation (global ∪ chat ∪ team per **`MetadataReadContext`**) |
| **`metadata-authoring`** (CAPTURE — `propose_facet_assignment`) | **Write** proposals only to **`writable: true`** scope; reject or fail validation if `scopeUrn` points at read-only scope |

**`AgentContext` responsibilities (WI-347 / autoconfigure):** bind `chatId`, derive chat scope URN, set **default writable scope** for the turn (chat scope in HTTP chat). **`list_metadata_scopes`** reflects context; capture validator enforces writable target.

#### Capture → scope assignment rule

When the model calls **`propose_facet_assignment`** with a **`scopeUrn`** from **`list_metadata_scopes`**:

- **Writable scope available** → artefact **persisted** (chat SSE/GET replay) **and** tagged with **`scopeUrn`** for **merge into that metadata scope** when the consumer promotes/writes (M-23 / chat-scope projection). In chat, **`scopeUrn`** should be the **chat** scope unless product explicitly allows another writable catalogue scope (MCP).
- **`list_metadata_scopes` returned `[]` (empty)** → capture may still **emit and persist** the **`facet-proposal` chat artefact**, but it is **NOT saved or merged into any metadata scope**. No implicit global write. **Consumer responsibility** (chat UI, promotion flow, MCP client) decides what to do with orphan proposals.
- **`scopeUrn` on read-only scope** → **reject** at capture (shared validator) — do not persist as scope-targeted proposal.

This story remains **proposal-only** for the metadata service — “saved into scope” means **artefact carries `scopeUrn` + consumer merge path**, not automatic `POST` to Metadata REST in WI-347.

#### Authoring loop (normative)

```
ground (schema tools) → metadataEntityId
→ list_metadata_scopes()              # global (read) + chat (write) in HTTP chat
→ pick scopeUrn where writable: true  # default: chat scope from AgentContext
→ list_facet_types → get_facet_type → validate_facet_payload → propose_facet_assignment(…, scopeUrn)
```

Prompts: **`metadata-authoring`** must use **`writable: true`** scope only; **`metadata`** read tools may reference facets from global + chat merge.

#### Implementation owners

| Piece | Owner |
| ----- | ----- |
| Tool + context-sensitive handler + scope row shape | **WI-347** (`metadata.yaml`, `MetadataCapabilities.kt`, **`AgentContext`**) |
| Default writable scope on **`AgentContext`** | **WI-347** / autoconfigure |
| Capture rejects read-only `scopeUrn` | **WI-347** (`MetadataAuthoringCapability` + shared validator) |
| Chat scope URN + prelude merge | **WI-233** — `metadata_scope` row ensure-or-create; display name sync on rename |
| Empty-list + orphan consumer semantics | **WI-347** + [`ai-v3-chat-metadata-scope.md`](../../../design/agentic/ai-v3-chat-metadata-scope.md) |
| MCP inventory row | **WI-349** |

**Rejected for v1:** **A** (defer tool), **C** (implicit scope only), **D** (catalogue-only list).

**Cross-links:** GAPS §5 (`scopeUrn` on artefact); WI-233; M-23.

---

## 4. P1 catalog data — no owning WI

| Item | Purpose |
|------|---------|
| **`examplePayload` / `examples[]`** on facet type YAML seeds | Few-shot per type (especially DQ, relation) |
| **Category index** in `metadata-authoring.reasoning` | Short map descriptive / relation / dq → `list_facet_types` |

**Decision needed:** required for v1 (WI-347 / metadata seeds) or follow-up?

---

## 5. `scopeUrn` and `mergeAction` on capture — **partially locked**

**Gap (remaining):** **`mergeAction`** on capture artefact — default `SET` vs defer.

**Locked via GAPS §3c:**

- **`scopeUrn`** on **`propose_facet_assignment`** / **`facet-proposal`** artefact JSON — must be a **`writable: true`** scope from **`list_metadata_scopes`** (chat: `urn:mill/metadata/scope:chat-<chatId>`).
- **Global** in chat is **read-only** for authoring; **`metadata`** reads all scopes, **`metadata-authoring`** writes writable only.
- **Empty scope list:** artefact **still persisted** for chat replay; **not** merged into metadata scope — **consumer** handles orphans.

Domain merge semantics apply at **promotion/write** time, not in WI-347 capture.

**Decision needed:** include **`mergeAction`** on artefact body in WI-347, or defer until write workflow (M-23)?

---

## 6. Relation facet type keys

**Gap:** Examples and prompts mix **`relation`**, **`relation-source`**, **`relation-target`**. Platform seeds define a **family** of relation types; unified `RelationFacet` normalization exists in data layer.

**Decision needed:** normative rule for LLM + scenarios, e.g.:

- Always consult `list_facet_types` filtered by entity kind + category; or
- Prefer unified **`relation`** when catalog exposes it; or
- Document per-use-case mapping in design doc ([`schema-facet-ai-tool-field-mapping.md`](../../../design/metadata/schema-facet-ai-tool-field-mapping.md)).

Without this, WI-349 relation scenarios and prompts may disagree.

---

## 7. `schema-authoring` capability after WI-350

**Gap:** WI-350 rewrites schema-authoring prompts toward `propose_facet_assignment`, but that CAPTURE tool is on **`metadata-authoring`**, not **`schema-authoring`**.

Current WI-350 out of scope: removing **`request_clarification`**.

**Clarify target state:**

| Asset | Intended owner after story |
|-------|----------------------------|
| `propose_facet_assignment` | `metadata-authoring` capability only |
| `metadata-authoring.intent` / `.reasoning` / `.batch` | `metadata-authoring.yaml` (WI-347) |
| `request_clarification` | `schema-authoring` only? or move to `metadata-authoring`? |
| `schema-authoring.intent` / `.batch` | Remove or reduce to non-facet helpers |

**Decision needed:** WI-350 scope statement — schema-authoring capability = clarification-only vs empty shell kept for profile compatibility.

---

## 8. Profile strategy — `schema-authoring` vs `metadata-authoring`

**Gap:** Story adds **`metadata-authoring`** profile (`conversation`, `schema`, `metadata`, `metadata-authoring` — no SQL). Deploy defaults remain **`schema-authoring`** ([`apps/mill-service/application.yml`](../../../../apps/mill-service/application.yml), GCP config).

**WI-348:** “document only” for `mill.ai.chat.default-profile` — no default change.

**Open questions:**

- Recommended operator choice: facet-only work → **`metadata-authoring`** profile?
- Does **`schema-authoring`** profile keep **`sql-query`**, **`value-mapping`**, **`sql-dialect`** after this story?
- Should public docs deprecate facet capture on `schema-authoring` in favour of `metadata-authoring`?

---

## 9. Partial batch failure (multi-facet turn)

**Gap:** User message implies N facets; parallel capture returns mixed success/failure.

**Not specified:**

- Emit artefacts for **successful** captures only and continue tool round for failures?  
- Or treat batch as all-or-nothing?

**Recommendation (for review):** emit successes; remediate failures in next iteration (align with legacy `schema-authoring.batch` / capture-remediation). Add to WI-351 and WI-345.

**Decision needed:** confirm or reject.

---

## 10. Mixed artefact turns (SQL + facets in one message)

**Gap:** e.g. “orders must be not null” + “show me order counts” in one utterance.

**Not specified:** single-turn SQL artefact **and** facet proposals, or intent picks one primary path.

**Recommendation (for review):** out of scope for v1 — intent classifies primary task; user sends follow-up for secondary. Document in STORY out of scope.

**Decision needed:** confirm.

---

## 11. `FacetProposalWire` and historical replay

**Gap:** WI-350 removes `schema.authoring.capture` / `capture_description` path for **new** captures. [`FacetProposalWire`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/core/artifact/FacetProposalWire.kt) still normalizes legacy shapes for GET replay.

**Decision needed:**

- Keep wire normalization **indefinitely** for old chats?  
- Or migration window + deprecation note in WI-349?

---

## 12. `HarnessMetadataReadPort` timing

**Gap:** Story requires harness catalog beyond **descriptive** (relation + DQ) for scenarios. Expansion is listed in **WI-349**; **WI-347** unit tests likely need it earlier.

**Recommendation (for review):** expand harness in **WI-346** or **WI-347**; WI-349 adds scenario packs only.

**Decision needed:** assign WI.

---

## 13. Design doc ownership and paths

| Deliverable | Currently assigned |
|-------------|-------------------|
| Normative tool matrix + authoring loop | WI-345 (WI markdown); partial [`metadata-facet-catalog-v3.md`](../../../design/agentic/metadata-facet-catalog-v3.md) |
| Batch `ProtocolFinal` / fan-out | WI-351 → [`artifact-foundation.md`](../../../design/agentic/artifact-foundation.md) |
| Catalog-generic authoring (full) | WI-349 → rewrite `metadata-facet-catalog-v3.md` |
| Public operator guide | WI-349 → `docs/public/src/mill-ui.md` |

**Gap:** WI-345 does not name a single canonical design file (new `metadata-authoring-catalog.md` vs extend v3).

**Decision needed:** one primary design doc path to avoid drift between WI-345 and WI-349.

---

## 14. `SchemaExplorationAgent` vs `LangChain4jAgent`

**Gap:** WI-351 mentions updating **`SchemaExplorationAgent`** “if applicable”. Production chat uses **`LangChain4jAgent`** only ([`LangChain4jChatRuntime`](../../../../ai/mill-ai-autoconfigure/src/main/kotlin/io/qpointz/mill/ai/autoconfigure/chat/LangChain4jChatRuntime.kt)). `SchemaExplorationAgent` appears unused in service path.

**Recommendation (for review):** WI-351 scope = **LangChain4jAgent** only; WI-350 updates dead `SchemaExplorationAgent` prompt text if still present.

**Decision needed:** confirm deferral or dual maintenance.

---

## 15. Active artifact pointers vs multi-facet

**Gap:** Descriptor uses **`last-metadata-facet-proposal`** (singular). Multi-facet turns produce N records; pointer stores **last** only.

**Clarify:** GET replay and UI use **`artifacts[]` on turn**, not pointer — document in WI-351. Any feature still relying on pointer for multi-facet needs audit.

---

## 16. SSE `item.completed` hint with N structured parts

**Gap:** `AgentEventToSseMapper` keeps one `structuredCompletionPartType` per turn (last structured final wins). Live UI accumulates artefacts via **`onNonTextPartUpdated`** — likely OK.

**Verify in WI-351:** `item.completed` `partType` hint does not break multi-facet layout when N `item.part.updated` events precede it.

---

## 17. `list_facet_types` payload size / truncation — **LOCKED** (see §3b)

**Resolved by §3b:** `list_facet_types` returns **summary rows only**; full **`contentSchema`** via **`get_facet_type`**. WI-347 unit tests: list output omits nested schema; get returns it for a large type (catalog with 15+ types safe).

---

## 18. MCP tool surface

**Gap:** STORY says document `mcp.enabled` guidance; no WI owns MCP inventory update when profiles/capabilities change.

**Decision needed:** WI-348 or WI-349 adds MCP doc note; or out of scope.

---

## 19. Verify commands vs UI work

**Gap:** STORY verify block has no **`ui/mill-ui`** test task. WI-351 adds Vitest for multi-card display.

**Recommendation:** add `./gradlew :ui:mill-ui:test` (or targeted pattern) to STORY verify after WI-351.

---

## 20. Story checklist vs sequence table

**Gap:** [`STORY.md`](STORY.md) Work Items list order (346 before 348) does not match sequence table (348 before 346). Cosmetic only.

---

## 21. Interim N × `ProtocolFinal` vs batch envelope

**Gap:** Design prefers one batch `{ results: [] }`; interim allows N scalar finals if batch slips in WI-351.

**Risk:** interim path left in place — WI-351 acceptance should require batch envelope as primary; N-finals only behind explicit flag or removed before story close.

**Decision needed:** hard requirement on batch envelope for story closure?

---

## 22. `propose_facet_assignments` batch tool

**Gap:** STORY P2 defers batch **tool** if parallel `propose_facet_assignment` + WI-351 fan-out work.

**Decision needed:** if live-LLM tests (A-94) show unreliable parallel tool calls, escalate to in-story batch tool — trigger criteria undefined.

---

## Summary — decisions for product / tech review

| # | Topic | Choices |
|---|--------|---------|
| 1 | WI-351 test vehicle | **Locked: D** — mock LLM (2× `propose_facet_assignment`) + L1–L6 layer tests |
| 2 | `applicableTo` validation | **Locked:** extend `MetadataReadPort.validateFacetPayload(..., metadataEntityId?)` — WI-346 adapter, WI-347 tools |
| 3–4 | P1 tools + seed examples | §3b + §3c **locked**; §4 examples open |
| 5 | `scopeUrn` / `mergeAction` | **`scopeUrn` locked** (§3c); **`mergeAction`** open |
| 6 | Relation facet type keys | Normative mapping rule |
| 7 | `schema-authoring` capability post-350 | Clarification-only vs empty |
| 8 | Profile defaults & fat `schema-authoring` | Document / change default |
| 9 | Partial batch failure | Emit partial / all-or-nothing |
| 10 | SQL + facets same turn | In scope / out of scope |
| 11 | Legacy `FacetProposalWire` replay | Forever / sunset |
| 12 | Harness catalog breadth | WI-346 / WI-347 / WI-349 |
| 13 | Canonical design doc path | New file vs v3 rewrite |
| 21 | Batch envelope mandatory at story close | Yes / interim OK |

---

## Related

- [`STORY.md`](STORY.md) — architectural decisions, WI order  
- [`WI-345-metadata-authoring-design-contract.md`](WI-345-metadata-authoring-design-contract.md)  
- [`WI-351-multi-artifact-protocol-runtime.md`](WI-351-multi-artifact-protocol-runtime.md)  
- [`WI-347-metadata-authoring-capability.md`](WI-347-metadata-authoring-capability.md)
