# WI-039 — AI v3 Capability Discovery

Status: `planned`  
Type: `✨ feature`  
Area: `ai`, `platform`  
Backlog refs: `TBD`

## Problem Statement

The `v3` runtime should operate on dynamically discovered capabilities rather than a hardcoded capability list.

## Goal

Define and implement the initial dynamic capability discovery mechanism for `v3`.

## In Scope

1. Choose an initial framework-free discovery mechanism.
2. Support runtime loading/indexing of installed capabilities.
3. Keep the design suitable for future extension and MCP exposure.

## Out of Scope

- Remote/plugin distribution of capabilities.

## Acceptance Criteria

- The runtime can discover installed capabilities without hardcoded agent wiring.
- Discovery works in Kotlin/JVM without Spring-specific assumptions.

## Deliverables

- This work item definition (`docs/workitems/WI-039-ai-v3-capability-discovery.md`).

