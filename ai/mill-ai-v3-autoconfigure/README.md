# mill-ai-v3-autoconfigure

**Assembly / starter module for Boot hosts:** declare **`implementation(project(":ai:mill-ai-v3-autoconfigure"))`** (or the Mill `ai-chat-service` feature) and you get **`mill-ai-v3-service`** (REST + SSE + OpenAPI) transitively, plus the auto-configurations below.

Spring Boot autoconfiguration for AI v3: LangChain4j chat model, profile registry, chat runtime, **UnifiedChatService** wiring, and **capability dependency assembly** for schema-capable agent profiles.

## Schema-capable chats (`schema-authoring`, `schema-exploration`, …)

For profiles that declare `schema`, `sql-dialect`, `sql-query`, and/or `value-mapping`, the chat runtime must populate `AgentContext.capabilityDependencies` before `CapabilityRegistry` validates capabilities.

Beans (when present on the application classpath) are combined by `SpringCapabilityDependencyAssembler`:

| Collaborator | Typical source |
|--------------|----------------|
| `SchemaCatalogPort` | `MillAiV3DataAutoConfiguration` when `SchemaFacetService` exists |
| `SqlValidator` | Same, when `SqlProvider` exists (`BackendSqlValidator`) |
| `SqlQueryToolHandlers.SqlValidationService` | `MillAiV3SqlValidatorAutoConfiguration` from `SqlValidator`, or your own bean |
| `SqlDialectSpec` | Host `SqlAutoConfiguration` (`mill.data.sql.*`) when `mill-data-autoconfigure` is on the classpath |
| `ValueMappingResolver` | Your metadata integration, or the default **`MockValueMappingResolver`** stub (empty mappings) |

If a required collaborator is missing for a profile capability, **message send** fails at capability validation (not at chat create). Profile list HTTP endpoints still work.

## Overrides

- `CapabilityDependencyAssembler` — replace to customize dependency wiring.
- `ValueMappingResolver` — replace the stub with a real resolver when available.

See also: `docs/design/agentic/v3-chat-service.md`, work items WI-167 / WI-160.
