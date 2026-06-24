# WI-349 — Tests, scenario pack, and documentation

Status: `planned`  
Type: `🧪 test`, `📝 docs`  
Area: `ai`  
Depends on: [WI-350](WI-350-schema-authoring-description-tool-cleanup.md)

## Problem Statement

Without Skymill **`testIT`**, scenario replay, and updated design/public docs, the story cannot be
verified or operated by the next developer.

## Goal

Prove end-to-end **`metadata-authoring`** profile behaviour and document the new profile family.

## In Scope

1. **`AiChatControllerIT`** — SSE smoke with real catalog; facet-proposal for **non-descriptive** type when harness allows
2. **`mill-ai-test`** scenario packs:
   - **Intent routing** — natural language (no “capture a facet” meta): e.g. “orders.customer_id must not be null” → **`dq-null-check`** capture; **must not** call `validate_sql`
   - **Multi-facet batch** — one utterance with description + DQ + relation (or two+ types): **≥2** `propose_facet_assignment` calls; replay shows **≥2** `facet-proposal` artefacts
   - **Descriptive** — `validate_facet_payload` then `propose_facet_assignment(target, descriptive, payload)`
   - **Relation** — schema-valid payload for relation facet type
   - **DQ** — e.g. `dq-null-check` with attribute target; assert validate rejects bad payload
   - Harness port expanded beyond single `descriptive` ([`HarnessMetadataReadPort`](../../../../ai/mill-ai-test/src/main/kotlin/io/qpointz/mill/ai/test/runner/ScenarioHarnessSupport.kt))
3. **Design docs** — rewrite [`metadata-facet-catalog-v3.md`](../../../design/agentic/metadata-facet-catalog-v3.md): catalog-generic loop; multi-facet batch; deprecate dual capture paths
4. **Public docs** — `docs/public/src/mill-ui.md` § agent profiles: YAML seeding, `metadata-authoring` vs
   `schema-authoring` use cases; operator guide for `mill.ai.profiles.seed.resources`
5. **`mill-ai-cli` README** — example profile flag for metadata authoring

## Out of Scope

- Multi-artifact platform implementation (**WI-351** — validated here via e2e only)
- Live-LLM CI matrix (**A-94** / **A-95**)
- Story closure (`MILESTONE.md`, `BACKLOG.md`, archive) — explicit user request only
- mill-ui facet editor changes (Data Model mutation UI)
- Composite single artefact bundling multiple facets (use **N** `facet-proposal` rows instead)

## Acceptance Criteria

- [ ] `testIT` proves `metadata-authoring` chat path on Skymill fixture
- [ ] Tests prove `validate_facet_payload` and `propose_facet_assignment` reject schema-invalid payloads
- [ ] **Intent scenario:** documentary DQ utterance produces facet capture without SQL tools
- [ ] **Multi-facet scenario:** one turn persists/replays **≥2** `facet-proposal` artefacts; mill-ui shows **≥2** cards
- [ ] Descriptive, relation, and DQ captures all persist/replay as **`facet-proposal`** (same artefact kind; `facetTypeKey` in JSON body)
- [ ] No scenario or manifest uses any `capture_<facet>` tool
- [ ] Design + public docs describe when to pick `metadata-authoring` vs `schema-authoring`
- [ ] STORY verify commands pass locally

## Suggested commit

`[docs] WI-349: metadata-authoring scenarios, IT coverage, and design docs`
