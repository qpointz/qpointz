# WI-327 — MCP Core Module (Catalog, Executor, Admission)

Status: `done`  
Type: `✨ feature`  
Area: `ai`  
Backlog refs: **A-56**  
Depends on: [WI-326](WI-326-external-capability-asset-descriptors.md)

## Problem Statement

The v3 capability registry (`CapabilityRegistry` + `ServiceLoader`) is in-process only. There is no
framework-free layer that projects discovered capabilities into an MCP-consumable catalog or
executes tools outside the LangChain4j agent loop.

## Goal

Create **`ai/mill-ai-mcp-core`** with catalog and executor services that map the existing capability
model to MCP tool/prompt/resource definitions and invoke `ToolBinding` handlers directly.

## In Scope

1. **Gradle module** `ai/mill-ai-mcp-core`:
   - `api(project(":ai:mill-ai"))` only — no Spring, no MCP SDK.
   - Register in root [`settings.gradle.kts`](../../../../settings.gradle.kts).
   - Add `io.modelcontextprotocol.sdk:mcp-bom` to [`libs.versions.toml`](../../../../libs.versions.toml)
     (version pin for transport modules; not a dependency of core).
2. **Capability manifest `mcp:` block** (in `mill-ai`):
   - Extend `CapabilityManifestYaml` with optional top-level `mcp:` section:
     ```yaml
     mcp:
       enabled: true   # default when section omitted
     ```
   - Kotlin type `CapabilityMcpSettings(enabled: Boolean = true)` — extensible for future fields.
   - Surface via `CapabilityManifest.mcpSettings` and `CapabilityDescriptor.mcp`.
3. **`CapabilityMcpCatalog`**:
   - Built from `CapabilityRegistry` (injected or `load()`) + `McpExposureConfig` + optional
     `AgentProfile` + `CapabilityAdmissionGate`.
   - **Does not** call `CapabilityProvider.create()` or read `Capability.tools` for listing.
   - For each eligible capability id, loads manifest metadata via
     `CapabilityManifest.load("capabilities/{capabilityId}.yaml")` (convention: `name:` matches
     descriptor id).
   - Uses manifest-only accessors (`declaredTools()`, `allPrompts`, `allProtocols`) plus
     `CapabilityDescriptor` for capability-level resources.
   - **MCP exposure filter:** omit capabilities where manifest `mcp.enabled == false`
     (default `true` when `mcp:` block absent).
   - **Server allowlist:** `McpExposureConfig.capabilities` — when non-empty, only listed capability ids
     (empty = no server restriction). Core type configured on mill-service (`mill.ai.mcp.*`); stdio bridge is transparent.
   - Lists tools (`{capabilityId}.{toolName}`) with **kind** (QUERY/CAPTURE) from manifest `kind`.
4. **`CapabilityMcpExecutor`**:
   - Constructed with the **same exposure view** as `CapabilityMcpCatalog` (shared
     `McpExposureConfig` + profile + admission gate) — **not** a raw registry lookup.
   - Holds `AgentContext` + `CapabilityDependencies` (supplied by transport layer, e.g.
     `SpringCapabilityDependencyAssembler` on HTTP backend).
   - Resolves namespaced tool name **only** if the tool is present in the catalog's exposed tool
     index (profile, allowlist, and `mcp.enabled` enforced at invocation, not only at list time).
   - Rejects unknown or non-exposed tools with a clear error (e.g. tool not in current MCP exposure
     set), not silent fallback to the full registry.
   - On invoke: `registry.provider(capabilityId).create(context, dependencies)` → find
     `ToolBinding` by tool name → `handler(ToolRequest)` → structured JSON result.
5. **`CapabilityAdmissionGate`** interface + **`PermissiveAdmissionGate`** default for POC.
6. **mill-ai manifest API** (supports catalog without instantiation):
   - `CapabilityManifest.declaredTools()` — per tool: name, description, **input schema**, **output
     schema** (when declared in YAML), and `kind` (no handler).
   - **Schema mapping:** MCP `tools/list` registration uses **input** schema only (MCP tool spec /
     LangChain4j `ToolSpecification` pattern). `ExternalCapabilityAssetDescriptor.Tool` (WI-326)
     carries **both** input and output contract metadata for self-describing tool resources.
   - Extend `CapabilityManifestYaml` / loader for top-level `mcp:` block → `CapabilityMcpSettings`.
7. **Unit tests** (see story **Testing strategy**):
   - `CapabilityMcpCatalogTest`, `CapabilityMcpExecutorTest`, `ExternalCapabilityAssetDescriptorTest`
   - `CapabilityRegistry.from(...)` fixtures; no ServiceLoader in unit tests
   - Extend `mill-ai` `CapabilityManifestTest` for `mcp:` YAML block

## Out of Scope

- MCP wire protocol / transports (WI-328 stdio bridge, WI-329 HTTP).
- `SpringCapabilityDependencyAssembler` integration (WI-329).
- Populating future reserved `mcp.*` keys (document only in WI-325).

## Acceptance Criteria

- `./gradlew :ai:mill-ai-mcp-core:test` passes.
- No MCP tool name exists outside the capability registry projection.
- Capabilities with `mcp.enabled: false` are hidden from catalog and blocked at executor.
- Server allowlist with `capabilities=["demo"]` exposes only `demo` tools; invoking a non-allowlisted
  tool by name is rejected.
- Profile filter applies to **both** catalog listing and executor resolution (e.g. `schema.*` tools
  rejected under `hello-world` profile even if client guesses the namespaced name).
- Catalog exposes **23 tools** per [design doc inventory §15](../../../design/agentic/v3-mcp-capability-exposure.md)
  in **unit test** when all nine capability manifests are loaded, `McpExposureConfig` has empty allowlist,
  **no** `AgentProfile` filter, and all capabilities have `mcp.enabled: true` (distinct from
  `hello-world` profile used in transport `testIT`).
- Core module dependency graph contains only `mill-ai` (+ test libs).

## Deliverables

- `ai/mill-ai-mcp-core/` module with KDoc on public types.
- `CapabilityMcpSettings` + manifest loader changes in `mill-ai`.
- Unit tests per `docs/workitems/RULES.md` test structure.
