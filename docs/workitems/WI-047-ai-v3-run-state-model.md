# WI-047 — AI v3 Run State Model

Status: `planned`  
Type: `✨ feature`  
Area: `ai`, `platform`  
Backlog refs: `TBD`

## Problem Statement

Agentic workflows need managed state, but the ownership and persistence boundaries for `v3` run state are not yet clearly defined.

## Goal

Define the `v3` run state model, including ephemeral and durable workflow state.

## In Scope

1. Define runtime-owned workflow state.
2. Distinguish ephemeral versus durable state.
3. Define the relationship between run state, artifacts, and clarification state.

## Out of Scope

- Full persistence implementation.

## Acceptance Criteria

- `v3` run state is defined clearly enough for planner, observer, and runtime design.
- Ephemeral versus durable state is explicitly separated.

## Deliverables

- This work item definition (`docs/workitems/WI-047-ai-v3-run-state-model.md`).

