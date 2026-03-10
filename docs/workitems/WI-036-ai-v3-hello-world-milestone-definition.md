# WI-036 — AI v3 Hello World Milestone Definition

Status: `planned`  
Type: `📝 docs`  
Area: `ai`  
Backlog refs: `TBD`

## Problem Statement

The first `v3` milestone must be explicitly framed as architecture validation rather than domain delivery.

## Goal

Define the Hello World / platform-validation milestone for `ai/v3`.

This milestone should be treated as:

- low business value
- high architectural completeness

Its purpose is to validate the `v3` architecture end to end before domain-specific agent work
begins.

## In Scope

1. Record the purpose of the first milestone.
2. Clarify that it should validate capability architecture, streaming, and LangChain4j fit.
3. Clarify that it should use real LLM calls and preferably `testIT`.
4. Define the minimal end-to-end run shape the milestone must prove.
5. Define explicit success criteria so later implementation does not reinterpret the milestone.

## Out of Scope

- Implementing the Hello World agent.
- Delivering meaningful schema, metadata, SQL, or charting value.
- Domain-specific capability design beyond what is needed for architecture validation.

## Hello World Milestone Contract

The Hello World milestone exists to prove that all major `v3` components can work together
coherently, even when the domain value is intentionally trivial.

At the end of the milestone, the system should prove:

1. capabilities can be discovered dynamically
2. an agent profile can be assembled from discovered capabilities
3. a bounded agentic workflow can run end to end
4. the run uses a real LLM call
5. the run emits live streaming output
6. LangChain4j fits the intended architecture
7. the flow can be validated through `testIT`

## Required Characteristics

The Hello World milestone should:

- use real LLM-backed execution rather than only mocks
- prefer low-risk trivial/no-op tools
- stream visible progress early in the run
- include at least one structured tool input
- include at least one structured tool result
- include at least one protocol-defined streamed payload

## Minimal Example Interactions

Representative interactions may include:

- “say hello”
- “say hello to Alice”
- “what can you do”
- “show a demo run”

These are examples only; the architectural proof matters more than the exact surface phrasing.

## Expected End-to-End Shape

The milestone should prove a minimal loop of the form:

```text
user input
-> capability discovery / profile resolution
-> bounded planning step
-> trivial tool call or direct action
-> observer/runtime continuation or stop
-> streamed final answer
```

The milestone should not require:

- domain-specific knowledge
- schema inspection
- metadata enrichment
- SQL generation or execution
- visualization

## Success Criteria

The milestone is successful if:

- the capability architecture is exercised in minimal form
- prompts, tools, protocols, and descriptors are all present in the run
- the system produces a real streamed run through LangChain4j
- `testIT` can validate the flow at the event/protocol level

## Acceptance Criteria

- The first `v3` milestone is documented as low business value and high architectural completeness.
- The milestone definition is clear enough to guide implementation sequencing.
- The milestone definition explicitly states what architectural behaviors must be proven.
- The milestone definition clearly excludes domain-value requirements.

## Deliverables

- This work item definition (`docs/workitems/WI-036-ai-v3-hello-world-milestone-definition.md`).
