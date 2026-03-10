# Agentic Runtime v3 - Foundation Decisions

**Status:** Planning  
**Date:** March 10, 2026  
**Scope:** `ai/v3` proof of concept and architectural direction

---

## 1. Goal

Mill should evolve from the current NL2SQL-oriented implementation into a **generic
agentic application platform** that can support a **family of agents** across multiple
contexts:

- general chat
- model-context chat
- knowledge-context chat
- analysis-context chat
- future specialized agents

The first implementation will be developed as a new `ai/v3` stack, side by side with
existing `v1` and `v2` modules.

---

## 2. Primary Architectural Decisions

### 2.1 `ai/v3` is a new side-by-side implementation

The initial proof of concept should be built in new `ai/v3` modules and should not
refactor or directly extend `v1`/`v2`.

Reasons:

- preserves freedom to design correct abstractions
- avoids contamination from the current intent-centric runtime
- allows validation of the new architecture before wider adoption

### 2.2 Kotlin-only, no Spring contamination in core runtime

The `v3` runtime should be implemented in Kotlin and remain framework-free.

Spring, if needed later, should exist only in adapter/integration modules and must not
define the core runtime architecture.

### 2.3 LangChain4j is an integration layer, not the architecture

LangChain4j should be used for:

- LLM access
- tool calling
- structured outputs
- streaming model output

LangChain4j should not define:

- runtime state model
- planner/execution-loop architecture
- protocol/event schema
- capability discovery model

### 2.4 `v3` must implement a real agentic workflow

The first `v3` runtime must not be a thin wrapper around the old intent pipeline.

From the outset, `v3` should support:

- multi-step planning
- dynamic tool choice
- tool-result-aware continuation
- clarification pause/resume
- streamed user-visible progress
- final synthesis after iterative evidence gathering

This means the runtime should follow a loop of the form:

```text
input
-> plan
-> execute step
-> observe result
-> replan or continue
-> produce artifacts
-> finalize answer
```

### 2.5 Streaming is a first-class runtime concern

`v3` should be designed as a **stream-first runtime**, not as a request/response system
with optional streaming.

The platform should stream:

- user-visible thinking/progress narration
- plan and step transitions
- tool calls and tool results
- artifacts such as tables, charts, or patches
- final answer fragments where useful

The runtime must distinguish between:

- internal reasoning state
- user-visible streamed narration

The system should never rely on streaming hidden chain-of-thought.

---

## 3. Capability Model

### 3.1 Reuse the `v2` capability idea

The `v2` idea of **capabilities** should be retained and generalized.

Capabilities are the main reusable building blocks of the `v3` agent platform.

Each capability should package:

- prompts
- tools
- output definitions / protocols
- policies / constraints
- reasoning descriptors
- optional memory/state hooks

### 3.2 Capabilities are passive building blocks

A capability is **not**:

- an agent
- a workflow
- a chat session
- a planner

A capability is a modular building block that can be composed into many agents.

### 3.3 Capabilities must be dynamically discoverable

Capability discovery is a first-class platform property.

The platform should be able to discover capabilities dynamically at runtime rather than
hardcoding them.

Discovery may later use:

- classpath/provider registration
- resource descriptors
- plugin-style extension mechanisms

The important design decision is that the runtime should operate on the discovered set of
capabilities, not on a compile-time fixed list.

### 3.3A Discovery requires trust and authorization boundaries

Dynamic discovery must not imply blind execution of whatever capability happens to be present.

`v3` should distinguish three separate concerns:

- **discovery**
  - finding candidate capabilities and indexing their descriptors
- **admission**
  - deciding whether a discovered capability is allowed to participate in the runtime
- **execution authorization**
  - deciding whether a specific tool call is allowed in the current run context

This separation is required because a capability may be:

- installed but not trusted
- trusted for development but not for production
- allowed for one tenant/context but not another
- allowed for read operations but not for side-effecting operations

Minimum day-one expectations:

- each capability descriptor should include stable identity and version metadata
- capability admission should be explicit rather than implicit
- tool descriptors should declare side-effect and scope characteristics
- runtime execution should pass through an authorization gate before tool invocation

The first proof of concept does not need a full production security model, but it must avoid a
shape where discovery and execution are effectively the same operation.

### 3.4 Capabilities should be designed for future MCP exposure

The capability model should be designed so that capabilities can later be exposed through an
MCP server.

This enables:

- reuse of Mill capabilities by external agents
- integration with non-Mill runtimes
- use from ecosystems such as LangChain Python
- a cleaner separation between Mill capability implementation and agent runtime embedding

This is a future-facing architectural requirement and should influence capability design from
the beginning.

Implications:

- capability descriptors should remain self-describing
- tool contracts should be explicit and machine-readable
- protocols should be stable and externally consumable
- capability boundaries should not depend on in-process-only assumptions

MCP exposure is not required for the first proof of concept, but `v3` should avoid design
choices that would block it later.

### 3.5 Day-one MCP-friendliness requirement

Even though MCP support is not part of the first proof-of-concept delivery, `v3` should be
designed to be **MCP-friendly from day one**.

This means that capability definitions should avoid hidden assumptions that only work inside a
single in-process Mill runtime.

The following parts of the capability model should be designed with future MCP exposure in
mind from the start:

- **tools**
  - explicit input and output schemas
  - stable names and descriptions
  - no dependence on local runtime-only object identity

- **prompts**
  - capability-level prompts should be structured and externally consumable
  - prompt assets should be addressable and versionable

- **protocols**
  - output/event schemas should be explicit, machine-readable, and stable enough for external
    clients

- **capability descriptors**
  - descriptors should communicate what a capability is, what contexts it supports, what tools
    it exposes, and what outputs it can produce

- **resources/artifacts**
  - capabilities may need to expose additional resources beyond tools and prompts, such as:
    - protocol schemas
    - prompt templates
    - reference documents
    - example payloads
    - artifact schemas
    - capability metadata/help text
  - these should be treated as first-class externalizable resources where appropriate

- **policy and auth boundaries**
  - authorization, tenancy, and side-effect boundaries should remain explicit so that future
    MCP exposure does not accidentally bypass Mill control points

### 3.5A Capability trust and authorization model

To keep MCP-friendliness and dynamic discovery safe, `v3` should define a basic trust model
for capabilities and tools.

Recommended conceptual layers:

- **capability trust**
  - is this capability package/provider trusted to be loaded at all
- **capability admission**
  - is this capability enabled for the current environment or tenant
- **tool authorization**
  - may this run invoke this tool with this input in this scope

Recommended minimum descriptor fields:

- capability id
- provider id
- capability version
- contract/schema version
- supported contexts
- trust class or installation source
- declared side-effect level
- optional required permissions/tags

Recommended minimum runtime checks:

1. discover candidate capabilities
2. reject incompatible or untrusted capabilities
3. resolve an agent profile only from admitted capabilities
4. authorize each tool invocation against run context and policy
5. emit explicit denial/failure events when authorization blocks execution

This model should apply both:

- in-process inside Mill
- later through MCP-exposed tools/resources

Otherwise external exposure would create a path around Mill's intended control points.

In practice, this means `v3` should model capabilities as self-describing packages whose
contracts can later be surfaced both:

- in-process inside Mill
- externally through MCP

### 3.6 MCP exposure model

For future MCP exposure, capabilities should not be treated as “tools only”.

The intended mapping is:

- **tools** -> MCP tools
- **protocols** -> MCP resources
- **prompt assets** -> MCP prompts or MCP resources
- **capability descriptors** -> MCP resources
- **artifact schemas / reference documents / examples** -> MCP resources

This mapping is preferred because:

- tools represent executable behavior
- protocols represent readable schemas/contracts
- prompts represent reusable templates or prompt assets
- descriptors and examples represent discoverable reference material

### 3.7 MCP resource model requirements

An MCP resource should not be treated as an anonymous blob.

If Mill exposes protocols, prompt assets, descriptors, or other capability assets through
MCP resources, each resource should carry enough metadata for clients to understand:

- what the resource is
- which capability it belongs to
- what kind of thing it describes
- how it should be interpreted

For example, a protocol resource should be identifiable as a protocol and should indicate:

- capability id
- protocol name
- version
- description
- content/media type
- optional tags such as `streaming`, `artifact`, `chart`, or `table`

This means the `v3` capability model should eventually support explicit descriptors for assets
such as:

- capability descriptor
- tool descriptor
- protocol descriptor
- prompt descriptor
- artifact/resource descriptor

These descriptors are useful both:

- internally in Mill
- externally when surfaced through MCP

---

## 4. Agent Family Model

### 4.1 Mill needs a platform for many agents, not one NL2SQL agent

The target architecture is a shared runtime that can host multiple context-bound agents.

Examples:

- general analytics agent
- model-context explain agent
- concept/knowledge agent
- analysis-context agent
- future reconciliation/comparison agents

### 4.2 Agents are assembled from capabilities

An agent should be defined as a **profile** composed from capabilities for a given context.

This means:

- one runtime
- many agent profiles
- shared capability registry
- context-aware composition

This is preferred over building many unrelated agent implementations.

### 4.3 Capability reuse should work both inside and outside Mill

The long-term capability model should support two usage modes:

1. **In-process usage inside Mill**
2. **External consumption through MCP**

This means capability design should favor clear contracts, explicit tool inputs/outputs, and
portable protocol definitions.

---

## 5. Tool Surface Decisions

### 5.1 Do not expose internal metadata architecture directly to the LLM

The LLM should not need to know that Mill internally separates:

- physical schema
- schema-bound metadata
- concept metadata
- future data quality or semantic layers

That separation is valuable internally, but it should not drive the public tool surface.

### 5.2 Expose unified `schema` tools

Schema tools should provide a merged view over:

- physical schema
- schema-bound metadata
- relations
- descriptions
- value mappings
- similar schema-level information

The LLM should think in terms of:

- inspect schema
- inspect table
- inspect column
- inspect relations

not in terms of separate metadata subsystems.

### 5.3 Expose separate `concept` tools

Concept tools should serve non-physical or cross-physical knowledge such as:

- concepts
- business definitions
- rules
- data quality notes
- domain-specific semantics

This creates a better agent-facing abstraction than exposing “metadata” as a raw category.

### 5.4 Recommended initial outward capability categories

For the proof of concept, the platform should think in terms of:

- `conversation`
- `schema`
- `concept`

Later extensions may add:

- `data`
- `visualization`
- `comparison`

---

## 6. POC Scope Decision

### 6.1 Relax strict `v1` intent parity for the first proof of concept

Strict feature parity with all existing `v1` intents is not the best first milestone if it
forces too much tooling complexity early.

The first proof of concept should instead prove:

- dynamic capability discovery
- capability-based agent assembly
- real agentic workflow
- streaming output
- reusable foundation for future agents

### 6.2 Start with simpler agents first

The recommended first proof-of-concept scope is to build simpler, metadata-grounded agents
before SQL-heavy agents.

Recommended progression:

1. **Hello World / Platform Validation Agent**
2. **Schema Exploration Agent**
3. **Concept / Knowledge Explain Agent**
4. **Schema or Knowledge Enrichment Agent**

These agents are easier to build and still prove the platform.

### 6.3 Why simpler agents are preferred for POC

Starting with a Hello World validation agent followed by schema/concept/enrichment agents
proves the architecture while avoiding the
 hardest early problems:

- SQL correctness
- query validation
- result execution
- chart generation
- comparison and reconciliation logic

This yields a better platform proof with lower complexity.

### 6.4 First milestone is platform validation, not domain value

The first implementation milestone should explicitly be a **platform-validation milestone**
rather than a domain-agent milestone.

Its purpose is to validate:

- the capability architecture
- dynamic discovery and composition
- stream-first runtime behavior
- bounded agentic workflow shape
- LangChain4j fit in the target architecture

This first milestone should use **real LLM calls**, not only mocks or local fakes.

Preferably, it should be validated through `testIT` integration tests so that:

- streaming behavior is exercised end to end
- LangChain4j integration is proven against a real configured model
- capability composition is tested in realistic runtime conditions

The first milestone is not expected to deliver real schema or metadata product value.
It is expected to demonstrate that all major `v3` components work together coherently.

---

## 7. Recommended First Agents

### 7.1 Hello World / Platform Validation Agent

Purpose:

- validate the `v3` capability architecture with minimal domain complexity
- prove dynamic capability discovery and profile composition
- prove live streaming behavior
- validate LangChain4j integration in the planned runtime shape

This agent should be intentionally simple and low-risk.

It should prove architecture rather than business usefulness.

The intended character of this milestone is:

- low business value
- high architectural completeness

The goal is to observe the full `v3` architecture working together coherently even before any
meaningful domain value is delivered.

Likely ingredients:

- a minimal `conversation` capability
- a minimal demo capability with one or more trivial/no-op tools
- a tiny protocol/output definition for streamed events

Ideally, this milestone should exercise **all major capability aspects** in a minimal form,
including:

- capability descriptor
- prompt assets
- tool definitions
- protocol/output definitions
- discovery/registration
- streaming integration
- LangChain4j usage

Typical interactions may include:

- “say hello”
- “show a demo run”
- “what can you do”

The important outcome is not the behavior itself; the important outcome is validating that:

- capabilities are discovered
- an agent profile is assembled
- the runtime plans and executes at least one bounded step
- streaming events are emitted live
- LangChain4j fits the design
- the setup works in `testIT` using real LLM calls

The Hello World agent should therefore use tools that have little or no business value but are
useful for architecture validation, for example:

- `echo_text`
- `say_hello`
- `noop`
- `emit_demo_fact`
- `list_demo_capabilities`

### 7.2 Schema Exploration Agent

Purpose:

- explain tables, columns, relationships, and schema-level meaning
- answer natural-language questions about data structure
- use schema tools only

This agent is a strong first fit for model-context chat.

### 7.3 Concept / Knowledge Explain Agent

Purpose:

- explain business concepts and non-physical metadata
- answer questions about concepts, rules, and semantic meaning
- use concept tools

This agent is a strong first fit for knowledge-context chat.

### 7.4 Enrichment Agent

Purpose:

- propose and eventually apply schema or concept enrichments
- support write-oriented metadata workflows
- operate against explicit target scopes

This aligns well with the collaborative metadata direction and activity/custom scopes
described in `docs/design/metadata/`.

---

## 8. Streaming UX Decisions

The runtime should emit structured streaming events from the beginning.

Recommended event families:

- run started / completed / failed
- thinking or progress delta
- plan created / updated
- tool call
- tool result
- artifact ready
- clarification requested
- final answer delta / completed

Streaming should prioritize low-latency user-visible feedback.

The system should emit useful progress quickly rather than waiting for a full plan or final
result.

The event model should use **user-visible progress/evidence**, not hidden chain-of-thought.

If the runtime emits events with names such as `thinking.delta` or `reasoning.delta`, those
events should be interpreted as safe progress narration or provider-supplied reasoning signals
that are explicitly allowed for display, not as a requirement to surface private internal
reasoning.

---

## 9. Design Principle for Initial Success

The first `v3` proof of concept should prove:

1. capabilities can be discovered dynamically
2. agents can be assembled from discovered capabilities
3. the runtime supports a bounded but real agentic workflow
4. streaming provides visible low-latency progress
5. the core runtime remains framework-free
6. the result is a credible base for future SQL/data/visualization agents

If these conditions are met, `v3` is a successful foundation even before full `v1`
behavioral parity is implemented.

---

## 10. Deferred Scope

The following areas are intentionally deferred beyond the first proof of concept:

- full `v1` intent parity
- SQL execution-heavy agents
- chart generation
- reconciliation/comparison workflows
- broad persistence and production integration concerns
- Spring-based service integration in the core runtime

These should be added only after the core `v3` platform shape is validated.

---

## 11. Proposed Work Items

The following proposed work items break the effort into:

- foundation groundwork for `ai/v3`
- implementation of the first agent: **Hello World / Platform Validation Agent**
- implementation of the first schema-focused workflow agent: **Schema Exploration Agent**
- implementation of the first reusable schema capability boundary used by that agent

These are planning-level work items, not a final delivery plan.

### 11.1 Foundation Groundwork

#### WI-01: Create `ai/v3` module skeleton

Create side-by-side `v3` modules and package layout for:

- runtime/core
- capabilities
- LangChain4j integration
- test/scenario support

Expected outcome:

- `v3` code can evolve independently of `v1` and `v2`
- package/module boundaries are fixed early

#### WI-01A: Define Hello World / Platform Validation milestone

Define the first `v3` implementation milestone as a platform-validation milestone rather than
as a domain-agent milestone.

This milestone should explicitly validate:

- capability architecture
- streaming behavior
- bounded agentic runtime flow
- LangChain4j fit
- real LLM-backed integration testing

Expected outcome:

- the team agrees on the purpose and scope of the first implementation step
- early implementation focuses on architectural risk reduction

#### WI-02: Define core domain vocabulary

Define the core `v3` abstractions and terminology:

- capability
- capability descriptor
- capability provider
- agent profile
- agent context
- run state
- protocol
- artifact
- planner step

Expected outcome:

- stable shared language for the rest of the design and implementation

#### WI-03: Define capability model and descriptor format

Design the `v3` capability model, including:

- prompts
- tools
- protocols
- reasoning descriptors
- policies
- supported contexts/tags
- future MCP compatibility requirements

Expected outcome:

- a concrete capability contract that can be implemented by multiple capability providers
- the capability contract is suitable for future in-process and MCP-based exposure

#### WI-04: Implement capability discovery design

Design and implement the initial dynamic discovery mechanism for capabilities.

The first mechanism should be framework-free and suitable for Kotlin/JVM usage.

Expected outcome:

- runtime can discover installed capabilities without hardcoded registration in agent logic

#### WI-04A: Define MCP-aligned capability exposure plan

Define how discovered capabilities could later be exposed through an MCP server without
redesigning the capability model.

This work item is architectural/design-focused and does not require implementing an MCP server
in the first proof of concept.

Expected outcome:

- `v3` capability design remains compatible with future external agent consumption
- a clear path exists for exposing Mill capabilities to external runtimes such as LangChain
  Python

#### WI-04B: Identify first-class externally exposable capability resources

Define which capability assets should be modeled as first-class externalizable resources from
day one.

This should include at least:

- tool schemas
- prompt assets
- protocol schemas
- capability descriptors
- artifact/resource descriptors

Expected outcome:

- `v3` does not limit MCP exposure to tools only
- the capability package model is suitable for richer external reuse

#### WI-04C: Define descriptor model for externally exposed capability assets

Define the metadata model used to describe externally exposed capability assets, especially
resources.

This should cover descriptor requirements for:

- protocols
- prompts
- capability manifests
- artifact schemas
- reference/example documents

Expected outcome:

- future MCP resources are self-describing
- external clients can determine what a resource represents without relying on filename or
  implicit convention

#### WI-05: Define protocol and streaming event model

Define:

- shared runtime event envelope
- event families for planning, tool execution, artifacts, clarification, and answer streaming
- capability-scoped payload/protocol model

Expected outcome:

- all later agents use the same stream-first contract

#### WI-06: Define agent profile and profile resolution model

Design how agents are composed from discovered capabilities for a given context.

Initial contexts should at least include:

- general
- model
- knowledge

Expected outcome:

- runtime can resolve a context-bound agent from installed capability sets

#### WI-07: Define planner and execution-loop contracts

Define the bounded agentic loop for `v3`, including:

- plan creation
- step execution
- observation of tool results
- replanning/continuation
- clarification pause/resume

Expected outcome:

- `v3` has a genuine agentic runtime shape from the start

This work item should explicitly include:

- max iteration budget
- max tool-call budget
- timeout and cancellation behavior
- duplicate-step / duplicate-tool-call detection
- clarification pause and resume rules
- terminal outcomes and terminal reason codes

#### WI-08: Define tool contract and tool execution boundary

Design the tool abstraction used by capabilities and the runtime.

Expected outcome:

- tools become reusable, capability-scoped building blocks
- runtime can execute tools while remaining independent from specific tool implementations

#### WI-09: Define run state model

Design the state carried through an agent run, including:

- current plan
- completed steps
- pending clarification
- artifacts
- streamed user-visible progress state

Expected outcome:

- runtime can support multi-step execution without reconstructing execution from chat text

#### WI-10: Add LangChain4j adapter layer

Integrate LangChain4j in a dedicated `v3` adapter layer for:

- model calls
- structured outputs
- tool calling
- streamed LLM output

Expected outcome:

- `v3` uses LangChain4j productively without letting it define the architecture

#### WI-11: Create scenario-based and `testIT` validation approach

Create a `v3`-specific scenario/testing approach for:

- capability discovery
- streaming event behavior
- planner behavior
- end-to-end bounded agent runs

Preferably, this should include `testIT` coverage that exercises real LLM-backed flows for
the Hello World milestone.

Expected outcome:

- `v3` can be validated by scenarios and integration tests rather than only low-level unit
  tests

This work item should not rely only on live-model integration.

It should also define a deterministic validation harness with:

- scripted planner outputs
- scripted tool results
- exact event-trace assertions
- loop termination assertions
- reusable scenario fixtures shared with later `testIT`
### 11.2 Hello World / Platform Validation Agent

#### WI-12: Define minimal validation capability set

Define the minimal capability set needed for architecture validation.

This should likely include:

- a minimal `conversation` capability
- a minimal demo/action capability
- a minimal streamed output/protocol definition

The validation capability set should collectively exercise all major parts of the capability
architecture in minimal form:

- descriptors
- prompts
- tools
- protocols
- discovery metadata

Expected outcome:

- the first `v3` agent can be built with almost no domain/tooling complexity

#### WI-13: Define Hello World agent profile

Define the first concrete agent profile using the minimal validation capability set.

Expected outcome:

- the runtime can assemble an agent profile from discovered capabilities

#### WI-14: Define bounded validation workflow

Define the first bounded agentic workflow used by the Hello World agent.

The workflow should be simple, but it must still exercise:

- planning
- tool selection or direct action
- streamed progress
- final answer completion

Prefer low-value or no-op tools so the workflow validates runtime behavior rather than domain
logic.

Expected outcome:

- the platform proves a real, minimal agentic loop

#### WI-15: Define validation streaming sequence

Define the exact streaming lifecycle for the Hello World agent, including:

- run started
- progress/thinking events
- tool call/result if applicable
- final message streaming/completion

Expected outcome:

- streaming behavior can be validated independently from domain complexity

#### WI-16: Add real-LLM `testIT` coverage for Hello World agent

Prepare integration coverage for the Hello World milestone using real LLM calls where
possible.

This item is intended to validate:

- LangChain4j fit
- streaming behavior under real model integration
- end-to-end capability composition

Expected outcome:

- the first implementation item validates the architecture under realistic runtime conditions

### 11.3 Schema Exploration Agent

#### WI-17: Define workflow-validation agent scope

Define the scope of the first post-Hello-World workflow agent.

This agent should validate planner/observer orchestration in a simple schema-oriented domain
without requiring SQL execution.

Expected outcome:

- the next milestone after Hello World is clearly defined as a workflow-validation milestone

#### WI-18: Define planner responsibilities for schema exploration

Define how the planner creates the initial multi-step inspection plan for schema exploration
questions.

Expected outcome:

- the planner role is explicit and testable in the first real workflow example

#### WI-19: Define observer responsibilities for schema exploration

Define how the observer interprets intermediate tool results and chooses whether to:

- continue
- branch
- summarize
- ask clarification

Expected outcome:

- the observer role is explicit and separate from raw tool execution

#### WI-20: Define schema exploration tool set

Define the low-risk tool set needed for the workflow-validation agent, likely including:

- inspect schema entity
- inspect relations
- inspect metadata completeness

Expected outcome:

- the first planner/observer workflow can be built without SQL-heavy dependencies

#### WI-21: Define schema exploration end-to-end scenarios

Define scenarios that demonstrate the planner/observer workflow, such as:

- entity is well documented
- entity has sparse metadata
- entity has complex relations
- entity is ambiguous and requires clarification

Expected outcome:

- the first workflow-validation agent can be exercised end to end

### 11.4 Schema Exploration Agent

#### WI-22: Define the Schema capability

Define the first outward capability focused on schema exploration.

The public agent-facing abstraction should hide internal separation between physical schema
and schema-bound metadata.

Expected outcome:

- one coherent schema capability surface for the LLM/runtime

#### WI-23: Define schema tool set

Design the first schema tool set, likely including operations equivalent to:

- list schema entities
- inspect schema entity
- inspect relations
- search schema
- retrieve merged schema description

Expected outcome:

- minimal but useful schema tool surface for explain-style workflows

#### WI-24: Build schema data aggregation boundary

Design the adapter/service boundary that combines:

- physical schema
- descriptive metadata
- relation metadata
- schema-bound rules or mappings where relevant

Expected outcome:

- schema tools can return unified outputs without exposing internal subsystem boundaries

#### WI-25: Define Schema Exploration Agent profile

Define the first concrete agent profile using:

- conversation capability
- schema capability

Bound it to model-context usage first, while keeping it extensible to general chat later.

Expected outcome:

- first real `v3` agent profile assembled from capabilities

#### WI-26: Define explain workflow for schema questions

Define the bounded agentic workflow for the first explain agent.

Typical flow:

- inspect request/context
- inspect schema entities
- inspect relations or descriptions as needed
- stream explanation as evidence is gathered

Expected outcome:

- a narrow but real agentic workflow that proves planning, tool usage, and streaming

#### WI-27: Define exploration-oriented streaming UX

Define the exact event sequence expected from the Schema Exploration Agent, including:

- early progress feedback
- schema inspection/tool events
- partial explanation deltas
- final explanation completion

Expected outcome:

- the first domain explain agent demonstrates low-latency user-visible feedback

#### WI-28: Implement end-to-end Schema Exploration Agent scenarios

Prepare scenario coverage for representative questions such as:

- what does this table represent
- what does this column mean
- how are two entities related
- what metadata is missing for this entity

Expected outcome:

- the first domain explain agent can be evaluated as a meaningful proof of concept

---

## 12. Architectural Weaknesses and Suggestions

This section records current concerns and suggested follow-up clarifications so they remain
visible during implementation planning.

### 12.1 Planner / observer contract is still underspecified

The document establishes planner and observer as important runtime roles, but the exact
contract between them is not yet defined precisely enough.

Current risk:

- planner and observer responsibilities may blur together during implementation
- runtime logic may become ad hoc instead of following a stable loop contract

Suggestion:

- define an explicit planner/observer handoff model
- specify what the planner produces
- specify what the observer consumes
- specify what decisions the observer may make: continue, branch, clarify, stop, replan

Suggested first structured-output contract:

For the early Hello World milestone, the planner can be constrained to a very small typed
decision object instead of free-form JSON or generic maps.

Example shape:

```kotlin
data class PlannerDecision(
    val mode: Mode,
    val toolName: String? = null,
    val toolArguments: Map<String, String> = emptyMap(),
    val responseText: String? = null
) {
    enum class Mode {
        DIRECT_RESPONSE,
        CALL_TOOL
    }
}
```

Intended interpretation:

- `DIRECT_RESPONSE`
  - the model provides `responseText`
- `CALL_TOOL`
  - the model provides `toolName` and `toolArguments`

This is a good first structured LLM contract because it:

- keeps the planner output small
- makes control flow explicit
- avoids generic map-based planner responses
- still supports both direct and tool-using paths

### 12.2 Capability scope could become too broad

Capabilities are expected to include prompts, tools, protocols, policies, descriptors, and
future MCP-facing assets.

Current risk:

- capabilities may become overly large or internally unstructured
- “capability” could turn into a catch-all package instead of a disciplined building block

Suggestion:

- define a clear internal capability package structure
- separate descriptor assets, prompt assets, tools, protocol assets, and policies within the
  capability model

### 12.3 Runtime event model vs capability protocol model needs clearer separation

The design correctly distinguishes streaming runtime behavior and capability-defined output
contracts, but the boundary is not yet fully explicit.

Current risk:

- event schemas and protocol schemas may overlap or duplicate each other
- the streaming contract may become inconsistent across capabilities

Suggestion:

- define the layering explicitly:
  - runtime event envelope
  - capability protocol payloads
  - artifact schemas
- decide which parts are universal runtime concerns and which belong to individual capabilities

### 12.4 Capability discovery needs compatibility/versioning rules

Dynamic discovery is a core design decision, but the document does not yet define how
capability compatibility and versioning should be handled.

Current risk:

- future capability evolution may create conflicts between providers, protocol versions, or
  exposed resources

Suggestion:

- introduce versioning expectations for:
  - capability descriptors
  - protocols
  - prompt assets
  - externally exposed resources

Short clarification:

- versioning alone is not enough; discovery also needs admission/trust rules so the runtime can
  distinguish compatible capabilities from allowed capabilities

### 12.5 Public vs internal capability contracts are not fully separated yet

The design correctly aims for MCP-friendly capability exposure, but it does not yet specify
which elements are intended as stable public contracts and which remain internal runtime
details.

Current risk:

- internal implementation details may accidentally become external compatibility obligations

Suggestion:

- define which capability assets are considered public/stable
- define which are internal and may change without external compatibility guarantees

### 12.6 Run-state ownership and persistence boundaries need clarification

The architecture assumes a multi-step runtime with observer/planner state, but the document is
still light on what state is:

- ephemeral
- persisted
- replayable
- capability-local vs runtime-owned

Current risk:

- agent runs may become hard to resume, replay, or inspect consistently later

Suggestion:

- add a dedicated state-ownership section covering:
  - runtime-owned run state
  - capability-local state
  - persisted artifacts
  - replay/reconstruction expectations

Short clarification:

- **ephemeral state** is temporary in-memory execution state that exists only for the active
  run, such as token buffers, temporary step-local scratch values, and transient execution
  context
- **durable state** is state that must survive process restarts or later inspection, such as
  run status, completed steps, clarification pause state, artifact references, and persisted
  conversation/run records

Not all state in an agentic workflow needs to be durable. The most important durable state is
usually workflow state rather than raw model-internal state.

### 12.7 Hello World milestone could remain too shallow if not constrained carefully

The Hello World milestone is correctly defined as low business value and high architectural
completeness, but it could still become too shallow if it validates mostly text generation and
streaming.

Current risk:

- the milestone may not sufficiently prove structured inputs, structured outputs, or protocol
  usage

Suggestion:

- require the Hello World milestone to include:
  - at least one structured tool input
  - at least one structured tool result
  - at least one protocol-defined streamed payload

### 12.8 Overall assessment

The overall direction is strong:

- milestone sequencing is good
- capability composition is the right architectural center
- stream-first design is appropriate
- MCP-friendliness is strategically valuable

The main remaining architectural risk is not direction but under-specification of key
contracts.

The most important next clarifications are:

1. planner/observer contract
2. event/protocol/artifact layering
3. capability package structure
4. versioning/public-contract boundaries
5. state ownership and persistence model
6. capability trust/admission/authorization
7. loop safety and termination guards
8. deterministic validation harness

Define the exact event sequence expected from the Schema Exploration Agent, including:

- early progress feedback
- schema inspection/tool events
- partial explanation deltas
- final explanation completion

Expected outcome:

- the first agent demonstrates low-latency user-visible feedback

#### WI-18: Implement end-to-end Schema Exploration Agent scenarios

Prepare scenario coverage for representative questions such as:

- what does this table represent
- what does this column mean
- how are two entities related
- what metadata is missing for this entity

Expected outcome:

- the first agent can be evaluated as a meaningful proof of concept

### 11.3 Suggested Delivery Order

Recommended implementation sequence:

1. WI-01 to WI-05
2. WI-06 to WI-11
3. WI-12 to WI-17
4. WI-18

This sequence prioritizes:

- stable platform foundations first
- first agent second
- scenario proof last
