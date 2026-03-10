# WI-048 — AI v3 LangChain4j Adapter Layer

Status: `planned`  
Type: `✨ feature`  
Area: `ai`  
Backlog refs: `TBD`

## Problem Statement

`v3` should use LangChain4j, but only as an integration layer rather than as the architectural center.

## Goal

Add a dedicated LangChain4j adapter layer to `v3`.

## In Scope

1. Integrate model calls.
2. Integrate structured outputs.
3. Integrate tool calling and streamed output.

## Out of Scope

- Letting LangChain4j define the core runtime architecture.

## Acceptance Criteria

- LangChain4j is integrated through a dedicated adapter layer.
- `v3` core runtime remains independent from LangChain4j abstractions where practical.

## Deliverables

- This work item definition (`docs/workitems/WI-048-ai-v3-langchain4j-adapter-layer.md`).

