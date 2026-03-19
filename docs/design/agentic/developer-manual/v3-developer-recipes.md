# Agentic Runtime v3 - Developer Recipes

**Status:** Active  
**Date:** March 19, 2026

---

## 1. Purpose

This chapter is intentionally practical. It contains implementation recipes for the work developers
actually do in `ai/v3`.

Use it when you want to answer:

- how do I add a new tool?
- how do I create a new profile?
- how do I make something persist as an artifact?
- how do I attach artifacts to a chat turn?
- how do I plug in a custom observer?

---

## 2. Recipe: Add A Read-Only Tool To An Existing Capability

Example target: add `list_indexes` to the `schema` capability.

### Steps

1. Add the tool declaration to `capabilities/schema.yaml`
2. Add the input/output schema
3. Add a typed args class in `SchemaCapability`
4. Bind the handler with `manifest.tool("list_indexes")`
5. Return a structured object or map
6. Add a test for handler binding and output shape

### Example manifest addition

```yaml
tools:
  list_indexes:
    description: Return all indexes for a given table.
    input:
      type: object
      required:
        - schemaName
        - tableName
      properties:
        schemaName:
          type: string
          description: Exact schema name.
        tableName:
          type: string
          description: Exact table name.
    output:
      type: array
      items:
        type: object
        properties:
          indexName:
            type: string
            description: Index name.
```

### Example binding

```kotlin
private data class ListIndexesArgs(
    val schemaName: String,
    val tableName: String,
)

manifest.tool("list_indexes") { request ->
    val args = request.argumentsAs<ListIndexesArgs>()
    ToolResult(listIndexes(service, args.schemaName, args.tableName))
}
```

---

## 3. Recipe: Add A New Capability From Scratch

Example target: `metrics`

### Steps

1. Create `capabilities/metrics.yaml`
2. Create `MetricsCapabilityDependency` if external collaborators are needed
3. Create `MetricsCapabilityProvider`
4. Create `MetricsCapability`
5. Register provider in `META-INF/services`
6. Add the capability id to a profile
7. Validate with `CapabilityRegistry.load()`

### Sanity checklist

- manifest `name` matches descriptor id
- provider is registered
- `supportedContexts` includes the runtime context you use
- required dependencies are declared and supplied

---

## 4. Recipe: Create A Profile That Reuses Existing Capabilities

Example target: query-only data agent

### Goal

Use schema grounding, dialect help, and SQL execution, but no metadata authoring.

### Example

```kotlin
object QueryAgentProfile {
    val profile = AgentProfile(
        id = "query-agent",
        capabilityIds = setOf(
            "conversation",
            "schema",
            "sql-dialect",
            "sql-query",
            "value-mapping",
        ),
    )
}
```

### When this is enough

This is enough when:

- runtime loop can stay the same
- only capability set changes
- default routing policy is acceptable

---

## 5. Recipe: Add Profile-Specific Artifact Pointers

Example target: preserve latest generated chart config as `last-chart-config`.

### Steps

1. Identify the event type that produces the artifact
2. Override the profile rule for that event type
3. Set `artifactPointerKeys`

### Example

```kotlin
object ChartAgentProfile {
    private val routingPolicy = DefaultEventRoutingPolicy.policy.overriding(
        requireNotNull(DefaultEventRoutingPolicy.policy.ruleFor("protocol.final")).copy(
            artifactPointerKeys = setOf("last-chart-config"),
        )
    )

    val profile = AgentProfile(
        id = "chart-agent",
        capabilityIds = setOf("conversation", "charting"),
        routingPolicy = routingPolicy,
    )
}
```

### When not to use this

Do not use a single pointer key when multiple artifact kinds must stay separately addressable.
In that case use multiple keys or a more specific routed event kind.

---

## 6. Recipe: Make A Tool Result Persist As An Artifact

This is the pattern used when the raw event is still `tool.result`, but the result is a canonical artifact.

### Example case

`execute_sql` returns:

```json
{
  "artifactType": "sql-result",
  "statementId": "1",
  "resultId": "mock-1"
}
```

### Current pattern

1. tool handler returns structured result with `artifactType`
2. router inspects `AgentEvent.ToolResult.result`
3. router emits an extra artifact-routed event
4. projector persists the artifact
5. observer receives the persisted artifact

### Important rule

Do not teach the projector to inspect arbitrary tool results ad hoc. Artifact promotion belongs in routing.

---

## 7. Recipe: Attach Multiple Artifacts To One Chat Turn

Example target:

- one assistant response
- one SQL artifact
- one chart config artifact

### Pattern

1. keep a single assistant `turnId` for the run
2. ensure all related artifact events carry that `turnId`
3. persist artifacts independently
4. attach artifact ids to the owning turn through `ConversationStore.attachArtifacts(...)`

### Why this is correct

The chat transcript should show one chat item. Artifacts are side effects of that item.

### What to avoid

- making every artifact a separate transcript turn
- relying on insertion ordering only
- inventing a one-off “composite event” when turn attachment already solves the problem

---

## 8. Recipe: Add A Custom Artifact Observer

Example target: start a relation indexer.

### Minimal implementation

```kotlin
class RelationIndexObserver : ArtifactObserver {
    override fun onArtifactCreated(artifact: ArtifactRecord) {
        val kind = artifact.kind
        if (kind != "schema-authoring.capture") return
        // derive relations asynchronously or enqueue work
    }
}
```

### Wiring

```kotlin
val persistence = AgentPersistenceContext(
    artifactObservers = listOf(
        NoOpArtifactObserver(),
        RelationIndexObserver(),
    )
)
```

### Design rules

- observers must be best-effort
- observers consume persisted artifacts, not raw tool outputs
- observers should not mutate transcript correctness

---

## 9. Recipe: Instantiate A Profile-Driven Agent In Tests

```kotlin
val registry = CapabilityRegistry.load()
val persistence = AgentPersistenceContext()
val memory = InMemoryChatMemoryStore()

val agent = LangChain4jAgent(
    model = fakeModel,
    profile = MyAgentProfile.profile,
    registry = registry,
    chatMemoryStore = memory,
    persistenceContext = persistence,
)
```

Use this when:

- your profile uses the standard runtime
- you want in-memory transcript/artifact/event state
- you want to assert over routing and persistence outcomes

---

## 10. Recipe: Build A Custom Runtime

Create a new runtime only if execution semantics differ materially.

### Skeleton

```kotlin
class MyCustomAgent(
    private val model: StreamingChatModel,
    private val profile: AgentProfile,
    private val registry: CapabilityRegistry = CapabilityRegistry.load(),
    private val persistenceContext: AgentPersistenceContext = AgentPersistenceContext(),
) {
    fun run(input: String, session: ConversationSession, listener: (AgentEvent) -> Unit = {}): String {
        val runId = UUID.randomUUID().toString()
        val assistantTurnId = UUID.randomUUID().toString()

        val routedListener: (AgentEvent) -> Unit = { event ->
            listener(event)
            DefaultAgentEventRouter.route(
                AgentEventRoutingInput(
                    event = event,
                    policy = profile.routingPolicy,
                    conversationId = session.conversationId,
                    runId = runId,
                    profileId = profile.id,
                    turnId = assistantTurnId,
                )
            ).forEach(persistenceContext.publisher::publish)
        }

        // custom run logic here
        routedListener(AgentEvent.RunStarted(profile.id))
        return ""
    }
}
```

### Do not forget

- `runId` is per `run()` call, not per conversation
- transcript user turn should be appended before execution
- assistant `turnId` should be stable across the run

---

## 11. Recipe: Add A New Chat-Facing Live Event

Example target: `progress.delta`

### Steps

1. add new `AgentEvent` subtype
2. add routing rule
3. decide whether it is:
   - `CHAT_STREAM` only
   - `CHAT_STREAM` plus `TELEMETRY`
4. update CLI rendering if needed
5. add tests

### Rule of thumb

If the event is useful live but should not be replayed as conversation history, make it `CHAT_STREAM` only.

---

## 12. Recipe: Add A New Durable Artifact Type

Example target: `chart-config`

### Steps

1. define the structured payload contract
2. decide whether it is produced by:
   - protocol output
   - canonical artifact-bearing tool result
3. route it into `ARTIFACT`
4. decide pointer keys
5. add projector and observer tests

### Payload guidance

Keep artifact payloads machine-readable:

- prefer maps or typed objects that serialize to maps cleanly
- include a stable `artifactType`
- avoid nested stringified JSON

---

## 13. Recipe: Use Chat Memory Without Polluting Transcript

Pattern:

1. keep canonical transcript in `ConversationStore`
2. load memory through `ChatMemoryStore`
3. use `LlmMemoryStrategy` to project the subset sent back to the model
4. save only the messages the strategy should retain

Use this when:

- model context should be bounded
- transcript must remain durable and complete

---

## 14. Recipe: Prepare For REST Wiring

If you are building a service adapter next, make sure your framework-level implementation already has:

1. stable profile id
2. durable transcript shape
3. artifact semantics defined
4. live event stream semantics defined
5. persistence context abstraction in place

The service layer should adapt these contracts, not reinvent them.

---

## 15. Common Failure Cases

- provider not registered in `META-INF/services`
- manifest resource path wrong
- dependency declared but not supplied in `AgentContext`
- `supportedContexts` excludes your runtime context
- tool returns stringified JSON instead of structured content
- profile forgets routing override for needed pointer keys
- observer expected to fire even though no artifact was actually persisted
