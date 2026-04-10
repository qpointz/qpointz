# PLAN - AI v3 Schema Exploration Agent

Status: `active`
Owner: `user-implemented, assistant-supervised`
Last updated: `2026-03-13`

## Purpose

This plan is for implementing the first `ai/v3` Schema Exploration agent in a way that helps the
implementer learn the architecture, understand class roles, and expose architectural defects early.

The user will implement each step and then ask the assistant to validate the result before moving
to the next step.

This file is intended to be sufficient for cold start on another computer or by another agent.

## Implementation Goal

Build the first `ai/v3` Schema Exploration agent on top of a reusable data-domain boundary that:

- preserves all physical schema elements from `SchemaProvider`
- attaches metadata facets without making metadata the source of truth
- exposes reusable `*WithFacets` objects for both AI and UI consumers

The agent should be bounded, stream-first, and schema-focused. It should not execute SQL.

## Agreed Architecture

### Core boundary

- Module: `data/mill-data-schema-core`
- Language: Kotlin
- Spring: no Spring in core
- Service: `SchemaFacetService`
- Model:
  - `SchemaWithFacets`
  - `SchemaTableWithFacets`
  - `SchemaAttributeWithFacets`

### Wiring

- Spring wiring belongs in `data/mill-data-autoconfigure`

### Sources of truth

- Physical schema source:
  - `data/mill-data-backend-core/src/main/java/io/qpointz/mill/data/backend/SchemaProvider.java`
- Metadata source:
  - `metadata/mill-metadata-core`
  - especially `MetadataRepository`, `MetadataEntity`, and facet types

### Hard boundary rule

`ai/v3` must not implement its own physical-plus-metadata merge logic.

It should consume `SchemaFacetService`.

## Architectural Decisions Made During Implementation

### ToolSchemaField — no description field

`ToolSchemaField` does not have a `description` field. Description belongs on `ToolSchema` only,
avoiding redundancy. Field-level descriptions go on the schema:

```kotlin
ToolSchemaField("schemaName", ToolSchema.string(description = "Exact schema name..."))
```

### Tool definitions — YAML-based

Tool `name`, `description`, `inputSchema`, and `outputSchema` are declared in YAML resources.
The handler is the only imperative part supplied in code. This separation allows prompt engineers
to iterate on descriptions without recompiling.

Two factory methods on `ToolDefinition.Companion` (in `ToolDefinitionLoader.kt`):

```kotlin
// Full schema declared in YAML (types, structure, descriptions all from file)
ToolDefinition.fromFullSchema("capabilities/schema/list_tables.yaml") { request -> ... }

// Descriptions in YAML, schema structure in code
ToolDefinition.fromDescription("capabilities/schema/list_tables.yaml",
    inputSchema = ToolSchema.obj(...),
    outputSchema = ToolSchema.array(...)
) { request -> ... }
```

YAML format for `fromFullSchema`:

```yaml
name: list_tables
description: |
  Return all tables within a given schema.
  Call list_schemas first if you do not know the schema name.
input:
  type: object
  description: Identifies the schema to list tables for.
  properties:
    schemaName:
      type: string
      description: Exact schema name as returned by list_schemas.
output:
  type: array
  description: All tables within the requested schema.
  items:
    type: object
    properties:
      tableName:
        type: string
        description: Exact table name to use in subsequent tool calls.
```

YAML resources live in `src/main/resources/capabilities/<capability>/` of the module that owns
the capability (currently `mill-ai-v3-capabilities`).

### argumentsAs — type-safe argument extraction

`ToolRequest` has an extension function `argumentsAs<T>()` in `ToolRequestExtensions.kt`
(`mill-ai-v3-core`) that converts `request.arguments: Map<String, Any?>` to a data class using
Jackson `convertValue`. No JSON round-trip. Missing required fields throw `IllegalArgumentException`.

```kotlin
data class ListTablesArgs(val schemaName: String)

handler = ToolHandler { request ->
    val args = request.argumentsAs<ListTablesArgs>()
    ToolResult(listTables(svc, args.schemaName))
}
```

### Dependency injection in capabilities

`SchemaCapabilityProvider.create()` must extract the service from `dependencies`, not from
`request.context`. The service is captured at construction time and closed over in handlers:

```kotlin
override fun create(context: AgentContext, dependencies: CapabilityDependencies): Capability =
    SchemaCapability(descriptor(), dependencies.require(SchemaCapabilityDependency::class.java).schemaFacetService)
```

`request.context` (`ToolExecutionContext`) is a separate service-locator bag — do not use it
for capability dependencies.

### SchemaToolHandlers — pure handler logic

`SchemaToolHandlers` is a plain `object` with pure functions that take `SchemaFacetService` as
a parameter. This separates testable domain logic from the `ToolDefinition` wiring in
`SchemaCapability`. No capability or runtime concerns in `SchemaToolHandlers`.

### RelationDirection — no constructor parameter

`RelationDirection` is a plain enum with no constructor parameter. Jackson serializes enums as
their name string by default (`INBOUND`, `OUTBOUND`, `BOTH`). No annotation needed.

### Test conventions

Tests in `mill-ai-v3-capabilities` and `data/mill-data-schema-core` use `mockito-kotlin`:
- `whenever(mock.method()).thenReturn(value)` instead of `Mockito.\`when\``
- `@ExtendWith(MockitoExtension::class)` handles mock lifecycle — no manual `reset()` needed

## Current State Snapshot (2026-03-13)

### data-domain files (stable)

- `data/mill-data-schema-core/src/main/kotlin/io/qpointz/mill/data/schema/SchemaFacetService.kt`
- `data/mill-data-schema-core/src/main/kotlin/io/qpointz/mill/data/schema/SchemaFacetServiceImpl.kt`
- `data/mill-data-schema-core/src/main/kotlin/io/qpointz/mill/data/schema/SchemaWithFacets.kt`
- `data/mill-data-schema-core/src/main/kotlin/io/qpointz/mill/data/schema/SchemaTableWithFacets.kt`
- `data/mill-data-schema-core/src/main/kotlin/io/qpointz/mill/data/schema/SchemaAttributeWithFacets.kt`
- `data/mill-data-schema-core/src/main/kotlin/io/qpointz/mill/data/schema/SchemaFacets.kt`
- `data/mill-data-schema-core/src/main/kotlin/io/qpointz/mill/data/schema/WithFacets.kt`

### data-domain tests (stable)

- `data/mill-data-schema-core/src/test/kotlin/io/qpointz/mill/data/schema/SchemaFacetServiceImplTest.kt`
  — uses `mockito-kotlin`, covers all merge scenarios and facet attachment
- `data/mill-data-schema-core/src/testIT/kotlin/io/qpointz/mill/data/schema/SchemaFacetServiceSkyMillIT.kt`

### ai/v3 core — new files

- `ai/mill-ai-v3-core/src/main/kotlin/io/qpointz/mill/ai/ToolDefinitionLoader.kt`
  — `fromFullSchema` and `fromDescription` factory extensions on `ToolDefinition.Companion`
- `ai/mill-ai-v3-core/src/main/kotlin/io/qpointz/mill/ai/ToolRequestExtensions.kt`
  — `argumentsAs<T>()` extension on `ToolRequest`
- `ai/mill-ai-v3-core/src/main/kotlin/io/qpointz/mill/ai/ToolDefinition.kt`
  — `ToolDefinition` now has `companion object`; `ToolSchemaField` has no `description` field

### ai/v3 capabilities — schema tool handlers (implemented and tested)

- `ai/mill-ai-v3-capabilities/src/main/kotlin/io/qpointz/mill/ai/capabilities/schema/SchemaToolHandlers.kt`
  — `listSchemas`, `listTables`, `listColumns`, `listRelations` (with `RelationDirection` filter)
- `ai/mill-ai-v3-capabilities/src/test/kotlin/io/qpointz/mill/ai/capabilities/schema/SchemaToolHandlersTest.kt`
  — full coverage including direction filtering and field mapping

### ai/v3 capabilities — SchemaCapability (partially wired)

- `ai/mill-ai-v3-capabilities/src/main/kotlin/io/qpointz/mill/ai/capabilities/schema/SchemaCapability.kt`
  — `SchemaCapabilityProvider`, `SchemaCapabilityDependency`, `SchemaCapability`
  — currently wires: `list_schemas`, `list_tables` via `fromFullSchema`
  — NOT YET wired: `list_columns`, `list_relations`
  — NOT YET registered in `META-INF/services`

### ai/v3 capabilities — YAML resources (partial)

- `ai/mill-ai-v3-capabilities/src/main/resources/capabilities/schema/list_schemas.yaml`
- `ai/mill-ai-v3-capabilities/src/main/resources/capabilities/schema/list_tables.yaml`
- NOT YET created: `list_columns.yaml`, `list_relations.yaml`

### Existing AI v3 reference implementation (Hello World — stable)

Use Hello World as the reference for package shape and class roles:

- `ai/mill-ai-v3-core/src/main/kotlin/io/qpointz/mill/ai/Capability.kt`
- `ai/mill-ai-v3-core/src/main/kotlin/io/qpointz/mill/ai/CapabilityDescriptor.kt`
- `ai/mill-ai-v3-core/src/main/kotlin/io/qpointz/mill/ai/CapabilityRegistry.kt`
- `ai/mill-ai-v3-core/src/main/kotlin/io/qpointz/mill/ai/AgentProfile.kt`
- `ai/mill-ai-v3-core/src/main/kotlin/io/qpointz/mill/ai/AgentEvent.kt`
- `ai/mill-ai-v3-capabilities/src/main/kotlin/io/qpointz/mill/ai/capabilities/ConversationCapability.kt`
- `ai/mill-ai-v3-capabilities/src/main/kotlin/io/qpointz/mill/ai/capabilities/DemoCapability.kt`
- `ai/mill-ai-v3-langchain4j/src/main/kotlin/io/qpointz/mill/ai/langchain4j/OpenAiHelloWorldAgent.kt`

### Existing fixtures

- Physical model: `test/skymill.yaml`
- Physical dataset family: `test/datasets/skymill/`
- Metadata facets: `test/datasets/skymill/skymill-meta-repository.yaml`

## Relevant Work Items

Implementation order:

1. `WI-062-ai-v3-schema-data-aggregation-boundary.md`
2. `WI-061-ai-v3-schema-tool-set.md`
3. `WI-060-ai-v3-schema-capability.md`
4. `WI-063-ai-v3-schema-exploration-agent-profile.md`
5. `WI-064-ai-v3-schema-exploration-workflow.md`
6. `WI-065-ai-v3-schema-exploration-streaming-ux.md`
7. `WI-066-ai-v3-schema-exploration-scenarios.md`

Reference/scope support:

- `WI-058-ai-v3-schema-exploration-tool-set.md`
- `WI-059-ai-v3-schema-exploration-scenarios.md`

The earlier scope/planner/observer stub work items (`WI-055`, `WI-056`, `WI-057`) were later
folded into this plan and the implemented schema exploration/runtime docs.

## Supervisor Protocol

After each step:

1. implement the step
2. run the relevant tests locally
3. ask the assistant to validate the step
4. include:
   - changed files
   - tests run
   - anything uncertain or intentionally deferred

Expected validation style from the assistant:

- review for architectural drift
- review class-role clarity
- review test adequacy
- identify risks and missing cases before next step starts

## Step Plan

## Step 1 - Review and lock the data-domain boundary

Status: `completed`

What was done:

- All `SchemaFacetService` and `*WithFacets` classes reviewed
- Boundary confirmed sound: physical schema preserved, metadata attached without becoming source of truth
- `SchemaFacetServiceImplTest` migrated to `mockito-kotlin` (`whenever` instead of `Mockito.\`when\``)
- Test fixture bugs fixed (duplicate table name, redundant `reset()`, missing `listColumns` test)
- `mockito-kotlin` added to `data/mill-data-schema-core` test dependencies

## Step 2 - Harden the schema boundary only if needed

Status: `completed`

What was done:

- Boundary reviewed, no structural changes needed
- Tests hardened — fixture extended with `RelationFacet` data for direction filtering tests
- `listRelations` direction tests cover OUTBOUND, INBOUND, BOTH by name (not just count)
- `createRelation` bug fixed: was using `EntityReference.attribute` for join columns instead
  of `Relation.sourceAttributes` / `Relation.targetAttributes`

## Step 3 - Define the AI tool surface on top of the boundary

Status: `completed`

What was done:

- `SchemaToolHandlers` implemented as a pure `object` with testable functions:
  - `listSchemas(svc)` → `List<ListSchemasItem>`
  - `listTables(svc, schemaName)` → `List<ListTablesItem>`
  - `listColumns(svc, schemaName, tableName)` → `List<ListColumnsItem>`
  - `listRelations(svc, schemaName, tableName, direction)` → `List<ListRelationsItem>`
- `RelationDirection` enum: `INBOUND`, `OUTBOUND`, `BOTH` (no constructor parameter needed)
- `ListRelationsItem` exposes both endpoints explicitly (`sourceSchema`, `sourceTable`,
  `targetSchema`, `targetTable`) — direction is derivable from the data
- `SchemaToolHandlersTest` covers all handlers with full field-mapping assertions

## Step 4 - Implement the schema capability in `ai/v3`

Status: `in-progress`

What is done:

- `SchemaCapabilityProvider` and `SchemaCapabilityDependency` implemented
- `SchemaCapability` wires `list_schemas` and `list_tables` via `fromFullSchema`
- YAML resources created for both wired tools
- Dependency injection correct: service extracted from `dependencies.require()` in `create()`,
  closed over in handlers — `request.context` is NOT used
- `argumentsAs<T>()` extension available for type-safe argument extraction
- `ToolDefinitionLoader` provides `fromFullSchema` and `fromDescription` factory methods
- `ToolSchemaField` has no `description` field — description lives on `ToolSchema`

What remains before Step 4 is complete:

- [ ] Create `list_columns.yaml` and wire `listColumns` in `SchemaCapability`
- [ ] Create `list_relations.yaml` and wire `listRelations` in `SchemaCapability`
  (note: `RelationDirection` parameter needs to be accepted from tool input)
- [ ] Register `SchemaCapabilityProvider` in
  `META-INF/services/io.qpointz.mill.ai.CapabilityProvider`
- [ ] Add unit tests for capability registration and tool loading

Validation request:

- "Validate Step 4 of PLAN-ai-v3-schema-exploration-agent"

## Step 5 - Add the Schema Exploration agent profile

Status: `pending`

Goal:

- define the concrete profile that composes conversation + schema capability

Likely location:

- `ai/mill-ai-v3-core`

Done when:

- profile id and required capability ids are explicit
- profile does not embed workflow logic

Validation request:

- "Validate Step 5 of PLAN-ai-v3-schema-exploration-agent"

## Step 6 - Implement the bounded exploration workflow

Status: `pending`

Goal:

- create the first bounded agent flow for schema exploration

Expected behavior:

- inspect request
- choose target entity or ask clarification
- gather evidence through one or more schema tools
- stop when enough evidence exists or clarification is required

Rules:

- no SQL generation
- no hidden reasoning leakage
- user-visible progress should be progress/evidence oriented

Done when:

- one bounded path works end to end
- planner, executor, and observer responsibilities are still distinguishable

Validation request:

- "Validate Step 6 of PLAN-ai-v3-schema-exploration-agent"

## Step 7 - Add streaming UX and event integration

Status: `pending`

Goal:

- make the exploration flow stream useful progress and evidence

Expected stream shape:

- run started
- progress update
- tool call
- tool result
- partial answer or evidence delta
- completion

Reference:

- `AgentEvent` and Hello World streaming in `OpenAiHelloWorldAgent.kt`

Done when:

- event sequence is consistent with `WI-065`
- stream content is user-safe

Validation request:

- "Validate Step 7 of PLAN-ai-v3-schema-exploration-agent"

## Step 8 - Add tests for the AI capability and agent

Status: `pending`

Goal:

- cover the new AI layer without duplicating `SchemaFacetService` boundary tests

Recommended coverage:

- unit tests for capability registration and tool definitions
- scenario tests for exploration paths
- integration tests for representative schema exploration on top of skymill-backed boundary

Keep test split clear:

- `mill-data-schema-core` proves merge correctness
- `ai/v3` proves capability, workflow, and event behavior

Validation request:

- "Validate Step 8 of PLAN-ai-v3-schema-exploration-agent"

## Architectural Defects To Watch For

These are common failure modes the assistant should keep checking for:

- AI code bypasses `SchemaFacetService` and reimplements merge logic
- `mill-data-schema-core` starts taking Spring or AI-specific dependencies
- tool contracts leak metadata internals instead of domain-friendly schema exploration outputs
- planner, observer, and executor logic collapse into one hard-to-test class
- event stream exposes chain-of-thought instead of progress/evidence
- AI tests duplicate low-level merge correctness instead of testing AI behavior
- capability handlers fetch dependencies from `request.context` instead of constructor injection
- `ToolSchemaField` gains a `description` field (it must not — description belongs on `ToolSchema`)
- YAML tool resources placed in `mill-ai-v3-core` instead of the capability module that owns them

## Suggested Commands

Run from repository root:

```powershell
.\gradlew.bat :data:mill-data-schema-core:test
.\gradlew.bat :data:mill-data-schema-core:testIT
.\gradlew.bat :ai:mill-ai-v3-capabilities:test
.\gradlew.bat :ai:mill-ai-v3-core:test
```

Run from `ai/` directory:

```bash
./gradlew :mill-ai-v3-capabilities:test
./gradlew :mill-ai-v3-core:test
```

## Notes For Cold Start Validator

When validating on another machine or in another session:

- read this plan first — it contains all architectural decisions made so far
- read the "Architectural Decisions Made During Implementation" section carefully
- inspect current `data/mill-data-schema-core` for boundary stability
- inspect `SchemaToolHandlers.kt` for the current tool surface
- inspect `SchemaCapability.kt` for wired tools and what is missing
- inspect YAML resources under `mill-ai-v3-capabilities/src/main/resources/capabilities/schema/`
- inspect Hello World `ai/v3` implementation for runtime shape reference
- validate step-by-step, not by jumping to the final desired architecture

The purpose is not only to finish the feature. The purpose is also to teach the implementer the
architecture and catch defects while the design is still easy to change.
