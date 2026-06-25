# WI-349 — Tests, scenario pack, and documentation

Status: `planned`  
Type: `🧪 test`, `📝 docs`  
Area: `ai`  
Depends on: [WI-350](WI-350-schema-authoring-description-tool-cleanup.md), [WI-353](WI-353-facet-artifact-lifecycle-events.md)  
**Stage:** 8 — branch `test/meta-authoring-e2e` (see [`STORY.md`](STORY.md))

## Problem Statement

Without Skymill **`testIT`**, scenario replay, and updated design/public docs, the story cannot be
verified or operated by the next developer.

## Goal

Prove end-to-end **`metadata-authoring`** profile behaviour and document the new profile family.

## In Scope

1. **`AiChatControllerIT`** — SSE smoke with real catalog; facet-proposal for **non-descriptive** type when harness allows
2. **`mill-ai-test`** scenario packs:
   - **Intent routing** — documentary DQ utterance → facet capture; **must not** call `validate_sql` when query-only
   - **Mixed SQL + facet** — one utterance: documentary constraint + data query → `generated-sql` + `facet-proposal` on same turn; UI shows both
   - **Multi-SQL binding** — one turn with **2+** `sql` artefacts: each card gets its own `executionId` / grid via **`artifactId`** (**WI-353** §0)
   - **Multi-facet batch** — one utterance with description + DQ + relation (or two+ types): **≥2** `propose_facet_assignment` calls; replay shows **≥2** `facet-proposal` artefacts
   - **Partial batch** — 2+ parallel `propose_facet_assignment`, one fails: replay shows artefacts for **successes only**; remediation in follow-up round
   - **Descriptive** — `validate_facet_payload` then `propose_facet_assignment(target, descriptive, payload)`
   - **Relation — source** — table is **source** of join → **`relation-source`** with schema-valid payload
   - **Relation — target** — table is **target** of join → **`relation-target`** with schema-valid payload
   - **Accept / Reject** — pending facet → Accept locks; Reject clears scope + artefact (**WI-353**)
   - Harness uses **WI-346** expanded port ([`HarnessMetadataReadPort`](../../../../ai/mill-ai-test/src/main/kotlin/io/qpointz/mill/ai/test/runner/ScenarioHarnessSupport.kt)) — **no** harness expansion in this WI ([`GAPS.md`](GAPS.md) §12)
3. **Design docs** ([`GAPS.md`](GAPS.md) §13) — **integration pass**; each WI patches its doc during implementation:
   - **[`metadata-facet-catalog-v3.md`](../../../design/agentic/metadata-facet-catalog-v3.md)** — full catalog-generic rewrite (canonical hub)
   - **[`metadata-content.md`](../../../design/metadata/metadata-content.md)** — verify WI-352 sections complete
   - **[`ai-v3-chat-metadata-scope.md`](../../../design/agentic/ai-v3-chat-metadata-scope.md)** — verify WI-353 lifecycle rewrite
   - **[`artifact-foundation.md`](../../../design/agentic/artifact-foundation.md)** — verify WI-351 batch §
   - **[`v3-mcp-capability-exposure.md`](../../../design/agentic/v3-mcp-capability-exposure.md)** — §15 inventory: add `list_facet_categories`, `get_facet_type`, `list_metadata_scopes`; remove **`schema-authoring`** rows; **do not** recommend `mcp.enabled: false` for new metadata tools ([`GAPS.md`](GAPS.md) §18); refresh tool totals; optional `CapabilityMcpCatalog` test for `metadata-authoring` profile
4. **Public docs** — `docs/public/src/mill-ui.md` § agent profiles: YAML seeding, `metadata-authoring` vs
   `schema-authoring` use cases; operator guide for `mill.ai.profiles.seed.resources`
5. **`mill-ai-cli` README** — example profile flag for metadata authoring

## Out of Scope

- Multi-artifact platform implementation (**WI-351** — validated here via e2e only)
- Live-LLM CI matrix (**A-94** / **A-95**)
- Story closure (`MILESTONE.md`, `BACKLOG.md`, archive) — explicit user request only
- mill-ui facet editor changes (Data Model mutation UI)
- Composite single artefact bundling multiple facets (use **N** `facet-proposal` rows instead)
- Legacy `schema-authoring.capture` / `FacetProposalWire` replay compatibility ([`GAPS.md`](GAPS.md) §11)

## Acceptance Criteria

- [ ] `testIT` proves `metadata-authoring` chat path on Skymill fixture
- [ ] Tests prove `validate_facet_payload` and `propose_facet_assignment` reject schema-invalid payloads
- [ ] **Intent scenario (query-only):** documentary DQ utterance produces facet capture without SQL tools
- [ ] **Multi-SQL scenario:** one turn with **≥2** `sql` artefacts — each pairs with its own `data` / grid by **`artifactId`** ([`GAPS.md`](GAPS.md) §10 phase B)
- [ ] **Mixed SQL + facet scenario:** same turn replays **both** SQL and `facet-proposal` artefacts ([`GAPS.md`](GAPS.md) §10)
- [ ] **Multi-facet scenario:** one turn persists/replays **≥2** `facet-proposal` artefacts; mill-ui shows **≥2** cards
- [ ] Descriptive, relation, and DQ captures all persist/replay as **`facet-proposal`** (same artefact kind; `facetTypeKey` in JSON body)
- [ ] No scenario or manifest uses any `capture_<facet>` tool
- [ ] Design + public docs describe when to pick `metadata-authoring` vs deprecated `schema-authoring` profile id
- [ ] **`docs/design/`** docs consistent per §13 (hub + cross-links; no drift vs WI markdown)
- [ ] **`list_facet_categories`** / **`get_facet_type`** examples exercised in scenario or IT
- [ ] Artefact replay includes **`writeScopeUrns[]`** when chat scopes bound
- [ ] **Accept/Reject scenario:** pending facet → Accept locks artefact; Reject clears chat scope + removes artefact from replay (**WI-353**)
- [ ] **MCP inventory** ([`GAPS.md`](GAPS.md) §18): `v3-mcp-capability-exposure.md` §15 lists all new `metadata` / `metadata-authoring` tools; no `schema-authoring` rows; totals match open-filter catalog test

## Suggested commit

`[docs] WI-349: metadata-authoring scenarios, IT coverage, and design docs`
