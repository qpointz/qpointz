# WI-303 — ArtifactDescriptorRegistry

Status: `planned`  
Type: `✨ feature`  
Area: `ai`  
Story: [`STORY.md`](STORY.md)

## Depends on

- **WI-300** (emit contract design + canonical descriptor schema).
- **WI-302** recommended (harness ready to add emit scenario stubs).

## Goal

Load artefact descriptors from capability YAML — **one canonical shape** consumed by coordinator (WI-304), router, SSE bridge (WI-305), and persistence projection.

## Canonical `ArtifactDescriptor` (accept before implementation)

| Field | Purpose |
|-------|---------|
| `id` | Descriptor key within capability (e.g. `generated-sql`) |
| `protocolId` | Protocol invoked or synthesized (e.g. `sql-query.generated-sql`) |
| `artifactKind` | Logical kind for checks / UI (e.g. `generated-sql`) |
| `persistKind` | Persistence bucket (e.g. `sql.generated`) |
| `pointerKeys` | Active pointer names (e.g. `[last-sql]`) |
| `wirePartType` | SSE / structured part type (e.g. `sql`, `facet-proposal`) |
| `presentation` | `structured` \| `conversation` |
| `protocolMode` | `STRUCTURED_FINAL` \| … |
| `sourceEvent` | `tool.result` \| `protocol.final` — which event type drives routing/persistence |
| `emissionStrategy` | `OnToolSuccess` \| `OnCaptureSuccess` \| `FromToolResult` |
| `destinations` | `CHAT_STREAM`, `ARTIFACT`, … |

Registry API examples:

- `descriptorForProtocol(protocolId)` → routing + SSE
- `descriptorForToolResultArtifactType(artifactType)` → legacy tool-result path
- `emitTriggersForTool(toolName)` → coordinator

## Deliverables

**Extend [`CapabilityManifest.kt`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/core/capability/CapabilityManifest.kt):**

- [ ] YAML `artifacts:` block per capability (fields above)
- [ ] Tool `emitsOnSuccess:` (artifact id + `when` predicate on tool result fields)

**New package `io.qpointz.mill.ai.core.artifact`:**

- [ ] `ArtifactDescriptor`, `EmissionStrategy` sealed hierarchy
- [ ] `ArtifactDescriptorRegistry` — aggregate from all loaded capability manifests; reject duplicate `persistKind` + `sourceEvent` for same logical artefact
- [ ] Spring bean wiring in [`AiV3AutoConfiguration`](../../../../ai/mill-ai-autoconfigure/src/main/kotlin/io/qpointz/mill/ai/autoconfigure/AiV3AutoConfiguration.kt)

**Tests (`:ai:mill-ai:test`):**

- [ ] `ArtifactDescriptorRegistryTest` — `sql-query`, `metadata-authoring` descriptors load with full field set
- [ ] Schema validation test — missing required field fails manifest load

## Acceptance criteria

- [ ] **Descriptor schema frozen** in design doc before WI-304 starts; WI-303 PR includes schema acceptance test listing all required fields.
- [ ] Registry resolves `generated-sql` (`sourceEvent: protocol.final`) and `sql-validation` (`sourceEvent: tool.result`) as **distinct** descriptors.
- [ ] `inferred-facet` resolves with `OnCaptureSuccess` + `protocol.final`.
- [ ] No hardcoded Kotlin routing tables needed for new descriptors added in YAML only (verified in WI-305).
