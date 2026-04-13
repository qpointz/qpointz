# Agentic Runtime v3 - Capabilities, Profiles, And Dependencies

**Status:** Active  
**Date:** March 19, 2026

---

## 1. Purpose

This chapter explains how to build the top half of an `ai/v3` agent:

- capability manifests
- capability providers
- capability dependencies
- profiles
- capability discovery

If you are adding a new agent family, this is the first chapter to implement from.

---

## 2. Core Concepts

### 2.1 Capability

A `Capability` is a passive package of:

- `prompts`
- `tools`
- `protocols`

It does not execute the workflow itself.

Current contract:

```kotlin
interface Capability {
    val descriptor: CapabilityDescriptor
    val prompts: List<PromptAsset>
    val tools: List<ToolBinding>
    val protocols: List<ProtocolDefinition>
}
```

### 2.2 CapabilityProvider

A `CapabilityProvider` is the factory discovered by the runtime.

It is responsible for:

- exposing the descriptor
- validating required dependencies through `CapabilityDescriptor.requiredDependencies`
- creating a concrete `Capability`

Current contract:

```kotlin
interface CapabilityProvider {
    fun descriptor(): CapabilityDescriptor
    fun create(
        context: AgentContext,
        dependencies: CapabilityDependencies = CapabilityDependencies.empty(),
    ): Capability
}
```

### 2.3 CapabilityRegistry

`CapabilityRegistry` is the lookup and materialization layer.

It can:

- enumerate descriptors
- filter providers by `contextType`
- instantiate all capabilities for a context
- instantiate the exact capability set required by a profile

The registry is usually loaded through `CapabilityRegistry.load()`, which uses `ServiceLoader`.

---

## 3. Directory Layout

The current convention is:

- Kotlin providers and handlers:
  `ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/capabilities/...`
- YAML manifests:
  `ai/mill-ai-v3/src/main/resources/capabilities/...`
- service registration:
  `ai/mill-ai-v3/src/main/resources/META-INF/services/io.qpointz.mill.ai.core.capability.CapabilityProvider`

Examples already present:

- `conversation`
- `schema`
- `schema-authoring`
- `sql-dialect`
- `sql-query`
- `value-mapping`

---

## 4. How To Create A Capability

### 4.1 Step 1: choose the capability boundary

A capability should have one coherent responsibility.

Good boundaries:

- schema inspection
- SQL validation and execution
- schema metadata authoring
- value mapping

Bad boundaries:

- "all data things"
- "everything query related and also some metadata and maybe clarification"

If a tool family is reusable, make it a capability.

### 4.2 Step 2: create the manifest

Each capability gets one YAML manifest.

Example:

```yaml
name: my-capability
description: Demo capability

prompts:
  my-capability.system:
    description: Core behavior
    content: |
      Use the provided tools to answer the user.

tools:
  ping:
    description: Return a health marker.
    output:
      type: object
      properties:
        status:
          type: string
          description: Always "ok".
```

The manifest is the home for:

- prompt text
- tool descriptions
- tool schemas
- protocol definitions

Do not hardcode that text in Kotlin unless it is truly dynamic.

### 4.3 Step 3: implement the provider

Minimal provider:

```kotlin
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
```

### 4.4 Step 4: implement the capability instance

Use `CapabilityManifest.load(...)` and bind handlers with `manifest.tool(...)`.

```kotlin
private data class MyCapability(
    override val descriptor: CapabilityDescriptor,
) : Capability {

    private val manifest = CapabilityManifest.load("capabilities/my-capability.yaml")

    override val prompts: List<PromptAsset> = manifest.allPrompts
    override val protocols: List<ProtocolDefinition> = manifest.allProtocols
    override val tools: List<ToolBinding> = listOf(
        manifest.tool("ping") {
            ToolResult(mapOf("status" to "ok"))
        },
    )
}
```

### 4.5 Step 5: register the provider

Add the fully qualified class name to:

`META-INF/services/io.qpointz.mill.ai.core.capability.CapabilityProvider`

Current examples:

```text
io.qpointz.mill.ai.capabilities.ConversationCapabilityProvider
io.qpointz.mill.ai.capabilities.schema.SchemaCapabilityProvider
io.qpointz.mill.ai.capabilities.schema.SchemaAuthoringCapabilityProvider
io.qpointz.mill.ai.capabilities.sqldialect.SqlDialectCapabilityProvider
io.qpointz.mill.ai.capabilities.sqlquery.SqlQueryCapabilityProvider
io.qpointz.mill.ai.capabilities.valuemapping.ValueMappingCapabilityProvider
```

If you forget this step, `CapabilityRegistry.load()` will never find your provider.

---

## 5. Dependency Injection Pattern

### 5.1 The framework pattern

`ai/v3` keeps Spring out of the core capability contracts. Dependencies are passed through
`CapabilityDependencies`.

Pattern:

1. define a `CapabilityDependency`
2. require it in `CapabilityDescriptor.requiredDependencies`
3. pull it from `CapabilityDependencies` inside `create(...)`
4. close over it in tool handlers

### 5.2 Example: schema capability

Current pattern:

```kotlin
data class SchemaCapabilityDependency(
    val schemaFacetService: SchemaFacetService,
) : CapabilityDependency
```

Descriptor:

```kotlin
requiredDependencies = setOf(SchemaCapabilityDependency::class.java)
```

Use:

```kotlin
dependencies.require(SchemaCapabilityDependency::class.java).schemaFacetService
```

### 5.3 Example: SQL query capability

The SQL query capability depends on a **validator** only. Validated SQL and generated-SQL artifacts are emitted through capability protocols; **execution stays in the host** (chat service, CLI harness, etc.), not in the tool loop.

```kotlin
data class SqlQueryCapabilityDependency(
    val validator: SqlQueryToolHandlers.SqlValidationService,
) : CapabilityDependency
```

This keeps the capability generic and runtime-agnostic.

### 5.4 Best practices

- prefer one dependency object per capability boundary
- keep the dependency type small and explicit
- close over collaborators in handlers instead of fishing them out from request context each call
- use `AgentContext` only for run-scoped context, not general service wiring

---

## 6. Tool Binding Pattern

### 6.1 ToolBinding

A tool binding combines:

- LangChain4j `ToolSpecification`
- `ToolHandler`
- `ToolKind`
- optional `protocolId`

Current shape:

```kotlin
data class ToolBinding(
    val spec: ToolSpecification,
    val handler: ToolHandler,
    val kind: ToolKind = ToolKind.QUERY,
    val protocolId: String? = null,
)
```

### 6.2 QUERY vs CAPTURE

This distinction matters to the runtime.

`QUERY`:

- read-only or intermediate
- used to continue planning
- usually not terminal

`CAPTURE`:

- terminal side-effecting or artifact-producing tool
- runtime stops the loop and may invoke a synthesis protocol

Current real examples:

- `schema.list_tables` -> `QUERY`
- `schema-authoring.capture_description` -> `CAPTURE`
- `sql-query.validate_sql` -> `QUERY` (bounded correction loop; generated SQL is finalized via `sql-query.generated-sql` / `sql-query.validation` protocols, not an execute tool)

### 6.3 Binding from manifest

Use:

```kotlin
manifest.tool("list_tables") { request ->
    val args = request.argumentsAs<ListTablesArgs>()
    ToolResult(listTables(service, args.schemaName))
}
```

If the manifest already declares `kind`, it overrides the fallback kind passed from Kotlin.

### 6.4 Handler return values

Return structured objects or maps, not serialized JSON strings.

Good:

```kotlin
ToolResult(mapOf("artifactType" to "sql-result", "resultId" to "123"))
```

Good:

```kotlin
ToolResult(CaptureResult(...))
```

Bad:

```kotlin
ToolResult("{\"artifactType\":\"sql-result\"}")
```

---

## 7. Protocol Pattern

Protocols are capability-owned output contracts.

Current protocol modes:

- `TEXT`
- `STRUCTURED_FINAL`
- `STRUCTURED_STREAM`

Use a protocol when:

- the output shape matters
- the artifact needs schema validation
- the runtime should produce a stable event contract

Current examples:

- `conversation.stream`
- `schema-authoring.capture`
- `sql-query.generated-sql`
- `sql-query.validation`

Important rule:

Protocols describe output. They do not replace tools. Tools do work; protocols validate and shape the output of a phase.

---

## 8. How To Compose A Profile

An `AgentProfile` is intentionally small:

```kotlin
data class AgentProfile(
    val id: String,
    val capabilityIds: Set<String>,
    val routingPolicy: EventRoutingPolicy = DefaultEventRoutingPolicy.policy,
)
```

Profiles answer two questions:

1. Which capabilities are active?
2. How should raw runtime events be routed?

### 8.1 Minimal profile

```kotlin
object MyAgentProfile {
    val profile = AgentProfile(
        id = "my-agent",
        capabilityIds = setOf("conversation", "my-capability"),
    )
}
```

### 8.2 Profile with routing override

Current real example:

```kotlin
object SchemaAuthoringAgentProfile {
    private val routingPolicy = DefaultEventRoutingPolicy.policy.overriding(
        requireNotNull(DefaultEventRoutingPolicy.policy.ruleFor("protocol.final")).copy(
            artifactPointerKeys = setOf("last-schema-capture"),
        )
    )

    val profile = AgentProfile(
        id = "schema-authoring",
        capabilityIds = setOf(
            "conversation",
            "schema",
            "schema-authoring",
            "sql-dialect",
            "sql-query",
            "value-mapping",
        ),
        routingPolicy = routingPolicy,
    )
}
```

Use profile overrides for:

- pointer keys
- artifact persistence decisions
- profile-specific lane semantics

Do not create a new runtime just to tweak pointer keys.

---

## 9. Capability Selection Recipes

### 9.1 Conversation-only agent

Use:

- `conversation`
- one small domain capability

Example:

```kotlin
capabilityIds = setOf("conversation", "demo")
```

### 9.2 Data agent with schema grounding

Use:

- `conversation`
- `schema`
- `sql-dialect`
- `sql-query`
- optionally `value-mapping`

### 9.3 Schema authoring agent

Use:

- `conversation`
- `schema`
- `schema-authoring`
- `sql-dialect`
- `sql-query`
- `value-mapping`

This is the current `schema-authoring` profile.

---

## 10. Anti-Patterns

### 10.1 Putting all prompt text in Kotlin

This makes prompt changes expensive and harder to review.

### 10.2 Reaching into tool request context for global services

Prefer capability dependencies injected at creation time.

### 10.3 Treating profiles as marketing names only

Profile id is operational. It affects routing, persistence, and identity.

### 10.4 Creating one huge capability

This destroys reuse and makes profiles meaningless.

### 10.5 Making tools share implicit contracts

If a tool returns a structured artifact, define that shape clearly and consistently. Do not rely on vague ad hoc maps.

---

## 11. Checklist For Adding A New Capability

1. Create the YAML manifest
2. Create the dependency type if needed
3. Create the provider
4. Create the capability instance
5. Bind tools from the manifest
6. Register the provider with `ServiceLoader`
7. Add the capability id to a profile
8. Add tests for manifest loading and handler behavior
9. Validate in a runtime with real or mock dependencies

---

## 12. Related Documents

- `v3-capability-manifest.md`
- `v3-developer-runtime-events-persistence.md`
- `v3-developer-recipes.md`
