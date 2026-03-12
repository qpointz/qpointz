# WI-067 — AI v3 Multi-Mode Protocol Execution

Status: `planned`  
Type: `✨ feature`  
Area: `ai`, `platform`  
Backlog refs: `TBD`

## Problem Statement

`ai/v3` currently treats protocols too weakly. In practice, protocols mostly document event type
names while LLM-produced output remains text-centric.

This is not sufficient for the intended agent platform. Mill needs to support three different
output styles:

1. unstructured conversational streaming
2. final structured output for specialized tasks
3. structured streaming output for progressive machine-readable artifacts

The runtime must support all three without assuming that provider-native structured streaming is
reliably available across major LLMs.

## Goal

Implement multi-mode protocol execution in `ai/v3` so protocols become executable output
contracts rather than metadata-only declarations.

This work item should make `v3` capable of handling:

- `TEXT`
- `STRUCTURED_FINAL`
- `STRUCTURED_STREAM`

while preserving the existing runtime-owned event model.

## In Scope

1. Evolve the protocol model to describe output mode and payload schemas.
2. Extend the runtime event model to carry protocol-produced structured payloads.
3. Make planner decisions choose the protocol used for synthesis/finalization.
4. Add a protocol execution boundary in the runtime.
5. Implement LangChain4j-backed execution for:
   - text streaming
   - final structured output
   - JSONL/NDJSON-style structured streaming over text
6. Add fallback behavior between protocol modes where explicitly declared.
7. Adapt current Hello World runtime to the new protocol execution flow without regressing
   existing behavior.

## Out of Scope

- Frontend rendering changes beyond the event contract.
- Provider-specific native structured streaming integrations beyond what is needed for the
  current LangChain4j/OpenAI path.
- Full NL2SQL/table/chart protocol adoption in this work item.

## Required Design Decisions

### 1. Protocol modes

Protocols must support exactly three modes:

- `TEXT`
- `STRUCTURED_FINAL`
- `STRUCTURED_STREAM`

### 2. Mode semantics

#### `TEXT`

Use for:

- conversation
- clarification
- free-form explanation

Behavior:

- runtime streams plain text deltas from the model
- no schema validation is performed on LLM text payloads

#### `STRUCTURED_FINAL`

Use for:

- planner outputs
- chart specs
- patch proposals
- typed final result objects

Behavior:

- model returns one final JSON object
- runtime validates it against the protocol final schema
- runtime emits a structured protocol event

#### `STRUCTURED_STREAM`

Use for:

- progressive table-style payloads
- streamed findings
- typed artifact event streams

Behavior:

- runtime asks the model to emit JSON Lines / NDJSON over normal text streaming
- each line must contain:
  - `event`
  - `content`
- runtime parses line-by-line
- runtime validates `content` against the matching protocol event schema
- runtime emits structured protocol stream events as each line is accepted

This mode is application-level structured streaming over text and must not depend on
provider-native structured streaming support.

### 3. Fallback

Protocols may define a fallback mode.

Expected use:

- `STRUCTURED_STREAM` may fall back to `STRUCTURED_FINAL`
- `STRUCTURED_FINAL` may fall back to `TEXT`

Fallback must only happen when explicitly declared in the protocol definition.

## Detailed Changes To Be Done

### A. Core protocol model

Update `ProtocolDefinition` so it becomes a real output contract.

Replace the current event-type-only shape with a model equivalent to:

```kotlin
data class ProtocolDefinition(
    val id: String,
    val description: String,
    val version: String = "1",
    val mode: ProtocolMode,
    val fallbackMode: ProtocolMode? = null,
    val finalSchema: ToolSchema? = null,
    val events: List<ProtocolEventDefinition> = emptyList(),
)

enum class ProtocolMode {
    TEXT,
    STRUCTURED_FINAL,
    STRUCTURED_STREAM,
}

data class ProtocolEventDefinition(
    val type: String,
    val description: String,
    val payloadSchema: ToolSchema,
)
```

Required validation rules:

- `TEXT`
  - `finalSchema == null`
  - `events.isEmpty()`
- `STRUCTURED_FINAL`
  - `finalSchema != null`
- `STRUCTURED_STREAM`
  - `events.isNotEmpty()`

### B. Runtime event model

Keep runtime-owned events separate from protocol-produced payload events.

Add protocol-level event variants to `AgentEvent`, for example:

- `ProtocolTextDelta`
- `ProtocolFinal`
- `ProtocolStreamEvent`

These events should carry:

- `protocolId`
- event type where applicable
- payload JSON string

The runtime should continue to own:

- `run.started`
- `plan.created`
- `tool.call`
- `tool.result`
- `observation.made`
- `answer.completed`

### C. Planner contract

Extend `PlannerDecision` so the planner explicitly chooses the synthesis protocol.

At minimum add:

- `protocolId: String?`

Rules:

- all synthesis-oriented decisions must specify the protocol used for final rendering
- direct conversation answer should choose a text protocol
- specialized outputs should choose structured protocols explicitly

### D. Run state

Extend `RunState` / `RunStep` so protocol execution is visible in workflow state.

At minimum capture:

- selected `protocolId`
- final protocol payload or stream events when relevant

This keeps protocol outputs observable for:

- later persistence
- observer logic
- test assertions

### E. Protocol execution boundary

Add a new runtime abstraction, e.g. `ProtocolExecutor`.

Responsibilities:

- execute the selected protocol mode
- validate outputs
- emit protocol events
- return normalized result to the runtime

This must be separate from:

- planner
- tool executor
- capability declaration

### F. LangChain4j adapter support

Implement all three protocol modes in the LangChain4j adapter layer.

#### `TEXT`

- use existing streaming chat path
- map partial tokens/chunks to protocol text events

#### `STRUCTURED_FINAL`

- request final JSON object
- validate against protocol final schema
- emit one structured protocol final event

#### `STRUCTURED_STREAM`

- instruct the model to emit JSONL/NDJSON
- parse streamed text line-by-line
- validate each event against matching protocol event schema
- emit structured protocol stream events incrementally

### G. Current agent migration

Adapt Hello World first.

Required migration:

- conversation protocol becomes `TEXT`
- final rendering goes through protocol execution instead of direct `message.delta`-only logic
- existing user-visible behavior should remain the same

Schema Exploration may remain `TEXT` initially, but its synthesis path should also move through
the protocol executor so future structured schema outputs can be added cleanly.

## Implementation Order

1. Extend `ProtocolDefinition` and add validation rules.
2. Add protocol-level `AgentEvent` variants.
3. Extend `PlannerDecision` with `protocolId`.
4. Add protocol-aware fields to run state / step state.
5. Introduce `ProtocolExecutor` contract in core.
6. Implement `TEXT` mode in LangChain4j adapter.
7. Implement `STRUCTURED_FINAL` mode in LangChain4j adapter.
8. Migrate Hello World to protocol-aware rendering.
9. Implement `STRUCTURED_STREAM` parsing and validation.
10. Add fallback-mode handling.
11. Extend tests and real `testIT` coverage.

## Acceptance Criteria

- `ai/v3` supports all three protocol modes: `TEXT`, `STRUCTURED_FINAL`, `STRUCTURED_STREAM`.
- Protocols describe executable payload contracts rather than event names only.
- Runtime events and protocol payload events are explicitly separated.
- Hello World continues to work after migration to protocol-aware rendering.
- At least one protocol path is validated for each supported mode in tests.
- Structured streaming is implemented as application-level JSONL/NDJSON parsing over text and
  does not assume provider-native structured streaming.

## Test Plan

Unit tests:

- protocol definition validation for all three modes
- final-schema validation behavior
- structured-stream event-schema validation behavior
- fallback-mode selection logic

Runtime tests:

- `TEXT` protocol emits protocol text delta events
- `STRUCTURED_FINAL` protocol emits a validated final payload event
- `STRUCTURED_STREAM` protocol parses JSONL lines into validated stream events

Integration tests:

- Hello World still passes real-model `testIT`
- at least one structured-final path is exercised end to end

## Deliverables

- Core protocol model upgraded to support three protocol modes.
- Runtime protocol execution boundary added.
- LangChain4j adapter supports text, structured final, and structured stream output handling.
- Existing Hello World agent migrated to the new protocol execution model.
- This work item definition (`docs/workitems/WI-067-ai-v3-multi-mode-protocol-execution.md`).
