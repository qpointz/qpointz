# Agentic Runtime v3 — Capability Manifest

**Status:** Active
**Date:** March 13, 2026
**Scope:** `ai/v3` capability declaration format and `CapabilityManifest` Kotlin class

---

## 1. Purpose

A `CapabilityManifest` is the declarative contract for a single `v3` capability.

It is loaded from one YAML resource file per capability and provides:

- all tool names, descriptions, and schemas
- all prompt assets (id, description, content)
- the Kotlin API to bind handlers to declared tools

Handlers are the **only** imperative part supplied in code. Everything textual lives in the YAML.

This separation allows prompt engineers to iterate on descriptions without recompiling.

---

## 2. Design Rules

### 2.1 One file per capability

Each capability has exactly one manifest file. All tools and prompts for that capability
are declared in that file, not split across multiple resources.

```
src/main/resources/capabilities/schema.yaml       ← schema capability
src/main/resources/capabilities/conversation.yaml ← conversation capability
src/main/resources/capabilities/demo.yaml         ← demo capability
```

### 2.2 Tools are owned by capabilities, not shared

Tools must not be shared across capabilities. If two capabilities need the same tool
behaviour, the capability itself should be shared — the agent profile includes both
capabilities, and the tool comes along as part of that capability.

Designing for tool sharing is the wrong abstraction level.

### 2.3 Tool name is the map key

Tool name is the key in the `tools` map, not a `name:` field inside the tool block.
This avoids redundancy and makes the manifest easier to scan.

### 2.4 Manifest resource lives in the capability module

YAML resources live in `src/main/resources/capabilities/` of the module that owns the
capability (currently `mill-ai-v3-capabilities`).

They must not live in `mill-ai-v3-core`.

---

## 3. YAML Schema Reference

### 3.1 Top-level structure

```yaml
name: <string>           # Required. Capability id. Must match CapabilityDescriptor.id.
description: <string>    # Required. Short capability description.

prompts:                 # Optional. Map of prompt assets.
  <prompt-id>:
    description: <string>
    content: <string>

tools:                   # Optional. Map of tool declarations.
  <tool-name>:
    description: <string>
    input: <ToolSchema>  # Optional. Omit for tools with no input.
    output: <ToolSchema> # Optional. Omit for tools with no output (defaults to empty object).
```

### 3.2 ToolSchema

A `ToolSchema` block is recursive. Every node has:

```yaml
type: <string>           # Required. One of: string, integer, number, boolean, object, array.
description: <string>    # Optional. Shown to the LLM in the tool contract.
```

Additional fields by type:

#### object

```yaml
type: object
description: <string>
additionalProperties: false   # Optional, default false.
required:                     # Optional. List of required property names.
  - fieldName
properties:
  <fieldName>:
    <ToolSchema>              # Recursive. Any type.
```

Properties not listed in `required` are treated as optional.
By default, if `required` is omitted, all properties are considered required.

#### array

```yaml
type: array
description: <string>
items:
  <ToolSchema>               # Required. Describes the element type.
```

#### string

```yaml
type: string
description: <string>
enum:                        # Optional. Restricts valid values. See section 3.3.
  - VALUE_A
  - VALUE_B
```

#### integer / number / boolean

```yaml
type: integer   # or number, boolean
description: <string>
```

### 3.3 Enum constraint on string fields

The `enum` list on a `string` field restricts valid values at the schema level.
Use this for fields that accept only a known set of named values, such as direction
flags or cardinality codes.

```yaml
direction:
  type: string
  description: "OUTBOUND (this table is source), INBOUND (this table is target), BOTH (all)."
  enum:
    - INBOUND
    - OUTBOUND
    - BOTH
```

The `enum` constraint is only valid on `string` fields. The runtime and LangChain4j
adapter will surface it as an enumeration constraint in the tool contract sent to the LLM.

> **Note:** `enum` support in `ToolSchema` is planned. Until it is implemented, use a
> `description` to communicate valid values.

### 3.4 Prompt entry

```yaml
prompts:
  <prompt-id>:
    description: <string>   # Required. Short description of the prompt's purpose.
    content: <string>       # Required. Full prompt text (can be multi-line with YAML |).
```

The `<prompt-id>` should be namespaced by capability, e.g. `schema.system`,
`conversation.progress`. This avoids collisions when multiple capability prompts are
assembled into an agent profile.

---

## 4. Full Example — Schema Capability

```yaml
name: schema
description: Schema exploration capability

prompts:
  schema.system:
    description: Guidance for schema exploration tools.
    content: |
      Use schema exploration tools to discover and describe the data platform structure.
      Always call list_schemas first when you do not yet know which schemas exist.
      Use the returned names exactly as provided in subsequent tool calls.

tools:
  list_schemas:
    description: |
      Return all schemas available in the data platform.
      Call this first when you do not yet know which schemas exist.
      An empty description means no business metadata has been provided for that schema.
    output:
      type: array
      description: All available schemas.
      items:
        type: object
        description: A single schema entry.
        properties:
          schemaName:
            type: string
            description: Exact schema name to use in subsequent tool calls.
          description:
            type: string
            description: Business description of the schema, or empty if no metadata is available.

  list_tables:
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
        description: A single table entry.
        properties:
          schemaName:
            type: string
            description: Schema this table belongs to.
          tableName:
            type: string
            description: Exact table name to use in subsequent tool calls.
          description:
            type: string
            description: Business description of the table, or empty if no metadata is available.

  list_columns:
    description: |
      Return all columns for a given table within a schema.
      Call list_tables first if you do not know the table name.
    input:
      type: object
      description: Identifies the table to list columns for.
      properties:
        schemaName:
          type: string
          description: Exact schema name as returned by list_schemas.
        tableName:
          type: string
          description: Exact table name as returned by list_tables.
    output:
      type: array
      description: All columns within the requested table.
      items:
        type: object
        description: A single column entry.
        properties:
          schemaName:
            type: string
            description: Schema this column belongs to.
          tableName:
            type: string
            description: Table this column belongs to.
          columnName:
            type: string
            description: Exact column name.
          description:
            type: string
            description: Business description of the column, or empty if no metadata is available.
          nullable:
            type: string
            description: Nullability of the column (NULLABLE, NOT_NULL, or NOT_SPECIFIED_NULL).
          type:
            type: string
            description: Logical data type identifier (e.g. INTEGER, VARCHAR, TIMESTAMP).

  list_relations:
    description: |
      Return relations (foreign key / join paths) associated with a given table.
      Use direction to filter: OUTBOUND (this table is the source), INBOUND (this table is
      the target), BOTH (all relations).
    input:
      type: object
      description: Identifies the table and direction of relations to list.
      properties:
        schemaName:
          type: string
          description: Exact schema name as returned by list_schemas.
        tableName:
          type: string
          description: Exact table name as returned by list_tables.
        direction:
          type: string
          description: "OUTBOUND (source), INBOUND (target), or BOTH."
          enum:
            - INBOUND
            - OUTBOUND
            - BOTH
    output:
      type: array
      description: Relations associated with the requested table.
      items:
        type: object
        description: A single relation entry.
        properties:
          sourceSchema:
            type: string
            description: Schema of the source table.
          sourceTable:
            type: string
            description: Name of the source table.
          sourceAttributes:
            type: array
            description: Source join column names.
            items:
              type: string
              description: Column name.
          targetSchema:
            type: string
            description: Schema of the target table.
          targetTable:
            type: string
            description: Name of the target table.
          targetAttributes:
            type: array
            description: Target join column names.
            items:
              type: string
              description: Column name.
          name:
            type: string
            description: Relation name.
          description:
            type: string
            description: Business description of the relation.
          cardinality:
            type: string
            description: Relation cardinality (e.g. ONE_TO_MANY, MANY_TO_ONE, ONE_TO_ONE).
```

---

## 5. Minimal Example — Prompts Only (no tools)

```yaml
name: conversation
description: Minimal conversation capability skeleton for ai/v3.

prompts:
  conversation.system:
    description: Minimal system guidance for user-facing conversation.
    content: Be concise, user-facing, and stream progress without exposing hidden reasoning.

  conversation.progress:
    description: Guidance for short progress narration during streaming runs.
    content: When streaming progress, prefer short factual updates such as 'thinking'.
```

---

## 6. Kotlin API

### 6.1 Loading

```kotlin
val manifest = CapabilityManifest.load("capabilities/schema.yaml")
```

Load once — typically at `Capability` construction time (captured in the instance).

The resource path is classpath-relative. The classloader used is
`Thread.currentThread().contextClassLoader`.

### 6.2 Binding tool handlers

```kotlin
override val tools: List<ToolDefinition> = listOf(
    manifest.tool("list_schemas") {
        ToolResult(listSchemas(svc))
    },
    manifest.tool("list_tables") { request ->
        val args = request.argumentsAs<ListTablesArgs>()
        ToolResult(listTables(svc, args.schemaName))
    },
)
```

`manifest.tool(name, handler)` returns a `ToolDefinition` with the name, description,
`inputSchema`, and `outputSchema` taken from the YAML. The handler is the only
code-supplied part. Throws if `name` is not declared in the manifest.

### 6.3 Accessing prompts

```kotlin
// All prompts as PromptAsset list — use for Capability.prompts
override val prompts: List<PromptAsset> = manifest.allPrompts

// Single prompt by id
val systemPrompt: PromptAsset = manifest.promptAsset("schema.system")
```

`manifest.allPrompts` returns all declared prompts as `PromptAsset` instances in
declaration order.

`manifest.promptAsset(id)` returns one prompt by id. Throws if not declared.

### 6.4 Type-safe argument extraction

Use `request.argumentsAs<T>()` (from `ToolRequestExtensions.kt`) to deserialize tool
arguments into a typed data class:

```kotlin
data class ListRelationsArgs(
    val schemaName: String,
    val tableName: String,
    val direction: RelationDirection = RelationDirection.BOTH,
)

manifest.tool("list_relations") { request ->
    val args = request.argumentsAs<ListRelationsArgs>()
    ToolResult(listRelations(svc, args.schemaName, args.tableName, args.direction))
}
```

Jackson `convertValue` is used — no JSON string round-trip. Enum fields deserialize from
their name string (`"INBOUND"` → `RelationDirection.INBOUND`). Missing required fields
(no default) throw `IllegalArgumentException`.

---

## 7. Class Location

| Class | Module | File |
|-------|--------|------|
| `CapabilityManifest` | `mill-ai-v3-core` | `io/qpointz/mill/ai/CapabilityManifest.kt` |
| `ToolRequestExtensions` (argumentsAs) | `mill-ai-v3-core` | `io/qpointz/mill/ai/ToolRequestExtensions.kt` |

YAML resources live in the module that owns the capability:

| Capability | Module | Resource path |
|------------|--------|---------------|
| `schema` | `mill-ai-v3-capabilities` | `capabilities/schema.yaml` |
| `demo` | `mill-ai-v3-capabilities` | `capabilities/demo.yaml` |
| `conversation` | `mill-ai-v3-capabilities` | `capabilities/conversation.yaml` |

---

## 8. Recipes

Common YAML patterns ready to copy.

### 8.1 Tool with no input

```yaml
tools:
  ping:
    description: Health check — returns a fixed marker.
    output:
      type: object
      properties:
        status:
          type: string
          description: Always "ok".
```

```kotlin
manifest.tool("ping") { ToolResult(mapOf("status" to "ok")) }
```

### 8.2 Tool with simple string input

```yaml
tools:
  greet:
    description: Return a greeting for the given name.
    input:
      type: object
      properties:
        name:
          type: string
          description: Name to greet.
    output:
      type: object
      properties:
        greeting:
          type: string
          description: Rendered greeting.
```

```kotlin
data class GreetArgs(val name: String)

manifest.tool("greet") { request ->
    val args = request.argumentsAs<GreetArgs>()
    ToolResult(mapOf("greeting" to "Hello, ${args.name}!"))
}
```

### 8.3 Enum-constrained string input

Use when only a fixed set of values is valid. LangChain4j will surface this as an enum
constraint to the model.

```yaml
    input:
      type: object
      properties:
        direction:
          type: string
          description: "OUTBOUND (source), INBOUND (target), or BOTH."
          enum:
            - INBOUND
            - OUTBOUND
            - BOTH
```

```kotlin
data class Args(val direction: MyDirection = MyDirection.BOTH)

manifest.tool("list_relations") { request ->
    val args = request.argumentsAs<Args>()
    // Jackson converts "INBOUND" → MyDirection.INBOUND automatically
    ToolResult(...)
}
```

### 8.4 Required vs optional fields

Fields listed under `required:` are required. All others are optional. When `required:` is
absent, all fields default to required.

```yaml
    input:
      type: object
      required:
        - schemaName
      properties:
        schemaName:
          type: string
          description: Required — must be supplied.
        filter:
          type: string
          description: Optional — may be omitted.
```

### 8.5 All scalar types

```yaml
    input:
      type: object
      properties:
        label:
          type: string
          description: A text label.
        count:
          type: integer
          description: A whole number count.
        ratio:
          type: number
          description: A floating-point ratio.
        enabled:
          type: boolean
          description: A true/false flag.
```

### 8.6 Array of strings output

```yaml
    output:
      type: array
      description: List of schema names.
      items:
        type: string
        description: A single schema name.
```

### 8.7 Array of objects output

```yaml
    output:
      type: array
      description: List of table entries.
      items:
        type: object
        description: A single table entry.
        properties:
          tableName:
            type: string
            description: Exact table name.
          description:
            type: string
            description: Business description, or empty.
```

### 8.8 Nested object in input

```yaml
    input:
      type: object
      properties:
        filter:
          type: object
          description: Optional filter criteria.
          properties:
            schemaName:
              type: string
              description: Restrict to this schema.
            tableNamePrefix:
              type: string
              description: Restrict to tables whose name starts with this prefix.
```

### 8.9 Array of arrays (matrix / grouped output)

```yaml
    output:
      type: array
      description: Groups of related items.
      items:
        type: array
        description: One group.
        items:
          type: string
          description: An item in the group.
```

### 8.10 Prompts-only capability (no tools)

Omit the `tools:` block entirely. `manifest.allPrompts` still works.

```yaml
name: conversation
description: Conversation guidance prompts.

prompts:
  conversation.system:
    description: Core system prompt.
    content: Be concise and stream progress without exposing hidden reasoning.

  conversation.progress:
    description: Short progress narration guidance.
    content: Prefer short factual updates such as 'thinking' or 'checking schema'.
```

```kotlin
override val prompts: List<PromptAsset> = manifest.allPrompts
override val tools: List<ToolDefinition> = emptyList()
```

---

## 9. Defects to Watch For

- Splitting one capability across multiple YAML files (violates one-file-per-capability rule)
- Placing YAML resources in `mill-ai-v3-core` instead of the owning capability module
- Defining a tool in the manifest but not binding a handler (the tool silently disappears
  from `Capability.tools`)
- Sharing a tool definition between capabilities — share the capability instead
- Fetching dependencies from `request.context` inside handlers — inject at construction time
  and close over them in the lambda
