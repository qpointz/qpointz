# WI-153 — Capability Admission and Tool-Invocation Observability

Status: `planned`  
Type: `✨ feature`  
Area: `ai`  
Milestone: `0.8.0`  
Backlog refs: `A-79`

## Problem Statement

Tool execution in `LangChain4jAgent` should not assume every resolved capability is always
authorized for the current profile and chat context. Operators also need visibility when a tool
is skipped or denied rather than failing opaquely later — a gap relative to **predictable,
debuggable** behaviour expected when matching **v1-class** chat and SQL flows.

## Goal

Introduce an explicit **admission** step before tool invocation: validate that the capability and
tool are allowed for the active `AgentProfile` / context, and emit structured **events** (or
equivalent observability hooks) on denial or skip paths — supporting parity items in **WI-151**
where v1 had clearer failure or policy boundaries.

## In Scope

1. Define the admission policy seam (interface + default implementation) colocated with the v3
   agent runtime, without pulling unnecessary framework dependencies into `mill-ai-v3-core` types.
2. Integrate admission checks in `LangChain4jAgent` (or the narrowest responsible layer) before
   tools run.
3. Emit denial or skip signals compatible with existing `AgentEvent` streaming where applicable.

## Out of Scope

- Full RBAC product integration (external IdP, admin UI) — hooks only.
- Changing the public REST contract unless required for event shape consistency already planned elsewhere.

## Acceptance Criteria

- Unknown capability IDs, profile mismatches, or policy denial produce a **clear, test-covered**
  path (no silent no-op tool calls).
- At least one integration or high-level unit test demonstrates denial event behavior.

## Deliverables

- This work item definition.
- Implementation and tests on the story branch per `docs/workitems/RULES.md`.
