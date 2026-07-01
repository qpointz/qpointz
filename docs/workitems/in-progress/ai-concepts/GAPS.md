# Gaps and open decisions — AI concepts

**Story:** [`STORY.md`](STORY.md)  
**Plan:** [`PLAN.md`](PLAN.md)  
**Branch:** `feat/ai-concepts`  
**Status:** planning review (2026-06-30) — **no implementation yet**

This document collects **gaps**, **ambiguities**, and **decisions still needed** before or during
implementation. Resolved items should move into WI acceptance criteria, `PLAN.md`, or
`docs/design/agentic/concept-metadata-model.md`, then be struck or marked **LOCKED** here.

---

## Gap tracker

| ID | Title | Status | Priority | Owner WI |
|----|-------|--------|----------|----------|
| GAP-1 | Model-level facet vs concept-entity storage | **locked** | **critical** | WI-366 |
| GAP-2 | Concept logical ref URN (`urn:mill/model/concept:<slug>`) | **locked** | high | WI-366, WI-367 |
| GAP-3 | One concept facet per model vs many concepts in one facet | **locked** | high | WI-366 |
| GAP-4 | Platform `concept` facet type not in bootstrap seed | **locked** | high | WI-366 |
| GAP-5 | Concept ref resolution (not `MetadataEntityIds`) | **locked** | high | WI-366, WI-367 |
| GAP-6 | Concept catalog adapter read sources (entity browse vs `w` facets) | **locked** | high | WI-366, WI-367 |
| GAP-7 | Concept ref at capture time vs relate-event timing | **locked** | medium | WI-366, WI-370, `concept-object-relations` |
| GAP-8 | `data-analysis.intent` composition with `concept.intent` | **locked** | medium | WI-367, WI-369 |
| GAP-9 | Knowledge chat `contextEntityType` on create | **deferred** | — | WI-368 (out of story scope) |
| GAP-10 | Test fixtures (`example.yml`) use legacy standalone CONCEPT entities | **locked** | medium | WI-366 |
| GAP-11 | WI-372 not placed in staged execution | **locked** | low | STORY, WI-372 |
| GAP-12 | Cross-WI end-to-end acceptance scenario undefined | **locked** | low | STORY, PLAN |
| GAP-13 | Milestone / backlog row not set | **locked** | low | STORY |

---

## 1. Model-level facet vs concept-entity storage — **LOCKED**

**Gap (original):** The story states captured concepts attach to the **model entity** in chat `w`
scope. Existing platform data and UI patterns still treat concepts as **standalone metadata
entities** (`type: CONCEPT`, `urn:mill/model/concept:<id>`).

### Locked decision (2026-06-30)

**All concepts are assigned only to the logical model root entity.** This is the sole adequate
assignment target because business concepts may span schemas, tables, and attributes.

| Constant | Value |
|----------|--------|
| **`ModelEntityUrn.MODEL_ENTITY_ID`** | `urn:mill/model/model:model-entity` |

| Concern | Normative rule |
|---------|----------------|
| **Assignment target (read + write)** | `MODEL_ENTITY_ID` only — `applicableTo` on the platform `concept` facet type must reference this URN |
| **Scope** | Chat `w` context for capture; global (or configured read scope) for published/seeded concepts — still on **`MODEL_ENTITY_ID`**, not per-schema or per-table entities |
| **Capture** | `propose_facet_assignment` with `facetTypeKey=concept` on `MODEL_ENTITY_ID` in chat `w` scope only |
| **Standalone `type: CONCEPT` entities** | **Not** an assignment target in v1 — legacy seeds (e.g. [`example.yml`](../../../../metadata/mill-metadata-core/src/test/resources/metadata/example.yml)) are deprecated for new work; migrate fixtures to model-level facets |
| **Logical concept ref** | `urn:mill/model/concept:<slug>` — canonical id for tools and catalog reads; identifies a concept within model-level facet assignments on `MODEL_ENTITY_ID`, not a separate metadata entity row |
| **Knowledge / contextual chat** | **Out of story scope** — `contextType=knowledge`, focused concept explain, and mill-ui inline chat deferred (WI-368, WI-371) |
| **Legacy `targets[]`** | Read for compatibility on old data only; never written on capture in v1 |

**Rationale:** Concepts describe cross-cutting business meaning; binding them to schema/table
entities would fragment definitions and break multi-schema grounding.

**Owner:** [`WI-366-concept-metadata-model.md`](WI-366-concept-metadata-model.md) →
`docs/design/agentic/concept-metadata-model.md`

---

## 2. Concept logical ref URN — **LOCKED**

**Gap (original):** Concept identity for tools and catalog reads needed a single URN grammar;
legacy IT used `concept.clv`-style ids in knowledge-context chat (deferred — see §9).

### Locked decision (2026-06-30)

**Canonical concept ref for tools, catalog, and capture:**

```text
urn:mill/model/concept:<slug>
```

| Rule | Detail |
|------|--------|
| **`<slug>`** | kebab-case concept identifier (e.g. `premium-customers`, `vip-passengers`) |
| **Resolution** | [`ConceptRefs`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/capabilities/metadata/MetadataEntityIds.kt) (WI-367) — full URN only; not dot-separated catalog paths |
| **Lookup** | Slug → concept facet assignment on `MODEL_ENTITY_ID` (GAP-1, GAP-6) |
| **General chat** | Agents use `get_concept` / `search_concepts` / `get_model_concepts` by ref or keyword — no `contextType=knowledge` binding in this story |
| **Knowledge contextual chat** | **Deferred** (WI-368) — do not design `contextId` / `contextEntityType` rules here |

This matches [`ModelEntityUrn.forConcept`](../../../../data/mill-data-metadata/src/main/kotlin/io/qpointz/mill/data/metadata/ModelEntityUrn.kt)
notation.

**Owner:** WI-366 (contract); WI-367 (implementation + tests)

---

## 3. Cardinality: multiple assignments, one concept per assignment — **LOCKED**

**Gap (original):** `ConceptFacet` is `concepts: List<Concept>` while platform reference had
`targetCardinality: SINGLE`. This incorrectly conflated assignment cardinality with payload shape.

### Locked decision (2026-06-30)

**Model entities may have multiple `concept` facet assignments; each assignment represents exactly
one concept.**

| Rule | Detail |
|------|--------|
| **Facet assignments on `MODEL_ENTITY_ID`** | One `concept` facet assignment **per business concept** |
| **`targetCardinality`** | `MULTIPLE` — the model root may carry many concept facet assignments |
| **Payload `concepts[]`** | Exactly **one** entry (`[0]`) per assignment; array retained for v1/`ConceptFacet` wire compatibility and enrich-model shape only |
| **Identity** | 1:1 mapping: `urn:mill/model/concept:<slug>` ↔ one facet assignment ↔ one `concepts[0]` entry |
| **Many concepts on the model** | Many facet assignments on `MODEL_ENTITY_ID`, not one facet with many entries |
| **LLM infers multiple concepts in one turn** | **N concepts → N facet assignments** — parallel `propose_facet_assignment` calls (same batch/`results[]` lifecycle as other multi-facet capture); never pack multiple concepts into one assignment |
| **Merge on accept** | Never append a second concept into an existing assignment; propose/update the assignment for that slug |

**Owner:** WI-366

---

## 4. Platform `concept` facet type in bootstrap seed — **LOCKED**

**Gap (original):** `platform-bootstrap.yaml` comment listed `concept` but had **no**
`FacetTypeDefinition` entry. `platform-facet-types.json` had an empty `contentSchema`.

### Locked decision (2026-06-30)

**Added `urn:mill/metadata/facet-type:concept` to platform seed facets** (WI-366):

| Item | Value |
|------|--------|
| **Bootstrap** | [`platform-bootstrap.yaml`](../../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-bootstrap.yaml) — loaded via `mill.metadata.seed.resources` |
| **Reference JSON** | [`platform-facet-types.json`](../../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-facet-types.json) — aligned with YAML |
| **`applicableTo`** | `urn:mill/metadata/entity-type:model` only ([`SchemaEntityTypeUrns.MODEL`](../../../../data/mill-data-schema-core/src/main/kotlin/io/qpointz/mill/data/schema/SchemaEntityTypeUrns.kt)) |
| **`targetCardinality`** | `MULTIPLE` (GAP-3) |
| **`contentSchema`** | `concepts[]` with one entry: `name`, `description`, `sql`, `tags`, `source`, `sourceSession` — **no `category`**, **no `targets`** in v1 |
| **Assignment instance** | Facets attach to [`ModelEntityUrn.MODEL_ENTITY_ID`](../../../../data/mill-data-metadata/src/main/kotlin/io/qpointz/mill/data/metadata/ModelEntityUrn.kt) |

**Owner:** WI-366 — design doc still to capture normative prose; seed implementation **done**.

---

## 5. Concept ref resolution — **LOCKED**

**Gap (original):** [`MetadataEntityIds`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/capabilities/metadata/MetadataEntityIds.kt)
resolves physical catalog objects by hierarchical name (`schema`, `schema.table`,
`schema.table.attribute`). Concepts are a **different entity kind** and are not physical objects.

### Locked decision (2026-06-30)

**Do not extend `MetadataEntityIds` for concepts.**

| Resolver | Scope | Input |
|----------|-------|--------|
| **`MetadataEntityIds`** | Physical / relational catalog only | Qualified catalog path or `urn:mill/model/schema\|table\|attribute:…` |
| **`ConceptRefs`** (new, `mill-ai` concept capability) | Business concepts only | Full URN `urn:mill/model/concept:<slug>` only (GAP-2 **locked**) |

| Rule | Detail |
|------|--------|
| **No hierarchical name** | Concepts do **not** resolve from dot-separated catalog paths (`premium.customers` is not a concept ref) |
| **URN only** | Parse via [`ModelEntityUrn.forConcept`](../../../../data/mill-data-metadata/src/main/kotlin/io/qpointz/mill/data/metadata/ModelEntityUrn.kt) / `kindOf == concept`; extract `<slug>` |
| **Lookup** | Slug → concept facet assignment on `MODEL_ENTITY_ID` (GAP-1, GAP-6) |
| **Tool / catalog input** | Full concept URN (GAP-2); validated by `ConceptRefs`, not `MetadataEntityIds` |

Implement `ConceptRefs` (or equivalent on `ConceptCatalogPort`) in **WI-367** with unit tests.
Document the split in WI-366 design note.

**Owner:** WI-366 (design); WI-367 (implementation + tests)

---

## 6. Concept catalog adapter read sources — **LOCKED**

**Gap (original):** WI-367 listed both `findByKind("concept")` and model-level facets from `w`.

### Locked decision (2026-06-30)

Follows **GAP-1**: canonical storage is concept facet assignments on **`MODEL_ENTITY_ID`**.

| Tool | Source |
|------|--------|
| `get_model_concepts` | All `concept` facet assignments on `MODEL_ENTITY_ID` in the active read scope (`w` for chat capture; global/merged scopes for published catalog) |
| `list_concept_tags` | Same — return distinct tags from concept facets on `MODEL_ENTITY_ID` in the active read scope |
| `list_concepts` / `search_concepts` | Same — enumerate concept facets on `MODEL_ENTITY_ID`; `list_concepts` supports optional exact tag filtering; `search_concepts` is lexical over name/description/tags; do **not** use `findByKind("concept")` as primary |
| `get_concept` | Resolve slug/URN → concept entry within a model-level `concept` facet on `MODEL_ENTITY_ID` |

Optional: legacy standalone CONCEPT entities may be read once for migration tests only; not part of
the normative v1 adapter contract.

**Dedup key:** kebab-case concept **slug** from `urn:mill/model/concept:<slug>` (GAP-3 **locked**).

**Owner:** WI-366 (contract); WI-367 (adapter)

---

## 7. Concept ref at capture time vs relate-event timing — **LOCKED**

**Gap (original):** WI-370 may emit candidate concept-to-object links at capture time, but a
persisted concept facet row (and any legacy standalone concept entity) does not exist until after
facet accept. Relate events need a stable concept key before `artifact.facet.persisted` materializes
the definition.

**Clarification:** Assign-facet and relate are **orthogonal events**, not duplicate pipelines:

| Event | Purpose | Owner |
|-------|---------|-------|
| `artifact.facet.persisted` | Materialize concept **definition** on `MODEL_ENTITY_ID` | Existing metadata consumers (`FacetProposalEventConsumers`) |
| `concept.link.*` (name TBD) | Materialize **relations** from concept → metadata objects | `concept-object-relations` (WI-374–375) |

### Locked decision (2026-06-30)

**Assign `conceptRef` at proposal time; key link candidates by that ref; defer relate
infrastructure to the follow-on story.**

| Rule | Detail |
|------|--------|
| **Logical concept id** | `urn:mill/model/concept:<slug>` — assigned when the facet proposal is built (normalized kebab-case from concept name); not a standalone metadata entity row |
| **Facet payload** | Include `conceptRef` in the proposed facet body (or equivalent canonical field) so accept materializes a resolvable slug |
| **Link candidates** | Live in the artifact **envelope** outside `serializedPayload`; each entry references `conceptRef`, `targetRef`, `linkKind`, optional evidence |
| **Correlation** | `parentFacetArtifactId` (or `sourceFacetArtifactId`) ties link candidates to the facet-proposal artifact before accept |
| **Timing** | Capture turn may emit facet proposal + link candidates together; **facet accept** fires assign-facet; **link accept** (separate or bundled lifecycle) fires relate events |
| **Relate consumer input** | Resolve by `conceptRef` on `MODEL_ENTITY_ID` after definition exists; idempotency by `(sourceArtifactId, conceptRef, targetRef, linkKind)` — not by facet `uid` at proposal time |

| In `ai-concepts` (WI-366, WI-370) | In `concept-object-relations` (WI-373–377) |
|-----------------------------------|--------------------------------------------|
| Normalize name → `conceptRef`; facet capture via `propose_facet_assignment` | Protocol shape for candidate links and relation projections |
| Detect grounding; emit link candidates in envelope keyed by `conceptRef` | Relate event types, producers, consumers |
| No relate event consumers or relation persistence | Projection persistence, rebuild, read API, UI navigation |

**Owner:** WI-366 (design contract); WI-370 (capture + envelope); WI-373–377 (relate pipeline)

---

## 8. `data-analysis.intent` composition — **LOCKED**

**Gap (original):** [`platform-agent-profiles.yaml`](../../../../ai/mill-ai/src/main/resources/profiles/platform-agent-profiles.yaml)
`data-analysis.intent` composed sql-query, schema, and metadata-authoring only. WI-369 adds
`concept` without profile-level wording or MCP exposure rules.

### Locked decision (2026-06-30)

**Add `concept` to the `data-analysis` profile; compose `concept.intent` at profile level; expose
concept QUERY tools through MCP when the server uses the `data-analysis` profile filter.**

| Rule | Detail |
|------|--------|
| **Profile capabilities** | Extend `data-analysis` with **`concept`** alongside existing `conversation`, `schema`, `metadata`, `metadata-authoring`, `sql-dialect`, `sql-query`, `value-mapping` (WI-369) |
| **`data-analysis.intent`** | Add bullet **after** `sql-query.intent`, **before** schema/authoring routes: use **`concept.intent`** + concept tools (`list_concept_tags`, `list_concepts`, `search_concepts`, `get_model_concepts`, `get_concept`) as **semantic hints** when the user uses domain/business language — **do not** classify DATA_QUERY or replace `sql-query.intent` |
| **`concept.intent`** | Stays capability-local: lookup, explain, define/refine signals only — never owns DATA_QUERY |
| **MCP exposure** | No new MCP transport. Declare tools in `capabilities/concept.yaml`; **MCP-enabled by default** (omit `mcp.enabled: false`). When `mill.ai.mcp.profile=data-analysis`, [`CapabilityMcpCatalog`](../../../../ai/mill-ai-mcp-core/src/main/kotlin/io/qpointz/mill/ai/mcp/CapabilityMcpCatalog.kt) exposes `concept.*` QUERY tools with other profile capabilities (`sql-query.*`, `schema.*`, …) per [`v3-mcp-capability-exposure.md`](../../../design/agentic/v3-mcp-capability-exposure.md) |
| **CAPTURE tools** | Concept capture stays on `metadata-authoring.propose_facet_assignment` — not duplicated on `concept` |

**Tests (WI-369):** `ProfileIntentPromptTest` — `data-analysis.intent` references `concept.intent`;
`concept.intent` does not contain DATA_QUERY. Optional `CapabilityMcpCatalogTest` with
`mill.ai.mcp.profile=data-analysis` lists `concept.*` tools once WI-367 lands.

**Owner:** WI-367 (capability manifest + MCP-default tools); WI-369 (profile YAML, intent, tests)

---

## 9. Knowledge chat `contextEntityType` on create — **DEFERRED**

**Status:** Out of **`ai-concepts`** story scope (2026-06-30). Knowledge / contextual inline chat
(`contextType=knowledge`, mill-ui `/knowledge`, `concept-exploration` profile) is subject to
separate review — see deferred WI-368 / WI-371.

No decision required for general-chat delivery. If revived, distinguish:

| Field | Meaning (for reference only) |
|-------|------------------------------|
| `contextType=knowledge` | Chat started from Knowledge UI (not general `/chat`) |
| `contextEntityType=concept` | `contextId` refers to a business concept ref |

**Owner:** Future story / WI-368 review

---

## 10. Fixture alignment with new model — **LOCKED**

**Gap (original):** [`example.yml`](../../../../metadata/mill-metadata-core/src/test/resources/metadata/example.yml)
uses standalone `CONCEPT` entities; story fixtures must follow GAP-1.

### Locked decision (2026-06-30)

WI-366 fixtures:

- Add 2–3 **`concept` facet assignments on `MODEL_ENTITY_ID`** in `w` (or dedicated
  `mill-ai-test` seed) with descriptions, tags, and indicative SQL
- **No** capture-time `targets[]`
- Do **not** add new standalone `type: CONCEPT` entity rows for story tests
- Legacy `example.yml` CONCEPT entity may remain until migrated; new tests must not depend on it

**Owner:** WI-366

---

## 11. WI-372 placement in staged execution — **LOCKED**

**Gap:** WI-372 (configurable `mill.ai.chat.max-iterations`) is in the story checklist but not in
[`STORY.md`](STORY.md) staged execution. Concept tool scenarios may exhaust the hard-coded
`MAX_ITERATIONS = 20` in [`LangChain4jAgent`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/runtime/langchain4j/LangChain4jAgent.kt).

### Locked decision (2026-07-01)

Run WI-372 as the first runtime guardrail in stage 2, before or alongside WI-367
`mill-ai-test` scenarios. It is not concept-specific behavior, but it removes the hard-coded
iteration ceiling as a source of false failures while the concept capability adds more tool calls.

**Owner:** STORY update; WI-372

---

## 12. Cross-WI end-to-end acceptance scenario — **LOCKED**

**Gap:** Each WI has local acceptance criteria; no single story-level scenario ties seed → read →
SQL grounding → capture in general chat.

### Locked decision (2026-07-01)

Add the following story-level acceptance scenario to [`STORY.md`](STORY.md) and [`PLAN.md`](PLAN.md)
(general chat only):

1. WI-366 seed: VIP passenger concept on model in `w`
2. WI-367: `get_concept` / `get_model_concepts` return it
3. WI-369: `data-analysis` chat uses concept tools / injection for "vip passengers" SQL
4. WI-370: general-chat capture → accept → readable via step 2–3
5. WI-372: configurable loop limit keeps the above scenario reliable under concept tool use

**Owner:** STORY / PLAN hygiene

---

## 13. Milestone and backlog — **LOCKED**

**Gap:** Story and all WIs use **Milestone: TBD**; no `BACKLOG.md` row confirmed.

### Locked decision (2026-07-01)

Set story and WI milestones to **0.8.0** for implementation planning and add planned backlog row
**A-99** pointing to this story. Per [`RULES.md`](../../RULES.md), do not mark the backlog row
`done`, update `MILESTONE.md`, or archive the story until explicit story closure.

**Owner:** Story maintainer

---

## Related decisions already documented elsewhere

| Topic | Where |
|-------|--------|
| GAP-7 assign-facet vs relate; `conceptRef` at capture time | This file §7; WI-366, WI-370; `concept-object-relations` |
| GAP-8 `data-analysis` + `concept`; MCP `concept.*` tools | This file §8; WI-367, WI-369; [`v3-mcp-capability-exposure.md`](../../../design/agentic/v3-mcp-capability-exposure.md) |
| WI-363 non-overlap (concept vs schema/sql/metadata/authoring) | [`PLAN.md`](PLAN.md) § Non-overlap contract |
| Concept-to-object links (full pipeline) | [`concept-object-relations/STORY.md`](../concept-object-relations/STORY.md) |
| v3 design intent for `concept` tools | [`v3-foundation-decisions.md`](../../../design/agentic/v3-foundation-decisions.md) §5.3 |
| Knowledge / contextual chat (deferred) | WI-368, WI-371 — out of story scope |

---

## Resolution log

| Date | ID | Decision |
|------|-----|----------|
| 2026-06-30 | GAP-1 | All concepts assigned only to `ModelEntityUrn.MODEL_ENTITY_ID` (`urn:mill/model/model:model-entity`); sole `applicableTo` target; standalone CONCEPT entities not used in v1 |
| 2026-06-30 | GAP-6 | Read tools enumerate concept facets on `MODEL_ENTITY_ID`; not `findByKind("concept")` |
| 2026-06-30 | GAP-2 | Concept logical ref is full URN `urn:mill/model/concept:<slug>` for tools/catalog; knowledge contextual chat deferred |
| 2026-07-01 | GAP-3 | `targetCardinality=MULTIPLE` lets the model root carry many concept facet assignments; each assignment still has exactly one `concepts[0]` entry; multiple LLM-inferred concepts → multiple parallel facet proposals |
| 2026-07-01 | GAP-4 | `concept` facet type in platform seed (`platform-bootstrap.yaml` + `platform-facet-types.json`); `applicableTo: entity-type:model`; payload excludes `category` and `targets` in v1 |
| 2026-06-30 | GAP-5 | `ConceptRefs` resolves `urn:mill/model/concept:<slug>` only; `MetadataEntityIds` stays physical catalog hierarchy |
| 2026-06-30 | GAP-10 | New fixtures use model-level facets on `MODEL_ENTITY_ID` only |
| 2026-06-30 | GAP-7 | Assign-facet (`artifact.facet.persisted`) and relate (`concept.link.*`) are orthogonal; assign `conceptRef` at proposal time; key link candidates by `conceptRef` + `parentFacetArtifactId`; relate producers/consumers in `concept-object-relations` |
| 2026-06-30 | GAP-8 | Add `concept` to `data-analysis` profile; compose `concept.intent` as semantic hints (not DATA_QUERY); MCP exposes `concept.*` QUERY tools when `mill.ai.mcp.profile=data-analysis` |
| 2026-07-01 | GAP-11 | Run WI-372 early in stage 2 before or alongside WI-367 concept-tool scenarios |
| 2026-07-01 | GAP-12 | Story-level general-chat acceptance covers seed, concept read tools, SQL grounding, capture/accept/readback, and configurable loop limit |
| 2026-07-01 | GAP-13 | Story and WIs target milestone 0.8.0; backlog row A-99 tracks planned delivery without closure-only `done`/MILESTONE updates |
