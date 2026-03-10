# WI-059 - AI v3 Schema Exploration End-to-End Scenarios

Status: `planned`  
Type: `test`  
Area: `ai`  
Backlog refs: `TBD`

## Problem Statement

The Schema Exploration workflow needs end-to-end scenarios that validate usefulness and
planner/observer behavior together.

## Goal

Define and validate end-to-end scenarios for the Schema Exploration agent.

## In Scope

1. Cover entity exploration with good metadata.
2. Cover sparse metadata and incomplete-description cases.
3. Cover relation-heavy exploration questions.
4. Cover ambiguous requests that require clarification.
5. Cover bounded failure or stop conditions such as authorization denial or exploration-budget
   stop.
6. Reuse the skymill fixture family for representative scenario coverage where practical.

## Out of Scope

- Large domain fixture suites.
- SQL-heavy scenarios.

## Acceptance Criteria

- The Schema Exploration agent has representative end-to-end scenarios that exercise branching,
  observation, and bounded termination.
- Scenario design is compatible with integration testing against the skymill test model and
  dataset fixtures.

## Deliverables

- This work item definition (`docs/workitems/WI-059-ai-v3-schema-exploration-scenarios.md`).
