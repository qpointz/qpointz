# WI-325 — MCP Capability Exposure Design Doc

Status: `completed`  
Type: `📝 docs`  
Area: `ai`, `agentic`  
Backlog refs: **A-56**  
Depends on: none

## Problem Statement

[`v3-foundation-decisions.md`](../../../design/agentic/v3-foundation-decisions.md) §3.4–3.7 defines
MCP-friendly capability exposure (tools → MCP tools, protocols → resources, prompts → MCP prompts)
but there is no implementation-focused design doc. Backlog **A-56** has no work-item anchor beyond
the foundation decisions.

## Goal

Author [`docs/design/agentic/v3-mcp-capability-exposure.md`](../../../design/agentic/v3-mcp-capability-exposure.md)
as the normative implementation guide for the MCP server POC: module layout, URI scheme, tool
namespacing, profile filtering, admission stub, and the **single extension path** rule (no bespoke
MCP catalog).

## In Scope

1. Create `v3-mcp-capability-exposure.md` covering:
   - core + transport module split
   - MCP asset mapping table (descriptor, tool, prompt, protocol, artifact schema)
   - `mill://` URI conventions
   - tool invocation flow (catalog → admission → executor → `ToolBinding`)
   - **capability YAML `mcp:` block** — `enabled` (default `true`); document reserved keys for
     future per-capability MCP options
   - filter order: registry → `mcp.enabled` → `mill.ai.mcp.capabilities` → profile → admission
   - **catalog construction:** manifest-level tool/prompt/protocol metadata (no capability `create()`)
   - POC defaults (`hello-world` profile, dependency requirements per profile)
   - explicit deferral of **P-10** bespoke MCP (capabilities-first rule)
   - **External integration** section with forward-reference to WI-330 (LangChain Python + Skymill; in story scope)
2. Extend [`v3-capability-manifest.md`](../../../design/agentic/v3-capability-manifest.md) with the
   `mcp:` manifest section schema.
3. Cross-link from `v3-foundation-decisions.md` §3.6–3.7 and WI-04A/04B to the new doc.
4. Update [`BACKLOG.md`](../../BACKLOG.md): **A-56** → `planned`, link to this story.

## Out of Scope

- Production code (WI-327+).
- HTTP transport details beyond a forward-reference to WI-329.

## Acceptance Criteria

- Design doc exists and is linked from agentic README or foundation decisions.
- **A-56** backlog row points at `completed/20260622-ai-v3-mcp-server-poc/`.
- Extension rule is explicit: new MCP surfaces require new `CapabilityProvider` in `mill-ai`.
- `mcp.enabled` YAML hierarchy documented with default `true` and opt-out semantics.

## Deliverables

- [`docs/design/agentic/v3-mcp-capability-exposure.md`](../../../design/agentic/v3-mcp-capability-exposure.md) — **drafted for review**
- [`v3-capability-manifest.md`](../../../design/agentic/v3-capability-manifest.md) §3.1.1 (`mcp:` block)
- [`REVIEW.md`](REVIEW.md) — review package index
- Backlog and cross-link updates on the story branch per `docs/workitems/RULES.md`.

**Note:** WI-325 design deliverables are ready for review. Mark `[x]` in `STORY.md` when merged on the
implementation branch.
