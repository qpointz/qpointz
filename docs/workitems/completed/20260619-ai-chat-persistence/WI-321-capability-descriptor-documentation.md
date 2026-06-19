# WI-321 — Capability descriptor format documentation

## Cold start

| Field | Value |
|-------|--------|
| **Story** | [STORY.md](STORY.md) — seq **6** |
| **Depends on** | **WI-319** (`persist` field must exist in code before documenting) |
| **Commit** | `[docs] WI-321: capability YAML descriptor documentation` |

### Verify when done

- Read-through: new author can write a capability YAML using only `v3-capability-manifest.md` + one example file.
- No Gradle test required (docs-only); optional `./gradlew :ai:mill-ai:test` if parser drift fixes included.

### Primary files to touch

| Area | Path |
|------|------|
| Main doc | `docs/design/agentic/v3-capability-manifest.md` — §3.7 artifacts, §3.8 emitsOnSuccess |
| Cross-links | `artifact-foundation.md`, `artifact-emit-contract.md`, `agentic/README.md`, developer-manual |
| Code reference (read-only) | `CapabilityManifest.kt`, `ArtifactDescriptor.kt` |

### Implementation checklist

1. Document all `ArtifactEntryYaml` / descriptor fields including `persist`.
2. Add generic YAML example + pointers to production manifests.
3. Trim duplicate YAML in other docs → link to manifest §3.7.
4. Mark `[x]` in [STORY.md](STORY.md).

---

## Goal

Document the **overall** capability YAML descriptor format in one authoritative design doc. Authors should not need to read three scattered documents to write a new capability manifest.

**Depends on WI-319** — documents `persist` and final artifact routing fields. Part of story docs phase (WI-320 covers chat/persistence docs; this WI covers capability YAML).

## Problem

[`v3-capability-manifest.md`](../../../design/agentic/v3-capability-manifest.md) covers `name`, `description`, `prompts`, `tools`, `protocols` but **not** `artifacts:` or `tools.*.emitsOnSuccess`. Those fields are partially described in [`artifact-foundation.md`](../../../design/agentic/artifact-foundation.md) and [`artifact-emit-contract.md`](../../../design/agentic/artifact-emit-contract.md) with sql-query-centric examples.

## Scope

### Primary doc: extend `v3-capability-manifest.md`

**§3 top-level structure** — add optional blocks:

```yaml
artifacts:
  <descriptor-id>: ...

tools:
  <tool-name>:
    kind: query | capture
    protocol: <protocol-id>
    emitsOnSuccess:
      artifact: <descriptor-id>
      when:
        field: <field>
        equals: <value>
```

**§3.7 Artifact descriptor entry** — all fields from [`ArtifactEntryYaml`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/core/capability/CapabilityManifest.kt) / [`ArtifactDescriptor`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/core/artifact/ArtifactDescriptor.kt):

| Field              | Required              | Purpose                                               |
| ------------------ | --------------------- | ----------------------------------------------------- |
| `artifactKind`     | yes                   | Logical kind in payloads                              |
| `persistKind`      | yes                   | Persistence bucket / routed `kind`                    |
| `persist`          | no (default `true`)   | When `false`, session/SSE only — no `ai_artifact` row |
| `sourceEvent`      | yes                   | `tool.result` \| `protocol.final`                     |
| `emissionStrategy` | yes                   | `OnToolSuccess`, `OnCaptureSuccess`, `FromToolResult` |
| `destinations`     | yes                   | `CHAT_STREAM`, `ARTIFACT`, `TELEMETRY`, …             |
| `protocolId`       | when `protocol.final` | Protocol id                                           |
| `pointerKeys`      | no                    | Active pointer names                                  |
| `wirePartType`     | no                    | SSE `partType`                                        |
| `presentation`     | no                    | SSE presentation                                      |
| `protocolMode`     | no                    | e.g. `STRUCTURED_FINAL`                               |

Document load-time validation: `(persistKind, sourceEvent)` uniqueness; `protocolId` required for `protocol.final`; at least one destination.

**§3.8 Tool emit triggers** — `emitsOnSuccess` / `when` (coordinator path).

**§4 examples** — generic minimal artifact-capable example (not sql-query-specific) plus pointers to:

- [`conversation.yaml`](../../../../ai/mill-ai/src/main/resources/capabilities/conversation.yaml)
- [`schema-authoring.yaml`](../../../../ai/mill-ai/src/main/resources/capabilities/schema-authoring.yaml)
- [`metadata-authoring.yaml`](../../../../ai/mill-ai/src/main/resources/capabilities/metadata-authoring.yaml)
- [`test-full.yaml`](../../../../ai/mill-ai/src/test/resources/capabilities/test-full.yaml)

### Cross-links (reduce duplication)

- [`artifact-foundation.md`](../../../design/agentic/artifact-foundation.md) — runtime pipeline; link to manifest §3.7 for YAML authoring.
- [`artifact-emit-contract.md`](../../../design/agentic/artifact-emit-contract.md) — replace inline YAML with pointer to manifest §3.7.
- [`developer-manual/v3-developer-capabilities-profiles-and-dependencies.md`](../../../design/agentic/developer-manual/v3-developer-capabilities-profiles-and-dependencies.md) — schema reference link.
- [`agentic/README.md`](../../../design/agentic/README.md) — manifest doc as YAML authority.

## Out of scope

- Rewriting runtime/emit pipeline docs (artifact-foundation stays canonical for behavior).
- New capability YAML files (unless doc audit finds parser drift — fix in same WI if small).

## Acceptance

- New capability author can write valid YAML using **only** `v3-capability-manifest.md` + one production example.
- All `CapabilityManifestYaml` / `ArtifactEntryYaml` fields documented.
- `persist: false` semantics match WI-319 implementation.

## Modules

- `docs/design/agentic/` (docs only)
