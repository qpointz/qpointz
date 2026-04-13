# Mill AI v3 — `mill-ai-v3-data` boundary (schema port and SQL validation)

**Status:** in progress (see [`docs/workitems/in-progress/ai-v3-schema-exploration-port/STORY.md`](../../workitems/in-progress/ai-v3-schema-exploration-port/STORY.md))  
**Milestone:** 0.8.0 (target)

## Intent

Keep **`ai/mill-ai-v3`** free of **`mill-data-*`** dependencies by introducing **AI-owned ports** for:

- **Schema exploration** — list schemas, tables, columns, relations for the `schema` capability (today wired through **`SchemaFacetService`**).
- **SQL validation** — the **`SqlValidator`** / **`SqlValidationService`** contracts stay in **`mill-ai-v3`**; **engine-backed** validation implementations live beside Mill Data integration.

The Gradle module **`ai/mill-ai-v3-data`** holds **adapters** that implement those ports using **`io.qpointz.mill.data.schema.SchemaFacetService`**, **`mill-sql`**, and related stack components as needed.

**SQL validation testing (WI-165):** unit tests rely on **mocks**; **`testIT`** exercises **parse + schema-bound** behaviour against shared fixtures (e.g. **Skymill + flow**, same pattern as **`FlowDescriptorMetadataSourceIT`** in **`mill-data-backends`**).

## Module responsibilities

| Module | Role |
|--------|------|
| **`mill-ai-v3`** | Capability manifests, **`SchemaExplorationPort`** (name TBD in implementation), **`SqlValidator`** fun interface, thin **`validateSql`**, **`LangChain4jAgent` / `SchemaExplorationAgent`**, no `io.qpointz.mill.data.*` imports after refactor. |
| **`mill-ai-v3-data`** | **`SchemaFacetService` → schema port** adapter; optional **default `SqlValidator`** using dialect/engine validation; demo fixtures if shared by CLI. |
| **`mill-ai-v3-autoconfigure`** | **Canonical Spring wiring**: register **`SchemaExplorationPort`** and (with **WI-165**) optional default **`SqlValidator`** beans using **`@ConditionalOnMissingBean`**; extend **`MillAiV3SqlValidatorAutoConfiguration`** patterns as appropriate. |
| **`mill-ai-v3-cli`** | Optional **playground / test bench**: may depend on **`mill-ai-v3-data`** **temporarily** for standalone REPL; not the canonical integration surface. |
| **`mill-ai-v3-service`** | **Primary Boot consumer** of **`mill-ai-v3-autoconfigure`**: **`SchemaExplorationPort`**, **`SqlValidationService`**, and related beans — not ad hoc **`SchemaFacetService`** wiring in application code once the story is complete. |

## Dependency rule of thumb

- **`mill-ai-v3-service`** (and other production hosts) consume **`SchemaExplorationPort`** / **`SqlValidationService`** from Spring via **`mill-ai-v3-autoconfigure`** — not **`SchemaFacetService`** directly in **`mill-ai-v3`** APIs.
- **`mill-ai-v3-cli`** is not the canonical wiring surface; **`mill-ai-v3-autoconfigure`** is.

## Related documents

- [`developer-manual/v3-developer-capabilities-profiles-and-dependencies.md`](./developer-manual/v3-developer-capabilities-profiles-and-dependencies.md) — capabilities, dependencies, profiles.
- [`v3-chat-service.md`](./v3-chat-service.md) — unified chat and runtime rehydration (intersects **`WI-160`** capability wiring).
