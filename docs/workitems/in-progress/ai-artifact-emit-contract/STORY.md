# ai-artifact-emit-contract — conversation replay harness + artefact emit

Implement a **YAML-driven conversation replay harness** in [`ai/mill-ai-test`](../../../../ai/mill-ai-test/) (greenfield; breaking changes to the existing skeleton), then a **capability YAML artefact emit contract** in [`ai/mill-ai`](../../../../ai/mill-ai/) so structured chat artefacts (SQL, inferred metadata facets) are emitted reliably — not only as prose in the message bubble.

**Branch:** `feat/ai-chat-artefacts`  
**Predecessor:** [`completed/20260506-ai-v3-mill-ui-general-chat`](../../completed/20260506-ai-v3-mill-ui-general-chat/STORY.md) (WI-231 transport seams; basic UI cards on `dev`).  
**Deferred:** `origin/feat/ai-chat-sql-result-view` — condensed/expand UI, GET replay wire, salvage; merge after emit is proven by scenario packs.

**Story folder:** [`docs/workitems/in-progress/ai-artifact-emit-contract/`](.) — harness foundation complete (WI-300–302).

## Strategy

1. **Harness first (WI-300–302)** — `ScenarioPack` YAML, scripted replay, extensible turn checks, **`ConversationRegressionRecord`** (full turn snapshots for regression diff).
2. **Emit contract (WI-303–306)** — `ArtifactDescriptorRegistry`, `ArtifactEmissionCoordinator`, registry-driven router/SSE; `data-analysis` profile + shared SQL descriptor.
3. **Acceptance (WI-307–308)** — POC scenario packs in `mill-ai-test` go green; baselines committed; supplementary service tests.
4. **Closure checklist (WI-309)** — reference only; **user-triggered** per [`RULES.md`](../../RULES.md) — not part of agent execution order.

Inspired by retired v1 integration: [`docs/design/ai/ai-v1-integration/README.md`](../../../design/ai/ai-v1-integration/README.md).

## Proof-of-concept

| POC | Profiles | Artefact | Capability source |
|-----|----------|----------|-------------------|
| **A — SQL** | `data-analysis`, `schema-authoring` | `generated-sql` → wire `partType: sql` | [`sql-query.yaml`](../../../../ai/mill-ai/src/main/resources/capabilities/sql-query.yaml) |
| **B — Inferred facet** | `schema-authoring` only | `inferred-facet` → wire `partType: facet-proposal` | [`metadata-authoring.yaml`](../../../../ai/mill-ai/src/main/resources/capabilities/metadata-authoring.yaml) |

Descriptors are **capability-owned**; profiles only select `capabilityIds` (cross-profile reuse without YAML duplication).

## Design decisions (normative)

### Emission payload source

| Strategy | Path | Payload source | Model call? |
|----------|------|----------------|-------------|
| **OnToolSuccess** | `validate_sql` → `generated-sql` | Coordinator **constructs** `ProtocolFinal` payload directly from tool result fields (`normalizedSql`, dialect from run context) per protocol `finalSchema` | **No** — does not invoke `LangChain4jProtocolExecutor` |
| **OnCaptureSuccess** | `propose_facet_assignment` → `inferred-facet` | **`LangChain4jProtocolExecutor`** (STRUCTURED_FINAL) synthesizes from message context | **Yes** — consumes a script step in harness |

### Tool-result vs protocol-final persistence

Both may exist for the same turn; they are **distinct persist kinds**, not duplicates:

| Source event | Persist kind | Role |
|--------------|--------------|------|
| `ToolResult` with `artifactType: sql-validation` | `sql.validation` | Validator audit trail (pass/fail, message, normalized SQL) |
| `ProtocolFinal` for `sql-query.generated-sql` | `sql.generated` | Canonical structured SQL for UI / pointers (`last-sql`) |

**No duplicate `sql.generated`:** coordinator emits **one** `ProtocolFinal`; router must **not** also map tool-result `artifactType: generated-sql` to `sql.generated` when coordinator already emitted. Registry `sourceEvent` field drives which path applies.

### Canonical descriptor shape (WI-303)

One YAML descriptor drives routing, SSE, persistence, and coordinator triggers:

`id`, `protocolId`, `artifactKind`, `persistKind`, `pointerKeys`, `wirePartType`, `presentation`, `protocolMode`, `sourceEvent` (`tool.result` | `protocol.final`), `emissionStrategy`, `destinations`

### Profile capability matrix

| Profile | Capabilities | Authoring tools excluded |
|---------|--------------|--------------------------|
| `data-analysis` | `conversation`, `schema`, `metadata`, `sql-dialect`, `sql-query` | No `metadata-authoring` → no `propose_facet_assignment` |
| `schema-authoring` | above + `metadata-authoring` | — |

“SQL-only POC” means **no metadata-authoring capability**, not a single capability in the profile.

### Baseline determinism

Normalized baselines scrub: UUIDs, `runId`, `chatId`, `turnId`, `eventId`, `artifactId`, wall-clock timestamps, token stats, `recordedAt`, `gitCommit`. Structural fields (event types, protocol ids, persist kinds, payloads, SSE partTypes) are preserved. See WI-301 / WI-307.

## Root problem

[`validate_sql`](../../../../ai/mill-ai/src/main/resources/capabilities/sql-query.yaml) is a **QUERY** tool; [`LangChain4jAgent`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/runtime/langchain4j/LangChain4jAgent.kt) auto-invokes protocols only after **CAPTURE** tools. The model usually answers with prose SQL → no `ProtocolFinal` → no structured artefact.

## Work Items

**Agent execution order** (WI-300–308):

- [x] **WI-300** — Design docs + story scaffolding (**[`WI-300-conversation-scenario-design.md`](WI-300-conversation-scenario-design.md)**)
- [x] **WI-301** — `ScenarioPack` loader, checks, regression record (**[`WI-301-scenario-pack-core.md`](WI-301-scenario-pack-core.md)**)
- [x] **WI-302** — `ScriptedAgentRunner` + harness smoke ITs (**[`WI-302-scripted-agent-runner.md`](WI-302-scripted-agent-runner.md)**)
- [ ] **WI-303** — `ArtifactDescriptorRegistry` (**[`WI-303-artifact-descriptor-registry.md`](WI-303-artifact-descriptor-registry.md)**)
- [ ] **WI-304** — `ArtifactEmissionCoordinator` (**[`WI-304-artifact-emission-coordinator.md`](WI-304-artifact-emission-coordinator.md)**)
- [ ] **WI-305** — Registry-driven router + SSE bridge (**[`WI-305-router-sse-bridge.md`](WI-305-router-sse-bridge.md)**)
- [ ] **WI-306** — Capability manifests + `data-analysis` profile (**[`WI-306-manifests-and-profiles.md`](WI-306-manifests-and-profiles.md)**)
- [ ] **WI-307** — POC scenario packs (primary acceptance) (**[`WI-307-poc-scenario-packs.md`](WI-307-poc-scenario-packs.md)**)
- [ ] **WI-308** — Supplementary unit/service IT (**[`WI-308-supplementary-tests.md`](WI-308-supplementary-tests.md)**)

**Closure checklist (not in execution order — user-triggered only):**

- [ ] **WI-309** — Story closure reference (**[`WI-309-story-closure.md`](WI-309-story-closure.md)**) — archive, MILESTONE, BACKLOG, squash **only when user explicitly requests closure** per [`RULES.md`](../../RULES.md)

## Scope

| In | Out |
|----|-----|
| Greenfield harness in **`ai/mill-ai-test`** | GET replay UI (`ArtifactWireMapper`) |
| `ConversationRegressionRecord` + baselines | mill-ui condensed/expand (sql-result-view branch) |
| Full emit registry + coordinator | Live-LLM YAML CI packs (optional later) |
| `data-analysis` profile | Server default profile change (`hello-world` stays) |
| POC scenario packs + shape checks | CLI `--scenario` batch mode |
| Design: [`ai-v3-conversation-scenarios.md`](../../../design/agentic/ai-v3-conversation-scenarios.md), [`artifact-emit-contract.md`](../../../design/agentic/artifact-emit-contract.md) | HTTP scenario runner (follow-up) |

## Verification (story done)

```bash
./gradlew :ai:mill-ai-test:test :ai:mill-ai-test:testIT
./gradlew :ai:mill-ai:test
```

- All `artifact-emit/*.yml` scenario packs pass.
- Each pack writes `ai/mill-ai-test/build/reports/scenarios/<pack>.record.json` with full turn outcomes.
- Committed baselines under `src/test/resources/scenarios/baselines/` match via comparator.
