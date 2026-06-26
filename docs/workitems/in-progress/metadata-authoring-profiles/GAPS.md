# Gaps and open decisions — metadata-authoring-profiles

**Story:** [`STORY.md`](STORY.md)  
**Branch:** staged per [`STORY.md`](STORY.md) § Staged delivery (not one long integration branch)  
**Status:** planning review — **§1–§18, §21–§22 locked**; §19–§20 resolved (hygiene); **no implementation yet**

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
| **`list_facet_categories`** | Category routing + guidance from **`MetadataContent`** | **Locked** — §4 |
| **`build_metadata_entity_urn`** | Catalog path → canonical `metadataEntityId` | **Locked: not needed** — see §3a |
| **`get_facet_type`** | Full manifest + **`contentSchema`** for one type | **Locked** — see §3b |
| **`list_metadata_scopes`** | Context-sensitive assignable scopes | **Locked** — see §3c |

**Decision needed (remaining):** §6–§10 and other open rows in summary table.

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

### 3c. `list_metadata_scopes` — **LOCKED** (Option B + access flags)

**Decision (2026-06-24):** **Option B — context-sensitive `list_metadata_scopes`**, with explicit **persistence vs metadata-merge** split when the scope list is empty.

**Decision (2026-06-25, scope rows):** In **chat**, return **global + chat** scopes; each row flags **`access`**: **`r`** (read-only), **`w`** (write-only), or **`rw`** (read-write). **Global** → **`r`**; **chat** → **`rw`**. Scopes are carried on **`AgentContext`** (not chosen by the LLM per capture call). **`metadata`** QUERY tools **read** merged metadata from scopes with **`r` or `rw`**; **`metadata-authoring`** CAPTURE writes to all **`w` + `rw`** scopes from context (see §5).

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

**Global (read catalogue):** `urn:mill/metadata/scope:global` — always **`access: r`** in chat authoring.

#### Tool behaviour

Add **`list_metadata_scopes`** on the **`metadata`** capability. Handler reads **`AgentContext`** (transport — `chatId`, MCP auth, default writable scope, …):

| Runtime | Rows returned | Write targets |
| ------- | ------------- | ------------- |
| **HTTP chat** (`chatId` bound) | **`global`** (`access: r`) + **`chat-<chatId>`** (`access: rw`) | Runtime sets **`writeScopeUrns[]`** = all **`w` + `rw`** scopes from context |
| **MCP** (no chat session) | Catalogue scopes from auth (at minimum **global**); **`access`** per **`AgentContext`** / policy | No chat row; never invent `chat-*` scope |
| **Harness / tests** | Configurable fake list + access flags | Explicit test doubles |

**Scope row (minimum):**

| Field | Required | Notes |
| ----- | -------- | ----- |
| `scopeUrn` | yes | e.g. `urn:mill/metadata/scope:global`, `urn:mill/metadata/scope:chat-<chatId>` |
| `access` | yes | **`r`** \| **`w`** \| **`rw`** — authoring writes to **`w` + `rw`** only |
| `label` | recommended | UI / prompt hint (“Global catalogue”, “This chat”) |

**`AgentContext.scopes` (WI-347 / autoconfigure):** array of `{ scopeUrn, access, label? }`. Chat default: global **`r`**, chat **`rw`**. **`list_metadata_scopes`** reflects context.

**Read vs write split:**

| Capability | Scope use |
| ---------- | --------- |
| **`metadata`** (QUERY) | **Read** merged metadata from scopes with **`r` or `rw`** |
| **`metadata-authoring`** (CAPTURE) | **No `scopeUrn` tool arg** — runtime stamps **`writeScopeUrns[]`** on artefact from context |

#### Capture → scope assignment rule (amended §5)

- **LLM does not pass `scopeUrn` or `mergeAction`** on **`propose_facet_assignment`**.
- On success, artefact body includes **`writeScopeUrns[]`** = all **`w` + `rw`** scopes from **`AgentContext`**.
- **`list_metadata_scopes` returned `[]` (empty)** → capture may still **persist** **`facet-proposal`** for chat replay; **`writeScopeUrns[]`** empty — **not** merged into metadata scope (consumer handles orphans).
- **`mergeAction`** is **metadata-owned** in **`FacetProposalMerger`** at scope assign — see §5; **not** an LLM tool arg.

This story remains **proposal-only** for the metadata service.

#### Authoring loop (normative)

```
ground (schema tools) → metadataEntityId
→ list_metadata_scopes()              # discover access flags
→ list_facet_categories() → list_facet_types → get_facet_type
→ validate_facet_payload → propose_facet_assignment(target, facetType, payload)
→ facet-proposal artefact with writeScopeUrns[] from context
```

#### Implementation owners

| Piece | Owner |
| ----- | ----- |
| Tool + context-sensitive handler + scope row shape | **WI-347** (`metadata.yaml`, `MetadataCapabilities.kt`, **`AgentContext`**) |
| Default writable scope on **`AgentContext`** | **WI-347** / autoconfigure |
| `AgentContext.scopes` + `writeScopeUrns[]` on artefact | **WI-347** (`MetadataAuthoringCapability` + autoconfigure) |
| Chat scope URN + prelude merge | **WI-233** — `metadata_scope` row ensure-or-create; display name sync on rename |
| Empty-list + orphan consumer semantics | **WI-347** + [`ai-v3-chat-metadata-scope.md`](../../../design/agentic/ai-v3-chat-metadata-scope.md) |
| MCP inventory row | **WI-349** |

**Rejected for v1:** **A** (defer tool), **C** (implicit scope only), **D** (catalogue-only list).

**Cross-links:** GAPS §5 (`scopeUrn` on artefact); WI-233; M-23.

---

## 4. P1 catalog data — **`MetadataContent`** — **LOCKED**

**Gap (original):** Few-shot examples and category cookbooks had no owning WI; risk of polluting **`FacetTypeDefinition`** / **`FacetTypeManifest`** with `examples[]`.

### Locked decision (2026-06-25)

**Do not** add `examples[]` or category guidance to facet type definitions. Introduce separate entity **`MetadataContent`** in metadata modules (**WI-352**).

#### Content kinds (v1)

| `contentKind` | `targetUrn` | Body (`mediaType: application/json`) |
| ------------- | ----------- | ------------------------------------ |
| **`facet-type-example`** | Facet type URN (`urn:mill/metadata/facet-type:…`) | `{ "metadataEntityId"?, "payload": {…} }` |
| **`facet-type-category`** | `urn:mill/metadata/facet-type-category:<slug>` | `{ "category", "summary", "signalPhrases[]", "antiSignalPhrases[]", "typicalEntityKinds[]", "exampleFacetTypeKeys[]", "nextStep" }` |

#### Standard row shape

`content_id`, `uuid`, `content_urn`, `content_kind`, `target_urn`, optional `scope_urn`, `title`, `description`, `content_body`, `media_type`, `sort_order`, `enabled`, `schema_version`, audit quad (`created_at`, `created_by`, `last_modified_at`, `last_modified_by`), audit listener.

#### Seed files (platform)

| File | Min rows |
| ---- | -------- |
| `platform-facet-category-guidance.yaml` | `general`, `relation`, `data-quality` |
| `platform-facet-authoring-examples.yaml` | `descriptive`, `relation-source`, **`relation-target`**, `dq-null-check`, `dq-predicate` |

**Import order:** facet type definitions → category guidance → examples (examples validate payload against target type `contentSchema`).

#### AI wire (WI-347)

| Tool | Role |
| ---- | ---- |
| **`list_facet_categories`** | Distinct categories from catalog + joined **`facet-type-category`** guidance |
| **`list_content`** | List `MetadataContent` by `targetUrn` / `contentKind` |
| **`get_content`** | Full content row by `contentUrn` |
| **`get_facet_type`** | Full manifest + **synthetic `examples[]`** joined from `facet-type-example` rows (wire only — not stored on manifest) |

**Category index in prompts:** **dynamic** via **`list_facet_categories`** — **not** a hardcoded table in `metadata-authoring.reasoning` YAML.

**Owner:** entity + seeds → **WI-352**; port methods + tools → **WI-346** / **WI-347**; design doc → **WI-345** (`metadata-content.md`).

---

## 5. `scopeUrn`, `writeScopeUrns[]`, and `mergeAction` — **LOCKED**

**Gap (original):** Whether LLM passes scope/merge on capture; default merge semantics when assigning to scope.

### Locked decision (2026-06-25)

| Concern | Decision |
| ------- | -------- |
| **LLM tool args** | **`propose_facet_assignment(metadataEntityId, facetTypeKey, payload)`** only — **no** `scopeUrn`, **no** `mergeAction` |
| **Artefact body** | `facetTypeKey`, `metadataEntityId`, `serializedPayload`, **`writeScopeUrns[]`** (all **`w` + `rw`** scopes from **`AgentContext`**) |
| **`mergeAction`** | Set by **`FacetProposalMerger`** on scope assignment — LLM never sets it |
| **Empty scope list** | Artefact persisted for chat replay; **`writeScopeUrns[]`** empty — **no** scope assignment event |

#### “Written to scope” (terminology — **amended 2026-06-25**)

**Means:** after capture, facet assignments are **materialized in metadata scopes** listed in **`writeScopeUrns[]`** (typically **chat scope**), via the **in-process `mill-events` bus** — an **architectural** producer/consumer boundary, not inline in the CAPTURE tool handler.

**Normative flow:**

```
propose_facet_assignment (LLM)
  → persist facet-proposal artefact (status: pending)
  → publish artifact.facet.persisted
  → FacetArtifactPersistedHandler: FacetProposalMerger → write metadata_entity_facet per writeScopeUrns[]
  → SSE / GET → UI (Accept | Reject)
  → Accept: POST accept → status accepted (scope unchanged)
  → Reject: POST reject → publish artifact.retracted → remove scope rows + delete artefact
```

**Not** deferred M-23-only promotion. **Not** global scope. User **Reject** rolls back scope + artefact.

See **§23** (event bus) and **WI-353**.

#### `FacetProposalMerger` (WI-352)

Invoked by **`FacetArtifactPersistedHandler`** (WI-353) on **`artifact.facet.persisted`**:

| `targetCardinality` | Default behaviour per scope |
| ------------------- | --------------------------- |
| **`SINGLE`** | One assignment, **`merge_action: SET`** — overrides prior effective value in scope |
| **`MULTIPLE`** | One assignment, **`merge_action: SET`** — adds to collection |

Aligns with read merge in **`MetadataReader`**. **`TOMBSTONE` / `CLEAR`** — operator lifecycle only.

**Owner:** merger → **WI-352**; event handlers + REST + UI → **WI-353**; capture stamping → **WI-347**.

**Cross-links:** GAPS §3c; §23; WI-233 chat scope row.

---

## 23. Facet artefact lifecycle & event bus — **LOCKED** (direction)

**Gap (original):** How to decouple chat artefact persistence from metadata scope assignment and retraction — and which transport to use at current platform maturity.

### Locked decision (2026-06-25)

Use **`:core:mill-events`** ([`general-event-bus.md`](../../../design/platform/general-event-bus.md)) with **in-process** transport (`InMemoryEventTransport` / `SpringEventTransport` via `mill-events-autoconfigure`). This is an **architectural** choice — the first production **consumer** of the event-bus foundation delivered by **WI-311**–**WI-314** — **not** operational-drift remediation (no ad-hoc callback chain exists to “fix”; greenfield wiring follows the documented bus contract).

| Concern | Decision |
| ------- | -------- |
| **Why events** | Producer/consumer boundary: `ai` persists artefacts; `metadata` applies scope writes; REST stays thin (publish only) |
| **Transport at story close** | **In-process only** — sufficient for single-JVM mill-service |
| **Not in scope** | Kafka / outbox (**P-50** backlog) — not required for facet lifecycle correctness |
| **Handlers** | `EventConsumer` DSL beans; kind-routed `artifact.retracted` |

| Step | Component |
| ---- | --------- |
| Persist artefact | `StandardPersistenceProjector` / post-save hook → **`EventPublisher.publish(artifact.facet.persisted)`** |
| Assign to scope | **`EventConsumer`** → **`FacetArtifactPersistedHandler`** → metadata facet write per **`writeScopeUrns[]`** |
| User Reject | REST → **`artifact.retracted`** (generic) |
| Scope + artefact cleanup | **`FacetArtifactRetractedHandler`** — tombstone assignments by **`sourceArtifactId`** / `correlationId` |

### Generic retraction — **LOCKED** (approach A)

| Approach | Decision |
| -------- | -------- |
| **A — Kind-routed `artifact.retracted`** | **Chosen** — one event type; handlers route on `kind` |
| **B — Per-kind events** | Rejected — proliferates catalog entries |

Payload: `{ artifactId, kind, conversationId, correlationId }`. Facet handler uses **`correlationId`** (same as `artifactId` at capture) to tombstone scope rows idempotently.

**Reject** deletes **both** chat artefact store row **and** scope assignment(s) — not artefact-only.

**Accept** = artefact `status: accepted` only (scope already written on persist).

**Owner:** **WI-353**; design sync → **WI-345** / [`ai-v3-chat-metadata-scope.md`](../../../design/agentic/ai-v3-chat-metadata-scope.md).

---

## 6. Relation facet type keys — **LOCKED**

**Gap (original):** Examples and prompts mix **`relation`**, **`relation-source`**, **`relation-target`**. Platform seeds define a **family** of relation types; unified `RelationFacet` normalization exists in data layer.

### Locked decision (2026-06-25)

The three keys are **not interchangeable aliases**. Choice is constrained by **(1)** facet type **`applicableTo`** and **(2)** the **role of the grounded target entity** in the relation being documented.

| `facetTypeKey` | Target entity | Payload shape | When to use |
| -------------- | ------------- | ------------- | ----------- |
| **`relation-source`** | **Table** that is the **source** (outbound) side of the join | Source columns on **this** table + target table/columns | User documents a FK/join **from** the grounded table **to** another table |
| **`relation-target`** | **Table** that is the **target** (inbound) side of the join | Source table/columns + target columns on **this** table | User documents a FK/join **into** the grounded table **from** another table |
| **`relation`** | **Schema** or **model** entity (not a single table endpoint) | **Both** source and target tables/columns in one payload | User documents the **whole edge** at catalogue/model level, independent of which table is “current” |

**Normative selection rule (LLM + prompts):**

1. Ground **`metadataEntityId`** (schema / table / attribute URN).
2. **`list_facet_types(category=relation, metadataEntityId=…)`** — only types whose **`applicableTo`** matches the target entity kind remain.
3. If target is a **table**, determine **role in the utterance**:
   - Grounded table is the **source** of the described join → **`relation-source`**
   - Grounded table is the **target** of the described join → **`relation-target`**
   - Utterance describes the **relation as a whole** (both ends) and target is **schema/model** → **`relation`**
4. **`get_facet_type`** → build payload from that type’s **`contentSchema`** (field names differ per key).
5. **`validate_facet_payload(…, metadataEntityId)`** — rejects wrong type for entity kind or role.

**Rejected:**

- Prefer **`relation`** whenever the catalog lists it — wrong for table-scoped outbound/inbound captures.
- Hardcoded “always `relation-source` on tables” — ignores inbound (target) cases.

**Examples (WI-345 / WI-349):**

- “`orders.customer_id` references `customers.id`” on table **`orders`** → **`relation-source`** on `urn:mill/model/table:…/orders`
- Same utterance reframed on table **`customers`** (“`customers` is referenced by `orders.customer_id`”) → **`relation-target`** on `urn:mill/model/table:…/customers`
- “Schema **skymill** links **orders** to **customers** via customer_id” on schema entity → **`relation`** on `urn:mill/model/schema:skymill`

**Owner:** normative rule → **WI-345** design doc; prompts → **WI-347** (`metadata-authoring.reasoning`); scenarios → **WI-349** (include **both** `relation-source` and `relation-target` table cases); platform **`facet-type-example`** seed may add **`relation-target`** row alongside **`relation-source`** (**WI-352**).

**Cross-link:** [`schema-facet-ai-tool-field-mapping.md`](../../../design/metadata/schema-facet-ai-tool-field-mapping.md).

---

## 7. `schema-authoring` capability — **LOCKED** (discontinue)

**Gap (original):** Historical overlap between **`schema-authoring`** and **`metadata-authoring`** — duplicate prompts, typed `capture_*`, and `schema-authoring.capture` protocol.

### Locked decision (2026-06-25)

**Discontinue the `schema-authoring` capability entirely.** Absorb authoring into **`metadata-authoring`**.

| Capability | Role |
| ---------- | ---- |
| **`schema`** | Physical catalog — **read only** (`list_schemas`, `list_tables`, `list_columns`, `list_relations`, …) |
| **`metadata`** | Metadata catalog — **read only** (facet list/get, content, scopes, `validate_facet_payload`, `list_entity_facets`) |
| **`metadata-authoring`** | Metadata capture — **`propose_facet_assignment`** only |

**Also in profiles (unchanged):** `conversation`; `sql-query`, `sql-dialect`, `value-mapping` for SQL profiles.

**Repo today:** [`schema-authoring.yaml`](../../../../ai/mill-ai/src/main/resources/capabilities/schema-authoring.yaml) has prompts + `capture_*` + `request_clarification`; profile **`schema-authoring`** loads that capability plus SQL stack ([`SchemaAuthoringAgentProfile`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/profile/SchemaAuthoringAgentProfile.kt)).

**WI-350:** delete capability manifest + provider. **WI-347:** migrate prompts to `metadata-authoring.yaml`. **`request_clarification`** → **`conversation`** capability (**locked 2026-06-25**).

---

## 8. Profile strategy — schema-authoring profile id **deprecated** — **LOCKED**

**Gap (original):** Profile id **schema-authoring** (GCP default) overlapped with capability name and mixed SQL + facet capture.

### Locked decision (2026-06-25)

- **Deprecate profile id schema-authoring.** Do not ship it in platform YAML after WI-348/WI-350.
- **Facet work** → profile **metadata-authoring** (conversation, schema, metadata, metadata-authoring).
- **SQL work** → profile **`data-analysis`** (add **`value-mapping`** there if needed for parity).
- **Read-only** → **schema-exploration**.

| Profile | Capabilities |
| ------- | ------------ |
| schema-exploration | conversation, schema, metadata |
| metadata-authoring | conversation, schema, metadata, metadata-authoring |
| `data-analysis` | `conversation`, `schema`, `metadata`, `sql-dialect`, `sql-query` (+ **`value-mapping`** if retained) |

**mill.ai.chat.default-profile:** document migration in **WI-349**; GCP schema-authoring default → operator choice (metadata-authoring or data-analysis). No silent default change unless product requests in WI-348.

**Owner:** WI-348 (YAML seeds), WI-349 (public docs + default-profile guidance).

---

## 9. Partial batch failure (multi-facet turn) — **LOCKED**

**Gap (original):** User message implies N facets; parallel capture may return mixed success/failure.

### Locked decision (2026-06-25)

**Emit all successes; never all-or-nothing.** When parallel **`propose_facet_assignment`** calls run in one iteration:

| Rule | Behaviour |
| ---- | --------- |
| **Continue on failure** | A failed capture **must not** abort siblings — process **every** parallel CAPTURE result in the batch |
| **Persist all successes** | Each successful capture → one **`facet-proposal`** artefact (fan-out via WI-351); **do not stop after the first failure** |
| **Batch envelope** | One `ProtocolFinal` `{ results[] }` contains **only successful** proposals (or per-item success flags — implementer choice; wire must fan out **all** successes) |
| **Failures** | Failed items are **omitted** from persisted artefacts for that round; model **remediates in the next tool iteration** (re-ground, fix payload, retry) — align with legacy `schema-authoring.batch` / capture-remediation |
| **User messaging** | Do not claim facets were captured until `propose_facet_assignment` succeeded for that tuple |

**Rejected:** all-or-nothing batch (one failure rolls back successes).

**Owner:** normative rules → **WI-345** § A3; agent aggregation + fan-out of partial success set → **WI-351**; prompts (`metadata-authoring.batch`, capture-remediation) → **WI-347**; e2e scenario → **WI-349**.

---

## 10. Mixed artefact turns (SQL + facets in one message) — **LOCKED**

**Gap (original):** e.g. “`orders.customer_id` must not be null **and** show me order counts” — one utterance, two artefact families.

### Locked decision (2026-06-25)

**In scope (platform goal).** Multi-artifact capture must support **heterogeneous** artefacts on **one assistant turn** — not only N × same kind (§9). Client (mill-ui) renders each artefact by **`artifactKind`**.

| Artefact kind | Capture path | Persist | Downstream |
| ------------- | ------------ | ------- | ---------- |
| **`generated-sql`** (wire often `sql`) | `validate_sql` → auto-emit **`sql-query.generated-sql`** | Chat artefact row | **Executed later** by host / UI (`chatSqlExecution` — unchanged) |
| **`facet-proposal`** | `propose_facet_assignment` → **`metadata.faceting.capture`** | Chat artefact + **`writeScopeUrns[]`**; **`artifact.facet.persisted`** → scope assign (**WI-353**) | **Accept** locks; **Reject** retracts scope + artefact (§23) |

**Normative turn behaviour:**

1. **`metadata-authoring.intent`** (or combined intent) **decomposes** mixed utterances into documentary (facet) and data-retrieval (SQL) subtasks — **both** may run in one turn when the user asks both.
2. Agent **does not terminate** after the first structured final if the other subtask is still pending — collect **all** CAPTURE / structured emissions for the turn (extends WI-351 beyond single-protocol `{ results[] }` batch).
3. **Partial failure (§9)** applies per subtask: SQL validation failure does not block facet successes (and vice versa) when independent.
4. **UI:** `artifacts[]` on the turn may contain **both** `sql` / `generated-sql` and one or more **`facet-proposal`** entries; [`MessageArtifactComposer`](../../../../ui/mill-ui/src/components/chat/artifactPreview/MessageArtifactComposer.tsx) / [`artifactGroups.ts`](../../../../ui/mill-ui/src/components/chat/artifactPreview/artifactGroups.ts) render each group.

**Profile:** requires **`sql-query`** + **`metadata-authoring`** on the same profile (resolves the old fat **`schema-authoring`** use case without reviving that id — e.g. extend **`data-analysis`** with `metadata-authoring`, or a new profile id in WI-348; document in WI-349).

**Phasing:**

| Phase | Deliverable |
| ----- | ----------- |
| **A — WI-351** | Multi **same-kind** batch (`facet-proposal` × N) — prerequisite |
| **B — stage 4 (WI-360 / WI-362)** | Multi **mixed-kind** per turn (SQL + facets); **`ArtifactRef`** on wire + per-artefact execution binding; agent multi-protocol aggregation; WI-362 e2e scenario |

**Rejected:** v1 “intent picks one primary path only” / force follow-up message.

**Transitional intent (stages 3–4):** `metadata-authoring.intent` lists cross-capability routes (`DATA_QUERY`, `EXPLORE`) so `data-analysis` can decompose mixed turns without a profile-level router yet. MR !412 review (2026-06-26) — merge as-is; document in design.

**Target intent model (stage 5 — WI-363):** each capability declares **only** capability-scoped intents; profiles compose non-overlapping union (`sql-query.intent`, trimmed `metadata-authoring.intent`, profile `data-analysis` composition). Scenario packs updated after WI-362 baseline.

**Owner:** design → **WI-345**; heterogeneous agent + persist/SSE → **WI-351** (phase B) + **WI-347** intent (transitional); intent refactor → **WI-363**; UI → **WI-351** / mill-ui; e2e → **WI-349** / **WI-362**.

---

## 11. `FacetProposalWire` and historical replay — **LOCKED**

**Gap (original):** WI-350 removes `schema.authoring.capture` / `capture_description` for **new** captures. [`FacetProposalWire`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/core/artifact/FacetProposalWire.kt) still normalizes legacy shapes for GET replay.

### Locked decision (2026-06-25)

| Choice | Decision |
| ------ | -------- |
| Backward-compat investment | **None** — accept **breaking change** for historical replay |
| `FacetProposalWire` | **Leave as-is** in codebase; **no** removal, **no** migration, **no** sunset project |
| Old chats (`schema-authoring.capture`, `captureType`) | Best-effort replay via existing wire only; **incompatibility with new artefact fields** (`writeScopeUrns[]`, `status`, …) is **ignored** |
| New captures | **`metadata.faceting.capture`** only (**WI-350**) |

**Rejected:** indefinite normalization maintenance; migration window; WI-349 replay-compat work.

**Owner:** **WI-350** (new path only); **no** dedicated WI for legacy replay.

---

## 12. `HarnessMetadataReadPort` timing — **LOCKED**

**Gap (original):** Story requires harness catalog beyond **descriptive** (relation + DQ) for scenarios. Expansion was listed in **WI-349**; **WI-347** unit tests need it earlier.

### Locked decision (2026-06-25)

| WI | Role |
| -- | ---- |
| **WI-346** | **Expand `HarnessMetadataReadPort`** — minimal manifests for `descriptive`, `relation-source`, `relation-target`, `dq-null-check`, `dq-predicate`; stub new port methods (`listContent`, `listFacetCategories`, `validateFacetPayload(…, metadataEntityId?)`) |
| **WI-347** | Tool unit tests — use expanded harness or inline mocks; **no** harness expansion work |
| **WI-349** | Scenario packs + e2e only — **consume** WI-346 harness; align type keys with **WI-352** seeds |

**Rationale (execution):** WI-346 already updates `EmptyMetadataReadPort`, harness, and `validateFacetPayloadInternal` when the port contract changes ([§2](GAPS.md)); stage **3** lands before **WI-347** (stage 5) and **WI-349** (stage 8). Keeps harness as a lightweight test double in `mill-ai-test` — not platform seed import.

**Rejected:** defer harness expansion to WI-349 (too late for WI-347 tests); load WI-352 seeds into harness (unnecessary JPA coupling).

**Owner:** [`WI-346-metadata-read-port-adapter.md`](WI-346-metadata-read-port-adapter.md) § harness; WI-349 references only.

## 13. Design doc ownership and paths — **LOCKED**

**Gap (original):** WI-345 did not name a single canonical design file (`metadata-authoring-catalog.md` vs extend v3).

### Locked decision (2026-06-25)

**Normative architecture lives under [`docs/design/`](../../../design/)** — patch **existing** docs by concern; **no** new `metadata-authoring-catalog.md`. **Canonical authoring hub:** [`metadata-facet-catalog-v3.md`](../../../design/agentic/metadata-facet-catalog-v3.md) (extend in place; keep filename for link stability).

| Deliverable | Design doc (under `docs/design/`) | Authoring WI | Notes |
| ----------- | --------------------------------- | ------------ | ----- |
| Tool matrix, authoring loop, capability split, prompts, relation keys, profiles | **[`agentic/metadata-facet-catalog-v3.md`](../../../design/agentic/metadata-facet-catalog-v3.md)** | **WI-345** — outline + TOC at stage 0; **WI-349** — full rewrite + drift check | **Primary** entry point; cross-link all others |
| **`MetadataContent`** entity, seeds, merger interface | **[`metadata/metadata-content.md`](../../../design/metadata/metadata-content.md)** (**create**) | **WI-345** — skeleton; **WI-352** — normative entity/DDL | Domain layer; link `metadata-urn-platform.md`, `dq-rule-facet-types.md` |
| Chat scope, `writeScopeUrns[]`, Accept/Reject, event bus | **[`agentic/ai-v3-chat-metadata-scope.md`](../../../design/agentic/ai-v3-chat-metadata-scope.md)** | **WI-353** — rewrite lifecycle; **WI-349** — verify | Supersedes promotion-only language |
| Batch `ProtocolFinal` / multi-artifact fan-out | **[`agentic/artifact-foundation.md`](../../../design/agentic/artifact-foundation.md)** | **WI-351** — new § batch envelope | Cross-link [`chat-artefact-architecture.md`](../ai/chat-artefact-architecture.md) |
| Event bus (`artifact.facet.persisted`, `artifact.retracted`) | **[`platform/general-event-bus.md`](../../../design/platform/general-event-bus.md)** — event catalog note only | **WI-353** | Handlers documented in `ai-v3-chat-metadata-scope.md` + WI-353 |
| YAML `AgentProfile` seed format | **§ in `metadata-facet-catalog-v3.md`** (or link from [`v3-capability-manifest.md`](../../../design/agentic/v3-capability-manifest.md)) | **WI-348** | No separate profile design file |
| MCP tool inventory | **[`agentic/v3-mcp-capability-exposure.md`](../../../design/agentic/v3-mcp-capability-exposure.md)** | **WI-349** — row updates | |
| Operator / UI guide | **`docs/public/src/`** (not `docs/design/`) | **WI-349** | |

**WI markdown rule:** [`WI-345`](WI-345-metadata-authoring-design-contract.md) and sibling WIs hold **acceptance checklists** and **links** — not a second normative copy of the design.

**Rejected:** new `metadata-authoring-catalog.md`; deferring all design writes to WI-349 only; keeping normative prose only in `docs/workitems/`.

**Owner:** stage **0** creates/updates **outlines** in design docs; each implementation WI patches its doc(s); **WI-349** integration pass before story close.

---

## 14. `SchemaExplorationAgent` vs `LangChain4jAgent` — **LOCKED**

**Gap (original):** WI-351 mentioned updating **`SchemaExplorationAgent`** “if applicable”. Production chat uses **`LangChain4jAgent`** only ([`LangChain4jChatRuntime`](../../../../ai/mill-ai-autoconfigure/src/main/kotlin/io/qpointz/mill/ai/autoconfigure/chat/LangChain4jChatRuntime.kt)). **`SchemaExplorationAgent`** has **no** callers outside its own file.

### Locked decision (2026-06-25)

| Item | Decision |
| ---- | -------- |
| Production runtime | **`LangChain4jAgent`** only — all profiles (`schema-exploration`, `metadata-authoring`, …) |
| **`SchemaExplorationAgent.kt`** | **Delete** — dead duplicate agent loop (~400 lines) |
| **`SchemaExplorationAgentProfile`** | **Keep** — profile id `schema-exploration` remains (YAML in **WI-348**); not the same as the agent class |
| **`SchemaAuthoringAgentProfile`** | Remove in **WI-350** with `schema-authoring` capability (separate from this gap) |
| WI-351 scope | **`LangChain4jAgent`** only — no dual maintenance |

**Related cleanup (**WI-351**):** delete [`SchemaExplorationAgent.kt`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/runtime/langchain4j/SchemaExplorationAgent.kt); grep confirms no tests import the class. Update stale design references (`v3-authoring-protocol.md`, developer manual) to **`LangChain4jAgent`** in **WI-349** §13 pass or a short note in **WI-351**.

**Rejected:** keeping `SchemaExplorationAgent` for future layer-1/2 protocol (`v3-authoring-protocol.md`); patching dead agent in WI-350.

**Owner:** delete file → **WI-351**; design doc stale refs → **WI-349** (or **WI-351** if touched).

---

## 15. Active artifact pointers vs multi-facet — **LOCKED**

**Gap (original):** Descriptor uses **`last-metadata-facet-proposal`** (singular). Multi-facet turns produce N records; pointer store **upsert** keeps **last** only. GET replay must hydrate **all** artefacts per turn.

### Locked decision (2026-06-25)

**Multi-artifact is first-class in WI-351** — not “turn `artifacts[]` only, pointers optional”. Persist, pointers, SSE, and GET must agree.

| Layer | Contract |
| ----- | -------- |
| **Per-turn authority** | `ConversationTurn.artifactIds[]` lists **every** artefact for that turn in **persist order** |
| **GET hydration** | `UnifiedChatService.mapTurnResponses` → `TurnResponse.artifacts[]` with **N** wire entries; stable order matches persist / `artifactIds` |
| **Batch persist** | After `{ results[] }` fan-out: **N** `ArtifactRecord` rows (shared `turnId`) + **one** `attachArtifacts(turnId, allIds)` |
| **Pointer rework** | Singular **`last-*`** keys remain for **single-artifact** protocols (e.g. SQL). **Facet batch:** replace `last-metadata-facet-proposal` with plural **`metadata-facet-proposals`** — **list pointer** (ordered artifact IDs), **append all N** per multi-capture turn (no last-wins) |
| **Manifest** | `metadata-authoring.yaml` descriptor: `pointerKeys: [metadata-facet-proposals]` + **`pointerCardinality: multiple`** (new manifest field on structured-final / artifact descriptor) |
| **Store API** | Extend [`ActiveArtifactPointerStore`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/persistence/ActiveArtifactPointerStore.kt): **`appendAll(conversationId, pointerKey, artifactIds)`** + **`findAll(conversationId, pointerKey): List<ActiveArtifactPointer>`** (ordered); JPA adapter + migration for **multiple rows per `(chatId, pointerKey)`** or equivalent list entity |
| **Consumers** | Tools / follow-up turns that need “recent facet proposals” read **list pointer** or **turn `artifactIds`** — not singleton `find()` last-wins |

**SSE / `item.completed`:** **§16** — same WI-351 tranche (breaking SSE wire).

**Rejected:** document-only “use turn not pointer”; defer pointer rework post-WI-351; keep `last-metadata-facet-proposal` with silent overwrite.

**Owner:** **WI-351** (pointer schema + projector + GET tests L3/L5); design → [`artifact-foundation.md`](../../../design/agentic/artifact-foundation.md) § multi-artifact + [`chat-artefact-architecture.md`](../../../design/ai/chat-artefact-architecture.md).

---

## 16. SSE `item.completed` hint with N structured parts — **LOCKED**

**Gap (original):** [`AgentEventToSseMapper`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/sse/AgentEventToSseMapper.kt) keeps one `structuredCompletionPartType` (last wins). Structured finals use **`mode: replace`**, dropping earlier parts. Multi-artifact must be **fully** supported on the live SSE path.

### Locked decision (2026-06-25)

**Breaking changes accepted** — protocol + SSE wire (no backward-compat shim for multi structured parts on one turn).

| Concern | Contract |
| ------- | -------- |
| **Fan-out** | Batch / multi-lane finals → **N** `item.part.updated` events per turn (one per wire artefact) |
| **`item.part.updated` mode** | **1st** structured part on turn: `mode: replace` (optional shell). **2..N:** **`mode: append`** — **breaking** vs today’s always-replace |
| **`item.completed` hint** | Track structured parts emitted this turn. **N = 1:** unchanged (`presentation` + `partType`). **N > 1:** `presentation: "structured"`, **`partType: "multi"`** (new sentinel), optional **`structuredPartCount: N`** + **`partTypes: string[]`** on [`ChatSseEvent.ItemCompleted`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/sse/ChatSseEvent.kt) |
| **Mapper state** | Replace single `structuredCompletionPartType` with **ordered list** / count of structured parts for the turn |
| **mill-ui live path** | Each non-text `item.part.updated` → `onNonTextPartUpdated` → **`APPEND_MESSAGE_ARTIFACT`** (already). **`item.completed`:** `deriveAssistantReplyView` prefers **accumulated `artifacts[]`** over hint; must work when **N ≥ 2** facet cards visible before complete |
| **UI verification** | **Vitest (required in WI-351):** (1) `chatService.test.ts` — N structured parts streamed + `item.completed` with `partType: "multi"`; (2) `ChatContext` / reducer — N `APPEND_MESSAGE_ARTIFACT` → message has N artefacts; (3) `MessageArtifactComposer` / `artifactGroups` — N cards; (4) `assistantReplyView` — plural section title when ≥2 `facet-proposal` |
| **Design** | [`ai-v3-chat-transport-extensions.md`](../../../design/agentic/ai-v3-chat-transport-extensions.md) + [`chat-artefact-architecture.md`](../../../design/ai/chat-artefact-architecture.md) — document breaking SSE fields |

**Rejected:** rely on last-wins `partType` hint; defer `item.completed` multi shape; SSE-only fix without UI tests.

**Owner:** **WI-351** (mapper + `ChatSseEvent` + mill-ui Vitest); cross-link **§15** (GET hydration).

---

## 17. `list_facet_types` payload size / truncation — **LOCKED** (see §3b)

**Resolved by §3b:** `list_facet_types` returns **summary rows only**; full **`contentSchema`** via **`get_facet_type`**. WI-347 unit tests: list output omits nested schema; get returns it for a large type (catalog with 15+ types safe).

---

## 18. MCP tool surface — **LOCKED**

**Gap (original):** STORY says document `mcp.enabled` guidance; no WI owned MCP inventory update when profiles/capabilities change. Design doc §15 suggested **`mcp.enabled: false`** for some CAPTURE tools.

### Locked decision (2026-06-25)

**MCP is profile-driven — minimal runtime changes in this story.**

Exposure already flows through [`CapabilityMcpCatalog`](../../../../ai/mill-ai-mcp-core/src/main/kotlin/io/qpointz/mill/ai/mcp/CapabilityMcpCatalog.kt): manifest YAML → capability id → optional **`mill.ai.mcp.profile`** filter (`AgentProfile.capabilityIds`) → optional server allowlist (`mill.ai.mcp.capabilities`). **No new MCP transport, catalog builder, or per-tool registration code** beyond capability manifests and profile YAML.

| Concern | Contract |
| ------- | -------- |
| **New tools** | **All** story tools are **MCP-enabled by default** — omit `mcp:` block or `mcp.enabled: true`. **Do not** set `mcp.enabled: false` on new `metadata` / `metadata-authoring` tools (`list_facet_categories`, `get_facet_type`, `list_metadata_scopes`, reshaped `list_facet_types`, `validate_facet_payload`, `propose_facet_assignment`, …) |
| **How tools appear** | Declare in capability YAML ([`metadata.yaml`](../../../../ai/mill-ai/src/main/resources/capabilities/metadata.yaml), [`metadata-authoring.yaml`](../../../../ai/mill-ai/src/main/resources/capabilities/metadata-authoring.yaml)); MCP names `{capabilityId}.{toolName}` automatically when capability passes filters ([`v3-mcp-capability-exposure.md`](../../../design/agentic/v3-mcp-capability-exposure.md) §5) |
| **Profile binding** | **`mill.ai.mcp.profile`** selects which capability ids are exposed. **`metadata-authoring`** profile must include **`metadata`** + **`metadata-authoring`** ([**WI-348**](WI-348-agent-profiles-metadata-authoring.md)) so MCP clients get catalog + capture tools together |
| **Chat vs MCP** | Same profiles runtime-wide (`mill.ai` scope). MCP `list_metadata_scopes` uses auth/global scopes (no chat row) per §3c — tool behaviour only, not MCP plumbing |
| **Doc inventory** | **[`v3-mcp-capability-exposure.md`](../../../design/agentic/v3-mcp-capability-exposure.md)** §15 — add new tool rows, remove **`schema-authoring`** rows, refresh totals — **WI-349** |
| **Tests** | **WI-349** (optional light): `CapabilityMcpCatalog` with `metadata-authoring` profile lists expected `metadata.*` + `metadata-authoring.*` tools. **WI-350:** no `schema-authoring` tools in catalog |

**Rejected:** dedicated MCP implementation WI; per-tool MCP opt-out for story tools; runtime MCP changes beyond manifests + profiles.

**Owners:** **WI-347** (tool YAML), **WI-348** (profile capability ids), **WI-349** (inventory doc + optional catalog test), **WI-350** (remove legacy capability from MCP).

---

## 19. Verify commands vs UI work — **RESOLVED** (no action)

**What this was (planning hygiene only):** During story review, someone asked whether `./gradlew :ui:mill-ui:test` in the STORY **Verify** block meant UI work belonged only to WI-351, or whether running mill-ui tests “after WI-351” was ambiguous when UI also changes in WI-353.

**Resolution — already in [`STORY.md`](STORY.md):**

| Question | Answer |
| -------- | ------ |
| Who implements UI? | **WI-355** (stage 2) — multi-card SSE/live + Vitest (§16). **WI-360** (stage 4) — Accept/Reject on `FacetCondensedPreview`. |
| When run `:ui:mill-ui:test` per stage? | Each WI runs **its** UI tests when that WI lands (WI-355 L6 Vitest; WI-360 button tests). |
| When run the **full** verify block? | **Once at story close** — stage **8 / WI-349** before MR ([`STORY.md`](STORY.md) § Verify, [`COLDSTART.md`](COLDSTART.md) § Verify commands). |

**No product decision required.** Do not treat §19 as an open gap.

---

## 20. Story checklist vs sequence table — **RESOLVED** (no action)

STORY Work Items checklist, sequence table, and staged delivery table are aligned (4 stages; WI-355 isolated in stage 2). No further action.

---

## 21. Interim N × `ProtocolFinal` vs batch envelope — **LOCKED**

**Locked (recommended):** batch **`{ results: [] }`** is **mandatory at story close**. N × scalar finals acceptable only as **WI-351** interim escape hatch; remove before WI-349 / story closure.

---

## 22. `propose_facet_assignments` batch tool — **LOCKED** (out of story)

**Gap (original):** Whether to add a plural **`propose_facet_assignments`** tool if live-LLM parallel calls prove unreliable (A-94) — escalation criteria were undefined.

### Locked decision (2026-06-25)

**Stay in the sweet spot — no separate batch capture tool in this story.**

| Layer | Normative approach |
| ----- | ------------------ |
| **LLM surface** | **N parallel** calls to singular **`propose_facet_assignment`** (same agent iteration when the model supports it) |
| **Prompting** | **`metadata-authoring.batch`** (WI-347) instructs parallel multi-facet capture + partial-failure remediation (§9) |
| **Runtime** | **WI-351** aggregates parallel capture successes into one batch **`ProtocolFinal`** `{ results: [] }`; fan-out to N artefacts / SSE / GET (§15–§16) |
| **Partial failure** | Emit all successes; remediate failures in the **next** tool round (§9) — not a batch-tool concern |

**Rejected for this story:**

- New **`propose_facet_assignments`** (plural) tool registration
- A-94 / live-LLM “unreliable parallel calls” escalation criteria (too hard to define; not a story gate)
- All-or-nothing batch semantics

**Future (optional backlog only):** if operators later need a plural tool for a specific model family, track as a **separate** follow-up — **not** a metadata-authoring-profiles deliverable and **no** trigger criteria in this story.

**Owners:** **WI-347** (singular tool + batch prompt); **WI-351** (protocol batch envelope); **WI-349** (multi-facet scenario with parallel singular calls).

---

## Summary — decisions for product / tech review

| # | Topic | Choices |
|---|--------|---------|
| 1 | WI-351 test vehicle | **Locked: D** — mock LLM (2× `propose_facet_assignment`) + L1–L6 layer tests |
| 2 | `applicableTo` validation | **Locked:** extend `MetadataReadPort.validateFacetPayload(..., metadataEntityId?)` — WI-346 adapter, WI-347 tools |
| 3–4 | P1 tools + catalog content | §3b + §3c + **§4 locked** (`MetadataContent`, WI-352) |
| 5 | Scopes / lifecycle | **Locked:** `writeScopeUrns[]`; persist event → scope assign; Accept/Reject (§5, §23); **in-process `mill-events`** = architectural boundary |
| 23 | Event bus retract | **Locked:** `artifact.retracted` + kind handlers (**WI-360**); not operational-drift remediation |
| 6 | Relation facet type keys | **Locked:** `applicableTo` + table role → `relation-source` \| `relation-target` \| `relation` (§6) |
| 7 | `schema-authoring` capability | **Locked:** discontinue; roles → `schema` / `metadata` / `metadata-authoring` (§7) |
| 8 | Profile `schema-authoring` | **Locked:** deprecate profile id; use `metadata-authoring` / `data-analysis` (§8) |
| 9 | Partial batch failure | **Locked:** emit all successes; continue on failure; remediate next iteration (§9) |
| 10 | SQL + facets same turn | **Locked:** heterogeneous multi-artifact; facets → scope on persist; Accept/Reject (§10, §23) |
| 11 | Legacy `FacetProposalWire` replay | **Locked:** leave wire as-is; ignore replay incompatibility (breaking change OK) (§11) |
| 12 | Harness catalog breadth | **Locked:** expand in **WI-346**; WI-349 scenario packs only (§12) |
| 13 | Design doc paths | **Locked:** hub = `metadata-facet-catalog-v3.md`; patch `docs/design/` per WI (§13) |
| 14 | Agent runtime | **Locked:** `LangChain4jAgent` only; delete `SchemaExplorationAgent.kt` (§14) |
| 15 | Multi-artifact pointers + GET | **Locked:** list pointers + full turn hydration in **WI-351** (§15) |
| 16 | SSE multi structured parts | **Locked:** append mode + `partType: multi` hint; UI Vitest (**WI-351**, §16) |
| 18 | MCP tool surface | **Locked:** profile-driven; all new tools MCP-enabled; inventory **WI-349** (§18) |
| 21 | Batch envelope mandatory at story close | **Locked: yes** (§21) |
| 22 | Plural `propose_facet_assignments` tool | **Locked: no** — parallel singular + batch protocol sweet spot (§22) |

---

## Related

- [`STORY.md`](STORY.md) — architectural decisions, WI order  
- [`WI-345-metadata-authoring-design-contract.md`](WI-345-metadata-authoring-design-contract.md)  
- [`WI-351-multi-artifact-protocol-runtime.md`](WI-351-multi-artifact-protocol-runtime.md)  
- [`WI-347-metadata-authoring-capability.md`](WI-347-metadata-authoring-capability.md)
