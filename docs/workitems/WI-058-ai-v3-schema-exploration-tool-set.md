# WI-058 - AI v3 Schema Exploration Minimal Tool Usage Set

Status: `planned`  
Type: `feature`  
Area: `ai`, `metadata`  
Backlog refs: `TBD`

## Problem Statement

The first Schema Exploration agent needs a minimal non-SQL tool usage set that still allows
meaningful branching and observation.

Those tools must be built on top of a merged foundation rather than directly against only one
source. Physical existence must come from `SchemaProvider`, while descriptive enrichment must
come from metadata entities and facets.

## Goal

Define the minimal tool usage set required by the Schema Exploration agent.

## In Scope

1. Identify the minimum subset of schema tools the first exploration agent actually needs.
2. Define tool usage expectations for entity inspection, relation inspection, and metadata-gap
   detection.
3. Keep the first workflow implementable without requiring the full future schema capability
   surface.
4. Ensure tool outputs preserve the distinction between:
   - guaranteed physical existence
   - optional descriptive enrichment
   - missing metadata

## Out of Scope

- SQL or charting tools.
- Broader concept/knowledge tools.
- Final full schema capability design beyond what the first agent actually needs.

## Acceptance Criteria

- The first schema-exploration workflow has a concrete minimal tool subset sufficient to drive
  planner and observer behavior.
- The work item makes clear which tools are required for the first POC versus deferred for later
  schema capability growth.
- The required tool subset is defined against merged physical-plus-metadata outputs rather than
  against metadata-only responses.

## Deliverables

- This work item definition (`docs/workitems/WI-058-ai-v3-schema-exploration-tool-set.md`).
