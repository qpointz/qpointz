# WI-160 — Chat Runtime: Inject Capability Dependencies into `AgentContext`

Status: `planned`  
Type: `✨ feature` / `🧪 test`  
Area: `ai`  
Milestone: `TBD`

**Depends on:** [**WI-167**](WI-167-ai-v3-capability-dependency-assembler.md) — introduces the **`CapabilityDependencyAssembler`** contract in **`mill-ai-v3`**, the default Spring implementation and **`LangChain4jChatRuntime`** merge in **`mill-ai-v3-autoconfigure`**. This WI covers **verification**, **documentation**, and **acceptance** after that wiring exists.

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

So **`capabilityDependencies`** is always **empty**. **`LangChain4jAgent`** then calls **`CapabilityRegistry.capabilitiesFor(profile, context)`**, which **validates** each capability’s **`requiredDependencies`**. Profiles that include **`schema`**, **`sql-query`**, **`sql-dialect`**, **`value-mapping`**, etc. **cannot** be instantiated — the chat **REST** path fails for **`schema-authoring`** (and similar). **Target:** server-side assembly via [**WI-167**](WI-167-ai-v3-capability-dependency-assembler.md); **`mill-ai-v3-cli`** moves to HTTP-only per [**WI-169**](WI-169-mill-ai-v3-cli-http-test-bench.md) (no in-process agent).

## Goal

**Server-owned wiring:** For each chat turn (or at rehydration time), populate **`AgentContext.capabilityDependencies`** from **Spring** (or the host runtime) so **`LangChain4jAgent`** materializes the **same** capability set the profile declares — **mirroring** what in-process agents receive today.

**Tactical acceptance:** **`POST /api/v1/ai/chats`** with **`profileId`** = **`schema-authoring`** (and configured defaults) followed by **`POST …/messages`** completes a turn **without** missing-dependency failures; tools that need **`SchemaFacetService`**, dialect spec, **`SqlValidator`** / **`SqlValidationService`**, value-mapping resolver, etc. receive them via **`CapabilityDependencyContainer`**.

## In Scope

1. **Integration tests:** Create a chat with **`schema-authoring`** (or **`schema-exploration`** as a minimal case) and **`POST …/messages`** — **no** `validateDependencies` / missing-dependency failures when the Spring context supplies the collaborators expected by [**WI-167**](WI-167-ai-v3-capability-dependency-assembler.md).
2. **`mill-ai-v3-service` / autoconfigure README (or equivalent):** Document which beans must exist for schema-capable chats in production and what test doubles are used in **`testIT`**.
3. **OpenAPI / springdoc (explicit scope — no adjacent churn):** Only files **touched for this story’s** changes in **`mill-ai-v3-service`**:
   - **`AiChatController.kt`** — **must** meet the annotation bar **only if** this branch edits it.
   - **`AiProfileController.kt`** and DTOs it uses (e.g. **`AgentProfileResponse`** in **`dto`**).
   - Any **new** HTTP types under **`io.qpointz.mill.ai.service.dto`** introduced **by this story**.
   **Exclude:** unrelated controllers (admin, future routes), **`GlobalExceptionHandler`** unless edited for this WI, wholesale reannotation of existing files not in the diff. Annotation pattern: **`@Tag`, `@Operation`, `@ApiResponses`**, **`Content` / `Schema(implementation = …)`** — align with **[`AiChatController`](../../../../ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/AiChatController.kt)**.
4. Cross-reference [**WI-169**](WI-169-mill-ai-v3-cli-http-test-bench.md) in README: HTTP-only CLI test bench (CLI implementation is WI-169, not this WI).
5. **KDoc** on any test-only helpers or service-level hooks added solely for this WI.

## Out of Scope

- Replacing **`LangChain4jAgent`** with **`SchemaExplorationAgent`** in the chat path — not required if dependency injection makes **`LangChain4jAgent`** sufficient.
- **Frontend / SPA integration** — separate story; this work item covers **API** readiness, tests, and docs only.
- Full **sql.generate** / execute split — delivered in [`../20260413-ai-sql-generate-capability/`](../20260413-ai-sql-generate-capability/); this WI assumes **`SqlQueryCapabilityDependency`** matches the current contract from that work.

## Acceptance Criteria

- **`AgentContext`** used by **`LangChain4jChatRuntime`** for profiles that declare **`schema`**, **`sql-query`**, **`sql-dialect`**, **`value-mapping`** includes the **required** dependency types for those capabilities.
- **No** `IllegalArgumentException` / `validateDependencies` failure for **`schema-authoring`** when dependencies are configured in the Spring context.
- **Documentation:** KDoc on new extension points; short note in **`mill-ai-v3-service`** or autoconfigure README on which beans must exist for schema chats; pointer to [**WI-169**](WI-169-mill-ai-v3-cli-http-test-bench.md) for CLI.
- **OpenAPI:** Controllers/DTOs in scope above meet the annotation bar (springdoc generation).
- **Partial stack behaviour** (document in README / cross-ref **WI-167**): if optional beans are missing, **message turn** may fail at capability validation while **profile list** still works — align error responses with existing chat/runtime handlers.

## Deliverables

- This work item definition.
- Tests and documentation on the story branch per `docs/workitems/RULES.md` (implementation of the assembler and runtime merge lives under **WI-167**).

## Reference

- [**WI-167**](WI-167-ai-v3-capability-dependency-assembler.md) — contract and autoconfigure wiring
- [**WI-169**](WI-169-mill-ai-v3-cli-http-test-bench.md) — HTTP-only CLI test bench
- **`ChatRehydration`:** `ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/profile/ChatRehydration.kt`
- **`LangChain4jChatRuntime`:** `ai/mill-ai-v3-autoconfigure/.../LangChain4jChatRuntime.kt`
- **`UnifiedChatService`:** `ai/mill-ai-v3-service/.../UnifiedChatService.kt`
- **`SchemaAuthoringAgentProfile`:** `ai/mill-ai-v3/.../SchemaAuthoringAgentProfile.kt`
- Story: [`STORY.md`](STORY.md)
