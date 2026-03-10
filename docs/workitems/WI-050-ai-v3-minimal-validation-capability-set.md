# WI-050 — AI v3 Minimal Validation Capability Set

Status: `planned`  
Type: `✨ feature`  
Area: `ai`  
Backlog refs: `TBD`

## Problem Statement

The Hello World milestone should validate the full capability architecture, not just text generation.

## Goal

Define the minimal capability set for the Hello World / platform-validation agent.

This work item should make the Hello World milestone concrete enough that the first
implementation can proceed without reinterpreting the capability shape.

## In Scope

1. Define a minimal conversation capability.
2. Define a minimal demo capability.
3. Define the minimum prompt/tool/protocol/descriptor set required to exercise the architecture.
4. Define the Hello World agent profile that composes these capabilities.
5. Define the minimal workflow and streaming sequence that the capabilities support.

## Out of Scope

- Real domain capabilities.
- Schema or concept capability design.
- SQL/data/chart behaviors.

## Proposed Minimal Capability Set

The recommended initial Hello World capability set is:

1. `conversation`
2. `demo`

This keeps the milestone small while still exercising the major architectural surfaces.

### Capability: `conversation`

Purpose:

- provide minimal user-facing conversation behavior
- provide progress narration and final-answer tone guidance
- participate in streamed text output

Minimum contents:

- capability descriptor
- one system prompt asset
- one prompt asset for progress/final-answer behavior
- one minimal protocol definition for text/progress events

Expected characteristics:

- no meaningful domain tools required
- focused on narration, structure, and safe user-visible output

### Capability: `demo`

Purpose:

- provide trivial/non-domain actions that let the runtime validate discovery, tool execution,
  structured I/O, and streaming

Minimum contents:

- capability descriptor
- one prompt asset that instructs when to use demo tools
- one minimal protocol or artifact definition for demo output
- two to four trivial tools

Recommended demo tools:

- `say_hello`
- `echo_text`
- `noop`
- `list_demo_capabilities`

These tools should have little or no business value. Their purpose is to validate runtime
behavior, not domain usefulness.

## Proposed Hello World Agent Profile

The initial Hello World agent profile should include:

- `conversation`
- `demo`

Initial context support should be limited to:

- `general`

This keeps the first milestone focused and avoids premature context-specific behavior.

## Proposed Minimal Workflow

The Hello World workflow should be bounded and simple, but still agentic.

Representative flow:

1. resolve/discover capabilities
2. assemble Hello World profile
3. accept user input
4. produce a minimal plan or next-step decision
5. execute trivial tool or direct action
6. observe result
7. stop and stream final answer

## Proposed Streaming Sequence

The Hello World milestone should validate a minimal streaming sequence such as:

1. `run.started`
2. `thinking.delta`
3. `tool.call` (when a tool is used)
4. `tool.result`
5. `message.delta`
6. `answer.completed`

This sequence should be small but real.

## `testIT` Validation Expectations

The Hello World capability set should be designed so it can be validated with:

- a real LLM call through LangChain4j
- real capability discovery/profile composition
- event/protocol assertions instead of brittle exact-text assertions

Suggested assertions:

- the expected capability set is present
- the profile contains both capabilities
- the event sequence is produced in valid order
- at least one structured tool input/result is observed
- final completion event is emitted

## Acceptance Criteria

- The Hello World milestone includes descriptors, prompts, tools, protocols, and discovery metadata in minimal form.
- The capability set is intentionally low business value and high architectural completeness.
- The capability set is reduced to a small concrete profile that can actually be implemented next.
- The work item defines the expected workflow and streaming lifecycle for the first implementation.

## Deliverables

- This work item definition (`docs/workitems/WI-050-ai-v3-minimal-validation-capability-set.md`).
