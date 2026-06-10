# Agentic Runtime v3 - Deterministic Validation Harness

**Status:** Planning
**Date:** March 12, 2026
**Scope:** `ai/v3` scenario validation, deterministic replay, and real-LLM `testIT` layering

---

## 1. Purpose

`ai/v3` needs a validation approach that is stronger than unit tests and less fragile than
running every scenario against a live model.

The deterministic validation harness exists to give the runtime a repeatable contract test
layer for:

- capability discovery and profile resolution
- planner, executor, and observer transitions
- streamed runtime event ordering
- tool contract validation
- run termination behavior

This harness complements, but does not replace, real-LLM `testIT`.

---

## 2. Why a deterministic harness is required

Real-model integration is necessary to validate:

- provider fit
- streaming integration
- structured-output prompting under live conditions
- end-to-end infrastructure wiring

It is not sufficient as the primary regression layer because real-model runs are:

- nondeterministic
- slower
- more expensive
- harder to debug when failures are ambiguous

`ai/v3` should therefore validate agent runs in three layers:

1. unit tests for isolated components
2. deterministic scenario/harness tests for runtime contracts
3. selective real-LLM `testIT` for end-to-end integration confidence

---

## 3. Test pyramid for `v3`

### 3.1 Unit tests

Use unit tests for:

- descriptor parsing
- capability discovery helpers
- tool schema validation
- step-state reducers
- termination guard logic

### 3.2 Deterministic harness tests

Use the deterministic harness for:

- agent run scenarios expressed as typed fixtures
- scripted planner decisions
- scripted tool results and failures
- exact event-trace assertions
- exact loop-transition assertions

This is the primary regression layer for agentic behavior.

### 3.3 Real-LLM `testIT`

Use real-LLM `testIT` selectively for:

- Hello World end-to-end flow
- one or two representative schema workflows
- provider-specific streaming integration
- structured-output conformance under live prompting

These tests should assert stable structural properties, not exact prose.

---

## 4. Harness design principles

### 4.1 Assert contracts, not prose

The harness should assert:

- event type order
- required fields on emitted events
- planner mode transitions
- tool invocation shape
- observation and termination reasons
- final run outcome

It should avoid asserting exact final natural-language text unless the source is fully
scripted.

### 4.2 Replayability over realism

The harness should prefer replayable scripted behavior over approximate realism.

If a scenario can be made deterministic by scripting planner outputs and tool results, that is
better than asking a live model to regenerate equivalent behavior in CI.

### 4.3 Runtime-first validation

The harness should validate the runtime contracts directly, not only agent wrapper classes.

The most important artifact is the emitted run trace, because that is where planner,
executor, observer, and termination behavior become visible.

### 4.4 Same assertions for fake and real runs where possible

The same scenario shape should be reusable across:

- deterministic harness execution
- real-LLM `testIT`

The assertion strictness differs, but the scenario intent should not fork unnecessarily.

---

## 5. Proposed harness components

### 5.1 Scripted model adapter

Add a deterministic model adapter that can return predeclared planner outputs and message
deltas without calling a live provider.

The adapter should support:

- structured planner decisions
- direct-response path
- tool-calling path
- malformed response path for error handling tests
- optional streamed token chunks

### 5.2 Scripted tool executor

Add a tool executor test double that can produce:

- success results
- typed failures
- authorization denials
- timeouts
- duplicate-call responses

### 5.3 Trace recorder

Every deterministic scenario should collect a typed event trace containing at least:

- run id
- profile id
- iteration number
- event type
- event payload
- timestamp or logical sequence number

The harness should compare traces structurally rather than via ad hoc log scraping.

### 5.4 Scenario fixture model

Scenarios should be expressed as typed fixtures that declare:

- installed capabilities
- requested profile/context
- user input
- scripted planner outputs
- scripted tool outcomes
- expected termination outcome
- expected event sequence

### 5.5 Contract validators

The harness should include reusable validators for:

- capability-descriptor compatibility
- profile resolution invariants
- tool input/output schema conformance
- event-envelope conformance
- termination-reason conformance

---

## 6. Suggested scenario fixture shape

Illustrative shape:

```kotlin
data class AgentScenario(
    val name: String,
    val profileId: String,
    val capabilities: Set<String>,
    val input: String,
    val plannerScript: List<PlannerDecision>,
    val toolScript: Map<String, ScriptedToolOutcome>,
    val expectedOutcome: RunOutcome,
    val expectedEvents: List<ExpectedEvent>,
)
```

The exact API may differ, but the fixture should remain typed and readable.

The main point is that runtime behavior can be declared once and asserted repeatedly.

---

## 7. What deterministic scenarios should cover first

### 7.1 Hello World

Minimum deterministic scenarios:

- direct response with no tool call
- single trivial tool call then finish
- planner chooses unavailable tool
- tool returns typed failure
- planner emits invalid structured decision

### 7.2 Loop safety

Minimum deterministic scenarios:

- max-iteration limit reached
- duplicate tool call blocked by guard
- observer requests continue after budget exhausted
- clarification requested and run pauses
- cancellation received during streamed answer

### 7.3 Schema workflow

Minimum deterministic scenarios:

- inspect entity then finish
- inspect entity then inspect relations then finish
- sparse metadata triggers clarification
- tool authorization denied for protected scope

---

## 8. Relationship to real-LLM `testIT`

Real-LLM tests should verify that the live adapter still satisfies the same runtime contracts.

Recommended assertions for live tests:

- required capabilities are discovered
- selected profile is resolved
- required event families are emitted
- event order is valid
- at least one structured tool input/result is observed when expected
- termination outcome is correct

Do not assert:

- exact wording of the final answer
- exact count of message deltas
- exact rationale text from planner or observer

---

## 9. CI expectations

Recommended CI policy:

- deterministic harness tests run on every relevant change
- real-LLM `testIT` runs in a separate integration stage or opt-in gated stage
- live-test failures should not be the only signal for runtime regressions

This keeps regressions visible without making CI depend on provider variance.

---

## 10. Deliverables for implementation planning

Implementation planning should include explicit work for:

- scripted model adapter
- scripted tool executor
- trace recorder and trace assertions
- scenario fixture DSL or typed builder
- shared validators for events, schemas, and termination outcomes
- a small live `testIT` suite for provider verification

Without these pieces, `ai/v3` validation will drift toward manual CLI testing plus expensive
live integration checks.
