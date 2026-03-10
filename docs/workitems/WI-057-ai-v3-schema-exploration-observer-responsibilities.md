# WI-057 - AI v3 Schema Exploration Observer Responsibilities

Status: `planned`  
Type: `feature`  
Area: `ai`  
Backlog refs: `TBD`

## Problem Statement

The observer role is central to the agentic loop but still needs a concrete first-use definition
in a real domain agent.

## Goal

Define observer responsibilities for the Schema Exploration workflow.

## In Scope

1. Define how the observer evaluates schema inspection results.
2. Define continue, branch, clarify, and stop decisions after each exploration step.
3. Define how the observer reacts to missing or weak evidence without fabricating conclusions.
4. Keep the observer separate from raw tool execution.

## Out of Scope

- Generic observer implementations for all future agents.
- Planner prompt design.

## Acceptance Criteria

- The observer role is explicit and distinct from planner and executor.
- The observer defines concrete follow-up decisions for at least:
  - sufficient evidence to answer
  - relation follow-up needed
  - metadata is incomplete
  - clarification is required
  - exploration budget should stop the run

## Deliverables

- This work item definition (`docs/workitems/WI-057-ai-v3-schema-exploration-observer-responsibilities.md`).
