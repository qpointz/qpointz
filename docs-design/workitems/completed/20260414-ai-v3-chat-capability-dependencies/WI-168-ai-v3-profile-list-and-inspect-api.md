# WI-168 — Profile list and inspect HTTP API

Status: `planned`  
Type: `✨ feature`  
Area: `ai`  
Milestone: `TBD`

## Problem Statement

Clients creating chats via **`POST /api/v1/ai/chats`** need a stable **`profileId`**, but there is no HTTP surface to **enumerate** or **inspect** registered **`AgentProfile`** definitions (see [`docs/design/agentic/README.md`](../../../design/agentic/README.md)). [`ProfileRegistry`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/profile/ProfileRegistry.kt) only exposed **`resolve`**; listing was not part of the contract.

## Profile source of truth (maintenance)

- **No profile repository in persistence:** JPA stores **`profileId`** on chat metadata only; there is **no** `AgentProfile` entity or CRUD for profile **definitions**.
- **Registry:** The Spring **`ProfileRegistry`** bean (default: **`DefaultProfileRegistry`** — compile-time list of **`AgentProfile`** Kotlin objects) **is** the catalog. **`GET /api/v1/ai/profiles`** surfaces **`registeredProfiles()`**; **`GET …/{profileId}`** uses **`resolve`**.
- **Ongoing maintenance:** Add or change profiles by editing **code** (new profile object + register in **`DefaultProfileRegistry`** or replace the bean with **`MapProfileRegistry`** / custom implementation). Dynamic/config/DB-backed profiles are **out of scope** for this WI — a **separate story** if needed.
- **Design reference:** [`v3-chat-service.md`](../../../design/agentic/v3-chat-service.md) (runtime rehydration and HTTP list).

## Goal

1. Extend **`ProfileRegistry`** with **`registeredProfiles(): List<AgentProfile>`** (sorted by **`id`**) in **`mill-ai-v3`**, implemented by **`MapProfileRegistry`** and **`DefaultProfileRegistry`**.
2. Add **`mill-ai-v3-service`** endpoints:
   - **`GET /api/v1/ai/profiles`** — JSON array of profiles (**`AgentProfileResponse`**: **`id`**, sorted **`capabilityIds`**).
   - **`GET /api/v1/ai/profiles/{profileId}`** — **same DTO** as each list element (**`AgentProfileResponse`**). **API stability:** v1 list and inspect are **identical shapes**; **no** extra fields on inspect unless the contract is versioned later. **404** when unknown via **`throw MillStatuses.notFound("Unknown profile: …")`**. **HTTP body:** JSON **`MillStatusDetails`** (same as chat unknown-resource routes), produced via **`MillStatuses` → [`AiChatExceptionHandler`](../../../../ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/AiChatExceptionHandler.kt)**, not a plain string error — mirror **`AiChatController.getChat`** **404** **`@ApiResponse`** **`schema = MillStatusDetails::class`**.
3. **OpenAPI (springdoc):** class **`@Tag`**, per-method **`@Operation`** and **`@ApiResponses`** with **`Content` / `Schema(implementation = …)`** on **`AgentProfileResponse`** and **`MillStatusDetails`** for **404** — same annotations as **[`AiChatController`](../../../../ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/AiChatController.kt)**.
4. **Unit** / **integration** tests consistent with existing **`AiChatController`** tests.

## Out of Scope

- **`mill-ui`** or any SPA wiring — **separate story**.
- Exposing full **`EventRoutingPolicy`** JSON unless product explicitly requires it (default: **`id`** + **`capabilityIds`** only).

## Acceptance Criteria

- **`ProfileRegistry.registeredProfiles()`** is documented and covered by unit tests.
- HTTP contract above works in **`testIT`** with the real Boot context.
- No new coupling from **`mill-ai-v3-persistence`** to this API (read-only registry + controller only).
- **OpenAPI:** profile endpoints appear in the generated spec with correct schemas (springdoc annotations complete).
- **Errors:** unknown **`profileId`** → **HTTP 404** with **JSON body type `MillStatusDetails`** (via **`AiChatExceptionHandler`**), matching chat **`404`** contract.

## Reference

- [**WI-169**](WI-169-mill-ai-v3-cli-http-test-bench.md) — CLI consumes profile list
- Story: [`STORY.md`](STORY.md)
