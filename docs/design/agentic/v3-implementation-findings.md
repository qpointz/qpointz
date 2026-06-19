# Agentic Runtime v3 - Implementation Findings

**Status:** Review snapshot (partially superseded — see §5)  
**Date:** March 21, 2026  
**Scope:** `ai/mill-ai*` modules and related runtime/chat-service integration

## 1. Purpose

This document captures prioritized implementation findings discovered by reviewing:

- `ai/mill-ai`
- `ai/mill-ai-service`
- `ai/mill-ai-autoconfigure`
- `ai/mill-ai-persistence`

against current design/workitem intent in `docs/design/agentic/` and `docs/workitems/`.

It focuses on:

- architectural issues
- potential improvements
- code issues
- test gaps

## 2. Prioritized Findings

## 2.1 High Priority

1. **Missing ownership checks on chat-by-id operations** — **Resolved (WI-318)**
   - **Category:** Architecture, security, code issue
   - **Was:** `UnifiedChatService` enforced user identity for list/create/context lookup, but not for `getChat`, `updateChat`, `deleteChat`, or `sendMessage` paths that load by `chatId`.
   - **Resolution:** `SecurityUserIdResolver` + ownership assertions on all `chatId` operations. See [`WI-318`](../../workitems/completed/20260619-ai-chat-persistence/WI-318-auth-bound-user-ownership.md).

2. **`ConversationStore.delete` contract is not implemented in JPA adapter** — **Resolved (WI-324)**
   - **Category:** Code issue, data lifecycle
   - **Was:** Service invoked `conversationStore.delete(chatId)` on hard-delete, but JPA adapter did not override the default no-op.
   - **Resolution:** `JpaConversationStore.delete` removes turns; `ai_chat` CASCADE FKs (V12) remove satellites. **`JpaChatDeleteCascadeIT`**, **`AiChatPersistenceIT`**.

3. **Hard-delete semantics are incomplete for artifacts and run-events** — **Resolved (WI-324)**
   - **Category:** Architecture, persistence boundaries
   - **Was:** No FK from artifact/run-event tables to `ai_chat`; orphans possible after hard-delete.
   - **Resolution:** Flyway **V12** adds `ON DELETE CASCADE` from `ai_chat_memory`, `ai_chat_artifact`, `ai_chat_run_event` to `ai_chat`; table names aligned under `ai_chat_*` (**V11**). See [`db-naming-convention.md`](../persistence/db-naming-convention.md).

## 2.2 Medium Priority

4. **Default runtime is not stream-first in practice**
   - **Category:** Architecture mismatch
   - **What is happening:** `LangChain4jAgent` primarily uses `complete()` and emits final `AnswerCompleted`; `MessageDelta` is generally not emitted on normal path. A streaming helper exists but is unused in this class.
   - **Risk:** Drift from stream-first design target and weaker progressive UX behavior.
   - **Where:** `ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/runtime/langchain4j/LangChain4jAgent.kt`
   - **Improvement:** Route final synthesis through streaming path (or explicitly mark this runtime mode as non-streaming and isolate stream-first behavior in a different implementation).

5. **Unknown profile IDs are accepted at chat creation**
   - **Category:** Code issue, API robustness
   - **What is happening:** `createChat` persists provided/default profile without validation; runtime later fails on rehydration if profile is unknown.
   - **Risk:** Delayed runtime failure instead of fast API validation.
   - **Where:**
     - `ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/UnifiedChatService.kt`
     - `ai/mill-ai-v3-autoconfigure/src/main/kotlin/io/qpointz/mill/ai/autoconfigure/chat/LangChain4jChatRuntime.kt`
   - **Improvement:** Validate profile existence in service create path using `ProfileRegistry`; return 4xx on invalid profile.

6. **Capability admission and tool authorization boundaries are not yet explicit**
   - **Category:** Architecture gap
   - **What is happening:** Registry performs context/dependency filtering, but no trust/admission layer. Tool invocations execute directly from planned calls.
   - **Risk:** Future policy/tenant/side-effect controls become invasive to retrofit.
   - **Where:**
     - `ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/core/capability/CapabilityRegistry.kt`
     - `ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/runtime/langchain4j/LangChain4jAgent.kt`
   - **Improvement:** Add explicit admission and per-tool authorization seams before invocation, plus denial events.

## 2.3 Lower Priority

7. **Profile registry remains compile-time only**
   - **Category:** Architectural evolution
   - **What is happening:** `DefaultProfileRegistry` is static and manually enumerated.
   - **Risk:** Profile growth increases drift risk and operational coupling.
   - **Where:** `ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/profile/ProfileRegistry.kt`
   - **Improvement:** Move to Spring-managed or data-driven registry with startup validation and diagnostics.

## 3. Test Gaps

1. **Ownership/authorization tests for `chatId` operations** — **Partially addressed (WI-318, WI-320)** — `AiChatPersistenceIT` multi-user isolation; dedicated controller denial tests remain optional (**BACKLOG A-81**).

2. **JPA delete contract test for transcript deletion** — **Resolved (WI-324)** — `JpaChatDeleteCascadeIT`, `JpaChatRegistryIT.delete`.

3. **Artifact/run-event cleanup tests during chat hard-delete** — **Resolved (WI-324)** — `JpaChatDeleteCascadeIT`.

4. **No focused unit tests for `ChatRuntimeEventToSseMapper`**
   - Mapper behavior is exercised indirectly, but lacks dedicated edge-case tests for completed-only, chunked, and failure sequencing.

5. **No real default-runtime streaming assertion**
   - Service `testIT` replaces runtime with a stub and therefore does not validate streaming behavior of `LangChain4jChatRuntime` + `LangChain4jAgent`.

6. **Schema exploration scenario tests planned in workitems are not present**
   - Gaps remain relative to `WI-059` and `WI-066` scenario coverage goals.

## 4. Recommended Execution Order

1. ~~Enforce ownership checks on all `chatId` operations.~~ **Done (WI-318)**
2. ~~Implement and test `JpaConversationStore.delete`.~~ **Done (WI-324)**
3. ~~Define and implement explicit artifact/run-event delete policy.~~ **Done (WI-324)**
4. Validate profile IDs during chat creation.
5. Introduce admission + tool authorization boundaries.
6. Add targeted tests for ownership denial, SSE mapper, and scenario coverage (**A-81** remainder).

## 5. Story closures (June 2026)

| Story | WIs | Archive |
|-------|-----|---------|
| **ai-chat-persistence** | WI-317–WI-321 (+ WI-322 in pgvector story) | [`completed/20260619-ai-chat-persistence/`](../../workitems/completed/20260619-ai-chat-persistence/STORY.md) |
| **ai-chat-table-naming** | WI-323–WI-324 | [`completed/20260619-ai-chat-table-naming/`](../../workitems/completed/20260619-ai-chat-table-naming/STORY.md) |

