# WI-304 — ArtifactEmissionCoordinator

Status: `done`  
Type: `✨ feature` / `🐛 fix`  
Area: `ai`  
Story: [`STORY.md`](STORY.md)

## Depends on

- **WI-303** (`ArtifactDescriptorRegistry`).

## Goal

Fix the SQL-as-prose bug: after successful `validate_sql`, emit `sql-query.generated-sql` as `ProtocolFinal` **without** an extra model round-trip. Migrate CAPTURE path to registry-driven `OnCaptureSuccess`.

## Payload source rules (normative — see STORY.md)

| Strategy | Behaviour |
|----------|-----------|
| **OnToolSuccess** (`validate_sql` → `generated-sql`) | Coordinator **constructs** `ProtocolFinal` payload from tool result + run context (`normalizedSql`, dialect id, `statementKind`). **Does not** call `LangChain4jProtocolExecutor`. |
| **OnCaptureSuccess** (`propose_facet_assignment` → `inferred-facet`) | Delegate to **`LangChain4jProtocolExecutor`** (existing CAPTURE path); model synthesizes structured JSON. |

## Duplicate-artifact prevention

| Persist kind | Source | Rule |
|--------------|--------|------|
| `sql.validation` | `ToolResult` (`artifactType: sql-validation`) | Always persisted via router `sourceEvent: tool.result` |
| `sql.generated` | `ProtocolFinal` only | Coordinator emits once; router **must not** also persist `generated-sql` from tool result when descriptor says `sourceEvent: protocol.final` |

Remove or gate legacy [`AgentEventRouter`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/runtime/events/AgentEventRouter.kt) mapping of tool-result `artifactType: generated-sql` → `sql.generated` once coordinator owns emission.

## Deliverables

- [ ] `ArtifactEmissionCoordinator` in `io.qpointz.mill.ai.runtime.langchain4j` (or adjacent package)
- [ ] `constructProtocolFinal(descriptor, toolResult, runState)` for `OnToolSuccess`
- [ ] Integrate into [`LangChain4jAgent.kt`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/runtime/langchain4j/LangChain4jAgent.kt):
  - After QUERY tool batch: evaluate `OnToolSuccess` triggers → emit `ProtocolFinal` directly
  - Refactor CAPTURE branch to registry `OnCaptureSuccess` → existing `LangChain4jProtocolExecutor`
- [ ] Inject coordinator + registry via agent factory / autoconfigure

**Tests (`:ai:mill-ai:test`):**

- [ ] `ArtifactEmissionCoordinatorTest` — direct payload construction from `validate_sql` result
- [ ] `LangChain4jAgentEmitTest` — scripted model; assert single `sql.generated` artefact + one `sql.validation`
- [ ] Negative: failed `validate_sql` does not emit `generated-sql`

## Acceptance criteria

- [ ] `validate_sql` success → `ProtocolFinal(sql-query.generated-sql)` with payload from `normalizedSql` — **zero** protocol-executor model calls.
- [ ] Same turn persists `sql.validation` (tool result) **and** `sql.generated` (protocol final) — distinct kinds, not duplicates.
- [ ] `propose_facet_assignment` CAPTURE still emits `metadata.faceting.capture` via executor + registry.
- [ ] `./gradlew :ai:mill-ai:test` passes.
