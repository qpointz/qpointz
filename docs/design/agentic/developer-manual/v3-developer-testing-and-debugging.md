# Agentic Runtime v3 - Testing And Debugging

**Status:** Active  
**Date:** March 19, 2026

---

## 1. Purpose

This chapter explains how to validate new `ai/v3` development safely:

- unit-test capabilities and handlers
- test routing and persistence rules
- inspect live behavior in the CLI
- debug missing artifacts, missing pointers, and missing observer output

The goal is to keep framework changes observable before REST or UX layers exist.

---

## 2. Testing Pyramid For ai/v3

Use a layered strategy.

### 2.1 Capability-level tests

Test:

- provider descriptor metadata
- dependency validation
- manifest binding
- handler output structure

These should be fast and deterministic.

### 2.2 Routing tests

Test:

- raw event -> routed event mapping
- one-to-many routing
- profile overrides
- artifact pointer keys

### 2.3 Projector/store tests

Test:

- transcript persistence
- artifact persistence
- artifact-to-turn attachment
- pointer updates
- telemetry accumulation
- observer notification

### 2.4 Runtime tests

Test:

- run loop behavior
- CAPTURE tool termination
- memory updates
- conversation creation

### 2.5 Manual CLI inspection

Use the CLI when you need to inspect:

- live event shapes
- model/tool flow
- protocol output
- observer output

---

## 3. What To Assert For New Work

When you add a new capability or artifact type, try to cover these assertions:

1. provider is discoverable
2. capability is resolvable from profile
3. tool arguments deserialize correctly
4. tool result is structured, not stringified
5. router emits expected destinations
6. artifact is persisted when expected
7. transcript turn exists when expected
8. artifact ids are attached to the turn
9. pointers update when expected
10. observer runs only after artifact persistence

---

## 4. Useful Existing Test Targets

Useful current test areas include:

- router tests
- in-memory store tests
- persistence projector tests
- profile routing override tests

When you add a new behavior, prefer extending the closest existing test file instead of inventing a parallel pattern.

---

## 5. CLI As A Debug Tool

The current CLI is a framework inspection surface.

It shows:

- run lifecycle
- tool calls and results
- streamed reasoning and answer text
- protocol outputs
- token usage
- no-op artifact-indexer prints when artifacts are actually persisted

This makes it the fastest way to validate end-to-end event flow before adding service wiring.

---

## 6. How To Read A CLI Trace

Example patterns:

### 6.1 Normal query run

Look for:

- `run.started`
- one or more `tool.call`
- one or more `tool.result`
- `llm`
- final `answer.completed`

### 6.2 Capture or artifact-bearing run

Look for:

- `tool.result` with structured artifact output or `protocol.final`
- persisted artifact side effects
- `[artifact-indexer] noop request ...`

If you do not see artifact-indexer output, first ask:

> Did this run actually persist an artifact?

That is the right first diagnostic question.

---

## 7. Debugging Missing Artifact Observer Output

If the no-op indexer does not print, check these in order:

1. Was an artifact actually persisted?
2. Did routing include `ARTIFACT` destination?
3. Did the rule set `persistAsArtifact = true`?
4. Did the projector run?
5. Was the observer registered in `AgentPersistenceContext`?

### Common real cause

A `tool.result` may be visible in the CLI but not yet be artifact-routed.

The fix is usually:

- promote canonical artifact-bearing tool results in the router

not:

- change CLI wiring

---

## 8. Debugging Missing Transcript Turns

Check:

1. Was `answer.completed` emitted?
2. Does the routing rule persist it as transcript?
3. Did the runtime use a stable `turnId` for the run?
4. Did `ConversationStore.ensureExists(...)` run?

Remember:

- user turns are currently appended directly by the runtime
- assistant turns are projected from routed `answer.completed`

---

## 9. Debugging Missing Artifact Attachment

If artifacts exist but the transcript turn does not reference them:

1. confirm the artifact `turnId`
2. confirm the assistant transcript turn used the same `turnId`
3. confirm `ConversationStore.attachArtifacts(...)` is implemented correctly
4. confirm your test covers artifact-before-turn and turn-before-artifact cases

This matters for:

- SQL plus chart config
- capture outputs with one owning assistant item

---

## 10. Debugging Capability Resolution

If a profile fails to resolve:

1. confirm the provider is in `META-INF/services`
2. confirm `descriptor.id` matches the profile capability id
3. confirm `supportedContexts` includes the runtime `contextType`
4. confirm required dependencies were supplied

Typical failure:

```text
Missing providers for profile `my-profile`: [my-capability]
```

This usually means provider registration or id mismatch.

---

## 11. Debugging Memory Issues

If the runtime seems to forget previous turns, check:

1. `conversationId` stability
2. `ChatMemoryStore.load(...)`
3. `LlmMemoryStrategy.project(...)`
4. post-run `saveToMemory(...)`

If transcript looks correct but the model still forgets, the problem is usually memory projection, not transcript persistence.

---

## 12. Recommended Manual Validation Flow

When you add a new agent behavior, validate in this order:

1. unit-test the capability and routing
2. run the CLI with the relevant agent
3. inspect tool calls and results
4. inspect artifact-indexer output if artifacts should exist
5. inspect in-memory stores in tests
6. only then move to service wiring

This sequence catches most framework mistakes early.

---

## 13. Debugging Checklist For New Artifact Types

1. Tool or protocol returns structured payload
2. Payload contains stable identifying fields such as `artifactType`
3. Router promotes the raw event into artifact lane
4. Projector persists artifact
5. Artifact attaches to transcript turn if applicable
6. Pointer keys update if required
7. Observer sees persisted artifact
8. CLI or tests show the expected behavior

---

## 14. What The CLI Does Not Yet Prove

The CLI is valuable, but it is not a complete substitute for tests.

It does not by itself prove:

- durable adapter correctness
- JPA mapping correctness
- API serialization behavior
- concurrent load behavior
- cross-process persistence

Use it for fast inspection, not as the only validation layer.

---

## 15. Related Documents

- `v3-interactive-cli.md`
- `v3-validation-harness.md`
- `v3-developer-runtime-events-persistence.md`
- `v3-developer-recipes.md`
