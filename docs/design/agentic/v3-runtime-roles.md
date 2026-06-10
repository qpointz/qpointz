# Agentic Runtime v3 - Capabilities, Planner, and Observer

**Status:** Working design aligned with current Hello World implementation  
**Date:** March 11, 2026  
**Scope:** `ai/v3` runtime roles and Hello World example

---

## 1. Purpose

This document explains the current intended roles of:

- agent
- agent profile
- capabilities
- planner
- observer
- runtime / executor

It uses the current Hello World implementation as the concrete example so the design stays
anchored to real code rather than abstract future architecture.

This document describes runtime roles generically, but the strategic target is a family of
context-bound agents aligned to `ui/mill-ui`, especially:

- `model`
- `knowledge`
- `analysis`

It should also support cross-cutting enrichment/authoring flows that can be composed into those
contexts rather than treated as a separate unrelated runtime.

These agents should operate over durable conversations rather than ephemeral in-memory turns only.
Persisted conversation/run records are part of the intended runtime shape.

---

## 2. High-Level Runtime Shape

The current `v3` runtime is intended to follow this shape:

```text
user input
-> resolve capabilities
-> resolve agent profile
-> planner chooses next step
-> runtime executes chosen action
-> observer decides continue / finish / fail
-> runtime streams final answer
```

For the Hello World milestone, this is intentionally small:

- one planning decision
- zero or one tool call
- one observation decision
- final streamed answer

This is sufficient to validate the runtime roles before moving to richer multi-step agents.

For the strategic end state, the runtime should be shared across multiple agent families whose
policies differ by context. The runtime shape should remain stable even when:

- planner policy differs between `model`, `knowledge`, and `analysis`
- observer policy differs between explain flows and enrichment/authoring flows
- a profile composes both contextual read capabilities and cross-cutting enrichment capabilities
- the same conversation must be resumed, inspected, or continued after persistence boundaries

---

## 3. Agent Role

An agent is the runnable unit that handles a user turn.

In `v3`, an agent is expected to:

- accept input in a runtime context
- resolve the capabilities it needs
- use a profile to define its active composition
- coordinate planning, execution, observation, and streaming

The current Hello World example agent is:

- [OpenAiHelloWorldAgent.kt](/home/vm/wip/qpointz/qpointz/ai/mill-ai-v3-langchain4j/src/main/kotlin/io/qpointz/mill/ai/langchain4j/OpenAiHelloWorldAgent.kt)

### 3.1 Agent responsibilities

- own a single end-to-end run
- bridge the model integration layer to the runtime contracts
- emit typed runtime events
- execute the flow defined by planner and observer decisions

### 3.2 Agent non-responsibilities

- defining reusable capability assets
- acting as the permanent definition of a chat type
- replacing the planner or observer abstractions

The current Hello World implementation still keeps multiple runtime roles in one class, but it
already behaves as a real agent rather than a plain utility wrapper.

---

## 4. Agent Profile Role

An agent profile defines which capabilities are active for a given agent shape.

A profile is not a running agent. It is a composition definition that says:

- which capability ids belong to the profile
- what runtime identity the agent should have

Strategically, profiles should represent bounded user-facing contexts and workflows, not just
different prompt presets.

Examples of likely future profile families:

- model exploration / model explanation
- knowledge concept explanation
- analysis query assistance
- context-specific enrichment/authoring

Current profile model:

```kotlin
data class AgentProfile(
    val id: String,
    val capabilityIds: Set<String>,
)
```

See:

- [AgentProfile.kt](/home/vm/wip/qpointz/qpointz/ai/mill-ai-v3-core/src/main/kotlin/io/qpointz/mill/ai/AgentProfile.kt)

Current Hello World profile:

```kotlin
object HelloWorldAgentProfile {
    val profile = AgentProfile(
        id = "hello-world",
        capabilityIds = HelloWorldCapabilitySet.requiredCapabilityIds,
    )
}
```

See:

- [HelloWorldAgentProfile.kt](/home/vm/wip/qpointz/qpointz/ai/mill-ai-v3-core/src/main/kotlin/io/qpointz/mill/ai/HelloWorldAgentProfile.kt)

### 4.1 Agent profile responsibilities

- define a stable agent identity
- define the expected capability composition
- act as the bridge between capability discovery and concrete agent behavior

Strategically, a profile should also be stable enough to anchor durable conversations over time.
Persisted conversations should be able to record which profile/family owned the run.

### 4.2 Agent profile non-responsibilities

- dynamically deciding next steps
- owning tool logic
- running the workflow

In the current Hello World milestone, the profile is fixed and validated at runtime before the
turn proceeds.

---

## 5. Capability Role

Capabilities are passive building blocks. They package reusable runtime assets, but they do
not own orchestration.

In the current implementation, a capability contributes:

- descriptor metadata
- prompt assets
- tools
- protocol definitions

Current capability contract:

```kotlin
interface Capability {
    val descriptor: CapabilityDescriptor
    val prompts: List<PromptAsset>
    val tools: List<ToolDefinition>
    val protocols: List<ProtocolDefinition>
}
```

See:

- [Capability.kt](/home/vm/wip/qpointz/qpointz/ai/mill-ai-v3-core/src/main/kotlin/io/qpointz/mill/ai/Capability.kt)

### 5.1 Capability responsibilities

- expose reusable runtime assets
- remain dynamically discoverable
- stay independent from a specific agent workflow
- support composition into different future agents

This composition requirement is especially important for enrichment. Enrichment should be modeled
as a reusable capability family that can be composed into `model`, `knowledge`, or `analysis`
profiles rather than embedded ad hoc into each context agent.

### 5.2 Capability non-responsibilities

- choosing the next step
- executing the workflow
- deciding when to stop
- owning run state

### 5.3 Hello World example

The Hello World milestone currently uses two capabilities:

- `conversation`
- `demo`

`conversation` contributes prompt and protocol assets for user-facing streaming:

- [ConversationCapability.kt](/home/vm/wip/qpointz/qpointz/ai/mill-ai-v3-capabilities/src/main/kotlin/io/qpointz/mill/ai/capabilities/ConversationCapability.kt)

`demo` contributes trivial tools used to validate the runtime shape:

- `say_hello`
- `echo_text`
- `noop`
- `list_demo_capabilities`

See:

- [DemoCapability.kt](/home/vm/wip/qpointz/qpointz/ai/mill-ai-v3-capabilities/src/main/kotlin/io/qpointz/mill/ai/capabilities/DemoCapability.kt)

---

## 6. Planner Role

The planner decides what should happen next.

For the current Hello World milestone, planning is intentionally minimal: the planner chooses
between:

- direct response
- call one tool

Strategically, the runtime should expect multiple planner families rather than one universal
planner implementation. The reusable part is the planner contract; the family-specific part is the
decision policy.

Likely planner families include:

- context-reading planners for `model`, `knowledge`, and `analysis`
- enrichment/authoring planners for proposing metadata or concept changes

This is preferable to one oversized planner that tries to encode every context's workflow in a
single decision taxonomy.

Current planner decision model:

```kotlin
data class PlannerDecision(
    val mode: Mode,
    val toolName: String? = null,
    val toolArguments: Map<String, String> = emptyMap(),
    val rationale: String? = null,
) {
    enum class Mode {
        DIRECT_RESPONSE,
        CALL_TOOL,
    }
}
```

See:

- [PlannerDecision.kt](/home/vm/wip/qpointz/qpointz/ai/mill-ai-v3-core/src/main/kotlin/io/qpointz/mill/ai/PlannerDecision.kt)

### 6.1 Planner responsibilities

- inspect the user request
- inspect available tools
- return a structured next-step decision
- keep the decision small and executable

### 6.2 Planner non-responsibilities

- running tools
- deciding from side effects whether execution succeeded
- streaming the final answer
- owning persisted run state

### 6.3 Hello World implementation example

The Hello World agent asks the model for a structured planning decision before any tool or
answer execution happens.

From:

- [OpenAiHelloWorldAgent.kt](/home/vm/wip/qpointz/qpointz/ai/mill-ai-v3-langchain4j/src/main/kotlin/io/qpointz/mill/ai/langchain4j/OpenAiHelloWorldAgent.kt)

```kotlin
val decision = plan(baseMessages, tools)
listener(AgentEvent.PlanCreated(mode = decision.mode.name, toolName = decision.toolName))
```

The planning call uses structured JSON output with a schema:

```kotlin
ChatRequest.builder()
    .messages(planningMessages)
    .responseFormat(
        ResponseFormat.builder()
            .type(ResponseFormatType.JSON)
            .jsonSchema(plannerDecisionSchema())
            .build()
    )
    .build()
```

This keeps the planning boundary explicit and machine-readable.

---

## 7. Observer Role

The observer interprets what happened after execution and decides whether the run should:

- continue
- finish
- fail

Current observer model:

```kotlin
data class Observation(
    val decision: ObservationDecision,
    val reason: String,
)

enum class ObservationDecision {
    CONTINUE,
    FINISH,
    FAIL,
}
```

See:

- [Observation.kt](/home/vm/wip/qpointz/qpointz/ai/mill-ai-v3-core/src/main/kotlin/io/qpointz/mill/ai/Observation.kt)

### 7.1 Observer responsibilities

- interpret the result of the executed step
- decide whether more work is required
- make stop/continue/fail explicit
- produce user-safe observation state for the runtime event stream

### 7.2 Observer non-responsibilities

- planning the next step in detail
- running tools
- defining capability contents

### 7.3 Hello World implementation example

The current Hello World observer is small but explicit.

Direct-response path:

```kotlin
val observation = Observation(
    decision = ObservationDecision.FINISH,
    reason = "Planner selected direct response path.",
)
listener(AgentEvent.ObservationMade(observation.decision.name, observation.reason))
```

Tool path:

```kotlin
val observation = Observation(
    decision = ObservationDecision.CONTINUE,
    reason = "Tool result is available and should be synthesized into the final answer.",
)
listener(AgentEvent.ObservationMade(observation.decision.name, observation.reason))
```

This is still intentionally simple, but it establishes the runtime boundary needed for more
advanced agents.

---

## 8. Runtime / Executor Role

The runtime coordinates the flow between capabilities, planner, execution, observer, and
streaming.

For Hello World, the executor responsibilities live inside
[OpenAiHelloWorldAgent.kt](/home/vm/wip/qpointz/qpointz/ai/mill-ai-v3-langchain4j/src/main/kotlin/io/qpointz/mill/ai/langchain4j/OpenAiHelloWorldAgent.kt).

Current responsibilities:

- resolve active capabilities from `CapabilityRegistry`
- resolve the Hello World profile
- build the system prompt from capability prompt assets
- call the planner
- execute a tool when the planner selects one
- emit typed runtime events
- stream the final answer

This is still a single class, but the role split is already visible.

### 8.1 Authorization boundary

The runtime must remain the control point for tool execution.

Capabilities may declare tools, but capabilities should not self-authorize their use.

Recommended runtime sequence:

1. planner requests a tool call
2. runtime resolves the tool definition
3. runtime authorizes the invocation against run context and policy
4. runtime executes the tool only if authorization succeeds
5. runtime emits either a normal result event or an authorization-denied failure event

This keeps discovery, planning, and execution separate from policy enforcement.

### 8.2 Capability admission boundary

Before profile resolution, the runtime should filter discovered capabilities through admission
rules.

At minimum, admission should be able to reject a capability because it is:

- untrusted
- incompatible with the contract version
- disabled for the current environment
- outside the current tenant or context policy

The active profile should be composed only from admitted capabilities.

---

## 9. Runtime Events

The runtime owns the streamed event model. The LLM does not emit these event types directly.

Relevant current event types:

- `run.started`
- `thinking.delta`
- `plan.created`
- `tool.call`
- `tool.result`
- `observation.made`
- `reasoning.delta`
- `message.delta`
- `answer.completed`

See:

- [AgentEvent.kt](/home/vm/wip/qpointz/qpointz/ai/mill-ai-v3-core/src/main/kotlin/io/qpointz/mill/ai/AgentEvent.kt)

The event model now makes planner and observer activity visible instead of hiding them inside
the model/tool loop.

User-visible progress events should remain distinct from private execution state.

If reasoning-like events are emitted, they should be treated as display-safe progress or
provider-approved reasoning streams rather than an obligation to expose hidden chain-of-thought.

---

## 10. Hello World End-to-End Example

The current Hello World flow is:

```text
user input
-> resolve capabilities
-> build prompt context
-> planner returns DIRECT_RESPONSE or CALL_TOOL
-> runtime emits plan.created
-> runtime executes direct answer or tool
-> observer emits observation.made
-> runtime streams final answer
```

Example tool path:

```text
"Use the say_hello tool to greet Alice."
-> planner: CALL_TOOL(name = say_hello, args = {name: Alice})
-> runtime executes say_hello
-> observer: CONTINUE
-> runtime synthesizes final answer from tool result
-> answer.completed
```

This is not yet a full multi-step planner/executor/observer runtime, but it is now a real
agentic workflow rather than a plain one-shot chat call.

---

## 11. Loop Safety and Termination Contract

The `v3` runtime should make loop safety explicit instead of relying on each agent to stop
"naturally."

### 11.1 Required guards

Every run should carry explicit guardrails for:

- maximum planner/executor iterations
- maximum tool calls
- maximum repeated call count per tool/signature
- overall run timeout
- optional per-step timeout
- cancellation signal handling

These values may start small in Hello World and expand later, but they should exist from the
first bounded workflow.

### 11.2 Terminal outcomes

The runtime should treat a run as ending in one of a small set of terminal outcomes:

- `COMPLETED`
- `FAILED`
- `CANCELLED`
- `PAUSED_FOR_CLARIFICATION`
- `ABORTED_BY_GUARD`

The observer may recommend continue or finish, but the runtime is responsible for converting
that into a terminal state when a guard or cancellation boundary is reached.

### 11.3 Termination reasons

Terminal outcomes should carry a machine-readable reason code, for example:

- `finished_normally`
- `planner_selected_direct_response`
- `tool_budget_exhausted`
- `iteration_budget_exhausted`
- `duplicate_tool_call_blocked`
- `clarification_required`
- `tool_authorization_denied`
- `tool_timeout`
- `run_cancelled`
- `unexpected_runtime_error`

This is important for:

- deterministic tests
- operator debugging
- future resume/replay behavior

### 11.4 Duplicate-step protection

Multi-step workflows should not assume the planner always makes forward progress.

The runtime should therefore detect at least:

- repeated identical tool calls with identical arguments
- repeated no-op continue cycles
- replans that produce the same blocked action after denial/failure

The first proof of concept does not need sophisticated semantic deduplication, but it should
have a basic repeated-action guard.

### 11.5 Planner vs observer vs runtime responsibilities

- planner chooses the next intended action
- observer interprets the result and recommends continue/finish/fail/clarify
- runtime enforces budgets, authorization, timeout, cancellation, and final termination

Loop safety belongs primarily to the runtime, not to the planner prompt.

---

## 12. Why This Matters

These role boundaries are important because they give `v3` a clean path to richer agents.

The next milestone, such as Schema Exploration, can now build on:

- capability composition
- structured planner decisions
- explicit observation decisions
- typed runtime events
- real streaming behavior

without having to redesign the Hello World runtime shape first.

---

## 13. Open Follow-Up

The current Hello World implementation is intentionally minimal. Likely next refinements are:

- extract planner and observer into separate runtime components
- move from one planning step to repeated next-step planning
- persist run state and observation history
- validate emitted events directly against protocol definitions
- extend the same runtime shape to the first schema-focused agent
- add capability admission and tool authorization checks ahead of execution
- add explicit guard-based termination reasons to the event model
