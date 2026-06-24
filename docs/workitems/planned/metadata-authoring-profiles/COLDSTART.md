# Cold start — metadata-authoring-profiles

**Audience:** agent or developer picking up this story with no prior chat context.  
**Branch:** `feat/meta-capability-improve` (rebase on `origin/dev` before MR).  
**Status:** **planning complete — no implementation committed yet** (story folder was untracked until handover commit).  
**Milestone:** 0.8.0 (tentative)

## What this story does

**Primary:** Catalog-generic **metadata facet authoring** — LLM uses `list_facet_types` (reasoning) → `get_facet_type` (generation) → `validate_facet_payload` → `propose_facet_assignment` for **any** facet type (not only `descriptive`). No `capture_<facet>` tools.

**Secondary:** YAML **agent profiles** (`kind: AgentProfile`), `mill.ai.profiles.seed.resources`, real **`MetadataReadPort`** on mill-service.

**Platform prerequisite:** **WI-351** multi-artifact batch `ProtocolFinal` + fan-out must land **before** facet capability rework (**WI-347**).

## Read order

1. **This file** — branch, WI sequence, file map, rules
2. [`STORY.md`](STORY.md) — locked architectural decisions, prompt enforcement, multi-facet batch
3. [`GAPS.md`](GAPS.md) — **22 open decisions** — resolve or assign to WIs before/during implementation
4. [`WI-345-metadata-authoring-design-contract.md`](WI-345-metadata-authoring-design-contract.md) — normative design contract (first WI to implement as docs)
5. Current WI file in sequence below

## Branch setup

```bash
git fetch origin
git checkout feat/meta-capability-improve
# if starting fresh from dev:
# git checkout -b feat/meta-capability-improve origin/dev
```

Working directory: **repository root** (`./gradlew`).

## Preconditions (already on `dev` / completed stories)

| Story | Relevance |
|-------|-----------|
| [`ai-facet-catalog-inference`](../../completed/20260428-ai-facet-catalog-inference/STORY.md) | `metadata` + `metadata-authoring` capabilities, `propose_facet_assignment` |
| [`dqm-metadata-facets`](../../completed/20260624-dqm-metadata-facets/STORY.md) | 15 DQ facet types in platform seeds |
| [`ai-chat-facet-display`](../../completed/20260619-ai-chat-facet-display/STORY.md) | `facet-proposal` UI shell (`FacetCondensedPreview`) |

## WI sequence (implement in this order)

| Seq | WI | Type | Depends on | Summary |
|-----|-----|------|------------|---------|
| 1 | [WI-345](WI-345-metadata-authoring-design-contract.md) | docs | — | Design contract under `docs/design/agentic/` |
| 2 | [WI-351](WI-351-multi-artifact-protocol-runtime.md) | feat | WI-345 | **Batch `ProtocolFinal` `{ results[] }`**, agent aggregation, persist/SSE fan-out, UI N-cards — **blocks WI-347** |
| 3 | [WI-348](WI-348-agent-profiles-metadata-authoring.md) | feat | WI-345 | YAML profiles, `ResourceProfileRegistry`, `mill.ai.profiles.seed.resources` |
| 4 | [WI-346](WI-346-metadata-read-port-adapter.md) | feat | WI-345 | In-process `MetadataReadPort` in `mill-ai-data` (replaces `EmptyMetadataReadPort`) |
| 5 | [WI-347](WI-347-metadata-authoring-capability.md) | feat | **WI-351**, WI-346 | Catalog tools, validators, facet prompts (`intent`, `reasoning`, `batch`) |
| 6 | [WI-350](WI-350-schema-authoring-description-tool-cleanup.md) | refactor | WI-347, WI-351 | Remove all `capture_*` tools; single `propose_facet_assignment` path |
| 7 | [WI-349](WI-349-metadata-authoring-tests-docs.md) | test/docs | WI-350 | Skymill IT, scenario packs, public docs |

**Do not start WI-347 until WI-351 is done** (see [`GAPS.md`](GAPS.md) §1 for WI-351 test strategy).

## Story folder map

```
docs/workitems/planned/metadata-authoring-profiles/
  COLDSTART.md          ← this file
  STORY.md              ← tracking checklist + architecture
  GAPS.md               ← open decisions for review
  WI-345-*.md … WI-351-*.md
```

## Key code today (before story)

| Area | Path | Current state |
|------|------|----------------|
| Metadata QUERY tools | `ai/mill-ai/src/main/resources/capabilities/metadata.yaml` | 3 tools; weak system prompt |
| Metadata CAPTURE | `ai/mill-ai/src/main/resources/capabilities/metadata-authoring.yaml` | `propose_facet_assignment`; one-line request prompt |
| Legacy capture | `ai/mill-ai/src/main/resources/capabilities/schema-authoring.yaml` | `capture_description`, `capture_relation`, intent/batch prompts |
| Agent loop | `ai/mill-ai/.../langchain4j/LangChain4jAgent.kt` | **Single** `captureBinding`; one `ProtocolFinal` per turn |
| SSE mapper | `ai/mill-ai/.../sse/AgentEventToSseMapper.kt` | Structured finals use `mode: replace` |
| Empty port | `ai/mill-ai/.../metadata/EmptyMetadataReadPort.kt` | `listFacetTypes()` → `[]` on mill-service |
| Profiles | `ai/mill-ai/.../profile/*AgentProfile.kt` | Compile-time Kotlin objects |
| Chat UI | `ui/mill-ui/.../artifactGroups.ts`, `MessageArtifactComposer.tsx` | **Already** renders N `facet-proposal` groups if `artifacts[]` has N rows |
| Facet seeds | `metadata/mill-metadata-core/src/main/resources/metadata/platform-*.yaml` | descriptive, relation, DQ L1/L2 |
| Design (stale) | `docs/design/agentic/metadata-facet-catalog-v3.md` | Pre-story; rewrite in WI-349 |

Production chat runtime: **`LangChain4jChatRuntime`** → **`LangChain4jAgent`** only (`SchemaExplorationAgent` is not the service path).

## Normative tool matrix (target)

| Tool | Capability |
|------|------------|
| `list_facet_types` | `metadata` (summary — reasoning) |
| `get_facet_type` | `metadata` (full schema — generation) |
| `list_entity_facets` | `metadata` |
| `validate_facet_payload` | `metadata` |
| `propose_facet_assignment` | `metadata-authoring` |

**Note:** `get_facet_type` registers on **`metadata`** — all profiles with that capability see it; authoring prompts own the list→get sequence ([`GAPS.md`](GAPS.md) §3b).

**Forbidden:** any `capture_<specific facet>` tool.

## Multi-facet / batch protocol (WI-351)

One user turn → N facet proposals → **one** batch `ProtocolFinal` with `results[]` → fan-out to **N** flat `facet-proposal` persist rows + N SSE parts. See STORY § *Protocol: batch ProtocolFinal*.

## New profile (WI-348)

```yaml
kind: AgentProfile
id: metadata-authoring
capabilities:
  - conversation
  - schema
  - metadata
  - metadata-authoring
```

No `sql-query`, `schema-authoring`, or `capture_*` tools.

Deploy default today: **`schema-authoring`** (`apps/mill-service/application.yml`) — see [`GAPS.md`](GAPS.md) §8.

## Verify commands (full story — before MR)

```bash
./gradlew :ai:mill-ai:test --tests "*Metadata*"
./gradlew :ai:mill-ai:test --tests "*SchemaAuthoring*"
./gradlew :ai:mill-ai:test --tests "*Profile*"
./gradlew :ai:mill-ai-data:test --tests "*Metadata*"
./gradlew :ai:mill-ai-service:testIT --tests "*metadata*"
./gradlew :ai:mill-ai-test:test --tests "*facet*"
./gradlew :ui:mill-ui:test   # after WI-351 UI assertions
```

## WI workflow (per [`RULES.md`](../../RULES.md))

1. Implement **one WI** at a time on `feat/meta-capability-improve`.
2. Run that WI's verify commands locally.
3. Mark WI `[x]` in [`STORY.md`](STORY.md); update WI file if needed.
4. **One commit per WI** — entire working tree for that WI (`[feat]` / `[docs]` / `[refactor]`).
5. Push branch; wait for CI before next WI (user merges to `dev`).

**Planning-only until user asks to implement:** this handover commit is **docs only**.

### Story closure (user request only)

- Squash/regroup commits (~10) per RULES  
- Update `MILESTONE.md`, `BACKLOG.md`  
- Move folder to `docs/workitems/completed/YYYYMMDD-metadata-authoring-profiles/`

## Open decisions — read before coding

All in [`GAPS.md`](GAPS.md). Priority items:

| # | Topic |
|---|--------|
| 1 | ~~WI-351 test vehicle~~ — **locked:** mock LLM 2× `propose_facet_assignment` + L1–L6 layer tests ([`GAPS.md`](GAPS.md) §1) |
| 2 | ~~`applicableTo`~~ — **locked:** extend `MetadataReadPort` with optional `metadataEntityId` ([`GAPS.md`](GAPS.md) §2) |
| 3 | ~~`list_facet_types` / `get_facet_type`~~ — **locked** ([`GAPS.md`](GAPS.md) §3b) |
| 3c | ~~**`list_metadata_scopes`**~~ — **locked:** Option B context-sensitive + empty-list caveat ([`GAPS.md`](GAPS.md) §3c) |
| 6 | Relation facet type key normative rule |
| 7–8 | `schema-authoring` capability vs `metadata-authoring` profile strategy |
| 9 | Partial batch failure semantics |
| 21 | Batch envelope mandatory at story close |

Resolve in GAPS or STORY before conflicting implementation.

## Out of scope (story)

- Metadata service **write** / approval (M-23)  
- M-32 admin UI  
- Unified Mill `kind` seed runner  
- `BACKLOG.md` promotion until user requests  

## Related design docs

| Doc | Role |
|-----|------|
| [`metadata-facet-catalog-v3.md`](../../../design/agentic/metadata-facet-catalog-v3.md) | Rewrite in WI-349 |
| [`artifact-foundation.md`](../../../design/agentic/artifact-foundation.md) | WI-351 batch section |
| [`chat-artefact-architecture.md`](../../../design/ai/chat-artefact-architecture.md) | N artefacts per turn |
| [`metadata-urn-platform.md`](../../../design/metadata/metadata-urn-platform.md) | Entity URNs |
| [`dq-rule-facet-types.md`](../../../design/metadata/dq-rule-facet-types.md) | DQ capture examples |
| [`schema-facet-ai-tool-field-mapping.md`](../../../design/metadata/schema-facet-ai-tool-field-mapping.md) | Field stereotypes |

## Common pitfalls

- Starting **WI-347** before **WI-351** — multi-facet capture will not persist/stream correctly.  
- Treating “column X not null” as SQL — use **`dq-null-check`** facet when `metadata-authoring` is active (prompt enforcement in WI-347).  
- Adding `capture_description` / `capture_relation` — **forbidden** after WI-350.  
- One composite artefact with embedded array — use **N** `facet-proposal` rows.  
- Assuming `SchemaExplorationAgent` is production chat — use **`LangChain4jAgent`**.  
- Editing `docs/workitems/BACKLOG.md` or archiving story without explicit user request.
