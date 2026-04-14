# AI v3 — Chat runtime capability dependencies (intermediate tactical goal)

**Location:** `docs/workitems/completed/20260414-ai-v3-chat-capability-dependencies/` *(archived; closed 2026-04-14)*

**Milestone:** **0.8.0** — see [`../../MILESTONE.md`](../../MILESTONE.md)

**Problem:** The unified chat path (**`UnifiedChatService`** → **`LangChain4jChatRuntime`** → **`LangChain4jAgent`**) rebuilds **`AgentContext`** from **`ChatMetadata`** via **`ProfileRegistry.rehydrate`**, but **`ChatRehydration`** only sets coarse fields (`contextType`, focus entity). **`CapabilityDependencyContainer`** stays **empty**. Profiles such as **`schema-authoring`** require **`SchemaCapabilityDependency`**, **`SqlDialectCapabilityDependency`**, **`SqlQueryCapabilityDependency`**, **`ValueMappingCapabilityDependency`**, etc. Without those, **`CapabilityRegistry.validateDependencies`** fails and schema-facing chats cannot run through the **HTTP chat API**.

**Tactical goal:** **Server-owned wiring:** populate **`capabilityDependencies`** from **existing Spring beans** supplied by **`mill-ai-v3-autoconfigure`**, **`mill-ai-v3-data`**, and host **`data` / `metadata`** modules (**WI-167** — thin assembler, no duplicate config). **`SqlDialectSpec`** aligns with **`mill.data.sql.*`** via host **`SqlAutoConfiguration`** where present; **`ValueMappingResolver`** uses a **stub** until metadata supplies a real bean (**document limitation**). Then **verify** and **document** (**WI-160**). **Profile discovery** over HTTP (**WI-168**). **`mill-ai-v3-cli`** becomes an **HTTP-only test bench** — default **`http://localhost:8080`**, optional **`baseUrl`**, **auth header hooks** (no-op default; see [`v3-mill-ai-v3-cli-http-client.md`](../../../design/agentic/v3-mill-ai-v3-cli-http-client.md)), optional **`userId`/`password`** — **no in-process agent** (**WI-169**).

**Out of scope:** **Frontend / SPA** (**`mill-ui`**) — **separate story**.

**Scope:** **v3 only.** Does not extend v1 NL-to-SQL.

**Relation to other stories:** Independent of [`../20260413-ai-sql-generate-capability/STORY.md`](../20260413-ai-sql-generate-capability/STORY.md) (generate-only `sql-query` / `SqlValidator` contract). This story **consumes** that contract; coordinate merge order if branches touch the same types.

**Documentation quality:** REST controllers in **`mill-ai-v3-service`** use **`io.swagger.v3.oas.annotations`** so **springdoc** can generate OpenAPI (**WI-160**, **WI-168**). **Scope:** only controllers and DTOs **changed for this story** meet the full annotation bar (see **WI-160** explicit list); **WI-169** CLI consumes the server spec, it does not duplicate OpenAPI. Internal services use KDoc per repo rules.

**Closure:** Archived under **`docs/workitems/completed/20260414-ai-v3-chat-capability-dependencies/`** per [`RULES.md`](../../RULES.md) (story closure).

**Branch / tree hygiene (clarification):** If the branch contains **experimental or partial implementation** from an earlier phase (e.g. profile API or CLI paths predating these WIs), **reconcile** files against **WI-167–WI-169** before continuing — remove or align obsolete code so the tree matches the work-item definitions.

**Profile definitions (maintenance):** There is **no** database table for **`AgentProfile`**. Chats persist only **`profileId: String`**. **`ProfileRegistry`** (today **`DefaultProfileRegistry`** + Kotlin profile objects) is the **contract** and **catalog**; **`GET /api/v1/ai/profiles`** lists that bean. Adding or changing profiles = **code** changes (new **`AgentProfile`** + registry entry) or a custom **`ProfileRegistry`** `@Bean`. A future **dynamic** profile store would be a **separate story**. See [`v3-chat-service.md`](../../../design/agentic/v3-chat-service.md) § Runtime rehydration.

## Work Items

Suggested execution order:

1. **WI-167** — Assembler contract + autoconfigure merge (**must** reuse existing data/metadata/autoconfigure beans — see WI text).
2. **WI-168** — Can land **in parallel** with WI-167 (profile HTTP API).
3. **WI-160** — Integration tests, README, OpenAPI bar, pointers to WI-169.
4. **WI-169** — CLI HTTP-only test bench (depends on **WI-168** for profile list; **WI-167** for schema profiles over HTTP).

Checklist:

- [x] WI-167 — `CapabilityDependencyAssembler` in `mill-ai-v3`; Spring implementation + `LangChain4jChatRuntime` wiring in `mill-ai-v3-autoconfigure`; **reuse existing configuration modules** (`WI-167-ai-v3-capability-dependency-assembler.md`)
- [x] WI-168 — `ProfileRegistry.registeredProfiles()` + `GET /api/v1/ai/profiles`; OpenAPI annotations (`WI-168-ai-v3-profile-list-and-inspect-api.md`)
- [x] WI-160 — Integration tests, service/autoconfigure docs, **OpenAPI** acceptance, cross-refs (`WI-160-ai-v3-chat-runtime-capability-dependencies.md`)
- [x] WI-169 — **`mill-ai-v3-cli`**: HTTP-only, **no local agent**; optional **`baseUrl`** (default localhost), **`userId`/`password`**; relaxed auth; test bench only (`WI-169-mill-ai-v3-cli-http-test-bench.md`)

## Related stories

- [`../20260413-ai-sql-generate-capability/STORY.md`](../20260413-ai-sql-generate-capability/STORY.md) — `sql-query` generate semantics and **`SqlValidator`** contract (feeds **`SqlQueryCapabilityDependency`**).

## Design references

- [`../../../design/agentic/README.md`](../../../design/agentic/README.md) — v3 runtime and capabilities.
- [`../../../design/agentic/v3-mill-ai-v3-cli-http-client.md`](../../../design/agentic/v3-mill-ai-v3-cli-http-client.md) — HTTP-only CLI test bench (default URL, auth hooks).
- [`../../../design/ai/capabilities_design.md`](../../../design/ai/capabilities_design.md) — passive capabilities and tools.
