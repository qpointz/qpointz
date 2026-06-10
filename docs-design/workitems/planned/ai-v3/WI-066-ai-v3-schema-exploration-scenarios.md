# WI-066 - AI v3 Schema Exploration End-to-End Scenarios

Status: `planned`  
Type: `test`  
Area: `ai`  
Backlog refs: `TBD`

## Problem Statement

The first schema-focused POC agent needs representative scenarios to validate usefulness,
correctness, and bounded exploration behavior.

## Goal

Define end-to-end scenarios for the Schema Exploration agent.

## In Scope

1. Cover table exploration.
2. Cover column exploration.
3. Cover relation exploration.
4. Cover missing-metadata exploration.
5. Cover ambiguous-target clarification.
6. Cover cases where physical schema exists but metadata is absent or stale.
7. Prefer the skymill fixture family for initial realistic scenario coverage.

## Out of Scope

- SQL-heavy or visualization scenarios.

## Acceptance Criteria

- The Schema Exploration agent has representative end-to-end scenarios covering its initial
  scope.
- Scenario coverage includes both:
  - physically guaranteed schema-only cases
  - physically guaranteed entities with descriptive metadata enrichment
- Scenario fixtures can be expressed against `SchemaFacetService` outputs and `*WithFacets`
  domain models.
- Scenario implementation is suitable for integration coverage using:
  - `test/skymill.yaml`
  - `test/datasets/skymill/`
  - the skymill metadata repository fixtures

## Deliverables

- This work item definition (`docs/workitems/WI-066-ai-v3-schema-exploration-scenarios.md`).
