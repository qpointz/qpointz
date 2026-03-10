# WI-056 - AI v3 Schema Exploration Planner Responsibilities

Status: `planned`  
Type: `feature`  
Area: `ai`  
Backlog refs: `TBD`

## Problem Statement

The planner role needs a concrete low-risk domain workflow that is richer than Hello World but
still bounded.

## Goal

Define planner responsibilities for the Schema Exploration agent.

## In Scope

1. Define how the planner classifies exploration requests such as table, column, relation, or
   ambiguity-driven questions.
2. Define how the planner sequences entity inspection, relation inspection, and metadata-gap
   inspection.
3. Define when the planner should prefer direct explanation versus one or more schema tool calls.
4. Define when the planner should request clarification instead of guessing the target entity.

## Out of Scope

- Observer result interpretation.
- Generic planner behavior for all future agents.

## Acceptance Criteria

- Planner behavior is explicit and testable in the schema-exploration workflow.
- The planner's initial decision space is bounded enough to implement with typed structured
  output.
- The planner can distinguish at least:
  - inspect entity
  - inspect relations
  - inspect metadata completeness
  - ask clarification
  - finish with direct answer

## Deliverables

- This work item definition (`docs/workitems/WI-056-ai-v3-schema-exploration-planner-responsibilities.md`).
