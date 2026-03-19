# Agentic Runtime v3 - Developer Manual

**Status:** Active  
**Date:** March 19, 2026  
**Audience:** Developers building agents, profiles, capabilities, persistence adapters, and runtime integrations on top of `ai/v3`

---

## 1. Purpose

This manual treats `ai/v3` as a construction framework for chat and agent runtimes.

It is not a user guide for the current demo agents. It is a framework guide for developers who
need to:

- add a new capability
- bind tools and protocols
- compose an agent profile
- route events into transcript, artifacts, memory, and telemetry
- wire persistence into a runtime or service
- test and debug new agents safely

The current `ai/v3` baseline already includes:

- `CapabilityProvider` discovery through `ServiceLoader`
- manifest-driven prompts, tools, and protocols
- agent profiles with profile-owned routing policy
- LangChain4j-backed runtime loops
- routed events with one-to-many projection
- separate chat transcript, chat memory, artifact, and telemetry lanes
- post-persist artifact observers

This manual explains how to use that baseline as a framework.

---

## 2. Mental Model

At the highest level, `ai/v3` is built from five layers:

1. Capability layer  
   A capability is a passive package of prompts, tools, and protocols.

2. Profile layer  
   A profile selects capability ids and owns routing policy.

3. Runtime layer  
   A runtime executes the tool loop, emits raw `AgentEvent`s, and resolves capabilities from the profile.

4. Routing and projection layer  
   Raw events are converted into `RoutedAgentEvent`s, then projected into transcript, artifacts, run events, telemetry, and downstream observers.

5. Adapter layer  
   CLI, future REST/SSE wiring, JPA adapters, and external consumers sit on top of the runtime and persistence ports.

The key design rule is:

> Capabilities define what an agent can do.  
> Profiles define which capabilities are active and how runtime events should be interpreted.  
> Runtimes execute.  
> Projectors persist.  
> Observers react after persistence.

---

## 3. Current Building Blocks

### 3.1 Core contracts

Important framework contracts in `ai/mill-ai-v3`:

- `Capability`
- `CapabilityProvider`
- `CapabilityRegistry`
- `CapabilityManifest`
- `AgentProfile`
- `AgentEvent`
- `AgentEventRouter`
- `RoutedAgentEvent`
- `ConversationStore`
- `ArtifactStore`
- `RunEventStore`
- `ChatMemoryStore`
- `ArtifactObserver`

### 3.2 Current runtime implementations

Today there are two concrete runtime entry points:

- `LangChain4jAgent`
  General profile-driven runtime for a resolved `AgentProfile`
- `SchemaExplorationAgent`
  Schema-facing runtime that currently exposes the combined `schema-authoring` profile

Both follow the same broad shape:

1. create `runId`
2. persist the user turn into `ConversationStore`
3. emit `AgentEvent.RunStarted`
4. resolve capabilities and build prompt/tool surfaces
5. execute the tool loop
6. emit raw runtime events
7. route each raw event into one or more routed events
8. publish routed events to projectors and listeners

### 3.3 Current persistence context

`AgentPersistenceContext` is the standard in-memory wiring bundle. It currently groups:

- `RunEventStore`
- `ConversationStore`
- `ArtifactStore`
- `ActiveArtifactPointerStore`
- `AgentEventPublisher`
- `RunTelemetryAccumulator`
- `ArtifactObserver` list

For framework development, this is the easiest default context to use in tests and local runs.

---

## 4. Framework Lifecycle

This is the lifecycle a framework developer should keep in mind:

1. Create or reuse capabilities
2. Expose them through `CapabilityProvider`
3. Register the provider in `META-INF/services/io.qpointz.mill.ai.CapabilityProvider`
4. Compose an `AgentProfile`
5. Decide whether the default runtime is enough or a custom runtime is needed
6. Define routing policy overrides if the profile needs artifact pointers or custom persistence behavior
7. Run through the standard event pipeline
8. Add tests for:
   - capability binding
   - runtime loop behavior
   - routed event mapping
   - persistence projection
   - artifact observer effects

---

## 5. When To Add What

### 5.1 Add a new capability when

- you have a reusable prompt/tool/protocol package
- the logic should be available to more than one agent profile
- the tool family has a clear conceptual boundary

Examples:

- `schema`
- `sql-query`
- `value-mapping`
- `schema-authoring`

### 5.2 Add a new profile when

- the capability set changes
- routing semantics change
- artifact pointer policy changes
- the chat-facing behavior should be different even with the same capabilities

Examples:

- `hello-world`
- `schema-authoring`

### 5.3 Add a new runtime when

- the execution loop is genuinely different
- capability resolution needs special dependency wiring
- there is a distinct system-prompt assembly or loop budget
- the profile alone is not enough to express the behavior

Do not create a new runtime just because the tool list differs. That is what profiles are for.

### 5.4 Add a new persistence adapter when

- you need durable transcript/artifact/event storage
- you need to move beyond the in-memory baseline
- you are wiring `ai/v3` into a service boundary

The contract belongs in `mill-ai-v3`. Durable adapters belong elsewhere.

---

## 6. Recommended Reading Order

If you are new to `ai/v3`, read the manual in this order:

1. this file
2. `v3-developer-capabilities-profiles-and-dependencies.md`
3. `v3-developer-runtime-events-persistence.md`
4. `v3-developer-recipes.md`
5. `v3-developer-testing-and-debugging.md`

Supporting references:

- `v3-capability-manifest.md`
- `v3-persistence-lanes.md`
- `v3-conversation-persistence.md`
- `v3-interactive-cli.md`

---

## 7. The Authoritative Data Lanes

One of the easiest mistakes when extending `ai/v3` is to confuse transcript, memory, artifacts,
and telemetry. They are not the same thing.

### 7.1 Chat transcript

Authority: `ConversationStore`

Use for:

- canonical chat reconstruction
- durable user/assistant turn history
- Chat API transcript views

Do not use for:

- tool plumbing
- token deltas
- usage counters
- machine-readable artifacts

### 7.2 Model memory

Authority: `ChatMemoryStore` plus `LlmMemoryStrategy`

Use for:

- projecting a bounded or transformed message history back into the model

Do not assume:

- it is identical to transcript
- it is complete
- it is the right source for conversation reconstruction

### 7.3 Artifacts

Authority: `ArtifactStore`

Use for:

- durable machine-readable outputs
- SQL artifacts
- schema captures
- future structured outputs consumed by downstream systems

Artifacts can be attached to a transcript turn, but they are not transcript turns.

### 7.4 Telemetry

Authority:

- `RunEventStore` for selected persisted events
- `RunTelemetryAccumulator` for in-memory per-run counters

Use for:

- token counts
- tool call counts
- run lifecycle markers
- operational diagnostics

### 7.5 Artifact observers

Authority: post-persist hook only

Use for:

- indexing requests
- downstream relation extraction
- notifications
- asynchronous enrichment

Do not use observers as a substitute for persistence. They are downstream and best-effort.

---

## 8. A Minimal End-to-End Example

The smallest useful `ai/v3` extension usually looks like this:

1. add `capabilities/my-capability.yaml`
2. create `MyCapabilityProvider : CapabilityProvider`
3. bind tool handlers with `manifest.tool(...)`
4. create `MyAgentProfile`
5. instantiate `LangChain4jAgent(profile = MyAgentProfile.profile, ...)`
6. optionally override routing policy for artifact pointers
7. validate behavior in the CLI or unit tests

Example sketch:

```kotlin
object MyAgentProfile {
    val profile = AgentProfile(
        id = "my-agent",
        capabilityIds = setOf("conversation", "my-capability"),
    )
}

class MyCapabilityProvider : CapabilityProvider {
    override fun descriptor(): CapabilityDescriptor = CapabilityDescriptor(
        id = "my-capability",
        name = "My Capability",
        description = "Demo capability",
        supportedContexts = setOf("general"),
    )

    override fun create(
        context: AgentContext,
        dependencies: CapabilityDependencies,
    ): Capability = MyCapability(descriptor())
}

private data class MyCapability(
    override val descriptor: CapabilityDescriptor,
) : Capability {
    private val manifest = CapabilityManifest.load("capabilities/my-capability.yaml")

    override val prompts = manifest.allPrompts
    override val protocols = manifest.allProtocols
    override val tools = listOf(
        manifest.tool("ping") {
            ToolResult(mapOf("status" to "ok"))
        }
    )
}
```

This is enough to make the capability discoverable and usable by a profile-driven runtime.

---

## 9. Common Design Rules

### 9.1 Keep capabilities passive

A capability should contribute declarative assets and tool bindings. It should not own the
runtime loop.

### 9.2 Keep profiles small and honest

A profile is a capability-set definition plus routing policy. Do not hide major runtime
behavior behind a misleading profile name.

### 9.3 Keep router logic stateless

If you need accumulation, do it in projectors or listeners, not in the router.

### 9.4 Use structured payloads end to end

`ToolResult.result`, protocol payloads, routed event `content`, and artifact `payload` should be
consumer-safe structured values, not nested JSON strings.

### 9.5 Use one-to-many routing when needed

One raw event may create multiple routed events.

Current example:

- a canonical artifact-bearing `tool.result` can produce:
  - normal telemetry/chat-stream `tool.result`
  - additional artifact-routed event such as `sql.result`

### 9.6 Transcript is the chat-facing authority

`ConversationSession.messages` is still an in-memory helper for runtime loops, but durable chat
reconstruction belongs to `ConversationStore`.

### 9.7 Artifact observers are never on the correctness path

If an observer fails, persistence must still remain correct.

---

## 10. Current Limits And Caveats

The framework is close to pre-final state, but there are still known limitations developers
should understand:

- the CLI is still a manual inspection tool, not a production API
- `/clear` in the CLI clears `ConversationSession` and `ChatMemoryStore`, but does not yet fully
  reset the in-memory persistence context
- `SchemaExplorationAgent` is exposed as the `schema-authoring` runtime surface
- some strategic docs still describe future layers not yet implemented
- durable JPA adapters and Spring autoconfiguration now exist through
  `mill-ai-v3-persistence` and `mill-ai-v3-autoconfigure`

These are not blockers for framework development, but they matter when writing service or UX
integrations.

---

## 11. What To Build Next

If you are starting a new agent family, the normal build order is:

1. define the capability boundaries
2. write manifests
3. write capability providers
4. add profile
5. run in CLI with in-memory persistence
6. lock routing and artifact semantics
7. add service wiring
8. add durable adapters

Do not start from REST or UX wiring first. Start from the framework contracts and event flow.

---

## 12. Manual Map

The rest of the manual set is split by responsibility:

- `v3-developer-capabilities-profiles-and-dependencies.md`
  How to build capabilities, providers, manifests, dependencies, and profiles
- `v3-developer-runtime-events-persistence.md`
  How runtime execution, routed events, transcript, artifacts, telemetry, and observers work together
- `v3-developer-recipes.md`
  Practical implementation recipes for adding tools, capabilities, profiles, artifact types, and observers
- `v3-developer-testing-and-debugging.md`
  Testing strategy and debugging playbook for ai/v3 framework development
