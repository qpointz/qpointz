# WI-319 — Ephemeral artifact routing

## Cold start

| Field | Value |
|-------|--------|
| **Story** | [STORY.md](STORY.md) — seq **4** (may start after WI-317 on same branch) |
| **Depends on** | None strict; coordinate **`chat_id`** column name if WI-317 merged first |
| **Commit** | `[feat] WI-319: ephemeral artifact routing with persist flag` |

### Verify when done

```bash
./gradlew :ai:mill-ai:test --tests "*AgentEventRouter*" --tests "*StandardPersistenceProjector*"
./gradlew :ai:mill-ai-test:testIT
```

### Primary files to touch

| Area | Path |
|------|------|
| Descriptor | `ai/mill-ai/.../ArtifactDescriptor.kt` — `persist: Boolean = true` |
| YAML parse | `ai/mill-ai/.../CapabilityManifest.kt` |
| Router | `ai/mill-ai/.../RegistryAgentEventRouter.kt` — skip ARTIFACT when `persist == false` |
| Capability YAML | `ai/mill-ai/src/main/resources/capabilities/sql-query.yaml` |
| Do **not** change | `UnifiedChatService.attachExecutionResult` |
| Tests | `AgentEventRouterRegistryTest.kt`, `StandardPersistenceProjectorTest.kt`, `mill-ai-test` baselines |

### Implementation checklist

1. Add `persist` to model + YAML loader.
2. Router excludes durable artifact lane when `persist: false`.
3. Update `sql-query.yaml` (`sql-validation`, `sql-result` ephemeral; `generated-sql` durable).
4. Update tests/baselines.
5. Mark `[x]` in [STORY.md](STORY.md).

---

## Goal

Stop persisting session-only artifacts to `ai_artifact`. Runtime/tool paths for `sql.validation` and tool-emitted `sql.result` remain available on live SSE where configured, but do not append durable rows. Durable kinds (`sql.generated`, facet/schema capture, client attach `sql.result`) unchanged.

Closes part of story goal #3. Can run in parallel with WI-317/318 on the story branch.

## Design

Add **`persist: Boolean`** (default `true`) to [`ArtifactDescriptor`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/core/artifact/ArtifactDescriptor.kt) and YAML parsing in [`CapabilityManifest`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/core/capability/CapabilityManifest.kt).

In [`RegistryAgentEventRouter`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/runtime/events/RegistryAgentEventRouter.kt), when `persist == false`:

- Keep `CHAT_STREAM` (and `TELEMETRY` if applicable).
- Exclude `ARTIFACT` from effective destinations **or** set `persistAsArtifact = false`.

[`StandardPersistenceProjector`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/persistence/StandardPersistenceProjector.kt) unchanged if router never routes ephemeral events to artifact lane.

## YAML updates ([`sql-query.yaml`](../../../../ai/mill-ai/src/main/resources/capabilities/sql-query.yaml))

```yaml
sql-validation:
  persist: false
  destinations: []

sql-result:
  persist: false
  destinations: [CHAT_STREAM]

generated-sql:
  persist: true
  destinations: [CHAT_STREAM, ARTIFACT]
```

## Do not change

[`UnifiedChatService.attachExecutionResult`](../../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/UnifiedChatService.kt) — client Run attach remains durable for GET replay.

## Tests

- [`AgentEventRouterRegistryTest`](../../../../ai/mill-ai/src/test/kotlin/io/qpointz/mill/ai/runtime/events/AgentEventRouterRegistryTest.kt): ephemeral descriptors do not set `persistAsArtifact`.
- [`StandardPersistenceProjectorTest`](../../../../ai/mill-ai/src/test/kotlin/io/qpointz/mill/ai/persistence/StandardPersistenceProjectorTest.kt): `sql.validation` tool result does not call `artifactStore.save`.
- Update `mill-ai-test` scenario baselines if artifact counts change.

## Optional (defer if risky)

- SQL migration or admin script to purge historical `sql.validation` / tool `sql.result` rows.

## Acceptance

- After a SQL turn: `ai_artifact` contains `sql.generated` (and attach rows after Run) but **not** new `sql.validation` or tool `sql.result` rows.
- Artifact rows use **`chat_id`** column name after WI-317 migration (update tests/queries if they reference `conversation_id`).
- `./gradlew :ai:mill-ai:test` and relevant `:ai:mill-ai-test:testIT` pass.

## Modules

- `ai/mill-ai`
- `ai/mill-ai-test` (baseline updates)
