# AI v3 — Chat runtime capability dependencies (intermediate tactical goal)

**Milestone:** **TBD** (align with next milestone when scheduled)

**Problem:** The unified chat path (**`UnifiedChatService`** → **`LangChain4jChatRuntime`** → **`LangChain4jAgent`**) rebuilds **`AgentContext`** from **`ChatMetadata`** via **`ProfileRegistry.rehydrate`**, but **`ChatRehydration`** only sets coarse fields (`contextType`, focus entity). **`CapabilityDependencyContainer`** stays **empty**. Profiles such as **`schema-authoring`** require **`SchemaCapabilityDependency`**, **`SqlDialectCapabilityDependency`**, **`SqlQueryCapabilityDependency`**, **`ValueMappingCapabilityDependency`**, etc. Without those, **`CapabilityRegistry.validateDependencies`** fails and schema-facing chats cannot run through the **HTTP chat API** — even though **`mill-ai-v3-cli`** can run **`SchemaExplorationAgent`** in-process with manual wiring.

**Tactical goal:** Make **chat REST + thin HTTP client (including evolved `mill-ai-v3-cli`)** **fully working** for schema-capable profiles: same capability surface as in-process CLI, with **wiring owned by the server** (Spring beans → **`AgentContext`**). This is an **intermediate** milestone before **`mill-ui`** relies on the same API.

**Scope:** **v3 only.** Does not extend v1 NL-to-SQL.

**Relation to other stories:** Independent of [`../../completed/20260413-ai-sql-generate-capability/STORY.md`](../../completed/20260413-ai-sql-generate-capability/STORY.md) (generate-only `sql-query` / `SqlValidator` contract). This story **consumes** a working **`SqlQueryCapabilityDependency`** shape once that story lands; coordinate merge order if both branches touch the same types.

## Work Items

- [ ] WI-160 — Chat runtime: inject capability dependencies into `AgentContext` (`WI-160-ai-v3-chat-runtime-capability-dependencies.md`)

## Related stories

- [`../../completed/20260413-ai-sql-generate-capability/STORY.md`](../../completed/20260413-ai-sql-generate-capability/STORY.md) — `sql-query` generate semantics and **`SqlValidator`** contract (feeds **`SqlQueryCapabilityDependency`**).

## Design references

- [`../../../design/agentic/README.md`](../../../design/agentic/README.md) — v3 runtime and capabilities.
- [`../../../design/ai/capabilities_design.md`](../../../design/ai/capabilities_design.md) — passive capabilities and tools.
