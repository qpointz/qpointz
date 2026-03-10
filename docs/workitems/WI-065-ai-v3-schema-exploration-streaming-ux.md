# WI-065 - AI v3 Schema Exploration Streaming UX

Status: `planned`  
Type: `feature`  
Area: `ai`, `ui`  
Backlog refs: `TBD`

## Problem Statement

The first schema-focused POC agent should demonstrate low-latency streamed feedback, but its
event sequence is not yet defined.

## Goal

Define the streaming UX sequence for the Schema Exploration agent.

## In Scope

1. Define early progress feedback.
2. Define schema inspection/tool events.
3. Define partial evidence and explanation deltas and completion behavior.
4. Keep the UX aligned with user-visible progress rather than hidden internal reasoning.

## Out of Scope

- Frontend implementation details beyond the event contract.

## Acceptance Criteria

- The Schema Exploration agent has a clear streaming UX/event sequence suitable for
  implementation and testing.

## Deliverables

- This work item definition (`docs/workitems/WI-065-ai-v3-schema-exploration-streaming-ux.md`).
