# WI-064 - AI v3 Schema Exploration Workflow

Status: `planned`  
Type: `feature`  
Area: `ai`  
Backlog refs: `TBD`

## Problem Statement

The first schema-focused POC agent needs a bounded workflow that uses schema tools and streamed
progress and evidence.

That workflow depends on merged schema outputs where physical existence is guaranteed first and
descriptive metadata is attached second.

## Goal

Define the Schema Exploration workflow for `v3`.

## In Scope

1. Define request/context inspection.
2. Define schema inspection and relation/description inspection steps.
3. Define how exploration moves from initial target selection to evidence gathering to final
   answer.
4. Define bounded clarification and stop behavior for ambiguous or weak-evidence cases.
5. Define how the workflow reacts when a physical entity exists but descriptive metadata is
   absent, partial, or stale.
6. Assume workflow steps consume `SchemaFacetService` results rather than calling `SchemaProvider`
   and `MetadataService` independently from the agent layer.

## Out of Scope

- SQL-backed explain flows.
- Free-form unbounded browsing across the full catalog.

## Acceptance Criteria

- The Schema Exploration workflow is a bounded but real agentic workflow using schema tools.
- The workflow is concrete enough to implement as the next POC agent.
- The workflow explicitly distinguishes physical-schema evidence from descriptive metadata
  evidence.
- The workflow depends on reusable `data/mill-data-schema-core` outputs rather than bespoke AI
  merge logic.

## Deliverables

- This work item definition (`docs/workitems/WI-064-ai-v3-schema-exploration-workflow.md`).
