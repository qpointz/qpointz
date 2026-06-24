# Gaps and open decisions — metadata-authoring-profiles

**Story:** [`STORY.md`](STORY.md)  
**Branch:** `feat/meta-capability-improve`  
**Status:** planning review (not implementation)

This document collects **gaps**, **ambiguities**, and **decisions still needed** before or during implementation. Resolved items should be moved to WI acceptance criteria or STORY architectural table, then struck from here.

---

## 1. WI-351 — proof strategy before WI-347

**Gap:** WI-351 requires two successful CAPTURE emissions → two artefacts, but catalog-generic `propose_facet_assignment` and full validation land in **WI-347**.

| Option | Approach | Pros | Cons |
|--------|----------|------|------|
| **A** | Two parallel `capture_description` on `schema-authoring` profile (removed in WI-350) | Real agent loop + protocol path | Depends on legacy tools briefly |
| **B** | Test-only capability YAML with dummy multi-capture tool | No legacy coupling | Extra test manifest maintenance |
| **C** | Unit tests only — mock capture results in `LangChain4jAgentEmitTest` | Fast, isolated | No full router/SSE/GET integration |

**Decision needed:** which option is normative for WI-351 acceptance?

---

## 2. `MetadataReadPort` and `applicableTo`

**Gap:** Story requires `validate_facet_payload(facetType, payload [, metadataEntityId])` for **applicableTo** checks. Current port:

```kotlin
fun validateFacetPayload(facetTypeKey: String, payload: Map<String, Any?>): List<String>
```

**WI-346** scopes classpath/manifest validation only — no target entity argument.

| Option | Owner |
|--------|--------|
| Extend **`MetadataReadPort`** with optional `metadataEntityId` | WI-346 or WI-347 |
| Keep port unchanged; applicability only in **`MetadataCapabilities`** handler (resolve entity kind from URN locally) | WI-347 |

**Decision needed:** port contract vs handler-only; assign to WI-346 or WI-347 explicitly.

---

## 3. P1 tools — no owning WI

From STORY § Tool gaps — not in any WI acceptance criteria:

| Item | Purpose |
|------|---------|
| **`build_metadata_entity_urn`** | Catalog path → canonical `metadataEntityId` |
| **`get_facet_type`** | Full `contentSchema` for one type (if filtered list insufficient) |
| **`list_metadata_scopes`** | Expose assignable scopes (at minimum global) |

**Decision needed:** in-story (WI-347), design-only deferral, or follow-up story?

---

## 4. P1 catalog data — no owning WI

| Item | Purpose |
|------|---------|
| **`examplePayload` / `examples[]`** on facet type YAML seeds | Few-shot per type (especially DQ, relation) |
| **Category index** in `metadata-authoring.reasoning` | Short map descriptive / relation / dq → `list_facet_types` |

**Decision needed:** required for v1 (WI-347 / metadata seeds) or follow-up?

---

## 5. `scopeUrn` and `mergeAction` on capture

**Gap:** STORY P0 proposes optional **`scopeUrn`** (default global) and **`mergeAction`** (default `SET`) on `propose_facet_assignment` and artefact JSON. **WI-347** does not list them.

Domain has merge semantics; story capture is proposal-only (no metadata service write).

**Decision needed:** include in WI-347 artefact body for downstream persist (M-23), or defer until write workflow exists?

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

## 17. `list_facet_types` payload size / truncation

**Gap:** STORY P0 — summary by default, full `contentSchema` when `facetTypeKey` filter set. Not in WI-347 acceptance criteria as explicit behaviour.

**Decision needed:** lock truncation/summary rules in WI-345 design + WI-347 tests (catalog with 15+ DQ types).

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
| 1 | WI-351 test vehicle | A legacy capture ×2 / B test capability / C unit mock |
| 2 | `applicableTo` validation | Extend port / handler-only |
| 3–4 | P1 tools + seed examples | In WI-347 / defer |
| 5 | `scopeUrn` / `mergeAction` | WI-347 / defer to M-23 |
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
