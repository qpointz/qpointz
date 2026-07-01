# Model view multi-scope facet visibility

**Status:** `closed` (**2026-07-01**)  
**Branch:** `feat/ai-concepts` (delivered with MR !416)  
**Milestone:** **0.8.0**  
**Story folder:** [`docs/workitems/completed/20260701-model-view-multi-scope/`](.) — archived **2026-07-01**.  
**Related backlog:** **U-17** (`done`)

Chat-captured metadata facets are stored under **chat scope**
(`urn:mill/metadata/scope:chat-<chatId>`), but the Data Model explorer (`/model`) reads **global**
only. Users who open a facet from chat cannot see the captured assignment until it is promoted or
copied to global.

This story adds **URL-driven multi-scope read** to the model explorer: scopes come **only** from the
`?scope=` query parameter (not the full scope registry). When multiple scopes are declared in the
URL, a checkbox picker in the Schema Browser toolbar lets the user enable/disable them. Chat
deep-links pass **global + chat scope**.

**Depends on (behavioural, not blocking):** chat facet capture and accept/reject
([`ai-concepts`](../completed/20260701-ai-concepts/STORY.md) **WI-370**).

**Design references:**

- [`docs/design/agentic/ai-v3-chat-metadata-scope.md`](../../../design/agentic/ai-v3-chat-metadata-scope.md)
- [`docs/design/metadata/metadata-layered-sources-and-ephemeral-facets.md`](../../../design/metadata/metadata-layered-sources-and-ephemeral-facets.md)

## Work Items

- [x] WI-378 — Model explorer multi-scope read and chat deep-links (`WI-378-model-view-multi-scope.md`)

## Story acceptance

1. `/model` without `?scope=` uses **global** only; no scope picker.
2. `?scope=global,chat-<chatId>` declares the scope set; checkboxes appear only for slugs in that param (not all registered scopes).
3. User can select/deselect among URL-declared scopes; at least one stays active; checkbox changes rewrite `?scope=`.
4. Active scopes are passed as comma-separated `scope` to schema/metadata APIs (merge order = URL order).
5. **Open in model** from a chat facet card navigates with `?scope=global,chat-<conversationId>`; accepted chat facets are visible on the target entity.
6. Vitest coverage for scope URL helpers, layout behaviour, and chat deep-link.

## Deferred (separate story)

**Authorized scope management** — which scopes a user is allowed to use (RBAC), UI to add or switch scopes in model view beyond URL deep-links, and integration with the scope registry. WI-378 does not implement scope discovery or permission checks; callers (e.g. chat open-in-model) pass explicit `?scope=` slugs only.
