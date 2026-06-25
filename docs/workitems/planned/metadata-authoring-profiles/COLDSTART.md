# Cold start — metadata-authoring-profiles

**Audience:** agent or developer picking up this story with **no prior chat context**.  
**Delivery:** **3 stages** — one branch + MR per stage, **multiple WIs per stage** (see [`PLAN.md`](PLAN.md) §2).  
**Status:** planning complete (2026-06-25); **no implementation yet**  
**Milestone:** 0.8.0 (tentative)

## Start here

| # | Document | Purpose |
| - | -------- | ------- |
| 1 | **This file** | Quick orientation |
| 2 | **[`PLAN.md`](PLAN.md)** | **Full handover** — stages, WIs, verify, locked GAPS, lifecycle + SSE notes |
| 3 | [`STORY.md`](STORY.md) | Locked architecture, WI tracker, staged delivery normative rules |
| 4 | [`GAPS.md`](GAPS.md) | All locked decisions with rationale |
| 5 | Current **WI-*.md** for the stage you implement |

## What this story does

**Primary:** Catalog-generic facet authoring — `list_facet_categories` → `list_facet_types` → `get_facet_type` → `validate_facet_payload` → `propose_facet_assignment`. No `capture_<facet>` tools.

**Secondary:** `MetadataContent` (**WI-356**), YAML profiles (**WI-358**), `MetadataReadPort` (**WI-357**), catalog tools (**WI-359**), multi-artifact batch (**WI-355**), facet lifecycle Accept/Reject (**WI-360**).

**Blocker:** WI-359 must not start until WI-355 (batch protocol) is merged via stage **1** MR.

## Three stages (summary)

| Stage | Branch | WIs | Focus |
| ----- | ------ | --- | ----- |
| **1** | `feat/meta-authoring-platform` | 354 → 355 → 356 → 358 | Design, batch/SSE, MetadataContent, profiles |
| **2** | `feat/meta-authoring-catalog` | 357 → 359 | ReadPort, catalog tools + prompts |
| **3** | `feat/meta-authoring-lifecycle` | 360 → 361 → 362 | Events, Accept/Reject, remove capture_*, e2e |

Detail, verify commands, dependency diagram: **[`PLAN.md`](PLAN.md)**.

## Branch setup (stage 1)

```bash
git fetch origin
git checkout -b feat/meta-authoring-platform origin/dev
```

Implement WIs in order; one commit per WI; `[x]` tracker after each; squash; push; MR → `dev`. After merge, stage 2:

```bash
git checkout -b feat/meta-authoring-catalog origin/dev
```

Full workflow: [`PLAN.md`](PLAN.md) §2 + [`STORY.md`](STORY.md) § Per-stage workflow + [`RULES.md`](../../RULES.md).

## WI files on disk (renumber pending)

| Planned ID | File (current name) |
| ---------- | ------------------- |
| WI-354 | [`WI-345-metadata-authoring-design-contract.md`](WI-345-metadata-authoring-design-contract.md) |
| WI-355 | [`WI-351-multi-artifact-protocol-runtime.md`](WI-351-multi-artifact-protocol-runtime.md) |
| WI-356 | [`WI-352-metadata-content-entity-and-seed.md`](WI-352-metadata-content-entity-and-seed.md) |
| WI-358 | [`WI-348-agent-profiles-metadata-authoring.md`](WI-348-agent-profiles-metadata-authoring.md) |
| WI-357 | [`WI-346-metadata-read-port-adapter.md`](WI-346-metadata-read-port-adapter.md) |
| WI-359 | [`WI-347-metadata-authoring-capability.md`](WI-347-metadata-authoring-capability.md) |
| WI-360 | [`WI-353-facet-artifact-lifecycle-events.md`](WI-353-facet-artifact-lifecycle-events.md) |
| WI-361 | [`WI-350-schema-authoring-description-tool-cleanup.md`](WI-350-schema-authoring-description-tool-cleanup.md) |
| WI-362 | [`WI-349-metadata-authoring-tests-docs.md`](WI-349-metadata-authoring-tests-docs.md) |

## Preconditions (completed stories on `dev`)

| Story | Relevance |
| ----- | --------- |
| [`ai-facet-catalog-inference`](../../completed/20260428-ai-facet-catalog-inference/STORY.md) | `metadata` + `metadata-authoring`, `propose_facet_assignment` |
| [`dqm-metadata-facets`](../../completed/20260624-dqm-metadata-facets/STORY.md) | 15 DQ facet types in seeds |
| [`ai-chat-facet-display`](../../completed/20260619-ai-chat-facet-display/STORY.md) | `facet-proposal` UI shell |

## Key code today (before story)

| Area | Path | Current state |
| ---- | ---- | ------------- |
| Metadata QUERY | `ai/mill-ai/.../capabilities/metadata.yaml` | 3 tools |
| Metadata CAPTURE | `ai/mill-ai/.../metadata-authoring.yaml` | `propose_facet_assignment` only |
| Legacy capture | `ai/mill-ai/.../schema-authoring.yaml` | `capture_description`, `capture_relation` |
| Agent loop | `ai/mill-ai/.../LangChain4jAgent.kt` | Single capture; multi-artifact gap |
| Dead agent | `ai/mill-ai/.../SchemaExplorationAgent.kt` | Delete in WI-355 |
| Empty port | `ai/mill-ai/.../EmptyMetadataReadPort.kt` | Returns `[]` on mill-service |
| Profiles | `ai/mill-ai/.../profile/*AgentProfile.kt` | Kotlin compile-time objects |
| Chat UI | `ui/mill-ui/.../FacetCondensedPreview.tsx` | Accept/Reject stub (`enabledActions={[]}`) |

## Open decisions

**None** — see [`GAPS.md`](GAPS.md). Ready for staged implementation on user request.

## Common pitfalls

See [`PLAN.md`](PLAN.md) §12.
