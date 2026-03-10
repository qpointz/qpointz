# WI-055 - AI v3 Schema Exploration POC Agent Scope

Status: `planned`  
Type: `docs`  
Area: `ai`  
Backlog refs: `TBD`

## Problem Statement

After Hello World, `v3` needs a first meaningful schema-focused agent that proves bounded
multi-step exploration without SQL complexity.

The agent cannot be implemented from metadata alone. It depends on a merged schema foundation
where:

- physical schema existence comes from
  `data/mill-data-backend-core/src/main/java/io/qpointz/mill/data/backend/SchemaProvider.java`
- detached descriptive metadata comes from the `metadata/` modules

The physical source guarantees which schemas, tables, and attributes actually exist in data.
Metadata enriches those physical entities with descriptions, relations, rules, and similar
descriptive facets that cannot be inferred from the physical model alone.

The reusable merge boundary for this work should live outside `ai/v3` in:

- `data/mill-data-schema-core` for pure Kotlin domain and merge logic
- `data/mill-data-autoconfigure` for Spring wiring where needed

## Goal

Define the scope of the Schema Exploration POC agent.

## In Scope

1. Define the agent as the first post-Hello-World domain milestone.
2. Keep the domain low risk and schema-oriented.
3. Clarify that the milestone validates schema capability shape and bounded orchestration
   together.
4. Position the agent as exploration-oriented rather than pure explanation or pure workflow
   validation.
5. Establish the dependency between reusable schema capability work (`WI-060` to `WI-062`) and
   agent-specific exploration work (`WI-063` to `WI-066`).
6. Clarify that schema exploration and later metadata triage both depend on the same merged
   physical-plus-descriptive foundation.

## Out of Scope

- SQL execution and charting.
- Concept/knowledge exploration.
- Write-oriented enrichment flows.

## Acceptance Criteria

- The first schema-focused POC agent is explicitly defined as a Schema Exploration agent.
- The milestone is positioned as both domain-facing and architecture-validating.
- The split between reusable schema capability work and agent-specific exploration work is clear.
- The initial scope stays bounded to schema discovery, inspection, relation traversal, and
  missing-metadata handling.
- The work item explicitly identifies `SchemaProvider` as the source of physical truth and the
  `metadata` module as the source of detached descriptive enrichment.
- The work item places the reusable merged boundary under `data/`, not `ai/` or `metadata/`.

## Deliverables

- This work item definition (`docs/workitems/WI-055-ai-v3-schema-exploration-scope.md`).
