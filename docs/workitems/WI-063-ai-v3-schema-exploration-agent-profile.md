# WI-063 - AI v3 Schema Exploration Agent Profile

Status: `planned`  
Type: `feature`  
Area: `ai`  
Backlog refs: `TBD`

## Problem Statement

The first real schema-focused POC agent needs a concrete profile definition.

## Goal

Define the Schema Exploration agent profile.

## In Scope

1. Compose the profile from the conversation and schema capabilities.
2. Bind the initial profile to model-context usage while keeping it extensible later.
3. Keep the profile narrow enough for the first POC while allowing later explain and enrichment
   growth.
4. Assume schema-facing tools are backed by `SchemaFacetService` from `data/mill-data-schema-core`.

## Out of Scope

- Knowledge/concept explain profiles.

## Acceptance Criteria

- A concrete Schema Exploration agent profile is defined for `v3`.
- The profile clearly states which capabilities are required for the first implementation.
- The profile does not own physical/metadata merge logic itself.

## Deliverables

- This work item definition (`docs/workitems/WI-063-ai-v3-schema-exploration-agent-profile.md`).
