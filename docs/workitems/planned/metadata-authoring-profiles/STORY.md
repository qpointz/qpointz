# Metadata authoring capability and agent profiles

**Status:** `planned`  
**Milestone:** **0.8.0** (or next open milestone at implementation time)  
**Branch:** `feat/meta-capability-improve`

## Goal

**Primary:** Make **`metadata-authoring`** **catalog-generic** — the LLM must recognize and capture
**any facet type** registered in the system (not only **`descriptive`**), using reviewed tools:

- **`list_facet_types`** — catalog **summary** for reasoning (shortlist `facetTypeKey`) — [`GAPS.md`](GAPS.md) §3b
- **`get_facet_type`** — full manifest + **`contentSchema`** for payload generation
- **`validate_facet_payload(facetType, payload)`** — validates proposal against facet type schema (dry-run)
- **`propose_facet_assignment(target, facetType, payload)`** — capture only when schema (+ applicability) valid
- **No `capture_<specific facet>`** tools

**Secondary:** YAML-defined **agent profiles** (including **`metadata-authoring`**), real
**`MetadataReadPort`** on mill-service, and operator seed config.

Today, facet capture is effectively **descriptive-only** in tests and practice: parallel
**`capture_description`** in [`schema-authoring.yaml`](../../../../ai/mill-ai/src/main/resources/capabilities/schema-authoring.yaml),
harness ports with a single facet type, and **`EmptyMetadataReadPort`** on mill-service so the catalog
tools return nothing in production.

## Architectural decisions (locked at WI-345)

| Decision | Choice |
|----------|--------|
| **Authoring model** | **`list_facet_types`** (reasoning) → **`get_facet_type`** (generation / `contentSchema`) → **`validate_facet_payload`** → **`propose_facet_assignment`** |
| **Proposal shape** | **`propose_facet_assignment(target, facetType, payload)`** — `metadataEntityId` (target URN), `facetTypeKey`, `payload` object **generated from that facet type's schema** |
| **Validation** | **`validate_facet_payload`** validates **`(facetType, payload)`** against **`contentSchema`**; **`metadata`** capability owns **`applicableTo`** / target-entity checks (extend QUERY tools if needed); **`propose_facet_assignment`** reuses that logic — no duplicate rules in **`metadata-authoring`** |
| **No per-facet capture tools** | **No `capture_<facet>` tools anywhere** — only **`propose_facet_assignment`** |
| **Profile definition** | Multi-document YAML, **`kind: AgentProfile`** (`id`, `capabilities`, optional `description`) |
| **Profile registry** | Resource-backed **`ProfileRegistry`** from **`mill.ai.profiles.seed.resources`** |
| **Profile split** | **`metadata-authoring`**: `conversation`, `schema`, `metadata`, `metadata-authoring` — no SQL/schema-authoring capture tools |
| **`schema-authoring`** | Keeps `schema-authoring` capability for **clarification** / non-facet helpers if any remain; facet writes go through **`metadata-authoring`** |
| **`MetadataReadPort`** | In-process adapter in **`mill-ai-data`**; must return **full platform facet-type catalog** (bootstrap + DQ seeds, etc.) |
| **`list_facet_types` output** | **Summary only:** `facetTypeKey`, `category`, `applicableTo`, `description`, optional `title` / `targetCardinality` / `source` — **no** full `contentSchema` ([`GAPS.md`](GAPS.md) §3b) |
| **`get_facet_type` output** | Full manifest including **`contentSchema`** for one `facetTypeKey` |
| **Capture scope** | Proposal-only — no metadata service persistence; **must** persist as **chat artefact** (SSE + GET replay) |
| **Chat artefact (facets)** | **One kind for all facet types** — wire `partType: facet-proposal` / descriptor `inferred-facet` / `persistKind: metadata.faceting.capture`; **`facetTypeKey`** inside payload distinguishes type (no per-facet artefact kinds) |
| **Intent → facet reasoning** | When **`metadata-authoring`** capability is active, **mandatory** prompt assets classify utterances and map **data-documentation** statements to catalog facet types (not SQL, not idle chat). See **Prompt enforcement** below. |
| **Multi-facet per turn** | One user message may imply **multiple** facet assignments (mixed types/entities). LLM must emit **all** recognized proposals in the same turn; runtime persists **N** `facet-proposal` artefacts; UI renders **N** condensed cards. See **Multi-facet batch** below. |
| **Batch protocol final** | Prefer **one** `ProtocolFinal` with **`results[]`** on `metadata.faceting.capture`; fan-out to N flat artefacts (not one array card). Interim: N × `ProtocolFinal`. See § *Protocol: batch ProtocolFinal*. |
| **`kind` (YAML)** | Hook for future unified Mill seeding — **out of scope** |
| **MCP** | Document `mcp.enabled` guidance; no repo default changes |

## Gap (why this story exists)

| Gap | Evidence |
|-----|----------|
| **Descriptive-only capture path** | `capture_description` + tests/harness only exercise `descriptive` |
| **Dual capture mechanisms** | `capture_description`, `capture_relation`, and any other `capture_<specific facet>` vs `propose_facet_assignment` |
| **Catalog tools unused in prod** | `EmptyMetadataReadPort`; `list_facet_types` returns `[]` on mill-service |
| **Split artefact paths** | `schema.authoring.capture` vs `metadata.faceting.capture` for facets — must converge on **one** chat artefact kind |
| **Profiles are code-only** | Kotlin `*AgentProfile.kt` objects |
| **Weak intent → facet routing** | `metadata-authoring` has a one-line `metadata.faceting.request`; no utterance→facet cookbook. Model treats “column X not null” as SQL or chat. `schema-authoring.intent` covers only description/relation, not DQ or catalog-generic facets. |
| **Single-capture runtime** | [`LangChain4jAgent`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/runtime/langchain4j/LangChain4jAgent.kt) keeps one `captureBinding` and terminates after first CAPTURE batch — parallel `propose_facet_assignment` calls lose all but the last emission. |
| **Batch prompt only on schema-authoring** | `schema-authoring.batch` targets removed `capture_*` tools; no `metadata-authoring.batch` for `propose_facet_assignment` yet. |

## Prerequisites

- [`ai-facet-catalog-inference`](../../completed/20260428-ai-facet-catalog-inference/STORY.md)
- [`dqm-metadata-facets`](../../completed/20260624-dqm-metadata-facets/STORY.md) — DQ facet types as capture test targets
- Platform facet seeds: [`platform-bootstrap.yaml`](../../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-bootstrap.yaml), DQ L1/L2 YAML
- [`ai-chat-facet-display`](../../completed/20260619-ai-chat-facet-display/STORY.md) — unified **`facet-proposal`** shell for **all** facet types (unchanged presentation; payload carries `facetTypeKey`)

## Module touchpoints

| Area | Change |
|------|--------|
| `ai/mill-ai` | **WI-351** batch protocol + agent fan-out; then **`metadata` / `metadata-authoring`** tools; **`schema-authoring`** cleanup |
| `ai/mill-ai-data` | `MetadataReadPort` → full facet-type catalog |
| `ai/mill-ai-autoconfigure` | Port + `mill.ai.profiles.seed` |
| `ai/mill-ai-test` | Multi-facet-type scenario packs (not descriptive-only) |
| `ui/mill-ui` | **WI-351** multi `facet-proposal` cards per turn; plural section label |
| `docs/design/agentic/` | Catalog-generic authoring + tool matrix |

## Out of scope

- Metadata service **write** / approval workflow
- **M-32** admin UI
- **A-60** three-layer authoring protocol
- Unified Mill seed runner (all `kind` types)
- `BACKLOG.md` promotion until requested

## Work item order

| Seq | WI | Rationale |
|-----|-----|-----------|
| 1 | WI-345 | Tool matrix, catalog authoring loop, profile YAML spec, batch protocol **design** |
| 2 | **WI-351** | **Multi-artifact platform** — batch `ProtocolFinal`, agent aggregation, persist/SSE fan-out, UI N-cards — **prerequisite for facet rework** |
| 3 | WI-348 | YAML profiles + registry (can follow WI-351; independent of catalog tools) |
| 4 | WI-346 | **`MetadataReadPort`** — required before catalog tools work end-to-end |
| 5 | WI-347 | **Facet capabilities** — tools, prompts, validators (depends on **WI-351** + WI-346) |
| 6 | WI-350 | Remove **`capture_*`** tools; migrate to batch `propose_facet_assignment` path |
| 7 | WI-349 | End-to-end scenarios, Skymill IT, public docs |

## Work Items

- [ ] WI-345 — Design contract: catalog authoring + YAML profiles (`WI-345-metadata-authoring-design-contract.md`)
- [ ] WI-351 — Multi-artifact protocol, runtime fan-out, chat UI (`WI-351-multi-artifact-protocol-runtime.md`) — **before WI-347**
- [ ] WI-346 — `MetadataReadPort` in-process adapter (`WI-346-metadata-read-port-adapter.md`)
- [ ] WI-347 — Catalog-generic facet tools (`WI-347-metadata-authoring-capability.md`)
- [ ] WI-348 — YAML agent profiles and seed config (`WI-348-agent-profiles-metadata-authoring.md`)
- [ ] WI-349 — Tests, scenarios, documentation (`WI-349-metadata-authoring-tests-docs.md`)
- [ ] WI-350 — Remove per-facet capture tools (`capture_*`) (`WI-350-schema-authoring-description-tool-cleanup.md`)

## Verify (full story — before MR)

```bash
./gradlew :ai:mill-ai:test --tests "*Metadata*"
./gradlew :ai:mill-ai:test --tests "*SchemaAuthoring*"
./gradlew :ai:mill-ai:test --tests "*Profile*"
./gradlew :ai:mill-ai-data:test --tests "*Metadata*"
./gradlew :ai:mill-ai-service:testIT --tests "*metadata*"
./gradlew :ai:mill-ai-test:test --tests "*facet*"
```

## Prompt enforcement (planning)

**Problem:** Users often **document** how data should behave (“table A column X must be not null”, “orders links to customers on customer_id”, “status is the lifecycle state”) — that is **metadata facet authoring**, not SQL generation and not generic conversation. Observed LLM behaviour: skips facet tools, answers in prose, or composes `WHERE … IS NOT NULL` when `sql-query` is also loaded.

**Requirement:** When the active profile includes **`metadata-authoring`**, system prompts **must** instruct the planner to:

1. **Classify** each turn (facet authoring vs exploration vs query vs clarification).
2. **Attempt** the catalog authoring loop when the user states or implies assignable metadata.
3. **Map** utterance patterns to **`facetTypeKey`** candidates before building `payload` from `contentSchema`.
4. **Not** treat normative data statements as SQL unless the user clearly asks to **retrieve** rows.

### Prompt assets (normative names — WI-347)

| Prompt id | Capability | Role |
|-----------|------------|------|
| **`metadata-authoring.intent`** | `metadata-authoring` | Turn classifier when capture tools are available. Tasks: `AUTHOR_FACET`, `EXPLORE_SCHEMA`, `QUERY_DATA` (only if `sql-query` also active), `ASK_CLARIFICATION`, `CONVERSATION`. **Priority:** normative / documentary phrasing → `AUTHOR_FACET` even when SQL capability is present. |
| **`metadata-authoring.reasoning`** | `metadata-authoring` | Utterance → facet reasoning: ground entity → **`list_metadata_scopes`** (pick `scopeUrn` when non-empty) → **`list_facet_types`** (shortlist `facetTypeKey`) → **`get_facet_type`** → draft payload → validate → capture. |
| **`metadata.faceting.system`** | `metadata` | Grounding order; **profile-neutral** — do not imply authoring loop on `data-analysis` / exploration-only paths ([`GAPS.md`](GAPS.md) §3b). |
| **`metadata.faceting.request`** | `metadata-authoring` | **Generation** step: build `payload` from **`get_facet_type`** `contentSchema`; then validate → `propose_facet_assignment`. |

Migrate the strongest rules from **`schema-authoring.intent`** (AUTHOR_METADATA, normative phrases, query-refinement disambiguation) into **`metadata-authoring.intent`**, then narrow **`schema-authoring`** prompts to clarification / legacy helpers ([WI-350](WI-350-schema-authoring-description-tool-cleanup.md)).

### Disambiguation rules (normative examples for design doc)

| User utterance | Wrong path | Correct path (`metadata-authoring` active) |
|----------------|------------|---------------------------------------------|
| “table **orders**, column **customer_id** must be **not null**” | `validate_sql` / `WHERE customer_id IS NOT NULL` | Ground column → `facetTypeKey: dq-null-check` on attribute URN → `propose_facet_assignment` |
| “**customer_id** is required on orders” | Conversational ack only | Same — `dq-null-check` (or `dq-empty-value-check` if whitespace rule) |
| “add a description: orders table holds all sales” | — | `descriptive` on table entity |
| “orders.**customer_id** references customers.**id**” | — | relation facet family on table(s) |
| “**show me** rows where customer_id is null” | — | `QUERY_DATA` (SQL) — **retrieval**, not documentation |
| “what tables are in skymill?” | — | `EXPLORE_SCHEMA` |

**Signal phrases for `AUTHOR_FACET`:** must, should, required, not null, unique, only values, means, represents, documents, rule, constraint, links to, references, foreign key, allowed values, pattern, format, tag as, treat as, business definition.

**Anti-signals (prefer SQL when `sql-query` active):** show me, list rows, how many, count, query, run, filter, where, last query, same query but, export.

### Profile behaviour

| Profile | Intent enforcement |
|---------|-------------------|
| **`metadata-authoring`** | Full enforcement; no `sql-query` — facet vs explore vs chat only. |
| **`schema-authoring`** | Same **`metadata-authoring.*`** prompts; **`metadata-authoring.intent`** must **override** `sql-query.system` for documentary utterances (explicit priority rule in both prompts). |
| **`data-analysis`** | No `metadata-authoring` — SQL path only; optional read-only `metadata` tools. |

### Verification (WI-349)

Scenario packs with **negative assertions**: utterance must **not** call `validate_sql`; must call `validate_facet_payload` then `propose_facet_assignment` with expected `facetTypeKey`. Include at least: **dq-null-check**, **descriptive**, **relation** from natural-language input (no “capture a facet” meta-instructions).

## Multi-facet batch (planning)

**Requirement:** A single user question may describe **several** metadata assets at once — e.g.
“**orders.customer_id** must not be null, **orders** table holds all sales, and **orders.customer_id**
references **customers.id**.” When **`metadata-authoring`** is active, the LLM must:

1. **Decompose** the utterance into independent facet intents (one per target + facet type + payload).
2. **Ground** all entities before capture (schema tools → URNs).
3. **Validate** each `(facetType, payload [, target])` (shared validator).
4. **Emit all** successful captures in **one model round** — parallel **`propose_facet_assignment`** tool calls (same pattern as legacy `schema-authoring.batch` / `capture_description` batching).

**Do not** stop after the first facet when others are clearly implied.

### Runtime (**WI-351** — implement before WI-347)

| Layer | Today | Target |
|-------|--------|--------|
| **Agent loop** | One `captureBinding`; one `handleCaptureSuccess`; early `return` | Collect all successful CAPTUREs → **one batch `ProtocolFinal`** `{ results: [...] }` (fan-out to N artefacts); interim: N × `ProtocolFinal` |
| **Persistence** | `turnId` already links multiple [`ArtifactRecord`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/persistence/ArtifactStore.kt) rows | No schema change — multiple `metadata.faceting.capture` rows per turn |
| **Pointer keys** | `last-metadata-facet-proposal` (singular) | Document: pointers are “latest” hints only; **GET replay uses `artifacts[]`**, not pointer alone |
| **Termination** | Turn ends when any capture succeeds | Turn ends when batch complete: aggregate successes → **one batch `ProtocolFinal`** (or N finals interim) |

Optional follow-up (not required if parallel emission works): batch tool **`propose_facet_assignments`** with `assignments[]` — defer unless parallel tool calls prove unreliable in live LLM tests.

### Prompt (WI-347)

Add **`metadata-authoring.batch`** (migrate spirit of `schema-authoring.batch`):

- Scan **entire** user message; do not stop at first entity or first facet type.
- After grounding, emit **all** `propose_facet_assignment` calls in **one parallel batch**.
- One call per distinct `(metadataEntityId, facetTypeKey, payload)`; skip duplicates.
- If one proposal fails validation, remediate and retry **that** proposal; do not drop sibling proposals.

### UI (**WI-351** platform + **WI-349** facet e2e)

| Layer | Today | Target |
|-------|--------|--------|
| **`artifactGroups.ts`** | Each `facet-proposal` → separate render group | **Already correct** — no structural change |
| **`MessageArtifactComposer`** | Maps all groups → one card each | **Already correct** — verify with multi-facet scenario |
| **Section label** | `structuredReplySectionTitle` → “Facet proposal” (singular) | Pluralize when `artifacts.filter(facet-proposal).length > 1` → “Facet proposals” |
| **GET replay / SSE** | Tested with single artefact | **IT + Vitest:** turn with **≥2** `facet-proposal` in `artifacts[]` renders **≥2** `FacetCondensedPreview` cards |

**Out of scope:** merging multiple facets into one composite artefact kind; Data Model entity editor batch apply.

### Protocol: batch `ProtocolFinal` (proposal)

`ProtocolFinal` today is **scalar** — one `payload` per event. For multi-facet turns, **extend the protocol** (not every protocol globally):

| Approach | Summary |
|----------|---------|
| **A. N × `ProtocolFinal`** | Same `protocolId`, one proposal per event — works with persistence; SSE `replace` mode risks dropping earlier live parts unless mapper is fixed |
| **B. Batch envelope (recommended)** | **One** `ProtocolFinal`: `{ results: [ proposal, … ] }` on `metadata.faceting.capture`; runtime **fans out** to N persist rows + N SSE `facet-proposal` parts |

**Why B:** one logical “capture completed” per turn; `finalSchema` documents the contract; agent aggregates parallel tool results once.

```yaml
# metadata-authoring.yaml — sketch
protocols:
  metadata.faceting.capture:
    mode: structured_final
    multi: true                    # NEW
    finalSchema:
      type: object
      required: [results]
      properties:
        results:
          type: array
          minItems: 1
          items: { /* single facet proposal shape */ }
```

**Fan-out:** router / projector / `AgentEventToSseMapper` split each `results[i]` → separate `ArtifactRecord` + `item.part.updated` — GET replay stays **flat `artifacts[]`** (UI already renders N cards).

Single-facet turns: `results` length 1; optional normalizer accepts legacy scalar payload.

**Interim:** N × `ProtocolFinal` only if batch envelope slips in WI-351 — must still yield N replay artefacts.

### Example (design doc)

> User: “**orders.customer_id** is required, **orders** is the main sales table, and it joins **customers** on **customer_id** = **id**.”

Expected captures (same turn, parallel):

| # | `facetTypeKey` | Target | Notes |
|---|----------------|--------|--------|
| 1 | `dq-null-check` | attribute `…/orders.customer_id` | `name`: `customer_id_not_null` |
| 2 | `descriptive` | table `…/orders` | description from utterance |
| 3 | `relation` or relation-source/target family | table `…/orders` | join to customers |

## Tool gaps and proposals (planning)

Inventory of **deficits** vs the planned four tools, and what to add or extend. Priority:
**P0** = include in this story if possible; **P1** = strong follow-up; **P2** = defer.

### Baseline (locked)

| Capability | Tools |
|------------|--------|
| `metadata` | `list_facet_types`, **`get_facet_type`**, **`list_metadata_scopes`**, `list_entity_facets`, `validate_facet_payload` |
| `metadata-authoring` | `propose_facet_assignment` |

### P0 — extend existing tools (no new tool names)

| Gap | Proposal |
|-----|----------|
| **Target URN construction** | **No new tool.** Ground with **`schema`** tools (`list_schemas` / `list_tables` / `list_columns`); build **`metadataEntityId`** from returned names + [`metadata-urn-platform.md`](../../../design/metadata/metadata-urn-platform.md) in prompts. **Optional WI-347:** echo canonical **`metadataEntityId`** on each schema row so the model copies instead of constructing URNs ([`GAPS.md`](GAPS.md) §3a). Verify via **`validate_facet_payload(…, metadataEntityId)`** and optionally **`list_entity_facets`**. |
| **Large catalog / token budget** | **`list_facet_types`**: optional filters (`metadataEntityId`, `category`, `applicableTo`); **summary rows only** — no `contentSchema` ([`GAPS.md`](GAPS.md) §3b). |
| **Full schema for one type** | **`get_facet_type(facetTypeKey)`**: full manifest + **`contentSchema`** for payload generation. |
| **Applicability + conflicts** | **`validate_facet_payload`**: with `metadataEntityId`, check **`applicableTo`**, and warn when **`targetCardinality: SINGLE`** but `list_entity_facets` shows an existing assignment of that type (validator may call port internally). |
| **Proposal completeness** | **`propose_facet_assignment`**: **`scopeUrn`** must reference a **`writable: true`** row from **`list_metadata_scopes`** + optional **`mergeAction`** ([`GAPS.md`](GAPS.md) §3c, §5). Chat default: `urn:mill/metadata/scope:chat-<chatId>`. **Empty scope list:** artefact persisted for replay but not merged — consumer handles orphans. |
| **Artefact body** | Chat **`facet-proposal`** content should always include **`facetTypeKey`**, **`metadataEntityId`**, **`serializedPayload`**, optional **`scopeUrn`** / **`mergeAction`** — same keys for all types. |

### P1 — new QUERY tools (WI-347)

| Tool | Capability | Status |
|------|------------|--------|
| **`get_facet_type`** | `metadata` | **Locked** — full manifest + `contentSchema` after `list_facet_types` shortlist ([`GAPS.md`](GAPS.md) §3b) |
| **`list_metadata_scopes`** | `metadata` | **Locked** — ensure **`metadata_scope`** chat row; global (`writable: false`) + chat (`writable: true`); `CHAT` / `PRIVATE` / owner / `Chat <title>` ([`GAPS.md`](GAPS.md) §3c) |

~~**`build_metadata_entity_urn`**~~ — **not planned**; resolve targets via existing **`schema`** + **`metadata`** tools ([`GAPS.md`](GAPS.md) §3a).

### P1 — catalog / seed **data** (not LLM tools)

| Data | Where | Why |
|------|--------|-----|
| **`examplePayload`** (or `examples[]`) on facet types | Platform + DQ facet type YAML | LLM few-shot per type (especially **`dq-null-check`**, **relation\***, **dq-predicate**). Complements **`metadata-authoring.reasoning`** without bloating prompts. |
| **Category index in prompts** | `metadata-authoring.reasoning` | Short map: `descriptive` → NL text; `relation-*` → joins; `dq-*` → constraints/rules — always resolve type via `list_facet_types`. |
| **Field stereotypes** | Already in `contentSchema` (`stereotype: tags`, etc.) | Surfaced via **`get_facet_type`**; list rows stay minimal ([`schema-facet-ai-tool-field-mapping.md`](../../../design/metadata/schema-facet-ai-tool-field-mapping.md)). |
| **Harness catalog breadth** | `HarnessMetadataReadPort` | Must include **descriptive + relation + DQ** manifests for scenarios (today: descriptive only). |

### P2 — defer

| Item | Reason |
|------|--------|
| **`propose_facet_assignments`** (batch) | Defer if parallel **`propose_facet_assignment`** + multi-emission runtime works; revisit only if live LLM batching is flaky. |
| **`search_facet_types`** (text) | Category + applicability filters enough until catalog is huge. |
| **`ensure_metadata_entity`** | No metadata write in story; entity rows assumed from seeds / import. |
| **Persist / approve facet** | Out of scope (separate promotion workflow **M-23**). |

### Explicitly **not** recommended

| Item | Reason |
|------|--------|
| Per-facet **`capture_*`** tools | Story rule |
| Per-facet **artefact kinds** | Single **`facet-proposal`** |
| Duplicate validation tools | One **`validate_facet_payload`** shared with capture |

## Related

- [`metadata-facet-catalog-v3.md`](../../../design/agentic/metadata-facet-catalog-v3.md)
- [`schema-authoring.yaml`](../../../../ai/mill-ai/src/main/resources/capabilities/schema-authoring.yaml) — cleanup target
- [`dq-rule-facet-types.md`](../../../design/metadata/dq-rule-facet-types.md)
- [`GAPS.md`](GAPS.md) — open gaps and decisions for review
- [`COLDSTART.md`](COLDSTART.md) — agent handover (read first)
