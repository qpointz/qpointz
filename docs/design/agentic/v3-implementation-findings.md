# Agentic Runtime v3 - Implementation Findings

**Status:** Review snapshot  
**Date:** March 21, 2026  
**Scope:** `ai/mill-ai-v3*` modules and related runtime/chat-service integration

## 1. Purpose

This document captures prioritized implementation findings discovered by reviewing:

- `ai/mill-ai-v3`
- `ai/mill-ai-v3-service`
- `ai/mill-ai-v3-autoconfigure`
- `ai/mill-ai-v3-persistence`

against current design/workitem intent in `docs/design/agentic/` and `docs/workitems/`.

It focuses on:

- architectural issues
- potential improvements
- code issues
- test gaps

## 2. Prioritized Findings

## 2.1 High Priority

1. **Missing ownership checks on chat-by-id operations**
   - **Category:** Architecture, security, code issue
   - **What is happening:** `UnifiedChatService` enforces user identity for list/create/context lookup, but not for `getChat`, `updateChat`, `deleteChat`, or `sendMessage` paths that load by `chatId`.
   - **Risk:** Cross-user read/update/delete/message-send if a `chatId` is known.
   - **Where:** `ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/UnifiedChatService.kt`
   - **Improvement:** Enforce `metadata.userId == userIdResolver.resolve()` for all `chatId` operations before returning data or mutating state.

2. **`ConversationStore.delete` contract is not implemented in JPA adapter**
   - **Category:** Code issue, data lifecycle
   - **What is happening:** Service invokes `conversationStore.delete(chatId)` on hard-delete, but `ConversationStore` default is no-op and `JpaConversationStore` does not override.
   - **Risk:** Durable transcript rows can remain after chat deletion in JPA mode.
   - **Where:**
     - `ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/persistence/ConversationStore.kt`
     - `ai/mill-ai-v3-persistence/src/main/kotlin/io/qpointz/mill/persistence/ai/jpa/adapters/JpaConversationStore.kt`
   - **Improvement:** Implement `override fun delete(conversationId: String)` in `JpaConversationStore` and verify expected cascade behavior.

3. **Hard-delete semantics are incomplete for artifacts and run-events**
   - **Category:** Architecture, persistence boundaries
   - **What is happening:** `UnifiedChatService.deleteChat` clears metadata, transcript, and memory, but does not explicitly remove artifacts/run-events. Baseline schema does not define FK from `ai_artifact`/`ai_run_event` to `ai_conversation`.
   - **Risk:** Orphaned records and retention drift versus documented "hard delete" semantics.
   - **Where:**
     - `ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/UnifiedChatService.kt`
     - `persistence/mill-persistence/src/main/resources/db/migration/V1__ai_v3_baseline.sql`
   - **Improvement:** Define and implement one explicit delete policy: transactional cascade at write time or guaranteed async cleanup with observability.

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

1. **No ownership/authorization tests for `chatId` operations**
   - Missing service/controller tests that prove non-owner cannot read/update/delete/send.

2. **No JPA delete contract test for transcript deletion**
   - Missing integration test that asserts `conversationStore.delete` removes conversation+turns.

3. **No explicit artifact/run-event cleanup tests during chat hard-delete**
   - Current tests do not verify final persistence state across all lanes after delete.

4. **No focused unit tests for `ChatRuntimeEventToSseMapper`**
   - Mapper behavior is exercised indirectly, but lacks dedicated edge-case tests for completed-only, chunked, and failure sequencing.

5. **No real default-runtime streaming assertion**
   - Service `testIT` replaces runtime with a stub and therefore does not validate streaming behavior of `LangChain4jChatRuntime` + `LangChain4jAgent`.

6. **Schema exploration scenario tests planned in workitems are not present**
   - Gaps remain relative to `WI-059` and `WI-066` scenario coverage goals.

## 4. Recommended Execution Order

1. Enforce ownership checks on all `chatId` operations.
2. Implement and test `JpaConversationStore.delete`.
3. Define and implement explicit artifact/run-event delete policy.
4. Validate profile IDs during chat creation.
5. Introduce admission + tool authorization boundaries.
6. Add targeted tests for ownership, delete semantics, SSE mapper, and scenario coverage.

