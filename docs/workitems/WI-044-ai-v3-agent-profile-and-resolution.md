# WI-044 — AI v3 Agent Profile and Resolution Model

Status: `planned`  
Type: `✨ feature`  
Area: `ai`  
Backlog refs: `TBD`

## Problem Statement

Mill needs many context-bound agents, but the model for resolving agent profiles from discovered capabilities is not yet fixed.

## Goal

Define how `v3` composes and resolves agent profiles from capabilities for a given context.

## In Scope

1. Define agent profile structure.
2. Define profile resolution behavior for at least general, model, and knowledge contexts.
3. Keep the design compatible with focused/context-bound sessions.

## Out of Scope

- Domain-specific tool implementations.

## Acceptance Criteria

- Agent profiles can be described independently from hardcoded runtime wiring.
- The runtime can resolve a context-bound profile from discovered capabilities.

## Deliverables

- This work item definition (`docs/workitems/WI-044-ai-v3-agent-profile-and-resolution.md`).

