# WI-368 — Chat context injection and knowledge profile

Status: `deferred` — **out of `ai-concepts` story scope** (knowledge / contextual chat subject to separate review)  
Type: `✨ feature`  
Area: `ai`  
Milestone: Deferred (future story)  
Depends on: [WI-367](WI-367-concept-catalog-capability.md)

> **Not on branch `feat/ai-concepts`.** General chat delivery uses WI-369 (`data-analysis`) only.
> Keep this WI for a future knowledge-context story.

## Problem Statement

The chat API supports `contextType=knowledge` and `contextId` (see `AiChatControllerIT`), but
runtime behaviour does not yet **inject** focused concept definitions into the agent system
prompt, auto-select a concept profile, or set `focusEntityType=concept` on rehydration. Knowledge
inline chat in mill-ui cannot get meaningful answers from stored concepts.

## Goal

Wire **concept context into chat turns**: profile selection, rehydration, and deterministic prompt
injection for knowledge-focused chats.

## In Scope (high level)

1. Agent profile `concept-exploration` (`conversation` + `concept` + `schema`).
2. `mill.ai.chat.context-profiles` mapping (`knowledge` → `concept-exploration`).
3. `ChatRehydration` / `AgentContext`: knowledge context → `focusEntityType=concept`.
4. `ConceptContextInjector` (or extend `buildAgentSystemPrompt`): inject focused concept when
   `contextType=knowledge` + `contextId=urn:mill/model/concept:<slug>` (GAP-2 **locked**).
5. Update `AiChatControllerIT` and fixtures: replace legacy `concept.clv` with full concept URN.
6. `mill-ai-test` / `testIT` scenarios for knowledge-context explain without extra tool round.

## Out of Scope

- Model-context injection for SQL (WI-369)
- mill-ui client wiring (WI-371)
- Concept authoring (WI-370)

## Acceptance Criteria (draft — refine later)

- Creating a chat with `contextType=knowledge` selects `concept-exploration` when no `profileId`.
- Agent system prompt includes seeded concept definition for matching
  `contextId=urn:mill/model/concept:<slug>`.
- Integration test demonstrates explain behaviour against fixture concepts.

## Deliverables

- This work item definition.
- Profile, config, injection, tests on the story branch.
