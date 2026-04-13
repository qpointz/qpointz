# WI-160 — Chat Runtime: Inject Capability Dependencies into `AgentContext`

Status: `planned`  
Type: `✨ feature` / `🧪 test`  
Area: `ai`  
Milestone: `TBD`

## Problem Statement

**`LangChain4jChatRuntime.send`** calls **`profileRegistry.rehydrate(metadata)`** and passes **`rehydration.agentContext`** into **`LangChain4jAgent`**. Today **`ProfileRegistry.rehydrate`** (see **`ChatRehydration`**) builds:

```kotlin
AgentContext(
    contextType = metadata.contextType ?: "general",
    focusEntityType = metadata.contextEntityType,
    focusEntityId = metadata.contextId,
    capabilityDependencies = CapabilityDependencyContainer.empty(),
)
```

So **`capabilityDependencies`** is always **empty**. **`LangChain4jAgent`** then calls **`CapabilityRegistry.capabilitiesFor(profile, context)`**, which **validates** each capability’s **`requiredDependencies`**. Profiles that include **`schema`**, **`sql-query`**, **`sql-dialect`**, **`value-mapping`**, etc. **cannot** be instantiated — the chat **REST** path fails for **`schema-authoring`** (and similar) even though **`mill-ai-v3-cli`** can run **`SchemaExplorationAgent`** with explicit **`SqlQueryCapabilityDependency`** and friends.

## Goal

**Server-owned wiring:** For each chat turn (or at rehydration time), populate **`AgentContext.capabilityDependencies`** from **Spring** (or the host runtime) so **`LangChain4jAgent`** materializes the **same** capability set the profile declares — **mirroring** what in-process agents receive today.

**Tactical acceptance:** **`POST /api/v1/ai/chats`** with **`profileId`** = **`schema-authoring`** (and configured defaults) followed by **`POST …/messages`** completes a turn **without** missing-dependency failures; tools that need **`SchemaFacetService`**, dialect spec, **`SqlValidator`** / **`SqlValidationService`**, value-mapping resolver, etc. receive them via **`CapabilityDependencyContainer`**.

## In Scope

1. **Design point:** Decide **where** dependencies are assembled — e.g. extend **`ChatRehydration`** with a pluggable **`AgentContextFactory`**, or enrich context inside **`LangChain4jChatRuntime`** / **`UnifiedChatService`** using beans from **`mill-ai-v3-autoconfigure`** (and **`data` / `metadata`** as needed).
2. **Implementation:** Wire **`CapabilityDependencyContainer`** entries **per capability id** (or a single container built for the active **`profileId`**) consistent with **`SqlQueryCapabilityProvider`**, **`SchemaCapabilityProvider`**, etc.
3. **`mill-ai-v3-service` / autoconfigure:** Register beans and configuration so the **chat** application context provides real collaborators (or documented test doubles for IT) where **`WI-159`** / **`ai-sql-generate-capability`** define **`SqlValidator`**.
4. **Tests:** Integration test that creates a chat with **`schema-authoring`** (or minimal profile requiring deps) and sends a message **without** dependency validation errors; unit tests for the factory if split out.

## Out of Scope

- Replacing **`LangChain4jAgent`** with **`SchemaExplorationAgent`** in the chat path — not required if dependency injection makes **`LangChain4jAgent`** sufficient.
- **`mill-ui`** React work — this WI enables the **API**; UI swaps mock **`chatService`** separately.
- Full **sql.generate** / execute split — delivered in [`../../completed/20260413-ai-sql-generate-capability/`](../../completed/20260413-ai-sql-generate-capability/); this WI assumes **`SqlQueryCapabilityDependency`** matches the current contract from that work.

## Acceptance Criteria

- **`AgentContext`** used by **`LangChain4jChatRuntime`** for profiles that declare **`schema`**, **`sql-query`**, **`sql-dialect`**, **`value-mapping`** includes the **required** dependency types for those capabilities.
- **No** `IllegalArgumentException` / `validateDependencies` failure for **`schema-authoring`** when dependencies are configured in the Spring context.
- **Documentation:** KDoc on new extension points; short note in **`mill-ai-v3-service`** or autoconfigure README on which beans must exist for schema chats.
- **Thin CLI story:** Documented path for **`mill-ai-v3-cli`** to call **only** HTTP + SSE (no in-process **`SchemaExplorationAgent`**) against the same server — can be a follow-up WI or a subsection of this WI’s commit message.

## Deliverables

- This work item definition.
- Code and tests on the story branch per `docs/workitems/RULES.md`.

## Reference

- **`ChatRehydration`:** `ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/profile/ChatRehydration.kt`
- **`LangChain4jChatRuntime`:** `ai/mill-ai-v3-autoconfigure/.../LangChain4jChatRuntime.kt`
- **`UnifiedChatService`:** `ai/mill-ai-v3-service/.../UnifiedChatService.kt`
- **`SchemaAuthoringAgentProfile`:** `ai/mill-ai-v3/.../SchemaAuthoringAgentProfile.kt`
- Story: [`STORY.md`](STORY.md)
