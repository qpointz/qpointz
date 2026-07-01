# AI concepts — metadata model, capability, injection, and authoring

**Branch:** `feat/ai-concepts`  
**Milestone:** **0.8.0** (planned)  
**Related backlog:** **[A-99](../../BACKLOG.md)**

Bring **business concepts** (domain knowledge beyond physical schema metadata) into the **mill-ai v3**
capability stack for **general chat** (`/chat`, `data-analysis` profile): platform `concept` facet
definition, canonical model-level facet representation, read tools, SQL grounding in mixed turns, and
v1 **enrich-model** capture parity via `metadata-authoring`.

Concepts are **model-level metadata**: captured concepts are always assigned to
[`ModelEntityUrn.MODEL_ENTITY_ID`](../../../data/mill-data-metadata/src/main/kotlin/io/qpointz/mill/data/metadata/ModelEntityUrn.kt)
(`urn:mill/model/model:model-entity`) — the only adequate target because concepts may span schemas
and objects.
The concept facet payload carries concept prose, indicative SQL, and tags. `targets[]`
is legacy/compatibility payload only in the first iteration; do not ask the LLM to infer separate
"reality targets" when the indicative SQL already describes how the concept maps to data.
Legacy **AI v1** treated concepts as transient LLM JSON in the `enrich-model` intent without
persisting or re-injecting them into prompts. This story closes that gap on the v3 architecture by
persisting concept facets into the `w` context and exposing them back to agents.

**Design reference:** [`docs/design/agentic/v3-foundation-decisions.md`](../../../design/agentic/v3-foundation-decisions.md) §5.3 / §7.3  
**Detailed plan (review):** [`PLAN.md`](PLAN.md) — full architecture, legacy analysis, injection design, WI mapping  
**Gaps and open decisions:** [`GAPS.md`](GAPS.md) — blocking items to lock in WI-366 before implementation  
**Normative design output (WI-366):** [`docs/design/agentic/concept-metadata-model.md`](../../../design/agentic/concept-metadata-model.md)

## Staged execution

1. **Model** — metadata representation and fixtures (WI-366)
2. **Runtime guardrail** — configurable agent iteration limit (WI-372) before or alongside concept tool scenarios
3. **Read + SQL grounding** — `concept` capability and `data-analysis` injection (WI-367, WI-369)
4. **Author** — v1 capture parity and facet persistence in general chat (WI-370)

## Work Items

- [x] WI-366 — Concept metadata model contract and seed fixtures (`WI-366-concept-metadata-model.md`)
- [x] WI-372 — Configurable agent iteration limit side quest (`WI-372-agent-iteration-limit-config.md`)
- [x] WI-367 — Concept catalog port and read capability (`WI-367-concept-catalog-capability.md`)
- [x] WI-369 — Concept injection in data-analysis profile (`WI-369-concept-sql-injection.md`)
- [x] WI-370 — Concept authoring and v1 enrich-model capture (`WI-370-concept-authoring-capture.md`)

## Story Acceptance

General-chat end-to-end acceptance ties the WIs together:

1. WI-366 seeds a VIP passenger concept as a model-level `concept` facet on `ModelEntityUrn.MODEL_ENTITY_ID` in `w`.
2. WI-367 exposes that concept through `get_concept` and `get_model_concepts`.
3. WI-369 lets the `data-analysis` profile use concept tools or injection so a "vip passengers" SQL request can ground on the concept definition and indicative SQL.
4. WI-370 captures a new or refined concept from general chat, persists it through `metadata-authoring.propose_facet_assignment`, accepts it, and makes it readable through the WI-367 path.
5. WI-372 keeps concept-tool integration tests reliable by making the native tool-loop iteration limit configurable while preserving the default of `20`.

## Related stories

- [`../ai-v1-v3-parity-baseline/STORY.md`](../ai-v1-v3-parity-baseline/STORY.md) (**WI-151** — parity matrix)
- [`../concept-object-relations/STORY.md`](../concept-object-relations/STORY.md) — candidate concept-to-object links via `core/mill-events`
- [`../../in-progress/metadata-authoring-profiles/STORY.md`](../../in-progress/metadata-authoring-profiles/STORY.md) — facet capture / `metadata-authoring` capability
- [`../../completed/20260417-schema-capability-metadata/STORY.md`](../../completed/20260417-schema-capability-metadata/STORY.md) — schema + metadata read on v3

## Out of scope (story level)

- **Knowledge / contextual inline chat** — `contextType=knowledge`, `concept-exploration` profile,
  mill-ui `/knowledge` inline chat (WI-368, WI-371 **deferred** — subject to separate review)
- Semantic / vector concept search (`search_concepts` is lexical in v1; use tags for deterministic narrowing)
- Full v1 step-back reasoner parity
- `ui/mill-grinder-ui` changes (legacy / retired)

## Deferred work items (not on this branch)

- [`WI-368-concept-chat-injection.md`](WI-368-concept-chat-injection.md) — knowledge-context chat
- [`WI-371-mill-ui-knowledge-chat.md`](WI-371-mill-ui-knowledge-chat.md) — mill-ui knowledge inline chat
