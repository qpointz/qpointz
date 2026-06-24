# WI-348 — YAML agent profiles, resource-backed registry, and seed config

Status: `planned`  
Type: `✨ feature`  
Area: `ai`  
Depends on: [WI-345](WI-345-metadata-authoring-design-contract.md)

## Problem Statement

Agent profiles are **compile-time Kotlin objects** (`DataAnalysisAgentProfile`, etc.) with only
`id` + `capabilityIds`. Operators cannot add or adjust profiles without a code change. This
does not match how metadata is seeded (`mill.metadata.seed.resources` + multi-document YAML with
`kind:`) and blocks a future **unified Mill seeding** model.

## Goal

Define agent profiles in **YAML**, load them through a **resource-backed** [`ProfileRegistry`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/profile/ProfileRegistry.kt),
and seed defaults via **`mill.ai.profiles.seed.resources`** at startup.

## In Scope

1. **YAML document format** (multi-document file, `---` separators — same convention as
   [`skymill-canonical.yaml`](../../../../test/datasets/skymill/skymill-canonical.yaml) and metadata seeds):
   ```yaml
   kind: AgentProfile
   id: schema-exploration
   description: Read-only schema and facet catalog exploration
   capabilities:
     - conversation
     - schema
     - metadata
   ---
   kind: AgentProfile
   id: metadata-authoring
   description: Facet assignment proposals without schema relation capture or SQL
   capabilities:
     - conversation
     - schema
     - metadata
     - metadata-authoring
   ```
   - **`kind: AgentProfile`** is required per document; unknown `kind` values are skipped (forward-compatible with a future cross-type seed runner — **out of scope** for this story).
   - **`id`** and **`capabilities`** required; **`description`** optional (maps to [`AgentProfile`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/profile/AgentProfile.kt)).
2. **`AgentProfile` model** — add `description: String? = null`; keep `routingPolicy` as runtime default (not YAML in this WI).
3. **`ResourceProfileRegistry`** (or equivalent) in `mill-ai`:
   - Factory / constructor accepting ordered `Resource` or location strings
   - Parses all documents; merges profiles by `id` (later resource wins on duplicate id)
   - Implements `ProfileRegistry`
4. **Platform seed file** — `ai/mill-ai/src/main/resources/profiles/platform-agent-profiles.yaml` containing migrated profiles:
   - `hello-world`, `data-analysis`, `schema-exploration`, `schema-authoring`, **`metadata-authoring`** (new)
5. **Configuration** — `mill.ai.profiles.seed.resources` (Java `@ConfigurationProperties`, mirror
   [`MetadataSeedProperties`](../../../../metadata/mill-metadata-autoconfigure/src/main/java/io/qpointz/mill/metadata/configuration/MetadataSeedProperties.java)):
   - Ordered Spring resource locations
   - Default: `classpath:profiles/platform-agent-profiles.yaml`
   - Bound under `mill.ai` (not `mill.ai.chat`) — profiles are runtime-wide (chat + MCP)
6. **`mill-ai-autoconfigure`** — build `ProfileRegistry` bean from seed properties + `ResourceLoader`;
   replace `DefaultProfileRegistry` bean when seeds configured; retain empty-registry fallback for tests
7. **Retire Kotlin profile objects** — remove `*AgentProfile.kt` objects; tests load YAML from classpath
8. **`AgentProfileResponse`** — expose optional `description` on `GET /api/v1/ai/profiles`
9. **`ProfileCapabilityMatrixTest`** — load registry from platform YAML; assert `metadata-authoring` tool matrix

## Out of Scope

- Unified Mill-level seed orchestration across metadata / AI / other `kind` types (future story; `kind` field is the hook)
- `displayName`, `tags`, routing policy in YAML
- Ledger / fingerprint / re-import semantics (metadata seed ledger pattern)
- Changing `mill.ai.chat.default-profile` default in all editions (document only)
- Dynamic profile hot-reload (**A-74**)

## Acceptance Criteria

- [ ] Platform YAML defines **5** profiles including **`metadata-authoring`**
- [ ] `ProfileRegistry` bean loads from `mill.ai.profiles.seed.resources` on mill-service boot
- [ ] `GET /api/v1/ai/profiles` returns `description` when present
- [ ] Duplicate profile `id` across seed files: later resource overrides (unit test)
- [ ] Unknown `kind` in a seed file does not fail profile load (skipped or ignored per design doc)
- [ ] Matrix tests pass without Kotlin profile objects

## Suggested commit

`[feat] WI-348: YAML agent profiles and mill.ai.profiles seed resources`
