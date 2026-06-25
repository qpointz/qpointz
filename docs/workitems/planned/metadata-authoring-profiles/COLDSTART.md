# Cold start — metadata-authoring-profiles

**Audience:** agent or developer picking up this story with no prior chat context.  
**Delivery:** **3 stages** — one branch + MR per stage, **multiple WIs per stage** (see [`STORY.md`](STORY.md) § Staged delivery).  
**Status:** **planning complete — docs updated 2026-06-25; no implementation yet**  
**Milestone:** 0.8.0 (tentative)

## What this story does

**Primary:** Catalog-generic **metadata facet authoring** — LLM uses `list_facet_categories` → `list_facet_types` (reasoning) → `get_facet_type` (generation) → `validate_facet_payload` → `propose_facet_assignment` for **any** facet type. No `capture_<facet>` tools.

**Secondary:** **`MetadataContent`** entity (**WI-356**), YAML **agent profiles**, real **`MetadataReadPort`**, and **facet lifecycle** — chat-scope assign + Accept/Reject via **`mill-events`** (**WI-360**).

**Platform prerequisite:** **WI-355** multi-artifact batch `ProtocolFinal` + fan-out must land **before** facet capability rework (**WI-359**).

## Read order

1. **This file** — stages, WI sequence, file map, rules
2. [`STORY.md`](STORY.md) — locked architectural decisions, staged delivery, prompt enforcement
3. [`GAPS.md`](GAPS.md) — locked + open decisions
4. [`WI-354-metadata-authoring-design-contract.md`](WI-354-metadata-authoring-design-contract.md) — normative design contract (stage **1**, first WI; *file rename pending*)
5. Current WI file for the stage you are implementing

## Preconditions (already on `dev` / completed stories)

| Story | Relevance |
|-------|-----------|
| [`ai-facet-catalog-inference`](../../completed/20260428-ai-facet-catalog-inference/STORY.md) | `metadata` + `metadata-authoring` capabilities, `propose_facet_assignment` |
| [`dqm-metadata-facets`](../../completed/20260624-dqm-metadata-facets/STORY.md) | 15 DQ facet types in platform seeds |
| [`ai-chat-facet-display`](../../completed/20260619-ai-chat-facet-display/STORY.md) | `facet-proposal` UI shell |

## Branch setup (example — stage 1)

```bash
git fetch origin
git checkout -b feat/meta-authoring-platform origin/dev
# WI-354 → WI-355 → WI-356 → WI-358 (one commit each); squash; push; MR
```

After stage 1 MR merged:

```bash
git checkout -b feat/meta-authoring-catalog origin/dev   # stage 2: WI-357 → WI-359
```

**General RULES still apply:** complete working copy per WI, `[x]` tracker after each WI, commit prefixes, no `Co-Authored-By`, CI check when GitLab MCP available, story closure explicit only.

**This story adds:**

| Step | Action |
| ---- | ------ |
| 1 | **Branch per stage** from `origin/dev` (after prior stage MR **merged**) |
| 2 | Implement **all WIs in stage order** (see table) — **one commit per WI** |
| 3 | **`[x]` in STORY.md** after each WI; first `[x]` → `planned/` → `in-progress/` |
| 4 | **Verify** — all merge-gate commands for the stage |
| 5 | **Squash** to 2–4 logical commits (or one if small); **push** |
| 6 | **MR** → `dev` — list all stage WIs; **wait for merge** before next stage |

## Staged WI sequence (3 stages)

| Stage | Branch | WIs (order) | Focus |
|-------|--------|-------------|-------|
| **1** | `feat/meta-authoring-platform` | WI-354 → WI-355 → WI-356 → WI-358 | Design, batch/SSE runtime, `MetadataContent`, YAML profiles |
| **2** | `feat/meta-authoring-catalog` | WI-357 → WI-359 | `MetadataReadPort`, catalog-generic tools + prompts |
| **3** | `feat/meta-authoring-lifecycle` | WI-360 → WI-361 → WI-362 | Events, Accept/Reject, remove `capture_*`, e2e + docs |

**Do not start stage 2 until stage 1 MR merged** (WI-359 needs WI-355). **Do not start WI-359 before WI-357** on stage 2 branch.

## Story folder map

```
docs/workitems/planned/metadata-authoring-profiles/
  COLDSTART.md
  STORY.md
  GAPS.md
  WI-354-*.md … WI-362-*.md   # renumber from WI-345…353 pending
```

## Key code today (before story)

| Area | Path | Current state |
|------|------|----------------|
| Metadata QUERY tools | `ai/mill-ai/src/main/resources/capabilities/metadata.yaml` | 3 tools; weak system prompt |
| Metadata CAPTURE | `ai/mill-ai/.../metadata-authoring.yaml` | `propose_facet_assignment` only |
| Legacy capture | `ai/mill-ai/.../schema-authoring.yaml` | `capture_description`, `capture_relation` |
| Agent loop | `ai/mill-ai/.../LangChain4jAgent.kt` | Production runtime; multi-capture gap |
| Dead agent | `ai/mill-ai/.../SchemaExplorationAgent.kt` | **Delete in WI-355** (§14) — no callers |
| Empty port | `ai/mill-ai/.../EmptyMetadataReadPort.kt` | `listFacetTypes()` → `[]` on mill-service |
| Profiles | `ai/mill-ai/.../profile/*AgentProfile.kt` | Compile-time Kotlin objects |
| Chat UI | `ui/mill-ui/.../FacetCondensedPreview.tsx` | Read-only preview; Accept/Reject stub (`enabledActions={[]}`) |
| Facet seeds | `metadata/.../platform-*.yaml` | descriptive, relation, DQ — **no MetadataContent** |

## Normative tool matrix (target)

| Tool | Capability |
|------|------------|
| `list_facet_categories` | `metadata` |
| `list_facet_types` | `metadata` (summary) |
| `get_facet_type` | `metadata` (full schema + examples[]) |
| `list_content` / `get_content` | `metadata` |
| `list_metadata_scopes` | `metadata` |
| `list_entity_facets` | `metadata` |
| `validate_facet_payload` | `metadata` |
| `propose_facet_assignment` | `metadata-authoring` |

**Forbidden:** any `capture_<specific facet>` tool.

## Locked gaps (2026-06-25)

| § | Topic |
|---|--------|
| 1 | WI-351 mock LLM + L1–L6 |
| 2 | `validateFacetPayload(..., metadataEntityId?)` |
| 3b | `list_facet_types` + `get_facet_type` |
| 3c | `list_metadata_scopes` with **`access`** flags; `writeScopeUrns[]` on artefact |
| **4** | **`MetadataContent`** — WI-352 |
| **5** | Capture-time scope assign + **`FacetProposalMerger`** on **`artifact.facet.persisted`** (§5, §23) — **WI-353** |
| **23** | Event bus: **`artifact.facet.persisted`** + kind-routed **`artifact.retracted`** (**WI-353**) |
| **6** | Relation keys: `applicableTo` + table role (§6) |
| **7** | `schema-authoring` capability removed (§7) |
| **8** | Profile id `schema-authoring` deprecated (§8) |
| **9** | Partial batch failure — emit all successes (§9) |
| **10** | Mixed SQL + facets per turn (§10) |
| **11** | `FacetProposalWire` — leave as-is; no legacy replay compat (§11) |
| **12** | Harness catalog expansion — **WI-346**; WI-349 scenario packs only (§12) |
| **13** | Design docs under `docs/design/` — hub `metadata-facet-catalog-v3.md` (§13) |
| **14** | Delete `SchemaExplorationAgent.kt`; `LangChain4jAgent` only (§14) |
| **15** | Multi-artifact first-class: list pointers + GET hydration (**WI-351**, §15) |
| **16** | SSE multi-part + `item.completed` multi hint; UI Vitest (**WI-351**, §16) |
| **18** | MCP profile-driven; all new tools enabled; inventory doc **WI-349** (§18) |
| **22** | No plural batch tool — parallel `propose_facet_assignment` + batch `ProtocolFinal` (§22) |
| 21 | Batch envelope mandatory at story close |

## Verify commands (full story)

```bash
./gradlew :metadata:mill-metadata-core:test --tests "*MetadataContent*"
./gradlew :ai:mill-ai:test --tests "*Metadata*"
./gradlew :ai:mill-ai:test --tests "*SchemaAuthoring*"
./gradlew :ai:mill-ai:test --tests "*Profile*"
./gradlew :ai:mill-ai-data:test --tests "*Metadata*"
./gradlew :ai:mill-ai-service:testIT --tests "*Artifact*"
./gradlew :ai:mill-ai-service:testIT --tests "*metadata*"
./gradlew :core:mill-events:test
./gradlew :ai:mill-ai-test:test --tests "*facet*"
./gradlew :ui:mill-ui:test
```

## WI workflow (summary)

See **WI workflow (staged)** above — branch per stage, tracker after each WI, squash + push + MR per stage.

## Open decisions (still in GAPS)

**None** — planning gaps **§1–§18, §21–§22 locked**; §19–§20 resolved (doc hygiene). Ready for staged implementation on user request.

## Related design docs ([`GAPS.md`](GAPS.md) §13)

| Doc | WI | Role |
|-----|-----|------|
| [`metadata-facet-catalog-v3.md`](../../../design/agentic/metadata-facet-catalog-v3.md) | WI-345 outline → WI-349 rewrite | **Canonical** authoring hub |
| [`metadata-content.md`](../../../design/metadata/metadata-content.md) | WI-345 skeleton → WI-352 | Domain entity |
| [`ai-v3-chat-metadata-scope.md`](../../../design/agentic/ai-v3-chat-metadata-scope.md) | WI-353 | Scope + Accept/Reject lifecycle |
| [`artifact-foundation.md`](../../../design/agentic/artifact-foundation.md) | WI-351 | Batch `ProtocolFinal` |
| [`general-event-bus.md`](../../../design/platform/general-event-bus.md) | WI-353 | Event type catalog note |

## Related

- [`GAPS.md`](GAPS.md) — locked gaps + remaining open items
- [`COLDSTART.md`](COLDSTART.md)

## Common pitfalls

- Starting **WI-359** before **WI-355** is merged — multi-facet capture will not persist/stream correctly.
- Putting **`examples[]`** on `FacetTypeDefinition` — use **`MetadataContent`** (WI-356).
- LLM passing **`scopeUrn`** on capture — runtime sets **`writeScopeUrns[]`** from context.
- Treating “column X not null” as SQL — use **`dq-null-check`** when `metadata-authoring` is active.
