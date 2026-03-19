# WI-078 - AI v3 JPA Persistence Adapters

Status: `planned`
Type: `feature`
Area: `ai`, `platform`
Backlog refs: `PS-5`
Depends on: `WI-073a`, `WI-073`, `WI-074`

## Problem Statement

`WI-073` delivered the Lane 3 chat-memory port contracts (`ChatMemoryStore`,
`LlmMemoryStrategy`, `ConversationMemory`) in `ai/mill-ai-v3` and an
`InMemoryChatMemoryStore` backed by Caffeine for dev/test use.

`WI-074` extends ai/v3 with additional durable persistence ports for:

- routed run events
- chat transcript persistence
- artifact history
- active artifact pointers
- telemetry/statistic persistence where formalized as a durable port

Two gaps remain for Spring Boot deployments:

1. **No Spring wiring for ai/v3 beans.** There is no autoconfiguration module
   that registers the ai/v3 runtime beans and persistence ports as Spring beans.
   Without it every Spring application must wire them manually.

2. **No durable JPA adapters.** Production deployments need recoverable storage
   for both Lane 3 chat memory and the `WI-074` persistence lanes. The
   `mill-persistence` module established by `WI-073a` is the designated home for
   those JPA adapters.

This WI covers both concerns.

## Goal

1. Create `ai/mill-ai-v3-autoconfigure`, a new Spring Boot autoconfiguration
   module that wires ai/v3 runtime beans with sensible in-memory defaults.
2. Implement `JpaChatMemoryStore` in `mill-persistence` as the durable Lane 3
   adapter.
3. Implement JPA adapters in `mill-persistence` for the `WI-074` ports:
   - `RunEventStore`
   - `ConversationStore`
   - `ArtifactStore`
   - `ActiveArtifactPointerStore`
   - telemetry/statistic persistence where the finalized `WI-074` contracts
     expose a durable store
4. Register those durable adapters in `mill-persistence-autoconfigure` so they
   override the in-memory defaults when the persistence module is on the
   classpath.

## Scope

In scope:

- **`ai/mill-ai-v3-autoconfigure`**:
  - Gradle module bootstrap (`build.gradle.kts`, package structure)
  - `MillAiV3AutoConfiguration`
  - default beans via `@ConditionalOnMissingBean` for:
    - `ChatMemoryStore` -> `InMemoryChatMemoryStore`
    - `LlmMemoryStrategy` -> `BoundedWindowMemoryStrategy`
    - `RunEventStore` -> in-memory adapter from `WI-074`
    - `ConversationStore` -> in-memory adapter from `WI-074`
    - `ArtifactStore` -> in-memory adapter from `WI-074`
    - `ActiveArtifactPointerStore` -> in-memory adapter from `WI-074`
  - `AutoConfiguration.imports` entry
  - unit tests for autoconfiguration conditions
- **`mill-persistence`**:
  - Flyway migration for Lane 3 chat memory
  - follow-on Flyway migrations for the `WI-074` tables
  - JPA entities, repositories, and adapters for:
    - chat memory
    - routed run events
    - chat conversations and turns
    - artifact history
    - transcript-turn artifact links
    - active artifact pointers
    - optional telemetry/statistic tables if required by finalized ports
- **`mill-persistence-autoconfigure`**:
  - `MillPersistenceAiAutoConfiguration`
  - `@ConditionalOnMissingBean` registration of the JPA-backed ai/v3 stores
- integration tests against H2 or Testcontainers Postgres verifying round-trip
  save/load/clear behavior and transcript-artifact linkage

Out of scope:

- relation indexing and analytics projections from `WI-075`
- Spring wiring for `LangChain4jAgent`, `SchemaExplorationAgent`, or other
  application-layer agent beans
- advanced eviction or archiving strategies
- multi-tenancy or per-profile partitioning beyond the identifiers already
  present in the domain ports
- changes to the `ChatMemoryStore` or `WI-074` port interfaces
- Chat API or UI surface changes

## Module Layout

```text
ai/
  mill-ai-v3/                      # port contracts, in-memory implementations
  mill-ai-v3-autoconfigure/        # new Spring Boot defaults for ai/v3 beans
persistence/
  mill-persistence/                # JPA entities + ai/v3 persistence adapters
  mill-persistence-autoconfigure/  # overrides with durable beans when present
```

Wiring precedence (highest wins):

1. user-defined `@Bean`
2. `mill-persistence-autoconfigure` JPA-backed ai/v3 stores
3. `mill-ai-v3-autoconfigure` in-memory defaults

## Design Requirements

- `ai/mill-ai-v3` owns only the port interfaces and in-memory adapters; it has
  no Spring or JPA dependencies.
- `mill-ai-v3-autoconfigure` depends on `ai/mill-ai-v3` and
  `spring-boot-autoconfigure`; it must not depend on JPA.
- `mill-persistence` depends on `ai/mill-ai-v3` for the port interfaces; the
  reverse dependency must never exist.
- `mill-persistence-autoconfigure` depends on `mill-persistence` and
  `mill-ai-v3-autoconfigure`; it overrides the in-memory defaults via
  `@ConditionalOnMissingBean`.
- JPA adapters map Mill domain records to entities internally; callers receive
  only Mill domain types.
- The durable schema must preserve the lane distinctions introduced in
  `WI-074`:
  - chat transcript is separate from routed run events
  - artifact history is separate from active artifact pointers
  - telemetry/statistics must not pollute transcript storage
- One chat transcript turn must be able to reference multiple artifacts.
- Routed event content and artifact payloads should be stored in structured form
  suitable for future migration and analytics work, preferably JSON/JSONB when
  available.
- Flyway must manage the schema under the baseline established by `WI-073a`.

## Schema

### Flyway V2 - Lane 3 Chat Memory

```sql
CREATE TABLE chat_memory (
    conversation_id VARCHAR(255) PRIMARY KEY,
    profile_id      VARCHAR(255) NOT NULL,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_chat_memory_profile ON chat_memory (profile_id);

CREATE TABLE chat_memory_message (
    id              BIGSERIAL    PRIMARY KEY,
    conversation_id VARCHAR(255) NOT NULL REFERENCES chat_memory (conversation_id) ON DELETE CASCADE,
    position        INT          NOT NULL,
    role            VARCHAR(32)  NOT NULL,
    content         TEXT         NOT NULL,
    tool_call_id    VARCHAR(255),
    tool_name       VARCHAR(255)
);

CREATE INDEX idx_chat_memory_message_conv ON chat_memory_message (conversation_id, position);
```

### Follow-on Migrations - WI-074 Durable Records

Logical model:

```sql
CREATE TABLE ai_run_event (
    event_id         VARCHAR(255) PRIMARY KEY,
    run_id           VARCHAR(255),
    conversation_id  VARCHAR(255),
    profile_id       VARCHAR(255) NOT NULL,
    runtime_type     VARCHAR(255) NOT NULL,
    kind             VARCHAR(255) NOT NULL,
    category         VARCHAR(64)  NOT NULL,
    route_json       TEXT         NOT NULL,
    content_json     TEXT         NOT NULL,
    created_at       TIMESTAMP    NOT NULL
);

CREATE INDEX idx_ai_run_event_conv ON ai_run_event (conversation_id, created_at);
CREATE INDEX idx_ai_run_event_run ON ai_run_event (run_id, created_at);

CREATE TABLE ai_conversation (
    conversation_id  VARCHAR(255) PRIMARY KEY,
    profile_id       VARCHAR(255) NOT NULL,
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL
);

CREATE TABLE ai_conversation_turn (
    turn_id          VARCHAR(255) PRIMARY KEY,
    conversation_id  VARCHAR(255) NOT NULL REFERENCES ai_conversation (conversation_id) ON DELETE CASCADE,
    role             VARCHAR(32)  NOT NULL,
    text             TEXT,
    position         INT          NOT NULL,
    created_at       TIMESTAMP    NOT NULL
);

CREATE INDEX idx_ai_conversation_turn_conv ON ai_conversation_turn (conversation_id, position);

CREATE TABLE ai_artifact (
    artifact_id      VARCHAR(255) PRIMARY KEY,
    conversation_id  VARCHAR(255) NOT NULL,
    run_id           VARCHAR(255),
    turn_id          VARCHAR(255),
    kind             VARCHAR(255) NOT NULL,
    payload_json     TEXT         NOT NULL,
    created_at       TIMESTAMP    NOT NULL
);

CREATE INDEX idx_ai_artifact_conv ON ai_artifact (conversation_id, created_at);
CREATE INDEX idx_ai_artifact_turn ON ai_artifact (turn_id);

CREATE TABLE ai_turn_artifact (
    turn_id          VARCHAR(255) NOT NULL REFERENCES ai_conversation_turn (turn_id) ON DELETE CASCADE,
    artifact_id      VARCHAR(255) NOT NULL REFERENCES ai_artifact (artifact_id) ON DELETE CASCADE,
    PRIMARY KEY (turn_id, artifact_id)
);

CREATE TABLE ai_active_artifact_pointer (
    conversation_id  VARCHAR(255) NOT NULL,
    pointer_key      VARCHAR(255) NOT NULL,
    artifact_id      VARCHAR(255) NOT NULL REFERENCES ai_artifact (artifact_id) ON DELETE CASCADE,
    updated_at       TIMESTAMP    NOT NULL,
    PRIMARY KEY (conversation_id, pointer_key)
);
```

If `WI-074` formalizes a durable telemetry store, add a separate telemetry table
or aggregate table rather than storing counters on transcript rows.

## Implementation Outline

1. Bootstrap `ai/mill-ai-v3-autoconfigure`.
   - add Gradle module with `spring-boot-autoconfigure`
   - add `MillAiV3AutoConfiguration`
   - register in-memory defaults for `ChatMemoryStore`, `LlmMemoryStrategy`,
     `RunEventStore`, `ConversationStore`, `ArtifactStore`, and
     `ActiveArtifactPointerStore`
   - register via `AutoConfiguration.imports`
   - add unit tests for conditional registration and override behavior
2. Add Flyway `V2__chat_memory.sql` in `mill-persistence`.
3. Add follow-on Flyway migrations for the `WI-074` tables:
   - routed run events
   - chat conversations and turns
   - artifact history
   - transcript-turn artifact links
   - active artifact pointers
   - optional telemetry/statistic tables if required by finalized ports
4. Define JPA entities for:
   - `ChatMemoryEntity`
   - `ChatMemoryMessageEntity`
   - `RunEventEntity`
   - `ConversationEntity`
   - `ConversationTurnEntity`
   - `ArtifactEntity`
   - transcript-turn artifact join entity or equivalent mapping
   - `ActiveArtifactPointerEntity`
5. Define Spring Data repositories for those entities.
6. Implement `JpaChatMemoryStore`.
   - `load(conversationId)` fetches memory and ordered messages
   - `save(memory)` upserts memory and replaces child messages transactionally
   - `clear(conversationId)` deletes the root row with cascade
7. Implement `WI-074` JPA adapters:
   - `JpaRunEventStore`
   - `JpaConversationStore`
   - `JpaArtifactStore`
   - `JpaActiveArtifactPointerStore`
   - telemetry/statistic adapter if the finalized contracts require one
8. Preserve transcript-artifact relationships.
   - maintain turn ordering by `position`
   - allow one turn to reference multiple artifacts
   - keep artifact history independent from active pointers
9. Write `MillPersistenceAiAutoConfiguration`.
   - register each JPA-backed store with `@ConditionalOnMissingBean`
   - let user-defined beans override all defaults
10. Add integration tests:
   - `JpaChatMemoryStoreIT`
   - `JpaConversationStoreIT`
   - `JpaRunEventStoreIT`
   - `JpaArtifactStoreIT`
   - `JpaActiveArtifactPointerStoreIT`
   - multi-artifact transcript scenario with one assistant turn linked to
     `sql-query` and `chart-config`
   - autoconfiguration tests with and without user overrides

## Acceptance Criteria

- `ai/mill-ai-v3-autoconfigure` exists and registers in-memory defaults for
  Lane 3 chat memory and the `WI-074` persistence ports.
- A Spring Boot application depending only on `mill-ai-v3-autoconfigure` gets a
  working in-memory ai/v3 persistence setup with no manual wiring.
- `JpaChatMemoryStore` implements `ChatMemoryStore` with no Spring/JPA types
  visible to callers.
- JPA adapters exist for the finalized `WI-074` persistence ports with no
  Spring/JPA types visible to callers.
- Flyway `V2` applies cleanly on top of the `WI-073a` baseline.
- Follow-on Flyway migrations for `WI-074` apply cleanly and preserve
  separation between routed events, chat transcript, artifact history, and
  active pointers.
- Adding `mill-persistence-autoconfigure` to the classpath transparently
  replaces in-memory ai/v3 stores with JPA-backed adapters.
- Both autoconfiguration modules yield to any user-defined ai/v3 store bean.
- Integration tests cover save/load/clear, transcript ordering, routed-event
  persistence, artifact history, pointer updates, and isolation by
  `conversationId`.
- One chat transcript turn can be durably linked to multiple artifacts.
- `ai/mill-ai-v3` has no Spring, JPA, or autoconfigure dependencies.
