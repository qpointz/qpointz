# WI-169 — `mill-ai-v3-cli`: HTTP-only test bench (no local agent)

Status: `planned`  
Type: `✨ feature`  
Area: `ai`  
Milestone: `TBD`

## Problem Statement

**`mill-ai-v3-cli`** today can run **in-process** agents (**`SchemaExplorationAgent`**, LangChain wiring, **`mill-ai-v3-data`** demos). That duplicates server wiring and drifts from production. The CLI should be a **thin test bench** against **`mill-ai-v3-service`** only.

## Goal

1. **Remove in-process / “local” agent mode entirely** — no **`LangChain4j`**, no **`SchemaExplorationAgent`**, no **`SchemaFacetServiceFactory`** / REPL path that embeds the runtime. Delete or relocate demo-only code per repo hygiene; **no** `--local` / `--offline` escape hatch unless explicitly reintroduced later as a separate story.
2. **HTTP + SSE only:** the CLI calls the same REST API as any client:
   - **`GET /api/v1/ai/profiles`** (requires **WI-168**) when the user needs discovery — **not mandatory on every startup.** Supported flows: explicit **`--profile-id`** (or env); **optional** interactive/list command that fetches profiles; **optional** in-memory cache of the last **`GET /profiles`** result for the **current process** (no cross-session cache requirement).
   - **`POST /api/v1/ai/chats`**, **`GET …`**, **`POST /api/v1/ai/chats/{chatId}/messages`** with **SSE** — see endpoint sketch in **WI-160** / service README.
3. **Connection parameters (test-bench ergonomics):**
   - **`baseUrl`** — optional; **canonical default** **`http://localhost:8080`** (override when local **`mill-service`** / v3 chat uses another port — document in README).
   - **`userId`** / **`password`** — optional CLI flags or env; reserved for future non-no-op auth; must align with service **`UserIdResolver`** when the server enforces identity.
4. **Authorization (outbound HTTP):** define **extension points** (e.g. a small interface or functions) that **decorate** the request with auth headers. **Default implementation does nothing** (no extra headers). Wires optional **`userId`/`password`** only when a non-no-op implementation is added later. Document in module README and **[`docs/design/agentic/v3-mill-ai-v3-cli-http-client.md`](../../../design/agentic/v3-mill-ai-v3-cli-http-client.md)**.
5. **Authentication posture:** **relaxed** — CLI is **developer test bench only**; hard security is **not** a goal of this WI.

## Out of Scope

- **`mill-ui`** — separate story.
- Production-grade CLI auth, SSO, or token refresh.

## Acceptance Criteria

- Default code path is **WebClient** (or equivalent) + SSE consumer **only**; repository grep shows **no** default dependency on **`OpenAiStreamingChatModel`**, **`SchemaExplorationAgent.fromEnv`**, or **`mill-ai-v3-data`** for the primary entrypoint (Gradle may drop **`mill-ai-v3-data`** from CLI if nothing else needs it — confirm in implementation).
- **`README`** documents default **`http://localhost:8080`**, auth **hook** API (no-op default), optional **`userId`/`password`**, and **test bench** role.
- **OpenAPI:** the CLI does **not** ship a separate OpenAPI document; clients rely on **`mill-ai-v3-service`** springdoc. Annotation scope for this story stays on the **server** only and follows **WI-160** / **WI-168**: annotate **`AiChatController`** only if edited for the branch; **`AiProfileController`** and profile DTOs when part of the diff; **no** repo-wide churn. CLI README may link to **`/swagger-ui.html`** (or project default) when useful.

## Dependencies

- **WI-168** — profile list API (for discoverable **`profileId`**).
- **WI-167** — server injects **`capabilityDependencies`** so schema profiles work over HTTP before declaring end-to-end success.

## Reference

- [**WI-160**](WI-160-ai-v3-chat-runtime-capability-dependencies.md) — IT, docs, service HTTP contract.
- Design: [`docs/design/agentic/v3-mill-ai-v3-cli-http-client.md`](../../../design/agentic/v3-mill-ai-v3-cli-http-client.md)
- Story: [`STORY.md`](STORY.md)
