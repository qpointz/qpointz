# WI-372 — Configurable agent iteration limit side quest

Status: `done`
Type: `🔧 change`
Area: `ai`
Milestone: 0.8.0

## Problem Statement

`LangChain4jAgent` currently controls the native tool loop with a hard-coded
`MAX_ITERATIONS = 20`. When the loop is exhausted, the agent returns the fallback answer
`Reached maximum iteration limit without producing a final answer.` There is no
`mill.ai.chat` property to tune this limit per deployment or test profile.

## Goal

Make the agent loop iteration limit configurable through the chat runtime configuration while
preserving the existing default behavior.

## In Scope (high level)

1. Add `mill.ai.chat.max-iterations` to `AiV3ChatProperties`, defaulting to `20`.
2. Thread the configured value through `LangChain4jChatRuntime` into `LangChain4jAgent`.
3. Replace the hard-coded private `MAX_ITERATIONS` usage with a constructor parameter.
4. Validate positive values at configuration/runtime boundary.
5. Add unit coverage proving the property binds and the agent stops after the configured limit.

## Out of Scope

- Changing profile-specific prompt repair instructions such as SQL's "up to 3 attempts".
- Per-profile or per-capability iteration limits.
- Changing the fallback answer contract unless needed for testability.

## Acceptance Criteria

- `mill.ai.chat.max-iterations` configures the maximum native tool-loop iterations.
- Default remains `20` when the property is omitted.
- Invalid non-positive values fail fast with a clear configuration/runtime error.
- Tests cover property binding and low-limit loop exhaustion behavior.

## Deliverables

- This work item definition.
- Config property, runtime wiring, and tests on the story branch.
