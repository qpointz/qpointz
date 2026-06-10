# Mill AI v3 — `mill-ai-v3-data` boundary (schema catalog and SQL validation)

**Status:** complete — story closed 2026-04-14 (see [`docs/workitems/completed/20260414-ai-v3-schema-exploration-port/STORY.md`](../../workitems/completed/20260414-ai-v3-schema-exploration-port/STORY.md))  
**Milestone:** 0.8.0 (target)

## Intent

Keep **`ai/mill-ai-v3`** free of **`mill-data-*`** dependencies by introducing **AI-owned contracts** for:

- **Schema exploration** — list schemas, tables, columns, relations for the `schema` capability (today wired through **`SchemaFacetService`**).
- **SQL validation** — the **`SqlValidator`** / **`SqlValidationService`** contracts stay in **`mill-ai-v3`**; **engine-backed** validation implementations live beside Mill Data integration.

The Gradle module **`ai/mill-ai-v3-data`** holds **adapters** that implement those contracts using **`io.qpointz.mill.data.schema.SchemaFacetService`**, **`mill-sql`**, and related stack components as needed.

**SQL validation testing:** unit tests rely on **mocks**; **`testIT`** exercises **parse + schema-bound** behaviour against shared fixtures (e.g. **Skymill + flow**, same pattern as **`FlowDescriptorMetadataSourceIT`** in **`mill-data-backends`**).

## Module responsibilities

| Module | Role |
|--------|------|
| **`mill-ai-v3`** | Capability manifests, **`SchemaCatalogPort`**, **`SqlValidator`** fun interface, thin **`validateSql`**, **`LangChain4jAgent` / `SchemaExplorationAgent`**, no `io.qpointz.mill.data.*` imports in this module. |
| **`mill-ai-v3-data`** | **`SchemaFacetService` → `SchemaCatalogPort`** adapter (`SchemaFacetCatalogAdapter`); optional **default `SqlValidator`** using dialect/engine validation; demo fixtures if shared by CLI. |
| **`mill-ai-v3-autoconfigure`** | **Canonical Spring wiring**: register **`SchemaCatalogPort`** and optional default **`SqlValidator`** beans using **`@ConditionalOnMissingBean`**; extend **`AiV3SqlValidatorAutoConfiguration`** patterns as appropriate. |
| **`mill-ai-v3-cli`** | Optional **playground / test bench**: may depend on **`mill-ai-v3-data`** **temporarily** for standalone REPL; not the canonical integration surface. |
| **`mill-ai-v3-service`** | **Primary Boot consumer** of **`mill-ai-v3-autoconfigure`**: **`SchemaCatalogPort`**, **`SqlValidationService`**, and related beans — not ad hoc **`SchemaFacetService`** wiring in application code. |

## Dependency rule of thumb

- **`mill-ai-v3-service`** (and other production hosts) consume **`SchemaCatalogPort`** / **`SqlValidationService`** from Spring via **`mill-ai-v3-autoconfigure`** — not **`SchemaFacetService`** directly in **`mill-ai-v3`** APIs.
- **`mill-ai-v3-cli`** is not the canonical wiring surface; **`mill-ai-v3-autoconfigure`** is.

## Related documents

- [`developer-manual/v3-developer-capabilities-profiles-and-dependencies.md`](./developer-manual/v3-developer-capabilities-profiles-and-dependencies.md) — capabilities, dependencies, profiles.
- [`v3-chat-service.md`](v3-chat-service.md) — unified chat and runtime rehydration (intersects **`WI-160`** capability wiring).

## Related work (value mappings)

**`mill-ai-v3-data` `testIT`** includes Skymill + Chroma scenarios (e.g. **`ChromaSkymillDistinctVectorIT`**) that exercise **`ValueMappingService`** → **`VectorMappingSynchronizer`** against a configured **`EmbeddingStore`**, alongside the schema/SQL validation flows above — see [**WI-177**](../../workitems/completed/20260416-implement-value-mappings/WI-177-vector-store-harness.md) / [**WI-180**](../../workitems/completed/20260416-implement-value-mappings/WI-180-value-mapping-service-orchestrator.md) and [`../ai/mill-ai-configuration.md`](../ai/mill-ai-configuration.md).
