# WI-326 — External Capability Asset Descriptors

Status: `done`  
Type: `✨ feature`  
Area: `ai`  
Backlog refs: **A-31**  
Depends on: [WI-325](WI-325-mcp-capability-exposure-design.md)  
Supersedes: [WI-042](../ai-v3/WI-042-ai-v3-external-capability-asset-descriptors.md) (moved to this story)

## Problem Statement

MCP resources must be **self-describing** so external clients can identify resource kind, owning
capability, version, and interpretation hints. Backlog **A-31** and planned **WI-042** define this
requirement but no Kotlin types exist.

## Goal

Introduce the **external capability asset descriptor model** in `mill-ai-mcp-core` (package
`io.qpointz.mill.ai.mcp`), sufficient for MCP resource metadata and internal catalog serialization.

## In Scope

1. Sealed hierarchy `ExternalCapabilityAssetDescriptor` with variants:
   - `Capability` — capability-level descriptor resource
   - `Tool` — tool contract metadata
   - `Protocol` — protocol schema / mode metadata
   - `Prompt` — prompt asset metadata
   - `ArtifactSchema` — artifact kind schema reference
2. Shared fields: `capabilityId`, `assetKind`, `version`, `uri`, `contentType`, `description`,
   `tags` (per `v3-foundation-decisions.md` §3.7).
3. **Tool descriptors** include `toolKind: QUERY | CAPTURE` from manifest/`ToolBinding.kind` —
   **metadata only**; no MCP filter/enforcement by kind in POC (capability `mcp.enabled` controls exposure).
   Tool variant carries **input and output** JSON schemas from manifest `declaredTools()` (output
   omitted when not declared in YAML).
4. `McpUriScheme` — constants and builders for `mill://capabilities/...` URIs.
5. Unit tests: descriptor JSON round-trip or snapshot; each variant carries required metadata.

## Out of Scope

- MCP wire protocol / transports (WI-328 bridge, WI-329 HTTP).
- Production trust/RBAC fields beyond a `trustClass` placeholder for future **A-79** alignment.

## Acceptance Criteria

- Descriptors are sufficient for a client to identify resource kind, ownership, and interpretation
  without reading opaque blob content first (WI-042 / A-31 acceptance).
- Types live in `mill-ai-mcp-core` with **no** Spring or MCP SDK dependencies.
- `planned/ai-v3/STORY.md` no longer tracks WI-042 (redirected to this story).

## Deliverables

- Kotlin types and unit tests in `ai/mill-ai-mcp-core`.
- Update to `ai-v3` story tracker (remove WI-042 line; add cross-reference).
