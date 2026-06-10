# Mill AI v3 — HTTP-only CLI test bench

**Status:** implemented — **`mill-ai-v3-cli`** is HTTP + SSE only; see module **`README.md`** and archived **[WI-169](../../workitems/completed/20260414-ai-v3-chat-capability-dependencies/WI-169-mill-ai-v3-cli-http-test-bench.md)** (story [`…/STORY.md`](../../workitems/completed/20260414-ai-v3-chat-capability-dependencies/STORY.md)).

## Purpose

**`mill-ai-v3-cli`** targets a **thin HTTP + SSE client** against **`mill-ai-v3-service`**, not an in-process LangChain runtime. This document captures **extension points** and **limitations** so implementation matches the work items.

## Connection

- **Default base URL:** **`http://localhost:8080`** when not overridden (CLI flag or environment variable — exact names in **WI-169** / module README).
- The CLI must use the same REST paths as other clients (`/api/v1/ai/profiles`, `/api/v1/ai/chats`, …).

## Profile discovery

- **`GET /api/v1/ai/profiles`** is **not** required on every CLI startup. Use it when the user asks for a list, runs an interactive picker, or validates an id; otherwise **`--profile-id`** (or a documented default profile) may suffice.
- **Optional:** keep an in-memory copy of the last profile list **for the current process** to avoid duplicate fetches in one session.

## Authorization headers (extension points)

The CLI **defines hooks** to attach credentials or custom headers to outbound requests, but the **default implementation is a no-op** (no headers added beyond what the HTTP client sets by default).

- Rationale: the tool is a **developer test bench**; production auth models are out of scope.
- Implementors may later supply:
  - empty or **Basic** credentials from optional **`userId` / `password`** CLI args;
  - **Bearer** tokens from env;
  - or other schemes — without changing call-site structure, by implementing the decorator/hook type defined in **WI-169** (see module sources).

**Limitation:** Until a non-no-op implementation is registered, servers that require authentication may reject requests; document any test setup (e.g. dev profile with relaxed security).

## OpenAPI

The CLI does **not** publish its own OpenAPI document; it consumes the **server** spec generated from **`mill-ai-v3-service`** (`io.swagger.v3.oas.annotations` on controllers).

## Related

- [`v3-chat-service.md`](v3-chat-service.md) — chat HTTP/SSE contract
- [`developer-manual/v3-developer-manual.md`](developer-manual/v3-developer-manual.md)
- Story archive: [`docs/workitems/completed/20260414-ai-v3-chat-capability-dependencies/`](../../workitems/completed/20260414-ai-v3-chat-capability-dependencies/STORY.md)
