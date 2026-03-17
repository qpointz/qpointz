# Agentic Runtime v3 - LangChain4j Simplification Notes

**Status:** Working design note based on current `LangChain4jAgent` implementation  
**Date:** March 17, 2026  
**Scope:** `ai/v3` simplification opportunities in `LangChain4jAgent` and adjacent LangChain4j adapter code

---

## 1. Purpose

This note maps the main simplification opportunities in the current generic
`LangChain4jAgent` implementation.

The goal is not to remove the `ai/v3` runtime model entirely. The goal is to identify which
parts of the current design carry real `Mill` value and which parts duplicate behavior that
LangChain4j can already handle directly.

This document focuses on:

- generic tool-calling flow
- planner and argument-generation flow
- answer synthesis flow
- adapter boundaries
- recommended simplification order

---

## 2. Current Shape

The current generic agent in
`ai/mill-ai-v3-langchain4j/src/main/kotlin/io/qpointz/mill/ai/langchain4j/LangChain4jAgent.kt`
owns all of the following responsibilities in one class:

- capability resolution
- system prompt assembly
- planner LLM call
- planner output parsing
- tool-argument LLM call
- tool dispatch
- observer decision policy
- answer synthesis
- LangChain4j request execution
- OpenAI model bootstrap

This produces a valid proof of concept, but it also means the generic agent is acting as:

- runtime coordinator
- framework adapter
- planner policy
- tool loop implementation
- answer renderer
- factory/bootstrap layer

That is the main source of complexity.

---

## 3. What Appears Worth Keeping

Some current `v3` abstractions still appear justified and should remain even if the
LangChain4j integration is simplified.

### 3.1 Capability model

The current capability contract is still useful:

- `Capability`
- `ToolDefinition`
- `ToolKind`
- `ProtocolDefinition`
- `CapabilityRegistry`

These types describe Mill-owned runtime semantics rather than LangChain4j-specific mechanics.

### 3.2 Protocol model

The protocol layer still appears valuable:

- `TEXT`
- `STRUCTURED_FINAL`
- `STRUCTURED_STREAM`

This is more than ordinary chat completion. It gives `Mill` an explicit output contract that
can remain stable even if the underlying model framework changes.

### 3.3 Capture-tool semantics

`ToolKind.CAPTURE` is meaningful domain behavior. It expresses that some tools do not merely
ground the next planning step; they produce an artifact that should terminate the run and be
rendered through a protocol-aware synthesis path.

That concept is worth preserving.

---

## 4. Highest-Value Simplifications

### 4.1 Remove the separate planner LLM call

Current state:

- `planWithModel()` issues a dedicated LLM call to decide:
  - direct response
  - one tool name
  - optional task metadata

This is extra machinery on top of native tool calling.

Simplification:

- provide `toolSpecifications` to the model in the main conversation request
- let the model either:
  - return tool execution requests
  - return a final answer

This is already how the more natural schema-agent path works.

Expected gain:

- one less model round trip per user turn
- less JSON schema plumbing
- less custom planner prompt maintenance
- fewer mismatches between planner intent and actual tool behavior

What can likely be deleted or collapsed:

- `planWithModel()`
- `plannerSelectionSchema()`
- planner-specific system prompt text that only exists to emulate native tool selection

### 4.2 Remove the separate tool-argument LLM call

Current state:

- `planToolArguments()` performs a second structured-output call after the planner has already
  selected a tool.

This is expensive and brittle because tool choice and tool arguments are produced in different
calls.

Simplification:

- let the model emit tool arguments as part of the native tool request

Expected gain:

- one less model round trip
- less JSON parsing code
- less drift between selected tool and selected arguments

What can likely be deleted:

- `planToolArguments()`
- the second-pass argument prompt
- the argument-generation schema wrapper in the generic path

### 4.3 Return tool results as tool-result messages, not synthetic user messages

Current state:

- after tool execution, the generic agent injects a synthetic `UserMessage` describing the tool
  name and JSON result

This is a custom prompt-level emulation of tool-result flow.

Simplification:

- append a framework-native `ToolExecutionResultMessage` after each executed tool call
- continue the conversation loop with the full message history

This matches the existing schema-agent pattern more closely and is more aligned with how the
framework expects tool results to be represented.

Expected gain:

- better model grounding
- clearer auditability of the tool loop
- less prompt hacking
- easier extension to multiple tool calls per turn

### 4.4 Reduce the observer to a smaller policy

Current state:

- the generic observer interprets planner decisions and tool results even though the planner is
  itself a custom wrapper around model behavior

Simplification:

- if the generic agent becomes a native tool loop, the observer policy can likely shrink to:
  - if a capture tool ran, answer via protocol
  - if regular tools ran, continue
  - if the model returned no tool calls, answer
  - if the run exceeds budget or errors, fail

Expected gain:

- fewer abstract runtime states
- easier reasoning about loop termination
- simpler event traces

### 4.5 Narrow or remove the generic `AgentExecutor` role in the LangChain4j adapter

Current state:

- the generic agent builds a full `AgentExecutor` with planner, observer, tool executor,
  synthesis policy, and protocol executor

Simplification options:

- keep `AgentExecutor`, but narrow it to run-state tracking and event emission
- or bypass it in the LangChain4j adapter and implement a direct tool loop similar to the schema
  agent

Expected gain:

- fewer layers between model behavior and runtime behavior
- easier debugging
- less adapter ceremony

Preferred direction:

- keep `AgentExecutor` only if it continues to provide real cross-adapter value
- otherwise, do not preserve it just to protect an abstraction boundary that the adapter no
  longer needs

---

## 5. Secondary Simplifications

### 5.1 Unify `complete()` and `stream()` plumbing

Current state:

- `LangChain4jAgent` contains separate `complete()` and `stream()` helpers
- `LangChain4jProtocolExecutor` contains similar callback-to-future plumbing

Simplification:

- introduce a shared internal LangChain4j request helper used by both the generic agent and the
  protocol executor

Expected gain:

- less duplicate callback code
- one place for error handling
- one place for streaming event adaptation

### 5.2 Move capability resolution out of the adapter class

Current state:

- `LangChain4jAgent` resolves the profile, registry, capabilities, and tool set itself

Simplification:

- inject a resolved runtime bundle or session context:
  - profile
  - capabilities
  - tool specifications
  - protocol set

Expected gain:

- smaller adapter class
- easier tests
- less registry logic mixed into model-execution code

### 5.3 Move OpenAI/bootstrap concerns out of `LangChain4jAgent`

Current state:

- `fromEnv()` and `fromConfig()` are embedded in the runtime class

Simplification:

- move model construction to a dedicated factory or provider

Expected gain:

- cleaner separation between runtime logic and transport/bootstrap
- simpler constructor
- easier injection of fake or alternate models

### 5.4 Unify message assembly

Current state:

- the generic agent assembles system prompts directly
- protocol execution uses a separate path with its own request construction

Simplification:

- create one message-building strategy for:
  - base system prompt
  - user message history
  - tool-result messages
  - optional protocol output constraints

Expected gain:

- clearer control over what the model actually sees
- fewer prompt-construction paths
- easier testing of prompt shape

---

## 6. Suggested Target Shape

The simplified generic LangChain4j agent can likely follow this flow:

```text
resolve profile and capabilities
-> build system prompt and tool specifications
-> send conversation to model
-> if model returns tool calls:
   -> execute tool calls
   -> append ToolExecutionResultMessage entries
   -> if a capture tool ran, synthesize via protocol
   -> else loop
-> if model returns final text:
   -> stream final answer directly
   -> or route through a selected protocol when required
```

This retains the Mill-owned concepts that still matter:

- capabilities
- tool definitions
- capture semantics
- protocols
- event model

It removes the parts that appear to be reimplementing framework behavior:

- planner-selection prompt
- planner-selection schema
- second-pass argument planning
- synthetic user-message tool result injection

---

## 7. Recommended Refactor Order

Recommended order of simplification:

1. Remove separate tool-argument planning.
2. Replace planner-selection prompt with native tool calling.
3. Return tool results as `ToolExecutionResultMessage`.
4. Shrink the observer to a small capture/continue/answer policy.
5. Consolidate LangChain4j request helpers used by both the agent and protocol executor.
6. Move model/bootstrap construction out of `LangChain4jAgent`.
7. Re-evaluate whether `AgentExecutor` still carries enough value to justify its indirection.

This order reduces risk because it starts with the most mechanical, highest-cost redundancies
before touching the broader runtime boundary.

---

## 8. Future Improvement: Batched Schema Query Tools

The current generic planner prompt asks the model to call "exactly one" tool. That constraint
is understandable as an early simplification, but it is likely too restrictive for schema
exploration workflows.

For schema-facing query tools, multiple tool calls in one planning step are often both safe and
useful because they are typically:

- read-only
- independent
- cheap relative to another model round trip
- needed together before the model can synthesize a good answer

Examples:

- `list_schemas` plus `list_tables`
- `list_columns` for several already-identified tables
- `list_relations` across a small set of candidate tables

For this tool family, the future target should be:

- allow zero or more independent `QUERY` tool calls in one model response
- execute them all in the same step
- append all tool-result messages back into the conversation
- continue the loop only if the model still needs more grounding

The current single-tool constraint is less defensible for schema exploration than for domains
where tools are stateful, expensive, or sequentially dependent.

The main guardrail is that `CAPTURE` tools should remain exclusive and terminal:

- do not batch a capture tool with other tools
- when a capture tool runs successfully, terminate the loop and synthesize through the selected
  protocol

This suggests a better long-term planner policy:

- zero or more independent query tools
- or one exclusive capture tool
- but not a mixed batch

---

## 9. Design Guardrail

The simplification goal should not be "let LangChain4j own everything."

The better guardrail is:

- keep Mill-owned abstractions where they encode domain/runtime semantics
- remove custom layers where they only emulate built-in framework behavior

That means `Capability`, `ToolDefinition`, `ProtocolDefinition`, and `ToolKind.CAPTURE`
still appear worth keeping.

What appears least justified in the current generic path is the custom two-pass
planner-plus-argument LLM flow built on top of a framework that already knows how to drive
tool selection and tool arguments in one conversation.
