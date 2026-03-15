# WI-038 — AI v3 Capability Model and Descriptor Format

Status: `done`  
Type: `✨ feature`  
Area: `ai`  
Backlog refs: `A-27`

## Problem Statement

`v3` depends on capabilities as its primary building block, so the runtime needs a
stable capability model and a concrete descriptor format that can be used by both
Kotlin implementations and future external consumers.

## Goal

Define and implement the `v3` capability model and descriptor format.

## In Scope

1. Define the top-level `CapabilityDescriptor` metadata used for discovery,
   composition, and context selection.
2. Define the per-capability manifest format for prompts, tools, and protocols.
3. Keep the contract self-describing and compatible with future MCP exposure.

## Out of Scope

- Runtime discovery implementation.
- Full external resource descriptor model for prompts/protocols/examples/artifacts
  tracked separately in `WI-042`.
- Capability admission, authorization, and policy enforcement implementation.
- Separate reasoning-descriptor assets beyond the shipped prompt/protocol/tool model.

## Acceptance Criteria

- `CapabilityDescriptor` defines stable capability metadata in code.
- `CapabilityManifest` defines a concrete YAML descriptor format for prompts,
  tools, and protocols.
- Capability descriptors/manifests are self-describing and externally usable.
- The design is documented and exercised by capability implementations/tests.

## Deliverables

- Capability descriptor model in
  `ai/mill-ai-v3-core/src/main/kotlin/io/qpointz/mill/ai/CapabilityDescriptor.kt`.
- Capability manifest loader in
  `ai/mill-ai-v3-core/src/main/kotlin/io/qpointz/mill/ai/CapabilityManifest.kt`.
- Manifest design/reference in `docs/design/agentic/v3-capability-manifest.md`.
- Capability implementations and tests using the manifest contract.
