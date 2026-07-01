# WI-371 — mill-ui knowledge inline chat to real AI

Status: `deferred` — **out of `ai-concepts` story scope** (knowledge / contextual chat subject to separate review)  
Type: `✨ feature`  
Area: `ui`, `ai`  
Milestone: Deferred (future story)  
Depends on: [WI-368](WI-368-concept-chat-injection.md)

> **Not on branch `feat/ai-concepts`.** This story targets general `/chat` only.

## Problem Statement

mill-ui **Knowledge** inline chat (`inlineChatKnowledgeContext` feature flag) uses mock responses
in `chatService.ts`. Users browsing `/knowledge/:conceptId` cannot chat with the real v3 AI
service against stored concept definitions.

## Goal

Wire inline knowledge chat to **`POST /api/v1/ai/chats`** and SSE messaging with
`contextType=knowledge` and `contextId=urn:mill/model/concept:<slug>` (GAP-2 **locked**), using
WI-368 profile/injection behaviour. For the first UI iteration, concept capture should reuse the
same captured-facet persistence, hydration, and presentation path as other metadata facets.

## In Scope (high level)

1. Replace mock knowledge responses with real AI chat API calls (contextual chat per concept).
2. Pass `contextType`, `contextId` as `urn:mill/model/concept:<slug>` (derive from route slug in
   `/knowledge/:slug`), optional `contextLabel`; rely on context-profile config or explicit
   `profileId=concept-exploration`.
3. Surface streamed assistant output and facet-proposal artifacts in inline chat UI (reuse existing
   artifact preview where applicable).
4. Ensure accepted concept facet proposals hydrate and present like other captured facets.
5. Manual / Vitest coverage for knowledge inline chat path.

## Out of Scope

- General `/chat` page redesign
- Knowledge view CRUD (read-only metadata remains)
- grinder-ui

## Acceptance Criteria (draft — refine later)

- With `inlineChatKnowledgeContext` enabled, inline chat on a concept calls real AI backend.
- Response reflects seeded concept definition from metadata fixtures.
- Accepted concept capture appears through the same persisted facet presentation path as other
  captured metadata facets.

## Deliverables

- This work item definition.
- `mill-ui` service + inline chat wiring on the story branch.
