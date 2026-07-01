# WI-378 — Model explorer multi-scope read and chat deep-links

Status: `closed`  
Type: `✨ feature`  
Area: `ui`  
Milestone: 0.8.0  
Depends on: chat facet capture accept path ([`ai-concepts` WI-370](../completed/20260701-ai-concepts/WI-370-concept-authoring-capture.md))

## Problem Statement

The Data Model explorer loads facets with a single **global** scope. Facets accepted in general chat
are persisted under **chat scope** (`urn:mill/metadata/scope:chat-<chatId>`). **Open in model** from
a chat facet card navigates to `/model/...` without scope context, so the captured facet does not
appear in the entity inspector.

The schema and metadata REST APIs already accept **comma-separated** `scope` values and merge per
[`MetadataReadContext`](../../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/service/MetadataReadContext.kt).
The UI does not read or pass multi-scope query params yet.

## Goal

Let users **read** metadata facets from scopes declared in the URL, with optional checkbox toggling
among those URL scopes — especially via chat deep-links. The model view must **not** list or browse
all registered metadata scopes.

## In Scope

1. **UI — scope URL helpers**
   - [`ui/mill-ui/src/utils/modelScopeQuery.ts`](../../../../ui/mill-ui/src/utils/modelScopeQuery.ts): parse/format `?scope=` param, `chat-<chatId>` slug helper, default `global`, local display labels.

2. **UI — scope checkbox picker**
   - New `MetadataScopeCheckboxPicker` in the explorer **content toolbar** (`ExplorerSplitLayout.viewPaneHeader`, right-aligned above the entity pane / Add Facet actions).
   - Shown only when URL declares **2+** scopes; options are **URL slugs only** (not server catalog).
   - Multi-select with at least one scope checked; merge order follows URL param order.

3. **UI — DataModelLayout**
   - Read/write `scope` via `useSearchParams()`; default `global` when param absent.
   - Pass comma-joined **active** scopes to tree/entity/facet API calls.
   - Use `facetMode=direct` for entity detail loads so `facetsResolved` includes chat-scope captures.

4. **UI — EntityDetails writes**
   - Mutate existing facets using row `scopeUrn`; new captures target last writable scope in active selection (typically chat scope when checked).
   - Optional scope badge on facet cards when multiple scopes are active.

5. **UI — chat deep link**
   - [`FacetCondensedPreview`](../../../../ui/mill-ui/src/components/chat/artifactPreview/FacetCondensedPreview.tsx): **Open in model** → `?scope=global,chat-<conversationId>`.

6. **Tests**
   - Vitest: scope query utils, layout (no picker without URL scopes; picker with `?scope=global,chat-x`), facet open-in-model URL.

7. **Docs**
   - Short public or design note: model explorer scopes are URL-driven only.

## Out of Scope

- Listing all metadata scopes in the model explorer
- **Scope management and authorization** — registering scopes, enforcing which scopes a user may read/write, or any UI to browse or add scopes beyond what the URL declares (follow-up story)
- Enriching `GET /api/v1/schema/context` with `MetadataScopeService.findAll()`
- Promoting chat facets to global (separate metadata lifecycle story)
- Admin UI for scope CRUD (`POST/DELETE /api/v1/metadata/scopes`)
- [`ai-concepts` WI-371](../completed/20260701-ai-concepts/WI-371-mill-ui-knowledge-chat.md) knowledge inline chat
- Changing merge/precedence rules on the server (client passes ordered slug list only)

## Follow-up story (not WI-378)

**Authorized scope management** — server-side rules for which scopes a user may use, optional scope picker fed by permitted scopes (not the full registry), and flows to add or attach scopes to a model-view session. WI-378 intentionally does not implement discovery, RBAC, or scope administration; it only reads scopes already present in `?scope=`.

## Acceptance Criteria

- [x] `/model` without `?scope=` reads **global** only; no scope picker rendered.
- [x] `?scope=global,chat-<id>` shows checkboxes for **only** those URL slugs (not other chat scopes).
- [x] Checking `global` + `chat-<id>` shows chat-captured facets on entity targets after accept.
- [x] Checkbox changes update `?scope=`; at least one slug remains active.
- [x] Open in model from chat includes `scope=global,chat-<conversationId>`.
- [x] Unit tests pass; `ui:npm-build` tsc clean.

## Deliverables

- This WI file and [`STORY.md`](STORY.md) tracker update on completion.
- Code changes in `mill-ui` as listed above.
- Brief documentation update under `docs/design/` or `docs/public/src/`.
